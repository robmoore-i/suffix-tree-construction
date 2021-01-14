package org.jetbrains.fulltextsearch.filesystem

import java.io.File
import java.nio.file.Path

class Directory(private val dirPath: Path) {
    init {
        val file = dirPath.toFile()
        if (!file.exists()) {
            throw IllegalArgumentException("$dirPath doesn't exist")
        }
        if (!file.isDirectory) {
            throw IllegalArgumentException("$dirPath is not a directory")
        }
    }

    fun <T> forEachFile(transform: (File) -> T): List<T> {
        return dirPath.toFile().walkTopDown()
            .filter { it.isFile }
            .toList().map(transform)
    }


    /**
     * Given a String for the absolute path of a file which is somewhere within
     * this file, this function will remove the leading part of the path String
     * which corresponds to the absolute path of this directory.
     */
    fun relativePathTo(absolutePathOfDescendentFile: String): String {
        if (!absolutePathOfDescendentFile.startsWith(dirPath.toString())) {
            throw IllegalArgumentException(
                "$absolutePathOfDescendentFile is not a descendent of " +
                        "this directory, which is $dirPath"
            )
        }
        return absolutePathOfDescendentFile
            .substring(dirPath.toString().length + 1)
    }
}
