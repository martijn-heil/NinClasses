package com.github.martijn_heil.ninclasses

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger

class LoggerOutputStream(private val target: Logger, private val level: Level) : ByteArrayOutputStream() {
    private val separator = System.getProperty("line.separator")

    @Throws(IOException::class)
    override fun flush() {
        synchronized(this) {
            super.flush()
            val record = this.toString().removeSuffix(separator)
            super.reset()
            if (record.isNotEmpty()) target.log(level, record)
        }
    }
}

fun UUID.toCompressedString() =
        this.mostSignificantBits.toString(16) + this.leastSignificantBits.toString(16)