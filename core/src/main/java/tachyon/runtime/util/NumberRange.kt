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
package tachyon.runtime.util

import tachyon.runtime.exp.ExpressionException

/**
 * checks for a Number range
 */
object NumberRange {
    /**
     * checks if number between from and to (inlude from and to)
     *
     * @param number
     * @param from
     * @param to
     * @return given number when range ok
     * @throws ExpressionException
     */
    @Throws(ExpressionException::class)
    fun range(number: Double, from: Double, to: Double): Double {
        if (number >= from && number <= to) return number
        throw ExpressionException("number must between [$from - $to] now $number")
    }

    /**
     * checks if number is greater than from (inlude from)
     *
     * @param number
     * @param from
     * @return given number when range ok
     * @throws ExpressionException
     */
    @Throws(ExpressionException::class)
    fun range(number: Double, from: Double): Double {
        if (number >= from) return number
        throw ExpressionException("number must be greater than [$from] now $number")
    }

    @Throws(ExpressionException::class)
    fun range(number: Int, from: Int): Int {
        if (number >= from) return number
        throw ExpressionException("number must be greater than [$from] now $number")
    }
}