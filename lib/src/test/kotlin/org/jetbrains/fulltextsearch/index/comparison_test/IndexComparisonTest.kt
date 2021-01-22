package org.jetbrains.fulltextsearch.index.comparison_test

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import org.jetbrains.fulltextsearch.filesystem.Directory
import org.jetbrains.fulltextsearch.index.IndexedFile
import org.jetbrains.fulltextsearch.index.naive.NaiveIndexedFile
import org.jetbrains.fulltextsearch.index.suffixtree.SuffixTreeIndexedFile
import org.jetbrains.fulltextsearch.indexer.IndexerStrategy
import org.jetbrains.fulltextsearch.indexer.async.AsyncIndexingProgressListener
import org.jetbrains.fulltextsearch.indexer.async.ParallelAsyncIndexer
import org.jetbrains.fulltextsearch.randominput.RandomInput
import org.jetbrains.fulltextsearch.search.IndexedDirectory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.reflect.KClass

/**
 * This class compares the suffix tree index and the naive index in order to identify any
 * shortcomings in the suffix tree index.
 */
class IndexComparisonTest {
    @Test
    @Disabled
    internal fun `compare naive index to suffix tree index`() = runBlocking {
        val dirPath = Paths.get("src/test/resources/example-java-project")
        val suffixTreeIndexer = ParallelAsyncIndexer(
            IndexerStrategy.alwaysUseSuffixTreeIndex(useFallback = true)
        )
        val naiveIndexer = ParallelAsyncIndexer(IndexerStrategy.alwaysUseNaiveIndex())
        val (suffixTreeIndex: IndexedDirectory, naiveIndex: IndexedDirectory) =
            buildIndices(dirPath, suffixTreeIndexer, naiveIndexer)

        repeat(50) {
            val queryTerm = RandomInput.generateRandomSearchQueryTerm()
            val suffixTreeResults = suffixTreeIndex.queryCaseSensitive(queryTerm).toSet()
            val expectedResults = naiveIndex.queryCaseSensitive(queryTerm).toSet()
            assertEquals(expectedResults, suffixTreeResults)
        }
    }

    private suspend fun buildIndices(
        dirPath: Path,
        suffixTreeIndexer: ParallelAsyncIndexer,
        naiveIndexer: ParallelAsyncIndexer
    ): Pair<IndexedDirectory, IndexedDirectory> = coroutineScope {
        val suffixTreeIndexResult = async {
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
            Pair(suffixTreeIndex!!, SuffixTreeIndexedFile::class)
        }

        val naiveIndexResult = async {
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
            Pair(naiveIndex!!, NaiveIndexedFile::class)
        }

        val results: List<Pair<IndexedDirectory, KClass<out IndexedFile>>> =
            awaitAll(suffixTreeIndexResult, naiveIndexResult)
        if (results[0].second == SuffixTreeIndexedFile::class) {
            Pair(results[0].first, results[1].first)
        } else {
            Pair(results[1].first, results[0].first)
        }
    }
}