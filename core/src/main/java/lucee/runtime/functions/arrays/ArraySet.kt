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
 * Implements the CFML Function arrayset
 */
package lucee.runtime.functions.arrays

import lucee.runtime.PageContext

class ArraySet : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        return if (args!!.size == 4) call(pc, Caster.toArray(args[0]), Caster.toDoubleValue(args[1]), Caster.toDoubleValue(args[2]), args[3]) else throw FunctionException(pc, "ArraySet", 4, 4, args.size)
    }

    companion object {
        private const val serialVersionUID = -7804363479876538167L
        @Throws(PageException::class)
        fun call(pc: PageContext?, array: Array?, from: Double, to: Double, value: Object?): Boolean {
            val f = from.toInt()
            val t = to.toInt()
            if (f < 1) throw ExpressionException("Start index of the function arraySet must be greater than zero; now [$f]")
            if (f > t) throw ExpressionException("End index of the function arraySet must be greater than the Start index; now [start:$f, end:$t]")
            if (array.getDimension() > 1) throw ExpressionException("Function arraySet can only be used with a one-dimensional array; this array has " + array.getDimension().toString() + " dimensions")
            for (i in f..t) {
                array.setE(i, Duplicator.duplicate(value, true))
            }
            return true
        }
    }
}