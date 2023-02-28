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
 * Implements the CFML Function listcontains
 */
package tachyon.runtime.functions.list

import tachyon.runtime.PageContext

class ListContains : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 2) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), ",", false, false)
        if (args.size == 3) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toString(args[2]), false, false)
        if (args.size == 4) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toString(args[2]), Caster.toBooleanValue(args[3]), false)
        if (args.size == 5) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toString(args[2]), Caster.toBooleanValue(args[3]), Caster.toBooleanValue(args[4]))
        throw FunctionException(pc, "ListContains", 2, 5, args.size)
    }

    companion object {
        private const val serialVersionUID = -7580788340022587225L
        fun call(pc: PageContext?, list: String?, value: String?): Double {
            return call(pc, list, value, ",", false, false)
        }

        fun call(pc: PageContext?, list: String?, value: String?, delimter: String?): Double {
            return call(pc, list, value, delimter, false, false)
        }

        fun call(pc: PageContext?, list: String?, value: String?, delimter: String?, includeEmptyFields: Boolean): Double {
            return call(pc, list, value, delimter, includeEmptyFields, false)
        }

        fun call(pc: PageContext?, list: String?, value: String?, delimter: String?, includeEmptyFields: Boolean, multiCharacterDelimiter: Boolean): Double {
            return ListUtil.listContains(list, value, delimter, includeEmptyFields, multiCharacterDelimiter) + 1
        }
    }
}