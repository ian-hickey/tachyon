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
package tachyon.commons.io.res.type.compress

import java.io.IOException

class CompressOutputStreamSynchronizer(os: OutputStream, zip: Compress, async: Boolean) : OutputStream() {
    private val os: OutputStream
    private val zip: Compress
    private val async: Boolean
    @Override
    @Throws(IOException::class)
    fun close() {
        os.close()
        zip.synchronize(async)
    }

    @Override
    @Throws(IOException::class)
    fun flush() {
        os.flush()
    }

    @Override
    @Throws(IOException::class)
    fun write(b: Int) {
        os.write(b)
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

    init {
        this.os = os
        this.zip = zip
        this.async = async
    }
}