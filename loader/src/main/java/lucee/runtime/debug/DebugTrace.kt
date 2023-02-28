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
package lucee.runtime.debug

import java.io.Serializable

interface DebugTrace : Serializable {
    /**
     * @return the category
     */
    val category: String?

    /**
     * @return the line
     */
    val line: Int

    /**
     * @return the template
     */
    val template: String?

    /**
     * @return the text
     */
    val text: String?

    /**
     * @return the time
     */
    val time: Long

    /**
     * @return the type
     */
    val type: Int

    /**
     * @return the var
     */
    val varName: String?

    /**
     * @return the var
     */
    val varValue: String?
    val action: String?

    companion object {
        const val TYPE_INFO = 0
        const val TYPE_DEBUG = 1
        const val TYPE_WARN = 2
        const val TYPE_ERROR = 3
        const val TYPE_FATAL = 4
        const val TYPE_TRACE = 5
    }
}