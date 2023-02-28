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
package lucee.runtime.interpreter.ref.op

import lucee.runtime.PageContext

/**
 * Plus operation
 */
class Negate(ref: Ref?, limited: Boolean) : RefSupport(), Ref {
    private val ref: Ref?
    private val limited: Boolean
    @Override
    @Throws(PageException::class)
    fun getValue(pc: PageContext?): Object? {
        return Double.valueOf(-Caster.toDoubleValue(ref.getValue(pc)))
    }

    @Override
    fun getTypeName(): String? {
        return "operation"
    }

    /**
     * constructor of the class
     *
     * @param ref
     */
    init {
        this.ref = ref
        this.limited = limited
    }
}