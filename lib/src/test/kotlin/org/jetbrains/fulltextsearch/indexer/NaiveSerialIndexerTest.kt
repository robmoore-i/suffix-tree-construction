package org.jetbrains.fulltextsearch.indexer

class NaiveSerialIndexerTest : FullTextSearchTest() {
    override fun indexerUnderTest() = NaiveSerialIndexer()
}