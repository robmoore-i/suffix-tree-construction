@file:Suppress("SpellCheckingInspection")

package org.jetbrains.fulltextsearch.index.suffixtree

import org.jetbrains.fulltextsearch.randominput.RandomInput
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SuffixTreeTest {

    @Test
    internal fun `constructs simple suffix tree with only leaf nodes`() {
        assertSuffixTreeIsCorrectlyConstructed("abcde")
    }

    @Test
    internal fun `constructs simple suffix tree with one internal node`() {
        assertSuffixTreeIsCorrectlyConstructed("memo")
    }

    @Test
    internal fun `can construct multiple internal nodes on different branches'`() {
        assertSuffixTreeIsCorrectlyConstructed("xabxa$")
    }

    @Test
    internal fun `can construct nested internal nodes'`() {
        assertSuffixTreeIsCorrectlyConstructed("xaxbxac")
    }

    @Test
    internal fun `can add all-new leaf nodes from internal nodes`() {
        assertSuffixTreeIsCorrectlyConstructed("xxaxb")
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
        assertSuffixTreeIsCorrectlyConstructed("yyxyyz")
    }

    @Test
    internal fun `checks for active node hop after following a suffix link`() {
        assertSuffixTreeIsCorrectlyConstructed("xyzxzyxy$")
    }

    @Test
    internal fun `checks for active node hop after root node insertion`() {
        val suffixTree = suffixTree("xzyxyxy$")
        assertEquals(setOf(4), suffixTree.offsetsOf("yxy$"))
        assertSuffixTreeIsCorrectlyConstructed("xzyxyxy$", suffixTree)
    }

    @Test
    internal fun `resets active length and edge when reverting to root after internal node insertion`() {
        assertSuffixTreeIsCorrectlyConstructed("xyyxyyy$")
    }

    @Test
    internal fun `hopping over internal nodes without adding a new leaf`() {
        assertSuffixTreeIsCorrectlyConstructed("xxyzyxyz$")
    }

    @Test
    internal fun `doesn't follow suffix links when hopping over internal node`() {
        assertSuffixTreeIsCorrectlyConstructed("xyxyzxyz$")
    }

    @Test
    internal fun `don't eagerly hop into internal nodes after reverting to root`() {
        assertSuffixTreeIsCorrectlyConstructed("xyyzxyzxy")
    }

    @Test
    internal fun `create suffix links when passing through internal nodes`() {
        assertSuffixTreeIsCorrectlyConstructed("xxzxxxx$")
    }

    @Test
    internal fun `create suffix links when reaching end of edge between internal nodes`() {
        assertSuffixTreeIsCorrectlyConstructed("yxzzxzxz$")
    }

    @Test
    internal fun `edge label validation doesn't go beyond the label length`() {
        assertSuffixTreeIsCorrectlyConstructed("xaxyaxax$")
    }

    @Test
    internal fun `recursively normalizes the active node`() {
        assertSuffixTreeIsCorrectlyConstructed("xyyyxyyy$")
    }

    @Test
    internal fun `create suffix links during active node normalization`() {
        assertSuffixTreeIsCorrectlyConstructed("xxyxxaxxa$")
        assertSuffixTreeIsCorrectlyConstructed("zxxyzxzzx$")
        assertSuffixTreeIsCorrectlyConstructed("xxzxzzxza")
    }

    private fun suffixTree(input: String): SuffixTree {
        val suffixTree = SuffixTree.ukkonenConstruction(input)
        println("\nSuffix Tree for '$input': $suffixTree")
        return suffixTree
    }

    private fun assertSuffixTreeIsCorrectlyConstructed(
        input: String
    ) {
        assertSuffixTreeIsCorrectlyConstructed(input, suffixTree(input))
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