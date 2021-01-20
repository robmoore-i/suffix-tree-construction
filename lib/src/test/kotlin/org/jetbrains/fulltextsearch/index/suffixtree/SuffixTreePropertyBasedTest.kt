@file:Suppress("SpellCheckingInspection")

package org.jetbrains.fulltextsearch.index.suffixtree

import org.jetbrains.fulltextsearch.index.naive.NaiveIndexedFile
import org.jetbrains.fulltextsearch.randominput.RandomInput
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class SuffixTreePropertyBasedTest {
    @Test
    @Disabled
    internal fun `there exists a leaf for every suffix of the input`() {
        repeat(10000) {
            val fileContent = RandomInput.generateRandomString(minLength = 1, maxLength = 15)
            val suffixTree = SuffixTree.defaultConstruction(fileContent)
            val leaves: Set<LeafNode> = suffixTree.leaves()
            try {
                assertEquals(fileContent.length + 1, leaves.size)
                (fileContent.indices).forEach { suffixOffset ->
                    assertEquals(
                        1,
                        leaves.filter { leafNode ->
                            setOf(suffixOffset) == leafNode.descendentSuffixOffsets()
                        }.size
                    )
                }
            } catch (e: Throwable) {
                println("Failed for input '$fileContent'")
                throw e
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