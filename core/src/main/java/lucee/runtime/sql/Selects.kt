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
package lucee.runtime.sql

import java.util.ArrayList

class Selects {
    private val orderbys: List<Expression?>? = ArrayList<Expression?>()
    private val selects: List<Select?>? = ArrayList<Select?>()
    fun addOrderByExpression(exp: Expression?) {
        orderbys.add(exp)
    }

    @Throws(PageException::class)
    fun calcOrderByExpressions() {
        if (getSelects()!!.size == 1) {
            // Check if this order by is already present in the select
            for (exp in getOrderbys()!!) {
                var ordinalIndex: Integer?
                // For literals who are integers that point to a select column, we'll use these as ordinal indexes later, so no need to do anything else with them.
                if (exp is Literal && Caster.toInteger((exp as Literal?).getValue(), null).also { ordinalIndex = it } != null && ordinalIndex <= getSelects()!![0]!!.getSelects().length) {
                    continue
                }

                // For each expression in the select column list
                for (col in getSelects()!![0]!!.getSelects()) {
                    // If this is the same column or the same alias...
                    if (col is Column && col.toString(true).equals(exp.toString(true)) || col.getAlias().equals(exp.getAlias())) {
                        // Then set our order by's index to point to the index
                        // of the column that has that data
                        exp.setIndex(col.getIndex())
                        break
                    }
                }
                // Didn't find it? It means we're ordering on a column we're not selecting like
                // SELECT col1 FROM table ORDER BY col2
                if (exp.getIndex() === 0) {

                    // Don't allow this invalid scenario
                    if (getSelects()!![0].isDistinct()) {
                        throw DatabaseException("ORDER BY items must appear in the select list if SELECT DISTINCT is specified. Order by expression not found is [" + exp.toString(true).toString() + "]", null, null, null)
                    }

                    // We need to add a phantom column into our result so
                    // we can track the value and order on it
                    exp.setAlias("__order_by_expression__" + getSelects()!![0]!!.getSelects().length)
                    getSelects()!![0]!!.addSelectExpression(exp)
                }
            }
        }
    }

    /**
     * @return the orderbys
     */
    fun getOrderbys(): Array<Expression?>? {
        return if (orderbys == null) arrayOfNulls<Expression?>(0) else orderbys.toArray(arrayOfNulls<Expression?>(orderbys.size()))
    }

    fun addSelect(select: Select?) {
        selects.add(select)
    }

    fun getSelects(): Array<Select?>? {
        return if (selects == null) arrayOfNulls<Select?>(0) else selects.toArray(arrayOfNulls<Select?>(selects.size()))
    }

    @Override
    override fun toString(): String {
        return _toString(this)!!
    }

    val tables: Array<Any?>?
        get() {
            val it: Iterator<Select?> = selects!!.iterator()
            var s: Select?
            val rtn: ArrayList<Column?> = ArrayList<Column?>()
            var froms: Array<Column?>
            while (it.hasNext()) {
                s = it.next()
                froms = s!!.getFroms()
                for (i in froms.indices) {
                    rtn.add(froms[i])
                }
            }
            return rtn.toArray(arrayOfNulls<Column?>(rtn.size()))
        }
    val isDistinct: Boolean
        get() {
            var s: Select?
            val len: Int = selects!!.size()
            if (len == 1) {
                s = selects[0]
                return s.isDistinct()
            }
            for (i in 1 until len) {
                s = selects[i]
                if (!s!!.isUnionDistinct()) return false
            }
            return true
        }

    @get:Throws(DatabaseException::class)
    val distincts: Array<Any?>?
        get() {
            val columns: List<Column?> = ArrayList<Column?>()
            val s: Select?
            val len: Int = selects!!.size()
            if (len == 1) {
                s = selects[0]
                val _selects: Array<Expression?> = s!!.getSelects()
                for (i in _selects.indices) {
                    if (_selects[i] is Column) {
                        columns.add(_selects[i] as Column?)
                    }
                }
                return columns.toArray(arrayOfNulls<Column?>(columns.size()))
            }
            throw DatabaseException("not supported for Union distinct", null, null, null)
        }

    companion object {
        fun _toString(__selects: Selects?): String? {
            val _selects: Array<Select?>? = __selects!!.getSelects()
            var s: Select?
            val sb = StringBuffer()
            for (y in _selects.indices) {
                s = _selects!![y]
                if (y > 0) {
                    if (s!!.isUnionDistinct()) sb.append("union distinct\n\n") else sb.append("union\n\n")
                }
                sb.append("select\n\t")
                if (s.isDistinct()) sb.append("distinct\n\t")
                val top: ValueNumber = s.getTop()
                if (top != null) sb.append("""top ${top.getString().toString()}
	""")
                // select
                val sels: Array<Expression?> = s!!.getSelects()
                var exp: Expression?
                var first = true
                for (i in sels.indices) {
                    if (!first) sb.append("\t,")
                    exp = sels[i]
                    sb.append(exp.toString(false).toString() + "\n")
                    first = false
                }

                // from
                sb.append("from\n\t")
                val forms: Array<Column?> = s!!.getFroms()
                first = true
                for (i in forms.indices) {
                    if (!first) sb.append("\t,")
                    exp = forms[i]
                    sb.append(exp.toString(false).toString() + "\n")
                    first = false
                }

                // where
                if (s!!.getWhere() != null) {
                    sb.append("where \n\t")
                    sb.append(s!!.getWhere().toString(true))
                    sb.append("\n")
                }

                // group by
                val gbs: Array<Expression?> = s!!.getGroupbys()
                if (gbs.size > 0) {
                    sb.append("group by\n\t")
                    first = true
                    for (i in gbs.indices) {
                        if (!first) sb.append("\t,")
                        exp = gbs[i]
                        sb.append(exp.toString(false).toString() + "\n")
                        first = false
                    }
                }

                // having
                val having: Operation = s.getHaving()
                if (having != null) {
                    sb.append("having \n\t")
                    sb.append(having.toString(true))
                    sb.append("\n")
                }
            }

            // order by
            if (__selects.orderbys != null && __selects.orderbys.size() > 0) {
                sb.append("order by\n\t")
                val it: Iterator<Expression?> = __selects.orderbys.iterator()
                var exp: Expression?
                var first = true
                while (it.hasNext()) {
                    if (!first) sb.append("\t,")
                    exp = it.next()
                    sb.append(exp.toString(false).toString() + " " + (if (exp.isDirectionBackward()) "DESC" else "ASC") + "\n")
                    first = false
                }
            }
            return sb.toString()
        }
    }
}