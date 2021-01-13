@file:Suppress("SpellCheckingInspection")

package org.jetbrains.fulltextsearch.performance_test

import org.junit.jupiter.api.Test

@PerformanceTest
class BibleIndexingTest {
    @Test
    internal fun `indexing the King James Bible`() {
        collectAndPrintExecutionTimeData(
            "example-input-directories/bible",
            100
        )
    }
}