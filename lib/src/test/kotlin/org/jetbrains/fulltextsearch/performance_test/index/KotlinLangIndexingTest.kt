@file:Suppress("SpellCheckingInspection")

package org.jetbrains.fulltextsearch.performance_test.index

import org.jetbrains.fulltextsearch.performance_test.PerformanceTest
import org.junit.jupiter.api.Test

@PerformanceTest
class KotlinLangIndexingTest {
    @Test
    internal fun `indexing the kotlin programming language repository`() {
        collectAndPrintIndexingExecutionTimeData(
            "example-input-directories/kotlin",
            1
        )
    }
}