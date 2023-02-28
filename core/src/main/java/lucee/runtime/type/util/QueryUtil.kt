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
package lucee.runtime.type.util

import java.math.BigDecimal

object QueryUtil {
    @Throws(SQLException::class)
    fun toCast(result: ResultSet?, type: Int): Cast? {
        if (type == Types.TIMESTAMP) {
            return if (isTeradata(result)) Cast.TIMESTAMP_NOTZ else Cast.TIMESTAMP
        } else if (type == Types.TIME) {
            return if (isTeradata(result)) Cast.TIME_NOTZ else Cast.TIME
        } else if (type == Types.DATE) {
            return if (isTeradata(result)) Cast.DATE_NOTZ else Cast.DATE
        } else if (type == Types.CLOB) return Cast.CLOB else if (type == Types.BLOB) return Cast.BLOB else if (type == Types.BIT) return Cast.BIT else if (type == Types.ARRAY) return Cast.ARRAY else if (type == Types.BIGINT) return Cast.BIGINT else if (type == Types.SQLXML) return Cast.SQLXML else if (isOracleType(type) && isOracle(result)) {
            if (type == CFTypes.ORACLE_OPAQUE) return Cast.ORACLE_OPAQUE else if (type == CFTypes.ORACLE_BLOB) return Cast.ORACLE_BLOB else if (type == CFTypes.ORACLE_CLOB) return Cast.ORACLE_CLOB else if (type == CFTypes.ORACLE_NCLOB) return Cast.ORACLE_NCLOB else if (type == CFTypes.ORACLE_TIMESTAMPTZ) return Cast.ORACLE_TIMESTAMPTZ else if (type == CFTypes.ORACLE_TIMESTAMPLTZ) return Cast.ORACLE_TIMESTAMPLTZ else if (type == CFTypes.ORACLE_TIMESTAMPNS) return Cast.ORACLE_TIMESTAMPNS

            /*
			 * TODO if(type==CFTypes.ORACLE_DISTINCT) return Cast.ORACLE_DISTINCT;
			 * if(type==CFTypes.ORACLE_JAVA_OBJECT) return Cast.ORACLE_JAVA_OBJECT; if(type==CFTypes.ORACLE_REF)
			 * return Cast.ORACLE_REF; if(type==CFTypes.ORACLE_STRUCT) return Cast.ORACLE_STRUCT;
			 */
        }
        return OtherCast(type)
    }

    private fun isOracleType(type: Int): Boolean {
        when (type) {
            CFTypes.ORACLE_OPAQUE, CFTypes.ORACLE_BLOB, CFTypes.ORACLE_CLOB, CFTypes.ORACLE_NCLOB, CFTypes.ORACLE_DISTINCT, CFTypes.ORACLE_JAVA_OBJECT, CFTypes.ORACLE_REF, CFTypes.ORACLE_STRUCT, CFTypes.ORACLE_TIMESTAMPTZ, CFTypes.ORACLE_TIMESTAMPLTZ, CFTypes.ORACLE_TIMESTAMPNS -> return true
        }
        return false
    }

    private fun isOracle(result: ResultSet?): Boolean {
        return try {
            if (result == null) return false
            val stat: Statement = result.getStatement() ?: return false
            val conn: Connection = stat.getConnection() ?: return false
            SQLUtil.isOracle(conn)
        } catch (e: Exception) {
            false
        }
    }

    private fun isTeradata(result: ResultSet?): Boolean {
        return try {
            if (result == null) return false
            val stat: Statement = result.getStatement() ?: return false
            val conn: Connection = stat.getConnection() ?: return false
            SQLUtil.isTeradata(conn)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * return column names as Key from a query
     *
     * @param qry
     * @return
     */
    fun getColumnNames(qry: Query?): Array<Key?>? {
        val qp: Query = Caster.toQuery(qry, null)
        if (qp != null) return qp.getColumnNames()
        val strNames: Array<String?> = qry.getColumns()
        val names: Array<Key?> = arrayOfNulls<Key?>(strNames.size)
        for (i in names.indices) {
            names[i] = KeyImpl.getInstance(strNames[i])
        }
        return names
    }

    fun toStringArray(keys: Array<Collection.Key?>?): Array<String?>? {
        if (keys == null) return arrayOfNulls<String?>(0)
        val strKeys = arrayOfNulls<String?>(keys.size)
        for (i in keys.indices) {
            strKeys[i] = keys[i].getString()
        }
        return strKeys
    }

    /**
     * check if there is a sql restriction
     *
     * @param dc
     * @param sql
     * @throws PageException
     */
    @Throws(PageException::class)
    fun checkSQLRestriction(dc: DatasourceConnection?, sql: SQL?) {
        val sqlparts: Array = ListUtil.listToArrayRemoveEmpty(SQLUtil.removeLiterals(sql.getSQLString()), " \t" + System.getProperty("line.separator"))

        // print.ln(List.toStringArray(sqlparts));
        val ds: DataSource = dc.getDatasource()
        if (!ds.hasAllow(DataSource.ALLOW_ALTER)) checkSQLRestriction(dc, "alter", sqlparts, sql)
        if (!ds.hasAllow(DataSource.ALLOW_CREATE)) checkSQLRestriction(dc, "create", sqlparts, sql)
        if (!ds.hasAllow(DataSource.ALLOW_DELETE)) checkSQLRestriction(dc, "delete", sqlparts, sql)
        if (!ds.hasAllow(DataSource.ALLOW_DROP)) checkSQLRestriction(dc, "drop", sqlparts, sql)
        if (!ds.hasAllow(DataSource.ALLOW_GRANT)) checkSQLRestriction(dc, "grant", sqlparts, sql)
        if (!ds.hasAllow(DataSource.ALLOW_INSERT)) checkSQLRestriction(dc, "insert", sqlparts, sql)
        if (!ds.hasAllow(DataSource.ALLOW_REVOKE)) checkSQLRestriction(dc, "revoke", sqlparts, sql)
        if (!ds.hasAllow(DataSource.ALLOW_SELECT)) checkSQLRestriction(dc, "select", sqlparts, sql)
        if (!ds.hasAllow(DataSource.ALLOW_UPDATE)) checkSQLRestriction(dc, "update", sqlparts, sql)
    }

    @Throws(PageException::class)
    private fun checkSQLRestriction(dc: DatasourceConnection?, keyword: String?, sqlparts: Array?, sql: SQL?) {
        if (ArrayFind.find(sqlparts, keyword, false) > 0) {
            throw DatabaseException("access denied to execute \"" + StringUtil.ucFirst(keyword).toString() + "\" SQL statement for datasource " + dc.getDatasource().getName(), null, sql,
                    dc)
        }
    }

    fun toDumpData(query: Query?, pageContext: PageContext?, maxlevel: Int, dp: DumpProperties?): DumpData? {
        var maxlevel = maxlevel
        maxlevel--
        val keys: Array<Collection.Key?> = CollectionUtil.keys(query)
        val heads: Array<DumpData?> = arrayOfNulls<DumpData?>(keys.size + 1)
        // int tmp=1;
        heads[0] = SimpleDumpData("")
        for (i in keys.indices) {
            heads[i + 1] = SimpleDumpData(keys[i].getString())
        }
        val comment = StringBuilder()

        // template
        var tmpl: String? = null
        if (query is QueryResult) {
            val tl: TemplateLine = (query as QueryResult?).getTemplateLine()
            if (tl != null) tmpl = tl.toString(pageContext, true)
        } else {
            tmpl = query.getTemplate()
        }
        if (!StringUtil.isEmpty(tmpl)) comment.append("Template: ").append(ContractPath.call(pageContext, tmpl)).append("\n")
        val top: Int = dp.getMaxlevel() // in Query dump maxlevel is used as Top
        comment.append("Execution Time: ").append(Caster.toString(FormatUtil.formatNSAsMSDouble(query.getExecutionTime()))).append(" ms \n")
        comment.append("Record Count: ").append(Caster.toString(query.getRecordcount()))
        if (query.getRecordcount() > top) comment.append(" (showing top ").append(Caster.toString(top)).append(")")
        comment.append("\n")
        comment.append("Cached: ").append(if (query.isCached()) "Yes\n" else "No\n")
        if (query.isCached() && query is Query) {
            comment.append("Cache Type: ").append(query.getCacheType()).append("\n")
        }
        if (query is QueryImpl) {
            val datasourceName: String = (query as QueryImpl?).getDatasourceName()
            if (datasourceName != null) comment.append("Datasource: ").append(datasourceName).append("\n")
        }
        comment.append("Lazy: ").append(if (query is SimpleQuery) "Yes\n" else "No\n")
        val sql: SQL = query.getSql()
        if (sql != null) comment.append("SQL: ").append("\n").append(StringUtil.suppressWhiteSpace(sql.toString().trim())).append("\n")

        // table.appendRow(1, new SimpleDumpData("Execution Time (ms)"), new SimpleDumpData(exeTime));
        // table.appendRow(1, new SimpleDumpData("recordcount"), new SimpleDumpData(getRecordcount()));
        // table.appendRow(1, new SimpleDumpData("cached"), new SimpleDumpData(isCached()?"Yes":"No"));
        val recs = DumpTable("query", "#cc99cc", "#ffccff", "#000000")
        recs.setTitle("Query")
        if (dp.getMetainfo()) recs.setComment(comment.toString())
        recs.appendRow(DumpRow(-1, heads))

        // body
        var items: Array<DumpData?>?
        val recordcount: Int = query.getRecordcount()
        val columncount: Int = query.getColumnNames().length
        for (i in 0 until recordcount) {
            items = arrayOfNulls<DumpData?>(columncount + 1)
            items!![0] = SimpleDumpData(i + 1)
            for (y in keys.indices) {
                try {
                    val o: Object = query.getAt(keys[y], i + 1)
                    if (o is String) items[y + 1] = SimpleDumpData(o.toString()) else if (o is Number) items[y + 1] = SimpleDumpData(Caster.toString(o as Number)) else if (o is Boolean) items[y + 1] = SimpleDumpData((o as Boolean).booleanValue()) else if (o is Date) items[y + 1] = SimpleDumpData(Caster.toString(o)) else if (o is Clob) items[y + 1] = SimpleDumpData(Caster.toString(o)) else items[y + 1] = DumpUtil.toDumpData(o, pageContext, maxlevel, dp)
                } catch (e: PageException) {
                    items[y + 1] = SimpleDumpData("[empty]")
                }
            }
            recs.appendRow(DumpRow(1, items))
            if (i == top - 1) break
        }
        return if (!dp.getMetainfo()) recs else recs

        // table.appendRow(1, new SimpleDumpData("result"), recs);
    }

    @Throws(PageException::class)
    fun removeRows(query: Query?, index: Int, count: Int) {
        if (query.getRecordcount() === 0) throw DatabaseException("cannot remove rows, query is empty", null, null, null)
        if (index < 0 || index >= query.getRecordcount()) throw DatabaseException("invalid index [" + index + "], index must be between 0 and " + (query.getRecordcount() - 1), null, null, null)
        if (index + count > query.getRecordcount()) throw DatabaseException("invalid count [" + count + "], count+index [" + (count + index) + "] must less or equal to " + query.getRecordcount(), null, null, null)
        // MUST better and faster impl
        for (row in count downTo 1) {
            query.removeRow(index + row)
        }
    }

    @Throws(SQLException::class)
    fun execute(pc: PageContext?, stat: Statement?, createGeneratedKeys: Boolean, sql: SQL?): Boolean {
        if (stat is StatementPro) {
            val sp: StatementPro? = stat as StatementPro?
            return if (createGeneratedKeys) sp.execute(pc, sql.getSQLString(), Statement.RETURN_GENERATED_KEYS) else sp.execute(pc, sql.getSQLString())
        }
        return if (createGeneratedKeys) stat.execute(sql.getSQLString(), Statement.RETURN_GENERATED_KEYS) else stat.execute(sql.getSQLString())
    }

    @Throws(SQLException::class)
    fun execute(pc: PageContext?, ps: PreparedStatement?): Boolean {
        if (ps is PreparedStatementPro) {
            val psp: PreparedStatementPro? = ps as PreparedStatementPro?
            return psp.execute(pc)
        }
        return ps.execute()
    }

    @Throws(SQLException::class)
    fun getColumnName(meta: ResultSetMetaData?, column: Int): String? {
        return try {
            meta.getColumnLabel(column)
        } catch (e: SQLException) {
            meta.getColumnName(column)
        }
    }

    @Throws(SQLException::class)
    fun getObject(rs: ResultSet?, columnIndex: Int, type: Class?): Object? {
        if (BigDecimal::class.java === type) return rs.getBigDecimal(columnIndex)
        if (Blob::class.java === type) return rs.getBlob(columnIndex)
        if (Boolean::class.javaPrimitiveType === type || Boolean::class.java === type) return rs.getBoolean(columnIndex)
        if (Byte::class.javaPrimitiveType === type || Byte::class.java === type) return rs.getByte(columnIndex)
        if (Clob::class.java === type) return rs.getClob(columnIndex)
        if (Date::class.java === type) return rs.getDate(columnIndex)
        if (Double::class.javaPrimitiveType === type || Double::class.java === type) return rs.getDouble(columnIndex)
        if (Float::class.javaPrimitiveType === type || Float::class.java === type) return rs.getFloat(columnIndex)
        if (Int::class.javaPrimitiveType === type || Integer::class.java === type) return rs.getInt(columnIndex)
        if (Long::class.javaPrimitiveType === type || Long::class.java === type) return rs.getLong(columnIndex)
        if (Short::class.javaPrimitiveType === type || Short::class.java === type) return rs.getShort(columnIndex)
        if (String::class.java === type) return rs.getString(columnIndex)
        if (Time::class.java === type) return rs.getTime(columnIndex)
        if (Ref::class.java === type) return rs.getRef(columnIndex)
        throw SQLFeatureNotSupportedException("type [" + type.getName().toString() + "] is not supported")
    }

    @Throws(SQLException::class)
    fun getObject(rs: ResultSet?, columnLabel: String?, type: Class?): Object? {
        if (BigDecimal::class.java === type) return rs.getBigDecimal(columnLabel)
        if (Blob::class.java === type) return rs.getBlob(columnLabel)
        if (Boolean::class.javaPrimitiveType === type || Boolean::class.java === type) return rs.getBoolean(columnLabel)
        if (Byte::class.javaPrimitiveType === type || Byte::class.java === type) return rs.getByte(columnLabel)
        if (Clob::class.java === type) return rs.getClob(columnLabel)
        if (Date::class.java === type) return rs.getDate(columnLabel)
        if (Double::class.javaPrimitiveType === type || Double::class.java === type) return rs.getDouble(columnLabel)
        if (Float::class.javaPrimitiveType === type || Float::class.java === type) return rs.getFloat(columnLabel)
        if (Int::class.javaPrimitiveType === type || Integer::class.java === type) return rs.getInt(columnLabel)
        if (Long::class.javaPrimitiveType === type || Long::class.java === type) return rs.getLong(columnLabel)
        if (Short::class.javaPrimitiveType === type || Short::class.java === type) return rs.getShort(columnLabel)
        if (String::class.java === type) return rs.getString(columnLabel)
        if (Time::class.java === type) return rs.getTime(columnLabel)
        if (Ref::class.java === type) return rs.getRef(columnLabel)
        throw SQLFeatureNotSupportedException("type [" + type.getName().toString() + "] is not supported")
    }

    /**
     * return the value at the given position (row), returns the default empty value ("" or null) for
     * wrong row or null values. this method only exist for backward compatibility and should not be
     * used for new functinality
     *
     * @param column
     * @param row
     * @return
     */
    @Deprecated
    @Deprecated("use instead QueryColumn.get(int,Object)")
    fun getValue(column: QueryColumn?, row: Int): Object? { // print.ds();
        if (NullSupportHelper.full()) return column.get(row, null)
        val v: Object = column.get(row, "")
        return if (v == null) "" else v
    }

    @Deprecated
    operator fun getValue(pc: PageContext?, column: QueryColumn?, row: Int): Object? { // print.ds();
        if (NullSupportHelper.full(pc)) return column.get(row, null)
        val v: Object = column.get(row, "")
        return if (v == null) "" else v
    }

    fun duplicate2QueryColumnImpl(targetQuery: QueryImpl?, col: QueryColumn?, deepCopy: Boolean): QueryColumnImpl? {
        if (col is QueryColumnImpl) return (col as QueryColumnImpl?).cloneColumnImpl(deepCopy)

        // fill array for column
        val content: Array = ArrayImpl()
        val len: Int = col.size()
        for (i in 1..len) {
            content.setEL(i, col.get(i, null))
        }

        // create and return column
        return try {
            QueryColumnImpl(targetQuery, col.getKey(), content, col.getType())
        } catch (e: PageException) {
            throw PageRuntimeException(e)
        }
    }
}