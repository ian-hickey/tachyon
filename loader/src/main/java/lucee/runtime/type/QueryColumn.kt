/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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
package lucee.runtime.type

import lucee.runtime.exp.PageException

/**
 * represent a Single column of a query object
 */
interface QueryColumn : Collection, Reference, Castable {
    /**
     * removes the value but dont the index
     *
     * @param row row number
     * @return removed Object
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun remove(row: Int): Object?

    /**
     * remove a row from query
     *
     * @param row row number
     * @return removed value
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun removeRow(row: Int): Object?

    /**
     * removes method with int as key
     *
     * @param row row number
     * @return removed Object
     */
    fun removeEL(row: Int): Object?

    /**
     * get method with an int as key, return empty default value for invalid row
     *
     * @param row row to get value
     * @return row value
     * @throws PageException Page Exceptiontion
     */
    @Deprecated
    @Deprecated("use instead <code>get(int row, Object defaultValue)</code>")
    @Throws(PageException::class)
    operator fun get(row: Int): Object?

    /**
     * return the value in this row (can be null), when row number is invalid the default value is
     * returned
     *
     * @param row row to get value
     * @param emptyValue value returned when row does not exists or the rows value is null
     * @return row value
     */
    operator fun get(row: Int, emptyValue: Object?): Object?

    /**
     * set method with an int as key
     *
     * @param row row to set
     * @param value value to set
     * @return setted value
     * @throws PageException Page Exceptionn
     */
    @Throws(PageException::class)
    operator fun set(row: Int, value: Object?): Object?

    /**
     * adds a value to the column
     *
     * @param value value to add
     */
    fun add(value: Object?)

    /**
     * setExpressionLess method with an int as key
     *
     * @param row row to set
     * @param value value to set
     * @return setted value
     */
    fun setEL(row: Int, value: Object?): Object?

    /**
     * @param count adds count row to the column
     */
    fun addRow(count: Int)

    /**
     * @return returns the type of the Column (java.sql.Types.XYZ)
     */
    val type: Int

    /**
     * @return returns the type of the Column as String
     */
    val typeAsString: String?

    /**
     * cuts row to defined size
     *
     * @param maxrows max rows
     */
    fun cutRowsTo(maxrows: Int)
}