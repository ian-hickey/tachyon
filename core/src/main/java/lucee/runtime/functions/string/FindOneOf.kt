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
 * Implements the CFML Function findoneof
 */
package lucee.runtime.functions.string

import lucee.runtime.PageContext

class FindOneOf : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 2) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]))
        if (args.size == 3) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toDoubleValue(args[2]))
        throw FunctionException(pc, "FindOneOf", 2, 3, args.size)
    }

    companion object {
        private const val serialVersionUID = -7521748254181624968L
        fun call(pc: PageContext?, set: String?, str: String?): Double {
            return call(pc, set, str, 1.0)
        }

        fun call(pc: PageContext?, strSet: String?, strData: String?, number: Double): Double {
            // strData
            val data: CharArray = strData.toCharArray()
            // set
            val set: CharArray = strSet.toCharArray()
            // start
            var start = number.toInt() - 1
            if (start < 0) start = 0
            if (start >= data.size || set.size == 0) return 0
            // else {
            for (i in start until data.size) {
                for (y in set.indices) {
                    if (data[i] == set[y]) return i + 1
                }
            }
            // }
            return 0
        }
    }
}