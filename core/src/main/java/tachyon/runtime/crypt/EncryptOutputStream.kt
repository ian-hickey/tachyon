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
package tachyon.runtime.crypt

import java.io.IOException

/**
 *
 */
class EncryptOutputStream : OutputStream() {
    @Override
    @Throws(IOException::class)
    fun close() {
        super.close()
    }

    @Override
    @Throws(IOException::class)
    fun flush() {
        super.flush()
    }

    @Override
    @Throws(IOException::class)
    fun write(b: ByteArray?, off: Int, len: Int) {
        super.write(b, off, len)
    }

    @Override
    @Throws(IOException::class)
    fun write(b: ByteArray?) {
        super.write(b)
    }

    @Override
    @Throws(IOException::class)
    fun write(b: Int) {
    }

    companion object {
        fun main(args: Array<String?>?) {}
    }
}