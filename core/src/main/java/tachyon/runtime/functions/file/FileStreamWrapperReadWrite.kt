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

import java.io.File

class FileStreamWrapperReadWrite(res: Resource?, // private BufferedOutputStream bos;
                                 private val charset: String?, private val seekable: Boolean) : FileStreamWrapper(res) {
    private var raf: RandomAccessFile? = null
    private var isEOF = false

    @Override
    @Throws(IOException::class)
    override fun write(obj: Object?) {
        var bytes: ByteArray? = null
        var `is`: InputStream? = null
        if (Decision.isBinary(obj)) {
            bytes = Caster.toBinary(obj, null)
        } else if (obj is FileStreamWrapper) {
            `is` = (obj as FileStreamWrapper?)!!.getResource().getInputStream()
        } else if (obj is Resource) {
            `is` = (obj as Resource?).getInputStream()
        } else { // if(Decision.isSimpleValue(obj)){
            val str: String = Caster.toString(obj, false, null)
            if (str != null) bytes = str.getBytes(charset)
        }
        if (bytes != null) {
            getRAF().write(bytes)
        } else if (`is` != null) {
            writeToRAF(`is`, getRAF())
        } else throw IOException("can't write down object of type [" + Caster.toTypeName(obj).toString() + "] to resource [" + res.toString() + "]")
    }

    @Override
    @Throws(IOException::class)
    override fun close() {
        super.setStatus(FileStreamWrapper.STATE_CLOSE)
        if (raf != null) raf.close()
    }

    @Override
    override fun getMode(): String? {
        return "readwrite"
    }

    @Override
    @Throws(IOException::class)
    override fun read(len: Int): Object? {
        var len = len
        var barr: ByteArray? = ByteArray(len)
        len = getRAF().read(barr)
        if (len != barr!!.size) {
            val rtn = ByteArray(len)
            for (i in 0 until len) {
                rtn[i] = barr[i]
            }
            barr = rtn
            isEOF = true
        }
        return barr
    }

    @Override
    override fun isEndOfFile(): Boolean {
        return isEOF
    }

    @Override
    override fun getSize(): Long {
        return res.length()
    }

    @Override
    @Throws(PageException::class)
    override fun skip(len: Int) {
        try {
            getRAF().skipBytes(len)
        } catch (e: IOException) {
            throw Caster.toPageException(e)
        }
    }

    @Override
    @Throws(PageException::class)
    override fun seek(pos: Long) {
        try {
            getRAF().seek(pos)
        } catch (e: IOException) {
            throw Caster.toPageException(e)
        }
    }

    @Throws(IOException::class)
    private fun getRAF(): RandomAccessFile? {
        if (raf == null) {
            if (res !is File) throw IOException("only resources for local filesytem support seekable")
            raf = RandomAccessFile(res as File?, "rw")
        }
        return raf
    }

    companion object {
        @Throws(IOException::class)
        fun writeToRAF(`is`: InputStream?, raf: RandomAccessFile?) {
            val buffer = ByteArray(2048)
            var tmp = 0
            while (`is`.read(buffer).also { tmp = it } != -1) {
                raf.write(buffer, 0, tmp)
            }
        }
    }
}