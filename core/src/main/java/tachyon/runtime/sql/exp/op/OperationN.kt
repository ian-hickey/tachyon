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

import java.util.Iterator

class OperationN(
        /**
         * @return the operator
         */
        val operator: String?, operants: List?) : ExpressionSupport(), Operation {
    private val operants: List?

    @Override
    override fun toString(noAlias: Boolean): String? {
        if (!hasIndex() || noAlias) {
            val sb = StringBuffer()
            sb.append(operator)
            sb.append('(')
            val it: Iterator = operants.iterator()
            var isFirst = true
            while (it.hasNext()) {
                if (!isFirst) sb.append(',')
                val exp: Expression = it.next() as Expression
                sb.append(exp.toString(!operator.equalsIgnoreCase("cast")))
                isFirst = false
            }
            sb.append(')')
            return sb.toString()
        }
        return toString(true).toString() + " as " + getAlias()
    }

    /**
     * @return the operants
     */
    fun getOperants(): Array<Expression?>? {
        return if (operants == null) arrayOfNulls<Expression?>(0) else operants.toArray(arrayOfNulls<Expression?>(operants.size()))
    }

    @Override
    override fun reset() {
        val it: Iterator = operants.iterator()
        while (it.hasNext()) {
            val exp: Expression = it.next() as Expression
            exp.reset()
        }
    }

    @Override
    override fun setCacheColumn(cacheColumn: Boolean) {
        val it: Iterator = operants.iterator()
        while (it.hasNext()) {
            val exp: Expression = it.next() as Expression
            exp.setCacheColumn(cacheColumn)
        }
    }

    @Override
    override fun hasAggregate(): Boolean {
        val it: Iterator = operants.iterator()
        while (it.hasNext()) {
            val exp: Expression = it.next() as Expression
            if (exp is OperationAggregate) {
                return true
            }
            if (exp is Operation && (exp as Operation)!!.hasAggregate()) {
                return true
            }
        }
        return false
    }

    init {
        this.operants = operants
    }
}