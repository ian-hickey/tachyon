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
package lucee.runtime.sql.old

import java.io.IOException

class SimpleCharStream(reader: Reader?, i: Int, j: Int, k: Int) {
    private fun ExpandBuff(flag: Boolean) {
        val ac = CharArray(bufsize + 2048)
        val ai = IntArray(bufsize + 2048)
        val ai1 = IntArray(bufsize + 2048)
        try {
            if (flag) {
                System.arraycopy(buffer, tokenBegin, ac, 0, bufsize - tokenBegin)
                System.arraycopy(buffer, 0, ac, bufsize - tokenBegin, bufpos)
                buffer = ac
                System.arraycopy(bufline, tokenBegin, ai, 0, bufsize - tokenBegin)
                System.arraycopy(bufline, 0, ai, bufsize - tokenBegin, bufpos)
                bufline = ai
                System.arraycopy(bufcolumn, tokenBegin, ai1, 0, bufsize - tokenBegin)
                System.arraycopy(bufcolumn, 0, ai1, bufsize - tokenBegin, bufpos)
                bufcolumn = ai1
                bufpos += bufsize - tokenBegin
                maxNextCharInd = bufpos
            } else {
                System.arraycopy(buffer, tokenBegin, ac, 0, bufsize - tokenBegin)
                buffer = ac
                System.arraycopy(bufline, tokenBegin, ai, 0, bufsize - tokenBegin)
                bufline = ai
                System.arraycopy(bufcolumn, tokenBegin, ai1, 0, bufsize - tokenBegin)
                bufcolumn = ai1
                bufpos -= tokenBegin
                maxNextCharInd = bufpos
            }
        } catch (throwable: Throwable) {
            ExceptionUtil.rethrowIfNecessary(throwable)
            throw Error(throwable.getMessage())
        }
        bufsize += 2048
        available = bufsize
        tokenBegin = 0
    }

    @Throws(IOException::class)
    private fun FillBuff() {
        if (maxNextCharInd == available) if (available == bufsize) {
            if (tokenBegin > 2048) {
                maxNextCharInd = 0
                bufpos = maxNextCharInd
                available = tokenBegin
            } else if (tokenBegin < 0) {
                maxNextCharInd = 0
                bufpos = maxNextCharInd
            } else ExpandBuff(false)
        } else if (available > tokenBegin) available = bufsize else if (tokenBegin - available < 2048) ExpandBuff(true) else available = tokenBegin
        var i: Int
        try {
            if (inputStream.read(buffer, maxNextCharInd, available - maxNextCharInd).also { i = it } == -1) {
                inputStream.close()
                throw IOException()
            }
            maxNextCharInd += i
            return
        } catch (ioexception: IOException) {
            bufpos--
            backup(0)
            if (tokenBegin == -1) tokenBegin = bufpos
            throw ioexception
        }
    }

    @Throws(IOException::class)
    fun BeginToken(): Char {
        tokenBegin = -1
        val c = readChar()
        tokenBegin = bufpos
        return c
    }

    private fun UpdateLineColumn(c: Char) {
        column++
        if (prevCharIsLF) {
            prevCharIsLF = false
            column = 1
            line += column
        } else if (prevCharIsCR) {
            prevCharIsCR = false
            if (c == '\n') prevCharIsLF = true else {
                column = 1
                line += column
            }
        }
        when (c) {
            13 -> prevCharIsCR = true
            10 -> prevCharIsLF = true
            9 -> {
                column--
                column += 8 - (column and 7)
            }
        }
        bufline!![bufpos] = line
        bufcolumn!![bufpos] = column
    }

    @Throws(IOException::class)
    fun readChar(): Char {
        if (inBuf > 0) {
            inBuf--
            if (++bufpos == bufsize) bufpos = 0
            return buffer!![bufpos]
        }
        if (++bufpos >= maxNextCharInd) FillBuff()
        val c = buffer!![bufpos]
        UpdateLineColumn(c)
        return c
    }

    @Deprecated
    @Deprecated("Method getColumn is deprecated")
    fun getColumn(): Int {
        return bufcolumn!![bufpos]
    }

    @Deprecated
    @Deprecated("Method getLine is deprecated")
    fun getLine(): Int {
        return bufline!![bufpos]
    }

    val endColumn: Int
        get() = bufcolumn!![bufpos]
    val endLine: Int
        get() = bufline!![bufpos]
    val beginColumn: Int
        get() = bufcolumn!![tokenBegin]
    val beginLine: Int
        get() = bufline!![tokenBegin]

    fun backup(i: Int) {
        inBuf += i
        if (i.let { bufpos -= it; bufpos } < 0) bufpos += bufsize
    }

    constructor(reader: Reader?, i: Int, j: Int) : this(reader, i, j, 4096) {}
    constructor(reader: Reader?) : this(reader, 1, 1, 4096) {}

    fun ReInit(reader: Reader?, i: Int, j: Int, k: Int) {
        inputStream = reader
        line = i
        column = j - 1
        if (buffer == null || k != buffer!!.size) {
            bufsize = k
            available = bufsize
            buffer = CharArray(k)
            bufline = IntArray(k)
            bufcolumn = IntArray(k)
        }
        prevCharIsCR = false
        prevCharIsLF = prevCharIsCR
        maxNextCharInd = 0
        inBuf = maxNextCharInd
        tokenBegin = inBuf
        bufpos = -1
    }

    fun ReInit(reader: Reader?, i: Int, j: Int) {
        ReInit(reader, i, j, 4096)
    }

    fun ReInit(reader: Reader?) {
        ReInit(reader, 1, 1, 4096)
    }

    constructor(inputstream: InputStream?, i: Int, j: Int, k: Int) : this(InputStreamReader(inputstream), i, j, 4096) {}
    constructor(inputstream: InputStream?, i: Int, j: Int) : this(inputstream, i, j, 4096) {}
    constructor(inputstream: InputStream?) : this(inputstream, 1, 1, 4096) {}

    fun ReInit(inputstream: InputStream?, i: Int, j: Int, k: Int) {
        ReInit(InputStreamReader(inputstream), i, j, 4096)
    }

    fun ReInit(inputstream: InputStream?) {
        ReInit(inputstream, 1, 1, 4096)
    }

    fun ReInit(inputstream: InputStream?, i: Int, j: Int) {
        ReInit(inputstream, i, j, 4096)
    }

    fun GetImage(): String? {
        return if (bufpos >= tokenBegin) String(buffer, tokenBegin, bufpos - tokenBegin + 1) else String(buffer, tokenBegin, bufsize - tokenBegin) + String(buffer, 0, bufpos + 1)
    }

    fun GetSuffix(i: Int): CharArray? {
        val ac = CharArray(i)
        if (bufpos + 1 >= i) {
            System.arraycopy(buffer, bufpos - i + 1, ac, 0, i)
        } else {
            System.arraycopy(buffer, bufsize - (i - bufpos - 1), ac, 0, i - bufpos - 1)
            System.arraycopy(buffer, 0, ac, i - bufpos - 1, bufpos + 1)
        }
        return ac
    }

    fun Done() {
        buffer = null
        bufline = null
        bufcolumn = null
    }

    fun adjustBeginLineColumn(i: Int, j: Int) {
        var i = i
        var k = tokenBegin
        val l: Int
        l = if (bufpos >= tokenBegin) bufpos - tokenBegin + inBuf + 1 else bufsize - tokenBegin + bufpos + 1 + inBuf
        var i1 = 0
        var j1 = 0
        // boolean flag = false;
        // boolean flag1 = false;
        var i2 = 0
        var k1: Int
        while (i1 < l && bufline!![k % bufsize.also { j1 = it }] == bufline!![++k % bufsize.also { k1 = it }]) {
            bufline!![j1] = i
            val l1 = i2 + bufcolumn!![k1] - bufcolumn!![j1]
            bufcolumn!![j1] = j + i2
            i2 = l1
            i1++
        }
        if (i1 < l) {
            bufline!![j1] = i++
            bufcolumn!![j1] = j + i2
            while (i1++ < l) if (bufline!![k % bufsize.also { j1 = it }] != bufline!![++k % bufsize]) bufline!![j1] = i++ else bufline!![j1] = i
        }
        line = bufline!![j1]
        column = bufcolumn!![j1]
    }

    var bufsize = 0
    var available = 0
    var tokenBegin = 0
    var bufpos = 0
    private var bufline: IntArray?
    private var bufcolumn: IntArray?
    private var column = 0
    private var line = 0
    private var prevCharIsCR = false
    private var prevCharIsLF = false
    private var inputStream: Reader? = null
    private var buffer: CharArray?
    private var maxNextCharInd = 0
    private var inBuf = 0

    companion object {
        const val staticFlag = false
    }

    init {
        bufpos = -1
        column = 0
        line = 1
        prevCharIsCR = false
        prevCharIsLF = false
        maxNextCharInd = 0
        inBuf = 0
        inputStream = reader
        line = i
        column = j - 1
        bufsize = k
        available = bufsize
        buffer = CharArray(k)
        bufline = IntArray(k)
        bufcolumn = IntArray(k)
    }
}