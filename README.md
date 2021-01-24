# JetBrains Test Task: Full-text search

This document is written for a reviewer of this code project, to help you use
your time more efficiently.

### Table of contents

* [Repository Layout](#repository-layout)
* [Mapping to requirements](#mapping-to-requirements)
  * [Indexing of a directory](#indexing-of-a-directory)
  * [Showing progress](#showing-progress)
  * [Building the index using several threads in parallel](#building-the-index-using-several-threads-in-parallel)
  * [Interrupting indexing](#interrupting-indexing)
  * [Finding the position of strings](#finding-the-position-of-strings)
  * [Processing search requests in parallel](#processing-search-requests-in-parallel)
* [Indexes: Suffix trees vs naive substring search](#indexes-suffix-trees-vs-naive-substring-search)
* [Developer Interface](#developer-interface)
* [Testing](#testing)
  * [Running Tests](#running-tests)
  * [Fuzz Tests](#fuzz-tests)

## Repository Layout

<a id="repository-layout"></a>

- `Plan.md` contains a brief implementation plan including the iterations and
  increments to the program which I've made until now.
- `Task-description.md` contains the original task description.
- `lib` contains the library implementing full-text search.
- `app` contains a small example program which uses the library.
- `scripts` contains a single script which you can use to fetch input
  directories from GitHub, to be used when test the full-text search
  functionality. The repositories it will clone are for the Kotlin programming
  language, the Kotlin web-site, and also a small Java project from my GitHub.

## Mapping to requirements

<a name="mapping-to-requirements"></a>

#### Indexing of a directory

<a name="indexing-of-a-directory"></a>

To model an index of a directory, I have a class called an `IndexedDirectory`.
This class has methods for performing queries on the underlying directory. It
does this by internally holding a collection of indexes - one for each file.
This is an interface called `IndexedFile`. These objects answer queries for the
content in them specifically. I will cover more about the developer interface of
the library in a dedicated section, further down.

#### Showing progress

<a name="showing-progress"></a>

To report progress to developers using the library, I used the Observer pattern.
Developers pass in an implementation of a functional interface, and the
coroutines which are indexing the files will send the appropriate events to the
supplied Observer, such as when a file have been indexed. I like this pattern
because it is easy to extend it to support new events, it works for synchronous
and asynchronous indexing, and it is easy to write tests for it.

#### Building the index using several threads in parallel

<a name="building-the-index-using-several-threads-in-parallel"></a>

I used coroutines, rather than threads, but I suspect it is still within your
expectations. For indexing, the parallel indexers will launch a coroutine to
build an index for each file, since the index of the directory is simply a
collection of file indexes.

#### Interrupting indexing

<a name="interrupting-indexing"></a>

Since indexing is a Kotlin coroutine Job, it can be cancelled using any of the
interfaces through which you can cancel a normal coroutine. You can see an
example of the cancellability in the `app` project, which uses the
`withTimeout(...) { ... }` construction as a demonstration.

#### Finding the position of strings

<a name="finding-the-position-of-strings"></a>

Once it has been built, the index responds to queries by returning the location
of substrings within the directory, including the relative path to the file they
are in, and the text offset within the file.

#### Processing search requests in parallel

<a name="processing-search-requests-in-parallel"></a>

Since queries are executed asynchronously, and they are read-only, I think it
should be trivial to execute parallel queries, although there aren't examples of
this in either the example `app` project, or the unit tests.

## Indexes: Suffix trees vs naive substring search

<a name="indexes-suffix-trees-vs-naive-substring-search"></a>

First, I'll explain the concept of the "naive" index. When I refer in the code
to a "naive" index, what I am talking about is not really an index at all. It is
simply the file's content loaded into memory, with no query performance
enhancements. The reason I started with this was to be able to begin by
supporting the parallelism requirements, and to discover an interface to use the
behaviour through.

To improve performance beyond scanning the file content in parallel, I have been
working on constructing a suffix tree from the file content. I have targeted
Ukkonen's algorithm for this, because it has desirable properties, such as being
linear in the time and space of the input files. Due to the algorithm's
complexity, I started with a simpler, more naive algorithm for suffix tree
construction. This ran in cubic time and was quite impractical.

The Ukkonen's algorithm implementation is now correct, and I have refactored it
to a point that I think is good enough. I'll give an overview of the
implementation here, although the code is close-by to look at. This
implementation quite explicitly processes the string one character at a time,
which I think makes the on-line property of Ukkonen's algorithm quite clear to
the reader. It stores the inbound edge as part of a `Node` object's internal
state, rather than having an Edge class as in my earlier and much more complex
attempts. The Node class has two subclasses - `RootNode` and `LeafNode`. The
internal node doesn't have a particular subclass because it doesn't add much to
do that. I did also comment the code extensively, because the problem domain is
quite complex. There is an `ActivePoint` class which encapsulates the role of
the `ActivePoint` in providing constant time suffix additions, by creating and
exploiting suffix links. Offsets are acquired by scanning from the root node of
the tree.

## Developer Interface

<a name="developer-interface"></a>

The library's developer interface consists of a few different objects. I
developed this interface by using the small `app` project as an example.

To index a directory, developers create a `Directory` object and an indexer of
their choice. The indexer types are `SyncIndexer` and `AsyncIndexer`.

Let's assume from now on that they have chosen to use the `AsyncIndexer`.

When a developer wants to perform indexing, they pass the `Directory` to the
`AsyncIndexer` via the `buildIndexAsync` method. In order to get feedback about
the progress of indexing, and pick up the result when it's done, they also pass
in an Observer, called `AsyncIndexingProgressListener`, which is notified by
the `AsyncIndexer` about two kinds of events - when a file has been indexed, and
when the whole directory has been indexed.

Once indexing is complete, the user will have access to an implementation of the
`IndexedDirectory` class, which represents an indexed, searchable directory,
corresponding to the `Directory` that was passed in initially.

The `IndexedDirectory` supports asynchronous and synchronous queries, which are
currently always case-sensitive. In the case of asynchronous queries, developers
pass in a `QueryMatchListener`. The `IndexedDirectory` will launch coroutines to
query every one of the `IndexedFile` objects within it, and each coroutine will
report its query results to the `QueryMatchListener` as soon as it has them.

## Testing

<a name="testing"/a>

### Running Tests

<a name="running-tests"></a>

- `./gradlew lib:test` runs the unit tests for the library.
- `./gradlew lib:performanceTest` runs some performance tests for the library,
  although these depend on having the input data cloned, using the script
  `scripts/fetch-performance-test-data.sh`. If the data isn't there, the tests
  will be skipped.

#### Fuzz Tests

<a name="fuzz-tests"></a>

While working on suffix tree construction, I've made use of "fuzz testing", in
which the test generates random inputs and see if the program behaves correctly.
This test is skipped by default because it isn't fast, due to the fact that it
is generating and running thousands of strings through the code to find a short
string which reproduces any bugs.


