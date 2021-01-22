package org.jetbrains.fulltextsearch.index.suffixtree

import org.jetbrains.fulltextsearch.index.IndexedFile
import org.jetbrains.fulltextsearch.index.naive.NaiveIndexedFile
import org.jetbrains.fulltextsearch.search.QueryMatch

class SuffixTreeIndexedFile(
    private val path: String, fileText: String, useFallback: Boolean = false
) : IndexedFile {
    @Suppress("JoinDeclarationAndAssignment")
    private val suffixTree: SuffixTree?
    private val fallbackIndexedFile = NaiveIndexedFile(path, fileText)

    init {
        this.suffixTree = try {
            SuffixTree.defaultConstruction(fileText)
        } catch (e: Throwable) {
            print("Suffix tree creation failed for file $path. ")
            if (useFallback) {
                println("Falling back to naive file index, which has no search query optimisation.")
                null
            } else {
                throw e
            }
        }
    }

    override fun query(queryString: String): List<QueryMatch> {
        @Suppress("IfThenToElvis") // I find this easier to read than the elvis statement.
        return if (suffixTree != null) {
            suffixTree.offsetsOf(queryString)
                .map { QueryMatch(path, it) }
        } else {
            fallbackIndexedFile.query(queryString)
        }
    }

    override fun path(): String = path
}