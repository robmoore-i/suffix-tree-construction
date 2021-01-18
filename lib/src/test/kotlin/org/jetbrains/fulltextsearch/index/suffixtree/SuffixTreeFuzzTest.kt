@file:Suppress("SpellCheckingInspection")

package org.jetbrains.fulltextsearch.index.suffixtree

import org.jetbrains.fulltextsearch.index.naive.NaiveIndexedFile
import org.jetbrains.fulltextsearch.randominput.RandomInput.generateRandomString
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SuffixTreeFuzzTest {
    @Test
    internal fun `find bugs`() {
        repeat(1) {
            val randomFileContent = generateRandomString(minLength = 5, maxLength = 10)
            val naiveIndexedFile = NaiveIndexedFile("some-file.txt", randomFileContent)
            val suffixTreeIndexedFile = SuffixTreeIndexedFile("some-file.txt", randomFileContent)
            repeat(100) {
                val randomQueryString = generateRandomString(maxLength = 10)
                assertEquals(
                    naiveIndexedFile.query(randomQueryString).toSet(),
                    suffixTreeIndexedFile.query(randomQueryString).toSet(),
                    "SuffixTreeIndexedFile and NaiveIndexedFile disagreed on the output " +
                            "of a query.\n" +
                            "Query string: '$randomQueryString'\n" +
                            "File content: '$randomFileContent'\n"
                )
            }
        }
    }

    @Test
    internal fun `regression 1`() {
        val suffixTree: SuffixTree = suffixTree("gsblwoxjt")
        assertEquals(setOf<Int>(), suffixTree.offsetsOf("te"))
    }

    @Suppress("SameParameterValue")
    private fun suffixTree(inputString: String): SuffixTree {
        val suffixTree = SuffixTree(inputString)
        println("\nSuffix Tree for '$inputString': $suffixTree")
        return suffixTree
    }
}