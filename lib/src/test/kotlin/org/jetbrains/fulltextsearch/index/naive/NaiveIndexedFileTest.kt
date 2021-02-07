@file:Suppress("SpellCheckingInspection")

package org.jetbrains.fulltextsearch.index.naive

import org.jetbrains.fulltextsearch.search.QueryMatch
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class NaiveIndexedFileTest {
    @Test
    internal fun `returns no matches for empty input`() {
        val indexedFile = NaiveIndexedFile("some-file.txt", "my file text")
        assertEquals(listOf<QueryMatch>(), indexedFile.query(""))
    }

    @Test
    internal fun `returns no matches for an empty file`() {
        val indexedFile = NaiveIndexedFile("some-file.txt", "")
        assertEquals(listOf<QueryMatch>(), indexedFile.query("abc"))
        assertEquals(listOf<QueryMatch>(), indexedFile.query("x"))
        assertEquals(listOf<QueryMatch>(), indexedFile.query(""))
    }

    @Test
    internal fun `can query for string 'memo'`() {
        val indexedFile = NaiveIndexedFile("file.txt", "memo")
        assertEquals(setOf(0, 2), indexedFile.offsetsOf("m"))
        assertEquals(setOf(1), indexedFile.offsetsOf("em"))
        assertEquals(setOf(2), indexedFile.offsetsOf("mo"))
        assertEquals(setOf<Int>(), indexedFile.offsetsOf("x"))
        assertEquals(setOf<Int>(), indexedFile.offsetsOf("mex"))
        assertEquals(setOf(0), indexedFile.offsetsOf("memo"))
    }

    @Test
    internal fun `can query for string 'xabxa$'`() {
        val indexedFile = NaiveIndexedFile("file.txt", "xabxa$")
        assertEquals(setOf(0, 3), indexedFile.offsetsOf("x"))
        assertEquals(setOf(2), indexedFile.offsetsOf("bx"))
        assertEquals(setOf(0, 3), indexedFile.offsetsOf("xa"))
        assertEquals(setOf(0), indexedFile.offsetsOf("xab"))
        assertEquals(setOf(5), indexedFile.offsetsOf("$"))
        assertEquals(setOf(0), indexedFile.offsetsOf("xabxa"))
        assertEquals(setOf(0), indexedFile.offsetsOf("xabxa$"))
        assertEquals(setOf<Int>(), indexedFile.offsetsOf("xabxa£"))
        assertEquals(setOf<Int>(), indexedFile.offsetsOf("txa"))
    }

    @Test
    internal fun `can query for string 'xaxbxac'`() {
        val indexedFile = NaiveIndexedFile("file.txt", "xaxbxac")
        assertEquals(setOf(0, 2, 4), indexedFile.offsetsOf("x"))
        assertEquals(setOf(0, 4), indexedFile.offsetsOf("xa"))
        assertEquals(setOf(4), indexedFile.offsetsOf("xac"))
        assertEquals(setOf(3), indexedFile.offsetsOf("b"))
        assertEquals(setOf<Int>(), indexedFile.offsetsOf("xx"))
        assertEquals(setOf(6), indexedFile.offsetsOf("c"))
        assertEquals(setOf(5), indexedFile.offsetsOf("ac"))
        assertEquals(setOf(2), indexedFile.offsetsOf("xbx"))
        assertEquals(setOf(0), indexedFile.offsetsOf("xaxbxac"))
        assertEquals(setOf<Int>(), indexedFile.offsetsOf("xaxcxac"))
        assertEquals(setOf<Int>(), indexedFile.offsetsOf("rx"))
    }

    @Test
    internal fun `can get line of query offset`() {
        assertEquals(
            "hello this is some text",
            NaiveIndexedFile("file.txt", "hello this is some text")
                .getLineOfChar(5)
        )
        assertEquals(
            "has two lines",
            NaiveIndexedFile("file.txt", "this text\nhas two lines")
                .getLineOfChar(15)
        )
        assertEquals(
            "has three lines",
            NaiveIndexedFile("file.txt", "this\nhas three lines\nof text")
                .getLineOfChar(15)
        )
        assertEquals(
            "starts with a line break",
            NaiveIndexedFile("file.txt", "\nstarts with a line break")
                .getLineOfChar(5)
        )
        assertEquals(
            "ends with a line break",
            NaiveIndexedFile("file.txt", "ends with a line break\n")
                .getLineOfChar(5)
        )
    }

    private fun NaiveIndexedFile.offsetsOf(queryString: String): Set<Int> {
        return query(queryString).mapTo(mutableSetOf()) {
            it.offset
        }
    }
}