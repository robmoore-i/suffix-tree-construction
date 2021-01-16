@file:Suppress("SpellCheckingInspection")

package org.jetbrains.fulltextsearch.performance_test.index

import org.jetbrains.fulltextsearch.performance_test.PerformanceTest
import org.junit.jupiter.api.Test

@PerformanceTest
class KotlinWebSiteIndexingTest {
    @Test
    internal fun `indexing the kotlin web site repository`() {
        collectAndPrintIndexingExecutionTimeData(
            "example-input-directories/kotlin-web-site",
            10
        )
    }
}