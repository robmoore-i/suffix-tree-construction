@file:Suppress("SpellCheckingInspection")

package org.jetbrains.fulltextsearch

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.jetbrains.fulltextsearch.filesystem.Directory
import org.jetbrains.fulltextsearch.index.async.AsyncIndexer
import org.jetbrains.fulltextsearch.index.async.AsyncIndexingProgressListener
import org.jetbrains.fulltextsearch.search.IndexedDirectory
import org.jetbrains.fulltextsearch.search.IndexedFile
import org.jetbrains.fulltextsearch.search.QueryMatch
import java.nio.file.Paths
import java.util.*
import kotlin.system.exitProcess

class Main {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runBlocking {
                printBanner()
                val userInput = UserInputSource()
                val directory: Directory = chooseSearchDirectory(userInput)
                val indexer = AsyncIndexer.default()
                // Note: You can trigger a timeout using the below directory:
                // example-input-directories/kotlin
                val indexedDirectory: IndexedDirectory = withTimeout(2000) {
                    println("Indexing...")
                    var theIndexedDirectory: IndexedDirectory? = null
                    indexer.buildIndexAsync(
                        directory,
                        object : AsyncIndexingProgressListener {
                            override fun onNewFileIndexed(indexedFile: IndexedFile) {
                                println("Index built for ${indexedFile.path()}")
                            }

                            override fun onIndexingCompleted(indexedDirectory: IndexedDirectory) {
                                theIndexedDirectory = indexedDirectory
                            }
                        })
                    println("Done.")
                    theIndexedDirectory!!
                }
                runSearchQueryREPL(userInput, indexedDirectory)
            }
        }

        private fun printBanner() {
            println("=== Full-text search application ===")
            println(
                "Please note that your current working directory is " +
                        "'${currentWorkingDirectory()}'"
            )
        }

        private fun chooseSearchDirectory(userInputSource: UserInputSource): Directory {
            var defaultSearchDirectory =
                "example-input-directories/LSystems/src"
            if (currentWorkingDirectory().endsWith("app")) {
                defaultSearchDirectory = "../$defaultSearchDirectory"
            }

            println("Which directory do you want to search in? (default = $defaultSearchDirectory)")
            var userInput = userInputSource.readLine()
            if (userInput == "quit") {
                exitProcess(0)
            }
            if (userInput.isBlank()) {
                userInput = defaultSearchDirectory
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
                return chooseSearchDirectory(userInputSource)
            }
            println("Directory '${userInput}' not found. Type 'quit' to quit.\n")
            return chooseSearchDirectory(userInputSource)
        }

        private fun runSearchQueryREPL(
            userInputSource: UserInputSource,
            indexedDirectory: IndexedDirectory
        ) {
            val queryInput = QueryInput(userInputSource)
            queryInput.readFromUser()
            while (!queryInput.hasQuit()) {
                val queryCaseSensitive: List<QueryMatch> =
                    indexedDirectory.queryCaseSensitive(queryInput.query!!)
                println(queryCaseSensitive.toString().replace("),", "),\n"))
                queryInput.readFromUser()
            }
        }

        private fun currentWorkingDirectory() = System.getProperty("user.dir")
    }
}

class UserInputSource {
    private val scan = Scanner(System.`in`)

    fun readLine(): String {
        return scan.nextLine().trim()
    }
}

class QueryInput(private val userInputSource: UserInputSource) {
    var query: String? = ""

    fun readFromUser() {
        val prompt = "What do you want to search for? " +
                "Note that searches are case sensitive. " +
                // If you want to search for 'quit', then I'm sorry.
                "Type 'quit' to quit."
        println(
            prompt
        )
        query = userInputSource.readLine()
        while (query.isNullOrBlank()) {
            println(
                prompt
            )
            query = userInputSource.readLine()
        }
    }

    fun hasQuit(): Boolean = query == "quit"
}