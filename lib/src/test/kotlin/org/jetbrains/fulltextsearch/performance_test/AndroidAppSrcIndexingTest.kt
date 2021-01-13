@file:Suppress("SpellCheckingInspection")

package org.jetbrains.fulltextsearch.performance_test

import org.junit.jupiter.api.Test

@PerformanceTest
class AndroidAppSrcIndexingTest {
    @Test
    internal fun `indexing an Android app repository`() {
        collectAndPrintExecutionTimeData(
            "example-input-directories/Learnification",
            100
        )
    }
}