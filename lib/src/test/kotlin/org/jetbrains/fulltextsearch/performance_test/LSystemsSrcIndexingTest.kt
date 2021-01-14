@file:Suppress("SpellCheckingInspection")

package org.jetbrains.fulltextsearch.performance_test

import org.junit.jupiter.api.Test

@PerformanceTest
class LSystemsSrcIndexingTest {
    @Test
    internal fun `indexing a small Java program repository`() {
        collectAndPrintExecutionTimeData(
            "example-input-directories/LSystems",
            100
        )
    }
}