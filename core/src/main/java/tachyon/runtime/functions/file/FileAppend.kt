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

import java.io.IOException

object FileAppend {
    @Throws(PageException::class)
    fun call(pc: PageContext?, file: Object?, data: Object?): String? {
        return call(pc, file, data, (pc as PageContextImpl?).getResourceCharset().name())
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, file: Object?, data: Object?, charset: String?): String? {
        var charset = charset
        var fsw: FileStreamWrapper? = null
        if (StringUtil.isEmpty(charset, true)) charset = (pc as PageContextImpl?).getResourceCharset().name()
        try {
            if (file is FileStreamWrapper) {
                fsw = file
            } else {
                val res: Resource = Caster.toResource(pc, file, false)
                pc.getConfig().getSecurityManager().checkFileLocation(res)
                fsw = FileStreamWrapperWrite(res, charset, true, false)
            }
            fsw!!.write(data)
        } catch (e: IOException) {
            throw Caster.toPageException(e)
        } finally {
            closeEL(fsw)
        }
        return null
    }

    private fun closeEL(fsw: FileStreamWrapper?) {
        if (fsw == null) return
        try {
            fsw.close()
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
        }
    }
}