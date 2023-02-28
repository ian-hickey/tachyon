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
 * Implements the CFML Function rereplacenocase
 */
package lucee.runtime.functions.string

import lucee.runtime.PageContext

class REReplaceNoCase : BIF(), Function {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 3) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toString(args[2]))
        if (args.size == 4) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toString(args[2]), Caster.toString(args[3]))
        throw FunctionException(pc, "REReplaceNoCase", 3, 4, args.size)
    }

    companion object {
        private const val serialVersionUID = 3261493342788819694L
        @Throws(PageException::class)
        fun call(pc: PageContext?, string: String?, regExp: String?, replace: String?): String? {
            val regex: Regex = (ThreadLocalPageContext.get() as PageContextImpl).getRegex()
            return regex.replace(string, regExp, replace, false, false)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, string: String?, regExp: String?, replace: String?, scope: String?): String? {
            val regex: Regex = (ThreadLocalPageContext.get() as PageContextImpl).getRegex()
            return if (scope.equalsIgnoreCase("all")) regex.replaceAll(string, regExp, replace, false, false) else regex.replace(string, regExp, replace, false, false)
        }
    }
}