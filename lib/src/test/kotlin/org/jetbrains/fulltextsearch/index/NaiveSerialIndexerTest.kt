package org.jetbrains.fulltextsearch.index

class NaiveSerialIndexerTest : FullTextSearchTest() {
    override fun indexerUnderTest() = NaiveSerialIndexer()
}