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
package tachyon.runtime.functions.file

import java.io.BufferedReader

class FileStreamWrapperRead
/**
 * Constructor of the class
 *
 * @param res
 * @param charset
 * @throws IOException
 */(res: Resource?, private val charset: String?, private val seekable: Boolean) : FileStreamWrapper(res) {
    private var br: BufferedReader? = null
    private var raf: RandomAccessFile? = null

    @Override
    @Throws(IOException::class)
    override fun read(len: Int): Object? {
        var len = len
        if (seekable) {
            val barr = ByteArray(len)
            len = getRAF().read(barr)
            if (len == -1) throw IOException("End of file reached")
            return String(barr, 0, len, charset)
        }
        val carr = CharArray(len)
        len = _getBR().read(carr)
        if (len == -1) throw IOException("End of file reached")
        return String(carr, 0, len)
    }

    @Override
    @Throws(IOException::class)
    override fun readLine(): String? {
        if (seekable) {
            return getRAF().readLine()
        }
        if (!_getBR().ready()) throw IOException(" End of file reached")
        return _getBR().readLine()
    }

    @Override
    @Throws(IOException::class)
    override fun close() {
        super.setStatus(FileStreamWrapper.STATE_CLOSE)
        if (br != null) br.close()
        if (raf != null) raf.close()
    }

    @Override
    override fun getMode(): String? {
        return "read"
    }

    @Override
    override fun isEndOfFile(): Boolean {
        if (seekable) {
            var pos: Long = 0
            pos = try {
                getRAF().getFilePointer()
            } catch (ioe: IOException) {
                throw PageRuntimeException(Caster.toPageException(ioe))
            }
            try {
                if (raf.read() === -1) return true
                raf.seek(pos)
            } catch (e: IOException) {
                return true
            }
            return false
        }
        return try {
            !_getBR().ready()
        } catch (e: IOException) {
            true
        }
    }

    @Override
    override fun getSize(): Long {
        return res.length()
    }

    @Override
    @Throws(PageException::class)
    override fun skip(len: Int) {
        if (seekable) {
            try {
                getRAF().skipBytes(len)
            } catch (e: IOException) {
                throw Caster.toPageException(e)
            }
            return
        }
        try {
            _getBR().skip(len)
            return
        } catch (e: IOException) {
        }
        throw Caster.toPageException(IOException("skip is only supported when you have set argument seekable of function fileOpen to true"))
    }

    @Override
    @Throws(PageException::class)
    override fun seek(pos: Long) {
        if (seekable) {
            try {
                getRAF().seek(pos)
            } catch (e: IOException) {
                throw Caster.toPageException(e)
            }
        } else throw Caster.toPageException(IOException("seek is only supported when you have set argument seekable of function fileOpen to true"))
    }

    @Throws(IOException::class)
    private fun getRAF(): RandomAccessFile? {
        if (raf == null) {
            if (res !is File) throw IOException("only resources for local filesytem support seekable")
            raf = RandomAccessFile(res as File?, "r")
        }
        return raf
    }

    @Throws(IOException::class)
    private fun _getBR(): BufferedReader? {
        if (br == null) {
            br = IOUtil.toBufferedReader(IOUtil.getReader(res.getInputStream(), charset))
        }
        return br
    }
}