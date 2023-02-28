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
package tachyon.runtime.cfx

import java.io.InputStream

/**
 * Implementation of the Query Interface
 */
class QueryWrap : Query, Cloneable {
    private val rst: tachyon.runtime.type.Query

    /**
     * @see com.allaire.cfx.Query.getName
     */
    @get:Override
    val name: String

    /**
     * constructor of the class
     *
     * @param rst runtime Query
     */
    constructor(rst: tachyon.runtime.type.Query) {
        this.rst = rst
        name = rst.getName()
    }

    /**
     * constructor of the class
     *
     * @param rst runtime Query
     * @param name name of the query (otherwise rst.getName())
     */
    constructor(rst: tachyon.runtime.type.Query, name: String) {
        this.rst = rst
        this.name = name
    }

    /**
     * @see com.allaire.cfx.Query.addRow
     */
    @Override
    fun addRow(): Int {
        return rst.addRow()
    }

    /**
     * @see com.allaire.cfx.Query.getColumnIndex
     */
    @Override
    fun getColumnIndex(coulmnName: String?): Int {
        return rst.getColumnIndex(coulmnName)
    }

    /**
     * @see com.allaire.cfx.Query.getColumns
     */
    @get:SuppressWarnings("deprecation")
    @get:Override
    val columns: Array<String>
        get() = rst.getColumns()

    @get:Override
    val columnNames: Array<Any>
        get() = rst.getColumnNames()

    @get:Override
    val columnNamesAsString: Array<String>
        get() = rst.getColumnNamesAsString()

    /**
     * @see com.allaire.cfx.Query.getData
     */
    @Override
    @Throws(IndexOutOfBoundsException::class)
    fun getData(row: Int, col: Int): String {
        return rst.getData(row, col)
    }

    /**
     * @see com.allaire.cfx.Query.getRowCount
     */
    @get:Override
    val rowCount: Int
        get() = rst.getRowCount()

    /**
     * @see com.allaire.cfx.Query.setData
     */
    @Override
    @Throws(IndexOutOfBoundsException::class)
    fun setData(row: Int, col: Int, value: String?) {
        rst.setData(row, col, value)
    }

    /**
     * @see java.sql.ResultSet.absolute
     */
    @Override
    @Throws(SQLException::class)
    fun absolute(row: Int): Boolean {
        return rst.absolute(row)
    }

    /**
     * @see java.sql.ResultSet.afterLast
     */
    @Override
    @Throws(SQLException::class)
    fun afterLast() {
        rst.afterLast()
    }

    /**
     * @see java.sql.ResultSet.beforeFirst
     */
    @Override
    @Throws(SQLException::class)
    fun beforeFirst() {
        rst.beforeFirst()
    }

    /**
     * @see java.sql.ResultSet.cancelRowUpdates
     */
    @Override
    @Throws(SQLException::class)
    fun cancelRowUpdates() {
        rst.cancelRowUpdates()
    }

    /**
     * @see java.sql.ResultSet.clearWarnings
     */
    @Override
    @Throws(SQLException::class)
    fun clearWarnings() {
        rst.clearWarnings()
    }

    /**
     * @see java.lang.Object.clone
     */
    @Override
    fun clone(): Object {
        return rst.clone()
    }

    /**
     * @see java.sql.ResultSet.close
     */
    @Override
    @Throws(SQLException::class)
    fun close() {
        rst.close()
    }

    /**
     * @see java.sql.ResultSet.deleteRow
     */
    @Override
    @Throws(SQLException::class)
    fun deleteRow() {
        rst.deleteRow()
    }

    /**
     * @see java.sql.ResultSet.findColumn
     */
    @Override
    @Throws(SQLException::class)
    fun findColumn(columnName: String?): Int {
        return rst.findColumn(columnName)
    }

    /**
     * @see java.sql.ResultSet.first
     */
    @Override
    @Throws(SQLException::class)
    fun first(): Boolean {
        return rst.first()
    }

    /**
     * @see java.sql.ResultSet.getArray
     */
    @Override
    @Throws(SQLException::class)
    fun getArray(i: Int): Array {
        return rst.getArray(i)
    }

    /**
     * @see java.sql.ResultSet.getArray
     */
    @Override
    @Throws(SQLException::class)
    fun getArray(colName: String?): Array {
        return rst.getArray(colName)
    }

    /**
     * @see java.sql.ResultSet.getAsciiStream
     */
    @Override
    @Throws(SQLException::class)
    fun getAsciiStream(columnIndex: Int): InputStream {
        return rst.getAsciiStream(columnIndex)
    }

    /**
     * @see java.sql.ResultSet.getAsciiStream
     */
    @Override
    @Throws(SQLException::class)
    fun getAsciiStream(columnName: String?): InputStream {
        return rst.getAsciiStream(columnName)
    }

    /**
     * @see java.sql.ResultSet.getBigDecimal
     */
    @Override
    @SuppressWarnings("deprecation")
    @Throws(SQLException::class)
    fun getBigDecimal(columnIndex: Int, scale: Int): BigDecimal {
        return rst.getBigDecimal(columnIndex, scale)
    }

    /**
     * @see java.sql.ResultSet.getBigDecimal
     */
    @Override
    @Throws(SQLException::class)
    fun getBigDecimal(columnIndex: Int): BigDecimal {
        return rst.getBigDecimal(columnIndex)
    }

    /**
     * @see java.sql.ResultSet.getBigDecimal
     */
    @Override
    @SuppressWarnings("deprecation")
    @Throws(SQLException::class)
    fun getBigDecimal(columnName: String?, scale: Int): BigDecimal {
        return rst.getBigDecimal(columnName, scale)
    }

    /**
     * @see java.sql.ResultSet.getBigDecimal
     */
    @Override
    @Throws(SQLException::class)
    fun getBigDecimal(columnName: String?): BigDecimal {
        return rst.getBigDecimal(columnName)
    }

    /**
     * @see java.sql.ResultSet.getBinaryStream
     */
    @Override
    @Throws(SQLException::class)
    fun getBinaryStream(columnIndex: Int): InputStream {
        return rst.getBinaryStream(columnIndex)
    }

    /**
     * @see java.sql.ResultSet.getBinaryStream
     */
    @Override
    @Throws(SQLException::class)
    fun getBinaryStream(columnName: String?): InputStream {
        return rst.getBinaryStream(columnName)
    }

    /**
     * @see java.sql.ResultSet.getBlob
     */
    @Override
    @Throws(SQLException::class)
    fun getBlob(i: Int): Blob {
        return rst.getBlob(i)
    }

    /**
     * @see java.sql.ResultSet.getBlob
     */
    @Override
    @Throws(SQLException::class)
    fun getBlob(colName: String?): Blob {
        return rst.getBlob(colName)
    }

    /**
     * @see java.sql.ResultSet.getBoolean
     */
    @Override
    @Throws(SQLException::class)
    fun getBoolean(columnIndex: Int): Boolean {
        return rst.getBoolean(columnIndex)
    }

    /**
     * @see java.sql.ResultSet.getBoolean
     */
    @Override
    @Throws(SQLException::class)
    fun getBoolean(columnName: String?): Boolean {
        return rst.getBoolean(columnName)
    }

    /**
     * @see java.sql.ResultSet.getByte
     */
    @Override
    @Throws(SQLException::class)
    fun getByte(columnIndex: Int): Byte {
        return rst.getByte(columnIndex)
    }

    /**
     * @see java.sql.ResultSet.getByte
     */
    @Override
    @Throws(SQLException::class)
    fun getByte(columnName: String?): Byte {
        return rst.getByte(columnName)
    }

    /**
     * @see java.sql.ResultSet.getBytes
     */
    @Override
    @Throws(SQLException::class)
    fun getBytes(columnIndex: Int): ByteArray {
        return rst.getBytes(columnIndex)
    }

    /**
     * @see java.sql.ResultSet.getBytes
     */
    @Override
    @Throws(SQLException::class)
    fun getBytes(columnName: String?): ByteArray {
        return rst.getBytes(columnName)
    }

    /**
     * @see java.sql.ResultSet.getCharacterStream
     */
    @Override
    @Throws(SQLException::class)
    fun getCharacterStream(columnIndex: Int): Reader {
        return rst.getCharacterStream(columnIndex)
    }

    /**
     * @see java.sql.ResultSet.getCharacterStream
     */
    @Override
    @Throws(SQLException::class)
    fun getCharacterStream(columnName: String?): Reader {
        return rst.getCharacterStream(columnName)
    }

    /**
     * @see java.sql.ResultSet.getClob
     */
    @Override
    @Throws(SQLException::class)
    fun getClob(i: Int): Clob {
        return rst.getClob(i)
    }

    /**
     * @see java.sql.ResultSet.getClob
     */
    @Override
    @Throws(SQLException::class)
    fun getClob(colName: String?): Clob {
        return rst.getClob(colName)
    }

    /**
     * @see java.sql.ResultSet.getConcurrency
     */
    @get:Throws(SQLException::class)
    @get:Override
    val concurrency: Int
        get() = rst.getConcurrency()

    /**
     * @see java.sql.ResultSet.getCursorName
     */
    @get:Throws(SQLException::class)
    @get:Override
    val cursorName: String
        get() = rst.getCursorName()

    /**
     * @see java.sql.ResultSet.getDate
     */
    @Override
    @Throws(SQLException::class)
    fun getDate(columnIndex: Int, cal: Calendar?): Date {
        return rst.getDate(columnIndex, cal)
    }

    /**
     * @see java.sql.ResultSet.getDate
     */
    @Override
    @Throws(SQLException::class)
    fun getDate(columnIndex: Int): Date {
        return rst.getDate(columnIndex)
    }

    /**
     * @see java.sql.ResultSet.getDate
     */
    @Override
    @Throws(SQLException::class)
    fun getDate(columnName: String?, cal: Calendar?): Date {
        return rst.getDate(columnName, cal)
    }

    /**
     * @see java.sql.ResultSet.getDate
     */
    @Override
    @Throws(SQLException::class)
    fun getDate(columnName: String?): Date {
        return rst.getDate(columnName)
    }

    /**
     * @see java.sql.ResultSet.getDouble
     */
    @Override
    @Throws(SQLException::class)
    fun getDouble(columnIndex: Int): Double {
        return rst.getDouble(columnIndex)
    }

    /**
     * @see java.sql.ResultSet.getDouble
     */
    @Override
    @Throws(SQLException::class)
    fun getDouble(columnName: String?): Double {
        return rst.getDouble(columnName)
    }
    /**
     * @see java.sql.ResultSet.getFetchDirection
     */
    /**
     * @see java.sql.ResultSet.setFetchDirection
     */
    @get:Throws(SQLException::class)
    @get:Override
    @set:Throws(SQLException::class)
    @set:Override
    var fetchDirection: Int
        get() = rst.getFetchDirection()
        set(direction) {
            rst.setFetchDirection(direction)
        }
    /**
     * @see java.sql.ResultSet.getFetchSize
     */
    /**
     * @see java.sql.ResultSet.setFetchSize
     */
    @get:Throws(SQLException::class)
    @get:Override
    @set:Throws(SQLException::class)
    @set:Override
    var fetchSize: Int
        get() = rst.getFetchSize()
        set(rows) {
            rst.setFetchSize(rows)
        }

    /**
     * @see java.sql.ResultSet.getFloat
     */
    @Override
    @Throws(SQLException::class)
    fun getFloat(columnIndex: Int): Float {
        return rst.getFloat(columnIndex)
    }

    /**
     * @see java.sql.ResultSet.getFloat
     */
    @Override
    @Throws(SQLException::class)
    fun getFloat(columnName: String?): Float {
        return rst.getFloat(columnName)
    }

    /**
     * @see java.sql.ResultSet.getInt
     */
    @Override
    @Throws(SQLException::class)
    fun getInt(columnIndex: Int): Int {
        return rst.getInt(columnIndex)
    }

    /**
     * @see java.sql.ResultSet.getInt
     */
    @Override
    @Throws(SQLException::class)
    fun getInt(columnName: String?): Int {
        return rst.getInt(columnName)
    }

    /**
     * @see java.sql.ResultSet.getLong
     */
    @Override
    @Throws(SQLException::class)
    fun getLong(columnIndex: Int): Long {
        return rst.getLong(columnIndex)
    }

    /**
     * @see java.sql.ResultSet.getLong
     */
    @Override
    @Throws(SQLException::class)
    fun getLong(columnName: String?): Long {
        return rst.getLong(columnName)
    }

    /**
     * @see java.sql.ResultSet.getMetaData
     */
    @get:Throws(SQLException::class)
    @get:Override
    val metaData: ResultSetMetaData
        get() = rst.getMetaData()

    /**
     * @see java.sql.ResultSet.getObject
     */
    @Override
    @Throws(SQLException::class)
    fun getObject(i: Int, map: Map<String?, Class<*>?>?): Object {
        return rst.getObject(i, map)
    }

    /**
     * @see java.sql.ResultSet.getObject
     */
    @Override
    @Throws(SQLException::class)
    fun getObject(columnIndex: Int): Object {
        return rst.getObject(columnIndex)
    }

    /**
     * @see java.sql.ResultSet.getObject
     */
    @Override
    @Throws(SQLException::class)
    fun getObject(colName: String?, map: Map<String?, Class<*>?>?): Object {
        return rst.getObject(colName, map)
    }

    /**
     * @see java.sql.ResultSet.getObject
     */
    @Override
    @Throws(SQLException::class)
    fun getObject(columnName: String?): Object {
        return rst.getObject(columnName)
    }

    /**
     * @return recordcount of the query
     */
    val recordcount: Int
        get() = rst.getRecordcount()

    /**
     * @see java.sql.ResultSet.getRef
     */
    @Override
    @Throws(SQLException::class)
    fun getRef(i: Int): Ref {
        return rst.getRef(i)
    }

    /**
     * @see java.sql.ResultSet.getRef
     */
    @Override
    @Throws(SQLException::class)
    fun getRef(colName: String?): Ref {
        return rst.getRef(colName)
    }

    /**
     * @see java.sql.ResultSet.getRow
     */
    @get:Throws(SQLException::class)
    @get:Override
    val row: Int
        get() = rst.getRow()

    /**
     * @see java.sql.ResultSet.getShort
     */
    @Override
    @Throws(SQLException::class)
    fun getShort(columnIndex: Int): Short {
        return rst.getShort(columnIndex)
    }

    /**
     * @see java.sql.ResultSet.getShort
     */
    @Override
    @Throws(SQLException::class)
    fun getShort(columnName: String?): Short {
        return rst.getShort(columnName)
    }

    /**
     * @see java.sql.ResultSet.getStatement
     */
    @get:Throws(SQLException::class)
    @get:Override
    val statement: Statement
        get() = rst.getStatement()

    /**
     * @see java.sql.ResultSet.getString
     */
    @Override
    @Throws(SQLException::class)
    fun getString(columnIndex: Int): String {
        return rst.getString(columnIndex)
    }

    /**
     * @see java.sql.ResultSet.getString
     */
    @Override
    @Throws(SQLException::class)
    fun getString(columnName: String?): String {
        return rst.getString(columnName)
    }

    /**
     * @see java.sql.ResultSet.getTime
     */
    @Override
    @Throws(SQLException::class)
    fun getTime(columnIndex: Int, cal: Calendar?): Time {
        return rst.getTime(columnIndex, cal)
    }

    /**
     * @see java.sql.ResultSet.getTime
     */
    @Override
    @Throws(SQLException::class)
    fun getTime(columnIndex: Int): Time {
        return rst.getTime(columnIndex)
    }

    /**
     * @see java.sql.ResultSet.getTime
     */
    @Override
    @Throws(SQLException::class)
    fun getTime(columnName: String?, cal: Calendar?): Time {
        return rst.getTime(columnName, cal)
    }

    /**
     * @see java.sql.ResultSet.getTime
     */
    @Override
    @Throws(SQLException::class)
    fun getTime(columnName: String?): Time {
        return rst.getTime(columnName)
    }

    /**
     * @see java.sql.ResultSet.getTimestamp
     */
    @Override
    @Throws(SQLException::class)
    fun getTimestamp(columnIndex: Int, cal: Calendar?): Timestamp {
        return rst.getTimestamp(columnIndex, cal)
    }

    /**
     * @see java.sql.ResultSet.getTimestamp
     */
    @Override
    @Throws(SQLException::class)
    fun getTimestamp(columnIndex: Int): Timestamp {
        return rst.getTimestamp(columnIndex)
    }

    /**
     * @see java.sql.ResultSet.getTimestamp
     */
    @Override
    @Throws(SQLException::class)
    fun getTimestamp(columnName: String?, cal: Calendar?): Timestamp {
        return rst.getTimestamp(columnName, cal)
    }

    /**
     * @see java.sql.ResultSet.getTimestamp
     */
    @Override
    @Throws(SQLException::class)
    fun getTimestamp(columnName: String?): Timestamp {
        return rst.getTimestamp(columnName)
    }

    /**
     * @see java.sql.ResultSet.getType
     */
    @get:Throws(SQLException::class)
    @get:Override
    val type: Int
        get() = rst.getType()

    /**
     * @see java.sql.ResultSet.getUnicodeStream
     */
    @Override
    @SuppressWarnings("deprecation")
    @Throws(SQLException::class)
    fun getUnicodeStream(columnIndex: Int): InputStream {
        return rst.getUnicodeStream(columnIndex)
    }

    /**
     * @see java.sql.ResultSet.getUnicodeStream
     */
    @Override
    @SuppressWarnings("deprecation")
    @Throws(SQLException::class)
    fun getUnicodeStream(columnName: String?): InputStream {
        return rst.getUnicodeStream(columnName)
    }

    /**
     * @see java.sql.ResultSet.getURL
     */
    @Override
    @Throws(SQLException::class)
    fun getURL(columnIndex: Int): URL {
        return rst.getURL(columnIndex)
    }

    /**
     * @see java.sql.ResultSet.getURL
     */
    @Override
    @Throws(SQLException::class)
    fun getURL(columnName: String?): URL {
        return rst.getURL(columnName)
    }

    /**
     * @see java.sql.ResultSet.getWarnings
     */
    @get:Throws(SQLException::class)
    @get:Override
    val warnings: SQLWarning
        get() = rst.getWarnings()

    /**
     * @see java.sql.ResultSet.insertRow
     */
    @Override
    @Throws(SQLException::class)
    fun insertRow() {
        rst.insertRow()
    }

    /**
     * @see java.sql.ResultSet.isAfterLast
     */
    @get:Throws(SQLException::class)
    @get:Override
    val isAfterLast: Boolean
        get() = rst.isAfterLast()

    /**
     * @see java.sql.ResultSet.isBeforeFirst
     */
    @get:Throws(SQLException::class)
    @get:Override
    val isBeforeFirst: Boolean
        get() = rst.isBeforeFirst()

    /**
     * @return is cached
     */
    val isCached: Boolean
        get() = rst.isCached()

    /**
     * @return has records
     */
    val isEmpty: Boolean
        get() = rst.isEmpty()

    /**
     * @see java.sql.ResultSet.isFirst
     */
    @get:Throws(SQLException::class)
    @get:Override
    val isFirst: Boolean
        get() = rst.isFirst()

    /**
     * @see java.sql.ResultSet.isLast
     */
    @get:Throws(SQLException::class)
    @get:Override
    val isLast: Boolean
        get() = rst.isLast()

    /**
     * @return iterator for he keys
     */
    fun keyIterator(): Iterator<Collection.Key> {
        return rst.keyIterator()
    }

    /**
     * @return all keys of the Query
     */
    @SuppressWarnings("deprecation")
    fun keys(): Array<Key> {
        return rst.keys()
    }

    /**
     * @see java.sql.ResultSet.last
     */
    @Override
    @Throws(SQLException::class)
    fun last(): Boolean {
        return rst.last()
    }

    /**
     * @see java.sql.ResultSet.moveToCurrentRow
     */
    @Override
    @Throws(SQLException::class)
    fun moveToCurrentRow() {
        rst.moveToCurrentRow()
    }

    /**
     * @see java.sql.ResultSet.moveToInsertRow
     */
    @Override
    @Throws(SQLException::class)
    fun moveToInsertRow() {
        rst.moveToInsertRow()
    }

    /**
     * @see java.sql.ResultSet.next
     */
    @Override
    @SuppressWarnings("deprecation")
    operator fun next(): Boolean {
        return rst.next()
    }

    /**
     * @see java.sql.ResultSet.previous
     */
    @Override
    @Throws(SQLException::class)
    fun previous(): Boolean {
        return rst.previous()
    }

    /**
     * @see java.sql.ResultSet.refreshRow
     */
    @Override
    @Throws(SQLException::class)
    fun refreshRow() {
        rst.refreshRow()
    }

    /**
     * @see java.sql.ResultSet.relative
     */
    @Override
    @Throws(SQLException::class)
    fun relative(rows: Int): Boolean {
        return rst.relative(rows)
    }

    /**
     * @see java.sql.ResultSet.rowDeleted
     */
    @Override
    @Throws(SQLException::class)
    fun rowDeleted(): Boolean {
        return rst.rowDeleted()
    }

    /**
     * @see java.sql.ResultSet.rowInserted
     */
    @Override
    @Throws(SQLException::class)
    fun rowInserted(): Boolean {
        return rst.rowInserted()
    }

    /**
     * @see java.sql.ResultSet.rowUpdated
     */
    @Override
    @Throws(SQLException::class)
    fun rowUpdated(): Boolean {
        return rst.rowUpdated()
    }

    /**
     * @return the size of the query
     */
    fun size(): Int {
        return rst.size()
    }

    /**
     * @param keyColumn name of the column to sort
     * @param order order type
     * @throws PageException thrown when sorting fails
     */
    @Synchronized
    @Throws(PageException::class)
    fun sort(keyColumn: Key?, order: Int) {
        rst.sort(keyColumn, order)
    }

    /**
     * @param column name of the column to sort
     * @throws PageException thrown when sorting fails
     */
    @Throws(PageException::class)
    fun sort(column: Key?) {
        rst.sort(column)
    }

    /**
     * @param strColumn name of the column to sort
     * @param order order type
     * @throws PageException thrown when sorting fails
     */
    @SuppressWarnings("deprecation")
    @Synchronized
    @Throws(PageException::class)
    fun sort(strColumn: String?, order: Int) {
        rst.sort(strColumn, order)
    }

    /**
     * @param column name of the column to sort
     * @throws PageException thrown when sorting fails
     */
    @SuppressWarnings("deprecation")
    @Throws(PageException::class)
    fun sort(column: String?) {
        rst.sort(column)
    }

    /**
     * @param pageContext page context object
     * @param maxlevel max level shown
     * @param dp property data
     * @return generated DumpData
     */
    fun toDumpData(pageContext: PageContext?, maxlevel: Int, dp: DumpProperties?): DumpData {
        return rst.toDumpData(pageContext, maxlevel, dp)
    }

    /**
     * @see java.lang.Object.toString
     */
    @Override
    override fun toString(): String {
        return rst.toString()
    }

    /**
     * @see java.sql.ResultSet.updateArray
     */
    @Override
    @Throws(SQLException::class)
    fun updateArray(columnIndex: Int, x: Array?) {
        rst.updateArray(columnIndex, x)
    }

    /**
     * @see java.sql.ResultSet.updateArray
     */
    @Override
    @Throws(SQLException::class)
    fun updateArray(columnName: String?, x: Array?) {
        rst.updateArray(columnName, x)
    }

    /**
     * @see java.sql.ResultSet.updateAsciiStream
     */
    @Override
    @Throws(SQLException::class)
    fun updateAsciiStream(columnIndex: Int, x: InputStream?, length: Int) {
        rst.updateAsciiStream(columnIndex, x, length)
    }

    /**
     * @see java.sql.ResultSet.updateAsciiStream
     */
    @Override
    @Throws(SQLException::class)
    fun updateAsciiStream(columnName: String?, x: InputStream?, length: Int) {
        rst.updateAsciiStream(columnName, x, length)
    }

    /**
     * @see java.sql.ResultSet.updateBigDecimal
     */
    @Override
    @Throws(SQLException::class)
    fun updateBigDecimal(columnIndex: Int, x: BigDecimal?) {
        rst.updateBigDecimal(columnIndex, x)
    }

    /**
     * @see java.sql.ResultSet.updateBigDecimal
     */
    @Override
    @Throws(SQLException::class)
    fun updateBigDecimal(columnName: String?, x: BigDecimal?) {
        rst.updateBigDecimal(columnName, x)
    }

    /**
     * @see java.sql.ResultSet.updateBinaryStream
     */
    @Override
    @Throws(SQLException::class)
    fun updateBinaryStream(columnIndex: Int, x: InputStream?, length: Int) {
        rst.updateBinaryStream(columnIndex, x, length)
    }

    /**
     * @see java.sql.ResultSet.updateBinaryStream
     */
    @Override
    @Throws(SQLException::class)
    fun updateBinaryStream(columnName: String?, x: InputStream?, length: Int) {
        rst.updateBinaryStream(columnName, x, length)
    }

    /**
     * @see java.sql.ResultSet.updateBlob
     */
    @Override
    @Throws(SQLException::class)
    fun updateBlob(columnIndex: Int, x: Blob?) {
        rst.updateBlob(columnIndex, x)
    }

    /**
     * @see java.sql.ResultSet.updateBlob
     */
    @Override
    @Throws(SQLException::class)
    fun updateBlob(columnName: String?, x: Blob?) {
        rst.updateBlob(columnName, x)
    }

    /**
     * @see java.sql.ResultSet.updateBoolean
     */
    @Override
    @Throws(SQLException::class)
    fun updateBoolean(columnIndex: Int, x: Boolean) {
        rst.updateBoolean(columnIndex, x)
    }

    /**
     * @see java.sql.ResultSet.updateBoolean
     */
    @Override
    @Throws(SQLException::class)
    fun updateBoolean(columnName: String?, x: Boolean) {
        rst.updateBoolean(columnName, x)
    }

    /**
     * @see java.sql.ResultSet.updateByte
     */
    @Override
    @Throws(SQLException::class)
    fun updateByte(columnIndex: Int, x: Byte) {
        rst.updateByte(columnIndex, x)
    }

    /**
     * @see java.sql.ResultSet.updateByte
     */
    @Override
    @Throws(SQLException::class)
    fun updateByte(columnName: String?, x: Byte) {
        rst.updateByte(columnName, x)
    }

    /**
     * @see java.sql.ResultSet.updateBytes
     */
    @Override
    @Throws(SQLException::class)
    fun updateBytes(columnIndex: Int, x: ByteArray?) {
        rst.updateBytes(columnIndex, x)
    }

    /**
     * @see java.sql.ResultSet.updateBytes
     */
    @Override
    @Throws(SQLException::class)
    fun updateBytes(columnName: String?, x: ByteArray?) {
        rst.updateBytes(columnName, x)
    }

    /**
     * @see java.sql.ResultSet.updateCharacterStream
     */
    @Override
    @Throws(SQLException::class)
    fun updateCharacterStream(columnIndex: Int, reader: Reader?, length: Int) {
        rst.updateCharacterStream(columnIndex, reader, length)
    }

    /**
     * @see java.sql.ResultSet.updateCharacterStream
     */
    @Override
    @Throws(SQLException::class)
    fun updateCharacterStream(columnName: String?, reader: Reader?, length: Int) {
        rst.updateCharacterStream(columnName, reader, length)
    }

    /**
     * @see java.sql.ResultSet.updateClob
     */
    @Override
    @Throws(SQLException::class)
    fun updateClob(columnIndex: Int, x: Clob?) {
        rst.updateClob(columnIndex, x)
    }

    /**
     * @see java.sql.ResultSet.updateClob
     */
    @Override
    @Throws(SQLException::class)
    fun updateClob(columnName: String?, x: Clob?) {
        rst.updateClob(columnName, x)
    }

    /**
     * @see java.sql.ResultSet.updateDate
     */
    @Override
    @Throws(SQLException::class)
    fun updateDate(columnIndex: Int, x: Date?) {
        rst.updateDate(columnIndex, x)
    }

    /**
     * @see java.sql.ResultSet.updateDate
     */
    @Override
    @Throws(SQLException::class)
    fun updateDate(columnName: String?, x: Date?) {
        rst.updateDate(columnName, x)
    }

    /**
     * @see java.sql.ResultSet.updateDouble
     */
    @Override
    @Throws(SQLException::class)
    fun updateDouble(columnIndex: Int, x: Double) {
        rst.updateDouble(columnIndex, x)
    }

    /**
     * @see java.sql.ResultSet.updateDouble
     */
    @Override
    @Throws(SQLException::class)
    fun updateDouble(columnName: String?, x: Double) {
        rst.updateDouble(columnName, x)
    }

    /**
     * @see java.sql.ResultSet.updateFloat
     */
    @Override
    @Throws(SQLException::class)
    fun updateFloat(columnIndex: Int, x: Float) {
        rst.updateFloat(columnIndex, x)
    }

    /**
     * @see java.sql.ResultSet.updateFloat
     */
    @Override
    @Throws(SQLException::class)
    fun updateFloat(columnName: String?, x: Float) {
        rst.updateFloat(columnName, x)
    }

    /**
     * @see java.sql.ResultSet.updateInt
     */
    @Override
    @Throws(SQLException::class)
    fun updateInt(columnIndex: Int, x: Int) {
        rst.updateInt(columnIndex, x)
    }

    /**
     * @see java.sql.ResultSet.updateInt
     */
    @Override
    @Throws(SQLException::class)
    fun updateInt(columnName: String?, x: Int) {
        rst.updateInt(columnName, x)
    }

    /**
     * @see java.sql.ResultSet.updateLong
     */
    @Override
    @Throws(SQLException::class)
    fun updateLong(columnIndex: Int, x: Long) {
        rst.updateLong(columnIndex, x)
    }

    /**
     * @see java.sql.ResultSet.updateLong
     */
    @Override
    @Throws(SQLException::class)
    fun updateLong(columnName: String?, x: Long) {
        rst.updateLong(columnName, x)
    }

    /**
     * @see java.sql.ResultSet.updateNull
     */
    @Override
    @Throws(SQLException::class)
    fun updateNull(columnIndex: Int) {
        rst.updateNull(columnIndex)
    }

    /**
     * @see java.sql.ResultSet.updateNull
     */
    @Override
    @Throws(SQLException::class)
    fun updateNull(columnName: String?) {
        rst.updateNull(columnName)
    }

    /**
     * @see java.sql.ResultSet.updateObject
     */
    @Override
    @Throws(SQLException::class)
    fun updateObject(columnIndex: Int, x: Object?, scale: Int) {
        rst.updateObject(columnIndex, x, scale)
    }

    /**
     * @see java.sql.ResultSet.updateObject
     */
    @Override
    @Throws(SQLException::class)
    fun updateObject(columnIndex: Int, x: Object?) {
        rst.updateObject(columnIndex, x)
    }

    /**
     * @see java.sql.ResultSet.updateObject
     */
    @Override
    @Throws(SQLException::class)
    fun updateObject(columnName: String?, x: Object?, scale: Int) {
        rst.updateObject(columnName, x, scale)
    }

    /**
     * @see java.sql.ResultSet.updateObject
     */
    @Override
    @Throws(SQLException::class)
    fun updateObject(columnName: String?, x: Object?) {
        rst.updateObject(columnName, x)
    }

    /**
     * @see java.sql.ResultSet.updateRef
     */
    @Override
    @Throws(SQLException::class)
    fun updateRef(columnIndex: Int, x: Ref?) {
        rst.updateRef(columnIndex, x)
    }

    /**
     * @see java.sql.ResultSet.updateRef
     */
    @Override
    @Throws(SQLException::class)
    fun updateRef(columnName: String?, x: Ref?) {
        rst.updateRef(columnName, x)
    }

    /**
     * @see java.sql.ResultSet.updateRow
     */
    @Override
    @Throws(SQLException::class)
    fun updateRow() {
        rst.updateRow()
    }

    /**
     * @see java.sql.ResultSet.updateShort
     */
    @Override
    @Throws(SQLException::class)
    fun updateShort(columnIndex: Int, x: Short) {
        rst.updateShort(columnIndex, x)
    }

    /**
     * @see java.sql.ResultSet.updateShort
     */
    @Override
    @Throws(SQLException::class)
    fun updateShort(columnName: String?, x: Short) {
        rst.updateShort(columnName, x)
    }

    /**
     * @see java.sql.ResultSet.updateString
     */
    @Override
    @Throws(SQLException::class)
    fun updateString(columnIndex: Int, x: String?) {
        rst.updateString(columnIndex, x)
    }

    /**
     * @see java.sql.ResultSet.updateString
     */
    @Override
    @Throws(SQLException::class)
    fun updateString(columnName: String?, x: String?) {
        rst.updateString(columnName, x)
    }

    /**
     * @see java.sql.ResultSet.updateTime
     */
    @Override
    @Throws(SQLException::class)
    fun updateTime(columnIndex: Int, x: Time?) {
        rst.updateTime(columnIndex, x)
    }

    /**
     * @see java.sql.ResultSet.updateTime
     */
    @Override
    @Throws(SQLException::class)
    fun updateTime(columnName: String?, x: Time?) {
        rst.updateTime(columnName, x)
    }

    /**
     * @see java.sql.ResultSet.updateTimestamp
     */
    @Override
    @Throws(SQLException::class)
    fun updateTimestamp(columnIndex: Int, x: Timestamp?) {
        rst.updateTimestamp(columnIndex, x)
    }

    /**
     * @see java.sql.ResultSet.updateTimestamp
     */
    @Override
    @Throws(SQLException::class)
    fun updateTimestamp(columnName: String?, x: Timestamp?) {
        rst.updateTimestamp(columnName, x)
    }

    /**
     * @see java.sql.ResultSet.wasNull
     */
    @Override
    @Throws(SQLException::class)
    fun wasNull(): Boolean {
        return rst.wasNull()
    }

    val query: tachyon.runtime.type.Query
        get() = rst

    @get:Throws(SQLException::class)
    @get:Override
    val holdability: Int
        get() {
            throw notSupported()
        }

    @Override
    @Throws(SQLException::class)
    fun getNCharacterStream(arg0: Int): Reader {
        throw notSupported()
    }

    @Override
    @Throws(SQLException::class)
    fun getNCharacterStream(arg0: String?): Reader {
        throw notSupported()
    }

    @Override
    @Throws(SQLException::class)
    fun getNString(arg0: Int): String {
        throw notSupported()
    }

    @Override
    @Throws(SQLException::class)
    fun getNString(arg0: String?): String {
        throw notSupported()
    }

    @get:Throws(SQLException::class)
    @get:Override
    val isClosed: Boolean
        get() {
            throw notSupported()
        }

    @Override
    @Throws(SQLException::class)
    fun updateAsciiStream(arg0: Int, arg1: InputStream?) {
        throw notSupported()
    }

    @Override
    @Throws(SQLException::class)
    fun updateAsciiStream(arg0: String?, arg1: InputStream?) {
        throw notSupported()
    }

    @Override
    @Throws(SQLException::class)
    fun updateAsciiStream(arg0: Int, arg1: InputStream?, arg2: Long) {
        throw notSupported()
    }

    @Override
    @Throws(SQLException::class)
    fun updateAsciiStream(arg0: String?, arg1: InputStream?, arg2: Long) {
        throw notSupported()
    }

    @Override
    @Throws(SQLException::class)
    fun updateBinaryStream(arg0: Int, arg1: InputStream?) {
        throw notSupported()
    }

    @Override
    @Throws(SQLException::class)
    fun updateBinaryStream(arg0: String?, arg1: InputStream?) {
        throw notSupported()
    }

    @Override
    @Throws(SQLException::class)
    fun updateBinaryStream(arg0: Int, arg1: InputStream?, arg2: Long) {
        throw notSupported()
    }

    @Override
    @Throws(SQLException::class)
    fun updateBinaryStream(arg0: String?, arg1: InputStream?, arg2: Long) {
        throw notSupported()
    }

    @Override
    @Throws(SQLException::class)
    fun updateBlob(arg0: Int, arg1: InputStream?) {
        throw notSupported()
    }

    @Override
    @Throws(SQLException::class)
    fun updateBlob(arg0: String?, arg1: InputStream?) {
        throw notSupported()
    }

    @Override
    @Throws(SQLException::class)
    fun updateBlob(arg0: Int, arg1: InputStream?, arg2: Long) {
        throw notSupported()
    }

    @Override
    @Throws(SQLException::class)
    fun updateBlob(arg0: String?, arg1: InputStream?, arg2: Long) {
        throw notSupported()
    }

    @Override
    @Throws(SQLException::class)
    fun updateCharacterStream(arg0: Int, arg1: Reader?) {
        throw notSupported()
    }

    @Override
    @Throws(SQLException::class)
    fun updateCharacterStream(arg0: String?, arg1: Reader?) {
        throw notSupported()
    }

    @Override
    @Throws(SQLException::class)
    fun updateCharacterStream(arg0: Int, arg1: Reader?, arg2: Long) {
        throw notSupported()
    }

    @Override
    @Throws(SQLException::class)
    fun updateCharacterStream(arg0: String?, arg1: Reader?, arg2: Long) {
        throw notSupported()
    }

    @Override
    @Throws(SQLException::class)
    fun updateClob(arg0: Int, arg1: Reader?) {
        throw notSupported()
    }

    @Override
    @Throws(SQLException::class)
    fun updateClob(arg0: String?, arg1: Reader?) {
        throw notSupported()
    }

    @Override
    @Throws(SQLException::class)
    fun updateClob(arg0: Int, arg1: Reader?, arg2: Long) {
        throw notSupported()
    }

    @Override
    @Throws(SQLException::class)
    fun updateClob(arg0: String?, arg1: Reader?, arg2: Long) {
        throw notSupported()
    }

    @Override
    @Throws(SQLException::class)
    fun updateNCharacterStream(arg0: Int, arg1: Reader?) {
        throw notSupported()
    }

    @Override
    @Throws(SQLException::class)
    fun updateNCharacterStream(arg0: String?, arg1: Reader?) {
        throw notSupported()
    }

    @Override
    @Throws(SQLException::class)
    fun updateNCharacterStream(arg0: Int, arg1: Reader?, arg2: Long) {
        throw notSupported()
    }

    @Override
    @Throws(SQLException::class)
    fun updateNCharacterStream(arg0: String?, arg1: Reader?, arg2: Long) {
        throw notSupported()
    }

    @Override
    @Throws(SQLException::class)
    fun updateNClob(arg0: Int, arg1: Reader?) {
        throw notSupported()
    }

    @Override
    @Throws(SQLException::class)
    fun updateNClob(arg0: String?, arg1: Reader?) {
        throw notSupported()
    }

    @Override
    @Throws(SQLException::class)
    fun updateNClob(arg0: Int, arg1: Reader?, arg2: Long) {
        throw notSupported()
    }

    @Override
    @Throws(SQLException::class)
    fun updateNClob(arg0: String?, arg1: Reader?, arg2: Long) {
        throw notSupported()
    }

    @Override
    @Throws(SQLException::class)
    fun updateNString(arg0: Int, arg1: String?) {
        throw notSupported()
    }

    @Override
    @Throws(SQLException::class)
    fun updateNString(arg0: String?, arg1: String?) {
        throw notSupported()
    }

    @Override
    @Throws(SQLException::class)
    fun isWrapperFor(arg0: Class<*>?): Boolean {
        throw notSupported()
    }

    @Override
    @Throws(SQLException::class)
    fun <T> unwrap(arg0: Class<T>?): T {
        throw notSupported()
    }

    // JDK6: uncomment this for compiling with JDK6
    @Override
    @Throws(SQLException::class)
    fun getNClob(arg0: Int): NClob {
        return rst.getNClob(arg0)
    }

    @Override
    @Throws(SQLException::class)
    fun getNClob(arg0: String?): NClob {
        return rst.getNClob(arg0)
    }

    @Override
    @Throws(SQLException::class)
    fun getRowId(arg0: Int): RowId {
        return rst.getRowId(arg0)
    }

    @Override
    @Throws(SQLException::class)
    fun getRowId(arg0: String?): RowId {
        return rst.getRowId(arg0)
    }

    @Override
    @Throws(SQLException::class)
    fun getSQLXML(arg0: Int): SQLXML {
        return rst.getSQLXML(arg0)
    }

    @Override
    @Throws(SQLException::class)
    fun getSQLXML(arg0: String?): SQLXML {
        return rst.getSQLXML(arg0)
    }

    @Override
    @Throws(SQLException::class)
    fun updateNClob(arg0: Int, arg1: NClob?) {
        rst.updateNClob(arg0, arg1)
    }

    @Override
    @Throws(SQLException::class)
    fun updateNClob(arg0: String?, arg1: NClob?) {
        rst.updateNClob(arg0, arg1)
    }

    @Override
    @Throws(SQLException::class)
    fun updateRowId(arg0: Int, arg1: RowId?) {
        rst.updateRowId(arg0, arg1)
    }

    @Override
    @Throws(SQLException::class)
    fun updateRowId(arg0: String?, arg1: RowId?) {
        rst.updateRowId(arg0, arg1)
    }

    @Override
    @Throws(SQLException::class)
    fun updateSQLXML(arg0: Int, arg1: SQLXML?) {
        rst.updateSQLXML(arg0, arg1)
    }

    @Override
    @Throws(SQLException::class)
    fun updateSQLXML(arg0: String?, arg1: SQLXML?) {
        rst.updateSQLXML(arg0, arg1)
    }

    @Override
    @SuppressWarnings("unchecked")
    @Throws(SQLException::class)
    fun <T> getObject(columnIndex: Int, type: Class<T>): T {
        try {
            val m: Method = rst.getClass().getMethod("getObject", arrayOf<Class>(Int::class.javaPrimitiveType, Class::class.java))
            return m.invoke(rst, arrayOf(columnIndex, type))
        } catch (t: Throwable) {
        }
        throw notSupported()
    }

    @Override
    @SuppressWarnings("unchecked")
    @Throws(SQLException::class)
    fun <T> getObject(columnLabel: String, type: Class<T>): T {
        try {
            val m: Method = rst.getClass().getMethod("getObject", arrayOf<Class>(String::class.java, Class::class.java))
            return m.invoke(rst, arrayOf(columnLabel, type))
        } catch (t: Throwable) {
        }
        throw notSupported()
    }

    private fun notSupported(): SQLException {
        return SQLException("this feature is not supported")
    }
}