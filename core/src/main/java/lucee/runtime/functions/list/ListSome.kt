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

class ListSome : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 2) return call(pc, Caster.toString(args[0]), Caster.toFunction(args[1]))
        if (args.size == 3) return call(pc, Caster.toString(args[0]), Caster.toFunction(args[1]), Caster.toString(args[2]))
        if (args.size == 4) return call(pc, Caster.toString(args[0]), Caster.toFunction(args[1]), Caster.toString(args[2]), Caster.toBooleanValue(args[3]))
        if (args.size == 5) return call(pc, Caster.toString(args[0]), Caster.toFunction(args[1]), Caster.toString(args[2]), Caster.toBooleanValue(args[3]), Caster.toBooleanValue(args[4]))
        if (args.size == 6) return call(pc, Caster.toString(args[0]), Caster.toFunction(args[1]), Caster.toString(args[2]), Caster.toBooleanValue(args[3]),
                Caster.toBooleanValue(args[4]), Caster.toBooleanValue(args[5]))
        if (args.size == 7) return call(pc, Caster.toString(args[0]), Caster.toFunction(args[1]), Caster.toString(args[2]), Caster.toBooleanValue(args[3]),
                Caster.toBooleanValue(args[4]), Caster.toBooleanValue(args[5]), Caster.toDoubleValue(args[6]))
        throw FunctionException(pc, "ListSome", 2, 7, args.size)
    }

    companion object {
        private const val serialVersionUID = -9092877950301316754L
        @Throws(PageException::class)
        fun call(pc: PageContext?, list: String?, udf: UDF?): Boolean {
            return call(pc, list, udf, ",", false, true, false, 20.0)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, list: String?, udf: UDF?, delimiter: String?): Boolean {
            return call(pc, list, udf, delimiter, false, true, false, 20.0)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, list: String?, udf: UDF?, delimiter: String?, includeEmptyFields: Boolean): Boolean {
            return call(pc, list, udf, delimiter, includeEmptyFields, true, false, 20.0)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, list: String?, udf: UDF?, delimiter: String?, includeEmptyFields: Boolean, multiCharacterDelimiter: Boolean): Boolean {
            return call(pc, list, udf, delimiter, includeEmptyFields, multiCharacterDelimiter, false, 20.0)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, list: String?, udf: UDF?, delimiter: String?, includeEmptyFields: Boolean, multiCharacterDelimiter: Boolean, parallel: Boolean): Boolean {
            return call(pc, list, udf, delimiter, includeEmptyFields, multiCharacterDelimiter, parallel, 20.0)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, list: String?, udf: UDF?, delimiter: String?, includeEmptyFields: Boolean, multiCharacterDelimiter: Boolean, parallel: Boolean,
                 maxThreads: Double): Boolean {
            val data = StringListData(list, delimiter, includeEmptyFields, multiCharacterDelimiter)
            return Some.call(pc, data, udf, parallel, maxThreads)
        }
    }
}