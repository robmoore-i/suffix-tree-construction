package org.jetbrains.fulltextsearch.indexer

import org.jetbrains.fulltextsearch.filesystem.Directory
import org.jetbrains.fulltextsearch.index.naive.NaiveIndexedFile
import org.jetbrains.fulltextsearch.index.none.NoSearchIndexedFile
import org.jetbrains.fulltextsearch.index.suffixtree.SuffixTreeIndexedFile
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.nio.file.Paths

class IndexerStrategyTest {
    @Test
    internal fun `default indexer strategy doesn't index jar files`() {
        val indexedFile = IndexerStrategy.default().buildIndexFor(
            Directory(Paths.get("src/test/resources/gradle-wrapper")),
            Paths.get("src/test/resources/gradle-wrapper/my-program.jar").toFile()
        )

        assertTrue(indexedFile is NoSearchIndexedFile)
    }

    @Test
    internal fun `default indexer strategy uses suffix tree index for files smaller than the threshold`() {
        val indexedFile = IndexerStrategy.default(suffixTreeMaxCharsThreshold = 1000).buildIndexFor(
            Directory(Paths.get("src/test/resources/one-file")),
            Paths.get("src/test/resources/one-file/file.txt").toFile()
        )

        assertTrue(indexedFile is SuffixTreeIndexedFile)
    }

    @Test
    internal fun `default indexer strategy uses naive index for files larger than the threshold`() {
        val indexedFile = IndexerStrategy.default(suffixTreeMaxCharsThreshold = 20).buildIndexFor(
            Directory(Paths.get("src/test/resources/one-file")),
            Paths.get("src/test/resources/one-file/file.txt").toFile()
        )

        assertTrue(indexedFile is NaiveIndexedFile)
    }
}