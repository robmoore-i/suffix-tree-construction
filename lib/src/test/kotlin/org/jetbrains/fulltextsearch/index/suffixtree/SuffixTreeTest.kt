@file:Suppress("SpellCheckingInspection")

package org.jetbrains.fulltextsearch.index.suffixtree

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SuffixTreeTest {
    @Test
    internal fun `constructs suffix tree for string 'memo'`() {
        val suffixTree: SuffixTree = suffixTree("memo")
        assertEquals(setOf(0, 2), suffixTree.offsetsOf("m"))
        assertEquals(setOf(1), suffixTree.offsetsOf("em"))
        assertEquals(setOf(2), suffixTree.offsetsOf("mo"))
        assertEquals(setOf<Int>(), suffixTree.offsetsOf("x"))
        assertEquals(setOf<Int>(), suffixTree.offsetsOf("mex"))
        assertEquals(setOf(0), suffixTree.offsetsOf("memo"))
    }

    @Test
    internal fun `constructs suffix tree for string 'xabxa$'`() {
        val suffixTree: SuffixTree = suffixTree("xabxa$")
        assertEquals(setOf(0, 3), suffixTree.offsetsOf("x"))
        assertEquals(setOf(2), suffixTree.offsetsOf("bx"))
        assertEquals(setOf(0, 3), suffixTree.offsetsOf("xa"))
        assertEquals(setOf(0), suffixTree.offsetsOf("xab"))
        assertEquals(setOf(5), suffixTree.offsetsOf("$"))
        assertEquals(setOf(0), suffixTree.offsetsOf("xabxa"))
        assertEquals(setOf(0), suffixTree.offsetsOf("xabxa$"))
        assertEquals(setOf<Int>(), suffixTree.offsetsOf("xabxa£"))
        assertEquals(setOf<Int>(), suffixTree.offsetsOf("txa"))
    }

    @Test
    internal fun `constructs suffix tree for string 'xaxbxac'`() {
        val suffixTree: SuffixTree = suffixTree("xaxbxac")
        assertEquals(setOf(0, 2, 4), suffixTree.offsetsOf("x"))
        assertEquals(setOf(0, 4), suffixTree.offsetsOf("xa"))
        assertEquals(setOf(4), suffixTree.offsetsOf("xac"))
        assertEquals(setOf(3), suffixTree.offsetsOf("b"))
        assertEquals(setOf<Int>(), suffixTree.offsetsOf("xx"))
        assertEquals(setOf(6), suffixTree.offsetsOf("c"))
        assertEquals(setOf(5), suffixTree.offsetsOf("ac"))
        assertEquals(setOf(2), suffixTree.offsetsOf("xbx"))
        assertEquals(setOf(0), suffixTree.offsetsOf("xaxbxac"))
        assertEquals(setOf<Int>(), suffixTree.offsetsOf("xaxcxac"))
        assertEquals(setOf<Int>(), suffixTree.offsetsOf("rx"))
    }

    private fun suffixTree(inputString: String): SuffixTree {
        val suffixTree = SuffixTree(inputString)
        println("\nSuffix Tree for '$inputString': $suffixTree")
        return suffixTree
    }
}