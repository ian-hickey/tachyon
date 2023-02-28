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
package lucee.loader.util

import java.io.File

object ZipUtil {
    @Throws(IOException::class)
    fun zip(src: File, trgZipFile: File) {
        if (trgZipFile.isDirectory()) throw IllegalArgumentException("argument trgZipFile is the name of an existing directory")
        val zos = ZipOutputStream(FileOutputStream(trgZipFile))
        try {
            if (src.isFile()) addEntries(zos, src.getParentFile(), src) else if (src.isDirectory()) addEntries(zos, src, src.listFiles())
        } finally {
            Util.closeEL(zos)
        }
    }

    @Throws(IOException::class)
    private fun addEntries(zos: ZipOutputStream, root: File, vararg files: File) {
        if (files != null) for (file in files) {

            // directory
            if (file.isDirectory()) {
                addEntries(zos, root, file.listFiles())
                continue
            }
            if (!file.isFile()) continue

            // file
            var `is`: InputStream? = null
            val ze: ZipEntry = generateZipEntry(root, file)
            try {
                zos.putNextEntry(ze)
                copy(FileInputStream(file).also { `is` = it }, zos)
            } finally {
                closeEL(`is`)
                zos.closeEntry()
            }
        }
    }

    private fun generateZipEntry(root: File, file: File): ZipEntry {
        val strRoot: String = root.getAbsolutePath()
        val strFile: String = file.getAbsolutePath()
        return ZipEntry(strFile.substring(strRoot.length() + 1, strFile.length()))
    }

    @Throws(IOException::class)
    private fun copy(`in`: InputStream, out: OutputStream) {
        val buffer = ByteArray(0xffff)
        var len: Int
        while (`in`.read(buffer).also { len = it } != -1) out.write(buffer, 0, len)
    }

    private fun closeEL(`is`: InputStream?) {
        if (`is` == null) return
        try {
            `is`.close()
        } catch (t: Throwable) {
        }
    }
}