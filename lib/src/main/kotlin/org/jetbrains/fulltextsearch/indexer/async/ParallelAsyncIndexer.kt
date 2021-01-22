package org.jetbrains.fulltextsearch.indexer.async

import kotlinx.coroutines.*
import org.jetbrains.fulltextsearch.filesystem.Directory
import org.jetbrains.fulltextsearch.index.IndexedFile
import org.jetbrains.fulltextsearch.indexer.IndexerStrategy
import org.jetbrains.fulltextsearch.indexer.sync.SyncIndexer
import org.jetbrains.fulltextsearch.indexer.sync.SyncIndexingProgressListener
import org.jetbrains.fulltextsearch.search.IndexedDirectory
import java.util.*

class ParallelAsyncIndexer(
    private val indexerStrategy: IndexerStrategy = IndexerStrategy.default()
) : AsyncIndexer, SyncIndexer {
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

    override fun buildIndex(
        directory: Directory,
        indexingProgressListener: SyncIndexingProgressListener
    ): IndexedDirectory = runBlocking {
        var theIndexedDirectory: IndexedDirectory? = null
        buildIndexAsync(
            directory,
            object : AsyncIndexingProgressListener {
                override fun onNewFileIndexed(indexedFile: IndexedFile) {
                    indexingProgressListener.onNewFileIndexed(indexedFile)
                }

                override fun onIndexingCompleted(indexedDirectory: IndexedDirectory) {
                    theIndexedDirectory = indexedDirectory
                }
            })
        theIndexedDirectory!!
    }
}