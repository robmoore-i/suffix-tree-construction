@file:Suppress("SpellCheckingInspection")

package org.jetbrains.fulltextsearch.randominput

import java.util.*
import kotlin.streams.asSequence

object RandomInput {
    private val random = Random()

    fun generateRandomString(
        alphabet: String = "abcdefghijklmnopqrstuvwxyz ",
        minLength: Int = 1,
        maxLength: Int = 20
    ): String {
        @Suppress("SpellCheckingInspection")
        val sizeOfRandomString: Long =
            random.longs(1, minLength.toLong(), maxLength.toLong())
                .findFirst()
                .orElseThrow { IllegalStateException("Couldn't generate a random string length") }

        return random.ints(sizeOfRandomString, 0, alphabet.length)
            .asSequence()
            .map(alphabet::get)
            .joinToString("")
    }
}