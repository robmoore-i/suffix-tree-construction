package com.rrm.fulltextsearch.indexer.async

import kotlinx.coroutines.*
import com.rrm.fulltextsearch.filesystem.Directory
import com.rrm.fulltextsearch.index.IndexedFile
import com.rrm.fulltextsearch.indexer.IndexerStrategy
import com.rrm.fulltextsearch.indexer.sync.SyncIndexer
import com.rrm.fulltextsearch.indexer.sync.SyncIndexingProgressListener
import com.rrm.fulltextsearch.search.IndexedDirectory
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
                val coroutineName = CoroutineName("build-index-for-${it.path}")
                indexingJobs.add(launch(Dispatchers.Default + coroutineName) {
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