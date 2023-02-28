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
package lucee.runtime.type.query

import java.io.InputStream

class SimpleQuery(pc: PageContext?, dc: DatasourceConnection?, sql: SQL?, maxrow: Int, fetchsize: Int, timeout: TimeSpan?, name: String?, templateLine: TemplateLine?, tz: TimeZone?) : Query, ResultSet, Objects, QueryResult, Cloneable {
    private var stat: Statement?
    private var res: ResultSet? = null
    private var meta: ResultSetMetaData? = null
    private var columnNames: Array<Collection.Key?>?
    private val columns: Map<String?, SimpleQueryColumn?>? = LinkedHashMap<String?, SimpleQueryColumn?>()
    private var _types: IntArray?
    private val name: String?
    private val templateLine: TemplateLine?
    private val sql: SQL?
    private val exeTime: Long
    private var recordcount = 0
    private val arrCurrentRow: ArrayInt? = ArrayInt()
    private var cacheType: String? = null
    private var updateCount = 0
    private val dc: DatasourceConnection?
    fun getDc(): DatasourceConnection? {
        return dc
    }

    @Throws(SQLException::class)
    private fun setAttributes(stat: Statement?, maxrow: Int, fetchsize: Int, timeout: TimeSpan?) {
        if (maxrow > -1) stat.setMaxRows(maxrow)
        if (fetchsize > 0) stat.setFetchSize(fetchsize)
        if (timeout != null && timeout.getSeconds() as Int > 0) DataSourceUtil.setQueryTimeoutSilent(stat, timeout.getSeconds() as Int)
    }

    @Throws(DatabaseException::class, PageException::class, SQLException::class)
    private fun setItems(pc: PageContext?, tz: TimeZone?, preStat: PreparedStatement?, items: Array<SQLItem?>?) {
        for (i in items.indices) {
            SQLCaster.setValue(pc, tz, preStat, i + 1, items!![i])
        }
    }

    @Throws(SQLException::class)
    private fun init(res: ResultSet?) {
        this.res = res
        meta = res.getMetaData()

        // init columns
        val columncount: Int = meta.getColumnCount()
        val tmpKeys: List<Key?> = ArrayList<Key?>()
        // List<Integer> tmpTypes=new ArrayList<Integer>();
        // int count=0;
        var key: Collection.Key
        var columnName: String
        var type: Int
        for (i in 0 until columncount) {
            try {
                columnName = meta.getColumnName(i + 1)
                type = meta.getColumnType(i + 1)
            } catch (e: SQLException) {
                throw toRuntimeExc(e)
            }
            if (StringUtil.isEmpty(columnName)) columnName = "column_$i"
            key = KeyImpl.init(columnName)
            val index = tmpKeys.indexOf(key)
            if (index == -1) {
                // mappings.put(key.getLowerString(), Caster.toInteger(i+1));
                tmpKeys.add(key)
                // tmpTypes.add(type);
                columns.put(key.getLowerString(), SimpleQueryColumn(this, res, key, type, i + 1))

                // count++;
            }
        }
        columnNames = tmpKeys.toArray(arrayOfNulls<Key?>(tmpKeys.size()))
        res.last()
        recordcount = res.getRow()
        res.beforeFirst()
        /*
		 * Iterator<Integer> it = tmpTypes.iterator(); types=new int[tmpTypes.size()]; int index=0;
		 * while(it.hasNext()){ types[index++]=it.next(); }
		 */
    }

    @Override
    fun executionTime(): Int {
        return exeTime.toInt()
    }

    @Override
    override fun getUpdateCount(): Int {
        return updateCount
    }

    @Override
    override fun setUpdateCount(updateCount: Int) {
        this.updateCount = updateCount
    }

    @Override
    fun size(): Int {
        return columnNames!!.size
    }

    @Override
    fun keys(): Array<Key?>? {
        return columnNames
    }

    @Override
    fun removeEL(key: Key?): Object? {
        throw notSupported()
    }

    @Override
    fun remove(key: Key?, defaultValue: Object?): Object? {
        throw notSupported()
    }

    @Override
    @Throws(PageException::class)
    fun remove(key: Key?): Object? {
        throw notSupported()
    }

    @Override
    fun clear() {
        throw notSupported()
    }

    @Override
    operator fun get(key: Key?, defaultValue: Object?): Object? {
        val pid = getPid()
        return getAt(key, getCurrentrow(pid), pid, defaultValue)
    }

    @Override
    operator fun get(key: String?, defaultValue: Object?): Object? {
        return get(KeyImpl.init(key), defaultValue)
    }

    @Override
    @Throws(PageException::class)
    operator fun get(key: String?): Object? {
        return get(KeyImpl.init(key))
    }

    @Override
    @Throws(PageException::class)
    operator fun get(key: Key?): Object? {
        val pid = getPid()
        return getAt(key, getCurrentrow(pid), pid)
    }

    fun getAt(key: Key?, row: Int, pid: Int, defaultValue: Object?): Object? {
        val c: Char = key.lowerCharAt(0)
        if (c == 'r') {
            if (key.equals(KeyConstants._RECORDCOUNT)) return Double.valueOf(getRecordcount())
        } else if (c == 'c') {
            if (key.equals(KeyConstants._CURRENTROW)) return Double.valueOf(getCurrentrow(pid)) else if (key.equals(KeyConstants._COLUMNLIST)) return getColumnlist()
        }
        val column: SimpleQueryColumn = columns!![key.getLowerString()] ?: return null
        return try {
            column.get(row, defaultValue)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            defaultValue
        }
    }

    @Throws(PageException::class)
    fun getAt(key: Key?, row: Int, pid: Int): Object? {
        val res: Object? = getAt(key, row, pid, DEFAULT_VALUE)
        if (res !== DEFAULT_VALUE) return res
        throw DatabaseException("key [$key] not found", null, null, null)
    }

    @Override
    fun getAt(key: Key?, row: Int, defaultValue: Object?): Object? {
        return getAt(key, row, getPid(), defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun getAt(key: Key?, row: Int): Object? {
        val res: Object? = getAt(key, row, getPid(), DEFAULT_VALUE)
        if (res !== DEFAULT_VALUE) return res
        throw DatabaseException("key [$key] not found", null, null, null)
    }

    @Override
    fun getAt(key: String?, row: Int, defaultValue: Object?): Object? {
        return getAt(KeyImpl.init(key), row, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun getAt(key: String?, row: Int): Object? {
        return getAt(KeyImpl.init(key), row)
    }

    @Override
    @Synchronized
    @Throws(PageException::class)
    fun removeRow(row: Int): Int {
        throw notSupported()
    }

    @Override
    fun removeRowEL(row: Int): Int {
        throw notSupported()
    }

    @Override
    @Throws(DatabaseException::class)
    fun removeColumn(key: String?): QueryColumn? {
        throw notSupported()
    }

    @Override
    @Throws(DatabaseException::class)
    fun removeColumn(key: Key?): QueryColumn? {
        throw notSupported()
    }

    @Override
    @Synchronized
    fun removeColumnEL(key: String?): QueryColumn? {
        throw notSupported()
    }

    @Override
    fun removeColumnEL(key: Key?): QueryColumn? {
        throw notSupported()
    }

    @Override
    fun setEL(key: String?, value: Object?): Object? {
        throw notSupported()
    }

    @Override
    fun setEL(key: Key?, value: Object?): Object? {
        throw notSupported()
    }

    @Override
    @Throws(PageException::class)
    operator fun set(key: String?, value: Object?): Object? {
        throw notSupported()
    }

    @Override
    @Throws(PageException::class)
    operator fun set(key: Key?, value: Object?): Object? {
        throw notSupported()
    }

    @Override
    @Throws(PageException::class)
    fun setAt(key: String?, row: Int, value: Object?): Object? {
        throw notSupported()
    }

    @Override
    @Throws(PageException::class)
    fun setAt(key: Key?, row: Int, value: Object?): Object? {
        throw notSupported()
    }

    @Override
    fun setAtEL(key: String?, row: Int, value: Object?): Object? {
        throw notSupported()
    }

    @Override
    fun setAtEL(key: Key?, row: Int, value: Object?): Object? {
        throw notSupported()
    }

    @Override
    @Synchronized
    operator fun next(): Boolean {
        return try {
            next(getPid())
        } catch (e: DatabaseException) {
            throw PageRuntimeException(e)
        }
    }

    @Override
    @Synchronized
    @Throws(DatabaseException::class)
    fun next(pid: Int): Boolean {
        throwIfClosed()
        if (recordcount >= arrCurrentRow.set(pid, arrCurrentRow.get(pid, 0) + 1)) {
            return true
        }
        arrCurrentRow.set(pid, 0)
        return false
    }

    @Override
    @Synchronized
    fun reset() {
        reset(getPid())
    }

    @Override
    @Synchronized
    fun reset(pid: Int) {
        arrCurrentRow.set(pid, 0)
    }

    @Override
    override fun getRecordcount(): Int {
        return recordcount
    }

    @Override
    override fun getColumncount(): Int {
        return if (columnNames == null) 0 else columnNames!!.size
    }

    @Override
    fun isEmpty(): Boolean {
        return recordcount + getColumnCount() == 0
    }

    @Override
    @Synchronized
    fun getCurrentrow(pid: Int): Int {
        return arrCurrentRow.get(pid, 1)
    }

    fun getColumnlist(upperCase: Boolean): String? {
        val columnNames: Array<Key?>? = keys()
        val sb = StringBuffer()
        for (i in columnNames.indices) {
            if (i > 0) sb.append(',')
            sb.append(if (upperCase) columnNames!![i].getUpperString() else columnNames!![i].getString())
        }
        return sb.toString()
    }

    fun getColumnlist(): String? {
        return getColumnlist(true)
    }

    @Throws(DatabaseException::class)
    fun go(index: Int): Boolean {
        return go(index, getPid())
    }

    @Override
    @Throws(DatabaseException::class)
    fun go(index: Int, pid: Int): Boolean {
        throwIfClosed()
        if (index > 0 && index <= recordcount) {
            arrCurrentRow.set(pid, index)
            return true
        }
        arrCurrentRow.set(pid, 0)
        return false
    }

    /*
	 * public synchronized boolean go(int index) { if(index==getCurrentrow()) return true; try { return
	 * res.absolute(index); } catch (SQLException e) { throw toRuntimeExc(e); } }
	 * 
	 * public boolean go(int index, int pid) { return go(index); }
	 */
    @Override
    fun toDumpData(pageContext: PageContext?, maxlevel: Int, dp: DumpProperties?): DumpData? {
        return QueryUtil.toDumpData(this, pageContext, maxlevel, dp)
    }

    @Override
    @Throws(PageException::class)
    fun sort(column: String?) {
        throw notSupported()
    }

    @Override
    @Throws(PageException::class)
    fun sort(column: Key?) {
        throw notSupported()
    }

    @Override
    @Synchronized
    @Throws(PageException::class)
    fun sort(strColumn: String?, order: Int) {
        throw notSupported()
    }

    @Override
    @Synchronized
    @Throws(PageException::class)
    fun sort(keyColumn: Key?, order: Int) {
        throw notSupported()
    }

    @Override
    @Synchronized
    fun addRow(count: Int): Boolean {
        throw notSupported()
    }

    @Override
    @Throws(DatabaseException::class)
    fun addColumn(columnName: String?, content: Array?): Boolean {
        throw notSupported()
    }

    @Override
    @Throws(PageException::class)
    fun addColumn(columnName: Key?, content: Array?): Boolean {
        throw notSupported()
    }

    @Override
    @Synchronized
    @Throws(DatabaseException::class)
    fun addColumn(columnName: String?, content: Array?, type: Int): Boolean {
        throw notSupported()
    }

    @Override
    @Throws(DatabaseException::class)
    fun addColumn(columnName: Key?, content: Array?, type: Int): Boolean {
        throw notSupported()
    }

    @Override
    fun clone(): Object {
        return cloneQuery(true)
    }

    @Override
    fun duplicate(deepCopy: Boolean): Collection? {
        return cloneQuery(deepCopy)
    }

    fun cloneQuery(deepCopy: Boolean): QueryImpl? {
        return QueryImpl.cloneQuery(this, deepCopy)
    }

    @Override
    @Synchronized
    fun getTypes(): IntArray? {
        if (_types == null) {
            _types = IntArray(columns!!.size())
            var i = 0
            val it: Iterator<Entry<String?, SimpleQueryColumn?>?> = columns.entrySet().iterator()
            while (it.hasNext()) {
                _types!![i++] = it.next().getValue().getType()
            }
        }
        return _types
    }

    @Override
    @Synchronized
    fun getTypesAsMap(): Map<Collection.Key?, String?>? {
        val map: Map<Collection.Key?, String?> = HashMap<Collection.Key?, String?>()
        val it: Iterator<SimpleQueryColumn?> = columns!!.values().iterator()
        var c: SimpleQueryColumn?
        while (it.hasNext()) {
            c = it.next()
            map.put(c!!.getKey(), c!!.getTypeAsString())
        }
        return map
    }

    @Override
    @Throws(DatabaseException::class)
    fun getColumn(key: String?): QueryColumn? {
        return getColumn(KeyImpl.init(key))
    }

    @Override
    @Throws(DatabaseException::class)
    fun getColumn(key: Key?): QueryColumn? {
        val rtn: QueryColumn = getColumn(key, null)
        if (rtn != null) return rtn
        throw DatabaseException("key [" + key.getString().toString() + "] not found in query, columns are [" + getColumnlist(false).toString() + "]", null, null, null)
    }

    @Override
    fun getColumn(key: String?, defaultValue: QueryColumn?): QueryColumn? {
        return getColumn(KeyImpl.init(key), defaultValue)
    }

    @Override
    fun getColumn(key: Key?, defaultValue: QueryColumn?): QueryColumn? {
        if (key.getString().length() > 0) {
            val c: Char = key.lowerCharAt(0)
            if (c == 'r') {
                if (key.equals(KeyConstants._RECORDCOUNT)) return QueryColumnRef(this, key, Types.INTEGER)
            } else if (c == 'c') {
                if (key.equals(KeyConstants._CURRENTROW)) return QueryColumnRef(this, key, Types.INTEGER) else if (key.equals(KeyConstants._COLUMNLIST)) return QueryColumnRef(this, key, Types.INTEGER)
            }
            val col: SimpleQueryColumn? = columns!![key.getLowerString()]
            if (col != null) return col
        }
        return defaultValue
    }

    @Override
    @Synchronized
    @Throws(ExpressionException::class)
    fun rename(columnName: Key?, newColumnName: Key?) {
        throw notSupported()
        // Integer index=mappings.get(columnName);
        // if(index==null) throw new ExpressionException("invalid column name definitions");
        // TODO implement
    }

    @Override
    override fun toString(): String {
        return res.toString()
    }

    @Override
    override fun setExecutionTime(exeTime: Long) {
        throw notSupported()
    }

    @Synchronized
    fun cutRowsTo(maxrows: Int): Boolean {
        throw notSupported()
    }

    @Override
    fun setCached(isCached: Boolean) {
        throw notSupported()
    }

    @Override
    override fun isCached(): Boolean {
        return false
    }

    @Override
    fun addRow(): Int {
        throw notSupported()
    }

    fun getColumnName(columnIndex: Int): Key? {
        val it: Iterator<SimpleQueryColumn?> = columns!!.values().iterator()
        var c: SimpleQueryColumn?
        while (it.hasNext()) {
            c = it.next()
            if (c!!.getIndex() === columnIndex) return c!!.getKey()
        }
        return null
    }

    @Override
    fun getColumnIndex(coulmnName: String?): Int {
        val col: SimpleQueryColumn = columns!![coulmnName.toLowerCase()] ?: return -1
        return col.getIndex()
    }

    @Override
    fun getColumns(): Array<String?>? {
        return getColumnNamesAsString()
    }

    @Override
    override fun getColumnNames(): Array<Key?>? {
        val _columns: Array<Key?> = arrayOfNulls<Key?>(columnNames!!.size)
        for (i in columnNames.indices) {
            _columns[i] = columnNames!![i]
        }
        return _columns
    }

    @Override
    override fun setColumnNames(trg: Array<Key?>?) {
        throw notSupported()
    }

    @Override
    fun getColumnNamesAsString(): Array<String?>? {
        val _columns = arrayOfNulls<String?>(columnNames!!.size)
        for (i in columnNames.indices) {
            _columns[i] = columnNames!![i].getString()
        }
        return _columns
    }

    @Override
    @Synchronized
    @Throws(IndexOutOfBoundsException::class)
    fun getData(row: Int, col: Int): String? {
        return try {
            val rowBefore: Int = res.getRow()
            try {
                res.absolute(row)
                if (col < 1 || col > columnNames!!.size) {
                    IndexOutOfBoundsException("invalid column index to retrieve Data from query, valid index goes from 1 to " + columnNames!!.size)
                }
                Caster.toString(get(columnNames!![col]))
            } finally {
                res.absolute(rowBefore)
            }
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            throw toRuntimeExc(t)
        }
    }

    @Override
    override fun getName(): String? {
        return name
    }

    @Override
    fun getRowCount(): Int {
        return getRecordcount()
    }

    @Override
    @Throws(IndexOutOfBoundsException::class)
    fun setData(row: Int, col: Int, value: String?) {
        throw notSupported()
    }

    @Override
    fun containsKey(key: String?): Boolean {
        return columns!![key.toLowerCase()] != null
    }

    @Override
    fun containsKey(key: Key?): Boolean {
        return containsKey(key.getString())
    }

    @Override
    @Throws(ExpressionException::class)
    fun castToString(): String? {
        throw notSupported()
    }

    @Override
    fun castToString(defaultValue: String?): String? {
        throw notSupported()
    }

    @Override
    @Throws(ExpressionException::class)
    fun castToBooleanValue(): Boolean {
        throw notSupported()
    }

    @Override
    fun castToBoolean(defaultValue: Boolean?): Boolean? {
        throw notSupported()
    }

    @Override
    @Throws(ExpressionException::class)
    fun castToDoubleValue(): Double {
        throw notSupported()
    }

    @Override
    fun castToDoubleValue(defaultValue: Double): Double {
        throw notSupported()
    }

    @Override
    @Throws(ExpressionException::class)
    fun castToDateTime(): DateTime? {
        throw notSupported()
    }

    @Override
    fun castToDateTime(defaultValue: DateTime?): DateTime? {
        throw notSupported()
    }

    @Override
    @Throws(ExpressionException::class)
    operator fun compareTo(b: Boolean): Int {
        throw notSupported()
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(dt: DateTime?): Int {
        throw notSupported()
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(d: Double): Int {
        throw notSupported()
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(str: String?): Int {
        throw notSupported()
    }

    @Override
    @Synchronized
    fun getMetaDataSimple(): Array? {
        val cols: Array = ArrayImpl()
        var sqc: SimpleQueryColumn
        var column: Struct?
        val it: Iterator<SimpleQueryColumn?> = columns!!.values().iterator()
        while (it.hasNext()) {
            sqc = it.next()
            column = StructImpl()
            column.setEL(KeyConstants._name, sqc!!.getKey())
            column.setEL("isCaseSensitive", Boolean.FALSE)
            column.setEL("typeName", sqc!!.getTypeAsString())
            cols.appendEL(column)
        }
        return cols
    }

    @Override
    @Throws(SQLException::class)
    fun getObject(columnName: String?): Object? {
        return res.getObject(toIndex(columnName))
    }

    @Override
    @Throws(SQLException::class)
    fun getObject(columnIndex: Int): Object? {
        return res.getObject(columnIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun getString(columnIndex: Int): String? {
        return res.getString(columnIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun getString(columnName: String?): String? {
        return res.getString(toIndex(columnName))
    }

    @Override
    @Throws(SQLException::class)
    fun getBoolean(columnIndex: Int): Boolean {
        return res.getBoolean(columnIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun getBoolean(columnName: String?): Boolean {
        return res.getBoolean(toIndex(columnName))
    }

    @Override
    @Throws(PageException::class)
    fun call(pc: PageContext?, methodName: Key?, arguments: Array<Object?>?): Object? {
        throw notSupported()
    }

    @Override
    @Throws(PageException::class)
    fun callWithNamedValues(pc: PageContext?, methodName: Key?, args: Struct?): Object? {
        throw notSupported()
    }

    @Override
    operator fun get(pc: PageContext?, key: Key?, defaultValue: Object?): Object? {
        return getAt(key, getCurrentrow(pc.getId()), pc.getId(), defaultValue)
    }

    @Override
    @Throws(PageException::class)
    operator fun get(pc: PageContext?, key: Key?): Object? {
        return getAt(key, getCurrentrow(pc.getId()), pc.getId())
    }

    fun isInitalized(): Boolean {
        return true
    }

    @Override
    @Throws(PageException::class)
    operator fun set(pc: PageContext?, propertyName: Key?, value: Object?): Object? {
        throw notSupported()
    }

    @Override
    fun setEL(pc: PageContext?, propertyName: Key?, value: Object?): Object? {
        throw notSupported()
    }

    @Override
    fun wasNull(): Boolean {
        return try {
            res.wasNull()
        } catch (e: SQLException) {
            throw toRuntimeExc(e)
        }
    }

    @Override
    @Synchronized
    @Throws(SQLException::class)
    fun absolute(row: Int): Boolean {
        return res.absolute(row)
    }

    @Override
    @Synchronized
    @Throws(SQLException::class)
    fun afterLast() {
        res.afterLast()
    }

    @Override
    @Synchronized
    @Throws(SQLException::class)
    fun beforeFirst() {
        res.beforeFirst()
    }

    @Override
    @Synchronized
    @Throws(SQLException::class)
    fun cancelRowUpdates() {
        res.cancelRowUpdates()
    }

    @Override
    @Synchronized
    @Throws(SQLException::class)
    fun clearWarnings() {
        res.clearWarnings()
    }

    @Override
    @Synchronized
    @Throws(SQLException::class)
    fun close() {
        if (res != null && !res.isClosed()) {
            res.close()
        }
        if (stat != null && !stat.isClosed()) {
            stat.close()
        }
    }

    @Override
    @Synchronized
    @Throws(SQLException::class)
    fun deleteRow() {
        res.deleteRow()
    }

    @Override
    @Throws(SQLException::class)
    fun findColumn(columnName: String?): Int {
        return res.findColumn(columnName)
    }

    @Override
    @Synchronized
    @Throws(SQLException::class)
    fun first(): Boolean {
        return res.first()
    }

    @Override
    @Throws(SQLException::class)
    fun getArray(i: Int): Array? {
        return res.getArray(i)
    }

    @Override
    @Throws(SQLException::class)
    fun getArray(colName: String?): Array? {
        return res.getArray(toIndex(colName))
    }

    @Override
    @Throws(SQLException::class)
    fun getAsciiStream(columnIndex: Int): InputStream? {
        return res.getAsciiStream(columnIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun getAsciiStream(columnName: String?): InputStream? {
        return res.getAsciiStream(toIndex(columnName))
    }

    @Override
    @Throws(SQLException::class)
    fun getBigDecimal(columnIndex: Int): BigDecimal? {
        return res.getBigDecimal(columnIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun getBigDecimal(columnName: String?): BigDecimal? {
        return res.getBigDecimal(toIndex(columnName))
    }

    @Override
    @Throws(SQLException::class)
    fun getBigDecimal(columnIndex: Int, scale: Int): BigDecimal? {
        return res.getBigDecimal(columnIndex, scale)
    }

    @Override
    @Throws(SQLException::class)
    fun getBigDecimal(columnName: String?, scale: Int): BigDecimal? {
        return res.getBigDecimal(toIndex(columnName), scale)
    }

    @Override
    @Throws(SQLException::class)
    fun getBinaryStream(columnIndex: Int): InputStream? {
        return res.getBinaryStream(columnIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun getBinaryStream(columnName: String?): InputStream? {
        return res.getBinaryStream(toIndex(columnName))
    }

    @Override
    @Throws(SQLException::class)
    fun getBlob(i: Int): Blob? {
        return res.getBlob(i)
    }

    @Override
    @Throws(SQLException::class)
    fun getBlob(colName: String?): Blob? {
        return res.getBlob(toIndex(colName))
    }

    @Override
    @Throws(SQLException::class)
    fun getByte(columnIndex: Int): Byte {
        return res.getByte(columnIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun getByte(columnName: String?): Byte {
        return res.getByte(toIndex(columnName))
    }

    @Override
    @Throws(SQLException::class)
    fun getBytes(columnIndex: Int): ByteArray? {
        return res.getBytes(columnIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun getBytes(columnName: String?): ByteArray? {
        return res.getBytes(toIndex(columnName))
    }

    @Override
    @Throws(SQLException::class)
    fun getCharacterStream(columnIndex: Int): Reader? {
        return res.getCharacterStream(columnIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun getCharacterStream(columnName: String?): Reader? {
        return res.getCharacterStream(toIndex(columnName))
    }

    @Override
    @Throws(SQLException::class)
    fun getClob(i: Int): Clob? {
        return res.getClob(i)
    }

    @Override
    @Throws(SQLException::class)
    fun getClob(colName: String?): Clob? {
        return res.getClob(toIndex(colName))
    }

    @Override
    @Throws(SQLException::class)
    fun getConcurrency(): Int {
        return res.getConcurrency()
    }

    @Override
    @Throws(SQLException::class)
    fun getCursorName(): String? {
        return res.getCursorName()
    }

    @Override
    @Throws(SQLException::class)
    fun getDate(columnIndex: Int): Date? {
        return res.getDate(columnIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun getDate(columnName: String?): Date? {
        return res.getDate(toIndex(columnName))
    }

    @Override
    @Throws(SQLException::class)
    fun getDate(columnIndex: Int, cal: Calendar?): Date? {
        return res.getDate(columnIndex, cal)
    }

    @Override
    @Throws(SQLException::class)
    fun getDate(columnName: String?, cal: Calendar?): Date? {
        return res.getDate(toIndex(columnName), cal)
    }

    @Override
    @Throws(SQLException::class)
    fun getDouble(columnIndex: Int): Double {
        return res.getDouble(columnIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun getDouble(columnName: String?): Double {
        return res.getDouble(toIndex(columnName))
    }

    @Override
    @Throws(SQLException::class)
    fun getFetchDirection(): Int {
        return res.getFetchDirection()
    }

    @Override
    @Throws(SQLException::class)
    fun getFetchSize(): Int {
        return res.getFetchSize()
    }

    @Override
    @Throws(SQLException::class)
    fun getFloat(columnIndex: Int): Float {
        return res.getFloat(columnIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun getFloat(columnName: String?): Float {
        return res.getFloat(toIndex(columnName))
    }

    @Override
    @Throws(SQLException::class)
    fun getInt(columnIndex: Int): Int {
        return res.getInt(columnIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun getInt(columnName: String?): Int {
        return res.getInt(toIndex(columnName))
    }

    @Override
    @Throws(SQLException::class)
    fun getLong(columnIndex: Int): Long {
        return res.getLong(columnIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun getLong(columnName: String?): Long {
        return res.getLong(toIndex(columnName))
    }

    @Override
    @Throws(SQLException::class)
    fun getObject(i: Int, map: Map?): Object? {
        return res.getObject(i, map)
    }

    @Override
    @Throws(SQLException::class)
    fun getObject(colName: String?, map: Map?): Object? {
        return res.getObject(toIndex(colName), map)
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
    fun getRef(i: Int): Ref? {
        return res.getRef(i)
    }

    @Override
    @Throws(SQLException::class)
    fun getRef(colName: String?): Ref? {
        return res.getRef(toIndex(colName))
    }

    @Override
    @Throws(SQLException::class)
    fun getRow(): Int {
        return res.getRow()
    }

    @Override
    @Throws(SQLException::class)
    fun getShort(columnIndex: Int): Short {
        return res.getShort(columnIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun getShort(columnName: String?): Short {
        return res.getShort(toIndex(columnName))
    }

    @Override
    @Throws(SQLException::class)
    fun getStatement(): Statement? {
        return res.getStatement()
    }

    @Override
    @Throws(SQLException::class)
    fun getTime(columnIndex: Int): Time? {
        return res.getTime(columnIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun getTime(columnName: String?): Time? {
        return res.getTime(toIndex(columnName))
    }

    @Override
    @Throws(SQLException::class)
    fun getTime(columnIndex: Int, cal: Calendar?): Time? {
        return res.getTime(columnIndex, cal)
    }

    @Override
    @Throws(SQLException::class)
    fun getTime(columnName: String?, cal: Calendar?): Time? {
        return res.getTime(toIndex(columnName), cal)
    }

    @Override
    @Throws(SQLException::class)
    fun getTimestamp(columnIndex: Int): Timestamp? {
        return res.getTimestamp(columnIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun getTimestamp(columnName: String?): Timestamp? {
        return res.getTimestamp(toIndex(columnName))
    }

    @Override
    @Throws(SQLException::class)
    fun getTimestamp(columnIndex: Int, cal: Calendar?): Timestamp? {
        return res.getTimestamp(columnIndex, cal)
    }

    @Override
    @Throws(SQLException::class)
    fun getTimestamp(columnName: String?, cal: Calendar?): Timestamp? {
        return res.getTimestamp(toIndex(columnName), cal)
    }

    @Override
    @Throws(SQLException::class)
    fun getType(): Int {
        return res.getType()
    }

    @Override
    @Throws(SQLException::class)
    fun getURL(columnIndex: Int): URL? {
        return res.getURL(columnIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun getURL(columnName: String?): URL? {
        return res.getURL(toIndex(columnName))
    }

    @Override
    @Throws(SQLException::class)
    fun getUnicodeStream(columnIndex: Int): InputStream? {
        return res.getUnicodeStream(columnIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun getUnicodeStream(columnName: String?): InputStream? {
        return res.getUnicodeStream(toIndex(columnName))
    }

    @Override
    @Throws(SQLException::class)
    fun getWarnings(): SQLWarning? {
        return res.getWarnings()
    }

    @Override
    @Throws(SQLException::class)
    fun insertRow() {
        res.insertRow()
    }

    @Override
    @Throws(SQLException::class)
    fun isAfterLast(): Boolean {
        return res.isAfterLast()
    }

    @Override
    @Throws(SQLException::class)
    fun isBeforeFirst(): Boolean {
        return res.isBeforeFirst()
    }

    @Override
    @Throws(SQLException::class)
    fun isFirst(): Boolean {
        return res.isFirst()
    }

    @Override
    @Throws(SQLException::class)
    fun isLast(): Boolean {
        return res.isLast()
    }

    @Override
    @Throws(SQLException::class)
    fun last(): Boolean {
        return res.last()
    }

    @Override
    @Throws(SQLException::class)
    fun moveToCurrentRow() {
        res.moveToCurrentRow()
    }

    @Override
    @Throws(SQLException::class)
    fun moveToInsertRow() {
        res.moveToInsertRow()
    }

    @Override
    fun previous(): Boolean {
        throw notSupported()
    }

    @Override
    fun previous(pid: Int): Boolean {
        throw notSupported()
    }

    @Override
    @Throws(SQLException::class)
    fun refreshRow() {
        res.refreshRow()
    }

    @Override
    @Throws(SQLException::class)
    fun relative(rows: Int): Boolean {
        return res.relative(rows)
    }

    @Override
    @Throws(SQLException::class)
    fun rowDeleted(): Boolean {
        return res.rowDeleted()
    }

    @Override
    @Throws(SQLException::class)
    fun rowInserted(): Boolean {
        return res.rowInserted()
    }

    @Override
    @Throws(SQLException::class)
    fun rowUpdated(): Boolean {
        return res.rowUpdated()
    }

    @Override
    @Throws(SQLException::class)
    fun setFetchDirection(direction: Int) {
        res.setFetchDirection(direction)
    }

    @Override
    @Throws(SQLException::class)
    fun setFetchSize(rows: Int) {
        res.setFetchSize(rows)
    }

    @Override
    @Throws(SQLException::class)
    fun updateArray(columnIndex: Int, x: Array?) {
        res.updateArray(columnIndex, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateArray(columnName: String?, x: Array?) {
        res.updateArray(toIndex(columnName), x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateAsciiStream(columnIndex: Int, x: InputStream?, length: Int) {
        res.updateAsciiStream(columnIndex, x, length)
    }

    @Override
    @Throws(SQLException::class)
    fun updateAsciiStream(columnName: String?, x: InputStream?, length: Int) {
        res.updateAsciiStream(toIndex(columnName), x, length)
    }

    @Override
    @Throws(SQLException::class)
    fun updateBigDecimal(columnIndex: Int, x: BigDecimal?) {
        res.updateBigDecimal(columnIndex, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateBigDecimal(columnName: String?, x: BigDecimal?) {
        res.updateBigDecimal(toIndex(columnName), x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateBinaryStream(columnIndex: Int, x: InputStream?, length: Int) {
        res.updateBinaryStream(columnIndex, x, length)
    }

    @Override
    @Throws(SQLException::class)
    fun updateBinaryStream(columnName: String?, x: InputStream?, length: Int) {
        res.updateBinaryStream(toIndex(columnName), x, length)
    }

    @Override
    @Throws(SQLException::class)
    fun updateBlob(columnIndex: Int, x: Blob?) {
        res.updateBlob(columnIndex, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateBlob(columnName: String?, x: Blob?) {
        res.updateBlob(toIndex(columnName), x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateBoolean(columnIndex: Int, x: Boolean) {
        res.updateBoolean(columnIndex, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateBoolean(columnName: String?, x: Boolean) {
        res.updateBoolean(toIndex(columnName), x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateByte(columnIndex: Int, x: Byte) {
        res.updateByte(columnIndex, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateByte(columnName: String?, x: Byte) {
        res.updateByte(toIndex(columnName), x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateBytes(columnIndex: Int, x: ByteArray?) {
        res.updateBytes(columnIndex, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateBytes(columnName: String?, x: ByteArray?) {
        res.updateBytes(toIndex(columnName), x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateCharacterStream(columnIndex: Int, reader: Reader?, length: Int) {
        res.updateCharacterStream(columnIndex, reader, length)
    }

    @Override
    @Throws(SQLException::class)
    fun updateCharacterStream(columnName: String?, reader: Reader?, length: Int) {
        res.updateCharacterStream(toIndex(columnName), reader, length)
    }

    @Override
    @Throws(SQLException::class)
    fun updateClob(columnIndex: Int, x: Clob?) {
        res.updateClob(columnIndex, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateClob(columnName: String?, x: Clob?) {
        res.updateClob(toIndex(columnName), x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateDate(columnIndex: Int, x: Date?) {
        res.updateDate(columnIndex, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateDate(columnName: String?, x: Date?) {
        res.updateDate(toIndex(columnName), x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateDouble(columnIndex: Int, x: Double) {
        res.updateDouble(columnIndex, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateDouble(columnName: String?, x: Double) {
        res.updateDouble(toIndex(columnName), x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateFloat(columnIndex: Int, x: Float) {
        res.updateFloat(columnIndex, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateFloat(columnName: String?, x: Float) {
        res.updateFloat(toIndex(columnName), x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateInt(columnIndex: Int, x: Int) {
        res.updateInt(columnIndex, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateInt(columnName: String?, x: Int) {
        res.updateInt(toIndex(columnName), x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateLong(columnIndex: Int, x: Long) {
        res.updateLong(columnIndex, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateLong(columnName: String?, x: Long) {
        res.updateLong(toIndex(columnName), x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateNull(columnIndex: Int) {
        res.updateNull(columnIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun updateNull(columnName: String?) {
        res.updateNull(toIndex(columnName))
    }

    @Override
    @Throws(SQLException::class)
    fun updateObject(columnIndex: Int, x: Object?) {
        res.updateObject(columnIndex, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateObject(columnName: String?, x: Object?) {
        res.updateObject(toIndex(columnName), x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateObject(columnIndex: Int, x: Object?, scale: Int) {
        res.updateObject(columnIndex, x, scale)
    }

    @Override
    @Throws(SQLException::class)
    fun updateObject(columnName: String?, x: Object?, scale: Int) {
        res.updateObject(toIndex(columnName), x, scale)
    }

    @Override
    @Throws(SQLException::class)
    fun updateRef(columnIndex: Int, x: Ref?) {
        res.updateRef(columnIndex, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateRef(columnName: String?, x: Ref?) {
        res.updateRef(toIndex(columnName), x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateRow() {
        res.updateRow()
    }

    @Override
    @Throws(SQLException::class)
    fun updateShort(columnIndex: Int, x: Short) {
        res.updateShort(columnIndex, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateShort(columnName: String?, x: Short) {
        res.updateShort(toIndex(columnName), x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateString(columnIndex: Int, x: String?) {
        res.updateString(columnIndex, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateString(columnName: String?, x: String?) {
        res.updateString(toIndex(columnName), x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateTime(columnIndex: Int, x: Time?) {
        res.updateTime(columnIndex, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateTime(columnName: String?, x: Time?) {
        res.updateTime(toIndex(columnName), x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateTimestamp(columnIndex: Int, x: Timestamp?) {
        res.updateTimestamp(columnIndex, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateTimestamp(columnName: String?, x: Timestamp?) {
        res.updateTimestamp(toIndex(columnName), x)
    }

    @Override
    @Throws(SQLException::class)
    fun getMetaData(): ResultSetMetaData? {
        return res.getMetaData()
    }

    @Override
    fun keyIterator(): Iterator<Collection.Key?>? {
        return KeyIterator(keys())
    }

    @Override
    fun keysAsStringIterator(): Iterator<String?>? {
        return StringIterator(keys())
    }

    @Override
    fun entryIterator(): Iterator<Entry<Key?, Object?>?>? {
        return EntryIterator(this, keys())
    }

    @Override
    fun valueIterator(): Iterator<Object?>? {
        return CollectionIterator(keys(), this)
    }

    @Override
    override fun equals(obj: Object?): Boolean {
        return res.equals(obj)
    }

    @Override
    @Throws(SQLException::class)
    fun getHoldability(): Int {
        return res.getHoldability()
    }

    @Override
    @Throws(SQLException::class)
    fun isClosed(): Boolean {
        return res.isClosed()
    }

    @Override
    @Throws(SQLException::class)
    fun updateNString(columnIndex: Int, nString: String?) {
        res.updateNString(columnIndex, nString)
    }

    @Override
    @Throws(SQLException::class)
    fun updateNString(columnLabel: String?, nString: String?) {
        res.updateNString(toIndex(columnLabel), nString)
    }

    @Override
    @Throws(SQLException::class)
    fun getNString(columnIndex: Int): String? {
        return res.getNString(columnIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun getNString(columnLabel: String?): String? {
        return res.getNString(toIndex(columnLabel))
    }

    @Override
    @Throws(SQLException::class)
    fun getNCharacterStream(columnIndex: Int): Reader? {
        return res.getNCharacterStream(columnIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun getNCharacterStream(columnLabel: String?): Reader? {
        return res.getNCharacterStream(toIndex(columnLabel))
    }

    @Override
    @Throws(SQLException::class)
    fun updateNCharacterStream(columnIndex: Int, x: Reader?, length: Long) {
        res.updateNCharacterStream(columnIndex, x, length)
    }

    @Override
    @Throws(SQLException::class)
    fun updateNCharacterStream(columnLabel: String?, reader: Reader?, length: Long) {
        res.updateNCharacterStream(toIndex(columnLabel), reader, length)
    }

    @Override
    @Throws(SQLException::class)
    fun updateAsciiStream(columnIndex: Int, x: InputStream?, length: Long) {
        res.updateAsciiStream(columnIndex, x, length)
    }

    @Override
    @Throws(SQLException::class)
    fun updateBinaryStream(columnIndex: Int, x: InputStream?, length: Long) {
        res.updateBinaryStream(columnIndex, x, length)
    }

    @Override
    @Throws(SQLException::class)
    fun updateCharacterStream(columnIndex: Int, x: Reader?, length: Long) {
        res.updateCharacterStream(columnIndex, x, length)
    }

    @Override
    @Throws(SQLException::class)
    fun updateAsciiStream(columnLabel: String?, x: InputStream?, length: Long) {
        res.updateAsciiStream(toIndex(columnLabel), x, length)
    }

    @Override
    @Throws(SQLException::class)
    fun updateBinaryStream(columnLabel: String?, x: InputStream?, length: Long) {
        res.updateBinaryStream(toIndex(columnLabel), x, length)
    }

    @Override
    @Throws(SQLException::class)
    fun updateCharacterStream(columnLabel: String?, reader: Reader?, length: Long) {
        res.updateCharacterStream(toIndex(columnLabel), reader, length)
    }

    @Override
    @Throws(SQLException::class)
    fun updateBlob(columnIndex: Int, inputStream: InputStream?, length: Long) {
        res.updateBlob(columnIndex, inputStream, length)
    }

    @Override
    @Throws(SQLException::class)
    fun updateBlob(columnLabel: String?, inputStream: InputStream?, length: Long) {
        res.updateBlob(toIndex(columnLabel), inputStream, length)
    }

    @Override
    @Throws(SQLException::class)
    fun updateClob(columnIndex: Int, reader: Reader?, length: Long) {
        res.updateClob(columnIndex, reader, length)
    }

    @Override
    @Throws(SQLException::class)
    fun updateClob(columnLabel: String?, reader: Reader?, length: Long) {
        res.updateClob(toIndex(columnLabel), reader, length)
    }

    @Override
    @Throws(SQLException::class)
    fun updateNClob(columnIndex: Int, reader: Reader?, length: Long) {
        res.updateNClob(columnIndex, reader, length)
    }

    @Override
    @Throws(SQLException::class)
    fun updateNClob(columnLabel: String?, reader: Reader?, length: Long) {
        res.updateNClob(toIndex(columnLabel), reader, length)
    }

    @Override
    @Throws(SQLException::class)
    fun updateNCharacterStream(columnIndex: Int, x: Reader?) {
        res.updateNCharacterStream(columnIndex, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateNCharacterStream(columnLabel: String?, reader: Reader?) {
        res.updateNCharacterStream(toIndex(columnLabel), reader)
    }

    @Override
    @Throws(SQLException::class)
    fun updateAsciiStream(columnIndex: Int, x: InputStream?) {
        res.updateAsciiStream(columnIndex, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateBinaryStream(columnIndex: Int, x: InputStream?) {
        res.updateBinaryStream(columnIndex, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateCharacterStream(columnIndex: Int, x: Reader?) {
        res.updateCharacterStream(columnIndex, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateAsciiStream(columnLabel: String?, x: InputStream?) {
        res.updateAsciiStream(toIndex(columnLabel), x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateBinaryStream(columnLabel: String?, x: InputStream?) {
        res.updateBinaryStream(columnLabel, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateCharacterStream(columnLabel: String?, reader: Reader?) {
        res.updateCharacterStream(toIndex(columnLabel), reader)
    }

    @Override
    @Throws(SQLException::class)
    fun updateBlob(columnIndex: Int, inputStream: InputStream?) {
        res.updateBlob(columnIndex, inputStream)
    }

    @Override
    @Throws(SQLException::class)
    fun updateBlob(columnLabel: String?, inputStream: InputStream?) {
        res.updateBlob(toIndex(columnLabel), inputStream)
    }

    @Override
    @Throws(SQLException::class)
    fun updateClob(columnIndex: Int, reader: Reader?) {
        res.updateClob(columnIndex, reader)
    }

    @Override
    @Throws(SQLException::class)
    fun updateClob(columnLabel: String?, reader: Reader?) {
        res.updateClob(toIndex(columnLabel), reader)
    }

    @Override
    @Throws(SQLException::class)
    fun updateNClob(columnIndex: Int, reader: Reader?) {
        res.updateNClob(columnIndex, reader)
    }

    @Override
    @Throws(SQLException::class)
    fun updateNClob(columnLabel: String?, reader: Reader?) {
        res.updateNClob(toIndex(columnLabel), reader)
    }

    @Override
    @Throws(SQLException::class)
    fun <T> unwrap(iface: Class<T?>?): T? {
        return res.unwrap(iface)
    }

    @Override
    @Throws(SQLException::class)
    fun isWrapperFor(iface: Class<*>?): Boolean {
        return res.isWrapperFor(iface)
    }

    @Override
    @Throws(SQLException::class)
    fun updateNClob(columnIndex: Int, nClob: NClob?) {
        res.updateNClob(columnIndex, nClob)
    }

    @Override
    @Throws(SQLException::class)
    fun updateNClob(columnLabel: String?, nClob: NClob?) {
        res.updateNClob(toIndex(columnLabel), nClob)
    }

    @Override
    @Throws(SQLException::class)
    fun getNClob(columnIndex: Int): NClob? {
        return res.getNClob(columnIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun getNClob(columnLabel: String?): NClob? {
        return res.getNClob(toIndex(columnLabel))
    }

    @Override
    @Throws(SQLException::class)
    fun getSQLXML(columnIndex: Int): SQLXML? {
        return res.getSQLXML(columnIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun getSQLXML(columnLabel: String?): SQLXML? {
        return res.getSQLXML(toIndex(columnLabel))
    }

    @Override
    @Throws(SQLException::class)
    fun updateSQLXML(columnIndex: Int, xmlObject: SQLXML?) {
        res.updateSQLXML(columnIndex, xmlObject)
    }

    @Override
    @Throws(SQLException::class)
    fun updateSQLXML(columnLabel: String?, xmlObject: SQLXML?) {
        res.updateSQLXML(toIndex(columnLabel), xmlObject)
    }

    @Override
    @Throws(SQLException::class)
    fun getRowId(columnIndex: Int): RowId? {
        return res.getRowId(columnIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun getRowId(columnLabel: String?): RowId? {
        return res.getRowId(toIndex(columnLabel))
    }

    @Override
    @Throws(SQLException::class)
    fun updateRowId(columnIndex: Int, x: RowId?) {
        res.updateRowId(columnIndex, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateRowId(columnLabel: String?, x: RowId?) {
        res.updateRowId(toIndex(columnLabel), x)
    }

    @Override
    @Synchronized
    fun enableShowQueryUsage() {
        throw notSupported()
    }

    @Throws(SQLException::class)
    private fun toIndex(columnName: String?): Int {
        val col: SimpleQueryColumn = columns!![columnName.toLowerCase()]
                ?: throw SQLException("There is no column with name [" + columnName + "], available columns are [" + getColumnlist() + "]")
        return col.getIndex()
    }

    fun getPid(): Int {
        var pc: PageContext = ThreadLocalPageContext.get()
        if (pc == null) {
            pc = CFMLEngineFactory.getInstance().getThreadPageContext()
            if (pc == null) throw RuntimeException("cannot get pid for current thread")
        }
        return pc.getId()
    }

    @Override
    fun getGeneratedKeys(): Query? {
        return null
    }

    @Override
    override fun getSql(): SQL? {
        return sql
    }

    @Override
    override fun getTemplate(): String? {
        return templateLine.template
    }

    @Override
    override fun getTemplateLine(): TemplateLine? {
        return templateLine
    }

    @Override
    override fun getExecutionTime(): Long {
        return exeTime
    }

    @Override
    fun getIterator(): Iterator<*>? {
        return ForEachQueryIterator(null, this, ThreadLocalPageContext.get().getId())
    }

    @Override
    override fun getCacheType(): String? {
        return cacheType
    }

    @Override
    override fun setCacheType(cacheType: String?) {
        this.cacheType = cacheType
    }

    @Override
    fun getColumnCount(): Int {
        return columnNames!!.size
    }

    @Throws(DatabaseException::class)
    fun throwIfClosed() {
        try {
            if (res != null && res.isClosed()) {
                throw RuntimeException("The query is already closed and cannot be read again.")
            }
        } catch (e: SQLException) {
            throw DatabaseException(e, dc)
        }
    }

    companion object {
        val DEFAULT_VALUE: Object? = Object()
        fun notSupported(): PageRuntimeException? {
            return toRuntimeExc(SQLFeatureNotSupportedException("not supported"))
        }

        fun toRuntimeExc(t: Throwable?): PageRuntimeException? {
            return PageRuntimeException(Caster.toPageException(t))
        }

        fun toPageExc(t: Throwable?): PageException? {
            return Caster.toPageException(t)
        }
    }

    init {
        this.dc = dc
        this.name = name
        this.templateLine = templateLine
        this.sql = sql

        // ResultSet result=null;
        stat = null
        // check SQL Restrictions
        if (dc.getDatasource().hasSQLRestriction()) {
            QueryUtil.checkSQLRestriction(dc, sql)
        }

        // Stopwatch stopwatch=new Stopwatch(Stopwatch.UNIT_NANO);
        // stopwatch.start();
        val start: Long = System.nanoTime()
        var hasResult = false
        try {
            val items: Array<SQLItem?> = sql.getItems()
            if (items.size == 0) {
                stat = dc.getConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)
                setAttributes(stat, maxrow, fetchsize, timeout)
                // some driver do not support second argument
                hasResult = stat.execute(sql.getSQLString())
            } else {
                // some driver do not support second argument
                val preStat: PreparedStatement = dc.getPreparedStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)
                stat = preStat
                setAttributes(preStat, maxrow, fetchsize, timeout)
                setItems(pc, tz, preStat, items)
                hasResult = preStat.execute()
            }
            val res: ResultSet
            do {
                if (hasResult) {
                    res = stat.getResultSet()
                    init(res)
                    break
                }
                throw ApplicationException("Simple queries can only be used for queries returning a resultset")
            } while (true)
        } catch (e: SQLException) {
            throw DatabaseException(e, sql, dc)
        } catch (e: Throwable) {
            ExceptionUtil.rethrowIfNecessary(e)
            throw Caster.toPageException(e)
        }
        exeTime = System.nanoTime() - start
        (pc as PageContextImpl?).registerLazyStatement(stat)
    }
}