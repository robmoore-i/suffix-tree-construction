package org.jetbrains.fulltextsearch.performance_test.search

import kotlinx.coroutines.runBlocking
import org.jetbrains.fulltextsearch.filesystem.Directory
import org.jetbrains.fulltextsearch.indexer.async.AsyncIndexer
import org.jetbrains.fulltextsearch.indexer.async.AsyncIndexingProgressListener
import org.jetbrains.fulltextsearch.search.IndexedDirectory
import org.jetbrains.fulltextsearch.search.IndexedFile
import org.opentest4j.TestAbortedException
import java.nio.file.Paths
import java.util.*
import kotlin.streams.asSequence
import kotlin.system.measureTimeMillis

fun collectAndPrintSearchExecutionTimeData(
    directoryPathFromSourceRoot: String,
    n: Int
) {
    val dirPath = Paths.get("../$directoryPathFromSourceRoot")
    if (!dirPath.toFile().exists()) {
        val message =
            "Assumption not met: The specified directory for the performance " +
                    "test input doesn't exist. Clone it using the script: " +
                    "`scripts/fetch-performance-test-data.sh`."
        println(message)
        // This has the effect of marking the test as 'skipped'.
        throw TestAbortedException(message)
    }
    val indexedDirectory: IndexedDirectory = runBlocking {
        println("Indexing...")
        val indexer = AsyncIndexer.default()
        var theIndexedDirectory: IndexedDirectory? = null
        indexer.buildIndexAsync(
            Directory(dirPath),
            object : AsyncIndexingProgressListener {
                override fun onNewFileIndexed(indexedFile: IndexedFile) {
                }

                override fun onIndexingCompleted(indexedDirectory: IndexedDirectory) {
                    theIndexedDirectory = indexedDirectory
                }
            })
        println("Done.")
        theIndexedDirectory!!
    }
    val searchQueryPerformanceTester = SearchQueryPerformanceTester()
    repeat(n) { searchQueryPerformanceTester.nextResult(indexedDirectory) }
    searchQueryPerformanceTester.printResults()
}

class SearchQueryPerformanceTester {
    private var counter = 0
    private val queryTerms = listOf(
        "class", "interface", "val", "var", "if",
        "else", "print", "String", "List", "list", "Set", "set", "Test", "test",
        "@Test", "assert", "{", ");", "\"hello world\"", "nonsense-text",
        "long search term unlikely to have any matches", "            "
    )
    private val results = mutableMapOf<String, Long>()

    fun nextResult(indexedDirectory: IndexedDirectory) {
        if (counter >= queryTerms.size) {
            val queryTerm: String = randomString()
            results[queryTerm] = measureTimeMillis {
                indexedDirectory.queryCaseSensitive(queryTerm)
            }
        } else {
            val queryTerm: String = queryTerms[counter]
            results[queryTerm] = measureTimeMillis {
                indexedDirectory.queryCaseSensitive(queryTerm)
            }
            counter++
        }
    }

    private val random = Random()
    private fun randomString(): String {
        @Suppress("SpellCheckingInspection")
        val source = "abcdefghijklmnopqrstuvwxyz "
        val sizeOfRandomString: Long =
            random.longs(1, 1, 20)
                .findFirst()
                .orElseThrow { RuntimeException("Couldn't create random query string") }

        return random.ints(sizeOfRandomString, 0, source.length)
            .asSequence()
            .map(source::get)
            .joinToString("")
    }

    fun printResults() =
        results.forEach { println("'${it.key}' : ${it.value}ms") }
}