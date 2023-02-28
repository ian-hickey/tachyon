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
package lucee.runtime.type

import java.util.Map

/**
 *
 */
@SuppressWarnings("rawtypes")
interface Struct : Collection, Map, Objects {
    companion object {
        const val TYPE_UNDEFINED = -1
        const val TYPE_WEAKED = 0
        const val TYPE_LINKED = 1
        const val TYPE_SYNC = 2
        const val TYPE_REGULAR = 3
        const val TYPE_SOFT = 4
    }
}