# Full-text search

### Implementation of Ukkonen's Suffix Tree construction

See: lib/src/main/kotlin/.../index/suffixtree/SuffixTree.kt

### Application of Suffix Tree Index to full text search

See: app/src/main/kotlin/.../Main.kt

### Running Tests

- `./gradlew lib:test` runs the unit tests for the library.
- `./gradlew lib:performanceTest` runs some performance tests for the library,
  although these depend on having the input data cloned, using the script
  `scripts/fetch-performance-test-data.sh`. If the data isn't there, the tests
  will be skipped.

#### Fuzz Tests

This test is skipped by default because it isn't fast, due to the fact that it
is generating and running thousands of strings through the code to find a short
string which triggers a bug.


