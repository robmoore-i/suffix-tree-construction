@file:Suppress("SpellCheckingInspection")

package org.jetbrains.fulltextsearch.performance_test.search

import org.jetbrains.fulltextsearch.performance_test.PerformanceTest
import org.junit.jupiter.api.Test

@PerformanceTest
class LSystemsSrcSearchTest {
    @Test
    internal fun `searching a small Java program repository`() {
        collectAndPrintSearchExecutionTimeData(
            "example-input-directories/LSystems",
            100
        )
    }
}