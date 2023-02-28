/**
 * Copyright (c) 2021, Lucee Assosication Switzerland. All rights reserved.
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
 */
package lucee.runtime.functions.other

import lucee.runtime.PageContext

/**
 * Returns a Struct with the HTTP Request Headers
 */
class GetHTTPRequestHeaders : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        return call(pc)
    }

    companion object {
        @Throws(PageException::class)
        fun call(pc: PageContext?): Struct? {
            val req: HttpServletRequest = pc.getHttpServletRequest()
            val charset: String = pc.getWebCharset().name()
            val result: Struct = StructImpl()
            val e: Enumeration = req.getHeaderNames()
            while (e.hasMoreElements()) {
                val key: String = e.nextElement().toString()
                result.set(KeyImpl.init(ReqRspUtil.decode(key, charset, false)), ReqRspUtil.decode(req.getHeader(key), charset, false))
            }
            return result
        }
    }
}