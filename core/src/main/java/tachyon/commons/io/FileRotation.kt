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

import java.io.IOException

object FileRotation {
    @Throws(IOException::class)
    fun checkFile(res: Resource, maxFileSize: Long, maxFiles: Int, header: ByteArray?) {
        var res: Resource = res
        var header = header
        var writeHeader = false
        // create file
        if (!res.exists()) {
            res.createFile(true)
            writeHeader = true
        } else if (res.length() === 0) {
            writeHeader = true
        } else if (res.length() > maxFileSize) {
            val parent: Resource = res.getParentResource()
            val name: String = res.getName()
            val lenMaxFileSize: Int = ("" + maxFiles).length()
            for (i in maxFiles downTo 1) {
                val to: Resource = parent.getRealResource(name + "." + StringUtil.addZeros(i, lenMaxFileSize) + ".bak")
                val from: Resource = parent.getRealResource(name + "." + StringUtil.addZeros(i - 1, lenMaxFileSize) + ".bak")
                if (from.exists()) {
                    if (to.exists()) to.delete()
                    from.renameTo(to)
                }
            }
            res.renameTo(parent.getRealResource(name + "." + StringUtil.addZeros(1, lenMaxFileSize) + ".bak"))
            res = parent.getRealResource(name)
            res.createNewFile()
            writeHeader = true
        } else if (header != null && header.size > 0) {
            val buffer = ByteArray(header.size)
            val len: Int
            var `in`: InputStream? = null
            try {
                `in` = res.getInputStream()
                var headerOK = true
                len = `in`.read(buffer)
                if (len == header.size) {
                    for (i in header.indices) {
                        if (header[i] != buffer[i]) {
                            headerOK = false
                            break
                        }
                    }
                } else headerOK = false
                if (!headerOK) writeHeader = true
            } finally {
                IOUtil.close(`in`)
            }
        }
        if (writeHeader) {
            if (header == null) header = ByteArray(0)
            IOUtil.write(res, header, false)
        }
    }
}