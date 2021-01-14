package org.jetbrains.fulltextsearch.index.async

import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import org.jetbrains.fulltextsearch.Directory
import org.jetbrains.fulltextsearch.IndexedFile
import org.jetbrains.fulltextsearch.search.IndexedDirectory
import java.io.File
import java.util.*

class NaiveParallelAsyncIndexer : AsyncIndexer {
    override suspend fun buildIndexAsync(
        directory: Directory,
        indexingProgressListener: AsyncIndexingProgressListener
    ): Job = coroutineScope {
        launch {
            val indexedFiles =
                Collections.synchronizedList(mutableListOf<IndexedFile>())
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

    private fun buildIndex(root: Directory, file: File): IndexedFile {
        return IndexedFile(root.relativePathTo(file.path), file.readText())
    }
}