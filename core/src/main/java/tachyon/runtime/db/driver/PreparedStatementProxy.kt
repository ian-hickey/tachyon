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
package tachyon.runtime.db.driver

import java.io.InputStream

class PreparedStatementProxy(conn: ConnectionProxy, stat: PreparedStatement, sql: String?) : StatementProxy(conn, stat), PreparedStatementPro {
    protected override var stat: PreparedStatement
    var sQL: String?
        protected set

    @Override
    @Throws(SQLException::class)
    fun execute(): Boolean {
        return stat.execute()
    }

    @Override
    @Throws(SQLException::class)
    fun executeQuery(): ResultSet {
        return stat.executeQuery()
    }

    @Override
    @Throws(SQLException::class)
    fun executeUpdate(): Int {
        return stat.executeUpdate()
    }

    @Override
    @Throws(SQLException::class)
    override fun execute(pc: PageContext?): Boolean {
        return stat.execute()
    }

    @Override
    @Throws(SQLException::class)
    override fun executeQuery(pc: PageContext?): ResultSet {
        return stat.executeQuery()
    }

    @Override
    @Throws(SQLException::class)
    override fun executeUpdate(pc: PageContext?): Int {
        return stat.executeUpdate()
    }

    @Override
    @Throws(SQLException::class)
    fun addBatch() {
        stat.addBatch()
    }

    @Override
    @Throws(SQLException::class)
    fun clearParameters() {
        stat.clearParameters()
    }

    @get:Throws(SQLException::class)
    @get:Override
    val metaData: ResultSetMetaData
        get() = stat.getMetaData()

    @get:Throws(SQLException::class)
    @get:Override
    val parameterMetaData: ParameterMetaData
        get() = stat.getParameterMetaData()

    @Override
    @Throws(SQLException::class)
    fun setArray(parameterIndex: Int, x: Array?) {
        stat.setArray(parameterIndex, x)
    }

    @Override
    @Throws(SQLException::class)
    fun setAsciiStream(parameterIndex: Int, x: InputStream?) {
        stat.setAsciiStream(parameterIndex, x)
    }

    @Override
    @Throws(SQLException::class)
    fun setAsciiStream(parameterIndex: Int, x: InputStream?, length: Int) {
        stat.setAsciiStream(parameterIndex, x, length)
    }

    @Override
    @Throws(SQLException::class)
    fun setAsciiStream(parameterIndex: Int, x: InputStream?, length: Long) {
        stat.setAsciiStream(parameterIndex, x, length)
    }

    @Override
    @Throws(SQLException::class)
    fun setBigDecimal(parameterIndex: Int, x: BigDecimal?) {
        stat.setBigDecimal(parameterIndex, x)
    }

    @Override
    @Throws(SQLException::class)
    fun setBinaryStream(parameterIndex: Int, x: InputStream?) {
        stat.setBinaryStream(parameterIndex, x)
    }

    @Override
    @Throws(SQLException::class)
    fun setBinaryStream(parameterIndex: Int, x: InputStream?, length: Int) {
        stat.setBinaryStream(parameterIndex, x, length)
    }

    @Override
    @Throws(SQLException::class)
    fun setBinaryStream(parameterIndex: Int, x: InputStream?, length: Long) {
        stat.setBinaryStream(parameterIndex, x, length)
    }

    @Override
    @Throws(SQLException::class)
    fun setBlob(parameterIndex: Int, x: Blob?) {
        stat.setBlob(parameterIndex, x)
    }

    @Override
    @Throws(SQLException::class)
    fun setBlob(parameterIndex: Int, inputStream: InputStream?) {
        stat.setBlob(parameterIndex, inputStream)
    }

    @Override
    @Throws(SQLException::class)
    fun setBlob(parameterIndex: Int, inputStream: InputStream?, length: Long) {
        stat.setBlob(parameterIndex, inputStream, length)
    }

    @Override
    @Throws(SQLException::class)
    fun setBoolean(parameterIndex: Int, x: Boolean) {
        stat.setBoolean(parameterIndex, x)
    }

    @Override
    @Throws(SQLException::class)
    fun setByte(parameterIndex: Int, x: Byte) {
        stat.setByte(parameterIndex, x)
    }

    @Override
    @Throws(SQLException::class)
    fun setBytes(parameterIndex: Int, x: ByteArray?) {
        stat.setBytes(parameterIndex, x)
    }

    @Override
    @Throws(SQLException::class)
    fun setCharacterStream(parameterIndex: Int, reader: Reader?) {
        stat.setCharacterStream(parameterIndex, reader)
    }

    @Override
    @Throws(SQLException::class)
    fun setCharacterStream(parameterIndex: Int, reader: Reader?, length: Int) {
        stat.setCharacterStream(parameterIndex, reader, length)
    }

    @Override
    @Throws(SQLException::class)
    fun setCharacterStream(parameterIndex: Int, reader: Reader?, length: Long) {
        stat.setCharacterStream(parameterIndex, reader, length)
    }

    @Override
    @Throws(SQLException::class)
    fun setClob(parameterIndex: Int, x: Clob?) {
        stat.setClob(parameterIndex, x)
    }

    @Override
    @Throws(SQLException::class)
    fun setClob(parameterIndex: Int, reader: Reader?) {
        stat.setClob(parameterIndex, reader)
    }

    @Override
    @Throws(SQLException::class)
    fun setClob(parameterIndex: Int, reader: Reader?, length: Long) {
        stat.setClob(parameterIndex, reader, length)
    }

    @Override
    @Throws(SQLException::class)
    fun setDate(parameterIndex: Int, x: Date?) {
        stat.setDate(parameterIndex, x)
    }

    @Override
    @Throws(SQLException::class)
    fun setDate(parameterIndex: Int, x: Date?, cal: Calendar?) {
        stat.setDate(parameterIndex, x, cal)
    }

    @Override
    @Throws(SQLException::class)
    fun setDouble(parameterIndex: Int, x: Double) {
        stat.setDouble(parameterIndex, x)
    }

    @Override
    @Throws(SQLException::class)
    fun setFloat(parameterIndex: Int, x: Float) {
        stat.setFloat(parameterIndex, x)
    }

    @Override
    @Throws(SQLException::class)
    fun setInt(parameterIndex: Int, x: Int) {
        stat.setInt(parameterIndex, x)
    }

    @Override
    @Throws(SQLException::class)
    fun setLong(parameterIndex: Int, x: Long) {
        stat.setLong(parameterIndex, x)
    }

    @Override
    @Throws(SQLException::class)
    fun setNCharacterStream(parameterIndex: Int, value: Reader?) {
        stat.setNCharacterStream(parameterIndex, value)
    }

    @Override
    @Throws(SQLException::class)
    fun setNCharacterStream(parameterIndex: Int, value: Reader?, length: Long) {
        stat.setNCharacterStream(parameterIndex, value, length)
    }

    @Override
    @Throws(SQLException::class)
    fun setNClob(parameterIndex: Int, value: NClob?) {
        stat.setNClob(parameterIndex, value)
    }

    @Override
    @Throws(SQLException::class)
    fun setNClob(parameterIndex: Int, reader: Reader?) {
        stat.setNClob(parameterIndex, reader)
    }

    @Override
    @Throws(SQLException::class)
    fun setNClob(parameterIndex: Int, reader: Reader?, length: Long) {
        stat.setNClob(parameterIndex, reader, length)
    }

    @Override
    @Throws(SQLException::class)
    fun setNString(parameterIndex: Int, value: String?) {
        stat.setNString(parameterIndex, value)
    }

    @Override
    @Throws(SQLException::class)
    fun setNull(parameterIndex: Int, sqlType: Int) {
        stat.setNull(parameterIndex, sqlType)
    }

    @Override
    @Throws(SQLException::class)
    fun setNull(parameterIndex: Int, sqlType: Int, typeName: String?) {
        stat.setNull(parameterIndex, sqlType, typeName)
    }

    @Override
    @Throws(SQLException::class)
    fun setObject(parameterIndex: Int, x: Object?) {
        stat.setObject(parameterIndex, x)
    }

    @Override
    @Throws(SQLException::class)
    fun setObject(parameterIndex: Int, x: Object?, targetSqlType: Int) {
        stat.setObject(parameterIndex, x, targetSqlType)
    }

    @Override
    @Throws(SQLException::class)
    fun setObject(parameterIndex: Int, x: Object?, targetSqlType: Int, scaleOrLength: Int) {
        stat.setObject(parameterIndex, x, targetSqlType, scaleOrLength)
    }

    @Override
    @Throws(SQLException::class)
    fun setRef(parameterIndex: Int, x: Ref?) {
        stat.setRef(parameterIndex, x)
    }

    @Override
    @Throws(SQLException::class)
    fun setRowId(parameterIndex: Int, x: RowId?) {
        stat.setRowId(parameterIndex, x)
    }

    @Override
    @Throws(SQLException::class)
    fun setSQLXML(parameterIndex: Int, xmlObject: SQLXML?) {
        stat.setSQLXML(parameterIndex, xmlObject)
    }

    @Override
    @Throws(SQLException::class)
    fun setShort(parameterIndex: Int, x: Short) {
        stat.setShort(parameterIndex, x)
    }

    @Override
    @Throws(SQLException::class)
    fun setString(parameterIndex: Int, x: String?) {
        stat.setString(parameterIndex, x)
    }

    @Override
    @Throws(SQLException::class)
    fun setTime(parameterIndex: Int, x: Time?) {
        stat.setTime(parameterIndex, x)
    }

    @Override
    @Throws(SQLException::class)
    fun setTime(parameterIndex: Int, x: Time?, cal: Calendar?) {
        stat.setTime(parameterIndex, x, cal)
    }

    @Override
    @Throws(SQLException::class)
    fun setTimestamp(parameterIndex: Int, x: Timestamp?) {
        stat.setTimestamp(parameterIndex, x)
    }

    @Override
    @Throws(SQLException::class)
    fun setTimestamp(parameterIndex: Int, x: Timestamp?, cal: Calendar?) {
        stat.setTimestamp(parameterIndex, x, cal)
    }

    @Override
    @Throws(SQLException::class)
    fun setURL(parameterIndex: Int, x: URL?) {
        stat.setURL(parameterIndex, x)
    }

    @Override
    @Throws(SQLException::class)
    fun setUnicodeStream(parameterIndex: Int, x: InputStream?, length: Int) {
        stat.setUnicodeStream(parameterIndex, x, length)
    }

    init {
        this.stat = stat
        sQL = sql
    }
}