# Full-text search

### Implementation of Ukkonen's Suffix Tree construction

See: lib/src/main/kotlin/.../index/suffixtree/SuffixTree.kt

### Application of Suffix Tree Index to full text search

See: app/src/main/kotlin/.../Main.kt

### Running Tests

- `./gradlew lib:test` runs the unit tests for the library, which includes fuzz tests (<10s run time on my machine).
- `./gradlew lib:performanceTest` runs some performance tests for the library,
  although these depend on having the input data cloned, using the script
  `scripts/fetch-performance-test-data.sh`. If the data isn't there, the tests
  will be skipped.
