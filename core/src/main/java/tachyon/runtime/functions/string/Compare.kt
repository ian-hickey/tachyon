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
 * Implements the CFML Function compare
 */
package tachyon.runtime.functions.string

import tachyon.runtime.PageContext

class Compare : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 2) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]))
        throw FunctionException(pc, "Compare", 2, 2, args.size)
    }

    companion object {
        private const val serialVersionUID = -6982310146145687711L
        fun call(pc: PageContext?, str1: String?, str2: String?): Double {
            val compare = str1!!.compareTo(str2!!)
            if (compare == 0) return 0
            return if (compare > 0) 1 else -1
        }
    }
}