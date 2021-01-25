package org.jetbrains.fulltextsearch.performance_test.search

import kotlinx.coroutines.runBlocking
import org.jetbrains.fulltextsearch.filesystem.Directory
import org.jetbrains.fulltextsearch.index.IndexedFile
import org.jetbrains.fulltextsearch.indexer.IndexerStrategy
import org.jetbrains.fulltextsearch.indexer.async.AsyncIndexer
import org.jetbrains.fulltextsearch.indexer.async.AsyncIndexingProgressListener
import org.jetbrains.fulltextsearch.randominput.RandomInput.generateRandomSearchQueryTerm
import org.jetbrains.fulltextsearch.search.IndexedDirectory
import org.opentest4j.TestAbortedException
import java.nio.file.Paths
import kotlin.system.measureTimeMillis

fun collectAndPrintSearchExecutionTimeData(
    directoryPathFromSourceRoot: String,
    numberOfSearchesToExecute: Int,
    indexerStrategy: IndexerStrategy = IndexerStrategy.default(fileCharsThreshold = 10000)
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