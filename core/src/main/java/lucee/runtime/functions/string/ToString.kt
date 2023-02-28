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
 * Implements the CFML Function tostring
 */
package lucee.runtime.functions.string

import java.nio.charset.Charset

object ToString : Function {
    fun call(pc: PageContext?): String? {
        return ""
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, `object`: Object?): String? {
        return call(pc, `object`, null)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, `object`: Object?, encoding: String?): String? {
        val charset: Charset
        charset = if (StringUtil.isEmpty(encoding)) {
            ReqRspUtil.getCharacterEncoding(pc, pc.getResponse())
        } else CharsetUtil.toCharset(encoding)
        return if (`object` is ByteArray) {
            if (charset != null) String(`object` as ByteArray?, charset) else String(`object` as ByteArray?)
        } else Caster.toString(`object`)
    }
}