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
package tachyon.commons.math

import java.math.BigDecimal

/**
 * Math Util
 */
object MathUtil {
    /**
     * abs
     *
     * @param number
     * @return abs value
     */
    fun abs(number: Double): Double {
        return if (number <= 0.0) 0.0 - number else number
    }

    fun sgn(number: Double): Double {
        return if (number != 0.0) if (number >= 0.0) 1 else -1 else 0
    }

    fun nextPowerOf2(value: Int): Int {
        var result = 1
        while (result < value) result = result shl 1
        return result
    }

    fun divide(left: BigDecimal, right: BigDecimal?): BigDecimal {
        return try {
            left.divide(right, BigDecimal.ROUND_UNNECESSARY)
        } catch (ex: ArithmeticException) {
            left.divide(right, MathContext.DECIMAL128)
        }
    }

    fun add(left: BigDecimal, right: BigDecimal?): BigDecimal {
        return try {
            left.add(right, MathContext.UNLIMITED)
        } catch (ex: ArithmeticException) {
            left.add(right, MathContext.DECIMAL128)
        }
    }

    fun subtract(left: BigDecimal, right: BigDecimal?): BigDecimal {
        return try {
            left.subtract(right, MathContext.UNLIMITED)
        } catch (ex: ArithmeticException) {
            left.subtract(right, MathContext.DECIMAL128)
        }
    }

    fun multiply(left: BigDecimal, right: BigDecimal?): BigDecimal {
        return try {
            left.multiply(right, MathContext.UNLIMITED)
        } catch (ex: ArithmeticException) {
            left.multiply(right, MathContext.DECIMAL128)
        }
    }
}