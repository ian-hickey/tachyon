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
class AutoCloseOutputStream(os: OutputStream) : OutputStream() {
    private val os: OutputStream
    @Override
    @Throws(IOException::class)
    fun write(b: Int) {
        os.write(b)
    }

    @Override
    @Throws(IOException::class)
    fun close() {
        os.close()
    }

    @Override
    @Throws(IOException::class)
    fun flush() {
        os.flush()
    }

    @Override
    @Throws(IOException::class)
    fun write(b: ByteArray?, off: Int, len: Int) {
        os.write(b, off, len)
    }

    @Override
    @Throws(IOException::class)
    fun write(b: ByteArray?) {
        os.write(b)
    }

    @Override
    @Throws(Throwable::class)
    fun finalize() {
        super.finalize()
        try {
            os.close()
        } catch (e: Exception) {
        }
    }

    /**
     * constructor of the class
     *
     * @param os
     */
    init {
        this.os = os
    }
}