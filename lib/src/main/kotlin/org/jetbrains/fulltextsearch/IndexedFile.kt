package org.jetbrains.fulltextsearch

class IndexedFile(private val path: String, private val fileText: String) {
    fun query(s: String): List<QueryMatch> {
        return Regex.fromLiteral(s)
            .findAll(fileText)
            .map { QueryMatch(path, it.range.first) }
            .toList()
    }
}