@file:Suppress("SpellCheckingInspection")

package org.jetbrains.fulltextsearch

import java.nio.file.Paths
import java.util.*

class Main {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            printBanner()
            val userInput = UserInputSource()
            val directory: Directory = chooseSearchDirectory(userInput)

            val indexer = Indexer()
            println("Indexing...")
            val indexedDirectory: IndexedDirectory =
                indexer.buildIndex(directory)
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
            val learnificationButtonDirectory =
                "example-input-directories/Learnification/app/src/main/java/com/rrm/learnification/button"
            val defaultSearchDirectory =
                if (currentWorkingDirectory().endsWith("app")) {
                    "../$learnificationButtonDirectory"
                } else {
                    learnificationButtonDirectory
                }

            println("Which directory do you want to search in? (default = $defaultSearchDirectory)")
            var path = userInputSource.readLine()
            if (path.isBlank()) {
                path = defaultSearchDirectory
            }
            return Directory(Paths.get(path))
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
        println(
            "What do you want to search for? " +
                    "Note that searches are case sensitive. " +
                    "Type 'quit' to quit."
        )
        query = userInputSource.readLine()
        while (query.isNullOrBlank()) {
            println(
                "What do you want to search for? " +
                        "Note that searches are case sensitive. " +
                        "Type 'quit' to quit."
            )
            query = userInputSource.readLine()
        }
    }

    fun hasQuit(): Boolean = query == "quit"
}