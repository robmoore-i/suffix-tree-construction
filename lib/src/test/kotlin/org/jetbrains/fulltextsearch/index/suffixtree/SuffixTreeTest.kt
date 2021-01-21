@file:Suppress("SpellCheckingInspection")

package org.jetbrains.fulltextsearch.index.suffixtree

import org.jetbrains.fulltextsearch.randominput.RandomInput
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SuffixTreeTest {

    @Test
    internal fun `constructs simple suffix tree with only leaf nodes`() {
        val input = "abcde"
        assertSuffixTreeIsCorrectlyConstructed(input, suffixTree(input))
    }

    @Test
    internal fun `constructs simple suffix tree with one internal node`() {
        val input = "memo"
        assertSuffixTreeIsCorrectlyConstructed(input, suffixTree(input))
    }

    @Test
    internal fun `can construct multiple internal nodes on different branches'`() {
        val input = "xabxa$"
        assertSuffixTreeIsCorrectlyConstructed(input, suffixTree(input))
    }

    @Test
    internal fun `can construct nested internal nodes'`() {
        val input = "xaxbxac"
        assertSuffixTreeIsCorrectlyConstructed(input, suffixTree(input))
    }

    @Test
    internal fun `can add all-new leaf nodes from internal nodes`() {
        val input = "xxaxb"
        assertSuffixTreeIsCorrectlyConstructed(input, suffixTree(input))
    }

    @Test
    internal fun `converts the implicit suffix tree into a true suffix tree`() {
        val suffixTree = suffixTree("xxx")
        assertEquals(setOf(0, 1, 2), suffixTree.offsetsOf("x"))
        assertEquals(setOf(0, 1), suffixTree.offsetsOf("xx"))
        assertEquals(setOf(0), suffixTree.offsetsOf("xxx"))
    }

    @Test
    internal fun `reverts to root after internal node insertion`() {
        val input = "yyxyyz"
        assertSuffixTreeIsCorrectlyConstructed(input, suffixTree(input))
    }

    @Test
    internal fun `checks for active node hop after following a suffix link`() {
        val input = "xyzxzyxy$"
        assertSuffixTreeIsCorrectlyConstructed(input, suffixTree(input))
    }

    @Test
    internal fun `checks for active node hop after root node insertion`() {
        val input = "xzyxyxy$"
        val suffixTree = suffixTree(input)
        assertEquals(setOf(4), suffixTree.offsetsOf("yxy$"))
        assertSuffixTreeIsCorrectlyConstructed(input, suffixTree)
    }

    @Test
    internal fun `resets active length and edge when reverting to root after internal node insertion`() {
        val input = "xyyxyyy$"
        assertSuffixTreeIsCorrectlyConstructed(input, suffixTree(input))
    }

    @Test
    internal fun `hopping over internal nodes without adding a new leaf`() {
        val input = "xxyzyxyz$"
        assertSuffixTreeIsCorrectlyConstructed(input, suffixTree(input))
    }

    @Test
    internal fun `doesn't follow suffix links when hopping over internal node`() {
        val input = "xyxyzxyz$"
        assertSuffixTreeIsCorrectlyConstructed(input, suffixTree(input))
    }

    @Test
    internal fun `don't eagerly hop into internal nodes after reverting to root`() {
        val input = "xyyzxyzxy"
        assertSuffixTreeIsCorrectlyConstructed(input, suffixTree(input))
    }

    @Test
    internal fun `create suffix links when passing through internal nodes`() {
        val input = "xxzxxxx$"
        assertSuffixTreeIsCorrectlyConstructed(input, suffixTree(input))
    }

    private fun suffixTree(input: String): SuffixTree {
        val suffixTree = SuffixTree.ukkonenConstruction(input)
        println("\nSuffix Tree for '$input': $suffixTree")
        return suffixTree
    }

    private fun assertSuffixTreeIsCorrectlyConstructed(
        input: String,
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
                    i = input.indexOf(queryString, i + 1)
                    when (i) {
                        -1 -> return indices
                        else -> indices.add(i)
                    }
                }
            }

            try {
                assertEquals(expectedIndices(), suffixTree.offsetsOf(queryString))
            } catch (e: Throwable) {
                println("Query for '$queryString' was incorrect for input '$input'")
                throw e
            }
        }

        // Test the first n characters up to n=5
        (1..minOf(5, input.length - 1)).forEach {
            assertQueryIsCorrect(input.substring(0, it))
        }
        // Test the last n characters up to n=5
        (1..minOf(5, input.length - 1)).forEach {
            assertQueryIsCorrect(input.substring(input.length - it, input.length))
        }
        // Test the full string
        assertQueryIsCorrect(input)
        // Test 200 random substrings for good measure
        repeat(200) {
            val randomInput = RandomInput.generateRandomString(
                alphabet = input + "abcdefghijklmnopqrstuvwxyz$",
                maxLength = input.length
            )
            assertQueryIsCorrect(randomInput)
        }
    }
}