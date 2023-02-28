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
package tachyon.commons.io.retirement

import java.io.IOException

class RetireOutputStream(res: Resource, append: Boolean, retireRangeInSeconds: Int, listener: RetireListener?) : OutputStream() {
    private val res: Resource
    private var append: Boolean
    private var os: OutputStream? = null
    private var lastAccess: Long = 0
    private val retireRange: Long
    private val listener: RetireListener?
    private val sync: Object = SerializableObject()

    @get:Throws(IOException::class)
    private val outputStream: OutputStream
        private get() {
            if (os == null) {
                os = res.getOutputStream(append)
                if (os == null) throw IOException("could not open a connection to [$res]")
                if (RetireOutputStreamFactory.isClosed()) return os
                RetireOutputStreamFactory.list.add(this)
                RetireOutputStreamFactory.startThread(retireRange)
            }
            lastAccess = System.currentTimeMillis()
            return os
        }

    @Throws(IOException::class)
    fun retire(): Boolean {
        synchronized(sync) {
            if (os == null || lastAccess + retireRange > System.currentTimeMillis()) {
                // print.e("not retire "+res);
                return false
            }
            // print.e("retire "+res);
            append = true
            close()
            return true
        }
    }

    @Throws(IOException::class)
    fun retireNow(): Boolean {
        synchronized(sync) {
            if (os == null) return false
            append = true
            close()
            return true
        }
    }

    @Override
    @Throws(IOException::class)
    fun close() {
        synchronized(sync) {
            if (os != null) {
                if (listener != null) listener.retire(this)
                try {
                    os.close()
                } finally {
                    RetireOutputStreamFactory.list.remove(this)
                    os = null
                }
            }
        }
    }

    @Override
    @Throws(IOException::class)
    fun flush() {
        synchronized(sync) {
            if (os != null) {
                outputStream.flush()
                if (retireRange == 0L) retireNow()
            }
        }
    }

    @Override
    @Throws(IOException::class)
    fun write(b: Int) {
        synchronized(sync) {
            outputStream.write(b)
            if (retireRange == 0L) retireNow()
        }
    }

    @Override
    @Throws(IOException::class)
    fun write(b: ByteArray?) {
        synchronized(sync) {
            outputStream.write(b)
            if (retireRange == 0L) retireNow()
        }
    }

    @Override
    @Throws(IOException::class)
    fun write(b: ByteArray?, off: Int, len: Int) {
        synchronized(sync) {
            outputStream.write(b, off, len)
            if (retireRange == 0L) retireNow()
        }
    }

    /**
     *
     * @param res
     * @param append
     * @param retireRange retire the stream after given time in minutes
     */
    init {
        this.res = res
        this.append = append
        retireRange = if (retireRangeInSeconds > 0) (retireRangeInSeconds * 1000).toLong() else 0.toLong()
        // print.e("range:"+retireRange);
        this.listener = listener
    }
}