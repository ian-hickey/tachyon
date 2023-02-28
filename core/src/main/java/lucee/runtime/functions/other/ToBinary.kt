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
 * Implements the CFML Function tobinary
 */
package lucee.runtime.functions.other

import java.nio.charset.Charset

object ToBinary : Function {
    private const val serialVersionUID = 4541724601337401920L
    @Throws(PageException::class)
    fun call(pc: PageContext?, data: Object?): ByteArray? {
        return call(pc, data, null)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, data: Object?, charset: String?): ByteArray? {
        var charset = charset
        if (!StringUtil.isEmpty(charset)) {
            charset = charset.trim().toLowerCase()
            var cs: Charset
            if ("web".equalsIgnoreCase(charset)) cs = pc.getWebCharset()
            cs = if ("resource".equalsIgnoreCase(charset)) (pc as PageContextImpl?).getResourceCharset() else CharsetUtil.toCharset(charset)
            val str: String = Caster.toString(data)
            return str.getBytes(cs)
        }
        return Caster.toBinary(data)
    }
}