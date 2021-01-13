package org.jetbrains.fulltextsearch.performance_test

import org.jetbrains.fulltextsearch.Directory
import org.jetbrains.fulltextsearch.Indexer
import org.junit.jupiter.api.Tag
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
    repeat(n) {
        executionTimes.add(measureTimeMillis {
            Indexer().buildIndex(Directory(Paths.get("../$directoryPathFromSourceRoot")))
        })
    }
    val maxExecutionTime = executionTimes.maxOrNull()!!
    val meanExecutionTime = executionTimes.toLongArray().sum() / n
    println("Max execution time: ${maxExecutionTime}ms")
    println("Mean execution time: ${meanExecutionTime}ms")
}