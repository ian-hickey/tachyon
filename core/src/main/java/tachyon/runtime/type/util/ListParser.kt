package tachyon.runtime.type.util

import java.io.IOException

class ListParser(private val str: String?, consumer: ListParserConsumer?, delimeter: Char, quote: Char, ignoreEmpty: Boolean, quoteRequired: Boolean) {
    private var pos = 0
    private val len: Int
    private val delimeter: Char
    private val quote: Char
    private val consumer: ListParserConsumer?
    private val ignoreEmpty: Boolean
    private val quoteRequired: Boolean

    constructor(str: String?, consumer: ListParserConsumer?) : this(str, consumer, ',', '"', true, false) {}
    constructor(str: String?, consumer: ListParserConsumer?, delimeter: Char) : this(str, consumer, delimeter, '"', true, false) {}
    constructor(str: String?, consumer: ListParserConsumer?, delimeter: Char, quote: Char) : this(str, consumer, delimeter, '"', true, false) {}
    constructor(str: String?, consumer: ListParserConsumer?, delimeter: Char, quote: Char, ignoreEmpty: Boolean) : this(str, consumer, delimeter, '"', ignoreEmpty, false) {}

    @Throws(IOException::class)
    fun parse() {
        del()
        while (pos < len) {
            entry()
            if (pos >= len) break else if (str.charAt(pos) === delimeter) {
                pos++
                del()
            } else throw IOException("Invalid Syntax at [" + (pos + 1) + "]: unexpected end")
        }
    }

    // [0=a][1=,][2=,]
    private fun del() {
        remws()
        while (pos < len && str.charAt(pos) === delimeter) {
            pos++
            if (!ignoreEmpty) consumer!!.entry("")
            remws()
        }
        if (pos == len && !ignoreEmpty) consumer!!.entry("")
    }

    @Throws(IOException::class)
    private fun entry() {
        val c: Char = str.charAt(pos)
        // read until we reach ending quote
        if (c == quote) quoted() else {
            if (quoteRequired) throw IOException("Invalid Syntax at [" + (pos + 1) + "]: all values must be between quotes")
            unquoted()
        }
        remws()
    }

    private fun unquoted() {
        var c: Char = str.charAt(pos)
        pos++
        val sb = StringBuilder()
        sb.append(c)
        while (pos < len) {
            c = str.charAt(pos)
            // print.e("=>" + c);
            if (c == delimeter || Character.isWhitespace(c)) break
            sb.append(c)
            pos++
        }
        consumer!!.entry(sb.toString())
    }

    @Throws(IOException::class)
    private fun quoted() {
        val start = pos
        pos++
        var c = 0.toChar()
        val sb = StringBuilder()
        while (pos < len) {
            c = str.charAt(pos)
            if (c == quote) {
                // escape
                if (pos + 1 < len && str.charAt(pos + 1) === quote) {
                    pos++
                } else {
                    pos++
                    break
                }
            }
            sb.append(c)
            pos++
        }
        if (pos == len && c != quote) {
            if (quoteRequired) throw IOException("Invalid Syntax at [" + (pos + 1) + "]: missing ending quote [" + quote + "]")
            pos = start
            unquoted()
            return
        }
        consumer!!.entry(sb.toString())
    }

    private fun remws() {
        while (pos < len) {
            if (!Character.isWhitespace(str.charAt(pos))) break
            pos++
        }
    }

    init {
        this.consumer = consumer
        this.delimeter = delimeter
        this.quote = quote
        this.ignoreEmpty = ignoreEmpty
        this.quoteRequired = quoteRequired
        len = str!!.length()
    }
}