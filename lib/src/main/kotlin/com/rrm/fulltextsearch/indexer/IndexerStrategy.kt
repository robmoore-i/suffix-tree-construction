package com.rrm.fulltextsearch.indexer

import com.rrm.fulltextsearch.filesystem.Directory
import com.rrm.fulltextsearch.index.IndexedFile
import com.rrm.fulltextsearch.index.naive.NaiveIndexedFile
import com.rrm.fulltextsearch.index.none.NoSearchIndexedFile
import com.rrm.fulltextsearch.index.suffixtree.SuffixTreeIndexedFile
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