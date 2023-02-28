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
package lucee.runtime.err

import lucee.runtime.PageSource

/**
 * represent an Error Page
 */
interface ErrorPage {
    /**
     * @return Returns the mailto.
     */
    /**
     * sets the mailto attribute
     *
     * @param mailto mail to address
     */
    var mailto: String?
    /**
     * @return Returns the template.
     */
    /**
     * sets the template attribute
     *
     * @param template template
     */
    var template: lucee.runtime.PageSource?
    /**
     * @return Returns the exception type.
     */
    /**
     * sets the exception attribute
     *
     * @param exception exception
     */
    @get:Deprecated("use instead <code>getException();</code>")
    @get:Deprecated
    @set:Deprecated("use instead <code>setException(String exception);</code>")
    @set:Deprecated
    var typeAsString: String?
    /**
     * @return Returns the exception type.
     */
    /**
     * sets the exception attribute
     *
     * @param exception exception
     */
    var exception: String?
    var type: Short

    companion object {
        const val TYPE_EXCEPTION: Short = 1
        const val TYPE_REQUEST: Short = 2
        const val TYPE_VALIDATION: Short = 4
    }
}