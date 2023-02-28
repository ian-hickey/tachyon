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

import java.io.IOException

object FileWriteLine {
    @Throws(PageException::class)
    fun call(pc: PageContext?, obj: Object?, text: String?): String? {
        var fsw: FileStreamWrapper? = null
        var close = false
        try {
            try {
                if (obj is FileStreamWrapper) {
                    fsw = obj
                } else {
                    close = true
                    val res: Resource = Caster.toResource(pc, obj, false)
                    pc.getConfig().getSecurityManager().checkFileLocation(res)
                    fsw = FileStreamWrapperWrite(res, (pc as PageContextImpl?).getResourceCharset().name(), false, false)
                }
                fsw!!.write("""
    ${text.toString()}
    
    """.trimIndent())
            } finally {
                if (close && fsw != null) fsw.close()
            }
        } catch (e: IOException) {
            throw Caster.toPageException(e)
        }
        return null
    }
}