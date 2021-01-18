@file:Suppress("SpellCheckingInspection")

package org.jetbrains.fulltextsearch.index.suffixtree

import org.jetbrains.fulltextsearch.index.naive.NaiveIndexedFile
import org.jetbrains.fulltextsearch.randominput.RandomInput.generateRandomString
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class SuffixTreeFuzzTest {
    @Test
    @Disabled
    internal fun `find bugs`() {
        repeat(100) {
            val fileContent = generateRandomString(minLength = 100, maxLength = 105)
            val naiveIndexedFile = NaiveIndexedFile("some-file.txt", fileContent)
            val suffixTreeIndexedFile = SuffixTreeIndexedFile("some-file.txt", fileContent)
            repeat(100) {
                val queryString = generateRandomString(maxLength = 10)
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
        val expectedQueryResults = naiveIndexedFile.query(queryString).toSet()
        assertEquals(
            expectedQueryResults,
            suffixTreeIndexedFile.query(queryString).toSet(),
            "SuffixTreeIndexedFile and NaiveIndexedFile disagreed on the output " +
                    "of a query.\n" +
                    "Query string: '$queryString'\n" +
                    "File content: '$fileContent'\n"
        )
    }
}