@file:Suppress("SpellCheckingInspection")

package com.rrm.fulltextsearch.performance_test.search

import com.rrm.fulltextsearch.indexer.IndexerStrategy
import com.rrm.fulltextsearch.performance_test.PerformanceTest
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

@PerformanceTest
class KotlinLangSearchTest {
    @Disabled
    @Test
    internal fun `searching the kotlin programming language repository`() {
        collectAndPrintSearchExecutionTimeData(
            "example-input-directories/kotlin",
            5,
            // At the moment my implementation can't really handle this data set.
            IndexerStrategy.default(suffixTreeMaxCharsThreshold = 10)
        )
    }
}