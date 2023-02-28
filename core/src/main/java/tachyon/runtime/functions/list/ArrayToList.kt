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
 * Implements the CFML Function arraytolist
 */
package tachyon.runtime.functions.list

import tachyon.runtime.PageContext

class ArrayToList : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        return if (args!!.size == 1) call(pc, Caster.toArray(args[0])) else call(pc, Caster.toArray(args[0]), Caster.toString(args[1]))
    }

    companion object {
        private const val serialVersionUID = -4909685848106371747L
        @Throws(PageException::class)
        fun call(pc: PageContext?, array: Array?): String? {
            return call(pc, array, ',')
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, array: Array?, delimiter: String?): String? {
            var array: Array? = array
            if (delimiter!!.length() === 1) return call(pc, array, delimiter.charAt(0))
            if (array is QueryColumn) array = unwrap(pc, array as QueryColumn?)
            val len: Int = array.size()
            if (len == 0) return ""
            if (len == 1) return Caster.toString(array.get(1, ""))
            var o: Object = array.get(1, null)
            val sb = StringBuilder(if (o == null) "" else Caster.toString(o))
            for (i in 2..len) {
                sb.append(delimiter)
                o = array.get(i, null)
                sb.append(if (o == null) "" else Caster.toString(o))
            }
            return sb.toString()
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, array: Array?, delimiter: Char): String? {
            var array: Array? = array
            if (array is QueryColumn) array = unwrap(pc, array as QueryColumn?)
            val len: Int = array.size()
            if (len == 0) return ""
            if (len == 1) return Caster.toString(array.get(1, ""))
            var o: Object = array.get(1, null)
            val sb = StringBuilder(if (o == null) "" else Caster.toString(o))
            for (i in 2..len) {
                sb.append(delimiter)
                o = array.get(i, null)
                sb.append(if (o == null) "" else Caster.toString(o))
            }
            return sb.toString()
        }

        private fun unwrap(pc: PageContext?, col: QueryColumn?): Array? {
            val arr: Array = Caster.toArray(col.get(pc, null as Object?), null)
            return if (arr != null) arr else col
        }
    }
}