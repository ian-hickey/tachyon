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
package lucee.commons.io.auto

import java.io.IOException

/**
 * Close the Reader automatically when object will destroyed by the garbage
 */
class AutoCloseReader(reader: Reader) : Reader() {
    private val reader: Reader
    @Override
    @Throws(IOException::class)
    fun close() {
        reader.close()
    }

    @Override
    @Throws(IOException::class)
    fun mark(readAheadLimit: Int) {
        reader.mark(readAheadLimit)
    }

    @Override
    fun markSupported(): Boolean {
        return reader.markSupported()
    }

    @Override
    @Throws(IOException::class)
    fun read(): Int {
        return reader.read()
    }

    @Override
    @Throws(IOException::class)
    fun read(cbuf: CharArray?, off: Int, len: Int): Int {
        return reader.read(cbuf, off, len)
    }

    @Override
    @Throws(IOException::class)
    fun read(cbuf: CharArray?): Int {
        return reader.read(cbuf)
    }

    @Override
    @Throws(IOException::class)
    fun ready(): Boolean {
        return reader.ready()
    }

    @Override
    @Throws(IOException::class)
    fun reset() {
        reader.reset()
    }

    @Override
    @Throws(IOException::class)
    fun skip(n: Long): Long {
        return reader.skip(n)
    }

    @Override
    @Throws(Throwable::class)
    fun finalize() {
        super.finalize()
        try {
            reader.close()
        } catch (e: Exception) {
        }
    }

    /**
     * constructor of the class
     *
     * @param reader
     */
    init {
        this.reader = reader
    }
}