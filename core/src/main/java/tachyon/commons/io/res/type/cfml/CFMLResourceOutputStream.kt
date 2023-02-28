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
package tachyon.commons.io.res.type.cfml

import java.io.ByteArrayOutputStream

class CFMLResourceOutputStream(res: CFMLResource) : OutputStream() {
    private val baos: ByteArrayOutputStream
    private val res: CFMLResource
    @Override
    @Throws(IOException::class)
    fun close() {
        baos.close()
        try {
            res.setBinary(baos.toByteArray())
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            throw ExceptionUtil.toIOException(t)
        } finally {
            res.getResourceProvider().unlock(res)
        }
    }

    @Override
    @Throws(IOException::class)
    fun flush() {
        baos.flush()
    }

    @Override
    @Throws(IOException::class)
    fun write(b: ByteArray?, off: Int, len: Int) {
        baos.write(b, off, len)
    }

    @Override
    @Throws(IOException::class)
    fun write(b: ByteArray?) {
        baos.write(b)
    }

    @Override
    @Throws(IOException::class)
    fun write(b: Int) {
        baos.write(b)
    }

    init {
        this.res = res
        baos = ByteArrayOutputStream()
    }
}