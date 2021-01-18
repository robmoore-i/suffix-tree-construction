package org.jetbrains.fulltextsearch.indexer.async

import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.hasItems
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.hasSize
import org.jetbrains.fulltextsearch.filesystem.Directory
import org.jetbrains.fulltextsearch.index.IndexedFile
import org.jetbrains.fulltextsearch.search.IndexedDirectory
import org.jetbrains.fulltextsearch.search.QueryMatch
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.nio.file.Paths
import java.util.Collections.synchronizedList

abstract class AsyncFullTextSearchTest {
    abstract fun indexerUnderTest(): AsyncIndexer

    private var indexedDirectory = IndexedDirectory(listOf())

    @Test
    internal fun `can search for a unique match in one file`() = runBlocking {
        indexerUnderTest()
            .buildIndexAsync(
                Directory(Paths.get("src/test/resources/one-file")),
                indexingProgressListener()
            )
            .join()

        val queryMatches = indexedDirectory.queryCaseSensitive("abracadabra")

        assertThat(queryMatches, hasSize(1))
        assertThat(
            queryMatches,
            CoreMatchers.hasItem(QueryMatch("file.txt", 38))
        )
    }

    @Test
    internal fun `can search for multiple matches in one file`() = runBlocking {
        indexerUnderTest()
            .buildIndexAsync(
                Directory(Paths.get("src/test/resources/one-file")),
                indexingProgressListener()
            )
            .join()

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
            indexerUnderTest()
                .buildIndexAsync(
                    Directory(Paths.get("src/test/resources/two-files")),
                    indexingProgressListener()
                )
                .join()

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
        indexerUnderTest()
            .buildIndexAsync(
                Directory(Paths.get("src/test/resources/nested-files")),
                indexingProgressListener()
            )
            .join()

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
        indexerUnderTest()
            .buildIndexAsync(
                Directory(Paths.get("src/test/resources/nested-files")),
                object : AsyncIndexingProgressListener {
                    override fun onNewFileIndexed(indexedFile: IndexedFile) {
                        indexedFileNames.add(indexedFile.path())
                    }

                    override fun onIndexingCompleted(indexedDirectory: IndexedDirectory) {
                        this@AsyncFullTextSearchTest.indexedDirectory =
                            indexedDirectory
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
        indexerUnderTest()
            .buildIndexAsync(
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

    private fun indexingProgressListener() =
        object : AsyncIndexingProgressListener {
            override fun onNewFileIndexed(indexedFile: IndexedFile) {
            }

            override fun onIndexingCompleted(indexedDirectory: IndexedDirectory) {
                this@AsyncFullTextSearchTest.indexedDirectory = indexedDirectory
            }
        }
}