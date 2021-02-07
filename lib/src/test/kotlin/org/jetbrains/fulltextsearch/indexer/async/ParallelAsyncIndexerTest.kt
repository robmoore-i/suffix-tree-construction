package org.jetbrains.fulltextsearch.indexer.async

import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.hasItems
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.jetbrains.fulltextsearch.filesystem.Directory
import org.jetbrains.fulltextsearch.index.IndexedFile
import org.jetbrains.fulltextsearch.search.IndexedDirectory
import org.jetbrains.fulltextsearch.search.QueryMatch
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.nio.file.Paths
import java.util.Collections.synchronizedList

class ParallelAsyncIndexerTest {
    private val indexer: AsyncIndexer = ParallelAsyncIndexer()

    private var indexedDirectory = IndexedDirectory(listOf())

    @Test
    internal fun `can search for a unique match in one file`() = runBlocking {
        indexer.buildIndexAsync(
            Directory(Paths.get("src/test/resources/one-file")),
            indexingProgressListener()
        ).join()

        val queryMatches = indexedDirectory.queryCaseSensitive("abracadabra")

        assertThat(queryMatches, hasSize(1))
        assertThat(
            queryMatches,
            CoreMatchers.hasItem(QueryMatch("file.txt", 38))
        )
    }

    @Test
    internal fun `can search for multiple matches in one file`() = runBlocking {
        indexer.buildIndexAsync(
            Directory(Paths.get("src/test/resources/one-file")),
            indexingProgressListener()
        ).join()

        val queryMatches = indexedDirectory.queryCaseSensitive("co")

        assertThat(queryMatches, hasSize(3))
        assertThat(
            queryMatches,
            hasItems(
                QueryMatch("file.txt", 10),
                QueryMatch("file.txt", 98),
                QueryMatch("file.txt", 144)
            )
        )
    }

    @Test
    internal fun `can search for multiple matches in multiple files`() =
        runBlocking {
            indexer.buildIndexAsync(
                Directory(Paths.get("src/test/resources/two-files")),
                indexingProgressListener()
            ).join()

            val queryMatches = indexedDirectory.queryCaseSensitive("this")

            assertThat(queryMatches, hasSize(2))
            assertThat(
                queryMatches, hasItems(
                    QueryMatch("file-1.txt", 169),
                    QueryMatch("file-2.txt", 56)
                )
            )
        }

    @Test
    internal fun `can search for matches in nested files`() = runBlocking {
        indexer.buildIndexAsync(
            Directory(Paths.get("src/test/resources/nested-files")),
            indexingProgressListener()
        ).join()

        val queryMatches = indexedDirectory.queryCaseSensitive("file")

        assertThat(queryMatches, hasSize(4))
        assertThat(
            queryMatches, hasItems(
                QueryMatch("file-1.txt", 5),
                QueryMatch("file-2.txt", 5),
                QueryMatch("file-2.txt", 61),
                QueryMatch("nested/file-3.txt", 17)
            )
        )
    }

    @Test
    internal fun `reports on each new indexed file`() = runBlocking {
        val indexedFileNames = synchronizedList(mutableListOf<String>())
        indexer.buildIndexAsync(
            Directory(Paths.get("src/test/resources/nested-files")),
            object : AsyncIndexingProgressListener {
                override fun onNewFileIndexed(indexedFile: IndexedFile) {
                    indexedFileNames.add(indexedFile.relativePath())
                }

                override fun onIndexingCompleted(indexedDirectory: IndexedDirectory) {
                    this@ParallelAsyncIndexerTest.indexedDirectory = indexedDirectory
                }
            })
            .join()

        assertThat(indexedFileNames, hasSize(3))
        assertThat(
            indexedFileNames,
            hasItems(
                "file-1.txt",
                "file-2.txt",
                "nested/file-3.txt"
            )
        )
    }

    @Test
    internal fun `reports on indexing completion`() = runBlocking {
        var indexingCompleted = false
        indexer.buildIndexAsync(
            Directory(Paths.get("src/test/resources/nested-files")),
            object : AsyncIndexingProgressListener {
                override fun onNewFileIndexed(indexedFile: IndexedFile) {
                }

                override fun onIndexingCompleted(indexedDirectory: IndexedDirectory) {
                    indexingCompleted = true
                }
            })
            .join()
        assertTrue(indexingCompleted)
    }

    /**
     * This test checks the number of active threads every time a new indexing has finished, gathers
     * that data, and asserts that the number of active threads has increased from the start of the
     * indexing to the end. This test will fail if you don't use Dispatcher.default when launching
     * indexing jobs.
     */
    @Test
    @Disabled(
        "This test currently works only for debugging purposes, and should be run " +
                "only with other tests in this test class, not with the full suite of tests. " +
                "Hence it is disabled by default."
    )
    internal fun `builds the index using multiple threads in parallel`() = runBlocking {
        val dirPath = Paths.get("src/test/resources/example-java-project")
        val threadCounts = mutableListOf<Int>()
        indexer.buildIndexAsync(
            Directory(dirPath),
            object : AsyncIndexingProgressListener {
                override fun onNewFileIndexed(indexedFile: IndexedFile) {
                    threadCounts.add(Thread.activeCount())
                }

                override fun onIndexingCompleted(indexedDirectory: IndexedDirectory) {
                }
            })
            .join()

        assertThat(threadCounts[0], lessThan(threadCounts[threadCounts.size - 1]))
        threadCounts.indices.drop(1).forEach {
            assertThat(threadCounts[it], greaterThanOrEqualTo(threadCounts[it - 1]))
        }
    }

    /**
     * Assigns the finished index to the value of the corresponding field in this class.
     */
    private fun indexingProgressListener() =
        object : AsyncIndexingProgressListener {
            override fun onNewFileIndexed(indexedFile: IndexedFile) {
            }

            override fun onIndexingCompleted(indexedDirectory: IndexedDirectory) {
                this@ParallelAsyncIndexerTest.indexedDirectory = indexedDirectory
            }
        }
}