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
package com.allaire.cfx

import java.sql.ResultSet

/**
 * Alternative Implementation of Jeremy Allaire's Query Interface
 */
interface Query : ResultSet {
    /**
     * @return adds a row to resultset
     */
    fun addRow(): Int

    /**
     * returns index of a columnName
     *
     * @param coulmnName column name to get index for
     * @return index of a columnName
     */
    fun getColumnIndex(coulmnName: String?): Int

    /**
     * @return All column Names of resultset as string
     */
    @get:Deprecated("use instead <code>getColumnNamesAsString()</code>")
    @get:Deprecated
    val columns: Array<String>
    val columnNamesAsString: Array<String?>?
    val columnNames: Array<Any?>?

    /**
     * returns one field of a Query as String
     *
     * @param row row number to get
     * @param col column number to get
     * @return data from query object
     * @throws IndexOutOfBoundsException thrown when col or/and row are invalid
     */
    @Throws(IndexOutOfBoundsException::class)
    fun getData(row: Int, col: Int): String

    /**
     * @return returns name of the query
     */
    val name: String

    /**
     * @return returns row count
     */
    val rowCount: Int

    /**
     * sets value at a defined position in Query
     *
     * @param row row number to set
     * @param col column number to set
     * @param value value to set
     * @throws IndexOutOfBoundsException thrown when col or/and row are invalid
     */
    @Throws(IndexOutOfBoundsException::class)
    fun setData(row: Int, col: Int, value: String?)
}