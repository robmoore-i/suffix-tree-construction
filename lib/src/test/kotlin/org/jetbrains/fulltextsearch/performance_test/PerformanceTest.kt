package org.jetbrains.fulltextsearch.performance_test

import org.jetbrains.fulltextsearch.Directory
import org.jetbrains.fulltextsearch.index.Indexer
import org.junit.jupiter.api.Tag
import org.opentest4j.TestAbortedException
import java.nio.file.Paths
import kotlin.system.measureTimeMillis

/**
 * This annotation is used to indicate that a test is a performance test. There
 * is an associated Gradle task for executing these tests, which is defined in
 * build.gradle.
 */
@Target(AnnotationTarget.CLASS)
@Retention
@Tag("performance-test")
annotation class PerformanceTest

fun collectAndPrintExecutionTimeData(
    directoryPathFromSourceRoot: String,
    n: Int
) {
    val executionTimes = mutableListOf<Long>()
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
    repeat(n) {
        executionTimes.add(measureTimeMillis {
            // Note that we are testing the default indexer.
            val defaultIndexer: Indexer = Indexer.defaultIndexer()
            defaultIndexer.buildIndex(Directory(dirPath))
        })
    }
    val maxExecutionTime = executionTimes.maxOrNull()!!
    val meanExecutionTime = executionTimes.toLongArray().sum() / n
    println("Max execution time: ${maxExecutionTime}ms")
    println("Mean execution time: ${meanExecutionTime}ms")
}