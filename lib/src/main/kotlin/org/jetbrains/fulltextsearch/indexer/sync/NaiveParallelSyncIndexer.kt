package org.jetbrains.fulltextsearch.indexer.sync

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jetbrains.fulltextsearch.filesystem.Directory
import org.jetbrains.fulltextsearch.search.IndexedDirectory
import org.jetbrains.fulltextsearch.search.IndexedFile
import org.jetbrains.fulltextsearch.search.NaiveIndexedFile
import java.io.File
import java.util.Collections.synchronizedList

class NaiveParallelSyncIndexer : SyncIndexer {
    override fun buildIndex(
        directory: Directory,
        indexingProgressListener: SyncIndexingProgressListener
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
        return IndexedDirectory(indexedFiles)
    }

    private fun buildIndex(root: Directory, file: File): NaiveIndexedFile {
        return NaiveIndexedFile(root.relativePathTo(file.path), file.readText())
    }
}