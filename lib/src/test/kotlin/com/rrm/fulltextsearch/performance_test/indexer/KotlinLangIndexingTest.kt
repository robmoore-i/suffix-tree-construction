@file:Suppress("SpellCheckingInspection")

package com.rrm.fulltextsearch.performance_test.indexer

import com.rrm.fulltextsearch.indexer.IndexerStrategy
import com.rrm.fulltextsearch.performance_test.PerformanceTest
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

@PerformanceTest
class KotlinLangIndexingTest {
    @Disabled
    @Test
    internal fun `indexing the kotlin programming language repository`() {
        collectAndPrintIndexingExecutionTimeData(
            "example-input-directories/kotlin",
            1,
            // At the moment my implementation can't really handle this data set.
            IndexerStrategy.default(suffixTreeMaxCharsThreshold = 10)
        )
    }
}