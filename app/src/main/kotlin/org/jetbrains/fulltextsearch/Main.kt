@file:Suppress("SpellCheckingInspection")

package org.jetbrains.fulltextsearch

import org.jetbrains.fulltextsearch.index.Indexer
import org.jetbrains.fulltextsearch.search.IndexedDirectory
import java.nio.file.Paths
import java.util.*
import kotlin.system.exitProcess

class Main {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            printBanner()
            val userInput = UserInputSource()
            val directory: Directory = chooseSearchDirectory(userInput)

            val indexer = Indexer.defaultIndexer()
            println("Indexing...")
            val indexedDirectory: IndexedDirectory = indexer.buildIndex(
                directory,
                indexingProgressListener = { file ->
                    println("Index built for ${directory.relativePathTo(file.path)}")
                })
            println("Done.")

            runSearchQueryREPL(userInput, indexedDirectory)
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