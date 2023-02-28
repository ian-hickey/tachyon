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

class Operation2(left: Expression?, right: Expression?, operator: Int) : ExpressionSupport(), Operation {
    private val left: Expression?
    private val right: Expression?

    /**
     * @return the operator
     */
    val operator: Int

    @Override
    override fun toString(noAlias: Boolean): String? {
        return if (noAlias || getIndex() === 0) left!!.toString(true).toString() + " " + toString(operator) + " " + right!!.toString(true) else toString(true).toString() + " as " + getAlias()
    }

    /**
     * @return the left
     */
    fun getLeft(): Expression? {
        return left
    }

    /**
     * @return the right
     */
    fun getRight(): Expression? {
        return right
    }

    @Override
    override fun reset() {
        if (left != null) {
            left.reset()
        }
        if (right != null) {
            right.reset()
        }
    }

    @Override
    override fun setCacheColumn(cacheColumn: Boolean) {
        if (left != null) {
            left.setCacheColumn(cacheColumn)
        }
        if (right != null) {
            right.setCacheColumn(cacheColumn)
        }
    }

    @Override
    override fun hasAggregate(): Boolean {
        if (left is OperationAggregate) {
            return true
        }
        if (left is Operation && (left as Operation?)!!.hasAggregate()) {
            return true
        }
        if (right is OperationAggregate) {
            return true
        }
        return if (right is Operation && (right as Operation?)!!.hasAggregate()) {
            true
        } else false
    }

    companion object {
        fun toString(operator: Int): String? {
            when (operator) {
                Operation.OPERATION2_DIVIDE -> return "/"
                Operation.OPERATION2_MINUS -> return "-"
                Operation.OPERATION2_MULTIPLY -> return "*"
                Operation.OPERATION2_PLUS -> return "+"
                Operation.OPERATION2_BITWISE -> return "^"
                Operation.OPERATION2_MOD -> return "%"
                Operation.OPERATION2_AND -> return "and"
                Operation.OPERATION2_OR -> return "or"
                Operation.OPERATION2_XOR -> return "xor"
                Operation.OPERATION2_EQ -> return "="
                Operation.OPERATION2_GT -> return ">"
                Operation.OPERATION2_GTE -> return "=>"
                Operation.OPERATION2_LT -> return "<"
                Operation.OPERATION2_LTE -> return "<="
                Operation.OPERATION2_LTGT -> return "<>"
                Operation.OPERATION2_NEQ -> return "!="
                Operation.OPERATION2_NOT_LIKE -> return "not like"
                Operation.OPERATION2_LIKE -> return "like"
                Operation.OPERATION1_PLUS -> return "+"
                Operation.OPERATION1_MINUS -> return "-"
                Operation.OPERATION1_NOT -> return "not"
                Operation.OPERATION1_IS_NOT_NULL -> return "is not null"
                Operation.OPERATION1_IS_NULL -> return "is null"
            }
            return null
        }
    }

    init {
        this.left = left
        this.right = right
        this.operator = operator
    }
}