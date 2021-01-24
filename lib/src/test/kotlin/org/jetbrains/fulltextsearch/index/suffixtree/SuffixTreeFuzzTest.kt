@file:Suppress("SpellCheckingInspection")

package org.jetbrains.fulltextsearch.index.suffixtree

import org.jetbrains.fulltextsearch.index.naive.NaiveIndexedFile
import org.jetbrains.fulltextsearch.randominput.RandomInput.generateRandomString
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test

class SuffixTreeFuzzTest {
    @Test
    internal fun `send random input and assert that the search results are correct`() {
        repeat(1000) {
            val fileContent = generateRandomString(
                // Use a smaller alphabet to increase likelihood of exposing a bug
                alphabet = "abcdefg",
                // I'm confident that this is big enough to catch any potential problems
                maxLength = 500
            )
            val naiveIndexedFile = NaiveIndexedFile("some-file.txt", fileContent)

            fun getSuffixTreeIndexedFile(): SuffixTreeIndexedFile {
                try {
                    return SuffixTreeIndexedFile("some-file.txt", fileContent)
                } catch (e: Exception) {
                    println("Fails for file content='$fileContent'")
                    throw e
                }
            }

            val suffixTreeIndexedFile = getSuffixTreeIndexedFile()

            // Lots of small queries
            repeat(1000) {
                val queryString = generateRandomString(maxLength = 5)
                assertQueryResultsMatch(
                    fileContent,
                    queryString,
                    naiveIndexedFile,
                    suffixTreeIndexedFile
                )
            }

            // A few slightly bigger queries
            repeat(50) {
                val queryString = generateRandomString(minLength = 10, maxLength = 50)
                assertQueryResultsMatch(
                    fileContent,
                    queryString,
                    naiveIndexedFile,
                    suffixTreeIndexedFile
                )
            }
        }
    }

    /**
     * You can use this assertion method in this file to create small test cases that recreate bug
     * scenarios, and then, using the test, refine the test data to minimal reproducing cases.
     */
    @Suppress("SameParameterValue", "unused")
    private fun `assert that suffix tree index matches naive index`(
        fileContent: String,
        queryString: String
    ) {
        val naiveIndexedFile = NaiveIndexedFile("some-file.txt", fileContent)
        val suffixTreeIndexedFile = SuffixTreeIndexedFile("some-file.txt", fileContent)
        assertQueryResultsMatch(fileContent, queryString, naiveIndexedFile, suffixTreeIndexedFile)
    }

    private fun assertQueryResultsMatch(
        fileContent: String,
        queryString: String,
        naiveIndexedFile: NaiveIndexedFile,
        suffixTreeIndexedFile: SuffixTreeIndexedFile
    ) {
        val expectedQueryResults = naiveIndexedFile.lookaheadQuery(queryString).toSet()
        val actualQueryResults = suffixTreeIndexedFile.query(queryString).toSet()
        if (expectedQueryResults != actualQueryResults) {
            var counter = 0
            for (expectedMatch in expectedQueryResults) {
                if (expectedMatch !in actualQueryResults) {
                    counter++
                    println(expectedMatch)
                }
            }
            println("\tThere were $counter expected query results which were missing")

            counter = 0
            for (match in expectedQueryResults) {
                if (match !in actualQueryResults) {
                    counter++
                    println(match)
                }
            }
            println("\tThere were $counter query results which were unexpected")
            fail<String>(
                "\tSuffixTreeIndexedFile and NaiveIndexedFile disagreed on the output " +
                        "of a query.\n" +
                        "Query string: '$queryString'\n" +
                        "File content: '$fileContent'\n"
            )
        }
    }
}