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
package tachyon.runtime.net.http

import java.util.ArrayList

object HttpUtil {
    /**
     * read all headers from request and return it
     *
     * @param req
     * @return
     */
    fun cloneHeaders(req: HttpServletRequest?): Array<Pair<String?, String?>?>? {
        val headers: List<Pair<String?, String?>?> = ArrayList<Pair<String?, String?>?>()
        val e: Enumeration<String?> = req.getHeaderNames()
        var ee: Enumeration<String?>
        var name: String
        while (e.hasMoreElements()) {
            name = e.nextElement()
            ee = req.getHeaders(name)
            while (ee.hasMoreElements()) {
                headers.add(Pair<String?, String?>(name, ee.nextElement().toString()))
            }
        }
        return headers.toArray(arrayOfNulls<Pair?>(headers.size())) as Array<Pair<String?, String?>?>?
    }

    fun getAttributesAsStruct(req: HttpServletRequest?): Struct? {
        val attributes: Struct = StructImpl()
        val e: Enumeration = req.getAttributeNames()
        var name: String
        while (e.hasMoreElements()) {
            name = e.nextElement() // MUST (hhlhgiug) can throw ConcurrentModificationException
            if (name != null) attributes.setEL(name, req.getAttribute(name))
        }
        return attributes
    }

    fun getAttributes(req: HttpServletRequest?): Array<Pair<String?, Object?>?>? {
        val attributes: List<Pair<String?, Object?>?> = ArrayList<Pair<String?, Object?>?>()
        val e: Enumeration = req.getAttributeNames()
        var name: String
        while (e.hasMoreElements()) {
            name = e.nextElement()
            attributes.add(Pair<String?, Object?>(name, req.getAttribute(name)))
        }
        return attributes.toArray(arrayOfNulls<Pair?>(attributes.size()))
    }

    fun cloneParameters(req: HttpServletRequest?): Array<Pair<String?, String?>?>? {
        val parameters: List<Pair<String?, String?>?> = ArrayList<Pair<String?, String?>?>()
        val e: Enumeration = req.getParameterNames()
        var values: Array<String?>
        var name: String
        while (e.hasMoreElements()) {
            name = e.nextElement()
            values = req.getParameterValues(name)
            if (values == null && ReqRspUtil.needEncoding(name, false)) values = req.getParameterValues(ReqRspUtil.encode(name, ReqRspUtil.getCharacterEncoding(null, req)))
            if (values == null) {
                val pc: PageContext = ThreadLocalPageContext.get()
                if (pc != null && ReqRspUtil.identical(pc.getHttpServletRequest(), req)) {
                    values = HTTPServletRequestWrap.getParameterValues(ThreadLocalPageContext.get(), name)
                }
            }
            if (values != null) for (i in values.indices) {
                parameters.add(Pair<String?, String?>(name, values[i]))
            }
        }
        return parameters.toArray(arrayOfNulls<Pair?>(parameters.size()))
    }

    fun cloneCookies(config: Config?, req: HttpServletRequest?): Array<Cookie?>? {
        val src: Array<Cookie?> = ReqRspUtil.getCookies(req, CharsetUtil.getWebCharset())
                ?: return arrayOfNulls<Cookie?>(0)
        val dest: Array<Cookie?> = arrayOfNulls<Cookie?>(src.size)
        for (i in src.indices) {
            dest[i] = src[i].clone() as Cookie
        }
        return dest
    }
}