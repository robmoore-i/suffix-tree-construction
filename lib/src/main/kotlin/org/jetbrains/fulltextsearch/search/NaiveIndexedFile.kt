package org.jetbrains.fulltextsearch.search

class NaiveIndexedFile(private val path: String, private val fileText: String) : IndexedFile {
    override fun query(queryString: String): List<QueryMatch> {
        return Regex.fromLiteral(queryString)
            .findAll(fileText)
            .map { QueryMatch(path, it.range.first) }
            .toList()
    }

    override fun path(): String = path
}
