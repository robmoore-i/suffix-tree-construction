package org.jetbrains.fulltextsearch.indexer

import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.hasItems
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers.hasSize
import org.jetbrains.fulltextsearch.Directory
import org.jetbrains.fulltextsearch.IndexedDirectory
import org.jetbrains.fulltextsearch.QueryMatch
import org.junit.jupiter.api.Test
import java.nio.file.Paths

abstract class FullTextSearchTest {
    abstract fun indexerUnderTest(): Indexer

    @Test
    internal fun `can search for a unique match in one file`() = runBlocking {
        val indexedDirectory: IndexedDirectory = indexerUnderTest()
            .buildIndex(Directory(Paths.get("src/test/resources/one-file")))

        val queryMatches = indexedDirectory.queryCaseSensitive("abracadabra")

        MatcherAssert.assertThat(queryMatches, hasSize(1))
        MatcherAssert.assertThat(
            queryMatches,
            CoreMatchers.hasItem(QueryMatch("file.txt", 38))
        )
    }

    @Test
    internal fun `can search for multiple matches in one file`() = runBlocking {
        val indexedDirectory: IndexedDirectory = indexerUnderTest()
            .buildIndex(Directory(Paths.get("src/test/resources/one-file")))

        val queryMatches = indexedDirectory.queryCaseSensitive("co")

        MatcherAssert.assertThat(queryMatches, hasSize(3))
        MatcherAssert.assertThat(
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

            MatcherAssert.assertThat(queryMatches, hasSize(2))
            MatcherAssert.assertThat(
                queryMatches, hasItems(
                    QueryMatch("file-1.txt", 169),
                    QueryMatch("file-2.txt", 56)
                )
            )
        }

    @Test
    internal fun `can search for matches in nested files`() =
        runBlocking {
            val indexedDirectory: IndexedDirectory = indexerUnderTest()
                .buildIndex(Directory(Paths.get("src/test/resources/nested-files")))

            val queryMatches = indexedDirectory.queryCaseSensitive("file")

            MatcherAssert.assertThat(queryMatches, hasSize(4))
            MatcherAssert.assertThat(
                queryMatches, hasItems(
                    QueryMatch("file-1.txt", 5),
                    QueryMatch("file-2.txt", 5),
                    QueryMatch("file-2.txt", 61),
                    QueryMatch("nested/file-3.txt", 17)
                )
            )
        }
}