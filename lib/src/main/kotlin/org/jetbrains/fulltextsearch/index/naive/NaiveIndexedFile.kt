package org.jetbrains.fulltextsearch.index.naive

import org.jetbrains.fulltextsearch.index.IndexedFile
import org.jetbrains.fulltextsearch.search.QueryMatch
import java.util.regex.Pattern
import java.util.stream.Collectors

class NaiveIndexedFile(private val path: String, private val fileText: String) : IndexedFile {
    override fun query(queryString: String): List<QueryMatch> {
        val pattern: Pattern = Pattern.compile("(?=$queryString)")
        val matcher = pattern.matcher(fileText)
        return matcher.results()
            .collect(Collectors.toList())
            .toList()
            .map { QueryMatch(path, it.start()) }
    }

    override fun path(): String = path
}
