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

class ListFilter : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 2) return call(pc, Caster.toString(args[0]), Caster.toFunction(args[1]), ",", false, true, false, 20.0)
        if (args.size == 3) return call(pc, Caster.toString(args[0]), Caster.toFunction(args[1]), Caster.toString(args[2]), false, true, false, 20.0)
        if (args.size == 4) return call(pc, Caster.toString(args[0]), Caster.toFunction(args[1]), Caster.toString(args[2]), Caster.toBooleanValue(args[3]), true, false, 20.0)
        if (args.size == 5) return call(pc, Caster.toString(args[0]), Caster.toFunction(args[1]), Caster.toString(args[2]), Caster.toBooleanValue(args[3]),
                Caster.toBooleanValue(args[4]), false, 20.0)
        if (args.size == 6) return call(pc, Caster.toString(args[0]), Caster.toFunction(args[1]), Caster.toString(args[2]), Caster.toBooleanValue(args[3]),
                Caster.toBooleanValue(args[4]), Caster.toBooleanValue(args[5]), 20.0)
        if (args.size == 7) return call(pc, Caster.toString(args[0]), Caster.toFunction(args[1]), Caster.toString(args[2]), Caster.toBooleanValue(args[3]),
                Caster.toBooleanValue(args[4]), Caster.toBooleanValue(args[5]), Caster.toDoubleValue(args[6]))
        throw FunctionException(pc, "ListFilter", 2, 7, args.size)
    }

    companion object {
        private const val serialVersionUID = 2182867537570796564L
        @Throws(PageException::class)
        fun call(pc: PageContext?, list: String?, filter: UDF?): String? {
            return call(pc, list, filter, ",", false, true, false, 20.0)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, list: String?, filter: UDF?, delimiter: String?): String? {
            return call(pc, list, filter, delimiter, false, true, false, 20.0)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, list: String?, filter: UDF?, delimiter: String?, includeEmptyFields: Boolean): String? {
            return call(pc, list, filter, delimiter, includeEmptyFields, true, false, 20.0)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, list: String?, filter: UDF?, delimiter: String?, includeEmptyFields: Boolean, multiCharacterDelimiter: Boolean): String? {
            return call(pc, list, filter, delimiter, includeEmptyFields, multiCharacterDelimiter, false, 20.0)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, list: String?, filter: UDF?, delimiter: String?, includeEmptyFields: Boolean, multiCharacterDelimiter: Boolean, parallel: Boolean): String? {
            return call(pc, list, filter, delimiter, includeEmptyFields, multiCharacterDelimiter, parallel, 20.0)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, list: String?, filter: UDF?, delimiter: String?, includeEmptyFields: Boolean, multiCharacterDelimiter: Boolean, parallel: Boolean,
                 maxThreads: Double): String? {
            return ListUtil.arrayToList(Filter.call(pc, StringListData(list, delimiter, includeEmptyFields, multiCharacterDelimiter), filter, parallel, maxThreads) as Array,
                    delimiter)
        }
    }
}