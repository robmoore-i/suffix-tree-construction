# JetBrains Test Task: Full-text search

## Implementation Plan

- Implement naive full-text search. Load all the file content into memory,
  serially, and call it an index. In queries, just scan the file content.
  :white_check_mark: (12/01)

- Create an application under the same Gradle root project, which utilises the
  library to provide a CLI text search tool.

- Create a small performance test suite which evaluates the library's execution
  speed, using some not-too-large datasets from my GitHub.

- Parallel index building. Load the files into memory in separate coroutines
  which run in parallel.

- Parallel queries. Update the application so that there is a way to execute
  queries in parallel. Make sure that there are automated tests which cover this
  use-case too.

- Add some larger datasets to the performance test suite so that the behaviour
  of the progress reporting feature will be easier to see visually. We will need
  to have fast-running automated tests for this behaviour as well though.

- Report progress. Use the observer pattern to send events to the library's
  users to tell them whenever a new file has been indexed. Note, there is still
  no meaningful index.

- Update the application so that it displays updates for index building using
  the progress reporting functionality provided by the library.

- Enable interrupts. Create a mechanism for indexing to be interrupted.

- Update the application so that the user can interrupt indexing. Users should
  be able to specify that indexing will automatically interrupt after N seconds.

- Implement suffix tree construction in the library, using a standard algorithm
  like Ukkonen's. Ensure that constructed suffix trees can be queried for all
  the positions of a given substring in the string it was constructed from.

- Update the library's indexing function so that it both loads the file content
  into memory and builds a suffix tree from it.

- Update the library's query function so that it exploits the suffix tree index.
  Verify that the query performance of the library has improved.

- **First iteration complete**

- I'll plan the next iteration when the first iteration is closer to completion.
  Broadly speaking, the subsequent iterations will contain the file-watching
  requirement, developer-experience improvements to the interface, and
  performance improvements by the use of parallel algorithms for suffix tree
  construction, or even the use of another data structure if appropriate.

## Plan Changelog

- 13/01 morning : Initial planning

