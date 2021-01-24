package org.jetbrains.fulltextsearch.index.suffixtree

import java.io.OutputStream

@Suppress("unused")
object Debugger {
    private var debug = false

    private val doNothingOutputStream = object : OutputStream() {
        override fun write(b: Int) {}
    }

    private var outputStream: OutputStream = doNothingOutputStream

    fun info(s: String) {
        outputStream.write(s.toByteArray())
    }

    fun debug(s: String) {
        if (debug) {
            outputStream.write(s.toByteArray())
        }
    }

    fun enableIf(function: () -> Boolean) {
        if (function()) {
            enable()
        } else {
            disable()
        }
    }

    private fun enable() {
        outputStream = System.out
    }

    private fun disable() {
        outputStream = doNothingOutputStream
    }

    fun debugFor(function: () -> Unit) {
        val wasEnabled = outputStream == System.out
        val wasDebug = debug
        enable()
        debug = true
        function.invoke()
        if (!wasEnabled) {
            disable()
        }
        debug = wasDebug
    }

    fun enableFor(function: () -> Unit) {
        val wasEnabled = outputStream == System.out
        enable()
        function.invoke()
        if (!wasEnabled) {
            disable()
        }
    }
}