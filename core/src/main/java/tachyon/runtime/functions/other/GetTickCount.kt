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
 * Implements the CFML Function gettickcount
 */
package tachyon.runtime.functions.other

import tachyon.commons.lang.StringUtil

object GetTickCount : Function {
    private const val serialVersionUID = 678332662578928144L
    var UNIT_NANO = 1.0
    var UNIT_MILLI = 2.0
    var UNIT_MICRO = 4.0
    var UNIT_SECOND = 8.0
    fun call(pc: PageContext?): Double {
        return System.currentTimeMillis()
    }

    @Throws(FunctionException::class)
    fun call(pc: PageContext?, unit: String?): Double {
        var unit = unit
        if (!StringUtil.isEmpty(unit, true)) {
            unit = unit.trim()
            val c: Char = unit.charAt(0)
            if (c == 'n' || c == 'N') return System.nanoTime() else if (c == 'm' || c == 'M') {
                return if ("micro".equalsIgnoreCase(unit)) System.nanoTime() / 1000 else System.currentTimeMillis()
            } else if (c == 's' || c == 'S') return System.currentTimeMillis() / 1000
        }
        throw FunctionException(pc, "GetTickCount", 1, "unit", "invalid value [$unit], valid values are (nano, micro, milli, second)")
    }

    // this function is only called when the evaluator validates the unit definition on compilation time
    fun call(pc: PageContext?, unit: Double): Double {
        if (UNIT_NANO == unit) return System.nanoTime()
        if (UNIT_MICRO == unit) return System.nanoTime() / 1000
        return if (UNIT_MILLI == unit) System.currentTimeMillis() else System.currentTimeMillis() / 1000
    }
}