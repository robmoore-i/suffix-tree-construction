package org.jetbrains.fulltextsearch.indexer.sync

import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.hasItems
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.hasSize
import org.jetbrains.fulltextsearch.filesystem.Directory
import org.jetbrains.fulltextsearch.index.IndexedFile
import org.jetbrains.fulltextsearch.search.IndexedDirectory
import org.jetbrains.fulltextsearch.search.QueryMatch
import org.junit.jupiter.api.Test
import java.nio.file.Paths
import java.util.Collections.synchronizedList

abstract class SyncFullTextSearchTest {
    abstract fun indexerUnderTest(): SyncIndexer

    @Test
    internal fun `can search for a unique match in one file`() = runBlocking {
        val indexedDirectory: IndexedDirectory = indexerUnderTest()
            .buildIndex(Directory(Paths.get("src/test/resources/one-file")))

        val queryMatches = indexedDirectory.queryCaseSensitive("abracadabra")

        assertThat(queryMatches, hasSize(1))
        assertThat(
            queryMatches,
            CoreMatchers.hasItem(QueryMatch("file.txt", 38))
        )
    }

    @Test
    internal fun `can search for multiple matches in one file`() = runBlocking {
        val indexedDirectory: IndexedDirectory = indexerUnderTest()
            .buildIndex(Directory(Paths.get("src/test/resources/one-file")))

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
            val indexedDirectory: IndexedDirectory = indexerUnderTest()
                .buildIndex(Directory(Paths.get("src/test/resources/two-files")))

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
        val indexedDirectory: IndexedDirectory = indexerUnderTest()
            .buildIndex(Directory(Paths.get("src/test/resources/nested-files")))

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
        indexerUnderTest().buildIndex(
            Directory(Paths.get("src/test/resources/nested-files")),
            indexingProgressListener = object : SyncIndexingProgressListener {
                override fun onNewFileIndexed(indexedFile: IndexedFile) {
                    indexedFileNames.add(indexedFile.path())
                }
            })

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
}