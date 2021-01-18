package org.jetbrains.fulltextsearch.index.async

import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import org.jetbrains.fulltextsearch.filesystem.Directory
import org.jetbrains.fulltextsearch.search.IndexedDirectory
import org.jetbrains.fulltextsearch.search.IndexedFile
import org.jetbrains.fulltextsearch.search.NaiveIndexedFile
import java.io.File
import java.util.*

class NaiveParallelAsyncIndexer : AsyncIndexer {
    override suspend fun buildIndexAsync(
        directory: Directory,
        indexingProgressListener: AsyncIndexingProgressListener
    ): Job = coroutineScope {
        launch {
            val indexedFiles = Collections.synchronizedList(mutableListOf<IndexedFile>())
            val indexingJobs = mutableListOf<Job>()
            directory.forEachFile {
                indexingJobs.add(launch {
                    val indexedFile = buildIndex(directory, it)
                    indexingProgressListener.onNewFileIndexed(indexedFile)
                    indexedFiles.add(indexedFile)
                })
            }
            joinAll(*indexingJobs.toTypedArray())
            indexingProgressListener.onIndexingCompleted(
                IndexedDirectory(indexedFiles)
            )
        }
    }

    private fun buildIndex(root: Directory, file: File): NaiveIndexedFile {
        return NaiveIndexedFile(root.relativePathTo(file.path), file.readText())
    }
}