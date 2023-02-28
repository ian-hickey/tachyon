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

import lucee.commons.lang.StringUtil

class UCFirst : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 1) return call(pc, Caster.toString(args[0]))
        if (args.size == 2) return call(pc, Caster.toString(args[0]), Caster.toBooleanValue(args[1]))
        if (args.size == 3) return call(pc, Caster.toString(args[0]), Caster.toBooleanValue(args[1]), Caster.toBooleanValue(args[2]))
        throw FunctionException(pc, "UCFirst", 1, 3, args.size)
    }

    companion object {
        private const val serialVersionUID = 6476775359884698477L
        fun call(pc: PageContext?, string: String?): String? {
            return call(pc, string, false)
        }

        fun call(pc: PageContext?, string: String?, doAll: Boolean): String? {
            return if (!doAll) StringUtil.ucFirst(string) else StringUtil.capitalize(string!!, null)
        }

        fun call(pc: PageContext?, string: String?, doAll: Boolean, doLowerIfAllUppercase: Boolean): String? {
            var string = string
            if (doLowerIfAllUppercase && StringUtil.isAllUpperCase(string)) string = string.toLowerCase()
            return if (!doAll) StringUtil.ucFirst(string) else StringUtil.capitalize(string!!, null)
        }
    }
}