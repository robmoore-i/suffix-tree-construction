package org.jetbrains.fulltextsearch.search

import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.jetbrains.fulltextsearch.index.NaiveIndexedFile
import org.junit.jupiter.api.Test

class IndexedDirectoryTest {
    @Test
    internal fun `finds one match`() {
        val indexedDirectory =
            IndexedDirectory(
                listOf(
                    NaiveIndexedFile(
                        "hello-world.txt",
                        "hello world"
                    )
                )
            )

        val queryResults: List<QueryMatch> =
            indexedDirectory.queryCaseSensitive("world")
        assertThat(queryResults, hasSize(1))
        assertThat(
            queryResults, hasItems(
                QueryMatch("hello-world.txt", 6)
            )
        )
    }

    @Test
    internal fun `finds multiple matches`() {
        val indexedDirectory =
            IndexedDirectory(listOf(NaiveIndexedFile("file.txt", "abracadabra")))

        val queryResults: List<QueryMatch> =
            indexedDirectory.queryCaseSensitive("abra")
        assertThat(queryResults, hasSize(2))
        assertThat(
            queryResults, hasItems(
                QueryMatch("file.txt", 0),
                QueryMatch("file.txt", 7)
            )
        )
    }

    @Test
    internal fun `can run search queries in parallel`() {
        val indexedDirectory =
            IndexedDirectory(listOf(NaiveIndexedFile("file.txt", "abracadabra")))

        val one = QueryMatchListener.MatchCollector()
        val two = QueryMatchListener.MatchCollector()
        runBlocking {
            indexedDirectory.queryCaseSensitivesAsync("abra", one)
            indexedDirectory.queryCaseSensitivesAsync("ra", two)
        }

        assertThat(one.matches(), hasSize(2))
        assertThat(
            one.matches(), hasItems(
                QueryMatch("file.txt", 0),
                QueryMatch("file.txt", 7)
            )
        )
        assertThat(two.matches(), hasSize(2))
        assertThat(
            two.matches(), hasItems(
                QueryMatch("file.txt", 2),
                QueryMatch("file.txt", 9)
            )
        )
    }

    @Test
    internal fun `doesn't send query matches event if no matches found`() {
        val indexedDirectory = IndexedDirectory(
            listOf(
                NaiveIndexedFile("file-1.txt", "abracadabra"),
                NaiveIndexedFile("file-2.txt", "nothing interesting")
            )
        )
        val eventCounter = object : QueryMatchListener {
            var eventCounter = 0

            override fun onQueryMatches(matches: List<QueryMatch>) {
                eventCounter++
            }

            fun numberOfReceivedEvents(): Int {
                return eventCounter
            }
        }

        runBlocking {
            indexedDirectory.queryCaseSensitivesAsync("abra", eventCounter)
        }

        assertThat(eventCounter.numberOfReceivedEvents(), equalTo(1))
    }
}