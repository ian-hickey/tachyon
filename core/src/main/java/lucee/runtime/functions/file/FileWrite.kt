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

object FileWrite {
    @Throws(PageException::class)
    fun call(pc: PageContext?, obj: Object?, data: Object?): String? {
        return call(pc, obj, data, (pc as PageContextImpl?).getResourceCharset().name())
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, obj: Object?, data: Object?, charset: String?): String? {
        var charset = charset
        var fsw: FileStreamWrapper? = null
        var close = false
        if (StringUtil.isEmpty(charset, true)) charset = (pc as PageContextImpl?).getResourceCharset().name()
        try {
            try {
                if (obj is FileStreamWrapper) {
                    fsw = obj
                } else {
                    close = true
                    val res: Resource = Caster.toResource(pc, obj, false)
                    pc.getConfig().getSecurityManager().checkFileLocation(res)
                    val parent: Resource = res.getParentResource()
                    //if (parent != null && !parent.exists())  throw new FunctionException(pc, "FileWrite", 1, "source", "parent directory for [" + res + "] doesn't exist");
                    fsw = FileStreamWrapperWrite(res, charset, false, false)
                }
                fsw!!.write(data)
                /* see LDEV-4081
				try { 
					fsw.write(data);
				}
				catch (IOException e) {
					throw new FunctionException(pc, "FileWrite", 1, "source", "Invalid file [" + Caster.toResource(pc, obj, false)  + "]",e.getMessage());
				}
				*/
            } finally {
                if (close && fsw != null) fsw.close()
            }
        } catch (e: IOException) {
            throw Caster.toPageException(e)
        }
        return null
    }
}