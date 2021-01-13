package org.jetbrains.fulltextsearch.indexer

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jetbrains.fulltextsearch.Directory
import org.jetbrains.fulltextsearch.IndexedDirectory
import org.jetbrains.fulltextsearch.IndexedFile
import java.io.File
import java.util.Collections.synchronizedList

class NaiveParallelIndexer : Indexer {
    override fun buildIndex(directory: Directory): IndexedDirectory {
        val indexedFiles = synchronizedList(mutableListOf<IndexedFile>())
        runBlocking {
            directory.forEachFile {
                launch {
                    indexedFiles.add(buildIndex(directory, it))
                }
            }
        }
        return IndexedDirectory(indexedFiles)
    }

    private fun buildIndex(root: Directory, file: File): IndexedFile {
        return IndexedFile(root.relativePathTo(file.path), file.readText())
    }
}