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
package tachyon.runtime.functions.string

import tachyon.runtime.PageContext

/**
 * implements the member method String.startWith(prefix, ignoreCase)
 */
class StringStartsWith : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size < 2 || args.size > 3) throw FunctionException(pc, "startsWith", 2, 3, args.size)
        return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), if (args.size == 3) Caster.toBoolean(args[2]) else false)
    }

    companion object {
        fun call(pc: PageContext?, input: String?, subs: String?, ignoreCase: Boolean): Boolean {
            return if (ignoreCase) input.regionMatches(true, 0, subs, 0, subs!!.length()) else input.startsWith(subs)
        }

        fun call(pc: PageContext?, input: String?, subs: String?): Boolean {
            return call(pc, input, subs, false)
        }
    }
}