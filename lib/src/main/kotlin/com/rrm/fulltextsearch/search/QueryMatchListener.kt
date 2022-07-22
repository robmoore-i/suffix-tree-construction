package com.rrm.fulltextsearch.search

import java.util.*

fun interface QueryMatchListener {
    fun onQueryMatches(matches: List<QueryMatch>)

    /**
     * This QueryMatchListener collects all the matches it receives into a
     * synchronized list, which can be accessed by callers.
     */
    class MatchCollector : QueryMatchListener {
        private val queryMatches: MutableList<QueryMatch> =
            Collections.synchronizedList(mutableListOf<QueryMatch>())

        override fun onQueryMatches(matches: List<QueryMatch>) {
            queryMatches.addAll(matches)
        }

        fun matches(): List<QueryMatch> {
            return queryMatches.toList()
        }
    }
}
