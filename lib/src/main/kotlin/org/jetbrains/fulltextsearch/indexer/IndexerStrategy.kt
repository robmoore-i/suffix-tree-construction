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
            if (
                listOf(".jar", ".png").any { relativePath.endsWith(it) } // Don't index these
                || fileText.length > 10000 // It's not really fast enough to handle any bigger files
            ) {
                NaiveIndexedFile(relativePath, fileText)
            } else {
                SuffixTreeIndexedFile(relativePath, fileText)
            }
        }

        fun alwaysUseSuffixTreeIndex() =
            IndexerStrategy { rootDirectory, file ->
                SuffixTreeIndexedFile(rootDirectory.relativePathTo(file.path), file.readText())
            }

        fun alwaysUseNaiveIndex() = IndexerStrategy { rootDirectory, file ->
            NaiveIndexedFile(rootDirectory.relativePathTo(file.path), file.readText())
        }
    }
}