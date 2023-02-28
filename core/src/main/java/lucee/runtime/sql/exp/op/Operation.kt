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
package lucee.runtime.sql.exp.op

import lucee.runtime.sql.exp.Expression

interface Operation : Expression {
    fun hasAggregate(): Boolean

    companion object {
        const val OPERATION2_PLUS = 0
        const val OPERATION2_MINUS = 1
        const val OPERATION2_MULTIPLY = 2
        const val OPERATION2_DIVIDE = 3
        const val OPERATION2_BITWISE = 4
        const val OPERATION2_MOD = 5
        const val OPERATION2_XOR = 10
        const val OPERATION2_OR = 11
        const val OPERATION2_AND = 12
        const val OPERATION2_EQ = 13
        const val OPERATION2_NEQ = 14
        const val OPERATION2_LT = 15
        const val OPERATION2_LTE = 16
        const val OPERATION2_GT = 17
        const val OPERATION2_GTE = 18
        const val OPERATION2_LTGT = 19
        const val OPERATION2_NOT_LIKE = 20
        const val OPERATION2_LIKE = 21
        const val OPERATION1_PLUS = 30
        const val OPERATION1_MINUS = 31
        const val OPERATION1_NOT = 32
        const val OPERATION1_IS_NULL = 33
        const val OPERATION1_IS_NOT_NULL = 34
        const val OPERATION3_BETWEEN = 50
        const val OPERATION3_LIKE = 51
    }
}