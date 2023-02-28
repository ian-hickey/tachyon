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
package lucee.loader.servlet

import java.io.IOException

/**
 */
class AMFServlet : AbsServlet() {
    /**
     * @see javax.servlet.Servlet.init
     */
    @Override
    @Throws(ServletException::class)
    fun init(sg: ServletConfig?) {
        super.init(sg)
        // do not get engine here, because then it is possible that the engine is initialized with this
        // values
    }

    /**
     * @see javax.servlet.http.HttpServlet.service
     */
    @Override
    @Throws(ServletException::class, IOException::class)
    protected fun service(req: HttpServletRequest?, rsp: HttpServletResponse?) {
        if (engine == null) engine = CFMLEngineFactory.getInstance(getServletConfig(), this)
        engine.serviceAMF(this, req, rsp)
    }

    companion object {
        private const val serialVersionUID = 2545934355390532318L
    }
}