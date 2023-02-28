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
 * Implements the CFML Function arrayinsertat
 */
package tachyon.runtime.functions.arrays

import tachyon.runtime.PageContext

class ArrayInsertAt : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        return if (args!!.size == 3) call(pc, Caster.toArray(args[0]), Caster.toDoubleValue(args[1]), args[2]) else throw FunctionException(pc, "ArrayInsertAt", 3, 3, args.size)
    }

    companion object {
        private const val serialVersionUID = -418752384898360791L
        @Throws(PageException::class)
        fun call(pc: PageContext?, array: Array?, number: Double, `object`: Object?): Boolean {
            return array.insert(number.toInt(), `object`)
        }
    }
}