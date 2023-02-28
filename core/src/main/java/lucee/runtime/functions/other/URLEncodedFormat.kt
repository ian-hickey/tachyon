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
/**
 * Implements the CFML Function urlencodedformat
 */
package lucee.runtime.functions.other

import java.io.UnsupportedEncodingException

object URLEncodedFormat : Function {
    private const val serialVersionUID = 5640029138134769481L
    @Throws(PageException::class)
    fun call(pc: PageContext?, str: String?): String? {
        return call(pc, str, "UTF-8", true)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, str: String?, encoding: String?): String? {
        return call(pc, str, encoding, true)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, str: String?, encoding: String?, force: Boolean): String? {
        return invoke(str, encoding, force)
    }

    @Throws(PageException::class)
    operator fun invoke(str: String?, encoding: String?, force: Boolean): String? {
        return if (!force && !ReqRspUtil.needEncoding(str, false)) str else try {
            val enc: String = lucee.commons.net.URLEncoder.encode(str, encoding)
            StringUtil.replace(
                    StringUtil.replace(StringUtil.replace(StringUtil.replace(StringUtil.replace(enc, "+", "%20", false), "*", "%2A", false), "-", "%2D", false), ".", "%2E", false),
                    "_", "%5F", false) // TODO do better
            // return enc;
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            try {
                URLEncoder.encode(str, encoding)
            } catch (e: UnsupportedEncodingException) {
                throw Caster.toPageException(e)
            }
        }
    }
}