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
 * Implements the CFML Function gethttprequestdata
 */
package tachyon.runtime.functions.other

import java.util.Enumeration

object GetHTTPRequestData : Function {
    private const val serialVersionUID = 1365182999286292317L
    @Throws(PageException::class)
    fun call(pc: PageContext?): Struct? {
        return call(pc, true)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, includeBody: Boolean): Struct? {
        val sct: Struct = StructImpl()
        val headers: Struct = StructImpl()
        val req: HttpServletRequest = pc.getHttpServletRequest()
        val charset: String = pc.getWebCharset().name()
        // headers
        val e: Enumeration = req.getHeaderNames()
        while (e.hasMoreElements()) {
            val key: String = e.nextElement().toString()
            headers.set(KeyImpl.init(ReqRspUtil.decode(key, charset, false)), ReqRspUtil.decode(req.getHeader(key), charset, false))
        }
        sct.set(KeyConstants._headers, headers)
        sct.set(KeyConstants._protocol, req.getProtocol())
        sct.set(KeyConstants._method, req.getMethod())
        if (includeBody) sct.set(KeyConstants._content, ReqRspUtil.getRequestBody(pc, false, ""))
        return sct
    }
}