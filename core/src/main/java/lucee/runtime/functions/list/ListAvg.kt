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
 * Implements the CFML Function arrayavg
 */
package lucee.runtime.functions.list

import lucee.runtime.PageContext

class ListAvg : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 1) return call(pc, Caster.toString(args[0]), ",", false)
        if (args.size == 2) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), false)
        if (args.size == 3) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toBooleanValue(args[2]))
        throw FunctionException(pc, "ListAvg", 1, 3, args.size)
    }

    companion object {
        private const val serialVersionUID = -7365055491706152507L
        @Throws(ExpressionException::class)
        fun call(pc: PageContext?, list: String?): Double {
            return call(pc, list, ",", false)
        }

        @Throws(ExpressionException::class)
        fun call(pc: PageContext?, list: String?, delimiter: String?): Double {
            return call(pc, list, delimiter, false)
        }

        @Throws(ExpressionException::class)
        fun call(pc: PageContext?, list: String?, delimiter: String?, multiCharacterDelimiter: Boolean): Double {
            return ArrayUtil.avg(ListUtil.listToArray(list, delimiter, false, multiCharacterDelimiter))
        }
    }
}