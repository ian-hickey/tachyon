/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.  If not, see <http:></http:>//www.gnu.org/licenses/>.
 *
 */
package tachyon.runtime.text.csv

import java.util.ArrayList

class CSVString(input: String?, delim: Char) {
    private val buffer: CharArray?
    private var pos = 0
    private val delim: Char
    fun parse(): List<List<String?>?>? {
        val result: List<List<String?>?> = ArrayList<List<String?>?>()
        var line: List<String?>? = ArrayList<String?>()
        if (buffer!!.size == 0) return result
        var sb: StringBuilder? = StringBuilder()
        var c: Char
        do {
            c = buffer[pos]
            if (c == '"' || c == '\'') {
                sb.append(fwdQuote(c))
            } else if (c == LF || c == CR) {
                if (c == CR && isNext(LF)) next()
                line.add(sb.toString().trim())
                sb = StringBuilder()
                if (isValidLine(line)) result.add(line)
                line = ArrayList<String?>()
            } else if (c == delim) {
                line.add(sb.toString().trim())
                sb = StringBuilder()
            } else sb.append(c)
            next()
        } while (pos < buffer.size)
        line.add(sb.toString())
        if (isValidLine(line)) result.add(line)
        return result
    }

    /** forward pos until the end of quote  */
    fun fwdQuote(q: Char): StringBuilder? {
        val sb = StringBuilder()
        while (hasNext()) {
            next()
            sb.append(buffer!![pos])
            if (isCurr(q)) {
                if (isNext(q)) { // consecutive quote sign
                    next()
                } else {
                    break
                }
            }
        }
        if (sb.length() > 0) sb.setLength(sb.length() - 1) // remove closing quote sign
        return sb
    }

    operator fun next() {
        pos++
    }

    operator fun hasNext(): Boolean {
        return pos < buffer!!.size - 1
    }

    fun isNext(c: Char): Boolean {
        return if (!hasNext()) false else buffer!![pos + 1] == c
    }

    fun isCurr(c: Char): Boolean {
        return if (!isValidPos) false else buffer!![pos] == c
    }

    val isValidPos: Boolean
        get() = pos >= 0 && pos < buffer!!.size - 1

    fun isValidLine(line: List<String?>?): Boolean {
        for (s in line!!) {
            if (!StringUtil.isEmpty(s, true)) return true
        }
        return false
    }

    companion object {
        private const val LF = 10.toChar()
        private const val CR = 13.toChar()
    }

    init {
        buffer = StringUtil.trim(input, true, false, input).toCharArray()
        this.delim = delim
    }
}