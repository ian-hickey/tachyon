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
 * Implements the CFML Function randrange
 */
package tachyon.runtime.functions.math

import tachyon.runtime.PageContext

object RandRange : Function {
    @Throws(ExpressionException::class)
    fun call(pc: PageContext?, number1: Double, number2: Double): Double {
        return call(pc, number1, number2, "cfmx_compat")
    }

    @Throws(ExpressionException::class)
    fun call(pc: PageContext?, number1: Double, number2: Double, algo: String?): Double {
        var min = number1.toInt()
        var max = number2.toInt()
        if (number1 > number2) {
            val tmp = min
            min = max
            max = tmp
        }
        val diff = max - min
        return ((Rand.call(pc, algo) * (diff + 1)) as Int + min).toDouble()
    }

    @Throws(ExpressionException::class)
    operator fun invoke(min: Int, max: Int): Int {
        var min = min
        var max = max
        if (min > max) {
            val tmp = min
            min = max
            max = tmp
        }
        val diff = max - min
        return (Rand.call(null, "cfmx_compat") * (diff + 1)) as Int + min
    }
}