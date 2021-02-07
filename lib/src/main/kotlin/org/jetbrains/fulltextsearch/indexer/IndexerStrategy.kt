package org.jetbrains.fulltextsearch.indexer

import org.jetbrains.fulltextsearch.filesystem.Directory
import org.jetbrains.fulltextsearch.index.IndexedFile
import org.jetbrains.fulltextsearch.index.naive.NaiveIndexedFile
import org.jetbrains.fulltextsearch.index.none.NoSearchIndexedFile
import org.jetbrains.fulltextsearch.index.suffixtree.SuffixTreeIndexedFile
import java.io.File

fun interface IndexerStrategy {
    fun buildIndexFor(rootDirectory: Directory, file: File): IndexedFile

    companion object {
        fun default(suffixTreeMaxCharsThreshold: Int? = 10000): IndexerStrategy =
            IndexerStrategy { rootDirectory, file ->
                val relativePath = rootDirectory.relativePathTo(file.path)
                val fileExtensionsToNotIndex = setOf(".jar", ".png", ".jpg", ".jpeg")
                if (fileExtensionsToNotIndex.any { relativePath.endsWith(it) }) {
                    return@IndexerStrategy NoSearchIndexedFile(relativePath)
                }

                val fileText = file.readText()
                if (suffixTreeMaxCharsThreshold != null && suffixTreeMaxCharsThreshold < fileText.length) {
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