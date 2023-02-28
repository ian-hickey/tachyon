/**
 * Copyright (c) 2023, TachyonCFML.org
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

import java.io.IOException

class RequestDispatcherWrap(req: HTTPServletRequestWrap?, private var realPath: String?) : RequestDispatcher {
    private val req: HTTPServletRequestWrap?
    @Override
    @Throws(ServletException::class, IOException::class)
    fun forward(req: ServletRequest?, rsp: ServletResponse?) {
        var req: ServletRequest? = req
        val pc: PageContext = ThreadLocalPageContext.get()
        req = HTTPUtil.removeWrap(req)
        if (pc == null) {
            this.req!!.getOriginalRequestDispatcher(realPath).forward(req, rsp)
            return
        }
        realPath = HTTPUtil.optimizeRealPath(pc, realPath)
        try {
            val disp: RequestDispatcher = this.req!!.getOriginalRequestDispatcher(realPath)
            disp.forward(req, rsp)
        } finally {
            ThreadLocalPageContext.register(pc)
        }
    }

    @Override
    @Throws(ServletException::class, IOException::class)
    fun include(req: ServletRequest?, rsp: ServletResponse?) {
        val pc: PageContext = ThreadLocalPageContext.get()
        if (pc == null) {
            this.req!!.getOriginalRequestDispatcher(realPath).include(req, rsp)
            return
        }
        HTTPUtil.include(pc, req, rsp, realPath)
    }

    init {
        this.req = req
    }
}