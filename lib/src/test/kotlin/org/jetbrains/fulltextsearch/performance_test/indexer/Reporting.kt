package org.jetbrains.fulltextsearch.performance_test.indexer

import kotlinx.coroutines.runBlocking
import org.jetbrains.fulltextsearch.filesystem.Directory
import org.jetbrains.fulltextsearch.index.IndexedFile
import org.jetbrains.fulltextsearch.indexer.IndexerStrategy
import org.jetbrains.fulltextsearch.indexer.async.AsyncIndexer
import org.jetbrains.fulltextsearch.indexer.async.AsyncIndexingProgressListener
import org.jetbrains.fulltextsearch.search.IndexedDirectory
import org.junit.jupiter.api.Assumptions.assumeTrue
import java.nio.file.Paths
import kotlin.system.measureTimeMillis

fun collectAndPrintIndexingExecutionTimeData(
    directoryPathFromSourceRoot: String,
    numberOfTimesToBuildTheIndex: Int,
    indexerStrategy: IndexerStrategy = IndexerStrategy.default(suffixTreeMaxCharsThreshold = 10000)
) {
    val dirPath = Paths.get("../$directoryPathFromSourceRoot")

    // If the directory doesn't exist, the test is marked as 'skipped'.
    assumeTrue(
        dirPath.toFile().exists(),
        "Assumption not met: The specified directory for the performance " +
                "test input doesn't exist. Clone it using the script: " +
                "`scripts/fetch-performance-test-data.sh`."
    )

    val executionTimes = mutableListOf<Long>()
    repeat(numberOfTimesToBuildTheIndex) {
        executionTimes.add(measureTimeMillis {
            // Note that we are testing the default indexer.
            val indexer = AsyncIndexer.default(indexerStrategy = indexerStrategy)
            runBlocking {
                indexer.buildIndexAsync(
                    Directory(dirPath),
                    object : AsyncIndexingProgressListener {
                        override fun onNewFileIndexed(indexedFile: IndexedFile) {
                        }

                        override fun onIndexingCompleted(indexedDirectory: IndexedDirectory) {
                        }
                    })
            }
        })
    }
    val maxExecutionTime = executionTimes.maxOrNull()!!
    val meanExecutionTime = executionTimes.toLongArray().sum() / numberOfTimesToBuildTheIndex
    println("Max execution time: ${maxExecutionTime}ms")
    println("Mean execution time: ${meanExecutionTime}ms")
}