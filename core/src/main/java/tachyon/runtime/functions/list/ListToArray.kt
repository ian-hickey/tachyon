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
 * Implements the CFML Function listtoarray
 */
package tachyon.runtime.functions.list

import tachyon.runtime.PageContext

class ListToArray : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 1) return call(pc, Caster.toString(args[0]))
        if (args.size == 2) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]))
        if (args.size == 3) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toBooleanValue(args[2]))
        if (args.size == 4) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toBooleanValue(args[2]), Caster.toBooleanValue(args[3]))
        throw FunctionException(pc, "ListToArray", 1, 4, args.size)
    }

    companion object {
        private const val serialVersionUID = 5883854318455975404L
        fun call(pc: PageContext?, list: String?): Array? {
            return if (list!!.length() === 0) ArrayImpl() else ListUtil.listToArrayRemoveEmpty(list, ',')
        }

        fun call(pc: PageContext?, list: String?, delimiter: String?): Array? {
            return call(pc, list, delimiter, false, false)
        }

        fun call(pc: PageContext?, list: String?, delimiter: String?, includeEmptyFields: Boolean): Array? {
            return call(pc, list, delimiter, includeEmptyFields, false)
        }

        fun call(pc: PageContext?, list: String?, delimiter: String?, includeEmptyFields: Boolean, multiCharacterDelimiter: Boolean): Array? {
            // empty
            if (list!!.length() === 0) {
                val a: Array = ArrayImpl()
                if (includeEmptyFields) a.appendEL("")
                return a
            }
            return ListUtil.listToArray(list, delimiter, includeEmptyFields, multiCharacterDelimiter)
        }
    }
}