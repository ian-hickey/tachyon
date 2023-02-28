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
package lucee.runtime.db.driver

import java.io.InputStream

class CallableStatementProxy(conn: ConnectionProxy, prepareCall: CallableStatement, sql: String?) : PreparedStatementProxy(conn, prepareCall, sql), CallableStatement {
    protected override var stat: CallableStatement
    @Override
    @Throws(SQLException::class)
    fun getArray(parameterIndex: Int): Array {
        return stat.getArray(parameterIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun getArray(parameterName: String?): Array {
        return stat.getArray(parameterName)
    }

    @Override
    @Throws(SQLException::class)
    fun getBigDecimal(parameterIndex: Int): BigDecimal {
        return stat.getBigDecimal(parameterIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun getBigDecimal(parameterName: String?): BigDecimal {
        return stat.getBigDecimal(parameterName)
    }

    @Override
    @Throws(SQLException::class)
    fun getBigDecimal(parameterIndex: Int, scale: Int): BigDecimal {
        return stat.getBigDecimal(parameterIndex, scale)
    }

    @Override
    @Throws(SQLException::class)
    fun getBlob(parameterIndex: Int): Blob {
        return stat.getBlob(parameterIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun getBlob(parameterName: String?): Blob {
        return stat.getBlob(parameterName)
    }

    @Override
    @Throws(SQLException::class)
    fun getBoolean(parameterIndex: Int): Boolean {
        return stat.getBoolean(parameterIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun getBoolean(parameterName: String?): Boolean {
        return stat.getBoolean(parameterName)
    }

    @Override
    @Throws(SQLException::class)
    fun getByte(parameterIndex: Int): Byte {
        return stat.getByte(parameterIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun getByte(parameterName: String?): Byte {
        return stat.getByte(parameterName)
    }

    @Override
    @Throws(SQLException::class)
    fun getBytes(parameterIndex: Int): ByteArray {
        return stat.getBytes(parameterIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun getBytes(parameterName: String?): ByteArray {
        return stat.getBytes(parameterName)
    }

    @Override
    @Throws(SQLException::class)
    fun getCharacterStream(parameterIndex: Int): Reader {
        return stat.getCharacterStream(parameterIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun getCharacterStream(parameterName: String?): Reader {
        return stat.getCharacterStream(parameterName)
    }

    @Override
    @Throws(SQLException::class)
    fun getClob(parameterIndex: Int): Clob {
        return stat.getClob(parameterIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun getClob(parameterName: String?): Clob {
        return stat.getClob(parameterName)
    }

    @Override
    @Throws(SQLException::class)
    fun getDate(parameterIndex: Int): Date {
        return stat.getDate(parameterIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun getDate(parameterName: String?): Date {
        return stat.getDate(parameterName)
    }

    @Override
    @Throws(SQLException::class)
    fun getDate(parameterIndex: Int, cal: Calendar?): Date {
        return stat.getDate(parameterIndex, cal)
    }

    @Override
    @Throws(SQLException::class)
    fun getDate(parameterName: String?, cal: Calendar?): Date {
        return stat.getDate(parameterName, cal)
    }

    @Override
    @Throws(SQLException::class)
    fun getDouble(parameterIndex: Int): Double {
        return stat.getDouble(parameterIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun getDouble(parameterName: String?): Double {
        return stat.getDouble(parameterName)
    }

    @Override
    @Throws(SQLException::class)
    fun getFloat(parameterIndex: Int): Float {
        return stat.getFloat(parameterIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun getFloat(parameterName: String?): Float {
        return stat.getFloat(parameterName)
    }

    @Override
    @Throws(SQLException::class)
    fun getInt(parameterIndex: Int): Int {
        return stat.getInt(parameterIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun getInt(parameterName: String?): Int {
        return stat.getInt(parameterName)
    }

    @Override
    @Throws(SQLException::class)
    fun getLong(parameterIndex: Int): Long {
        return stat.getLong(parameterIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun getLong(parameterName: String?): Long {
        return stat.getLong(parameterName)
    }

    @Override
    @Throws(SQLException::class)
    fun getNCharacterStream(parameterIndex: Int): Reader {
        return stat.getNCharacterStream(parameterIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun getNCharacterStream(parameterName: String?): Reader {
        return stat.getNCharacterStream(parameterName)
    }

    @Override
    @Throws(SQLException::class)
    fun getNClob(parameterIndex: Int): NClob {
        return stat.getNClob(parameterIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun getNClob(parameterName: String?): NClob {
        return stat.getNClob(parameterName)
    }

    @Override
    @Throws(SQLException::class)
    fun getNString(parameterIndex: Int): String {
        return stat.getNString(parameterIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun getNString(parameterName: String?): String {
        return stat.getNString(parameterName)
    }

    @Override
    @Throws(SQLException::class)
    fun getObject(parameterIndex: Int): Object {
        return stat.getObject(parameterIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun getObject(parameterName: String?): Object {
        return stat.getObject(parameterName)
    }

    @Override
    @Throws(SQLException::class)
    fun getObject(parameterIndex: Int, map: Map<String?, Class<*>?>?): Object {
        return stat.getObject(parameterIndex, map)
    }

    @Override
    @Throws(SQLException::class)
    fun getObject(parameterName: String?, map: Map<String?, Class<*>?>?): Object {
        return stat.getObject(parameterName, map)
    }

    @Override
    @Throws(SQLException::class)
    fun getRef(parameterIndex: Int): Ref {
        return stat.getRef(parameterIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun getRef(parameterName: String?): Ref {
        return stat.getRef(parameterName)
    }

    @Override
    @Throws(SQLException::class)
    fun getRowId(parameterIndex: Int): RowId {
        return stat.getRowId(parameterIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun getRowId(parameterName: String?): RowId {
        return stat.getRowId(parameterName)
    }

    @Override
    @Throws(SQLException::class)
    fun getSQLXML(parameterIndex: Int): SQLXML {
        return stat.getSQLXML(parameterIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun getSQLXML(parameterName: String?): SQLXML {
        return stat.getSQLXML(parameterName)
    }

    @Override
    @Throws(SQLException::class)
    fun getShort(parameterIndex: Int): Short {
        return stat.getShort(parameterIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun getShort(parameterName: String?): Short {
        return stat.getShort(parameterName)
    }

    @Override
    @Throws(SQLException::class)
    fun getString(parameterIndex: Int): String {
        return stat.getString(parameterIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun getString(parameterName: String?): String {
        return stat.getString(parameterName)
    }

    @Override
    @Throws(SQLException::class)
    fun getTime(parameterIndex: Int): Time {
        return stat.getTime(parameterIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun getTime(parameterName: String?): Time {
        return stat.getTime(parameterName)
    }

    @Override
    @Throws(SQLException::class)
    fun getTime(parameterIndex: Int, cal: Calendar?): Time {
        return stat.getTime(parameterIndex, cal)
    }

    @Override
    @Throws(SQLException::class)
    fun getTime(parameterName: String?, cal: Calendar?): Time {
        return stat.getTime(parameterName, cal)
    }

    @Override
    @Throws(SQLException::class)
    fun getTimestamp(parameterIndex: Int): Timestamp {
        return stat.getTimestamp(parameterIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun getTimestamp(parameterName: String?): Timestamp {
        return stat.getTimestamp(parameterName)
    }

    @Override
    @Throws(SQLException::class)
    fun getTimestamp(parameterIndex: Int, cal: Calendar?): Timestamp {
        return stat.getTimestamp(parameterIndex, cal)
    }

    @Override
    @Throws(SQLException::class)
    fun getTimestamp(parameterName: String?, cal: Calendar?): Timestamp {
        return stat.getTimestamp(parameterName, cal)
    }

    @Override
    @Throws(SQLException::class)
    fun getURL(parameterIndex: Int): URL {
        return stat.getURL(parameterIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun getURL(parameterName: String?): URL {
        return stat.getURL(parameterName)
    }

    @Override
    @Throws(SQLException::class)
    fun registerOutParameter(parameterIndex: Int, sqlType: Int) {
        stat.registerOutParameter(parameterIndex, sqlType)
    }

    @Override
    @Throws(SQLException::class)
    fun registerOutParameter(parameterName: String?, sqlType: Int) {
        stat.registerOutParameter(parameterName, sqlType)
    }

    @Override
    @Throws(SQLException::class)
    fun registerOutParameter(parameterIndex: Int, sqlType: Int, typeName: Int) {
        stat.registerOutParameter(parameterIndex, sqlType, typeName)
    }

    @Override
    @Throws(SQLException::class)
    fun registerOutParameter(parameterIndex: Int, sqlType: Int, typeName: String?) {
        stat.registerOutParameter(parameterIndex, sqlType, typeName)
    }

    @Override
    @Throws(SQLException::class)
    fun registerOutParameter(parameterName: String?, sqlType: Int, scale: Int) {
        stat.registerOutParameter(parameterName, sqlType, scale)
    }

    @Override
    @Throws(SQLException::class)
    fun registerOutParameter(parameterName: String?, sqlType: Int, typeName: String?) {
        stat.registerOutParameter(parameterName, sqlType, typeName)
    }

    @Override
    @Throws(SQLException::class)
    fun setAsciiStream(parameterName: String?, x: InputStream?) {
        stat.setAsciiStream(parameterName, x)
    }

    @Override
    @Throws(SQLException::class)
    fun setAsciiStream(parameterName: String?, x: InputStream?, length: Int) {
        stat.setAsciiStream(parameterName, x, length)
    }

    @Override
    @Throws(SQLException::class)
    fun setAsciiStream(parameterName: String?, x: InputStream?, length: Long) {
        stat.setAsciiStream(parameterName, x, length)
    }

    @Override
    @Throws(SQLException::class)
    fun setBigDecimal(parameterName: String?, x: BigDecimal?) {
        stat.setBigDecimal(parameterName, x)
    }

    @Override
    @Throws(SQLException::class)
    fun setBinaryStream(parameterName: String?, x: InputStream?) {
        stat.setBinaryStream(parameterName, x)
    }

    @Override
    @Throws(SQLException::class)
    fun setBinaryStream(parameterName: String?, x: InputStream?, length: Int) {
        stat.setBinaryStream(parameterName, x, length)
    }

    @Override
    @Throws(SQLException::class)
    fun setBinaryStream(parameterName: String?, x: InputStream?, length: Long) {
        stat.setBinaryStream(parameterName, x, length)
    }

    @Override
    @Throws(SQLException::class)
    fun setBlob(parameterName: String?, x: Blob?) {
        stat.setBlob(parameterName, x)
    }

    @Override
    @Throws(SQLException::class)
    fun setBlob(parameterName: String?, inputStream: InputStream?) {
        stat.setBlob(parameterName, inputStream)
    }

    @Override
    @Throws(SQLException::class)
    fun setBlob(parameterName: String?, `is`: InputStream?, length: Long) {
        stat.setBlob(parameterName, `is`, length)
    }

    @Override
    @Throws(SQLException::class)
    fun setBoolean(parameterName: String?, x: Boolean) {
        stat.setBoolean(parameterName, x)
    }

    @Override
    @Throws(SQLException::class)
    fun setByte(parameterName: String?, x: Byte) {
        stat.setByte(parameterName, x)
    }

    @Override
    @Throws(SQLException::class)
    fun setBytes(parameterName: String?, x: ByteArray?) {
        stat.setBytes(parameterName, x)
    }

    @Override
    @Throws(SQLException::class)
    fun setCharacterStream(parameterName: String?, reader: Reader?) {
        stat.setCharacterStream(parameterName, reader)
    }

    @Override
    @Throws(SQLException::class)
    fun setCharacterStream(parameterName: String?, reader: Reader?, length: Int) {
        stat.setCharacterStream(parameterName, reader, length)
    }

    @Override
    @Throws(SQLException::class)
    fun setCharacterStream(parameterName: String?, reader: Reader?, length: Long) {
        stat.setCharacterStream(parameterName, reader, length)
    }

    @Override
    @Throws(SQLException::class)
    fun setClob(parameterName: String?, x: Clob?) {
        stat.setClob(parameterName, x)
    }

    @Override
    @Throws(SQLException::class)
    fun setClob(parameterName: String?, reader: Reader?) {
        stat.setClob(parameterName, reader)
    }

    @Override
    @Throws(SQLException::class)
    fun setClob(parameterName: String?, reader: Reader?, length: Long) {
        stat.setClob(parameterName, reader, length)
    }

    @Override
    @Throws(SQLException::class)
    fun setDate(parameterName: String?, x: Date?) {
        stat.setDate(parameterName, x)
    }

    @Override
    @Throws(SQLException::class)
    fun setDate(parameterName: String?, x: Date?, cal: Calendar?) {
        stat.setDate(parameterName, x, cal)
    }

    @Override
    @Throws(SQLException::class)
    fun setDouble(parameterName: String?, x: Double) {
        stat.setDouble(parameterName, x)
    }

    @Override
    @Throws(SQLException::class)
    fun setFloat(parameterName: String?, x: Float) {
        stat.setFloat(parameterName, x)
    }

    @Override
    @Throws(SQLException::class)
    fun setInt(parameterName: String?, x: Int) {
        stat.setInt(parameterName, x)
    }

    @Override
    @Throws(SQLException::class)
    fun setLong(parameterName: String?, x: Long) {
        stat.setLong(parameterName, x)
    }

    @Override
    @Throws(SQLException::class)
    fun setNCharacterStream(parameterName: String?, value: Reader?) {
        stat.setNCharacterStream(parameterName, value)
    }

    @Override
    @Throws(SQLException::class)
    fun setNCharacterStream(parameterName: String?, value: Reader?, length: Long) {
        stat.setNCharacterStream(parameterName, value, length)
    }

    @Override
    @Throws(SQLException::class)
    fun setNClob(parameterName: String?, value: NClob?) {
        stat.setNClob(parameterName, value)
    }

    @Override
    @Throws(SQLException::class)
    fun setNClob(parameterName: String?, reader: Reader?) {
        stat.setNClob(parameterName, reader)
    }

    @Override
    @Throws(SQLException::class)
    fun setNClob(parameterName: String?, reader: Reader?, length: Long) {
        stat.setNClob(parameterName, reader, length)
    }

    @Override
    @Throws(SQLException::class)
    fun setNString(parameterName: String?, value: String?) {
        stat.setNString(parameterName, value)
    }

    @Override
    @Throws(SQLException::class)
    fun setNull(parameterName: String?, sqlType: Int) {
        stat.setNull(parameterName, sqlType)
    }

    @Override
    @Throws(SQLException::class)
    fun setNull(parameterName: String?, sqlType: Int, typeName: String?) {
        stat.setNull(parameterName, sqlType, typeName)
    }

    @Override
    @Throws(SQLException::class)
    fun setObject(parameterName: String?, x: Object?) {
        stat.setObject(parameterName, x)
    }

    @Override
    @Throws(SQLException::class)
    fun setObject(parameterName: String?, x: Object?, targetSqlType: Int) {
        stat.setObject(parameterName, x, targetSqlType)
    }

    @Override
    @Throws(SQLException::class)
    fun setObject(parameterName: String?, x: Object?, targetSqlType: Int, scale: Int) {
        stat.setObject(parameterName, x, targetSqlType, scale)
    }

    @Override
    @Throws(SQLException::class)
    fun setRowId(parameterName: String?, x: RowId?) {
        stat.setRowId(parameterName, x)
    }

    @Override
    @Throws(SQLException::class)
    fun setSQLXML(parameterName: String?, xmlObject: SQLXML?) {
        stat.setSQLXML(parameterName, xmlObject)
    }

    @Override
    @Throws(SQLException::class)
    fun setShort(parameterName: String?, x: Short) {
        stat.setShort(parameterName, x)
    }

    @Override
    @Throws(SQLException::class)
    fun setString(parameterName: String?, x: String?) {
        stat.setString(parameterName, x)
    }

    @Override
    @Throws(SQLException::class)
    fun setTime(parameterName: String?, x: Time?) {
        stat.setTime(parameterName, x)
    }

    @Override
    @Throws(SQLException::class)
    fun setTime(parameterName: String?, x: Time?, cal: Calendar?) {
        stat.setTime(parameterName, x, cal)
    }

    @Override
    @Throws(SQLException::class)
    fun setTimestamp(parameterName: String?, x: Timestamp?) {
        stat.setTimestamp(parameterName, x)
    }

    @Override
    @Throws(SQLException::class)
    fun setTimestamp(parameterName: String?, x: Timestamp?, cal: Calendar?) {
        stat.setTimestamp(parameterName, x, cal)
    }

    @Override
    @Throws(SQLException::class)
    fun setURL(parameterName: String?, `val`: URL?) {
        stat.setURL(parameterName, `val`)
    }

    @Override
    @Throws(SQLException::class)
    fun wasNull(): Boolean {
        return stat.wasNull()
    }

    // used only with java 7, do not set @Override
    @Throws(SQLException::class)
    fun <T> getObject(parameterIndex: Int, type: Class<T>): T {
        // used reflection to make sure this work with Java 5 and 6
        return try {
            stat.getClass().getMethod("getObject", arrayOf<Class>(Int::class.javaPrimitiveType, Class::class.java)).invoke(stat, arrayOf(parameterIndex, type))
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            if (t is InvocationTargetException && (t as InvocationTargetException).getTargetException() is SQLException) throw (t as InvocationTargetException).getTargetException() as SQLException
            throw PageRuntimeException(Caster.toPageException(t))
        }
    }

    // used only with java 7, do not set @Override
    @Throws(SQLException::class)
    fun <T> getObject(parameterName: String, type: Class<T>): T {
        // used reflection to make sure this work with Java 5 and 6
        return try {
            stat.getClass().getMethod("getObject", arrayOf<Class>(String::class.java, Class::class.java)).invoke(stat, arrayOf(parameterName, type))
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            if (t is InvocationTargetException && (t as InvocationTargetException).getTargetException() is SQLException) throw (t as InvocationTargetException).getTargetException() as SQLException
            throw PageRuntimeException(Caster.toPageException(t))
        }
    }

    init {
        stat = prepareCall
    }
}