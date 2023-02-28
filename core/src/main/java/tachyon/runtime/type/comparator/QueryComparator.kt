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
package tachyon.runtime.type.comparator

import java.util.Comparator

/**
 * Implementation of a Comparator that will sort multiple rows of a query all at the same time
 */
class QueryComparator(pc: PageContext?, target: QueryImpl?, sortExpressions: Array<Expression?>?, isUnion: Boolean, sql: SQL?) : Comparator<Integer?> {
    private val sorts: Array<Comparator?>?
    private val cols: Array<Key?>?
    private val target: Query?
    private val paramKey: Collection.Key? = KeyImpl("?")
    private var numSorts = 0
    @Throws(PageException::class)
    private fun addSOrt(columnKey: Key?, isAsc: Boolean) {
        cols!![numSorts] = columnKey
        val type: Int = target.getColumn(columnKey).getType()
        // These types use a numeric sort
        if (type == Types.BIGINT || type == Types.BIT || type == Types.INTEGER || type == Types.SMALLINT || type == Types.TINYINT || type == Types.DECIMAL || type == Types.DOUBLE || type == Types.NUMERIC || type == Types.REAL) {
            sorts!![numSorts] = NumberComparator(isAsc, true)
            // Everything else is a case-sensitive text sort
        } else {
            sorts!![numSorts] = TextComparator(isAsc, false)
        }
        numSorts++
    }

    @Override
    fun compare(oLeft: Integer?, oRight: Integer?): Int {
        var currentResult = 0
        return try {
            // Loop over all our sorts.  We'll keep checking until we find a column that sorts above or below,
            // or until we run out of sorts to check
            for (i in 0 until numSorts) {
                currentResult = sorts!![i].compare(
                        target.getAt(cols!![i], oLeft),
                        target.getAt(cols[i], oRight)
                )
                // Short circuit if one row is already sorted above or below another
                if (currentResult != 0) {
                    return currentResult
                }
                // If the current sorts were the same for both rows, we continue to the next sort
            }
            // If we made it all the way through the sorts, return the last value
            currentResult
        } catch (e: PageException) {
            //throw new RuntimeException(e);
            0
        }
    }

    /**
     * constructor of the class
     *
     */
    init {
        sorts = arrayOfNulls<Comparator?>(sortExpressions!!.size)
        cols = arrayOfNulls<Key?>(sortExpressions!!.size)
        this.target = target

        // Build up an array of comparators based on the valid sort expressions, in order
        for (i in sortExpressions.indices) {
            val sortExpression: Expression? = sortExpressions!![i]
            var columnKey: Key
            if (!isUnion) {
                var ordinalIndex: Integer?
                if (sortExpression is Literal) {
                    if (sortExpression is ValueNumber && Caster.toInteger((sortExpression as Literal?).getValue(), null).also { ordinalIndex = it } != null && ordinalIndex > 0 && ordinalIndex <= target.getColumnNames().length) {
                        // Sort the column referenced by the ordinal position
                        addSOrt(target.getColumnNames().get(ordinalIndex - 1), !sortExpression.isDirectionBackward())
                    } else {
                        // All other non-integer literals are invalid.
                        throw IllegalQoQException("ORDER BY item [" + sortExpression.toString(true).toString() + "] in position " + (i + 1).toString() + " cannot be a literal value unless it is an integer matching a select column's ordinal position.", null, sql, null)
                    }
                } else {
                    // order by ? -- ignore this as well
                    if (sortExpression is Column && (sortExpression as Column?).getColumn().equals(paramKey)) continue

                    // Lookup column in query based on the index stored in the order by expression
                    addSOrt(target.getColumnNames().get(sortExpression.getIndex() - 1), !sortExpression.isDirectionBackward())
                }
            } else if (sortExpression is Column) {
                val c: Column? = sortExpression as Column?
                // Lookup column in query based on name of column. unions don't allow operations in
                // the order by
                addSOrt(c.getColumn(), !sortExpression.isDirectionBackward())
            } else {
                throw IllegalQoQException("ORDER BY items must be a column name/alias from the first select list if the statement contains a UNION operator", null, sql, null)
            }
        }
    }
}