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
package tachyon.runtime.functions.list

import tachyon.runtime.PageContext

/**
 * Implements the CFML Function listlast
 */
class ListLast : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 1) return ListUtil.last(Caster.toString(args[0]), ",", true, 1)
        if (args.size == 2) return ListUtil.last(Caster.toString(args[0]), Caster.toString(args[1]), true, 1)
        if (args.size == 3) return ListUtil.last(Caster.toString(args[0]), Caster.toString(args[1]), !Caster.toBooleanValue(args[2]), 1)
        if (args.size == 4) return ListUtil.last(Caster.toString(args[0]), Caster.toString(args[1]), !Caster.toBooleanValue(args[2]), Caster.toIntValue(args[3]))
        throw FunctionException(pc, "ListLast", 1, 4, args.size)
    }

    companion object {
        private const val serialVersionUID = 2822477678831478329L
        fun call(pc: PageContext?, list: String?): String? {
            return ListUtil.last(list, ",", true)
        }

        fun call(pc: PageContext?, list: String?, delimiter: String?): String? {
            return ListUtil.last(list, delimiter, true)
        }

        fun call(pc: PageContext?, list: String?, delimiter: String?, includeEmptyFields: Boolean): String? {
            return ListUtil.last(list, delimiter, !includeEmptyFields)
        }

        fun call(pc: PageContext?, list: String?, delimiter: String?, includeEmptyFields: Boolean, count: Double): String? {
            return if (count == 1.0) ListUtil.last(list, delimiter, !includeEmptyFields) else ListUtil.last(list, delimiter, !includeEmptyFields, count.toInt())
        }
    }
}