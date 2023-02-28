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
package tachyon.commons.io.res.util

import java.io.IOException

class ResourceOutputStream(res: Resource, os: OutputStream) : OutputStream() {
    private val res: Resource
    private val os: OutputStream
    @Override
    @Throws(IOException::class)
    fun write(b: Int) {
        os.write(b)
    }

    @Override
    @Throws(IOException::class)
    fun close() {
        try {
            os.close()
        } finally {
            res.getResourceProvider().unlock(res)
        }
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

    /**
     * @return the os
     */
    val outputStream: OutputStream
        get() = os

    /**
     * @return the res
     */
    val resource: Resource
        get() = res

    /**
     * Constructor of the class
     *
     * @param res
     * @param os
     */
    init {
        this.res = res
        this.os = os
    }
}