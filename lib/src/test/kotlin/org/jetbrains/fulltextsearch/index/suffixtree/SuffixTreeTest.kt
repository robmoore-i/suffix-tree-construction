@file:Suppress("SpellCheckingInspection")

package org.jetbrains.fulltextsearch.index.suffixtree

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SuffixTreeTest {
    @Test
    internal fun `constructs simple suffix tree with only leaf nodes`() {
        val suffixTree: SuffixTree = suffixTree("abcde")
        assertEquals(setOf(0), suffixTree.offsetsOf("a"))
        assertEquals(setOf(1), suffixTree.offsetsOf("bc"))
        assertEquals(setOf(3), suffixTree.offsetsOf("de"))
        assertEquals(setOf(0), suffixTree.offsetsOf("abcde"))
        assertEquals(setOf(4), suffixTree.offsetsOf("e"))
        assertEquals(setOf<Int>(), suffixTree.offsetsOf("x"))
        assertEquals(setOf<Int>(), suffixTree.offsetsOf("xyz"))
        assertEquals(setOf<Int>(), suffixTree.offsetsOf("abd"))
    }

    @Test
    internal fun `constructs simple suffix tree with one internal node`() {
        val suffixTree: SuffixTree = suffixTree("memo")
        assertEquals(setOf(0, 2), suffixTree.offsetsOf("m"))
        assertEquals(setOf(1), suffixTree.offsetsOf("em"))
        assertEquals(setOf(2), suffixTree.offsetsOf("mo"))
        assertEquals(setOf<Int>(), suffixTree.offsetsOf("x"))
        assertEquals(setOf<Int>(), suffixTree.offsetsOf("mex"))
        assertEquals(setOf(0), suffixTree.offsetsOf("memo"))
    }

    @Test
    internal fun `can construct multiple internal nodes on different branches'`() {
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
    internal fun `can construct nested internal nodes'`() {
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

    @Test
    internal fun `queries which fall off at a leaf have no matches`() {
        val suffixTree: SuffixTree = suffixTree("xaxbxac")
        assertEquals(setOf<Int>(), suffixTree.offsetsOf("cxa"))
    }

    @Test
    internal fun `can add all-new leaf nodes from internal nodes`() {
        val suffixTree: SuffixTree = suffixTree("xxaxb")
        assertEquals(setOf(3), suffixTree.offsetsOf("xb"))
    }

    @Test
    internal fun `converts the implicit suffix tree into a true suffix tree`() {
        val suffixTree: SuffixTree = suffixTree("fff")
        assertEquals(setOf(0, 1, 2), suffixTree.offsetsOf("f"))
    }

    @Test
    internal fun `checks for active node hop after following a suffix link`() {
        val suffixTree = suffixTree("xyzxzyxy$")
        assertEquals(setOf(0, 6), suffixTree.offsetsOf("xy"))
        assertEquals(setOf(0), suffixTree.offsetsOf("xyz"))
        assertEquals(setOf<Int>(), suffixTree.offsetsOf("f"))
    }

    @Test
    internal fun `checks for active node hop after root node insertion`() {
        val suffixTree = suffixTree("xzyxyxy$")
        assertEquals(setOf(2, 4), suffixTree.offsetsOf("yxy"))
        assertEquals(setOf(3, 5), suffixTree.offsetsOf("xy"))
    }

    @Test
    internal fun `resets active length and edge when reverting to root after internal node insertion`() {
        val suffixTree = suffixTree("xyyxyyy$")
        assertEquals(setOf(6), suffixTree.offsetsOf("y$"))
        assertEquals(setOf(1, 2, 4, 5, 6), suffixTree.offsetsOf("y"))
        assertEquals(setOf(0, 3), suffixTree.offsetsOf("x"))
        assertEquals(setOf(0, 3), suffixTree.offsetsOf("xy"))
        assertEquals(setOf<Int>(), suffixTree.offsetsOf("xyx"))
        assertEquals(setOf(5), suffixTree.offsetsOf("yy$"))
        assertEquals(setOf(4), suffixTree.offsetsOf("yyy$"))
    }

    @Test
    internal fun `sets suffix link candidates only when splitting an edge`() {
        val suffixTree = suffixTree("xxyzyxyz$")
        assertEquals(setOf(1, 5), suffixTree.offsetsOf("xy"))
        assertEquals(setOf<Int>(), suffixTree.offsetsOf("xyx"))
        assertEquals(setOf<Int>(), suffixTree.offsetsOf("asd"))
        assertEquals(setOf<Int>(), suffixTree.offsetsOf("xxyzyxyzO"))
        assertEquals(setOf(1, 5), suffixTree.offsetsOf("xyz"))
        assertEquals(setOf(4), suffixTree.offsetsOf("yx"))
    }

    private fun suffixTree(inputString: String): SuffixTree {
        val suffixTree = SuffixTree(inputString)
        println("\nSuffix Tree for '$inputString': $suffixTree")
        return suffixTree
    }
}