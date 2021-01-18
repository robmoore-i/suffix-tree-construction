package org.jetbrains.fulltextsearch.index

import org.jetbrains.fulltextsearch.filesystem.Directory
import org.jetbrains.fulltextsearch.index.naive.NaiveIndexedFile
import org.jetbrains.fulltextsearch.search.QueryMatch
import java.io.File

interface IndexedFile {
    fun query(queryString: String): List<QueryMatch>

    fun path(): String

    companion object {
        fun buildFor(root: Directory, file: File): IndexedFile {
            return NaiveIndexedFile(root.relativePathTo(file.path), file.readText())
        }
    }
}