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
package tachyon.runtime.functions.decision

import tachyon.runtime.PageContext

object IsEmpty : Function {
    private const val serialVersionUID = -2839407878650099024L
    @Throws(PageException::class)
    fun call(pc: PageContext?, value: Object?): Boolean {
        if (value == null) return true
        if (value is Boolean || value is Number) return false
        val len: Double = Len.invoke(value, -1)
        if (len == -1.0) throw FunctionException(pc, "isEmpty", 1, "variable", "this type  [" + Caster.toTypeName(value).toString() + "] is not supported")
        return len == 0.0
    }
}