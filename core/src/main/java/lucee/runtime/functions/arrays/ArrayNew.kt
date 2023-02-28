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
 * Implements the CFML Function arraynew
 */
package lucee.runtime.functions.arrays

import lucee.commons.lang.StringUtil

class ArrayNew : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 0) return call(pc, 1.0, null, false)
        if (args.size == 1) return call(pc, Caster.toDoubleValue(args[0]), null, false)
        if (args.size == 2) return call(pc, Caster.toDoubleValue(args[0]), Caster.toString(args[1]), false)
        if (args.size == 3) return call(pc, Caster.toDoubleValue(args[0]), Caster.toString(args[1]), Caster.toBooleanValue(args[2]))
        return if (args.size == 4) call(pc, Caster.toDoubleValue(args[0]), Caster.toString(args[1]), Caster.toBooleanValue(args[2])) else throw FunctionException(pc, "ArrayNew", 0, 3, args.size)
    }

    companion object {
        private const val serialVersionUID = -5923269433550568279L
        @Throws(PageException::class)
        fun call(pc: PageContext?, dimension: Double, type: String?, _synchronized: Boolean): Array? {
            val a: Array?
            if (StringUtil.isEmpty(type, true) || Decision.isBoolean(type)) {
                a = ArrayUtil.getInstance(dimension.toInt(), _synchronized)
            } else {
                if (dimension > 1) {
                    throw ApplicationException("multi dimensional arrays are not supported with typed arrays")
                }
                a = ArrayTyped(type.trim())
            }
            return a
        }
    }
}