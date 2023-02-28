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
package tachyon.runtime.interpreter.ref.op

import tachyon.runtime.PageContext

class Elvis(left: Ref?, right: Ref?, limited: Boolean) : RefSupport(), Ref {
    private val left: Ref?
    private val right: Ref?
    private val limited: Boolean
    @Override
    @Throws(PageException::class)
    fun getValue(pc: PageContext?): Object? {
        if (limited) throw InterpreterException("invalid syntax, this operation is not supported in a json string.")
        if (left is Variable) {
            val `var`: Variable? = left as Variable?
            val arr: Array<String?> = LFunctionValue.toStringArray(pc, `var`)
            return if (tachyon.runtime.op.Elvis.operate(pc, arr)) left.getValue(pc) else right.getValue(pc)
        }
        val `val`: Object = left.getValue(pc)
        return if (`val` != null) `val` else right.getValue(pc)
    }

    @Override
    fun getTypeName(): String? {
        return "operation"
    }

    init {
        this.left = left
        this.right = right
        this.limited = limited
    }
}