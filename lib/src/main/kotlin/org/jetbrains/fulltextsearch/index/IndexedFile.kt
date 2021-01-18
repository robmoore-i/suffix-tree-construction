package org.jetbrains.fulltextsearch.index

import org.jetbrains.fulltextsearch.filesystem.Directory
import org.jetbrains.fulltextsearch.index.naive.NaiveIndexedFile
import org.jetbrains.fulltextsearch.index.suffixtree.SuffixTreeIndexedFile
import org.jetbrains.fulltextsearch.search.QueryMatch
import java.io.File

interface IndexedFile {
    fun query(queryString: String): List<QueryMatch>

    fun path(): String

    companion object {
        fun buildFor(root: Directory, file: File): IndexedFile {
            val relativePath = root.relativePathTo(file.path)
            val fileText = file.readText()
            return if (fileText.length < 50) {
                SuffixTreeIndexedFile(relativePath, fileText)
            } else {
                NaiveIndexedFile(relativePath, fileText)
            }
        }
    }
}