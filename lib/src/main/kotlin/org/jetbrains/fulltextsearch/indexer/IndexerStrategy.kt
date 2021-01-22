package org.jetbrains.fulltextsearch.indexer

import org.jetbrains.fulltextsearch.filesystem.Directory
import org.jetbrains.fulltextsearch.index.IndexedFile
import org.jetbrains.fulltextsearch.index.naive.NaiveIndexedFile
import org.jetbrains.fulltextsearch.index.suffixtree.SuffixTreeIndexedFile
import java.io.File

fun interface IndexerStrategy {
    fun buildIndexFor(rootDirectory: Directory, file: File): IndexedFile

    companion object {
        fun default(): IndexerStrategy = IndexerStrategy { rootDirectory, file ->
            val relativePath = rootDirectory.relativePathTo(file.path)
            val fileText = file.readText()
            if (fileText.length < 50) {
                SuffixTreeIndexedFile(relativePath, fileText, useFallback = true)
            } else {
                NaiveIndexedFile(relativePath, fileText)
            }
        }

        fun alwaysUseSuffixTreeIndex(useFallback: Boolean = false) =
            IndexerStrategy { rootDirectory, file ->
                SuffixTreeIndexedFile(
                    rootDirectory.relativePathTo(file.path), file.readText(),
                    useFallback = useFallback
                )
            }

        fun alwaysUseNaiveIndex() = IndexerStrategy { rootDirectory, file ->
            NaiveIndexedFile(rootDirectory.relativePathTo(file.path), file.readText())
        }
    }
}