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
 * Implements the CFML Function arraydeleteat
 */
package lucee.runtime.functions.arrays

import lucee.runtime.PageContext

class ArrayDelete : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        return if (args!!.size == 2) _call(pc, Caster.toArray(args[0]), args[1], null, true) else if (args.size == 3) _call(pc, Caster.toArray(args[0]), args[1], Caster.toString(args[2]), true) else throw FunctionException(pc, "ArrayDelete", 2, 3, args.size)
    }

    companion object {
        private const val serialVersionUID = 1120923916196967210L
        @Throws(PageException::class)
        fun call(pc: PageContext?, array: Array?, value: Object?): Boolean {
            return _call(pc, array, value, null, true)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, array: Array?, value: Object?, scope: String?): Boolean {
            return _call(pc, array, value, scope, true)
        }

        @Throws(PageException::class)
        fun _call(pc: PageContext?, array: Array?, value: Object?, scope: String?, caseSensitive: Boolean): Boolean {
            val onlyFirst: Boolean = !"all".equalsIgnoreCase(scope)
            var pos: Double
            if (find(pc, array, value, caseSensitive).also { pos = it } > 0) {
                array.removeE(pos.toInt())
                if (onlyFirst) return true
            } else return false
            while (find(pc, array, value, caseSensitive).also { pos = it } > 0) {
                array.removeE(pos.toInt())
            }
            return true
        }

        @Throws(PageException::class)
        private fun find(pc: PageContext?, array: Array?, value: Object?, caseSensitive: Boolean): Double {
            return if (caseSensitive) ArrayFind.call(pc, array, value) else ArrayFindNoCase.call(pc, array, value)
        }
    }
}