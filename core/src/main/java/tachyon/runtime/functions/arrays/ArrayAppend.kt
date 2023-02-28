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
 * Implements the CFML Function arrayappend
 */
package tachyon.runtime.functions.arrays

import tachyon.runtime.PageContext

/**
 * implementation of the Function arrayAppend
 */
class ArrayAppend : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        return if (args!!.size == 2) call(pc, Caster.toArray(args[0]), args[1]) else if (args.size == 3) call(pc, Caster.toArray(args[0]), args[1], Caster.toBooleanValue(args[2])) else throw FunctionException(pc, "ArrayAppend", 2, 3, args.size)
    }

    companion object {
        private const val serialVersionUID = 5989673419120862625L
        @Throws(PageException::class)
        fun call(pc: PageContext?, array: Array?, `object`: Object?): Boolean {
            return call(pc, array, `object`, false)
        }

        /**
         * @param pc
         * @param array
         * @param object
         * @return has appended
         * @throws PageException
         */
        @Throws(PageException::class)
        fun call(pc: PageContext?, array: Array?, `object`: Object?, merge: Boolean): Boolean {
            if (merge && Decision.isArray(`object`)) {
                val appends: Array<Object?> = Caster.toNativeArray(`object`)
                for (i in appends.indices) {
                    array.append(appends[i])
                }
            } else array.append(`object`)
            return true
        }
    }
}