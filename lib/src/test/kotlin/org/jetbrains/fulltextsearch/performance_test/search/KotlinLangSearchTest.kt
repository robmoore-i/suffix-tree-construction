@file:Suppress("SpellCheckingInspection")

package org.jetbrains.fulltextsearch.performance_test.search

import org.jetbrains.fulltextsearch.indexer.IndexerStrategy
import org.jetbrains.fulltextsearch.performance_test.PerformanceTest
import org.junit.jupiter.api.Test

@PerformanceTest
class KotlinLangSearchTest {
    @Test
    internal fun `searching the kotlin programming language repository`() {
        collectAndPrintSearchExecutionTimeData(
            "example-input-directories/kotlin",
            5,
            // At the moment my implementation can't really handle this data set.
            IndexerStrategy.default(fileCharsThreshold = 10)
        )
    }
}