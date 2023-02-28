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
package lucee.runtime.net.http

import java.io.ByteArrayInputStream

/**
 * implementation of `ServletInputStream`.
 */
class ServletInputStreamDummy : ServletInputStream {
    private var stream: InputStream? = null

    /**
     * @param data
     */
    constructor(data: ByteArray?) {
        stream = ByteArrayInputStream(data ?: ByteArray(0))
    }

    constructor(file: File?) {
        if (file == null) stream = ByteArrayInputStream(ByteArray(0)) else stream = FileInputStream(file)
    }

    /**
     * @param barr
     */
    constructor(`is`: InputStream?) {
        stream = if (`is` == null) ByteArrayInputStream(ByteArray(0)) else `is`
    }

    @Override
    @Throws(IOException::class)
    fun read(): Int {
        return stream.read()
    }

    @Override
    @Throws(IOException::class)
    fun readLine(barr: ByteArray?, arg1: Int, arg2: Int): Int {
        return stream.read(barr, arg1, arg2)
    }

    @Override
    @Throws(IOException::class)
    fun available(): Int {
        return stream.available()
    }

    @Override
    @Throws(IOException::class)
    fun close() {
        stream.close()
    }

    @Override
    @Synchronized
    fun mark(readlimit: Int) {
        stream.mark(readlimit)
    }

    @Override
    fun markSupported(): Boolean {
        return stream.markSupported()
    }

    @Override
    @Throws(IOException::class)
    fun read(b: ByteArray?, off: Int, len: Int): Int {
        return stream.read(b, off, len)
    }

    @Override
    @Throws(IOException::class)
    fun read(b: ByteArray?): Int {
        return stream.read(b)
    }

    @Override
    @Synchronized
    @Throws(IOException::class)
    fun reset() {
        stream.reset()
    }

    @Override
    @Throws(IOException::class)
    fun skip(n: Long): Long {
        return stream.skip(n)
    }

    @get:Override
    val isFinished: Boolean
        get() {
            throw RuntimeException("not supported!")
        }

    @get:Override
    val isReady: Boolean
        get() {
            throw RuntimeException("not supported!")
        }

    @Override
    fun setReadListener(arg0: ReadListener?) {
        throw RuntimeException("not supported!")
    }
}