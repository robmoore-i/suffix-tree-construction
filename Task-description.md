## Task Description

Please implement a library for full-text search.

This library is supposed to consist of two parts: text index builder and search
query executor.

Text index builder should:

- Be able to build a text index for a given folder in a file system.
- Show progress while building the index.
- Build the index using several threads in parallel.
- Be cancellable. It should be possible to interrupt indexing.
- (Optional) Be incremental. It would be nice if the builder would be able to
  listen to the file system changes and update the index accordingly.

Search query executor should:

- Find a position in files for a given string.
- Be able to process search requests in parallel.

Please also cover the library with a set of unit-tests. Your code should not use
third-party indexing libraries. To implement the library, you can use any JVM
languages and any build systems, but we would appreciate you choosing Kotlin and
Gradle. We don't set a deadline from our side but would appreciate if you would
be able to estimate the time needed and would set a deadline yourself and send
it to us before starting the work on the task.

Should you have any questions, we would be happy to answer them.