package org.jetbrains.fulltextsearch.index.suffixtree

import org.jetbrains.fulltextsearch.search.QueryMatch
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class SuffixTreeIndexedFileTest {
    @Test
    internal fun `returns no matches for empty input`() {
        val indexedFile = SuffixTreeIndexedFile("some-file.txt", "my file text")
        assertEquals(listOf<QueryMatch>(), indexedFile.query(""))
    }

    @Test
    internal fun `returns no matches for an empty file`() {
        val indexedFile = SuffixTreeIndexedFile("some-file.txt", "")
        assertEquals(listOf<QueryMatch>(), indexedFile.query("abc"))
        assertEquals(listOf<QueryMatch>(), indexedFile.query("x"))
        assertEquals(listOf<QueryMatch>(), indexedFile.query(""))
    }

    @Test
    internal fun `can get line of query offset`() {
        assertEquals(
            "hello this is some text",
            SuffixTreeIndexedFile("file.txt", "hello this is some text")
                .getLineOfChar(5)
        )
        assertEquals(
            "has two lines",
            SuffixTreeIndexedFile("file.txt", "this text\nhas two lines")
                .getLineOfChar(15)
        )
        assertEquals(
            "has three lines",
            SuffixTreeIndexedFile("file.txt", "this\nhas three lines\nof text")
                .getLineOfChar(15)
        )
        assertEquals(
            "starts with a line break",
            SuffixTreeIndexedFile("file.txt", "\nstarts with a line break")
                .getLineOfChar(5)
        )
        assertEquals(
            "ends with a line break",
            SuffixTreeIndexedFile("file.txt", "ends with a line break\n")
                .getLineOfChar(5)
        )
    }
}