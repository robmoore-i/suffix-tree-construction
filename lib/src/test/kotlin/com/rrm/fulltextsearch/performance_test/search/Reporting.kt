package com.rrm.fulltextsearch.performance_test.search

import kotlinx.coroutines.runBlocking
import com.rrm.fulltextsearch.filesystem.Directory
import com.rrm.fulltextsearch.index.IndexedFile
import com.rrm.fulltextsearch.indexer.IndexerStrategy
import com.rrm.fulltextsearch.indexer.async.AsyncIndexer
import com.rrm.fulltextsearch.indexer.async.AsyncIndexingProgressListener
import com.rrm.fulltextsearch.randominput.RandomInput.generateRandomSearchQueryTerm
import com.rrm.fulltextsearch.search.IndexedDirectory
import org.opentest4j.TestAbortedException
import java.nio.file.Paths
import kotlin.system.measureTimeMillis

fun collectAndPrintSearchExecutionTimeData(
    directoryPathFromSourceRoot: String,
    numberOfSearchesToExecute: Int,
    indexerStrategy: IndexerStrategy = IndexerStrategy.default(suffixTreeMaxCharsThreshold = 10000)
) {
    val dirPath = Paths.get("../$directoryPathFromSourceRoot")
    if (!dirPath.toFile().exists()) {
        val message =
            "Assumption not met: The specified directory for the performance " +
                    "test input doesn't exist. Clone it using the script: " +
                    "`scripts/fetch-performance-test-data.sh`."
        println(message)
        // This has the effect of marking the test as 'skipped'.
        throw TestAbortedException(message)
    }
    val indexedDirectory: IndexedDirectory = runBlocking {
        println("Indexing...")
        val indexer = AsyncIndexer.default(indexerStrategy = indexerStrategy)
        var theIndexedDirectory: IndexedDirectory? = null
        indexer.buildIndexAsync(
            Directory(dirPath),
            object : AsyncIndexingProgressListener {
                override fun onNewFileIndexed(indexedFile: IndexedFile) {
                }

                override fun onIndexingCompleted(indexedDirectory: IndexedDirectory) {
                    theIndexedDirectory = indexedDirectory
                }
            })
        println("Done.")
        theIndexedDirectory!!
    }
    val searchQueryPerformanceTester = SearchQueryPerformanceTester()
    repeat(numberOfSearchesToExecute) { searchQueryPerformanceTester.nextResult(indexedDirectory) }
    searchQueryPerformanceTester.printResults()
}

class SearchQueryPerformanceTester {
    private val results = mutableMapOf<String, Long>()

    fun nextResult(indexedDirectory: IndexedDirectory) {
        val queryTerm = generateRandomSearchQueryTerm()
        results[queryTerm] = measureTimeMillis {
            indexedDirectory.queryCaseSensitive(queryTerm)
        }
    }

    fun printResults() = results.forEach { println("'${it.key}' : ${it.value}ms") }
}