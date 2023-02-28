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
 * Implements the CFML Function bitmaskset
 */
package tachyon.runtime.functions.math

import tachyon.runtime.PageContext

object BitMaskSet : Function {
    @Throws(FunctionException::class)
    fun call(pc: PageContext?, dnumber: Double, dmask: Double, dstart: Double, dlength: Double): Double {
        val number = dnumber.toInt()
        var mask = dmask.toInt()
        val start = dstart.toInt()
        val length = dlength.toInt()
        if (start > 31 || start < 0) throw FunctionException(pc, "bitMaskSet", 2, "start", "must be beetween 0 and 31 now $start")
        if (length > 31 || length < 0) throw FunctionException(pc, "bitMaskSet", 3, "length", "must be beetween 0 and 31 now $length")
        val tmp = (1 shl length) - 1 shl start
        mask = mask and (1 shl length) - 1
        return (number and tmp.inv() or mask shl start).toDouble()
    }
}