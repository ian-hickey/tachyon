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
 * Implements the CFML Function structkeyexists
 */
package lucee.runtime.functions.arrays

import lucee.runtime.PageContext

class ArrayIsDefined : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        return if (args!!.size == 2) call(pc, Caster.toArray(args[0]), Caster.toDoubleValue(args[1])) else throw FunctionException(pc, "ArrayIsDefined", 2, 2, args.size)
    }

    companion object {
        private const val serialVersionUID = 5821478169641360902L
        fun call(pc: PageContext?, array: Array?, index: Double): Boolean {
            return ArrayIndexExists.call(pc, array, index)
        }
    }
}