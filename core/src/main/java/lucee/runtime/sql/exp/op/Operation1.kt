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

class Operation1(exp: Expression?, operator: Int) : ExpressionSupport(), Operation {
    private val exp: Expression?

    /**
     * @return the operator
     */
    val operator: Int

    /**
     * @return the exp
     */
    fun getExp(): Expression? {
        return exp
    }

    @Override
    override fun toString(noAlias: Boolean): String? {
        return if (!hasAlias() || noAlias) {
            if (operator == OPERATION1_IS_NULL || operator == OPERATION1_IS_NOT_NULL) {
                exp!!.toString(true).toString() + " " + Operation2.toString(operator)
            } else Operation2.toString(operator).toString() + " " + exp!!.toString(true)
        } else toString(true).toString() + " as " + getAlias()
    }

    @Override
    override fun reset() {
        if (exp != null) {
            exp.reset()
        }
    }

    @Override
    override fun setCacheColumn(cacheColumn: Boolean) {
        if (exp != null) {
            exp.setCacheColumn(cacheColumn)
        }
    }

    @Override
    override fun hasAggregate(): Boolean {
        if (exp is OperationAggregate) {
            return true
        }
        return if (exp is Operation && (exp as Operation?)!!.hasAggregate()) {
            true
        } else false
    }

    init {
        this.exp = exp
        this.operator = operator
    }
}