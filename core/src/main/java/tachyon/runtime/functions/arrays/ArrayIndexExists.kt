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
package tachyon.runtime.functions.arrays

import tachyon.runtime.PageContext

class ArrayIndexExists : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        return if (args!!.size == 2) call(pc, Caster.toArray(args[0]), Caster.toDoubleValue(args[1])) else throw FunctionException(pc, "ArrayIndexExists", 2, 2, args.size)
    }

    companion object {
        private const val serialVersionUID = -4490011932571314711L
        fun call(pc: PageContext?, array: Array?, index: Double): Boolean {
            val _null: Object = NullSupportHelper.NULL(pc)
            return array.get(index.toInt(), _null) !== _null
        }
    }
}