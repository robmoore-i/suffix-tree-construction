@file:Suppress("SpellCheckingInspection")

package org.jetbrains.fulltextsearch

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import org.jetbrains.fulltextsearch.filesystem.Directory
import org.jetbrains.fulltextsearch.index.IndexedFile
import org.jetbrains.fulltextsearch.indexer.IndexerStrategy
import org.jetbrains.fulltextsearch.indexer.async.AsyncIndexer
import org.jetbrains.fulltextsearch.indexer.async.AsyncIndexingProgressListener
import org.jetbrains.fulltextsearch.search.IndexedDirectory
import org.jetbrains.fulltextsearch.search.QueryMatch
import java.nio.file.Paths
import kotlin.system.exitProcess

class Main {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runBlocking {
                printBanner()
                val directory: Directory = chooseSearchDirectory()
                val indexer =
                    AsyncIndexer.default(IndexerStrategy.default(suffixTreeMaxCharsThreshold = null))
                val indexedDirectory: IndexedDirectory = coroutineScope {
                    println("Indexing...")
                    var theIndexedDirectory: IndexedDirectory? = null
                    indexer.buildIndexAsync(
                        directory,
                        object : AsyncIndexingProgressListener {
                            override fun onNewFileIndexed(indexedFile: IndexedFile) {
                                println("Index built for ${indexedFile.relativePath()}")
                            }

                            override fun onIndexingCompleted(indexedDirectory: IndexedDirectory) {
                                theIndexedDirectory = indexedDirectory
                            }
                        })
                    println("Done.")
                    theIndexedDirectory!!
                }
                runSearchQueryREPL(indexedDirectory)
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
                val queryCaseSensitive: List<QueryMatch> =
                    indexedDirectory.queryCaseSensitive(queryInput.query!!)
                println("Found ${queryCaseSensitive.size} matching lines:")
                queryCaseSensitive.forEach {
                    println(indexedDirectory.correspondingFileLine(it).trim())
                }
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