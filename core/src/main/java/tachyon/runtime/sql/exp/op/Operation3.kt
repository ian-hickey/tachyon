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
package tachyon.runtime.sql.exp.op

import tachyon.runtime.sql.exp.Expression

class Operation3(exp: Expression?, left: Expression?, right: Expression?, operator: Int) : ExpressionSupport(), Operation {
    private val exp: Expression?
    private val left: Expression?
    private val right: Expression?

    /**
     * @return the operator
     */
    val operator: Int

    @Override
    override fun toString(noAlias: Boolean): String? {
        // like escape
        if (Operation.OPERATION3_LIKE === operator) {
            return if (!hasAlias() || noAlias) {
                exp!!.toString(true).toString() + " like " + left!!.toString(true) + " escape " + right!!.toString(true)
            } else toString(true).toString() + " as " + getAlias()
        }
        // between
        return if (!hasAlias() || noAlias) {
            exp!!.toString(true).toString() + " between " + left!!.toString(true) + " and " + right!!.toString(true)
        } else toString(true).toString() + " as " + getAlias()
    }

    /**
     * @return the exp
     */
    fun getExp(): Expression? {
        return exp
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
        if (exp != null) {
            exp.reset()
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
        if (exp != null) {
            exp.setCacheColumn(cacheColumn)
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
        if (right is Operation && (right as Operation?)!!.hasAggregate()) {
            return true
        }
        if (exp is OperationAggregate) {
            return true
        }
        return if (exp is Operation && (exp as Operation?)!!.hasAggregate()) {
            true
        } else false
    }

    init {
        this.exp = exp
        this.left = left
        this.right = right
        this.operator = operator
    }
}