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
package tachyon.runtime.interpreter.ref.literal

import java.util.ArrayList

/**
 * ref for a functionValue
 */
class LFunctionValue : RefSupport, Ref {
    private var name: Ref?
    private var refValue: Ref? = null
    private var objValue: Object? = null

    /**
     * constructor of the class
     *
     * @param name
     * @param value
     */
    constructor(name: Ref?, value: Ref?) {
        this.name = name
        refValue = value
    }

    constructor(name: Ref?, value: Object?) {
        this.name = name
        objValue = value
    }

    @Override
    @Throws(PageException::class)
    fun getValue(pc: PageContext?): Object? {
        if (name is Variable) {
            return FunctionValueImpl(toStringArray(pc, name as Set?), if (refValue == null) objValue else refValue.getValue(pc))
        }
        if (name is Literal) {
            return FunctionValueImpl((name as Literal?)!!.getString(pc), if (refValue == null) objValue else refValue.getValue(pc))
        }

        // TODO no idea if this is ever used
        if (name is Set) {
            return FunctionValueImpl(tachyon.runtime.type.util.ListUtil.arrayToList(toStringArray(pc, name as Set?), "."), if (refValue == null) objValue else refValue.getValue(pc))
        }
        throw InterpreterException("invalid syntax in named argument")
        // return new FunctionValueImpl(key,value.getValue());
    }

    @Override
    fun getTypeName(): String? {
        return "function value"
    }

    companion object {
        @Throws(PageException::class)
        fun toStringArray(pc: PageContext?, set: Set?): Array<String?>? {
            var set: Set? = set
            var ref: Ref? = set
            var str: String
            val arr: List<String?> = ArrayList<String?>()
            do {
                set = ref
                str = set.getKeyAsString(pc)
                if (str != null) arr.add(0, str) else break
                ref = set.getParent(pc)
            } while (ref is Set)
            return arr.toArray(arrayOfNulls<String?>(arr.size()))
        }
    }
}