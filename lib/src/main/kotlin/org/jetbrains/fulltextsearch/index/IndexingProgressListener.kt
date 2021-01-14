package org.jetbrains.fulltextsearch.index

import java.io.File

fun interface IndexingProgressListener {
    fun onNewFileIndexed(file: File)

    class DoNothing : IndexingProgressListener {
        override fun onNewFileIndexed(file: File) {
        }
    }
}