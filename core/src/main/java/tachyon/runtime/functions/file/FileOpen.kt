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

import tachyon.commons.io.res.Resource

object FileOpen {
    @Throws(PageException::class)
    fun call(pc: PageContext?, path: String?): Object? {
        return call(pc, path, "read", (pc as PageContextImpl?).getResourceCharset().name(), false)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, path: String?, mode: String?): Object? {
        return call(pc, path, mode, (pc as PageContextImpl?).getResourceCharset().name(), false)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, path: String?, strMode: String?, charset: String?): Object? {
        return call(pc, path, strMode, charset, false)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, path: String?, strMode: String?, charset: String?, seekable: Boolean): Object? {
        var strMode = strMode
        var charset = charset
        strMode = strMode.trim().toLowerCase()
        if (StringUtil.isEmpty(charset, true)) charset = (pc as PageContextImpl?).getResourceCharset().name()
        // try {
        if ("read".equals(strMode)) {
            return FileStreamWrapperRead(check(pc, ResourceUtil.toResourceExisting(pc, path)), charset, seekable)
        }
        if ("readbinary".equals(strMode)) {
            return FileStreamWrapperReadBinary(check(pc, ResourceUtil.toResourceExisting(pc, path)), seekable)
        }
        if ("write".equals(strMode)) {
            return FileStreamWrapperWrite(check(pc, ResourceUtil.toResourceNotExisting(pc, path)), charset, false, seekable)
        }
        if ("append".equals(strMode)) {
            return FileStreamWrapperWrite(check(pc, ResourceUtil.toResourceNotExisting(pc, path)), charset, true, seekable)
        }
        if ("readwrite".equals(strMode)) {
            return FileStreamWrapperReadWrite(check(pc, ResourceUtil.toResourceNotExisting(pc, path)), charset, seekable)
        }
        throw FunctionException(pc, "FileOpen", 2, "mode", "invalid value [$strMode], valid values for argument mode are [read,readBinary,append,write,readwrite]")
    }

    @Throws(PageException::class)
    private fun check(pc: PageContext?, res: Resource?): Resource? {
        pc.getConfig().getSecurityManager().checkFileLocation(res)
        return res
    }
}