@file:Suppress("SpellCheckingInspection")

package org.jetbrains.fulltextsearch.index.suffixtree

import org.jetbrains.fulltextsearch.randominput.RandomInput
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SuffixTreeTest {
    @Test
    internal fun `constructs simple suffix tree with only leaf nodes`() {
        val inputString = "abcde"
        assertSuffixTreeIsCorrectlyConstructed(inputString, suffixTree(inputString))
    }

    @Test
    internal fun `constructs simple suffix tree with one internal node`() {
        val inputString = "memo"
        assertSuffixTreeIsCorrectlyConstructed(inputString, suffixTree(inputString))
    }

    @Test
    internal fun `can construct multiple internal nodes on different branches'`() {
        val inputString = "xabxa$"
        assertSuffixTreeIsCorrectlyConstructed(inputString, suffixTree(inputString))
    }

    @Test
    internal fun `can construct nested internal nodes'`() {
        val inputString = "xaxbxac"
        assertSuffixTreeIsCorrectlyConstructed(inputString, suffixTree(inputString))
    }

    @Test
    internal fun `can add all-new leaf nodes from internal nodes`() {
        val inputString = "xxaxb"
        assertSuffixTreeIsCorrectlyConstructed(inputString, suffixTree(inputString))
    }

    @Test
    internal fun `converts the implicit suffix tree into a true suffix tree`() {
        val suffixTree = suffixTree("xxx")
        assertEquals(setOf(0, 1, 2), suffixTree.offsetsOf("x"))
        assertEquals(setOf(0, 1), suffixTree.offsetsOf("xx"))
        assertEquals(setOf(0), suffixTree.offsetsOf("xxx"))
    }

    @Test
    internal fun `checks for active node hop after following a suffix link`() {
        val inputString = "xyzxzyxy$"
        assertSuffixTreeIsCorrectlyConstructed(inputString, suffixTree(inputString))
    }

    @Test
    internal fun `checks for active node hop after root node insertion`() {
        val inputString = "xzyxyxy$"
        val suffixTree = suffixTree(inputString)
        assertEquals(setOf(4), suffixTree.offsetsOf("yxy$"))
        assertSuffixTreeIsCorrectlyConstructed(inputString, suffixTree)
    }

    @Test
    internal fun `resets active length and edge when reverting to root after internal node insertion`() {
        val inputString = "xyyxyyy$"
        assertSuffixTreeIsCorrectlyConstructed(inputString, suffixTree(inputString))
    }

    @Test
    internal fun `sets suffix link candidates only when splitting an edge`() {
        val inputString = "xxyzyxyz$"
        assertSuffixTreeIsCorrectlyConstructed(inputString, suffixTree(inputString))
    }

    @Test
    internal fun `can split internal edges`() {
        val inputString = "abcdefggg$"
        val suffixTree = suffixTree(inputString)
        assertEquals(setOf(6), suffixTree.offsetsOf("ggg$"))
        assertSuffixTreeIsCorrectlyConstructed(inputString, suffixTree)
    }

    @Test
    internal fun `next failing test`() {
        suffixTree("xxyzxyazxy$")
    }

    private fun suffixTree(inputString: String): SuffixTree {
        val suffixTree = SuffixTree.ukkonenConstruction(inputString)
        println("\nSuffix Tree for '$inputString': $suffixTree")
        return suffixTree
    }

    private fun assertSuffixTreeIsCorrectlyConstructed(
        inputString: String,
        suffixTree: SuffixTree
    ) {
        fun assertQueryIsCorrect(queryString: String) {
            fun expectedIndices(): Set<Int> {
                val indices = mutableSetOf<Int>()
                if (queryString.isBlank()) {
                    return indices
                }

                var i = -1
                while (true) {
                    i = inputString.indexOf(queryString, i + 1)
                    when (i) {
                        -1 -> return indices
                        else -> indices.add(i)
                    }
                }
            }

            try {
                assertEquals(expectedIndices(), suffixTree.offsetsOf(queryString))
            } catch (e: Throwable) {
                println("Query for '$queryString' was incorrect for input '$inputString'!")
                throw e
            }
        }

        // Test the first n characters up to n=5
        (1..minOf(5, inputString.length - 1)).forEach {
            assertQueryIsCorrect(inputString.substring(0, it))
        }
        // Test the last n characters up to n=5
        (1..minOf(5, inputString.length - 1)).forEach {
            assertQueryIsCorrect(inputString.substring(inputString.length - it, inputString.length))
        }
        // Test the full string
        assertQueryIsCorrect(inputString)
        // Test 100 random substrings for good measure
        repeat(200) {
            val randomInput = RandomInput.generateRandomString(
                alphabet = inputString + "abcdefghijklmnopqrstuvwxyz$",
                maxLength = inputString.length
            )
            assertQueryIsCorrect(randomInput)
        }
    }
}