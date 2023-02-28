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
package tachyon.commons.io

import java.io.IOException

class CountingReader(reader: Reader) : Reader() {
    private val reader: Reader
    private var count = 0
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
        count++
        return reader.read()
    }

    @Override
    @Throws(IOException::class)
    fun read(cbuf: CharArray?): Int {
        return reader.read(cbuf)
    }

    @Override
    @Throws(IOException::class)
    fun read(arg0: CharBuffer): Int {
        return super.read(arg0.array())
    }

    @Override
    @Throws(IOException::class)
    fun ready(): Boolean {
        // TODO Auto-generated method stub
        return super.ready()
    }

    @Override
    @Throws(IOException::class)
    fun reset() {
        // TODO Auto-generated method stub
        super.reset()
    }

    @Override
    @Throws(IOException::class)
    fun skip(n: Long): Long {
        // TODO Auto-generated method stub
        return super.skip(n)
    }

    @Override
    @Throws(IOException::class)
    fun close() {
        // TODO Auto-generated method stub
    }

    @Override
    @Throws(IOException::class)
    fun read(cbuf: CharArray?, off: Int, len: Int): Int {
        // TODO Auto-generated method stub
        return 0
    }

    init {
        this.reader = reader
    }
}