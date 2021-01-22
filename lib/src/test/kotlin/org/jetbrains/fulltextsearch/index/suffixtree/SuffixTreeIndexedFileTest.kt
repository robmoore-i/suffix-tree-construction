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
}