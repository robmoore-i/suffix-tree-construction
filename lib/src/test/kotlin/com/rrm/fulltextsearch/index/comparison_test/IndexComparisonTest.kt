package com.rrm.fulltextsearch.index.comparison_test

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import com.rrm.fulltextsearch.filesystem.Directory
import com.rrm.fulltextsearch.index.IndexedFile
import com.rrm.fulltextsearch.index.naive.NaiveIndexedFile
import com.rrm.fulltextsearch.index.suffixtree.SuffixTreeIndexedFile
import com.rrm.fulltextsearch.indexer.IndexerStrategy
import com.rrm.fulltextsearch.indexer.async.AsyncIndexingProgressListener
import com.rrm.fulltextsearch.indexer.async.ParallelAsyncIndexer
import com.rrm.fulltextsearch.randominput.RandomInput
import com.rrm.fulltextsearch.search.IndexedDirectory
import org.junit.jupiter.api.Test
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.reflect.KClass

/**
 * This class compares the suffix tree index and the naive index to make sure that they are getting
 * the same query results for the same input.
 */
class IndexComparisonTest {
    @Test
    internal fun `compare naive index to suffix tree index`() = runBlocking {
        val dirPath = Paths.get("src/test/resources/example-java-project")
        val suffixTreeIndexer = ParallelAsyncIndexer(IndexerStrategy.alwaysUseSuffixTreeIndex())
        val naiveIndexer = ParallelAsyncIndexer(IndexerStrategy.alwaysUseNaiveIndex())
        val (suffixTreeIndex: IndexedDirectory, naiveIndex: IndexedDirectory) =
            buildIndices(dirPath, suffixTreeIndexer, naiveIndexer)

        repeat(50) {
            val queryTerm = RandomInput.generateRandomSearchQueryTerm()
            val suffixTreeMatches = suffixTreeIndex.queryCaseSensitive(queryTerm)
            val expectedMatches = naiveIndex.queryCaseSensitive(queryTerm)
            QueryResultsComparisons.printQueryResultComparison(suffixTreeMatches, expectedMatches)
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
                        println("Suffix tree indexer has finished indexing the file ${indexedFile.relativePath()}")
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
                        println("Naive indexer has finished indexing the file ${indexedFile.relativePath()}")
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