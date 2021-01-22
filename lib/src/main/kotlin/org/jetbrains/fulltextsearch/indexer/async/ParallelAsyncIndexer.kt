package org.jetbrains.fulltextsearch.indexer.async

import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import org.jetbrains.fulltextsearch.filesystem.Directory
import org.jetbrains.fulltextsearch.index.IndexedFile
import org.jetbrains.fulltextsearch.index.IndexerStrategy
import org.jetbrains.fulltextsearch.search.IndexedDirectory
import java.util.*

class ParallelAsyncIndexer(
    private val indexerStrategy: IndexerStrategy = IndexerStrategy.default
) : AsyncIndexer {
    override suspend fun buildIndexAsync(
        directory: Directory,
        indexingProgressListener: AsyncIndexingProgressListener
    ): Job = coroutineScope {
        launch {
            val indexedFiles = Collections.synchronizedList(mutableListOf<IndexedFile>())
            val indexingJobs = mutableListOf<Job>()
            directory.forEachFile {
                indexingJobs.add(launch {
                    val indexedFile = indexerStrategy.buildIndexFor(directory, it)
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
}