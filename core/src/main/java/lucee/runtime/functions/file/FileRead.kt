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

import java.io.ByteArrayOutputStream

object FileRead {
    @Throws(PageException::class)
    fun call(pc: PageContext?, path: Object?): Object? {
        return _call(pc, Caster.toResource(pc, path, true), (pc as PageContextImpl?).getResourceCharset().name())
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, obj: Object?, charsetOrSize: Object?): Object? {
        if (charsetOrSize == null) return call(pc, obj)
        if (obj is FileStreamWrapper) {
            return _call(obj as FileStreamWrapper?, Caster.toIntValue(charsetOrSize))
        }
        val res: Resource = Caster.toResource(pc, obj, true)
        var charset: String = Caster.toString(charsetOrSize)
        if (Decision.isInteger(charset)) {
            charset = (pc as PageContextImpl?).getResourceCharset().name()
            return _call(pc, res, charset, Caster.toIntValue(charset))
        }
        return _call(pc, res, charset)
    }

    @Throws(PageException::class)
    private fun _call(fs: FileStreamWrapper?, size: Int): Object? {
        return try {
            fs!!.read(size)
        } catch (e: IOException) {
            throw Caster.toPageException(e)
        }
    }

    @Throws(PageException::class)
    private fun _call(pc: PageContext?, res: Resource?, charset: String?): Object? {
        pc.getConfig().getSecurityManager().checkFileLocation(res)
        return try {
            IOUtil.toString(res, charset)
        } catch (e: IOException) {
            throw Caster.toPageException(e)
        }
    }

    @Throws(PageException::class)
    private fun _call(pc: PageContext?, res: Resource?, charset: String?, size: Int): Object? {
        pc.getConfig().getSecurityManager().checkFileLocation(res)
        var `is`: InputStream? = null
        val baos = ByteArrayOutputStream()
        return try {
            `is` = res.getInputStream()
            IOUtil.copy(`is`, baos, 0, size)
            String(baos.toByteArray(), charset)
        } catch (e: IOException) {
            throw Caster.toPageException(e)
        } finally {
            try {
                IOUtil.close(`is`)
            } catch (e: IOException) {
                throw Caster.toPageException(e)
            }
        }

        // TODO Auto-generated method stub
    }
}