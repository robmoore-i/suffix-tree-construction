@file:Suppress("SpellCheckingInspection")

package org.jetbrains.fulltextsearch.performance_test.search

import org.jetbrains.fulltextsearch.performance_test.PerformanceTest
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

@PerformanceTest
class KotlinWebSiteSearchTest {
    @Disabled
    @Test
    internal fun `searching the kotlin web site repository`() {
        collectAndPrintSearchExecutionTimeData(
            "example-input-directories/kotlin-web-site",
            10
        )
    }
}