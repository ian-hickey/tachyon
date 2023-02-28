/**
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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

import javax.servlet.ServletException

abstract class AbsServlet : HttpServlet(), EngineChangeListener {
    protected var engine: CFMLEngine? = null
    @Override
    fun onUpdate() {
        try {
            // make sure that config is registered
            engine = CFMLEngineFactory.getInstance(getServletConfig(), this)
        } catch (e: ServletException) {
        }
    }

    companion object {
        private const val serialVersionUID = 3911001884655921666L
    }
}