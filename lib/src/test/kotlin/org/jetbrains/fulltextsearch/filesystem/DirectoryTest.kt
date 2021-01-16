package org.jetbrains.fulltextsearch.filesystem

import org.hamcrest.CoreMatchers.hasItems
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.nio.file.Paths

class DirectoryTest {
    @Test
    internal fun `fails to construct if given File doesn't exist`() {
        assertThrows<RuntimeException> {
            Directory(Paths.get("nonsense"))
        }
    }

    @Test
    internal fun `fails to construct if given File isn't a directory`() {
        assertThrows<RuntimeException> {
            Directory(Paths.get(".gitignore"))
        }
    }

    @Test
    internal fun `can run a function on all the files in a directory`() {
        assertThat(
            Directory(Paths.get("src/test/resources/nested-files")).forEachFile {
                it.name
            },
            hasItems("file-1.txt", "file-2.txt", "file-3.txt")
        )
    }

    @Test
    internal fun `can get the relative path to a file underneath the directory`() {
        val path = "src/test/resources/nested-files"
        val directory = Directory(Paths.get(path))

        val fileOne = "file-1.txt"
        assertEquals(
            fileOne,
            directory.relativePathTo("$path/$fileOne")
        )

        val nestedFile = "nested/file-3.txt"
        assertEquals(
            nestedFile,
            directory.relativePathTo("$path/$nestedFile")
        )
    }

    @Test
    internal fun `throws if you try to get the relative path of a file that isn't within the directory`() {
        val directory = Directory(Paths.get("src/test/resources/nested-files"))
        assertThrows<RuntimeException> {
            directory.relativePathTo("src/test/resources/one-file/file.txt")
        }
    }
}