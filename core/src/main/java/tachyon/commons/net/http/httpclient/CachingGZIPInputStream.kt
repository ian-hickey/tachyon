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
package tachyon.commons.net.http.httpclient

import java.io.ByteArrayInputStream

class CachingGZIPInputStream(`is`: InputStream?) : InputStream() {
    private val barr: ByteArray
    private val `is`: GZIPInputStream
    @Override
    @Throws(IOException::class)
    fun available(): Int {
        return `is`.available()
    }

    @Override
    @Throws(IOException::class)
    fun close() {
        `is`.close()
    }

    @Override
    fun mark(readlimit: Int) {
        `is`.mark(readlimit)
    }

    @Override
    fun markSupported(): Boolean {
        return `is`.markSupported()
    }

    @Override
    @Throws(IOException::class)
    fun read(b: ByteArray?, off: Int, len: Int): Int {
        return `is`.read(b, off, len)
    }

    @Override
    @Throws(IOException::class)
    fun read(b: ByteArray?): Int {
        return `is`.read(b)
    }

    @Override
    @Throws(IOException::class)
    fun reset() {
        `is`.reset()
    }

    @Override
    @Throws(IOException::class)
    fun skip(n: Long): Long {
        return `is`.skip(n)
    }

    @Override
    @Throws(IOException::class)
    fun read(): Int {
        return `is`.read()
    }

    val rawData: InputStream
        get() = ByteArrayInputStream(barr)

    init {
        barr = IOUtil.toBytes(`is`, true)
        this.`is` = GZIPInputStream(ByteArrayInputStream(barr))
    }
}