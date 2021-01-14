@file:Suppress("SpellCheckingInspection")

package org.jetbrains.fulltextsearch.performance_test

import org.junit.jupiter.api.Test

@PerformanceTest
class KotlinLangIndexingTest {
    @Test
    internal fun `indexing the kotlin programming language repository`() {
        collectAndPrintExecutionTimeData(
            "example-input-directories/kotlin",
            1
        )
    }
}