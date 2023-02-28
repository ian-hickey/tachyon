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
 * Implements the CFML Function listrest
 */
package tachyon.runtime.functions.list

import java.util.HashSet

class ListRemoveDuplicates : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 1) return call(pc, Caster.toString(args[0]))
        if (args.size == 2) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]))
        if (args.size == 3) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toBooleanValue(args[2]))
        throw FunctionException(pc, "ListRemoveDuplicates", 1, 3, args.size)
    }

    companion object {
        private const val serialVersionUID = -6596215135126751629L
        @Throws(PageException::class)
        fun call(pc: PageContext?, list: String?): String? {
            return call(pc, list, ",", false)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, list: String?, delimiter: String?): String? {
            return call(pc, list, delimiter, false)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, list: String?, delimiter: String?, ignoreCase: Boolean): String? {
            var delimiter = delimiter
            if (list == null) return ""
            if (delimiter == null) delimiter = ","
            val array: Array = ListUtil.listToArrayRemoveEmpty(list, delimiter)
            val existing: Set<String?>?
            if (ignoreCase) existing = TreeSet<String?>(String.CASE_INSENSITIVE_ORDER) else existing = HashSet<String?>()
            val sb = StringBuilder()
            // Key[] keys = array.keys();
            val it: Iterator<Object?> = array.valueIterator()
            var value: String
            while (it.hasNext()) {
                value = Caster.toString(it.next())
                if (!existing!!.contains(value)) {
                    if (sb.length() > 0) sb.append(delimiter)
                    sb.append(value)
                    existing.add(value)
                }
            }
            return sb.toString()
        }
    }
}