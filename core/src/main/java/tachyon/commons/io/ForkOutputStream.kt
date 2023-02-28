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

class ForkOutputStream(os1: OutputStream, os2: OutputStream) : OutputStream() {
    private val os1: OutputStream
    private val os2: OutputStream
    @Override
    @Throws(IOException::class)
    fun close() {
        try {
            os1.close()
        } finally {
            os2.close()
        }
    }

    @Override
    @Throws(IOException::class)
    fun flush() {
        try {
            os1.flush()
        } finally {
            os2.flush()
        }
    }

    @Override
    @Throws(IOException::class)
    fun write(b: ByteArray?, off: Int, len: Int) {
        os1.write(b, off, len)
        os2.write(b, off, len)
    }

    @Override
    @Throws(IOException::class)
    fun write(b: ByteArray?) {
        os1.write(b)
        os2.write(b)
    }

    @Override
    @Throws(IOException::class)
    fun write(b: Int) {
        os1.write(b)
        os2.write(b)
    }

    init {
        this.os1 = os1
        this.os2 = os2
    }
}