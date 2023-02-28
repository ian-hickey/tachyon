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
 * Implements the CFML Function urldecode
 */
package lucee.runtime.functions.other

import java.io.UnsupportedEncodingException

object URLDecode : Function {
    @Throws(ExpressionException::class)
    fun call(pc: PageContext?, str: String?): String? {
        return call(pc, str, "utf-8")
    }

    @Throws(ExpressionException::class)
    fun call(pc: PageContext?, str: String?, encoding: String?): String? {
        return try {
            java.net.URLDecoder.decode(str, encoding)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            try {
                URLDecoder.decode(str, encoding, true)
            } catch (uee: UnsupportedEncodingException) {
                throw ExpressionException(uee.getMessage())
            }
        }
        /*
		 * try { return URLDecoder.decode(str,encoding); } catch (UnsupportedEncodingException e) { throw
		 * new ExpressionException(e.getMessage()); }
		 */
    }
}