/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Tachyon Assosication Switzerland
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
package tachyon.runtime.type

import java.util.Map
/**
 * interface for resultset (query) object
 */
interface Query : Collection, Iterator, com.allaire.cfx.QueryCloneable{
    /**
     * @return return how many lines are affected by an update/insert
     */
    fun getUpdateCount(): Int

    /**
     * return a value of the resultset by specified column and row
     *
     * @param key column to get
     * @param row row to get from (1-recordcount)
     * @return value at the called position
     * @throws PageException if invalid position definition
     */
    @Deprecated
    @Deprecated("use instead <code>{@link #getAt(tachyon.runtime.type.Collection.Key, int)}</code>")
    @Throws(PageException::class)
    fun getAt(key: String?, row: Int): Object?

    /**
     * return a value of the resultset by specified column and row
     *
     * @param key column to get
     * @param row row to get from (1-recordcount)
     * @return value at the called position
     * @throws PageException if invalid position definition
     */
    @Throws(PageException::class)
    fun getAt(key: Collection.Key?, row: Int): Object?

    /**
     * return a value of the resultset by specified column and row, otherwise to getAt this method throw
     * no exception if value dont exist (return null)
     *
     * @param key column to get
     * @param row row to get from (1-recordcount)
     * @param defaultValue default value returned in case there is no value
     * @return value at the called position
     */
    @Deprecated
    @Deprecated("""use instead
	              <code>{@link #getAt(tachyon.runtime.type.Collection.Key, int, Object)}</code>""")
    fun getAt(key: String?, row: Int, defaultValue: Object?): Object?

    /**
     * return a value of the resultset by specified column and row, otherwise return defaultValue
     *
     * @param key column to get
     * @param row row to get from (1-recordcount)
     * @param defaultValue value returned in case row or column does not exist
     * @return value at the called position
     */
    fun getAt(key: Collection.Key?, row: Int, defaultValue: Object?): Object?

    /**
     * set a value at the defined position
     *
     * @param key column to set
     * @param row row to set
     * @param value value to fill
     * @return filled value
     * @throws PageException thrown when fails to set the value
     */
    @Deprecated
    @Deprecated("""use instead
	              <code>{@link #setAtEL(tachyon.runtime.type.Collection.Key, int, Object)}</code>""")
    @Throws(PageException::class)
    fun setAt(key: String?, row: Int, value: Object?): Object?

    /**
     * set a value at the defined position
     *
     * @param key column to set
     * @param row row to set
     * @param value value to fill
     * @return filled value
     * @throws PageException thrown when fails to set the value
     */
    @Throws(PageException::class)
    fun setAt(key: Collection.Key?, row: Int, value: Object?): Object?

    /**
     * set a value at the defined position
     *
     * @param key column to set
     * @param row row to set
     * @param value value to fill
     * @return filled value
     */
    @Deprecated
    @Deprecated("""use instead
	              <code>{@link #setAtEL(tachyon.runtime.type.Collection.Key, int, Object)}</code>""")
    fun setAtEL(key: String?, row: Int, value: Object?): Object?

    /**
     * set a value at the defined position
     *
     * @param key column to set
     * @param row row to set
     * @param value value to fill
     * @return filled value
     */
    fun setAtEL(key: Collection.Key?, row: Int, value: Object?): Object?

    /**
     * adds a new row to the resultset
     *
     * @param count count of rows to add
     * @return return if row is addded or nod (always true)
     */
    fun addRow(count: Int): Boolean

    /**
     * remove row from query
     *
     * @param row row number to remove
     * @return return new rowcount
     * @throws PageException exception thrown when it fails to remove the row
     */
    @Throws(PageException::class)
    fun removeRow(row: Int): Int

    /**
     * remove row from query
     *
     * @param row row number to remove
     * @return return new rowcount
     */
    fun removeRowEL(row: Int): Int

    /**
     * adds a new column to the resultset
     *
     * @param columnName name of the new column
     * @param content content of the new column inside an array (must have same size like query has
     * records)
     * @return if column is added return true otherwise false (always true, throw error when false)
     * @throws PageException exception thrown when not able to add the column
     */
    @Deprecated
    @Deprecated("use instead <code>{@link #addColumn(tachyon.runtime.type.Collection.Key, Array)}</code>")
    @Throws(PageException::class)
    fun addColumn(columnName: String?, content: Array?): Boolean

    /**
     * adds a new column to the resultset
     *
     * @param columnName name of the new column
     * @param content content of the new column inside an array (must have same size like query has
     * records)
     * @return if column is added return true otherwise false (always true, throw error when false)
     * @throws PageException exception thrown when not able to add the column
     */
    @Throws(PageException::class)
    fun addColumn(columnName: Collection.Key?, content: Array?): Boolean

    /**
     * adds a new column to the resultset
     *
     * @param columnName name of the new column
     * @param content content of the new column inside an array (must have same size like query has
     * records)
     * @param type data type from (java.sql.Types)
     * @return if column is added return true otherwise false (always true, throw error when false)
     * @throws PageException exception thrown when not able to add the column
     */
    @Deprecated
    @Deprecated("""use instead
	              <code>{@link #addColumn(tachyon.runtime.type.Collection.Key, Array, int)}</code>""")
    @Throws(PageException::class)
    fun addColumn(columnName: String?, content: Array?, type: Int): Boolean

    /**
     * adds a new column to the resultset
     *
     * @param columnName name of the new column
     * @param content content of the new column inside an array (must have same size like query has
     * records)
     * @param type data type from (java.sql.Types)
     * @return if column is added return true otherwise false (always true, throw error when false)
     * @throws PageException exception thrown when not able to add the column
     */
    @Throws(PageException::class)
    fun addColumn(columnName: Collection.Key?, content: Array?, type: Int): Boolean

    /**
     * @return Coloned Object
     */
    @Override
    fun clone(): Object

    /**
     * @return return all types
     */
    fun getTypes(): IntArray?

    /**
     * @return returns all types as Map (key==column)
     */
    fun getTypesAsMap(): Map<Collection.Key?, String?>?

    /**
     * return the query column matching to key
     *
     * @param key key to get
     * @return QueryColumn object
     * @throws PageException exception thrown in case there is no column with that name
     */
    @Deprecated
    @Deprecated("use instead <code>{@link #getColumn(tachyon.runtime.type.Collection.Key)}</code>")
    @Throws(PageException::class)
    fun getColumn(key: String?): QueryColumn?

    /**
     * return the query column matching to key
     *
     * @param key key to get
     * @return QueryColumn object
     * @throws PageException exception thrown in case there is no column with that name
     */
    @Throws(PageException::class)
    fun getColumn(key: Collection.Key?): QueryColumn?

    /**
     * return the query column matching to key, if key not exist return null
     *
     * @param key key to get
     * @param column default value returned in case there is no matching column
     * @return QueryColumn object
     */
    @Deprecated
    @Deprecated("""use instead
	              <code>{@link #getColumn(tachyon.runtime.type.Collection.Key, QueryColumn)}</code>""")
    fun getColumn(key: String?, column: QueryColumn?): QueryColumn?

    /**
     * return the query column matching to key, if key not exist return null
     *
     * @param key key to get
     * @param column default value returned in case there is no matching column
     * @return QueryColumn object
     */
    fun getColumn(key: Collection.Key?, column: QueryColumn?): QueryColumn?

    /**
     * remove column matching to key
     *
     * @param key key to remove
     * @return QueryColumn object removed
     * @throws PageException thrown when fail to remove column
     */
    @Deprecated
    @Deprecated("use instead <code>{@link #removeColumn(tachyon.runtime.type.Collection.Key)}</code>")
    @Throws(PageException::class)
    fun removeColumn(key: String?): QueryColumn?

    /**
     * remove column matching to key
     *
     * @param key key to remove
     * @return QueryColumn object removed
     * @throws PageException thrown when fail to remove column
     */
    @Throws(PageException::class)
    fun removeColumn(key: Collection.Key?): QueryColumn?

    /**
     * remove column matching to key
     *
     * @param key key to remove
     * @return QueryColumn object removed or null if column not exist
     */
    @Deprecated
    @Deprecated("use instead <code>{@link #removeColumnEL(tachyon.runtime.type.Collection.Key)}</code>")
    fun removeColumnEL(key: String?): QueryColumn?

    /**
     * remove column matching to key
     *
     * @param key key to remove
     * @return QueryColumn object removed or null if column not exist
     */
    fun removeColumnEL(key: Collection.Key?): QueryColumn?

    /**
     * sets the execution Time of the query
     *
     * @param l execution time
     */
    fun setExecutionTime(l: Long)

    /**
     * sorts a query by a column, direction is asc
     *
     * @param column column to sort
     * @throws PageException if fails to sort
     */
    @Deprecated
    @Deprecated("use instead <code>{@link #sort(tachyon.runtime.type.Collection.Key)}</code>")
    @Throws(PageException::class)
    fun sort(column: String?)

    /**
     * sorts a query by a column, direction is asc
     *
     * @param column column to sort
     * @throws PageException if fails to sort
     */
    @Throws(PageException::class)
    fun sort(column: Collection.Key?)

    /**
     * sorts a query by a column
     *
     * @param strColumn column to sort
     * @param order sort type (Query.ORDER_ASC or Query.ORDER_DESC)
     * @throws PageException if fails to sort
     */
    @Deprecated
    @Deprecated("use instead <code>{@link #sort(tachyon.runtime.type.Collection.Key, int)}</code>")
    @Throws(PageException::class)
    fun sort(strColumn: String?, order: Int)

    /**
     * sorts a query by a column
     *
     * @param strColumn column to sort
     * @param order sort type (Query.ORDER_ASC or Query.ORDER_DESC)
     * @throws PageException if fails to sort
     */
    @Throws(PageException::class)
    fun sort(strColumn: Collection.Key?, order: Int)
    fun getCacheType(): String?
    fun setCacheType(cacheType: String?)

    /**
     * sets if query is form cache or not
     *
     * @param isCached is cached or not
     */
    fun setCached(isCached: Boolean)

    /**
     * is query from cache or not
     *
     * @return is cached or not
     */
    fun isCached(): Boolean
    /**
     * @return returns struct with meta data to the query
     */
    // public Struct getMetaData();
    /**
     * @return returns array with meta data to the query (only column names and type)
     */
    fun getMetaDataSimple(): Array?

    @Throws(PageException::class)
    fun rename(columnName: Collection.Key?, newColumnName: Collection.Key?)

    @Override
    fun getColumnNames(): Array<Collection.Key?>?

    @Override
    fun getColumnNamesAsString(): Array<String?>?
    fun getColumnCount(): Int
    fun getGeneratedKeys(): Query?
    fun getSql(): SQL?
    fun getTemplate(): String?

    /**
     * @return return the query execution time in nanoseconds
     */
    fun getExecutionTime(): Long

    /**
     * @return returns the execution time
     */
    @Deprecated
    @Deprecated("use <code>getExecutionTime()</code> instead")
    fun executionTime(): Int
    fun enableShowQueryUsage()
    companion object {
        /**
         * Constant `ORDER_ASC`, used for method sort
         */
        val ORDER_ASC = 1

        /**
         * Constant `ORDER_DESC`, used for method sort
         */
        val ORDER_DESC = 2
    }
}