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
package tachyon.runtime.type.trace

import java.io.InputStream

class TOQuery(debugger: Debugger?, qry: Query?, type: Int, category: String?, text: String?) : TOCollection(debugger, qry, type, category, text), Query, com.allaire.cfx.Query {
    private val qry: Query?
    @Override
    fun executionTime(): Int {
        return qry.executionTime()
    }

    @Override
    fun getUpdateCount(): Int {
        return qry.getUpdateCount()
    }

    @Override
    fun getGeneratedKeys(): Query? {
        return qry.getGeneratedKeys()
    }

    @Override
    fun getAt(key: String?, row: Int, defaultValue: Object?): Object? {
        return qry.getAt(key, row, defaultValue)
    }

    @Override
    fun getAt(key: Key?, row: Int, defaultValue: Object?): Object? {
        return qry.getAt(key, row, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun getAt(key: String?, row: Int): Object? {
        return qry.getAt(key, row)
    }

    @Override
    @Throws(PageException::class)
    fun getAt(key: Key?, row: Int): Object? {
        return qry.getAt(key, row)
    }

    @Override
    @Throws(PageException::class)
    fun removeRow(row: Int): Int {
        return qry.removeRow(row)
    }

    @Override
    fun removeRowEL(row: Int): Int {
        return qry.removeRowEL(row)
    }

    @Override
    @Throws(PageException::class)
    fun removeColumn(key: String?): QueryColumn? {
        log(key)
        return qry.removeColumn(key)
    }

    @Override
    @Throws(PageException::class)
    fun removeColumn(key: Key?): QueryColumn? {
        log(key.getString())
        return qry.removeColumn(key)
    }

    @Override
    fun removeColumnEL(key: String?): QueryColumn? {
        log(key)
        return qry.removeColumnEL(key)
    }

    @Override
    fun removeColumnEL(key: Key?): QueryColumn? {
        log(key.getString())
        return qry.removeColumnEL(key)
    }

    @Override
    @Throws(PageException::class)
    fun setAt(key: String?, row: Int, value: Object?): Object? {
        log(key)
        return qry.setAt(key, row, value)
    }

    @Override
    @Throws(PageException::class)
    fun setAt(key: Key?, row: Int, value: Object?): Object? {
        log(key.getString())
        return qry.setAt(key, row, value)
    }

    @Override
    fun setAtEL(key: String?, row: Int, value: Object?): Object? {
        log(key)
        return qry.setAtEL(key, row, value)
    }

    @Override
    fun setAtEL(key: Key?, row: Int, value: Object?): Object? {
        log(key.getString())
        return qry.setAtEL(key, row, value)
    }

    @Override
    operator fun next(): Boolean {
        log()
        return qry.next()
    }

    @Override
    @Throws(PageException::class)
    fun next(pid: Int): Boolean {
        log()
        return qry.next(pid)
    }

    @Override
    @Throws(PageException::class)
    fun reset() {
        log()
        qry.reset()
    }

    @Override
    @Throws(PageException::class)
    fun reset(pid: Int) {
        log()
        qry.reset(pid)
    }

    @Override
    fun getRecordcount(): Int {
        log()
        return qry.getRecordcount()
    }

    @Override
    fun getCurrentrow(pid: Int): Int {
        log()
        return qry.getCurrentrow(pid)
    }

    @Override
    @Throws(PageException::class)
    fun go(index: Int, pid: Int): Boolean {
        log()
        return qry.go(index, pid)
    }

    @Override
    fun isEmpty(): Boolean {
        log()
        return qry.isEmpty()
    }

    @Override
    @Throws(PageException::class)
    fun sort(column: String?) {
        log(column)
        qry.sort(column)
    }

    @Override
    @Throws(PageException::class)
    fun sort(column: Key?) {
        log(column.getString())
        qry.sort(column)
    }

    @Override
    @Throws(PageException::class)
    fun sort(strColumn: String?, order: Int) {
        log(strColumn)
        qry.sort(strColumn, order)
    }

    @Override
    @Throws(PageException::class)
    fun sort(keyColumn: Key?, order: Int) {
        log(keyColumn.getString())
        qry.sort(keyColumn, order)
    }

    @Override
    fun addRow(count: Int): Boolean {
        log("" + count)
        return qry.addRow(count)
    }

    @Override
    @Throws(PageException::class)
    fun addColumn(columnName: String?, content: Array?): Boolean {
        log(columnName)
        return qry.addColumn(columnName, content)
    }

    @Override
    @Throws(PageException::class)
    fun addColumn(columnName: Key?, content: Array?): Boolean {
        log(columnName.getString())
        return qry.addColumn(columnName, content)
    }

    @Override
    @Throws(PageException::class)
    fun addColumn(columnName: String?, content: Array?, type: Int): Boolean {
        log(columnName)
        return qry.addColumn(columnName, content, type)
    }

    @Override
    @Throws(PageException::class)
    fun addColumn(columnName: Key?, content: Array?, type: Int): Boolean {
        log()
        return qry.addColumn(columnName, content, type)
    }

    @Override
    fun getTypes(): IntArray? {
        log()
        return qry.getTypes()
    }

    @Override
    fun getTypesAsMap(): Map? {
        log()
        return qry.getTypesAsMap()
    }

    @Override
    @Throws(PageException::class)
    fun getColumn(key: String?): QueryColumn? {
        log(key)
        return qry.getColumn(key)
    }

    @Override
    @Throws(PageException::class)
    fun getColumn(key: Key?): QueryColumn? {
        log(key.getString())
        return qry.getColumn(key)
    }

    @Override
    @Throws(PageException::class)
    fun rename(columnName: Key?, newColumnName: Key?) {
        log(columnName.toString() + ":" + newColumnName)
        qry.rename(columnName, newColumnName)
    }

    @Override
    fun getColumn(key: String?, defaultValue: QueryColumn?): QueryColumn? {
        log(key)
        return qry.getColumn(key, defaultValue)
    }

    @Override
    fun getColumn(key: Key?, defaultValue: QueryColumn?): QueryColumn? {
        log(key.getString())
        return qry.getColumn(key, defaultValue)
    }

    @Override
    fun setExecutionTime(exeTime: Long) {
        log()
        qry.setExecutionTime(exeTime)
    }

    @Override
    fun setCached(isCached: Boolean) {
        log("" + isCached)
        qry.setCached(isCached)
    }

    @Override
    fun isCached(): Boolean {
        log()
        return qry.isCached()
    }

    @Override
    fun addRow(): Int {
        log()
        return qry.addRow()
    }

    @Override
    fun getColumnIndex(coulmnName: String?): Int {
        log(coulmnName)
        return qry.getColumnIndex(coulmnName)
    }

    @Override
    fun getColumns(): Array<String?>? {
        log()
        return qry.getColumns()
    }

    @Override
    fun getColumnNames(): Array<Key?>? {
        log()
        return qry.getColumnNames()
    }

    @Override
    fun getColumnNamesAsString(): Array<String?>? {
        log()
        return qry.getColumnNamesAsString()
    }

    @Override
    @Throws(IndexOutOfBoundsException::class)
    fun getData(row: Int, col: Int): String? {
        log("$row:$col")
        return qry.getData(row, col)
    }

    @Override
    fun getName(): String? {
        log()
        return qry.getName()
    }

    @Override
    fun getRowCount(): Int {
        log()
        return qry.getRowCount()
    }

    @Override
    @Throws(IndexOutOfBoundsException::class)
    fun setData(row: Int, col: Int, value: String?) {
        log("" + row)
        qry.setData(row, col, value)
    }

    @Override
    fun getMetaDataSimple(): Array? {
        log()
        return qry.getMetaDataSimple()
    }

    @Override
    @Throws(SQLException::class)
    fun getObject(columnName: String?): Object? {
        log(columnName)
        return qry.getObject(columnName)
    }

    @Override
    @Throws(SQLException::class)
    fun getObject(columnIndex: Int): Object? {
        log("" + columnIndex)
        return qry.getObject(columnIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun getString(columnIndex: Int): String? {
        log("" + columnIndex)
        return qry.getString(columnIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun getString(columnName: String?): String? {
        log(columnName)
        return qry.getString(columnName)
    }

    @Override
    @Throws(SQLException::class)
    fun getBoolean(columnIndex: Int): Boolean {
        log("" + columnIndex)
        return qry.getBoolean(columnIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun getBoolean(columnName: String?): Boolean {
        log(columnName)
        return qry.getBoolean(columnName)
    }

    @Override
    @Throws(SQLException::class)
    fun wasNull(): Boolean {
        log()
        return qry.wasNull()
    }

    @Override
    @Throws(SQLException::class)
    fun absolute(row: Int): Boolean {
        log()
        return qry.absolute(row)
    }

    @Override
    @Throws(SQLException::class)
    fun afterLast() {
        log()
        qry.afterLast()
    }

    @Override
    @Throws(SQLException::class)
    fun beforeFirst() {
        log()
        qry.beforeFirst()
    }

    @Override
    @Throws(SQLException::class)
    fun cancelRowUpdates() {
        log()
        qry.cancelRowUpdates()
    }

    @Override
    @Throws(SQLException::class)
    fun clearWarnings() {
        log()
        qry.clearWarnings()
    }

    @Override
    @Throws(SQLException::class)
    fun close() {
        log()
        qry.close()
    }

    @Override
    @Throws(SQLException::class)
    fun deleteRow() {
        log()
        qry.deleteRow()
    }

    @Override
    @Throws(SQLException::class)
    fun findColumn(columnName: String?): Int {
        log()
        return qry.findColumn(columnName)
    }

    @Override
    @Throws(SQLException::class)
    fun first(): Boolean {
        log()
        return qry.first()
    }

    @Override
    @Throws(SQLException::class)
    fun getArray(i: Int): Array? {
        log("" + i)
        return qry.getArray(i)
    }

    @Override
    @Throws(SQLException::class)
    fun getArray(colName: String?): Array? {
        log(colName)
        return qry.getArray(colName)
    }

    @Override
    @Throws(SQLException::class)
    fun getAsciiStream(columnIndex: Int): InputStream? {
        log("" + columnIndex)
        return qry.getAsciiStream(columnIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun getAsciiStream(columnName: String?): InputStream? {
        log(columnName)
        return qry.getAsciiStream(columnName)
    }

    @Override
    @Throws(SQLException::class)
    fun getBigDecimal(columnIndex: Int): BigDecimal? {
        log("" + columnIndex)
        return qry.getBigDecimal(columnIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun getBigDecimal(columnName: String?): BigDecimal? {
        log(columnName)
        return qry.getBigDecimal(columnName)
    }

    @Override
    @Throws(SQLException::class)
    fun getBigDecimal(columnIndex: Int, scale: Int): BigDecimal? {
        log("" + columnIndex)
        return qry.getBigDecimal(columnIndex, scale)
    }

    @Override
    @Throws(SQLException::class)
    fun getBigDecimal(columnName: String?, scale: Int): BigDecimal? {
        log(columnName)
        return qry.getBigDecimal(columnName, scale)
    }

    @Override
    @Throws(SQLException::class)
    fun getBinaryStream(columnIndex: Int): InputStream? {
        log("" + columnIndex)
        return qry.getBinaryStream(columnIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun getBinaryStream(columnName: String?): InputStream? {
        log(columnName)
        return qry.getBinaryStream(columnName)
    }

    @Override
    @Throws(SQLException::class)
    fun getBlob(i: Int): Blob? {
        log("" + i)
        return qry.getBlob(i)
    }

    @Override
    @Throws(SQLException::class)
    fun getBlob(colName: String?): Blob? {
        log(colName)
        return qry.getBlob(colName)
    }

    @Override
    @Throws(SQLException::class)
    fun getByte(columnIndex: Int): Byte {
        log("" + columnIndex)
        return qry.getByte(columnIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun getByte(columnName: String?): Byte {
        log("" + columnName)
        return qry.getByte(columnName)
    }

    @Override
    @Throws(SQLException::class)
    fun getBytes(columnIndex: Int): ByteArray? {
        log("" + columnIndex)
        return qry.getBytes(columnIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun getBytes(columnName: String?): ByteArray? {
        log(columnName)
        return qry.getBytes(columnName)
    }

    @Override
    @Throws(SQLException::class)
    fun getCharacterStream(columnIndex: Int): Reader? {
        log("" + columnIndex)
        return qry.getCharacterStream(columnIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun getCharacterStream(columnName: String?): Reader? {
        log(columnName)
        return qry.getCharacterStream(columnName)
    }

    @Override
    @Throws(SQLException::class)
    fun getClob(i: Int): Clob? {
        log("" + i)
        return qry.getClob(i)
    }

    @Override
    @Throws(SQLException::class)
    fun getClob(colName: String?): Clob? {
        log(colName)
        return qry.getClob(colName)
    }

    @Override
    @Throws(SQLException::class)
    fun getConcurrency(): Int {
        log()
        return qry.getConcurrency()
    }

    @Override
    @Throws(SQLException::class)
    fun getCursorName(): String? {
        log()
        return qry.getCursorName()
    }

    @Override
    @Throws(SQLException::class)
    fun getDate(columnIndex: Int): Date? {
        log("" + columnIndex)
        return qry.getDate(columnIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun getDate(columnName: String?): Date? {
        log(columnName)
        return qry.getDate(columnName)
    }

    @Override
    @Throws(SQLException::class)
    fun getDate(columnIndex: Int, cal: Calendar?): Date? {
        log(columnIndex.toString() + "")
        return qry.getDate(columnIndex, cal)
    }

    @Override
    @Throws(SQLException::class)
    fun getDate(columnName: String?, cal: Calendar?): Date? {
        log(columnName)
        return qry.getDate(columnName, cal)
    }

    @Override
    @Throws(SQLException::class)
    fun getDouble(columnIndex: Int): Double {
        log("" + columnIndex)
        return qry.getDouble(columnIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun getDouble(columnName: String?): Double {
        log(columnName)
        return qry.getDouble(columnName)
    }

    @Override
    @Throws(SQLException::class)
    fun getFetchDirection(): Int {
        log()
        return qry.getFetchDirection()
    }

    @Override
    @Throws(SQLException::class)
    fun getFetchSize(): Int {
        log()
        return qry.getFetchSize()
    }

    @Override
    @Throws(SQLException::class)
    fun getFloat(columnIndex: Int): Float {
        log(columnIndex.toString() + "")
        return qry.getFloat(columnIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun getFloat(columnName: String?): Float {
        log(columnName)
        return qry.getFloat(columnName)
    }

    @Override
    @Throws(SQLException::class)
    fun getInt(columnIndex: Int): Int {
        log("" + columnIndex)
        return qry.getInt(columnIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun getInt(columnName: String?): Int {
        log(columnName)
        return qry.getInt(columnName)
    }

    @Override
    @Throws(SQLException::class)
    fun getLong(columnIndex: Int): Long {
        log("" + columnIndex)
        return qry.getLong(columnIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun getLong(columnName: String?): Long {
        log(columnName)
        return qry.getLong(columnName)
    }

    @Override
    @Throws(SQLException::class)
    fun getRef(i: Int): Ref? {
        log("" + i)
        return qry.getRef(i)
    }

    @Override
    @Throws(SQLException::class)
    fun getRef(colName: String?): Ref? {
        log(colName)
        return qry.getRef(colName)
    }

    @Override
    @Throws(SQLException::class)
    fun getRow(): Int {
        log()
        return qry.getRow()
    }

    @Override
    @Throws(SQLException::class)
    fun getShort(columnIndex: Int): Short {
        log("" + columnIndex)
        return qry.getShort(columnIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun getShort(columnName: String?): Short {
        log(columnName)
        return qry.getShort(columnName)
    }

    @Override
    @Throws(SQLException::class)
    fun getStatement(): Statement? {
        log()
        return qry.getStatement()
    }

    @Override
    @Throws(SQLException::class)
    fun getTime(columnIndex: Int): Time? {
        log("" + columnIndex)
        return qry.getTime(columnIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun getTime(columnName: String?): Time? {
        log(columnName)
        return qry.getTime(columnName)
    }

    @Override
    @Throws(SQLException::class)
    fun getTime(columnIndex: Int, cal: Calendar?): Time? {
        log("" + columnIndex)
        return qry.getTime(columnIndex, cal)
    }

    @Override
    @Throws(SQLException::class)
    fun getTime(columnName: String?, cal: Calendar?): Time? {
        log(columnName)
        return qry.getTime(columnName, cal)
    }

    @Override
    @Throws(SQLException::class)
    fun getTimestamp(columnIndex: Int): Timestamp? {
        log("" + columnIndex)
        return qry.getTimestamp(columnIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun getTimestamp(columnName: String?): Timestamp? {
        log(columnName)
        return qry.getTimestamp(columnName)
    }

    @Override
    @Throws(SQLException::class)
    fun getTimestamp(columnIndex: Int, cal: Calendar?): Timestamp? {
        log("" + columnIndex)
        return qry.getTimestamp(columnIndex, cal)
    }

    @Override
    @Throws(SQLException::class)
    fun getTimestamp(columnName: String?, cal: Calendar?): Timestamp? {
        log(columnName)
        return qry.getTimestamp(columnName, cal)
    }

    @Override
    @Throws(SQLException::class)
    fun getType(): Int {
        log()
        return qry.getType()
    }

    @Override
    @Throws(SQLException::class)
    fun getURL(columnIndex: Int): URL? {
        log()
        return qry.getURL(columnIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun getURL(columnName: String?): URL? {
        log()
        return qry.getURL(columnName)
    }

    @Override
    @Throws(SQLException::class)
    fun getUnicodeStream(columnIndex: Int): InputStream? {
        log()
        return qry.getUnicodeStream(columnIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun getUnicodeStream(columnName: String?): InputStream? {
        log()
        return qry.getUnicodeStream(columnName)
    }

    @Override
    @Throws(SQLException::class)
    fun getWarnings(): SQLWarning? {
        log()
        return qry.getWarnings()
    }

    @Override
    @Throws(SQLException::class)
    fun insertRow() {
        log()
        qry.insertRow()
    }

    @Override
    @Throws(SQLException::class)
    fun isAfterLast(): Boolean {
        log()
        return qry.isAfterLast()
    }

    @Override
    @Throws(SQLException::class)
    fun isBeforeFirst(): Boolean {
        log()
        return qry.isBeforeFirst()
    }

    @Override
    @Throws(SQLException::class)
    fun isFirst(): Boolean {
        log()
        return qry.isFirst()
    }

    @Override
    @Throws(SQLException::class)
    fun isLast(): Boolean {
        log()
        return qry.isLast()
    }

    @Override
    @Throws(SQLException::class)
    fun last(): Boolean {
        log()
        return qry.last()
    }

    @Override
    @Throws(SQLException::class)
    fun moveToCurrentRow() {
        log()
        qry.moveToCurrentRow()
    }

    @Override
    @Throws(SQLException::class)
    fun moveToInsertRow() {
        log()
        qry.moveToInsertRow()
    }

    @Override
    @Throws(SQLException::class)
    fun previous(): Boolean {
        log()
        return qry.previous()
    }

    @Override
    fun previous(pid: Int): Boolean {
        log()
        return qry.previous(pid)
    }

    @Override
    @Throws(SQLException::class)
    fun refreshRow() {
        log()
        qry.refreshRow()
    }

    @Override
    @Throws(SQLException::class)
    fun relative(rows: Int): Boolean {
        log()
        return qry.relative(rows)
    }

    @Override
    @Throws(SQLException::class)
    fun rowDeleted(): Boolean {
        log()
        return qry.rowDeleted()
    }

    @Override
    @Throws(SQLException::class)
    fun rowInserted(): Boolean {
        log()
        return qry.rowInserted()
    }

    @Override
    @Throws(SQLException::class)
    fun rowUpdated(): Boolean {
        log()
        return qry.rowUpdated()
    }

    @Override
    @Throws(SQLException::class)
    fun setFetchDirection(direction: Int) {
        log()
        qry.setFetchDirection(direction)
    }

    @Override
    @Throws(SQLException::class)
    fun setFetchSize(rows: Int) {
        log("" + rows)
        qry.setFetchSize(rows)
    }

    @Override
    @Throws(SQLException::class)
    fun updateArray(columnIndex: Int, x: Array?) {
        log(columnIndex.toString() + "")
        qry.updateArray(columnIndex, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateArray(columnName: String?, x: Array?) {
        log(columnName)
        qry.updateArray(columnName, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateAsciiStream(columnIndex: Int, x: InputStream?, length: Int) {
        log("" + columnIndex)
        qry.updateAsciiStream(columnIndex, x, length)
    }

    @Override
    @Throws(SQLException::class)
    fun updateAsciiStream(columnName: String?, x: InputStream?, length: Int) {
        log(columnName)
        qry.updateAsciiStream(columnName, x, length)
    }

    @Override
    @Throws(SQLException::class)
    fun updateBigDecimal(columnIndex: Int, x: BigDecimal?) {
        log("" + columnIndex)
        qry.updateBigDecimal(columnIndex, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateBigDecimal(columnName: String?, x: BigDecimal?) {
        log(columnName)
        qry.updateBigDecimal(columnName, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateBinaryStream(columnIndex: Int, x: InputStream?, length: Int) {
        log("" + columnIndex)
        qry.updateBinaryStream(columnIndex, x, length)
    }

    @Override
    @Throws(SQLException::class)
    fun updateBinaryStream(columnName: String?, x: InputStream?, length: Int) {
        log(columnName)
        qry.updateBinaryStream(columnName, x, length)
    }

    @Override
    @Throws(SQLException::class)
    fun updateBlob(columnIndex: Int, x: Blob?) {
        log("" + columnIndex)
        qry.updateBlob(columnIndex, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateBlob(columnName: String?, x: Blob?) {
        log(columnName)
        qry.updateBlob(columnName, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateBoolean(columnIndex: Int, x: Boolean) {
        log("" + columnIndex)
        qry.updateBoolean(columnIndex, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateBoolean(columnName: String?, x: Boolean) {
        log(columnName)
        qry.updateBoolean(columnName, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateByte(columnIndex: Int, x: Byte) {
        log("" + columnIndex)
        qry.updateByte(columnIndex, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateByte(columnName: String?, x: Byte) {
        log(columnName)
        qry.updateByte(columnName, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateBytes(columnIndex: Int, x: ByteArray?) {
        log("" + columnIndex)
        qry.updateBytes(columnIndex, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateBytes(columnName: String?, x: ByteArray?) {
        log(columnName)
        qry.updateBytes(columnName, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateCharacterStream(columnIndex: Int, reader: Reader?, length: Int) {
        log("" + columnIndex)
        qry.updateCharacterStream(columnIndex, reader, length)
    }

    @Override
    @Throws(SQLException::class)
    fun updateCharacterStream(columnName: String?, reader: Reader?, length: Int) {
        log(columnName)
        qry.updateCharacterStream(columnName, reader, length)
    }

    @Override
    @Throws(SQLException::class)
    fun updateClob(columnIndex: Int, x: Clob?) {
        log("" + columnIndex)
        qry.updateClob(columnIndex, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateClob(columnName: String?, x: Clob?) {
        log(columnName)
        qry.updateClob(columnName, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateDate(columnIndex: Int, x: Date?) {
        log("" + columnIndex)
        qry.updateDate(columnIndex, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateDate(columnName: String?, x: Date?) {
        log(columnName)
        qry.updateDate(columnName, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateDouble(columnIndex: Int, x: Double) {
        log("" + columnIndex)
        qry.updateDouble(columnIndex, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateDouble(columnName: String?, x: Double) {
        log(columnName)
        qry.updateDouble(columnName, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateFloat(columnIndex: Int, x: Float) {
        log("" + columnIndex)
        qry.updateFloat(columnIndex, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateFloat(columnName: String?, x: Float) {
        log(columnName)
        qry.updateFloat(columnName, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateInt(columnIndex: Int, x: Int) {
        log("" + columnIndex)
        qry.updateInt(columnIndex, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateInt(columnName: String?, x: Int) {
        log(columnName)
        qry.updateInt(columnName, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateLong(columnIndex: Int, x: Long) {
        log("" + columnIndex)
        qry.updateLong(columnIndex, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateLong(columnName: String?, x: Long) {
        log(columnName)
        qry.updateLong(columnName, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateNull(columnIndex: Int) {
        log("" + columnIndex)
        qry.updateNull(columnIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun updateNull(columnName: String?) {
        log(columnName)
        qry.updateNull(columnName)
    }

    @Override
    @Throws(SQLException::class)
    fun updateObject(columnIndex: Int, x: Object?) {
        qry.updateObject(columnIndex, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateObject(columnName: String?, x: Object?) {
        log(columnName)
        qry.updateObject(columnName, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateObject(columnIndex: Int, x: Object?, scale: Int) {
        log("" + columnIndex)
        qry.updateObject(columnIndex, x, scale)
    }

    @Override
    @Throws(SQLException::class)
    fun updateObject(columnName: String?, x: Object?, scale: Int) {
        log(columnName)
        qry.updateObject(columnName, x, scale)
    }

    @Override
    @Throws(SQLException::class)
    fun updateRef(columnIndex: Int, x: Ref?) {
        log("" + columnIndex)
        qry.updateRef(columnIndex, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateRef(columnName: String?, x: Ref?) {
        log(columnName)
        qry.updateRef(columnName, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateRow() {
        log()
        qry.updateRow()
    }

    @Override
    @Throws(SQLException::class)
    fun updateShort(columnIndex: Int, x: Short) {
        log("" + columnIndex)
        qry.updateShort(columnIndex, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateShort(columnName: String?, x: Short) {
        log(columnName)
        qry.updateShort(columnName, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateString(columnIndex: Int, x: String?) {
        log("" + columnIndex)
        qry.updateString(columnIndex, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateString(columnName: String?, x: String?) {
        log(columnName)
        qry.updateString(columnName, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateTime(columnIndex: Int, x: Time?) {
        log("" + columnIndex)
        qry.updateTime(columnIndex, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateTime(columnName: String?, x: Time?) {
        log(columnName)
        qry.updateTime(columnName, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateTimestamp(columnIndex: Int, x: Timestamp?) {
        log("" + columnIndex)
        qry.updateTimestamp(columnIndex, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateTimestamp(columnName: String?, x: Timestamp?) {
        log(columnName)
        qry.updateTimestamp(columnName, x)
    }

    @Override
    @Throws(SQLException::class)
    fun getMetaData(): ResultSetMetaData? {
        log()
        return qry.getMetaData()
    }

    @Override
    @Throws(SQLException::class)
    fun getHoldability(): Int {
        log()
        return qry.getHoldability()
    }

    @Override
    @Throws(SQLException::class)
    fun isClosed(): Boolean {
        log()
        return qry.isClosed()
    }

    @Override
    @Throws(SQLException::class)
    fun updateNString(columnIndex: Int, nString: String?) {
        log("" + columnIndex)
        qry.updateNString(columnIndex, nString)
    }

    @Override
    @Throws(SQLException::class)
    fun updateNString(columnLabel: String?, nString: String?) {
        log(columnLabel)
        qry.updateNString(columnLabel, nString)
    }

    @Override
    @Throws(SQLException::class)
    fun getNString(columnIndex: Int): String? {
        log("" + columnIndex)
        return qry.getNString(columnIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun getNString(columnLabel: String?): String? {
        log(columnLabel)
        return qry.getNString(columnLabel)
    }

    @Override
    @Throws(SQLException::class)
    fun getNCharacterStream(columnIndex: Int): Reader? {
        log("" + columnIndex)
        return qry.getNCharacterStream(columnIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun getNCharacterStream(columnLabel: String?): Reader? {
        log(columnLabel)
        return qry.getNCharacterStream(columnLabel)
    }

    @Override
    @Throws(SQLException::class)
    fun updateNCharacterStream(columnIndex: Int, x: Reader?, length: Long) {
        log("" + columnIndex)
        qry.updateNCharacterStream(columnIndex, x, length)
    }

    @Override
    @Throws(SQLException::class)
    fun updateNCharacterStream(columnLabel: String?, reader: Reader?, length: Long) {
        log(columnLabel)
        qry.updateNCharacterStream(columnLabel, reader, length)
    }

    @Override
    @Throws(SQLException::class)
    fun updateAsciiStream(columnIndex: Int, x: InputStream?, length: Long) {
        log("" + columnIndex)
        qry.updateAsciiStream(columnIndex, x, length)
    }

    @Override
    @Throws(SQLException::class)
    fun updateBinaryStream(columnIndex: Int, x: InputStream?, length: Long) {
        log(columnIndex.toString() + "")
        qry.updateBinaryStream(columnIndex, x, length)
    }

    @Override
    @Throws(SQLException::class)
    fun updateCharacterStream(columnIndex: Int, x: Reader?, length: Long) {
        log(columnIndex.toString() + "")
        qry.updateCharacterStream(columnIndex, x, length)
    }

    @Override
    @Throws(SQLException::class)
    fun updateAsciiStream(columnLabel: String?, x: InputStream?, length: Long) {
        log(columnLabel)
        qry.updateAsciiStream(columnLabel, x, length)
    }

    @Override
    @Throws(SQLException::class)
    fun updateBinaryStream(columnLabel: String?, x: InputStream?, length: Long) {
        log(columnLabel)
        qry.updateBinaryStream(columnLabel, x, length)
    }

    @Override
    @Throws(SQLException::class)
    fun updateCharacterStream(columnLabel: String?, reader: Reader?, length: Long) {
        log(columnLabel)
        qry.updateCharacterStream(columnLabel, reader, length)
    }

    @Override
    @Throws(SQLException::class)
    fun updateBlob(columnIndex: Int, inputStream: InputStream?, length: Long) {
        log("" + columnIndex)
        qry.updateBlob(columnIndex, inputStream, length)
    }

    @Override
    @Throws(SQLException::class)
    fun updateBlob(columnLabel: String?, inputStream: InputStream?, length: Long) {
        log(columnLabel)
        qry.updateBlob(columnLabel, inputStream, length)
    }

    @Override
    @Throws(SQLException::class)
    fun updateClob(columnIndex: Int, reader: Reader?, length: Long) {
        log("" + columnIndex)
        qry.updateClob(columnIndex, reader, length)
    }

    @Override
    @Throws(SQLException::class)
    fun updateClob(columnLabel: String?, reader: Reader?, length: Long) {
        log(columnLabel)
        qry.updateClob(columnLabel, reader, length)
    }

    @Override
    @Throws(SQLException::class)
    fun updateNClob(columnIndex: Int, reader: Reader?, length: Long) {
        log("" + columnIndex)
        qry.updateNClob(columnIndex, reader, length)
    }

    @Override
    @Throws(SQLException::class)
    fun updateNClob(columnLabel: String?, reader: Reader?, length: Long) {
        log(columnLabel)
        qry.updateNClob(columnLabel, reader, length)
    }

    @Override
    @Throws(SQLException::class)
    fun updateNCharacterStream(columnIndex: Int, x: Reader?) {
        log("" + columnIndex)
        qry.updateNCharacterStream(columnIndex, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateNCharacterStream(columnLabel: String?, reader: Reader?) {
        log(columnLabel)
        qry.updateNCharacterStream(columnLabel, reader)
    }

    @Override
    @Throws(SQLException::class)
    fun updateAsciiStream(columnIndex: Int, x: InputStream?) {
        log("" + columnIndex)
        qry.updateAsciiStream(columnIndex, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateBinaryStream(columnIndex: Int, x: InputStream?) {
        log("" + columnIndex)
        qry.updateBinaryStream(columnIndex, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateCharacterStream(columnIndex: Int, x: Reader?) {
        log("" + columnIndex)
        qry.updateCharacterStream(columnIndex, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateAsciiStream(columnLabel: String?, x: InputStream?) {
        log(columnLabel)
        qry.updateAsciiStream(columnLabel, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateBinaryStream(columnLabel: String?, x: InputStream?) {
        log(columnLabel)
        qry.updateBinaryStream(columnLabel, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateCharacterStream(columnLabel: String?, reader: Reader?) {
        log(columnLabel)
        qry.updateCharacterStream(columnLabel, reader)
    }

    @Override
    @Throws(SQLException::class)
    fun updateBlob(columnIndex: Int, inputStream: InputStream?) {
        log("" + columnIndex)
        qry.updateBlob(columnIndex, inputStream)
    }

    @Override
    @Throws(SQLException::class)
    fun updateBlob(columnLabel: String?, inputStream: InputStream?) {
        log(columnLabel)
        qry.updateBlob(columnLabel, inputStream)
    }

    @Override
    @Throws(SQLException::class)
    fun updateClob(columnIndex: Int, reader: Reader?) {
        log("" + columnIndex)
        qry.updateClob(columnIndex, reader)
    }

    @Override
    @Throws(SQLException::class)
    fun updateClob(columnLabel: String?, reader: Reader?) {
        log(columnLabel)
        qry.updateClob(columnLabel, reader)
    }

    @Override
    @Throws(SQLException::class)
    fun updateNClob(columnIndex: Int, reader: Reader?) {
        log("" + columnIndex)
        qry.updateNClob(columnIndex, reader)
    }

    @Override
    @Throws(SQLException::class)
    fun updateNClob(columnLabel: String?, reader: Reader?) {
        log(columnLabel)
        qry.updateNClob(columnLabel, reader)
    }

    @Override
    @Throws(SQLException::class)
    fun <T> unwrap(iface: Class<T?>?): T? {
        log()
        return qry.unwrap(iface)
    }

    @Override
    @Throws(SQLException::class)
    fun isWrapperFor(iface: Class<*>?): Boolean {
        log()
        return qry.isWrapperFor(iface)
    }

    @Override
    fun duplicate(deepCopy: Boolean): Collection? {
        log()
        return TOQuery(debugger, Duplicator.duplicate(qry, deepCopy) as Query, type, category, text)
    }

    @Override
    @Throws(SQLException::class)
    fun getNClob(arg0: Int): NClob? {
        log("" + arg0)
        return qry.getNClob(arg0)
    }

    @Override
    @Throws(SQLException::class)
    fun getNClob(arg0: String?): NClob? {
        log(arg0)
        return qry.getNClob(arg0)
    }

    @Override
    @Throws(SQLException::class)
    fun getObject(arg0: Int, arg1: Map<String?, Class<*>?>?): Object? {
        log("" + arg0)
        return qry.getObject(arg0, arg1)
    }

    @Override
    @Throws(SQLException::class)
    fun getObject(arg0: String?, arg1: Map<String?, Class<*>?>?): Object? {
        log(arg0)
        return qry.getObject(arg0, arg1)
    }

    // used only with java 7, do not set @Override
    @Override
    @Throws(SQLException::class)
    fun <T> getObject(columnIndex: Int, type: Class<T?>?): T? {
        return QueryUtil.getObject(this, columnIndex, type)
    }

    // used only with java 7, do not set @Override
    @Override
    @Throws(SQLException::class)
    fun <T> getObject(columnLabel: String?, type: Class<T?>?): T? {
        return QueryUtil.getObject(this, columnLabel, type)
    }

    @Override
    @Throws(SQLException::class)
    fun getRowId(arg0: Int): RowId? {
        log("" + arg0)
        return qry.getRowId(arg0)
    }

    @Override
    @Throws(SQLException::class)
    fun getRowId(arg0: String?): RowId? {
        log(arg0)
        return qry.getRowId(arg0)
    }

    @Override
    @Throws(SQLException::class)
    fun getSQLXML(arg0: Int): SQLXML? {
        log("" + arg0)
        return qry.getSQLXML(arg0)
    }

    @Override
    @Throws(SQLException::class)
    fun getSQLXML(arg0: String?): SQLXML? {
        log(arg0)
        return qry.getSQLXML(arg0)
    }

    @Override
    @Throws(SQLException::class)
    fun updateNClob(arg0: Int, arg1: NClob?) {
        log("" + arg0)
        qry.updateNClob(arg0, arg1)
    }

    @Override
    @Throws(SQLException::class)
    fun updateNClob(arg0: String?, arg1: NClob?) {
        log(arg0)
        qry.updateNClob(arg0, arg1)
    }

    @Override
    @Throws(SQLException::class)
    fun updateRowId(arg0: Int, arg1: RowId?) {
        log("" + arg0)
        qry.updateRowId(arg0, arg1)
    }

    @Override
    @Throws(SQLException::class)
    fun updateRowId(arg0: String?, arg1: RowId?) {
        log(arg0)
        qry.updateRowId(arg0, arg1)
    }

    @Override
    @Throws(SQLException::class)
    fun updateSQLXML(arg0: Int, arg1: SQLXML?) {
        log(arg0.toString() + "")
        qry.updateSQLXML(arg0, arg1)
    }

    @Override
    @Throws(SQLException::class)
    fun updateSQLXML(columnIndex: String?, x: SQLXML?) {
        log(columnIndex)
        qry.updateSQLXML(columnIndex, x)
    }

    @Override
    fun getSql(): SQL? {
        log()
        return qry.getSql()
    }

    @Override
    fun getTemplate(): String? {
        log()
        return qry.getTemplate()
    }

    @Override
    fun getExecutionTime(): Long {
        log()
        return qry.getExecutionTime()
    }

    @Override
    fun getIterator(): Iterator<*>? {
        log()
        return ForEachQueryIterator(null, this, ThreadLocalPageContext.get().getId())
    }

    @Override
    fun getCacheType(): String? {
        log()
        return qry.getCacheType()
    }

    @Override
    fun setCacheType(cacheType: String?) {
        log(cacheType)
        qry.setCacheType(cacheType)
    }

    @Override
    fun getColumnCount(): Int {
        log()
        return qry.getColumnCount()
    }

    @Override
    fun enableShowQueryUsage() {
        log()
        qry.enableShowQueryUsage()
    }

    init {
        this.qry = qry
    }
}