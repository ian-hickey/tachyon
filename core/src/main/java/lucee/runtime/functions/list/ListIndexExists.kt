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
 * Implements the CFML Function structkeyexists
 */
package lucee.runtime.functions.list

import lucee.runtime.PageContext

class ListIndexExists : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 2) return call(pc, Caster.toString(args[0]), Caster.toDoubleValue(args[1]))
        if (args.size == 3) return call(pc, Caster.toString(args[0]), Caster.toDoubleValue(args[1]), Caster.toString(args[2]))
        if (args.size == 4) return call(pc, Caster.toString(args[0]), Caster.toDoubleValue(args[1]), Caster.toString(args[2]), Caster.toBooleanValue(args[3]))
        throw FunctionException(pc, "ListIndexExists", 2, 4, args.size)
    }

    companion object {
        private const val serialVersionUID = 7642583305678735361L
        fun call(pc: PageContext?, list: String?, index: Double): Boolean {
            return call(pc, list, index, ",", false)
        }

        fun call(pc: PageContext?, list: String?, index: Double, delimiter: String?): Boolean {
            return call(pc, list, index, delimiter, false)
        }

        fun call(pc: PageContext?, list: String?, index: Double, delimiter: String?, includeEmptyFields: Boolean): Boolean {
            return if (includeEmptyFields) ListUtil.listToArray(list, delimiter).get(index.toInt(), null) != null else ListUtil.listToArrayRemoveEmpty(list, delimiter).get(index.toInt(), null) != null
        }
    }
}