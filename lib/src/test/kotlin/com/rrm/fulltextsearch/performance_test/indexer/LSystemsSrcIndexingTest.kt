@file:Suppress("SpellCheckingInspection")

package com.rrm.fulltextsearch.performance_test.indexer

import com.rrm.fulltextsearch.performance_test.PerformanceTest
import org.junit.jupiter.api.Test

@PerformanceTest
class LSystemsSrcIndexingTest {
    @Test
    internal fun `indexing a small Java program repository`() {
        collectAndPrintIndexingExecutionTimeData(
            "example-input-directories/LSystems",
            100
        )
    }
}