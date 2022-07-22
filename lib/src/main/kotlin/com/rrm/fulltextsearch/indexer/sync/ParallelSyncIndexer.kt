package com.rrm.fulltextsearch.indexer.sync

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import com.rrm.fulltextsearch.filesystem.Directory
import com.rrm.fulltextsearch.index.IndexedFile
import com.rrm.fulltextsearch.indexer.IndexerStrategy
import com.rrm.fulltextsearch.search.IndexedDirectory
import java.util.Collections.synchronizedList

class ParallelSyncIndexer(
    private val indexerStrategy: IndexerStrategy = IndexerStrategy.default()
) : SyncIndexer {
    override fun buildIndex(
        directory: Directory,
        indexingProgressListener: SyncIndexingProgressListener
    ): IndexedDirectory {
        val indexedFiles = synchronizedList(mutableListOf<IndexedFile>())
        runBlocking {
            directory.forEachFile {
                val coroutineName = CoroutineName("build-index-for-${it.path}")
                launch(Dispatchers.Default + coroutineName) {
                    val indexedFile = indexerStrategy.buildIndexFor(directory, it)
                    indexingProgressListener.onNewFileIndexed(indexedFile)
                    indexedFiles.add(indexedFile)
                }
            }
        }
        return IndexedDirectory(indexedFiles)
    }
}