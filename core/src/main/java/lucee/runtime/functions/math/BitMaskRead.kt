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
 * Implements the CFML Function bitmaskread
 */
package lucee.runtime.functions.math

import lucee.runtime.PageContext

object BitMaskRead : Function {
    @Throws(FunctionException::class)
    fun call(pc: PageContext?, dnumber: Double, dstart: Double, dlength: Double): Double {
        val number = dnumber.toInt()
        val start = dstart.toInt()
        val length = dlength.toInt()
        if (start > 31 || start < 0) throw FunctionException(pc, "bitMaskRead", 2, "start", "must be beetween 0 and 31 now $start")
        if (length > 31 || length < 0) throw FunctionException(pc, "bitMaskRead", 3, "length", "must be beetween 0 and 31 now $length")
        return (number shr start and (1 shl length) - 1).toDouble()
    }
}