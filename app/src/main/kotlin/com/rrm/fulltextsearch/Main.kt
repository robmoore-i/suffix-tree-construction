@file:Suppress("SpellCheckingInspection")

package com.rrm.fulltextsearch

import com.rrm.fulltextsearch.filesystem.Directory
import com.rrm.fulltextsearch.index.IndexedFile
import com.rrm.fulltextsearch.indexer.IndexerStrategy
import com.rrm.fulltextsearch.indexer.async.ParallelAsyncIndexer
import com.rrm.fulltextsearch.indexer.sync.SyncIndexingProgressListener
import com.rrm.fulltextsearch.search.IndexedDirectory
import com.rrm.fulltextsearch.search.QueryMatch
import kotlinx.coroutines.runBlocking
import java.nio.file.Paths
import kotlin.system.exitProcess
import kotlin.system.measureTimeMillis

class Main {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runBlocking {
                printBanner()
                val directory: Directory = chooseSearchDirectory()
                val indexer = ParallelAsyncIndexer(
                    IndexerStrategy.default(suffixTreeMaxCharsThreshold = null)
                )
                println("Indexing...")
                var indexedDirectory: IndexedDirectory?
                val indexingMillis = measureTimeMillis {
                    indexedDirectory = indexer.buildIndex(
                        directory,
                        object : SyncIndexingProgressListener {
                            override fun onNewFileIndexed(indexedFile: IndexedFile) {
                                println("Index built for ${indexedFile.relativePath()}")
                            }
                        })
                }
                println("Finished indexing in ${indexingMillis}ms.")
                runSearchQueryREPL(indexedDirectory!!)
            }
        }

        private fun printBanner() {
            println("=== Full-text search application ===")
            println(
                "Please note that your current working directory is " +
                        "'${currentWorkingDirectory()}'"
            )
        }

        private fun chooseSearchDirectory(): Directory {
            var defaultSearchDirectory =
                "example-input-directories/LSystems/src"
            if (currentWorkingDirectory().endsWith("app")) {
                defaultSearchDirectory = "../$defaultSearchDirectory"
            }

            println("Which directory do you want to search in? (default = $defaultSearchDirectory)")
            val inputLine = readLine()
            val userInput = if (!inputLine.isNullOrBlank()) {
                inputLine
            } else {
                defaultSearchDirectory
            }
            if (userInput == "quit") {
                exitProcess(0)
            }
            val dirPath = Paths.get(userInput)

            if (dirPath.toFile().exists()) {
                return Directory(dirPath)
            }
            if (userInput == defaultSearchDirectory) {
                println(
                    "The default directory '${userInput}' hasn't been downloaded yet. " +
                            "Use the shell script " +
                            "`scripts/fetch-performance-test-data.sh` " +
                            "in order to download it. " +
                            // If your directory is called 'quit', then I'm sorry.
                            "Type 'quit' to quit.\n"
                )
                return chooseSearchDirectory()
            }
            println("Directory '${userInput}' not found. Type 'quit' to quit.\n")
            return chooseSearchDirectory()
        }

        private fun runSearchQueryREPL(indexedDirectory: IndexedDirectory) {
            val queryInput = QueryInput()
            queryInput.readFromUser()
            while (!queryInput.hasQuit()) {
                var queryCaseSensitive: List<QueryMatch>?
                val millisTaken = measureTimeMillis {
                    queryCaseSensitive = indexedDirectory.queryCaseSensitive(queryInput.query!!)
                }
                val matches = queryCaseSensitive!!
                matches.forEach {
                    println(indexedDirectory.correspondingFileLine(it).trim())
                }
                println("Found ${matches.size} matching lines in ${millisTaken}ms.")
                queryInput.readFromUser()
            }
        }

        private fun currentWorkingDirectory() = System.getProperty("user.dir")
    }
}

class QueryInput() {
    var query: String? = ""

    fun readFromUser() {
        val prompt = "\nWhat do you want to search for? " +
                "Note that searches are case sensitive. " +
                // If you want to search for 'quit', then I'm sorry.
                "Type 'quit' to quit."
        println(
            prompt
        )
        query = readLine()
        while (query.isNullOrBlank()) {
            println(
                prompt
            )
            query = readLine()
        }
    }

    fun hasQuit(): Boolean = query == "quit"
}