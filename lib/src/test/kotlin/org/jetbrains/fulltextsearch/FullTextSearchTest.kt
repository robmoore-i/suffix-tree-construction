package org.jetbrains.fulltextsearch

import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.hasItem
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test
import java.nio.file.Paths

class FullTextSearchTest {
    @Test
    internal fun `can search for a unique match in one file`() = runBlocking {
        val indexedDirectory: IndexedDirectory = Indexer()
            .buildIndex(Directory(Paths.get("src/test/resources/one-file")))

        val queryMatches = indexedDirectory.queryCaseSensitive("abracadabra")

        assertThat(queryMatches, hasSize(1))
        assertThat(queryMatches, hasItem(QueryMatch("file.txt", 38)))
    }

    @Test
    internal fun `can search for multiple matches in one file`() = runBlocking {
        val indexedDirectory: IndexedDirectory = Indexer()
            .buildIndex(Directory(Paths.get("src/test/resources/one-file")))

        val queryMatches = indexedDirectory.queryCaseSensitive("co")

        assertThat(queryMatches, hasSize(3))
        assertThat(queryMatches, hasItem(QueryMatch("file.txt", 10)))
        assertThat(queryMatches, hasItem(QueryMatch("file.txt", 98)))
        assertThat(queryMatches, hasItem(QueryMatch("file.txt", 144)))
    }

    @Test
    internal fun `can search for multiple matches in multiple files`() = runBlocking {
        val indexedDirectory: IndexedDirectory = Indexer()
            .buildIndex(Directory(Paths.get("src/test/resources/two-files")))

        val queryMatches = indexedDirectory.queryCaseSensitive("this")

        assertThat(queryMatches, hasSize(2))
        assertThat(queryMatches, hasItem(QueryMatch("file-1.txt", 169)))
        assertThat(queryMatches, hasItem(QueryMatch("file-2.txt", 56)))
    }
}