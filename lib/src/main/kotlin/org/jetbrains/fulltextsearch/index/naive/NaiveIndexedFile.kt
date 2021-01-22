package org.jetbrains.fulltextsearch.index.naive

import org.jetbrains.fulltextsearch.index.IndexedFile
import org.jetbrains.fulltextsearch.search.QueryMatch
import java.util.regex.Pattern
import kotlin.streams.toList

class NaiveIndexedFile(private val path: String, private val fileText: String) : IndexedFile {
    override fun query(queryString: String): List<QueryMatch> {
        if (queryString.isEmpty()) {
            return listOf()
        }
        return Regex.fromLiteral(queryString)
            .findAll(fileText)
            .map { QueryMatch(path, it.range.first) }
            .toList()
    }

    override fun path(): String = path

    // This method is useful for fuzz testing the suffix tree, because it provides accurate match
    // behaviour for text like 'aaa' with query 'aa', which should return 0 and 1. The 'query'
    // method of this class doesn't provide this, although it is correct in other, more typical
    // ways.
    fun lookaheadQuery(queryString: String): List<QueryMatch> {
        return Pattern.compile("(?=$queryString)").matcher(fileText).results()
            .map { QueryMatch(path, it.start()) }
            .toList()
    }
}
