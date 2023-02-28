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
 * Implements the CFML Function structdelete
 */
package tachyon.runtime.functions.struct

import tachyon.runtime.PageContext

class StructDelete : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 3) return call(pc, Caster.toStruct(args[0]), Caster.toString(args[1]), Caster.toBooleanValue(args[2]))
        if (args.size == 2) return call(pc, Caster.toStruct(args[0]), Caster.toString(args[1]))
        throw FunctionException(pc, "StructDelete", 2, 3, args.size)
    }

    companion object {
        private const val serialVersionUID = 6670961245029356618L
        @Throws(TemplateException::class)
        fun call(pc: PageContext?, struct: Struct?, key: String?): Boolean {
            return call(pc, struct, key, false)
        }

        @Throws(TemplateException::class)
        fun call(pc: PageContext?, struct: Struct?, key: String?, indicatenotexisting: Boolean): Boolean {
            if (indicatenotexisting && !struct.containsKey(key)) throw TemplateException("Cannot delete item with key $key", "The key doesn't exist.")
            return struct.removeEL(KeyImpl.init(key)) != null || !indicatenotexisting
        }
    }
}