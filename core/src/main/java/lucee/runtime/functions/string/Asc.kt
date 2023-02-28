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
 * Implements the CFML Function asc
 */
package lucee.runtime.functions.string

import lucee.runtime.PageContext

class Asc : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 1) return call(pc, Caster.toString(args[0]))
        if (args.size == 2) return call(pc, Caster.toString(args[0]), Caster.toDoubleValue(args[1]))
        throw FunctionException(pc, "asc", 1, 2, args.size)
    }

    companion object {
        private const val serialVersionUID = 8147532406904456091L
        fun call(pc: PageContext?, string: String?): Double {
            return if (string!!.length() === 0) 0 else string.charAt(0)
        }

        fun call(pc: PageContext?, string: String?, position: Double): Double {
            val pos = position.toInt()
            return if (pos < 1 || pos > string!!.length()) 0 else string.charAt(pos - 1)
        }
    }
}