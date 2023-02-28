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
package tachyon.runtime.functions.other

import java.io.UnsupportedEncodingException

object URLEncode {
    @Throws(PageException::class)
    fun call(pc: PageContext?, str: String?): String? {
        return invoke(str, "UTF-8", true)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, str: String?, encoding: String?): String? {
        return invoke(str, encoding, true)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, str: String?, encoding: String?, force: Boolean): String? {
        return invoke(str, encoding, force)
    }

    @Throws(PageException::class)
    operator fun invoke(str: String?, encoding: String?, force: Boolean): String? {
        return if (!force && !ReqRspUtil.needEncoding(str, false)) str else try {
            URLEncoder.encode(str, encoding)
        } catch (e: UnsupportedEncodingException) {
            throw Caster.toPageException(e)
        }
    }
}