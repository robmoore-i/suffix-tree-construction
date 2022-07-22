package com.rrm.fulltextsearch.index.comparison_test

import com.rrm.fulltextsearch.search.QueryMatch

object QueryResultsComparisons {
    fun printQueryResultComparison(
        actualQueryMatches: List<QueryMatch>,
        expectedQueryMatches: List<QueryMatch>,
        onMismatch: () -> Unit = { }
    ) {
        val suffixTreeMatches: Set<QueryMatch> = actualQueryMatches.toSet()
        val expectedMatches: Set<QueryMatch> = expectedQueryMatches.toSet()
        if (suffixTreeMatches != expectedMatches) {
            var counter = 0
            for (expectedMatch in expectedMatches) {
                if (expectedMatch !in suffixTreeMatches) {
                    counter++
                }
            }
            println("\tThere were $counter missing query results which were expected")

            counter = 0
            for (match in suffixTreeMatches) {
                if (match !in expectedMatches) {
                    counter++
                }
            }
            println("\tThere were $counter query results which were unexpected")
            onMismatch()
        }
    }
}