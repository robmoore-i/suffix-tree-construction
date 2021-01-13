@file:Suppress("SpellCheckingInspection")

package org.jetbrains.fulltextsearch.performance_test

import org.junit.jupiter.api.Test

@PerformanceTest
class KotlinWebSiteIndexingTest {
    @Test
    internal fun `indexing the kotlin web site repository`() {
        collectAndPrintExecutionTimeData(
            "example-input-directories/kotlin-web-site",
            10
        )
    }
}