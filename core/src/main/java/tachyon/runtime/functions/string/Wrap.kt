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
 * Implements the CFML Function refind
 */
package tachyon.runtime.functions.string

import tachyon.commons.io.SystemUtil

class Wrap : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 2) return call(pc, Caster.toString(args[0]), Caster.toDoubleValue(args[1]))
        if (args.size == 3) return call(pc, Caster.toString(args[0]), Caster.toDoubleValue(args[1]), Caster.toBooleanValue(args[2]))
        throw FunctionException(pc, "Wrap", 2, 3, args.size)
    }

    companion object {
        @Throws(PageException::class)
        fun call(pc: PageContext?, string: String?, limit: Double): String? {
            return call(pc, string, limit, false)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, string: String?, limit: Double, strip: Boolean): String? {
            var string = string
            if (strip) {
                string = REReplace.call(pc, string, "[[:space:]]", " ", "all")
            }
            val _limit = limit.toInt()
            if (limit < 1) throw FunctionException(pc, "Wrap", 2, "limit", "value mus be a positive number")
            return wrap(string, _limit)
        }

        /**
         * wraps a String to specified length
         *
         * @param str string to erap
         * @param wrapTextLength
         * @return wraped String
         */
        fun wrap(str: String?, wrapTextLength: Int): String? {
            if (wrapTextLength <= 0) return str
            val rtn = StringBuilder()
            val ls: String = SystemUtil.getOSSpecificLineSeparator()
            val arr: Array = ListUtil.listToArray(str, ls)
            val len: Int = arr.size()
            for (i in 1..len) {
                rtn.append(wrapLine(Caster.toString(arr.get(i, ""), ""), wrapTextLength))
                if (i + 1 < len) rtn.append(ls)
            }
            return rtn.toString()
        }

        /**
         * wrap a single line
         *
         * @param str
         * @param wrapTextLength
         * @return
         */
        private fun wrapLine(str: String?, wrapTextLength: Int): String? {
            if (str!!.length() <= wrapTextLength) return str
            val sub: String = str.substring(0, wrapTextLength)
            val rest: String = str.substring(wrapTextLength)
            val firstR: Char = rest.charAt(0)
            val ls: String = SystemUtil.getOSSpecificLineSeparator()
            if (firstR == ' ' || firstR == '\t') return sub + ls + wrapLine(if (rest.length() > 1) rest.substring(1) else "", wrapTextLength)
            val indexSpace: Int = sub.lastIndexOf(' ')
            val indexTab: Int = sub.lastIndexOf('\t')
            val index = if (indexSpace <= indexTab) indexTab else indexSpace
            return if (index == -1) sub + ls + wrapLine(rest, wrapTextLength) else sub.substring(0, index) + ls + wrapLine(sub.substring(index + 1) + rest, wrapTextLength)
        }
    }
}