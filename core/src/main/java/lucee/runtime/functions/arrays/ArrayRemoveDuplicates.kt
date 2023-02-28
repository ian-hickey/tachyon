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
package lucee.runtime.functions.arrays

import lucee.runtime.PageContext

class ArrayRemoveDuplicates : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        return if (args!!.size == 1) call(pc, Caster.toArray(args[0])) else if (args.size == 2) call(pc, Caster.toArray(args[0]), Caster.toBoolean(args[1])) else throw FunctionException(pc, "ArrayRemoveDuplicates", 1, 2, args.size)
    }

    companion object {
        private const val serialVersionUID = 1292207681027528972L
        @Throws(PageException::class)
        fun call(pc: PageContext?, arr: Array?): Array? {
            return call(pc, arr, false)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, arr: Array?, ignoreCase: Boolean): Array? {
            val a: Array = ArrayImpl()
            var i: Int
            i = 1
            while (i <= arr.size()) {
                val value: Object = arr.getE(i)
                if (ArrayFind.find(a, value, !ignoreCase) === 0) a.appendEL(value)
                i++
            }
            return a
        }
    }
}