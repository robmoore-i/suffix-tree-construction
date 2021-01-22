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

## Testing

### Running Tests

- `./gradlew lib:test` runs the unit tests for the library.
- `./gradlew lib:performanceTest` runs some performance tests for the library,
  although these depend on having the input data cloned, using the script
  `scripts/fetch-performance-test-data.sh`. If the data isn't there, the tests
  will be skipped.

#### Fuzz Tests

While working on suffix tree construction, I've made use of "fuzz testing", in
which the test generates random inputs and see if the program behaves correctly.
This test is skipped by default because it isn't fast, due to the fact that it
is generating and running thousands of strings through the code to find a short
string which reproduces any bugs.


