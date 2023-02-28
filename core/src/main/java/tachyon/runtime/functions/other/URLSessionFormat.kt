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

import javax.servlet.http.Cookie

object URLSessionFormat : Function {
    private const val serialVersionUID = 1486918425114400713L
    fun call(pc: PageContext?, strUrl: String?): String? {
        var strUrl = strUrl
        val cookies: Array<Cookie?> = ReqRspUtil.getCookies(pc.getHttpServletRequest(), pc.getWebCharset())
        if (!pc.getApplicationContext().isSetClientCookies() || cookies == null) {
            val indexQ: Int = strUrl.indexOf('?')
            val indexA: Int = strUrl.indexOf('&')
            val len: Int = strUrl!!.length()
            if (indexQ == len - 1 || indexA == len - 1) strUrl += pc.getURLToken() else if (indexQ != -1) strUrl += "&" + pc.getURLToken() else strUrl += "?" + pc.getURLToken()
        }
        return strUrl
    }
}