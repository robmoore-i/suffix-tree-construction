package org.jetbrains.fulltextsearch.index

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jetbrains.fulltextsearch.Directory
import org.jetbrains.fulltextsearch.IndexedFile
import org.jetbrains.fulltextsearch.search.IndexedDirectory
import java.io.File
import java.util.Collections.synchronizedList

class NaiveParallelIndexer : Indexer {
    override fun buildIndex(
        directory: Directory,
        indexingProgressListener: IndexingProgressListener
    ): IndexedDirectory {
        val indexedFiles = synchronizedList(mutableListOf<IndexedFile>())
        runBlocking {
            directory.forEachFile {
                launch {
                    val indexedFile = buildIndex(directory, it)
                    indexingProgressListener.onNewFileIndexed(it)
                    indexedFiles.add(indexedFile)
                }
            }
        }
        return IndexedDirectory(indexedFiles)
    }

    private fun buildIndex(root: Directory, file: File): IndexedFile {
        return IndexedFile(root.relativePathTo(file.path), file.readText())
    }
}