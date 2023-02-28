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
package lucee.runtime.functions.file

import lucee.commons.io.IOUtil

object FileGetMimeType {
    @Throws(PageException::class)
    fun call(pc: PageContext?, oSrc: Object?): String? {
        return call(pc, oSrc, true)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, oSrc: Object?, checkHeader: Boolean): String? {
        var src: Resource? = null
        var barr: ByteArray? = null
        try {
            src = Caster.toResource(pc, oSrc, false)
        } catch (e: ExpressionException) {
            barr = Caster.toBinary(oSrc, null)
            if (barr == null) throw e
        }
        if (barr != null) {
            val mimeType: String = IOUtil.getMimeType(barr, null)
            return if (StringUtil.isEmpty(mimeType, true)) "application/octet-stream" else mimeType
        }
        if (!src.exists()) {
            if (checkHeader) {
                throw FunctionException(pc, "FileGetMimeType", 1, "file", "File [$src] does not exist, strict was true")
            } else {
                val mimeType: String = IOUtil.getMimeType(src.getName(), null)
                if (!StringUtil.isEmpty(mimeType)) return mimeType
                throw FunctionException(pc, "FileGetMimeType", 1, "file", "File [$src] does not exist and couldn't detect mimetype from the file extension.")
            }
        }
        pc.getConfig().getSecurityManager().checkFileLocation(src)
        if (checkHeader && src.length() === 0) throw FunctionException(pc, "FileGetMimeType", 1, "file", "File [$src] was empty, strict was true")
        val mimeType: String = ResourceUtil.getMimeType(src, null)
        return if (StringUtil.isEmpty(mimeType, true)) "application/octet-stream" else mimeType
    }
}