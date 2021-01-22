# JetBrains Test Task: Full-text search

This document is written for a reviewer of this code project, to help you use
your time more efficiently.

## Repository Layout

- `Plan.md` contains a brief implementation plan including the iterations and
  increments to the program which I've made until now.
- `Task-description` contains the original task description.
- `lib` contains the library implementing full-text search.
- `app` contains a small example program which uses the library.
- `scripts` contains a single script which you can use to fetch input
  directories from GitHub, to be used when test the full-text search
  functionality. The repositories it will clone are for the Kotlin programming
  language, the Kotlin web-site, and also a small Java project from my GitHub.

## Mapping to requirements

#### Indexing of a directory

To model an index of a directory, I have a class called an `IndexedDirectory`.
This class has methods for performing queries on the underlying directory. It
does this by internally holding a collection of indexes - one for each file.
This is an interface called `IndexedFile`. These objects answer queries for the
content in them specifically. I will cover more about the developer interface of
the library in a dedicated section, further down.

#### Showing progress

To report progress to developers using the library, I used the Observer pattern.
Developers pass in an implementation of a functional interface, and the
coroutines which are indexing the files will send the appropriate events to the
supplied Observer, such as when a file have been indexed. I like this pattern
because it is easy to extend it to support new events, it works for synchronous
and asynchronous indexing, and it is easy to write tests for it.

#### Building the index using several threads in parallel

I used coroutines, rather than threads, but I suspect it is still within your
expectations. For indexing, the parallel indexers will launch a coroutine to
build an index for each file, since the index of the directory is simply a
collection of file indexes.

#### Interrupting indexing

Since indexing is a Kotlin coroutine Job, it can be cancelled using any of the
interfaces through which you can cancel a normal coroutine. You can see an
example of the cancellability in the `app` project, which uses the
`withTimeout(...) { ... }` construction as a demonstration.

#### Finding the position of strings

Once it has been built, the index responds to queries by returning the location
of substrings within the directory, including the relative path to the file they
are in, and the text offset within the file.

#### Processing search requests in parallel

Since queries are executed asynchronously, and they are read-only, I think it
should be trivial to execute parallel queries, although there aren't examples of
this in either the example `app` project, or the unit tests.

## Indexes: Suffix trees vs naive

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
construction. This ran in cubic time and was quite impractical. My aim is now to
iterate into a fast, correct implementation of Ukkonen's algorithm, and that is
what I have been doing. As of Friday evening (the deadline I set for myself) my
Ukkonen's algorithm implementation is still not correct, but it is now a
personal matter for me with this algorithm :) so I won't give up until it's done
and it's fast.

## Developer Interface

The library's developer interface consists of a few different objects. I
developed this interface by using the small `app` project as an example.

## Testing

### Running Tests

- `./gradlew lib:test` runs the unit tests for the library. There should be 86
  unit tests running.
- `./gradlew lib:performanceTest` runs some performance tests for the library,
  although these depend on having the input data cloned, using the script
  `scripts/fetch-performance-test-data.sh`. If the data isn't there, the tests
  will be skipped. There are 6 performance tests available.

#### Fuzz Tests

While working on suffix tree construction, I've made use of "fuzz testing", in
which the test generates random inputs and see if the program behaves correctly.
This test is skipped by default because it isn't fast, due to the fact that it
is generating and running thousands of strings through the code to find a short
string which reproduces any bugs.


