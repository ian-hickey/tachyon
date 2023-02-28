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
 * Implements the CFML Function refindnocase
 */
class REFindNoCase : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 2) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]))
        if (args.size == 3) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toDoubleValue(args[2]))
        if (args.size == 4) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toDoubleValue(args[2]), Caster.toBooleanValue(args[3]))
        if (args.size == 5) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toDoubleValue(args[2]), Caster.toBooleanValue(args[3]), Caster.toString(args[4]))
        if (args.size == 6) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toDoubleValue(args[2]), Caster.toBooleanValue(args[3]),
                Caster.toString(args[4]), Caster.toBooleanValue(args[5]))
        throw FunctionException(pc, "REFindNoCase", 2, 6, args.size)
    }

    companion object {
        private const val serialVersionUID = 1562665117076202965L
        @Throws(PageException::class)
        fun call(pc: PageContext?, regExpr: String?, str: String?): Object? {
            return call(pc, regExpr, str, 1.0, false, null, false)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, regExpr: String?, str: String?, start: Double): Object? {
            return call(pc, regExpr, str, start, false, null, false)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, regExpr: String?, str: String?, start: Double, returnsubexpressions: Boolean): Object? {
            return call(pc, regExpr, str, start, returnsubexpressions, null, false)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, regExpr: String?, str: String?, start: Double, returnsubexpressions: Boolean, scope: String?): Object? {
            return call(pc, regExpr, str, start, returnsubexpressions, scope, false)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, regExpr: String?, str: String?, start: Double, returnsubexpressions: Boolean, scope: String?, multiline: Boolean): Object? {
            val regex: Regex = (pc as PageContextImpl?).getRegex()
            val isMatchAll = if (scope == null) false else scope.equalsIgnoreCase("all")
            if (returnsubexpressions) {
                return if (isMatchAll) regex.findAll(regExpr, str, start.toInt(), false, multiline) else regex.find(regExpr, str, start.toInt(), false, multiline)
            }
            return if (isMatchAll) regex.indexOfAll(regExpr, str, start.toInt(), false, multiline) else regex.indexOf(regExpr, str, start.toInt(), false, multiline)
        }
    }
}