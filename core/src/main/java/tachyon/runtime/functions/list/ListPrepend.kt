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
 * Implements the CFML Function listprepend
 */
package tachyon.runtime.functions.list

import tachyon.runtime.PageContext

class ListPrepend : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 2) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), ",", true)
        if (args.size == 3) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toString(args[2]), true)
        if (args.size == 4) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toString(args[2]), Caster.toBooleanValue(args[3]))
        throw FunctionException(pc, "ListPrepend", 1, 4, args.size)
    }

    companion object {
        private const val serialVersionUID = -4252541560957800011L
        fun call(pc: PageContext?, list: String?, value: String?): String? {
            return call(pc, list, value, ",", true)
        }

        fun call(pc: PageContext?, list: String?, value: String?, delimiter: String?): String? {
            return call(pc, list, value, delimiter, true)
        }

        fun call(pc: PageContext?, list: String?, value: String?, delimiter: String?, includeEmptyFields: Boolean): String? {
            var list = list
            var value = value
            if (list!!.length() === 0) return value
            if (delimiter!!.length() === 0) return list
            val del: Char = delimiter.charAt(0)
            if (!includeEmptyFields) {
                list = ListUtil.listRemoveEmpty(list, del)
                value = ListUtil.listRemoveEmpty(value, del)
            }
            return StringBuilder(value).append(del).append(list).toString()
        }
    }
}