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
package lucee.runtime.functions.list

import lucee.runtime.PageContext

/**
 * Implements the CFML Function listfind
 */
class ListFind : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 2) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), ",", false)
        if (args.size == 3) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toString(args[2]), false)
        if (args.size == 4) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toString(args[2]), Caster.toBooleanValue(args[3]))
        throw FunctionException(pc, "ListFind", 2, 4, args.size)
    }

    companion object {
        private const val serialVersionUID = -8999117503012321225L
        fun call(pc: PageContext?, list: String?, value: String?): Double {
            return ListUtil.listFindIgnoreEmpty(list, value, ',') + 1
        }

        fun call(pc: PageContext?, list: String?, value: String?, delimter: String?): Double {
            return ListUtil.listFindIgnoreEmpty(list, value, delimter) + 1
        }

        fun call(pc: PageContext?, list: String?, value: String?, delimter: String?, includeEmptyFields: Boolean): Double {
            return if (includeEmptyFields) ListUtil.listFind(list, value, delimter) + 1 else ListUtil.listFindIgnoreEmpty(list, value, delimter) + 1
        }
    }
}