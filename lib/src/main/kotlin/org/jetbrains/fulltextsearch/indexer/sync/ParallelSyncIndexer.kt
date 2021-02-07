package org.jetbrains.fulltextsearch.indexer.sync

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jetbrains.fulltextsearch.filesystem.Directory
import org.jetbrains.fulltextsearch.index.IndexedFile
import org.jetbrains.fulltextsearch.indexer.IndexerStrategy
import org.jetbrains.fulltextsearch.search.IndexedDirectory
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