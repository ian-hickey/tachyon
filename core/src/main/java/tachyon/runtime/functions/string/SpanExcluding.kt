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
 * Implements the CFML Function spanexcluding
 */
package tachyon.runtime.functions.string

import java.util.StringTokenizer

class SpanExcluding : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 2) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]))
        throw FunctionException(pc, "SpanExcluding", 2, 2, args.size)
    }

    companion object {
        private const val serialVersionUID = -2597389089930732011L
        fun call(pc: PageContext?, str: String?, set: String?): String? {
            val stringtokenizer = StringTokenizer(str, set)
            if (stringtokenizer.hasMoreTokens()) {
                val rtn: String = stringtokenizer.nextToken()
                val i: Int = str.indexOf(rtn)
                return if (i != 0) "" else rtn
            }
            return ""
        }
    }
}