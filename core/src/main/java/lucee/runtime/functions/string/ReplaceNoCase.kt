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
 * Implements the CFML Function replacenocase
 */
package lucee.runtime.functions.string

import lucee.commons.lang.StringUtil

class ReplaceNoCase : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 2) return call(pc, Caster.toString(args[0]), args[1])
        if (args.size == 3) return call(pc, Caster.toString(args[0]), args[1], Caster.toString(args[2]))
        if (args.size == 4) return call(pc, Caster.toString(args[0]), args[1], Caster.toString(args[2]), Caster.toString(args[3]))
        throw FunctionException(pc, "Replace", 2, 4, args.size)
    }

    companion object {
        private const val serialVersionUID = 4991516019845001690L
        @Throws(FunctionException::class)
        fun call(pc: PageContext?, str: String?, sub1: String?, sub2: String?): String? {
            return _call(pc, str, sub1, sub2, true)
        }

        @Throws(FunctionException::class)
        fun call(pc: PageContext?, str: String?, sub1: String?, sub2: String?, scope: String?): String? {
            return _call(pc, str, sub1, sub2, !scope.equalsIgnoreCase("all"))
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, input: String?, find: Object?, repl: String?, scope: String?): String? {
            return _call(pc, input, find, repl, !scope.equalsIgnoreCase("all"))
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, input: String?, find: Object?, repl: String?): String? {
            return _call(pc, input, find, repl, true)
        }

        @Throws(FunctionException::class)
        private fun _call(pc: PageContext?, str: String?, sub1: String?, sub2: String?, onlyFirst: Boolean): String? {
            if (StringUtil.isEmpty(sub1)) throw FunctionException(pc, "ReplaceNoCase", 2, "sub1", "The string length must be greater than 0")
            return StringUtil.replace(str, sub1, sub2, onlyFirst, true)
        }

        @Throws(PageException::class)
        private fun _call(pc: PageContext?, input: String?, find: Object?, repl: String?, onlyFirst: Boolean): String? {
            if (!Decision.isSimpleValue(find)) throw FunctionException(pc, "ReplaceNoCase", 2, "sub1", "When passing three parameters or more, the second parameter must be a String.")
            return _call(pc, input, Caster.toString(find), repl, onlyFirst)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, input: String?, struct: Object?): String? {
            if (!Decision.isStruct(struct)) throw FunctionException(pc, "ReplaceNoCase", 2, "sub1", "When passing only two parameters, the second parameter must be a Struct.")
            return StringUtil.replaceStruct(input!!, Caster.toStruct(struct), true)
        }
    }
}