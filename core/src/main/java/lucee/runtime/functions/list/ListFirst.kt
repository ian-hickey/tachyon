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
 * Implements the CFML Function listfirst
 */
package lucee.runtime.functions.list

import lucee.runtime.PageContext

class ListFirst : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 1) return call(pc, Caster.toString(args[0]), ",", false, 1.0)
        if (args.size == 2) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), false, 1.0)
        if (args.size == 3) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toBooleanValue(args[2]), 1.0)
        if (args.size == 4) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toBooleanValue(args[2]), Caster.toDoubleValue(args[3]))
        throw FunctionException(pc, "ListFirst", 1, 4, args.size)
    }

    companion object {
        private const val serialVersionUID = 1098339742182832847L
        @Throws(FunctionException::class)
        fun call(pc: PageContext?, list: String?): String? {
            return ListUtil.first(list, ",", true, 1)
        }

        @Throws(FunctionException::class)
        fun call(pc: PageContext?, list: String?, delimiter: String?): String? {
            return ListUtil.first(list, delimiter, true, 1)
        }

        @Throws(FunctionException::class)
        fun call(pc: PageContext?, list: String?, delimiter: String?, includeEmptyFields: Boolean): String? {
            return ListUtil.first(list, delimiter, !includeEmptyFields, 1)
        }

        @Throws(FunctionException::class)
        fun call(pc: PageContext?, list: String?, delimiter: String?, includeEmptyFields: Boolean, count: Double): String? {
            if (count < 1) throw FunctionException(pc, "ListFirst", 4, "count", "Argument count must be a positive value greater than 0")
            return ListUtil.first(list, delimiter, !includeEmptyFields, count.toInt())
        }
    }
}