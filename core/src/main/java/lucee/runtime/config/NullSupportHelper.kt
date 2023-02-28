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
package lucee.runtime.config

import lucee.runtime.PageContext

object NullSupportHelper {
    @JvmOverloads
    fun full(pc: PageContext? = ThreadLocalPageContext.get()): Boolean {
        var pc: PageContext? = pc
        if (pc == null) {
            pc = ThreadLocalPageContext.get()
            if (pc == null) return false
        }
        return (pc as PageContextImpl?).getFullNullSupport()
    }

    fun NULL(fns: Boolean): Object? {
        return if (fns) Null.NULL else null
    }

    fun NULL(pc: PageContext?): Object? {
        return if (full(pc)) Null.NULL else null
    }

    fun NULL(): Object? {
        return if (full()) Null.NULL else null
    }

    fun empty(pc: PageContext?): Object? {
        return if (full(pc)) null else ""
    }
}