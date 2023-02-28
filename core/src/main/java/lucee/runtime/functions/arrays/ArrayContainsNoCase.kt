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
/**
 * Implements the CFML Function listcontainsnocase
 */
package lucee.runtime.functions.arrays

import lucee.runtime.PageContext

class ArrayContainsNoCase : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        return if (args!!.size == 2) call(pc, Caster.toArray(args[0]), args[1]) else if (args.size == 3) call(pc, Caster.toArray(args[0]), args[1], Caster.toBooleanValue(args[2])) else throw FunctionException(pc, "ArrayContainsNoCase", 2, 3, args.size)
    }

    companion object {
        private const val serialVersionUID = 4394078979692450076L
        @Throws(PageException::class)
        fun call(pc: PageContext?, array: Array?, value: Object?): Double {
            return ArrayFindNoCase.call(pc, array, value)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, array: Array?, value: Object?, substringMatch: Boolean): Double {
            if (substringMatch) {
                if (!Decision.isSimpleValue(value)) throw FunctionException(pc, "ArrayContainsNoCase", 3, "substringMatch",
                        "substringMatch can not be true when the value that is searched for is a complex object")
                val str: String = Caster.toString(value, null)
                return if (str != null) ArrayUtil.arrayContainsIgnoreEmpty(array, str, true) + 1 else ArrayFind.call(pc, array, value)
            }
            return ArrayFindNoCase.call(pc, array, value)
        }
    }
}