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
package tachyon.commons.io.reader

import java.io.BufferedReader

/**
 * InputStream Reader for byte arrays, support mark
 */
class ByteArrayInputStreamReader(bais: ByteArrayInputStream?, charset: Charset) : InputStreamReader(bais, charset) {
    private val br: BufferedReader
    private val charset: Charset

    constructor(barr: ByteArray?, charset: Charset?) : this(ByteArrayInputStream(barr), charset) {}
    constructor(str: String, charset: Charset?) : this(ByteArrayInputStream(str.getBytes(charset)), charset) {}

    /**
     * @param bais
     * @param charsetName
     * @throws IOException
     */
    @Deprecated
    @Deprecated("""use instead
	              <code>{@link #ByteArrayInputStreamReader(ByteArrayInputStream, Charset)}</code>
	  """)
    constructor(bais: ByteArrayInputStream?, charsetName: String?) : this(bais, CharsetUtil.toCharset(charsetName)) {
    }

    /**
     * @param barr
     * @param charsetName
     * @throws IOException
     */
    @Deprecated
    @Deprecated("""use instead <code>{@link #ByteArrayInputStreamReader(byte[], Charset)}</code>
	  """)
    constructor(barr: ByteArray?, charsetName: String?) : this(ByteArrayInputStream(barr), CharsetUtil.toCharset(charsetName)) {
    }

    /**
     * @param str
     * @param charsetName
     * @throws IOException
     */
    @Deprecated
    @Deprecated("""use instead <code>{@link #ByteArrayInputStreamReader(String, Charset)}</code>
	  """)
    constructor(str: String?, charsetName: String?) : this(str, CharsetUtil.toCharset(charsetName)) {
    }

    @Override
    @Throws(IOException::class)
    fun close() {
        br.close()
    }

    @get:Override
    val encoding: String
        get() = charset.name()

    fun getCharset(): Charset {
        return charset
    }

    @Override
    @Throws(IOException::class)
    fun read(): Int {
        return br.read()
    }

    @Override
    @Throws(IOException::class)
    fun read(cbuf: CharArray?, offset: Int, length: Int): Int {
        return br.read(cbuf, offset, length)
    }

    @Override
    @Throws(IOException::class)
    fun ready(): Boolean {
        return br.ready()
    }

    @Override
    @Throws(IOException::class)
    fun mark(readAheadLimit: Int) {
        br.mark(readAheadLimit)
    }

    @Override
    fun markSupported(): Boolean {
        return br.markSupported()
    }

    @Override
    @Throws(IOException::class)
    fun read(target: CharBuffer): Int {
        return br.read(target.array())
    }

    @Override
    @Throws(IOException::class)
    fun read(cbuf: CharArray?): Int {
        return br.read(cbuf)
    }

    @Override
    @Throws(IOException::class)
    fun reset() {
        br.reset()
    }

    @Override
    @Throws(IOException::class)
    fun skip(n: Long): Long {
        return br.skip(n)
    }

    init {
        br = IOUtil.toBufferedReader(IOUtil.getReader(bais, charset))
        this.charset = charset
    }
}