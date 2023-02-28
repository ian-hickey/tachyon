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

import java.io.ByteArrayInputStream

class TemporaryStream : OutputStream() {
    private var persis: Resource? = null
    private var count: Long = 0
    private var os: OutputStream
    var memoryMode = true
    var available = false
    @Override
    @Throws(IOException::class)
    fun write(b: Int) {
        count++
        check()
        os.write(b)
    }

    @Override
    @Throws(IOException::class)
    fun write(b: ByteArray?, off: Int, len: Int) {
        count += len.toLong()
        check()
        os.write(b, off, len)
    }

    @Override
    @Throws(IOException::class)
    fun write(b: ByteArray) {
        count += b.size.toLong()
        check()
        os.write(b)
    }

    @Throws(IOException::class)
    private fun check() {
        if (memoryMode && count >= MAX_MEMORY && os is java.io.ByteArrayOutputStream) {
            memoryMode = false
            val nos: OutputStream = persis.getOutputStream()
            nos.write((os as java.io.ByteArrayOutputStream).toByteArray())
            os = nos
        }
    }

    @Override
    @Throws(IOException::class)
    fun close() {
        os.close()
        available = true
    }

    @Override
    @Throws(IOException::class)
    fun flush() {
        os.flush()
    }

    @get:Throws(IOException::class)
    val inputStream: InputStream
        get() = InpuStreamWrap(this)

    internal inner class InpuStreamWrap(private val ts: TemporaryStream) : InputStream() {
        private var `is`: InputStream? = null
        private val sync: Object = SerializableObject()
        @Override
        @Throws(IOException::class)
        fun read(): Int {
            return `is`.read()
        }

        @Override
        @Throws(IOException::class)
        fun available(): Int {
            return `is`.available()
        }

        @Override
        @Throws(IOException::class)
        fun close() {
            ts.persis.delete()
            `is`.close()
        }

        @Override
        fun mark(readlimit: Int) {
            synchronized(sync) { `is`.mark(readlimit) }
        }

        @Override
        fun markSupported(): Boolean {
            return `is`.markSupported()
        }

        @Override
        @Throws(IOException::class)
        fun read(b: ByteArray?, off: Int, len: Int): Int {
            return `is`.read(b, off, len)
        }

        @Override
        @Throws(IOException::class)
        fun read(b: ByteArray?): Int {
            return `is`.read(b)
        }

        @Override
        @Throws(IOException::class)
        fun reset() {
            synchronized(sync) { `is`.reset() }
        }

        @Override
        @Throws(IOException::class)
        fun skip(n: Long): Long {
            return `is`.skip(n)
        }

        init {
            if (ts.os is java.io.ByteArrayOutputStream) {
                `is` = ByteArrayInputStream((ts.os as java.io.ByteArrayOutputStream).toByteArray())
            } else if (ts.available) {
                ts.available = false
                `is` = try {
                    ts.persis.getInputStream()
                } catch (e: IOException) {
                    ts.persis.delete()
                    throw e
                }
            } else throw IOException("InputStream no longer available")
        }
    }

    fun length(): Long {
        return count
    }

    companion object {
        private const val MAX_MEMORY = 1024 * 1024
        private var index = 1
        private var tempFile: Resource? = null// tempFile=CFMLEngineFactory.getInstance().getCastUtil().toResource(tmp.getParent(),null);

        // tempFile=CFMLEngineFactory.getInstance().getCastUtil().toResource(tmpStr,null);
        val tempDirectory: Resource?
            get() {
                if (tempFile != null) return tempFile
                val tmpStr: String = System.getProperty("java.io.tmpdir")
                if (tmpStr != null) {
                    tempFile = ResourceUtil.toResourceNotExisting(ThreadLocalPageContext.get(), tmpStr)
                    // tempFile=CFMLEngineFactory.getInstance().getCastUtil().toResource(tmpStr,null);
                    if (tempFile != null && tempFile.exists()) {
                        tempFile = getCanonicalResourceEL(tempFile)
                        return tempFile
                    }
                }
                var tmp: File? = null
                try {
                    tmp = File.createTempFile("a", "a")
                    tempFile = ResourceUtil.toResourceNotExisting(ThreadLocalPageContext.get(), tmp.getParent())
                    // tempFile=CFMLEngineFactory.getInstance().getCastUtil().toResource(tmp.getParent(),null);
                    tempFile = getCanonicalResourceEL(tempFile)
                } catch (ioe: IOException) {
                } finally {
                    if (tmp != null) tmp.delete()
                }
                return tempFile
            }

        private fun getCanonicalResourceEL(res: Resource?): Resource {
            return try {
                res.getCanonicalResource()
            } catch (e: IOException) {
                res.getAbsoluteResource()
            }
        }
    }

    /**
     * Constructor of the class
     */
    init {
        do {
            persis = tempDirectory.getRealResource("temporary-stream-" + index++)
        } while (persis.exists())
        os = ByteArrayOutputStream()
    }
}