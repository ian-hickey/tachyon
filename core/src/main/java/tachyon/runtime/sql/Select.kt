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
package tachyon.runtime.sql

import java.util.ArrayList

class Select {
    private val selects: List? = ArrayList()
    var additionalColumns: Set<String?>? = HashSet()
        private set
    private val froms: List? = ArrayList()
    private var where: Operation? = null
    private val groupbys: List? = ArrayList()
    /**
     * @return the havings
     */
    /**
     * @param having the having to set
     */
    var having: Operation? = null

    /**
     * @return the top
     */
    var top: ValueNumber? = null
    var isUnionDistinct = false
        set(b) {
            field = b
        }

    // print.out("-"+b);
    private var unionDistinct = false
    fun addSelectExpression(select: Expression?) {
        // Make sure there isn't already a column or alias of the same name. This will just cause issues down the road since our
        // column counts in the final query won't match the index in the expression
        for (col in getSelects()!!) {
            if (col.getAlias().equalsIgnoreCase(select.getAlias())) {
                return
            }
        }
        selects.add(select)
        select.setIndex(selects.size())
    }

    fun expandAsterisks(source: Query?) {
        val selectCols: Array<Expression?>? = getSelects()
        selects.clear()
        var it: Iterator<Key?>
        var k: Key?
        for (col in selectCols!!) {
            if (col.getAlias().equals("*")) {
                it = source.keyIterator()
                while (it.hasNext()) {
                    k = it.next()
                    addSelectExpression(ColumnExpression(k.getString(), 0))
                }
            } else {
                addSelectExpression(col)
            }
        }
        // We may not need all of these now.
        calcAdditionalColumns(additionalColumns)
    }

    fun addFromExpression(exp: Column?) {
        froms.add(exp)
        exp.setIndex(froms.size())
    }

    fun setWhereExpression(where: Operation?) {
        this.where = where
    }

    fun addGroupByExpression(col: Expression?) {
        groupbys.add(col)
    }

    fun calcAdditionalColumns(allColumns: Set<String?>?) {
        // Remove any columns we are explicitly selecting
        for (expSelect in getSelects()!!) {
            if (expSelect is ColumnExpression) {
                val ce: ColumnExpression? = expSelect as ColumnExpression?
                allColumns.remove(ce.getColumnName())
            }
        }
        // What's left are columns used by functions and aggregates,
        // but not directly part of the final result
        additionalColumns = allColumns
    }

    /**
     * @return the froms
     */
    fun getFroms(): Array<Column?>? {
        return froms.toArray(arrayOfNulls<Column?>(froms.size()))
    }

    /**
     * @return the groupbys
     */
    fun getGroupbys(): Array<Expression?>? {
        return if (groupbys == null) arrayOfNulls<Column?>(0) else groupbys.toArray(arrayOfNulls<Expression?>(groupbys.size()))
    }

    /**
     * @return the selects
     */
    fun getSelects(): Array<Expression?>? {
        return selects.toArray(arrayOfNulls<Expression?>(selects.size()))
    }

    /**
     * @return whether at least one select is an aggregate
     */
    fun hasAggregateSelect(): Boolean {
        for (col in getSelects()!!) {
            if (col is OperationAggregate) {
                return true
            }
            if (col is Operation && (col as Operation?).hasAggregate()) {
                return true
            }
        }
        return false
    }

    /**
     * @return the where
     */
    fun getWhere(): Operation? {
        return where
    }
}