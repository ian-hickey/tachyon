package lucee.runtime.type.query

import java.io.InputStream

class QueryFQ(qry: Query?) : Query, Objects, QueryResult, Serializable, Cloneable {
    private var qry: Query?

    @Transient
    private var qr: QueryResult?

    @Transient
    private var obj: Objects?

    @Transient
    private var cloned = false

    // method that do not change the inner state of the query
    @Override
    override fun getTemplate(): String? {
        return qry.getTemplate()
    }

    @Override
    override fun getTemplateLine(): TemplateLine? {
        return if (qry is QueryImpl) (qry as QueryImpl?).getTemplateLine() else TemplateLine(qry.getTemplate(), 0)
    }

    @Override
    fun executionTime(): Int {
        return qry.executionTime()
    }

    @Override
    override fun getUpdateCount(): Int {
        return qry.getUpdateCount()
    }

    @Override
    fun getGeneratedKeys(): Query? {
        return qry.getGeneratedKeys()
    }

    @Override
    fun size(): Int {
        return qry.size()
    }

    @Override
    fun keys(): Array<Key?>? {
        return qry.keys()
    }

    @Override
    operator fun get(key: String?, defaultValue: Object?): Object? {
        return qry.get(key, defaultValue)
    }

    @Override
    operator fun get(key: Key?, defaultValue: Object?): Object? {
        return qry.get(key, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    operator fun get(key: String?): Object? {
        return qry.get(key)
    }

    @Override
    @Throws(PageException::class)
    operator fun get(key: Key?): Object? {
        return qry.get(key)
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
    operator fun next(): Boolean {
        return qry.next()
    }

    @Override
    @Throws(PageException::class)
    fun next(pid: Int): Boolean {
        return qry.next(pid)
    }

    @Override
    @Throws(PageException::class)
    fun reset() {
        qry.reset()
    }

    @Override
    @Throws(PageException::class)
    fun reset(pid: Int) {
        qry.reset(pid)
    }

    @Override
    override fun getRecordcount(): Int {
        return qry.getRecordcount()
    }

    @Override
    override fun getColumncount(): Int {
        return qr!!.getColumncount()
    }

    @Override
    fun getCurrentrow(pid: Int): Int {
        return qry.getCurrentrow(pid)
    }

    @Override
    @Throws(PageException::class)
    fun go(index: Int, pid: Int): Boolean {
        return qry.go(index, pid)
    }

    @Override
    fun isEmpty(): Boolean {
        return qry.isEmpty()
    }

    @Override
    fun toDumpData(pageContext: PageContext?, maxlevel: Int, dp: DumpProperties?): DumpData? {
        return qry.toDumpData(pageContext, maxlevel, dp)
    }

    @Override
    @Synchronized
    fun getTypes(): IntArray? {
        return qry.getTypes()
    }

    @Override
    @Synchronized
    fun getTypesAsMap(): Map<Key?, String?>? {
        return qry.getTypesAsMap()
    }

    @Override
    fun clone(): Object {
        return qry.clone()
    }

    @Override
    fun duplicate(deepCopy: Boolean): Collection? {
        return qry.duplicate(deepCopy)
    }

    @Override
    @Throws(PageException::class)
    fun getColumn(key: String?): QueryColumn? {
        return qry.getColumn(key)
    }

    @Override
    @Throws(PageException::class)
    fun getColumn(key: Key?): QueryColumn? {
        return qry.getColumn(key)
    }

    @Override
    @Throws(PageException::class)
    fun rename(columnName: Key?, newColumnName: Key?) {
        qry.rename(columnName, newColumnName)
    }

    @Override
    fun getColumn(key: String?, defaultValue: QueryColumn?): QueryColumn? {
        // TODO wrap column
        return qry.getColumn(key, defaultValue)
    }

    @Override
    fun getColumn(key: Key?, defaultValue: QueryColumn?): QueryColumn? {
        // TODO wrap column
        return qry.getColumn(key, defaultValue)
    }

    @Override
    override fun toString(): String {
        return qry.toString()
    }

    @Override
    override fun getCacheType(): String? {
        return qry.getCacheType()
    }

    @Override
    override fun isCached(): Boolean {
        return qry.isCached()
    }

    @Override
    fun getColumnIndex(coulmnName: String?): Int {
        return qry.getColumnIndex(coulmnName)
    }

    @Override
    fun getColumns(): Array<String?>? {
        return qry.getColumns()
    }

    @Override
    override fun getColumnNames(): Array<Key?>? {
        return qry.getColumnNames()
    }

    @Override
    fun getColumnNamesAsString(): Array<String?>? {
        return qry.getColumnNamesAsString()
    }

    @Override
    fun getColumnCount(): Int {
        return qry.getColumnCount()
    }

    @Override
    @Throws(IndexOutOfBoundsException::class)
    fun getData(row: Int, col: Int): String? {
        return qry.getData(row, col)
    }

    @Override
    override fun getName(): String? {
        return qry.getName()
    }

    @Override
    fun getRowCount(): Int {
        return qry.getRowCount()
    }

    @Override
    fun containsKey(key: String?): Boolean {
        return qry.containsKey(key)
    }

    @Override
    fun containsKey(key: Key?): Boolean {
        return qry.containsKey(key)
    }

    @Override
    @Throws(PageException::class)
    fun castToString(): String? {
        return qry.castToString()
    }

    @Override
    fun castToString(defaultValue: String?): String? {
        return qry.castToString(defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun castToBooleanValue(): Boolean {
        return qry.castToBooleanValue()
    }

    @Override
    fun castToBoolean(defaultValue: Boolean?): Boolean? {
        return qry.castToBoolean(defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun castToDoubleValue(): Double {
        return qry.castToDoubleValue()
    }

    @Override
    fun castToDoubleValue(defaultValue: Double): Double {
        return qry.castToDoubleValue(defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun castToDateTime(): DateTime? {
        return qry.castToDateTime()
    }

    @Override
    fun castToDateTime(defaultValue: DateTime?): DateTime? {
        return qry.castToDateTime(defaultValue)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(b: Boolean): Int {
        return qry.compareTo(b)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(dt: DateTime?): Int {
        return qry.compareTo(dt)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(d: Double): Int {
        return qry.compareTo(d)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(str: String?): Int {
        return qry.compareTo(str)
    }

    @Override
    @Synchronized
    fun getMetaDataSimple(): Array? {
        return qry.getMetaDataSimple()
    }

    @Override
    override fun getSql(): SQL? {
        return qry.getSql()
    }

    @Override
    @Throws(SQLException::class)
    fun getObject(columnName: String?): Object? {
        return qry.getObject(columnName)
    }

    @Override
    @Throws(SQLException::class)
    fun getObject(columnIndex: Int): Object? {
        return qry.getObject(columnIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun getString(columnIndex: Int): String? {
        return qry.getString(columnIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun getString(columnName: String?): String? {
        return qry.getString(columnName)
    }

    @Override
    @Throws(SQLException::class)
    fun getBoolean(columnIndex: Int): Boolean {
        return qry.getBoolean(columnIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun getBoolean(columnName: String?): Boolean {
        return qry.getBoolean(columnName)
    }

    @Override
    @Throws(PageException::class)
    fun call(pc: PageContext?, methodName: Key?, arguments: Array<Object?>?): Object? {
        return obj.call(pc, methodName, arguments)
    }

    @Override
    @Throws(PageException::class)
    fun callWithNamedValues(pc: PageContext?, methodName: Key?, args: Struct?): Object? {
        return obj.callWithNamedValues(pc, methodName, args)
    }

    @Override
    operator fun get(pc: PageContext?, key: Key?, defaultValue: Object?): Object? {
        return obj.get(pc, key, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    operator fun get(pc: PageContext?, key: Key?): Object? {
        return obj.get(pc, key)
    }

    @Override
    @Throws(SQLException::class)
    fun wasNull(): Boolean {
        return qry.wasNull()
    }

    @Override
    @Throws(SQLException::class)
    fun absolute(row: Int): Boolean {
        return qry.absolute(row)
    }

    @Override
    @Throws(SQLException::class)
    fun afterLast() {
        qry.afterLast()
    }

    @Override
    @Throws(SQLException::class)
    fun beforeFirst() {
        qry.beforeFirst()
    }

    @Override
    @Throws(SQLException::class)
    fun findColumn(columnName: String?): Int {
        return qry.findColumn(columnName)
    }

    @Override
    @Throws(SQLException::class)
    fun first(): Boolean {
        return qry.first()
    }

    @Override
    @Throws(SQLException::class)
    fun getArray(i: Int): Array? {
        return qry.getArray(i)
    }

    @Override
    @Throws(SQLException::class)
    fun getArray(colName: String?): Array? {
        return qry.getArray(colName)
    }

    @Override
    @Throws(SQLException::class)
    fun getAsciiStream(columnIndex: Int): InputStream? {
        return qry.getAsciiStream(columnIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun getAsciiStream(columnName: String?): InputStream? {
        return qry.getAsciiStream(columnName)
    }

    @Override
    @Throws(SQLException::class)
    fun getBigDecimal(columnIndex: Int): BigDecimal? {
        return qry.getBigDecimal(columnIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun getBigDecimal(columnName: String?): BigDecimal? {
        return qry.getBigDecimal(columnName)
    }

    @Override
    @Throws(SQLException::class)
    fun getBigDecimal(columnIndex: Int, scale: Int): BigDecimal? {
        return qry.getBigDecimal(columnIndex, scale)
    }

    @Override
    @Throws(SQLException::class)
    fun getBigDecimal(columnName: String?, scale: Int): BigDecimal? {
        return qry.getBigDecimal(columnName, scale)
    }

    @Override
    @Throws(SQLException::class)
    fun getBinaryStream(columnIndex: Int): InputStream? {
        return qry.getBinaryStream(columnIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun getBinaryStream(columnName: String?): InputStream? {
        return qry.getBinaryStream(columnName)
    }

    @Override
    @Throws(SQLException::class)
    fun getBlob(i: Int): Blob? {
        return qry.getBlob(i)
    }

    @Override
    @Throws(SQLException::class)
    fun getBlob(colName: String?): Blob? {
        return qry.getBlob(colName)
    }

    @Override
    @Throws(SQLException::class)
    fun getByte(columnIndex: Int): Byte {
        return qry.getByte(columnIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun getByte(columnName: String?): Byte {
        return qry.getByte(columnName)
    }

    @Override
    @Throws(SQLException::class)
    fun getBytes(columnIndex: Int): ByteArray? {
        return qry.getBytes(columnIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun getBytes(columnName: String?): ByteArray? {
        return qry.getBytes(columnName)
    }

    @Override
    @Throws(SQLException::class)
    fun getCharacterStream(columnIndex: Int): Reader? {
        return qry.getCharacterStream(columnIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun getCharacterStream(columnName: String?): Reader? {
        return qry.getCharacterStream(columnName)
    }

    @Override
    @Throws(SQLException::class)
    fun getClob(i: Int): Clob? {
        return qry.getClob(i)
    }

    @Override
    @Throws(SQLException::class)
    fun getClob(colName: String?): Clob? {
        return qry.getClob(colName)
    }

    @Override
    @Throws(SQLException::class)
    fun getConcurrency(): Int {
        return qry.getConcurrency()
    }

    @Override
    @Throws(SQLException::class)
    fun getCursorName(): String? {
        return qry.getCursorName()
    }

    @Override
    @Throws(SQLException::class)
    fun getDate(columnIndex: Int): Date? {
        return qry.getDate(columnIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun getDate(columnName: String?): Date? {
        return qry.getDate(columnName)
    }

    @Override
    @Throws(SQLException::class)
    fun getDate(columnIndex: Int, cal: Calendar?): Date? {
        return qry.getDate(columnIndex, cal)
    }

    @Override
    @Throws(SQLException::class)
    fun getDate(columnName: String?, cal: Calendar?): Date? {
        return qry.getDate(columnName, cal)
    }

    @Override
    @Throws(SQLException::class)
    fun getDouble(columnIndex: Int): Double {
        return qry.getDouble(columnIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun getDouble(columnName: String?): Double {
        return qry.getDouble(columnName)
    }

    @Override
    @Throws(SQLException::class)
    fun getFetchDirection(): Int {
        return qry.getFetchDirection()
    }

    @Override
    @Throws(SQLException::class)
    fun getFetchSize(): Int {
        return qry.getFetchSize()
    }

    @Override
    @Throws(SQLException::class)
    fun getFloat(columnIndex: Int): Float {
        return qry.getFloat(columnIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun getFloat(columnName: String?): Float {
        return qry.getFloat(columnName)
    }

    @Override
    @Throws(SQLException::class)
    fun getInt(columnIndex: Int): Int {
        return qry.getInt(columnIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun getInt(columnName: String?): Int {
        return qry.getInt(columnName)
    }

    @Override
    @Throws(SQLException::class)
    fun getLong(columnIndex: Int): Long {
        return qry.getLong(columnIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun getLong(columnName: String?): Long {
        return qry.getLong(columnName)
    }

    @Override
    @Throws(SQLException::class)
    fun getObject(i: Int, map: Map?): Object? {
        return qry.getObject(i, map)
    }

    @Override
    @Throws(SQLException::class)
    fun getObject(colName: String?, map: Map?): Object? {
        return qry.getObject(colName, map)
    }

    @Override
    @Throws(SQLException::class)
    fun <T> getObject(columnIndex: Int, type: Class<T?>?): T? {
        return qry.getObject(columnIndex, type)
    }

    @Override
    @Throws(SQLException::class)
    fun <T> getObject(columnLabel: String?, type: Class<T?>?): T? {
        return qry.getObject(columnLabel, type)
    }

    @Override
    @Throws(SQLException::class)
    fun getRef(i: Int): Ref? {
        return qry.getRef(i)
    }

    @Override
    @Throws(SQLException::class)
    fun getRef(colName: String?): Ref? {
        return qry.getRef(colName)
    }

    @Override
    @Throws(SQLException::class)
    fun getRow(): Int {
        return qry.getRow()
    }

    @Override
    @Throws(SQLException::class)
    fun getShort(columnIndex: Int): Short {
        return qry.getShort(columnIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun getShort(columnName: String?): Short {
        return qry.getShort(columnName)
    }

    @Override
    @Throws(SQLException::class)
    fun getStatement(): Statement? {
        return qry.getStatement()
    }

    @Override
    @Throws(SQLException::class)
    fun getTime(columnIndex: Int): Time? {
        return qry.getTime(columnIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun getTime(columnName: String?): Time? {
        return qry.getTime(columnName)
    }

    @Override
    @Throws(SQLException::class)
    fun getTime(columnIndex: Int, cal: Calendar?): Time? {
        return qry.getTime(columnIndex, cal)
    }

    @Override
    @Throws(SQLException::class)
    fun getTime(columnName: String?, cal: Calendar?): Time? {
        return qry.getTime(columnName, cal)
    }

    @Override
    @Throws(SQLException::class)
    fun getTimestamp(columnIndex: Int): Timestamp? {
        return qry.getTimestamp(columnIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun getTimestamp(columnName: String?): Timestamp? {
        return qry.getTimestamp(columnName)
    }

    @Override
    @Throws(SQLException::class)
    fun getTimestamp(columnIndex: Int, cal: Calendar?): Timestamp? {
        return qry.getTimestamp(columnIndex, cal)
    }

    @Override
    @Throws(SQLException::class)
    fun getTimestamp(columnName: String?, cal: Calendar?): Timestamp? {
        return qry.getTimestamp(columnName, cal)
    }

    @Override
    @Throws(SQLException::class)
    fun getType(): Int {
        return qry.getType()
    }

    @Override
    @Throws(SQLException::class)
    fun getURL(columnIndex: Int): URL? {
        return qry.getURL(columnIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun getURL(columnName: String?): URL? {
        return qry.getURL(columnName)
    }

    @Override
    @Throws(SQLException::class)
    fun getUnicodeStream(columnIndex: Int): InputStream? {
        return qry.getUnicodeStream(columnIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun getUnicodeStream(columnName: String?): InputStream? {
        return qry.getUnicodeStream(columnName)
    }

    @Override
    @Throws(SQLException::class)
    fun getWarnings(): SQLWarning? {
        return qry.getWarnings()
    }

    @Override
    @Throws(SQLException::class)
    fun isAfterLast(): Boolean {
        return qry.isAfterLast()
    }

    @Override
    @Throws(SQLException::class)
    fun isBeforeFirst(): Boolean {
        return qry.isBeforeFirst()
    }

    @Override
    @Throws(SQLException::class)
    fun isFirst(): Boolean {
        return qry.isFirst()
    }

    @Override
    @Throws(SQLException::class)
    fun isLast(): Boolean {
        return qry.isLast()
    }

    @Override
    @Throws(SQLException::class)
    fun last(): Boolean {
        return qry.last()
    }

    @Override
    @Throws(SQLException::class)
    fun moveToCurrentRow() {
        qry.moveToCurrentRow()
    }

    @Override
    @Throws(SQLException::class)
    fun moveToInsertRow() {
        qry.moveToInsertRow()
    }

    @Override
    @Throws(SQLException::class)
    fun previous(): Boolean {
        return qry.previous()
    }

    @Override
    fun previous(pid: Int): Boolean {
        return qry.previous(pid)
    }

    @Override
    @Throws(SQLException::class)
    fun relative(rows: Int): Boolean {
        return qry.relative(rows)
    }

    @Override
    @Throws(SQLException::class)
    fun rowDeleted(): Boolean {
        return qry.rowDeleted()
    }

    @Override
    @Throws(SQLException::class)
    fun rowInserted(): Boolean {
        return qry.rowInserted()
    }

    @Override
    @Throws(SQLException::class)
    fun rowUpdated(): Boolean {
        return qry.rowUpdated()
    }

    @Override
    @Throws(SQLException::class)
    fun getMetaData(): ResultSetMetaData? {
        return qry.getMetaData()
    }

    @Override
    fun keyIterator(): Iterator<Key?>? {
        return qry.keyIterator()
    }

    @Override
    fun keysAsStringIterator(): Iterator<String?>? {
        return qry.keysAsStringIterator()
    }

    @Override
    fun entryIterator(): Iterator<Entry<Key?, Object?>?>? {
        return qry.entryIterator()
    }

    @Override
    fun valueIterator(): Iterator<Object?>? {
        return qry.valueIterator()
    }

    @Override
    @Throws(SQLException::class)
    fun getHoldability(): Int {
        return qry.getHoldability()
    }

    @Override
    @Throws(SQLException::class)
    fun isClosed(): Boolean {
        return qry.isClosed()
    }

    @Override
    @Throws(SQLException::class)
    fun getNString(columnIndex: Int): String? {
        return qry.getNString(columnIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun getNString(columnLabel: String?): String? {
        return qry.getNString(columnLabel)
    }

    @Override
    @Throws(SQLException::class)
    fun getNCharacterStream(columnIndex: Int): Reader? {
        return qry.getNCharacterStream(columnIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun getNCharacterStream(columnLabel: String?): Reader? {
        return qry.getNCharacterStream(columnLabel)
    }

    @Override
    @Throws(SQLException::class)
    fun <T> unwrap(iface: Class<T?>?): T? {
        return qry.unwrap(iface)
    }

    @Override
    @Throws(SQLException::class)
    fun isWrapperFor(iface: Class<*>?): Boolean {
        return qry.isWrapperFor(iface)
    }

    @Override
    @Throws(SQLException::class)
    fun getNClob(columnIndex: Int): NClob? {
        return qry.getNClob(columnIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun getNClob(columnLabel: String?): NClob? {
        return qry.getNClob(columnLabel)
    }

    @Override
    @Throws(SQLException::class)
    fun getSQLXML(columnIndex: Int): SQLXML? {
        return qry.getSQLXML(columnIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun getSQLXML(columnLabel: String?): SQLXML? {
        return qry.getSQLXML(columnLabel)
    }

    @Override
    @Throws(SQLException::class)
    fun getRowId(columnIndex: Int): RowId? {
        return qry.getRowId(columnIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun getRowId(columnLabel: String?): RowId? {
        return qry.getRowId(columnLabel)
    }

    @Override
    override fun getExecutionTime(): Long {
        return qry.getExecutionTime()
    }

    @Override
    fun getIterator(): Iterator? {
        return qry.getIterator()
    }

    @Override
    override fun equals(obj: Object?): Boolean {
        return qry.equals(obj)
    }

    // methods changing the inner state of the Query
    @Override
    override fun setUpdateCount(updateCount: Int) {
        if (!cloned) _clone()
        qr!!.setUpdateCount(updateCount)
    }

    @Override
    fun removeEL(key: Key?): Object? {
        if (!cloned) _clone()
        return qry.removeEL(key)
    }

    @Override
    @Throws(PageException::class)
    fun remove(key: Key?): Object? {
        if (!cloned) _clone()
        return qry.remove(key)
    }

    @Override
    fun remove(key: Key?, defaultValue: Object?): Object? {
        if (!cloned) _clone()
        return qry.remove(key, defaultValue)
    }

    @Override
    fun clear() {
        if (!cloned) _clone()
        qry.clear()
    }

    @Override
    @Synchronized
    @Throws(PageException::class)
    fun removeRow(row: Int): Int {
        if (!cloned) _clone()
        return qry.removeRow(row)
    }

    @Override
    fun removeRowEL(row: Int): Int {
        if (!cloned) _clone()
        return qry.removeRowEL(row)
    }

    @Override
    @Throws(PageException::class)
    fun removeColumn(key: String?): QueryColumn? {
        if (!cloned) _clone()
        return qry.removeColumn(key)
    }

    @Override
    @Throws(PageException::class)
    fun removeColumn(key: Key?): QueryColumn? {
        if (!cloned) _clone()
        return qry.removeColumn(key)
    }

    @Override
    @Synchronized
    fun removeColumnEL(key: String?): QueryColumn? {
        if (!cloned) _clone()
        return qry.removeColumnEL(key)
    }

    @Override
    fun removeColumnEL(key: Key?): QueryColumn? {
        if (!cloned) _clone()
        return qry.removeColumnEL(key)
    }

    @Override
    fun setEL(key: String?, value: Object?): Object? {
        if (!cloned) _clone()
        return qry.setEL(key, value)
    }

    @Override
    fun setEL(key: Key?, value: Object?): Object? {
        if (!cloned) _clone()
        return qry.setEL(key, value)
    }

    @Override
    @Throws(PageException::class)
    operator fun set(key: String?, value: Object?): Object? {
        if (!cloned) _clone()
        return qry.set(key, value)
    }

    @Override
    @Throws(PageException::class)
    operator fun set(key: Key?, value: Object?): Object? {
        if (!cloned) _clone()
        return qry.set(key, value)
    }

    @Override
    @Throws(PageException::class)
    fun setAt(key: String?, row: Int, value: Object?): Object? {
        if (!cloned) _clone()
        return qry.setAt(key, row, value)
    }

    @Override
    @Throws(PageException::class)
    fun setAt(key: Key?, row: Int, value: Object?): Object? {
        if (!cloned) _clone()
        return qry.setAt(key, row, value)
    }

    @Override
    fun setAtEL(key: String?, row: Int, value: Object?): Object? {
        if (!cloned) _clone()
        return qry.setAtEL(key, row, value)
    }

    @Override
    fun setAtEL(key: Key?, row: Int, value: Object?): Object? {
        if (!cloned) _clone()
        return qry.setAtEL(key, row, value)
    }

    @Override
    @Throws(PageException::class)
    fun sort(column: String?) {
        if (!cloned) _clone()
        qry.sort(column)
    }

    @Override
    @Throws(PageException::class)
    fun sort(column: Key?) {
        if (!cloned) _clone()
        qry.sort(column)
    }

    @Override
    @Throws(PageException::class)
    fun sort(strColumn: String?, order: Int) {
        if (!cloned) _clone()
        qry.sort(strColumn, order)
    }

    @Override
    @Throws(PageException::class)
    fun sort(keyColumn: Key?, order: Int) {
        if (!cloned) _clone()
        qry.sort(keyColumn, order)
    }

    @Override
    fun addRow(count: Int): Boolean {
        if (!cloned) _clone()
        return qry.addRow(count)
    }

    @Override
    @Throws(PageException::class)
    fun addColumn(columnName: String?, content: Array?): Boolean {
        if (!cloned) _clone()
        return qry.addColumn(columnName, content)
    }

    @Override
    @Throws(PageException::class)
    fun addColumn(columnName: Key?, content: Array?): Boolean {
        if (!cloned) _clone()
        return qry.addColumn(columnName, content)
    }

    @Override
    @Throws(PageException::class)
    fun addColumn(columnName: String?, content: Array?, type: Int): Boolean {
        if (!cloned) _clone()
        return qry.addColumn(columnName, content, type)
    }

    @Override
    @Throws(PageException::class)
    fun addColumn(columnName: Key?, content: Array?, type: Int): Boolean {
        if (!cloned) _clone()
        return qry.addColumn(columnName, content, type)
    }

    @Override
    override fun setExecutionTime(exeTime: Long) {
        if (!cloned) _clone()
        qry.setExecutionTime(exeTime)
    }

    @Override
    override fun setCacheType(cacheType: String?) {
        if (!cloned) _clone()
        qry.setCacheType(cacheType)
    }

    @Override
    fun setCached(isCached: Boolean) {
        if (!cloned) _clone()
        qry.setCached(isCached)
    }

    @Override
    fun addRow(): Int {
        if (!cloned) _clone()
        return qry.addRow()
    }

    @Override
    @Throws(PageException::class)
    override fun setColumnNames(trg: Array<Key?>?) {
        if (!cloned) _clone()
        qr!!.setColumnNames(trg)
    }

    @Override
    @Throws(IndexOutOfBoundsException::class)
    fun setData(row: Int, col: Int, value: String?) {
        if (!cloned) _clone()
        qry.setData(row, col, value)
    }

    @Override
    @Throws(PageException::class)
    operator fun set(pc: PageContext?, propertyName: Key?, value: Object?): Object? {
        if (!cloned) _clone()
        return obj.set(pc, propertyName, value)
    }

    @Override
    fun setEL(pc: PageContext?, propertyName: Key?, value: Object?): Object? {
        if (!cloned) _clone()
        return obj.setEL(pc, propertyName, value)
    }

    @Override
    @Throws(SQLException::class)
    fun cancelRowUpdates() {
        if (!cloned) _clone()
        qry.cancelRowUpdates()
    }

    @Override
    @Throws(SQLException::class)
    fun clearWarnings() {
        if (!cloned) _clone()
        qry.clearWarnings()
    }

    @Override
    @Throws(SQLException::class)
    fun close() {
        if (!cloned) _clone()
        qry.close()
    }

    @Override
    @Throws(SQLException::class)
    fun deleteRow() {
        if (!cloned) _clone()
        qry.deleteRow()
    }

    @Override
    @Throws(SQLException::class)
    fun insertRow() {
        if (!cloned) _clone()
        qry.insertRow()
    }

    @Override
    @Throws(SQLException::class)
    fun refreshRow() {
        if (!cloned) _clone()
        qry.refreshRow()
    }

    @Override
    @Throws(SQLException::class)
    fun setFetchDirection(direction: Int) {
        if (!cloned) _clone()
        qry.setFetchDirection(direction)
    }

    @Override
    @Throws(SQLException::class)
    fun setFetchSize(rows: Int) {
        if (!cloned) _clone()
        qry.setFetchSize(rows)
    }

    @Override
    @Throws(SQLException::class)
    fun updateArray(columnIndex: Int, x: Array?) {
        if (!cloned) _clone()
        qry.updateArray(columnIndex, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateArray(columnName: String?, x: Array?) {
        if (!cloned) _clone()
        qry.updateArray(columnName, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateAsciiStream(columnIndex: Int, x: InputStream?, length: Int) {
        if (!cloned) _clone()
        qry.updateAsciiStream(columnIndex, x, length)
    }

    @Override
    @Throws(SQLException::class)
    fun updateAsciiStream(columnName: String?, x: InputStream?, length: Int) {
        if (!cloned) _clone()
        qry.updateAsciiStream(columnName, x, length)
    }

    @Override
    @Throws(SQLException::class)
    fun updateBigDecimal(columnIndex: Int, x: BigDecimal?) {
        if (!cloned) _clone()
        qry.updateBigDecimal(columnIndex, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateBigDecimal(columnName: String?, x: BigDecimal?) {
        if (!cloned) _clone()
        qry.updateBigDecimal(columnName, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateBinaryStream(columnIndex: Int, x: InputStream?, length: Int) {
        if (!cloned) _clone()
        qry.updateBinaryStream(columnIndex, x, length)
    }

    @Override
    @Throws(SQLException::class)
    fun updateBinaryStream(columnName: String?, x: InputStream?, length: Int) {
        if (!cloned) _clone()
        qry.updateBinaryStream(columnName, x, length)
    }

    @Override
    @Throws(SQLException::class)
    fun updateBlob(columnIndex: Int, x: Blob?) {
        if (!cloned) _clone()
        qry.updateBlob(columnIndex, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateBlob(columnName: String?, x: Blob?) {
        if (!cloned) _clone()
        qry.updateBlob(columnName, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateBoolean(columnIndex: Int, x: Boolean) {
        if (!cloned) _clone()
        qry.updateBoolean(columnIndex, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateBoolean(columnName: String?, x: Boolean) {
        if (!cloned) _clone()
        qry.updateBoolean(columnName, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateByte(columnIndex: Int, x: Byte) {
        if (!cloned) _clone()
        qry.updateByte(columnIndex, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateByte(columnName: String?, x: Byte) {
        if (!cloned) _clone()
        qry.updateByte(columnName, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateBytes(columnIndex: Int, x: ByteArray?) {
        if (!cloned) _clone()
        qry.updateBytes(columnIndex, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateBytes(columnName: String?, x: ByteArray?) {
        if (!cloned) _clone()
        qry.updateBytes(columnName, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateCharacterStream(columnIndex: Int, reader: Reader?, length: Int) {
        if (!cloned) _clone()
        qry.updateCharacterStream(columnIndex, reader, length)
    }

    @Override
    @Throws(SQLException::class)
    fun updateCharacterStream(columnName: String?, reader: Reader?, length: Int) {
        if (!cloned) _clone()
        qry.updateCharacterStream(columnName, reader, length)
    }

    @Override
    @Throws(SQLException::class)
    fun updateClob(columnIndex: Int, x: Clob?) {
        if (!cloned) _clone()
        qry.updateClob(columnIndex, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateClob(columnName: String?, x: Clob?) {
        if (!cloned) _clone()
        qry.updateClob(columnName, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateDate(columnIndex: Int, x: Date?) {
        if (!cloned) _clone()
        qry.updateDate(columnIndex, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateDate(columnName: String?, x: Date?) {
        if (!cloned) _clone()
        qry.updateDate(columnName, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateDouble(columnIndex: Int, x: Double) {
        if (!cloned) _clone()
        qry.updateDouble(columnIndex, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateDouble(columnName: String?, x: Double) {
        if (!cloned) _clone()
        qry.updateDouble(columnName, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateFloat(columnIndex: Int, x: Float) {
        if (!cloned) _clone()
        qry.updateFloat(columnIndex, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateFloat(columnName: String?, x: Float) {
        if (!cloned) _clone()
        qry.updateFloat(columnName, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateInt(columnIndex: Int, x: Int) {
        if (!cloned) _clone()
        qry.updateInt(columnIndex, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateInt(columnName: String?, x: Int) {
        if (!cloned) _clone()
        qry.updateInt(columnName, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateLong(columnIndex: Int, x: Long) {
        if (!cloned) _clone()
        qry.updateLong(columnIndex, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateLong(columnName: String?, x: Long) {
        if (!cloned) _clone()
        qry.updateLong(columnName, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateNull(columnIndex: Int) {
        if (!cloned) _clone()
        qry.updateNull(columnIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun updateNull(columnName: String?) {
        if (!cloned) _clone()
        qry.updateNull(columnName)
    }

    @Override
    @Throws(SQLException::class)
    fun updateObject(columnIndex: Int, x: Object?) {
        if (!cloned) _clone()
        qry.updateObject(columnIndex, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateObject(columnName: String?, x: Object?) {
        if (!cloned) _clone()
        qry.updateObject(columnName, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateObject(columnIndex: Int, x: Object?, scale: Int) {
        if (!cloned) _clone()
        qry.updateObject(columnIndex, x, scale)
    }

    @Override
    @Throws(SQLException::class)
    fun updateObject(columnName: String?, x: Object?, scale: Int) {
        if (!cloned) _clone()
        qry.updateObject(columnName, x, scale)
    }

    @Override
    @Throws(SQLException::class)
    fun updateRef(columnIndex: Int, x: Ref?) {
        if (!cloned) _clone()
        qry.updateRef(columnIndex, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateRef(columnName: String?, x: Ref?) {
        if (!cloned) _clone()
        qry.updateRef(columnName, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateRow() {
        if (!cloned) _clone()
        qry.updateRow()
    }

    @Override
    @Throws(SQLException::class)
    fun updateShort(columnIndex: Int, x: Short) {
        if (!cloned) _clone()
        qry.updateShort(columnIndex, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateShort(columnName: String?, x: Short) {
        if (!cloned) _clone()
        qry.updateShort(columnName, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateString(columnIndex: Int, x: String?) {
        if (!cloned) _clone()
        qry.updateString(columnIndex, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateString(columnName: String?, x: String?) {
        if (!cloned) _clone()
        qry.updateString(columnName, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateTime(columnIndex: Int, x: Time?) {
        if (!cloned) _clone()
        qry.updateTime(columnIndex, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateTime(columnName: String?, x: Time?) {
        if (!cloned) _clone()
        qry.updateTime(columnName, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateTimestamp(columnIndex: Int, x: Timestamp?) {
        if (!cloned) _clone()
        qry.updateTimestamp(columnIndex, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateTimestamp(columnName: String?, x: Timestamp?) {
        if (!cloned) _clone()
        qry.updateTimestamp(columnName, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateNString(columnIndex: Int, nString: String?) {
        if (!cloned) _clone()
        qry.updateNString(columnIndex, nString)
    }

    @Override
    @Throws(SQLException::class)
    fun updateNString(columnLabel: String?, nString: String?) {
        if (!cloned) _clone()
        qry.updateNString(columnLabel, nString)
    }

    @Override
    @Throws(SQLException::class)
    fun updateNCharacterStream(columnIndex: Int, x: Reader?, length: Long) {
        if (!cloned) _clone()
        qry.updateNCharacterStream(columnIndex, x, length)
    }

    @Override
    @Throws(SQLException::class)
    fun updateNCharacterStream(columnLabel: String?, reader: Reader?, length: Long) {
        if (!cloned) _clone()
        qry.updateNCharacterStream(columnLabel, reader, length)
    }

    @Override
    @Throws(SQLException::class)
    fun updateAsciiStream(columnIndex: Int, x: InputStream?, length: Long) {
        if (!cloned) _clone()
        qry.updateAsciiStream(columnIndex, x, length)
    }

    @Override
    @Throws(SQLException::class)
    fun updateBinaryStream(columnIndex: Int, x: InputStream?, length: Long) {
        if (!cloned) _clone()
        qry.updateBinaryStream(columnIndex, x, length)
    }

    @Override
    @Throws(SQLException::class)
    fun updateCharacterStream(columnIndex: Int, x: Reader?, length: Long) {
        if (!cloned) _clone()
        qry.updateCharacterStream(columnIndex, x, length)
    }

    @Override
    @Throws(SQLException::class)
    fun updateAsciiStream(columnLabel: String?, x: InputStream?, length: Long) {
        if (!cloned) _clone()
        qry.updateAsciiStream(columnLabel, x, length)
    }

    @Override
    @Throws(SQLException::class)
    fun updateBinaryStream(columnLabel: String?, x: InputStream?, length: Long) {
        if (!cloned) _clone()
        qry.updateBinaryStream(columnLabel, x, length)
    }

    @Override
    @Throws(SQLException::class)
    fun updateCharacterStream(columnLabel: String?, reader: Reader?, length: Long) {
        if (!cloned) _clone()
        qry.updateCharacterStream(columnLabel, reader, length)
    }

    @Override
    @Throws(SQLException::class)
    fun updateBlob(columnIndex: Int, inputStream: InputStream?, length: Long) {
        if (!cloned) _clone()
        qry.updateBlob(columnIndex, inputStream, length)
    }

    @Override
    @Throws(SQLException::class)
    fun updateBlob(columnLabel: String?, inputStream: InputStream?, length: Long) {
        if (!cloned) _clone()
        qry.updateBlob(columnLabel, inputStream, length)
    }

    @Override
    @Throws(SQLException::class)
    fun updateClob(columnIndex: Int, reader: Reader?, length: Long) {
        if (!cloned) _clone()
        qry.updateClob(columnIndex, reader, length)
    }

    @Override
    @Throws(SQLException::class)
    fun updateClob(columnLabel: String?, reader: Reader?, length: Long) {
        if (!cloned) _clone()
        qry.updateClob(columnLabel, reader, length)
    }

    @Override
    @Throws(SQLException::class)
    fun updateNClob(columnIndex: Int, reader: Reader?, length: Long) {
        if (!cloned) _clone()
        qry.updateNClob(columnIndex, reader, length)
    }

    @Override
    @Throws(SQLException::class)
    fun updateNClob(columnLabel: String?, reader: Reader?, length: Long) {
        if (!cloned) _clone()
        qry.updateNClob(columnLabel, reader, length)
    }

    @Override
    @Throws(SQLException::class)
    fun updateNCharacterStream(columnIndex: Int, x: Reader?) {
        if (!cloned) _clone()
        qry.updateNCharacterStream(columnIndex, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateNCharacterStream(columnLabel: String?, reader: Reader?) {
        if (!cloned) _clone()
        qry.updateNCharacterStream(columnLabel, reader)
    }

    @Override
    @Throws(SQLException::class)
    fun updateAsciiStream(columnIndex: Int, x: InputStream?) {
        if (!cloned) _clone()
        qry.updateAsciiStream(columnIndex, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateBinaryStream(columnIndex: Int, x: InputStream?) {
        if (!cloned) _clone()
        qry.updateBinaryStream(columnIndex, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateCharacterStream(columnIndex: Int, x: Reader?) {
        if (!cloned) _clone()
        qry.updateCharacterStream(columnIndex, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateAsciiStream(columnLabel: String?, x: InputStream?) {
        if (!cloned) _clone()
        qry.updateAsciiStream(columnLabel, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateBinaryStream(columnLabel: String?, x: InputStream?) {
        if (!cloned) _clone()
        qry.updateBinaryStream(columnLabel, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateCharacterStream(columnLabel: String?, reader: Reader?) {
        if (!cloned) _clone()
        qry.updateCharacterStream(columnLabel, reader)
    }

    @Override
    @Throws(SQLException::class)
    fun updateBlob(columnIndex: Int, inputStream: InputStream?) {
        if (!cloned) _clone()
        qry.updateBlob(columnIndex, inputStream)
    }

    @Override
    @Throws(SQLException::class)
    fun updateBlob(columnLabel: String?, inputStream: InputStream?) {
        if (!cloned) _clone()
        qry.updateBlob(columnLabel, inputStream)
    }

    @Override
    @Throws(SQLException::class)
    fun updateClob(columnIndex: Int, reader: Reader?) {
        if (!cloned) _clone()
        qry.updateClob(columnIndex, reader)
    }

    @Override
    @Throws(SQLException::class)
    fun updateClob(columnLabel: String?, reader: Reader?) {
        if (!cloned) _clone()
        qry.updateClob(columnLabel, reader)
    }

    @Override
    @Throws(SQLException::class)
    fun updateNClob(columnIndex: Int, reader: Reader?) {
        if (!cloned) _clone()
        qry.updateNClob(columnIndex, reader)
    }

    @Override
    @Throws(SQLException::class)
    fun updateNClob(columnLabel: String?, reader: Reader?) {
        if (!cloned) _clone()
        qry.updateNClob(columnLabel, reader)
    }

    @Override
    @Throws(SQLException::class)
    fun updateNClob(columnIndex: Int, nClob: NClob?) {
        if (!cloned) _clone()
        qry.updateNClob(columnIndex, nClob)
    }

    @Override
    @Throws(SQLException::class)
    fun updateNClob(columnLabel: String?, nClob: NClob?) {
        if (!cloned) _clone()
        qry.updateNClob(columnLabel, nClob)
    }

    @Override
    @Throws(SQLException::class)
    fun updateSQLXML(columnIndex: Int, xmlObject: SQLXML?) {
        if (!cloned) _clone()
        qry.updateSQLXML(columnIndex, xmlObject)
    }

    @Override
    @Throws(SQLException::class)
    fun updateSQLXML(columnLabel: String?, xmlObject: SQLXML?) {
        if (!cloned) _clone()
        qry.updateSQLXML(columnLabel, xmlObject)
    }

    @Override
    @Throws(SQLException::class)
    fun updateRowId(columnIndex: Int, x: RowId?) {
        if (!cloned) _clone()
        qry.updateRowId(columnIndex, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateRowId(columnLabel: String?, x: RowId?) {
        if (!cloned) _clone()
        qry.updateRowId(columnLabel, x)
    }

    @Override
    @Synchronized
    fun enableShowQueryUsage() {
        if (!cloned) _clone()
        qry.enableShowQueryUsage()
    }

    @Synchronized
    fun _clone() {
        if (cloned) return  // we repeat this because the check outside the method is not sync
        qry = qry.duplicate(true) as Query
        qr = qry
        obj = qry as Objects?
        cloned = true
    }

    init {
        this.qry = qry
        qr = qry
        obj = qry as Objects?
    }
}