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
 * Close the Writer automatically when object will destroyed by the garbage
 */
class AutoCloseWriter(writer: Writer) : Writer() {
    private val writer: Writer
    @Override
    @Throws(IOException::class)
    fun close() {
        writer.close()
    }

    @Override
    @Throws(IOException::class)
    fun flush() {
        writer.flush()
    }

    @Override
    @Throws(IOException::class)
    fun write(cbuf: CharArray?, off: Int, len: Int) {
        writer.write(cbuf, off, len)
    }

    @Override
    @Throws(IOException::class)
    fun write(cbuf: CharArray?) {
        writer.write(cbuf)
    }

    @Override
    @Throws(IOException::class)
    fun write(c: Int) {
        writer.write(c)
    }

    @Override
    @Throws(IOException::class)
    fun write(str: String?, off: Int, len: Int) {
        writer.write(str, off, len)
    }

    @Override
    @Throws(IOException::class)
    fun write(str: String?) {
        writer.write(str)
    }

    @Override
    @Throws(Throwable::class)
    fun finalize() {
        super.finalize()
        try {
            writer.close()
        } catch (e: Exception) {
        }
    }

    /**
     * constructor of the class
     *
     * @param writer
     */
    init {
        this.writer = writer
    }
}