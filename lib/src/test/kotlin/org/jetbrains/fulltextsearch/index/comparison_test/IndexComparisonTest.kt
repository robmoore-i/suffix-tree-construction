package org.jetbrains.fulltextsearch.index.comparison_test

import kotlinx.coroutines.runBlocking
import org.jetbrains.fulltextsearch.filesystem.Directory
import org.jetbrains.fulltextsearch.index.IndexedFile
import org.jetbrains.fulltextsearch.index.IndexerStrategy
import org.jetbrains.fulltextsearch.indexer.async.AsyncIndexingProgressListener
import org.jetbrains.fulltextsearch.indexer.async.ParallelAsyncIndexer
import org.jetbrains.fulltextsearch.search.IndexedDirectory
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.nio.file.Paths

/**
 * This class compares the suffix tree index and the naive index, primarily to identify the
 * shortcomings of the suffix tree index.
 */
class IndexComparisonTest {
    @Test
    @Disabled
    internal fun `compare naive index to suffix tree index`() {
        val dirPath = Paths.get("src/test/resources/example-java-project")
        val suffixTreeIndexer = ParallelAsyncIndexer(IndexerStrategy.alwaysUseSuffixTreeIndex)
        val naiveIndexer = ParallelAsyncIndexer(IndexerStrategy.alwaysUseNaiveIndex)
        val (suffixTreeIndex: IndexedDirectory, naiveIndex: IndexedDirectory) = runBlocking {
            var suffixTreeIndex: IndexedDirectory? = null
            suffixTreeIndexer.buildIndexAsync(
                Directory(dirPath),
                object : AsyncIndexingProgressListener {
                    override fun onNewFileIndexed(indexedFile: IndexedFile) {
                        println("Suffix tree indexer has finished indexing the file ${indexedFile.path()}")
                    }

                    override fun onIndexingCompleted(indexedDirectory: IndexedDirectory) {
                        println("Suffix tree indexer has finished indexing the whole directory")
                        suffixTreeIndex = indexedDirectory
                    }
                })
            var naiveIndex: IndexedDirectory? = null
            naiveIndexer.buildIndexAsync(
                Directory(dirPath),
                object : AsyncIndexingProgressListener {
                    override fun onNewFileIndexed(indexedFile: IndexedFile) {
                        println("Naive indexer has finished indexing the file ${indexedFile.path()}")
                    }

                    override fun onIndexingCompleted(indexedDirectory: IndexedDirectory) {
                        println("Naive indexer has finished indexing the whole directory")
                        naiveIndex = indexedDirectory
                    }
                })
            Pair(suffixTreeIndex!!, naiveIndex!!)
        }
    }
}