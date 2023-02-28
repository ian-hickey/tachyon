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
 * Implements the CFML Function arrayMerge
 * Merge 2 arrays
 */
package tachyon.runtime.functions.arrays

import tachyon.runtime.PageContext

class ArrayMerge : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        return if (args!!.size == 2) call(pc, Caster.toArray(args[0]), Caster.toArray(args[1])) else if (args.size == 3) call(pc, Caster.toArray(args[0]), Caster.toArray(args[1]), Caster.toBooleanValue(args[2])) else throw FunctionException(pc, "ArrayMerge", 2, 3, args.size)
    }

    companion object {
        private const val serialVersionUID = -391473381762154998L
        @Throws(PageException::class)
        fun call(pc: PageContext?, arr1: Array?, arr2: Array?): Array? {
            return call(pc, arr1, arr2, false)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, arr1: Array?, arr2: Array?, leaveIndex: Boolean): Array? {
            val arr = ArrayImpl(arr1.size() + arr2.size())
            // arr.ensureCapacity(arr1.size() + arr2.size());
            if (leaveIndex) {
                Companion[arr] = arr2
                Companion[arr] = arr1
                return arr
            }
            append(arr, arr1)
            append(arr, arr2)
            return arr
        }

        @Throws(PageException::class)
        operator fun set(target: Array?, source: Array?) {
            val srcKeys: IntArray = source.intKeys()
            for (i in srcKeys.indices) {
                target.setE(srcKeys[i], source.getE(srcKeys[i]))
            }
        }

        @Throws(PageException::class)
        fun append(target: Array?, source: Array?) {
            val srcKeys: IntArray = source.intKeys()
            for (i in srcKeys.indices) {
                target.append(source.getE(srcKeys[i]))
            }
        }
    }
}