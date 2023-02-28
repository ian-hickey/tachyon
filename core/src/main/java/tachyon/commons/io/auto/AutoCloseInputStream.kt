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
package tachyon.commons.io.auto

import java.io.IOException

/**
 * Close the Stream automatically when object will destroyed by the garbage
 */
class AutoCloseInputStream(`is`: InputStream) : InputStream() {
    private val `is`: InputStream
    @Override
    @Throws(IOException::class)
    fun read(): Int {
        return `is`.read()
    }

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
    @Synchronized
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
    @Synchronized
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
    @Throws(Throwable::class)
    fun finalize() {
        super.finalize()
        IOUtil.close(`is`)
    }

    /**
     * constructor of the class
     *
     * @param is
     */
    init {
        this.`is` = `is`
    }
}