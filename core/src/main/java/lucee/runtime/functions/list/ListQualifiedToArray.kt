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

import java.io.IOException

/**
 * Implements the CFML Function listqualify
 */
class ListQualifiedToArray : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 5) return _call(pc, Caster.toString(args[0]), toDelimeter(pc, Caster.toString(args[1])), toQualifier(pc, Caster.toString(args[2])),
                Caster.toBooleanValue(args[3]), Caster.toBooleanValue(args[4]))
        if (args.size == 4) return _call(pc, Caster.toString(args[0]), toDelimeter(pc, Caster.toString(args[1])), toQualifier(pc, Caster.toString(args[2])), Caster.toBooleanValue(args[3]), false)
        if (args.size == 3) return _call(pc, Caster.toString(args[0]), toDelimeter(pc, Caster.toString(args[1])), toQualifier(pc, Caster.toString(args[2])), false, false)
        if (args.size == 2) return _call(pc, Caster.toString(args[0]), toDelimeter(pc, Caster.toString(args[1])), '"', false, false)
        if (args.size == 1) return _call(pc, Caster.toString(args[0]), ',', '"', false, false)
        throw FunctionException(pc, "ListQualifiedToArray", 1, 5, args.size)
    }

    private class ArrayConsumer : ListParserConsumer {
        private val array: Array? = ArrayImpl()
        @Override
        fun entry(str: String?) {
            array.appendEL(str)
        }

        fun getArray(): Array? {
            return array
        }
    }

    companion object {
        private const val serialVersionUID = 8140873337224497863L
        @Throws(PageException::class)
        fun call(pc: PageContext?, list: String?): Array? {
            return _call(pc, list, ',', '"', false, false)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, list: String?, delimiter: String?): Array? {
            return _call(pc, list, toDelimeter(pc, delimiter), '"', false, false)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, list: String?, delimiter: String?, qualifier: String?): Array? {
            return _call(pc, list, toDelimeter(pc, delimiter), toQualifier(pc, qualifier), false, false)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, list: String?, delimiter: String?, qualifier: String?, qualifierRequired: Boolean): Array? {
            return _call(pc, list, toDelimeter(pc, delimiter), toQualifier(pc, qualifier), qualifierRequired, false)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, list: String?, delimiter: String?, qualifier: String?, qualifierRequired: Boolean, includeEmptyFields: Boolean): Array? {
            return _call(pc, list, toDelimeter(pc, delimiter), toQualifier(pc, qualifier), qualifierRequired, includeEmptyFields)
        }

        @Throws(PageException::class)
        private fun _call(pc: PageContext?, list: String?, del: Char, qual: Char, qualifierRequired: Boolean, includeEmptyFields: Boolean): Array? {
            return try {
                val consumer = ArrayConsumer()
                ListParser(list, consumer, del, qual, !includeEmptyFields, qualifierRequired).parse()
                consumer.getArray()
            } catch (e: IOException) {
                throw Caster.toPageException(e)
            }
        }

        @Throws(FunctionException::class)
        private fun toDelimeter(pc: PageContext?, delimeter: String?): Char {
            if (delimeter == null || StringUtil.isEmpty(delimeter)) return ','
            if (delimeter.length() !== 1) throw FunctionException(pc, "ListQualifiedToArray", 2, "delimeter", "qualifier can only be a single character, now is [$delimeter]")
            return delimeter.charAt(0)
        }

        @Throws(FunctionException::class)
        private fun toQualifier(pc: PageContext?, qualifier: String?): Char {
            if (qualifier == null || StringUtil.isEmpty(qualifier)) return '"'
            if (qualifier.length() !== 1) throw FunctionException(pc, "ListQualifiedToArray", 3, "qualifier", "qualifier can only be a single character, now is [$qualifier]")
            return qualifier.charAt(0)
        }
    }
}