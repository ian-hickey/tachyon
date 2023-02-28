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
 * Implements the CFML Function bitshln
 */
package tachyon.runtime.functions.math

import tachyon.runtime.PageContext

object BitSHLN : Function {
    @Throws(FunctionException::class)
    fun call(pc: PageContext?, dnumber: Double, dcount: Double): Double {
        val number = dnumber.toInt()
        val count = dcount.toInt()
        if (count > 31 || count < 0) throw FunctionException(pc, "bitSHLN", 2, "count", "must be beetween 0 and 31 now $count")
        // TODO use bigInteger to shift further
        return (number shl count).toDouble()
    }
}