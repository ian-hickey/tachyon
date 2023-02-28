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
package lucee.runtime.functions.string

import lucee.runtime.PageContext

class REMatchNoCase : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 2) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]))
        if (args.size == 3) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toBooleanValue(args[2]))
        throw FunctionException(pc, "REMatchNoCase", 2, 3, args.size)
    }

    companion object {
        private const val serialVersionUID = 7300917722574558505L
        @Throws(PageException::class)
        fun call(pc: PageContext?, regExpr: String?, str: String?): Array? {
            val regex: Regex = (pc as PageContextImpl?).getRegex()
            return regex.matchAll(regExpr, str, 1, false, false)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, regExpr: String?, str: String?, multiline: Boolean): Array? {
            val regex: Regex = (pc as PageContextImpl?).getRegex()
            return regex.matchAll(regExpr, str, 1, false, multiline)
        }
    }
}