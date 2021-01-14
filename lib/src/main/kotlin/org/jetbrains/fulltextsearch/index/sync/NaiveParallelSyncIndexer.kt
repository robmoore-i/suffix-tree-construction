package org.jetbrains.fulltextsearch.index.sync

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jetbrains.fulltextsearch.Directory
import org.jetbrains.fulltextsearch.IndexedFile
import org.jetbrains.fulltextsearch.index.IndexingProgressListener
import org.jetbrains.fulltextsearch.search.IndexedDirectory
import java.io.File
import java.util.Collections.synchronizedList

class NaiveParallelSyncIndexer : SyncIndexer {
    override fun buildIndex(
        directory: Directory,
        indexingProgressListener: IndexingProgressListener
    ): IndexedDirectory {
        val indexedFiles = synchronizedList(mutableListOf<IndexedFile>())
        runBlocking {
            directory.forEachFile {
                launch {
                    val indexedFile = buildIndex(directory, it)
                    indexingProgressListener.onNewFileIndexed(indexedFile)
                    indexedFiles.add(indexedFile)
                }
            }
        }
        val indexedDirectory = IndexedDirectory(indexedFiles)
        indexingProgressListener.onIndexingCompleted(indexedDirectory)
        return indexedDirectory
    }

    private fun buildIndex(root: Directory, file: File): IndexedFile {
        return IndexedFile(root.relativePathTo(file.path), file.readText())
    }
}