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
package tachyon.runtime.interpreter.ref.`var`

import tachyon.runtime.PageContext

class Assign : RefSupport, Ref {
    private var value: Ref?
    private var coll: Set?
    private val limited: Boolean

    constructor(coll: Ref?, value: Ref?, limited: Boolean) {
        if (coll !is Set) throw InterpreterException("Invalid assignment left-hand side [" + coll.getTypeName().toString() + "]")
        this.coll = coll
        this.value = value
        this.limited = limited
    }

    constructor(coll: Set?, value: Ref?, limited: Boolean) {
        this.coll = coll
        this.value = value
        this.limited = limited
    }

    @Override
    @Throws(PageException::class)
    fun getValue(pc: PageContext?): Object? {
        if (limited) throw InterpreterException("Invalid syntax, variables are not supported in a JSON string.")
        return coll.setValue(pc, value.getValue(pc))
    }

    @Override
    fun getTypeName(): String? {
        return "operation"
    }
}