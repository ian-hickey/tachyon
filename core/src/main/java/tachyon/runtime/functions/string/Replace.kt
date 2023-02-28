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
 * Implements the CFML Function replace
 */
package tachyon.runtime.functions.string

import tachyon.commons.lang.StringUtil

class Replace : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 2) return call(pc, Caster.toString(args[0]), args[1])
        if (args.size == 3) return call(pc, Caster.toString(args[0]), args[1], args[2])
        if (args.size == 4) return call(pc, Caster.toString(args[0]), args[1], args[2], Caster.toString(args[3]))
        throw FunctionException(pc, "Replace", 2, 4, args.size)
    }

    companion object {
        private const val serialVersionUID = -313884944032266348L
        @Throws(PageException::class)
        fun call(pc: PageContext?, str: String?, sub1: String?, sub2: String?): String? { // old
            return _call(pc, str, sub1, sub2, true)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, str: String?, sub1: String?, sub2: Object?): String? {
            return _call(pc, str, sub1, sub2, true)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, str: String?, sub1: String?, sub2: String?, scope: String?): String? { // old
            return _call(pc, str, sub1, sub2, !scope.equalsIgnoreCase("all"))
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, str: String?, sub1: String?, sub2: Object?, scope: String?): String? {
            return _call(pc, str, sub1, sub2, !scope.equalsIgnoreCase("all"))
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, input: String?, find: Object?, repl: String?, scope: String?): String? {
            return _call(pc, input, find, repl, !scope.equalsIgnoreCase("all"))
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, input: String?, find: Object?, repl: Object?, scope: String?): String? {
            return _call(pc, input, find, repl, !scope.equalsIgnoreCase("all"))
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, input: String?, find: Object?, repl: String?): String? {
            return _call(pc, input, find, repl, true)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, input: String?, find: Object?, repl: Object?): String? {
            return _call(pc, input, find, repl, true)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, input: String?, struct: Object?): String? {
            if (!Decision.isStruct(struct)) throw FunctionException(pc, "replace", 2, "sub1", "When passing only two parameters, the second parameter must be a Struct.")
            return StringUtil.replaceStruct(input!!, Caster.toStruct(struct), false)
        }

        @Throws(PageException::class)
        private fun _call(pc: PageContext?, str: String?, sub1: String?, sub2: Object?, firstOnly: Boolean): String? {
            if (StringUtil.isEmpty(sub1)) return str
            return if (Decision.isSimpleValue(sub2)) StringUtil.replace(str, sub1, Caster.toString(sub2), firstOnly, false) else StringUtil.replace(pc, str, sub1, Caster.toFunction(sub2), firstOnly)
        }

        @Throws(PageException::class)
        private fun _call(pc: PageContext?, input: String?, find: Object?, repl: Object?, onlyFirst: Boolean): String? {
            if (!Decision.isSimpleValue(find)) throw FunctionException(pc, "replace", 2, "sub1", "When passing three parameters or more, the second parameter must be a simple value.")
            return _call(pc, input, Caster.toString(find), repl, onlyFirst)
        }
    }
}