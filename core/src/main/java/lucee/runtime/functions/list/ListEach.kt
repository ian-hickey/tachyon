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

class ListEach : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 2) return _call(pc, Caster.toString(args[0]), Caster.toFunction(args[1]), ",", false, true, false, 20)
        if (args.size == 3) return _call(pc, Caster.toString(args[0]), Caster.toFunction(args[1]), Caster.toString(args[2]), false, true, false, 20)
        if (args.size == 4) return _call(pc, Caster.toString(args[0]), Caster.toFunction(args[1]), Caster.toString(args[2]), Caster.toBooleanValue(args[3]), true, false, 20)
        if (args.size == 5) return _call(pc, Caster.toString(args[0]), Caster.toFunction(args[1]), Caster.toString(args[2]), Caster.toBooleanValue(args[3]),
                Caster.toBooleanValue(args[4]), false, 20)
        if (args.size == 6) return _call(pc, Caster.toString(args[0]), Caster.toFunction(args[1]), Caster.toString(args[2]), Caster.toBooleanValue(args[3]),
                Caster.toBooleanValue(args[4]), Caster.toBooleanValue(args[5]), 20)
        if (args.size == 7) return call(pc, Caster.toString(args[0]), Caster.toFunction(args[1]), Caster.toString(args[2]), Caster.toBooleanValue(args[3]),
                Caster.toBooleanValue(args[4]), Caster.toBooleanValue(args[5]), Caster.toDoubleValue(args[6]))
        throw FunctionException(pc, "ListEach", 2, 7, args.size)
    }

    companion object {
        private const val serialVersionUID = -2271260656749514177L
        @Throws(PageException::class)
        fun call(pc: PageContext?, list: String?, udf: UDF?): String? {
            return _call(pc, list, udf, ",", false, true, false, 20)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, list: String?, udf: UDF?, delimiter: String?): String? {
            return _call(pc, list, udf, delimiter, false, true, false, 20)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, list: String?, udf: UDF?, delimiter: String?, includeEmptyFields: Boolean): String? {
            return _call(pc, list, udf, delimiter, includeEmptyFields, true, false, 20)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, list: String?, udf: UDF?, delimiter: String?, includeEmptyFields: Boolean, multiCharacterDelimiter: Boolean): String? {
            return _call(pc, list, udf, delimiter, includeEmptyFields, multiCharacterDelimiter, false, 20)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, list: String?, udf: UDF?, delimiter: String?, includeEmptyFields: Boolean, multiCharacterDelimiter: Boolean, parallel: Boolean): String? {
            return _call(pc, list, udf, delimiter, includeEmptyFields, multiCharacterDelimiter, parallel, 20)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, list: String?, udf: UDF?, delimiter: String?, includeEmptyFields: Boolean, multiCharacterDelimiter: Boolean, parallel: Boolean,
                 maxThreads: Double): String? {
            return _call(pc, list, udf, delimiter, includeEmptyFields, multiCharacterDelimiter, parallel, maxThreads.toInt())
        }

        @Throws(PageException::class)
        private fun _call(pc: PageContext?, list: String?, udf: UDF?, delimiter: String?, includeEmptyFields: Boolean, multiCharacterDelimiter: Boolean, parallel: Boolean,
                          maxThreads: Int): String? {
            val data = StringListData(list, delimiter, includeEmptyFields, multiCharacterDelimiter)
            return Each.call(pc, data, udf, parallel, maxThreads)
        }
    }
}