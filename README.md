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


