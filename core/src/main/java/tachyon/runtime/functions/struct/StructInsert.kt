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
 * Implements the CFML Function structinsert
 */
package tachyon.runtime.functions.struct

import tachyon.runtime.PageContext

class StructInsert : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 4) return call(pc, Caster.toStruct(args[0]), Caster.toString(args[1]), args[2], Caster.toBooleanValue(args[3]))
        if (args.size == 3) return call(pc, Caster.toStruct(args[0]), Caster.toString(args[1]), args[2])
        throw FunctionException(pc, "StructInsert", 3, 4, args.size)
    }

    companion object {
        private const val serialVersionUID = 4244527243856690926L
        @Throws(PageException::class)
        fun call(pc: PageContext?, struct: Struct?, key: String?, value: Object?): Boolean {
            return call(pc, struct, key, value, false)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, struct: Struct?, strKey: String?, value: Object?, allowoverwrite: Boolean): Boolean {
            val key: Key = KeyImpl.init(strKey)
            if (allowoverwrite) {
                struct.set(key, value)
            } else {
                if (struct.get(key, null) != null) throw ExpressionException("key [$key] already exist in struct")
                struct.set(key, value)
            }
            return true
        }
    }
}