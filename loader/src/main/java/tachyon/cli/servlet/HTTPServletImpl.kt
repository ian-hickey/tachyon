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
package tachyon.cli.servlet

import javax.servlet.ServletConfig

class HTTPServletImpl(config: ServletConfig, context: ServletContext, servletName: String) : HttpServlet() {
    private val config: ServletConfig
    private val context: ServletContext

    /**
     * @see javax.servlet.GenericServlet.getServletName
     */
    @get:Override
    val servletName: String

    /**
     * @see javax.servlet.GenericServlet.getServletConfig
     */
    @get:Override
    val servletConfig: ServletConfig
        get() = config

    /**
     * @see javax.servlet.GenericServlet.getServletContext
     */
    @get:Override
    val servletContext: ServletContext
        get() = context

    companion object {
        private const val serialVersionUID = 3270816399105433603L
    }

    init {
        this.config = config
        this.context = context
        this.servletName = servletName
    }
}