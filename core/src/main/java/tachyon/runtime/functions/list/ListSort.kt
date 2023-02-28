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
 * Implements the CFML Function listsort
 */
package tachyon.runtime.functions.list

import tachyon.runtime.PageContext

class ListSort : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 2) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]))
        if (args.size == 3) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toString(args[2]))
        if (args.size == 4) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toString(args[2]), Caster.toString(args[3]))
        if (args.size == 5) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toString(args[2]), Caster.toString(args[3]), Caster.toBooleanValue(args[4]))
        throw FunctionException(pc, "ListSort", 2, 5, args.size)
    }

    companion object {
        private const val serialVersionUID = -1153055612742304078L
        @Throws(PageException::class)
        fun call(pc: PageContext?, list: String?): String? {
            return call(pc, list, "textnocase", "asc", ",", false)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, list: String?, sortType: String?): String? {
            return call(pc, list, sortType, "asc", ",", false)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, list: String?, sortType: String?, sortOrder: String?): String? {
            return call(pc, list, sortType, sortOrder, ",", false)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, list: String?, sortType: String?, sortOrder: String?, delimiter: String?): String? {
            return call(pc, list, sortType, sortOrder, delimiter, false)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, list: String?, sortType: String?, sortOrder: String?, delimiter: String?, includeEmptyFields: Boolean): String? {
            return if (includeEmptyFields) ListUtil.sort(list, sortType, sortOrder, delimiter) else ListUtil.sortIgnoreEmpty(ListUtil.trim(list, delimiter), sortType, sortOrder, delimiter)
        }
    }
}