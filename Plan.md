# JetBrains Test Task: Full-text search

## Implementation Plan

- Implement naive full-text search. Load all the file content into memory,
  serially, and call it an index. In queries, just scan the file content.
  :white_check_mark: (12/01)

- Create an application under the same Gradle root project, which utilises the
  library to provide a CLI text search tool. :white_check_mark: (13/01)

- Create a small performance test suite which evaluates the library's execution
  speed, using some not-too-large datasets. :white_check_mark: (13/01)

- Parallel index building. Load the files into memory using coroutines which run
  in parallel. :white_check_mark: (13/01)

- Query indexed files in parallel. Search the indexes of different files in
  parallel using coroutines. :white_check_mark: (13/01)

- Parallel queries. Update the library so that there is a way to execute queries
  in parallel. Make sure that there are automated tests which cover this
  use-case too. :white_check_mark: (14/01)

- Add some larger datasets to the performance test suite so that the behaviour
  of the progress reporting feature will be easier to see visually. We will need
  to have fast-running automated tests for this behaviour as well though.
  :white_check_mark: (14/01)

- Report progress. Use the observer pattern to send events to the library's
  users to tell them whenever a new file has been indexed. Note, there is still
  no meaningful index. :white_check_mark: (14/01)

- Update the application so that it displays updates for index building using
  the progress reporting functionality provided by the library.
  :white_check_mark: (14/01)

- Enable interrupts. Create a mechanism for indexing to be interrupted.
  :white_check_mark: (14/01)

- Update the application so that the user can interrupt indexing. Users should
  be able to specify that indexing will automatically interrupt after N seconds.
  :white_check_mark: (14/01)

- Write a fuzz test suite which provides confidence that the query results from
  the suffix tree index are equal to the results for the naive index. This is to
  ensure that any bugs in the suffix tree implementation are caught as early as
  possible. :white_check_mark: (18/01)

- Implement suffix tree construction in the library, using the naive cubic time
  algorithm which is the basis for Ukkonen's linear-time construction algorithm.
  Ensure that constructed suffix trees can be queried for all the positions of a
  given substring in the string it was constructed from.
  :white_check_mark: (18/01)

- Update the library's indexing function so that for small files, it both loads
  the file content into memory and builds a suffix tree from it.
  :white_check_mark: (18/01)

- Update the library's query function so that it exploits the suffix tree index.
  Verify that the query performance of the library has improved.
  :white_check_mark: (18/01)

- Update the suffix tree construction code so that it uses Ukkonen's linear-time
  algorithm. :white_check_mark: (24/01)

- **First iteration complete**

- Find out why the launched indexing coroutines are not executing concurrently,
  and change it so that they are, but that we don't accidentally launch too many
  worker threads. :white_check_mark: (07/02)

- Change the user interface of the demo app so using it is a slightly better
  experience. Users should be able to see the number of matches, and the
  matching lines. Start by implementing a basic line scanning implementation for
  the naive index. :white_check_mark: (07/02)

- When getting the matching lines from a suffix tree index, exploit the index.

## Plan Changelog

- 13/01 morning:   Initial planning
- 18/01 morning:   Split suffix tree index integration into two iterations.
- 07/02 afternoon: Second iteration planning

