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
package tachyon.runtime.exp

import javax.servlet.jsp.JspException

/**
 * root Exception for Tachyon runtime
 */
abstract class PageException
/**
 * constructor of the class
 *
 * @param message error message
 */
(message: String?) : JspException(message), IPageException, Dumpable {
    abstract var exposeMessage: Boolean

    companion object {
        private const val serialVersionUID = 2057718592238914705L
    }
}