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
package tachyon.runtime.db

import java.sql.Array

/**
 * SQL Caster
 */
object SQLCaster {
    /**
     *
     * sets a Value to a PreparedStatement
     *
     * @param stat
     * @param parameterIndex
     * @param item
     * @throws SQLException
     * @throws PageException
     * @throws DatabaseException
     */
    @Throws(PageException::class, DatabaseException::class)
    fun toSqlType(item: SQLItem): Object? {
        val value: Object = item.getValue()
        return try {
            if (item.isNulls() || value == null) {
                return null
            }
            val type: Int = item.getType()
            when (type) {
                Types.BIGINT -> Caster.toLong(value)
                Types.BIT -> Caster.toBoolean(value)
                Types.BLOB -> BlobImpl.toBlob(value)
                Types.CHAR -> Caster.toString(value)
                Types.CLOB, Types.NCLOB -> ClobImpl.toClob(value)
                Types.DATE -> Date(Caster.toDate(value, null).getTime())
                Types.NUMERIC, Types.DECIMAL -> Caster.toString(Caster.toBigDecimal(value))
                Types.DOUBLE -> Caster.toDouble(value)
                Types.FLOAT -> Caster.toFloat(value)
                Types.VARBINARY, Types.LONGVARBINARY, Types.BINARY -> Caster.toBinary(value)
                Types.REAL -> Caster.toFloat(value)
                Types.TINYINT -> Caster.toByte(value)
                Types.SMALLINT -> Caster.toShort(value)
                Types.INTEGER -> Caster.toInteger(value)
                Types.VARCHAR, Types.LONGVARCHAR, CFTypes.VARCHAR2, Types.NCHAR, Types.LONGNVARCHAR, Types.NVARCHAR, Types.SQLXML -> Caster.toString(value)
                Types.TIME -> Time(Caster.toDate(value, null).getTime())
                Types.TIMESTAMP -> Timestamp(Caster.toDate(value, null).getTime())
                Types.OTHER -> {
                    if (value is DateTime) return Date(Caster.toDate(value, null).getTime())
                    if (value is Array) return Caster.toList(value)
                    if (value is tachyon.runtime.type.Struct) Caster.toMap(value) else value
                    // toSQLObject(value); TODO alle tachyon spezifischen typen sollten in
                }
                else -> {
                    if (value is DateTime) return Date(Caster.toDate(value, null).getTime())
                    if (value is Array) return Caster.toList(value)
                    if (value is tachyon.runtime.type.Struct) Caster.toMap(value) else value
                }
            }
        } catch (pe: PageException) {
            if (!NullSupportHelper.full() && value is String && StringUtil.isEmpty(value as String)) return null
            throw pe
        }
    }

    @Throws(PageException::class, SQLException::class, DatabaseException::class)
    operator fun setValue(pc: PageContext?, tz: TimeZone?, stat: PreparedStatement, parameterIndex: Int, item: SQLItem) {
        var pc: PageContext? = pc
        pc = ThreadLocalPageContext.get(pc)
        val value: Object = item.getValue()
        if (item.isNulls() || value == null) {
            stat.setNull(parameterIndex, item.getType())
            return
        }
        val type: Int = item.getType()
        val fns: Boolean = NullSupportHelper.full(pc)
        when (type) {
            Types.BIGINT -> {
                try {
                    stat.setLong(parameterIndex, Caster.toLongValue(value))
                } catch (pe: PageException) {
                    if (!fns && value is String && StringUtil.isEmpty(value as String)) stat.setNull(parameterIndex, item.getType()) else throw pe
                }
                return
            }
            Types.BIT -> {
                try {
                    stat.setBoolean(parameterIndex, Caster.toBooleanValue(value))
                } catch (pe: PageException) {
                    if (!fns && value is String && StringUtil.isEmpty(value as String)) stat.setNull(parameterIndex, item.getType()) else throw pe
                }
                return
            }
            Types.BLOB -> {
                try {
                    stat.setBlob(parameterIndex, SQLUtil.toBlob(stat.getConnection(), value))
                } catch (pe: PageException) {
                    if (!fns && value is String && StringUtil.isEmpty(value as String)) stat.setNull(parameterIndex, item.getType()) else throw pe
                }
                return
            }
            Types.CLOB -> {
                try {
                    stat.setClob(parameterIndex, SQLUtil.toClob(stat.getConnection(), value))
                    /*
				 * if(value instanceof String) { try{ stat.setString(parameterIndex,Caster.toString(value)); }
				 * catch(Throwable t){ExceptionUtil.rethrowIfNecessary(t);
				 * stat.setClob(parameterIndex,SQLUtil.toClob(stat.getConnection(),value)); }
				 * 
				 * } else stat.setClob(parameterIndex,SQLUtil.toClob(stat.getConnection(),value));
				 */
                } catch (pe: PageException) {
                    if (!fns && value is String && StringUtil.isEmpty(value as String)) stat.setNull(parameterIndex, item.getType()) else throw pe
                }
                return
            }
            Types.CHAR -> {
                val str: String = Caster.toString(value)
                // if(str!=null && str.length()==0) str=null;
                stat.setObject(parameterIndex, str, type)
                //// stat.setString(parameterIndex,str);
                return
            }
            Types.DECIMAL, Types.NUMERIC -> {
                try {
                    stat.setDouble(parameterIndex, Caster.toDoubleValue(value))
                } catch (pe: PageException) {
                    if (!fns && value is String && StringUtil.isEmpty(value as String)) stat.setNull(parameterIndex, item.getType()) else throw pe
                }
                return
            }
            Types.DOUBLE, Types.FLOAT -> {
                try {
                    if (type == Types.FLOAT) stat.setFloat(parameterIndex, Caster.toFloatValue(value)) else if (type == Types.DOUBLE) stat.setDouble(parameterIndex, Caster.toDoubleValue(value)) else stat.setObject(parameterIndex, Caster.toDouble(value), type)
                } catch (pe: PageException) {
                    if (!fns && value is String && StringUtil.isEmpty(value as String)) stat.setNull(parameterIndex, item.getType()) else throw pe
                }
                return
            }
            Types.VARBINARY, Types.LONGVARBINARY, Types.BINARY -> {
                try {
                    stat.setObject(parameterIndex, Caster.toBinary(value), type)
                    //// stat.setBytes(parameterIndex,Caster.toBinary(value));
                } catch (pe: PageException) {
                    if (!fns && value is String && StringUtil.isEmpty(value as String)) stat.setNull(parameterIndex, item.getType()) else throw pe
                }
                return
            }
            Types.REAL -> {
                try {
                    stat.setObject(parameterIndex, Caster.toFloat(value), type)
                    //// stat.setFloat(parameterIndex,Caster.toFloatValue(value));
                } catch (pe: PageException) {
                    if (!fns && value is String && StringUtil.isEmpty(value as String)) stat.setNull(parameterIndex, item.getType()) else throw pe
                }
                return
            }
            Types.TINYINT -> {
                try {
                    stat.setObject(parameterIndex, Caster.toByte(value), type)
                    //// stat.setByte(parameterIndex,Caster.toByteValue(value));
                } catch (pe: PageException) {
                    if (!fns && value is String && StringUtil.isEmpty(value as String)) stat.setNull(parameterIndex, item.getType()) else throw pe
                }
                return
            }
            Types.SMALLINT -> {
                try {
                    stat.setObject(parameterIndex, Caster.toShort(value), type)
                    //// stat.setShort(parameterIndex,Caster.toShortValue(value));
                } catch (pe: PageException) {
                    if (!fns && value is String && StringUtil.isEmpty(value as String)) stat.setNull(parameterIndex, item.getType()) else throw pe
                }
                return
            }
            Types.INTEGER -> {
                try {
                    stat.setObject(parameterIndex, Caster.toInteger(value), type)
                    //// stat.setInt(parameterIndex,Caster.toIntValue(value));
                } catch (pe: PageException) {
                    if (!fns && value is String && StringUtil.isEmpty(value as String)) stat.setNull(parameterIndex, item.getType()) else throw pe
                }
                return
            }
            Types.SQLXML -> {
                // SQLXML is a JDBC 4 feature not supported in JDBC 3.
                var xml: SQLXML? = null
                val conn: Connection = stat.getConnection()
                xml = try {
                    conn.createSQLXML()
                } catch (t: Throwable) { // must be a throwable because it throws for example a AbstractMethodError with JDTS, but could also
                    // be other
                    ExceptionUtil.rethrowIfNecessary(t)
                    val md: DatabaseMetaData = conn.getMetaData()
                    if (md.getJDBCMajorVersion() < 4) throw DatabaseException("The data type [SQLXML] is not supported with this datasource.", ("The datasource JDBC driver compatibility is up to the versions ["
                            + md.getJDBCMajorVersion()) + "." + md.getJDBCMinorVersion().toString() + "], but this feature needs at least [4.0]", null, null)
                    throw Caster.toPageException(t)
                }
                xml.setString(Caster.toString(value))
                stat.setObject(parameterIndex, xml, type)
                return
            }
            Types.VARCHAR, Types.LONGVARCHAR, Types.LONGNVARCHAR, Types.NVARCHAR, CFTypes.VARCHAR2 -> {
                stat.setObject(parameterIndex, Caster.toString(value), type)
                //// stat.setString(parameterIndex,Caster.toString(value));
                return
            }
            Types.DATE -> {
                try {
                    stat.setDate(parameterIndex, Date(Caster.toDate(value, tz).getTime()), JREDateTimeUtil.getThreadCalendar(tz))

                    // stat.setDate(parameterIndex,new Date((Caster.toDate(value,null).getTime())));
                } catch (pe: PageException) {
                    if (!fns && value is String && StringUtil.isEmpty(value as String)) stat.setNull(parameterIndex, item.getType()) else throw pe
                }
                return
            }
            Types.TIME -> {
                try {

                    // stat.setObject(parameterIndex, new Time((Caster.toDate(value,null).getTime())),
                    // type);
                    stat.setTime(parameterIndex, Time(Caster.toDate(value, tz).getTime()), JREDateTimeUtil.getThreadCalendar(tz))
                } catch (pe: PageException) {
                    if (!fns && value is String && StringUtil.isEmpty(value as String)) stat.setNull(parameterIndex, item.getType()) else throw pe
                }
                return
            }
            Types.TIMESTAMP -> {
                try {
                    // stat.setObject(parameterIndex, new
                    // Timestamp((Caster.toDate(value,null).getTime())), type);
                    // stat.setObject(parameterIndex, value, type);
                    stat.setTimestamp(parameterIndex, Timestamp(Caster.toDate(value, tz).getTime()), JREDateTimeUtil.getThreadCalendar(tz))
                } catch (pe: PageException) {
                    if (!fns && value is String && StringUtil.isEmpty(value as String)) stat.setNull(parameterIndex, item.getType()) else throw pe
                }
                return
            }
            Types.OTHER -> {
                stat.setObject(parameterIndex, value, Types.OTHER)
                return
            }
            else -> stat.setObject(parameterIndex, value, type)
        }
    }

    /**
     * Cast a SQL Item to a String (Display) Value
     *
     * @param item
     * @return String Value
     */
    fun toString(item: SQLItem?): String {
        return try {
            _toString(item)
        } catch (e: PageException) {
            try {
                "[" + toStringType(item.getType()) + "]"
            } catch (e1: DatabaseException) {
                ""
            }
        }
    }

    @Throws(PageException::class)
    private fun _toString(item: SQLItem?): String {
        val type: Int = item.getType()

        // string types
        return if (type == Types.VARCHAR || type == Types.LONGVARCHAR || type == Types.CHAR || type == Types.CLOB || type == Types.NVARCHAR || type == Types.NCHAR || type == Types.SQLXML || type == Types.NCLOB || type == Types.LONGNVARCHAR) {
            matchString(item)
        } else if (type == Types.BIGINT) {
            Caster.toString(Caster.toLongValue(item.getValue()))
        } else if (type == Types.INTEGER || type == Types.SMALLINT || type == Types.TINYINT) {
            Caster.toString(Caster.toIntValue(item.getValue()))
        } else if (type == Types.DECIMAL || type == Types.NUMERIC || type == Types.DOUBLE || type == Types.FLOAT) {
            Caster.toString(Caster.toDoubleValue(item.getValue()))
        } else if (type == Types.TIME) {
            TimeImpl(DateCaster.toDateAdvanced(item.getValue(), null)).castToString()
        } else if (type == Types.DATE) {
            DateImpl(DateCaster.toDateAdvanced(item.getValue(), null)).castToString()
        } else if (type == Types.TIMESTAMP) {
            DateCaster.toDateAdvanced(item.getValue(), null).castToString()
        } else {
            Caster.toString(item.getValue())
        }
    }
    /*
	 * private static String toString(Clob clob) throws SQLException, IOException { Reader in =
	 * clob.getCharacterStream(); StringBuilder buf = new StringBuilder(); for(int c=in.read();c != -1;c
	 * = in.read()) { buf.append((char)c); } return buf.toString(); }
	 */
    /**
     * cast a Value to a correspondance CF Type
     *
     * @param item
     * @return cf type
     * @throws PageException
     */
    @Throws(PageException::class)
    fun toCFTypex(item: SQLItem): Object {
        return try {
            _toCFTypex(item)
        } catch (e: PageException) {
            if (item.isNulls()) return item.getValue()
            throw e
        }
    }

    fun toCFTypeEL(item: SQLItem): Object {
        return try {
            _toCFTypex(item)
        } catch (e: PageException) {
            item.getValue()
        }
    }

    @Throws(PageException::class)
    private fun _toCFTypex(item: SQLItem): Object {
        val type: Int = item.getType()
        // char varchar
        return if (type == Types.VARCHAR || type == Types.LONGVARCHAR || type == CFTypes.VARCHAR2 || type == Types.NVARCHAR || type == Types.LONGNVARCHAR || type == Types.SQLXML) {
            Caster.toString(item.getValue())
        } else if (type == Types.CHAR || type == Types.NCHAR) {
            Caster.toString(item.getValue())
        } else if (type == Types.BIGINT) {
            Caster.toLong(item.getValue())
        } else if (type == Types.INTEGER || type == Types.SMALLINT || type == Types.TINYINT) {
            Caster.toInteger(item.getValue())
        } else if (type == Types.DOUBLE || type == Types.FLOAT || type == Types.NUMERIC || type == Types.DECIMAL) {
            Caster.toDouble(item.getValue())
        } else if (type == Types.TIME) {
            TimeImpl(DateCaster.toDateAdvanced(item.getValue(), null)).castToString()
        } else if (type == Types.DATE) {
            DateImpl(DateCaster.toDateAdvanced(item.getValue(), null)).castToString()
        } else if (type == Types.TIMESTAMP) {
            DateCaster.toDateAdvanced(item.getValue(), null).castToString()
        } else {
            item.getValue()
        }
    }

    fun toCFType(value: Object, defaultValue: Object): Object {
        return try {
            if (value is Clob) {
                IOUtil.toString((value as Clob).getCharacterStream())
            } else if (value is Blob) {
                IOUtil.toBytes((value as Blob).getBinaryStream())
            } else if (value is Array) {
                (value as Array).getArray()
            } else value
        } catch (e: Exception) {
            defaultValue
        }
    }

    @Throws(PageException::class)
    fun toCFType(value: Object): Object {
        return try {
            if (value is Clob) {
                IOUtil.toString((value as Clob).getCharacterStream())
            } else if (value is Blob) {
                IOUtil.toBytes((value as Blob).getBinaryStream())
            } else if (value is Array) {
                (value as Array).getArray()
            } else if (value is ResultSet) {
                QueryImpl(value as ResultSet, "query", null)
            } else value
        } catch (e: SQLException) {
            throw DatabaseException(e, null)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            throw Caster.toPageException(t)
        }
    }

    @Throws(PageException::class)
    fun toCFType(value: Object, type: Int): Object {
        // char varchar
        return if (type == Types.VARCHAR || type == Types.LONGVARCHAR || type == CFTypes.VARCHAR2 || type == Types.NVARCHAR || type == Types.LONGNVARCHAR || type == Types.SQLXML) {
            Caster.toString(value)
        } else if (type == Types.CHAR || type == Types.NCHAR) {
            Caster.toString(value)
        } else if (type == Types.BIGINT) {
            Caster.toLong(value)
        } else if (type == Types.INTEGER || type == Types.SMALLINT || type == Types.TINYINT) {
            Caster.toInteger(value)
        } else if (type == Types.DOUBLE || type == Types.FLOAT || type == Types.NUMERIC || type == Types.DECIMAL) {
            Caster.toDouble(value)
        } else if (type == Types.TIME) {
            TimeImpl(DateCaster.toDateAdvanced(value, null)).castToString()
        } else if (type == Types.DATE) {
            DateImpl(DateCaster.toDateAdvanced(value, null)).castToString()
        } else if (type == Types.TIMESTAMP) {
            DateCaster.toDateAdvanced(value, null).castToString()
        } else {
            value
        }
    }

    @Throws(PageException::class)
    private fun matchString(item: SQLItem): String {
        val str: String = StringUtil.replace(Caster.toString(item.getValue()), "'", "''", false)
        return "'$str'"
    }

    /**
     * returns CF SQL Type as String
     *
     * @param type
     * @return SQL Type as String
     * @throws DatabaseException
     */
    fun toStringType(type: Int, defaultValue: String?): String? {
        return when (type) {
            Types.ARRAY -> "CF_SQL_ARRAY"
            Types.BIGINT -> "CF_SQL_BIGINT"
            Types.BINARY -> "CF_SQL_BINARY"
            Types.BIT -> "CF_SQL_BIT"
            Types.BOOLEAN -> "CF_SQL_BOOLEAN"
            Types.BLOB -> "CF_SQL_BLOB"
            Types.CHAR -> "CF_SQL_CHAR"
            Types.NCLOB -> "CF_SQL_NCLOB"
            Types.SQLXML -> "CF_SQL_SQLXML"
            Types.NCHAR -> "CF_SQL_NCHAR"
            Types.CLOB -> "CF_SQL_CLOB"
            Types.DATALINK -> "CF_SQL_DATALINK"
            Types.DATE -> "CF_SQL_DATE"
            Types.DISTINCT -> "CF_SQL_DISTINCT"
            Types.NUMERIC -> "CF_SQL_NUMERIC"
            Types.DECIMAL -> "CF_SQL_DECIMAL"
            Types.DOUBLE -> "CF_SQL_DOUBLE"
            Types.REAL -> "CF_SQL_REAL"
            Types.FLOAT -> "CF_SQL_FLOAT"
            Types.TINYINT -> "CF_SQL_TINYINT"
            Types.SMALLINT -> "CF_SQL_SMALLINT"
            Types.STRUCT -> "CF_SQL_STRUCT"
            Types.INTEGER -> "CF_SQL_INTEGER"
            Types.VARCHAR -> "CF_SQL_VARCHAR"
            Types.NVARCHAR -> "CF_SQL_NVARCHAR"
            Types.LONGNVARCHAR -> "CF_SQL_LONGNVARCHAR"
            CFTypes.VARCHAR2 -> "CF_SQL_VARCHAR2"
            Types.LONGVARBINARY -> "CF_SQL_LONGVARBINARY"
            Types.VARBINARY -> "CF_SQL_VARBINARY"
            Types.LONGVARCHAR -> "CF_SQL_LONGVARCHAR"
            Types.TIME -> "CF_SQL_TIME"
            Types.TIMESTAMP -> "CF_SQL_TIMESTAMP"
            Types.REF -> "CF_SQL_REF"
            CFTypes.CURSOR -> "CF_SQL_REFCURSOR"
            Types.OTHER -> "CF_SQL_OTHER"
            Types.NULL -> "CF_SQL_NULL"
            else -> null
        }
    }

    @Throws(DatabaseException::class)
    fun toStringType(type: Int): String {
        val rtn = toStringType(type, null)
        if (rtn != null) return rtn
        throw DatabaseException("invalid CF SQL Type", null, null, null)
    }
    /*
	 * * cast a String SQL Type to int Type
	 * 
	 * @param strType
	 * 
	 * @return SQL Type as int
	 * 
	 * @throws DatabaseException
	 */
    /*
	 * public static int cfSQLTypeToIntType(String strType) throws DatabaseException {
	 * strType=strType.toUpperCase().trim();
	 * 
	 * if(strType.equals("CF_SQL_ARRAY")) return Types.ARRAY; else if(strType.equals("CF_SQL_BIGINT"))
	 * return Types.BIGINT; else if(strType.equals("CF_SQL_BINARY")) return Types.BINARY; else
	 * if(strType.equals("CF_SQL_BIT")) return Types.BIT; else if(strType.equals("CF_SQL_BLOB")) return
	 * Types.BLOB; else if(strType.equals("CF_SQL_BOOLEAN")) return Types.BOOLEAN; else
	 * if(strType.equals("CF_SQL_CHAR")) return Types.CHAR; else if(strType.equals("CF_SQL_CLOB"))
	 * return Types.CLOB; else if(strType.equals("CF_SQL_DATALINK")) return Types.DATALINK; else
	 * if(strType.equals("CF_SQL_DATE")) return Types.DATE; else if(strType.equals("CF_SQL_DISTINCT"))
	 * return Types.DISTINCT; else if(strType.equals("CF_SQL_DECIMAL")) return Types.DECIMAL; else
	 * if(strType.equals("CF_SQL_DOUBLE")) return Types.DOUBLE; else if(strType.equals("CF_SQL_FLOAT"))
	 * return Types.FLOAT; else if(strType.equals("CF_SQL_IDSTAMP")) return CFTypes.IDSTAMP; else
	 * if(strType.equals("CF_SQL_INTEGER")) return Types.INTEGER; else if(strType.equals("CF_SQL_INT"))
	 * return Types.INTEGER; else if(strType.equals("CF_SQL_LONGVARBINARY"))return Types.LONGVARBINARY;
	 * else if(strType.equals("CF_SQL_LONGVARCHAR"))return Types.LONGVARCHAR; else
	 * if(strType.equals("CF_SQL_MONEY")) return Types.DOUBLE; else if(strType.equals("CF_SQL_MONEY4"))
	 * return Types.DOUBLE; else if(strType.equals("CF_SQL_NUMERIC")) return Types.NUMERIC; else
	 * if(strType.equals("CF_SQL_NULL")) return Types.NULL; else if(strType.equals("CF_SQL_REAL"))
	 * return Types.REAL; else if(strType.equals("CF_SQL_REF")) return Types.REF; else
	 * if(strType.equals("CF_SQL_REFCURSOR")) return CFTypes.CURSOR; else
	 * if(strType.equals("CF_SQL_OTHER")) return Types.OTHER; else if(strType.equals("CF_SQL_SMALLINT"))
	 * return Types.SMALLINT; else if(strType.equals("CF_SQL_STRUCT")) return Types.STRUCT; else
	 * if(strType.equals("CF_SQL_TIME")) return Types.TIME; else if(strType.equals("CF_SQL_TIMESTAMP"))
	 * return Types.TIMESTAMP; else if(strType.equals("CF_SQL_TINYINT")) return Types.TINYINT; else
	 * if(strType.equals("CF_SQL_VARBINARY")) return Types.VARBINARY; else
	 * if(strType.equals("CF_SQL_VARCHAR")) return Types.VARCHAR; else
	 * if(strType.equals("CF_SQL_NVARCHAR")) return Types.NVARCHAR; else
	 * if(strType.equals("CF_SQL_VARCHAR2")) return CFTypes.VARCHAR2;
	 * 
	 * 
	 * else throw new DatabaseException("invalid CF SQL Type ["+strType+"]",null,null,null); }
	 */
    /**
     * cast a String SQL Type, e.g. from cfqueryparam, to int Type
     *
     * @param strType
     * @return SQL Type as int
     * @throws DatabaseException
     */
    @Throws(DatabaseException::class)
    fun toSQLType(strType: String): Int {
        var strType = strType
        strType = strType.toUpperCase().trim()
        if (strType.startsWith("CF_SQL_")) strType = strType.substring(7)
        if (strType.startsWith("SQL_")) strType = strType.substring(4)
        if (strType.length() > 2) {
            val first: Char = strType.charAt(0)
            if (first == 'A') {
                if (strType.equals("ARRAY")) return Types.ARRAY
            } else if (first == 'B') {
                if (strType.equals("BIGINT")) return Types.BIGINT else if (strType.equals("BINARY")) return Types.BINARY else if (strType.equals("BIT")) return Types.BIT else if (strType.equals("BLOB")) return Types.BLOB else if (strType.equals("BOOLEAN")) return Types.BOOLEAN else if (strType.equals("BOOL")) return Types.BOOLEAN
            } else if (first == 'C') {
                if (strType.equals("CLOB")) return Types.CLOB else if (strType.equals("CHAR")) return Types.CHAR else if (strType.equals("CLOB")) return Types.CLOB else if (strType.equals("CURSOR")) return CFTypes.CURSOR
            } else if (first == 'D') {
                if (strType.equals("DATALINK")) return Types.DATALINK else if (strType.equals("DATE")) return Types.DATE else if (strType.equals("DATETIME")) return Types.TIMESTAMP else if (strType.equals("DISTINCT")) return Types.DISTINCT else if (strType.equals("DECIMAL")) return Types.DECIMAL else if (strType.equals("DOUBLE")) return Types.DOUBLE
            } else if (first == 'F') {
                if (strType.equals("FLOAT")) return Types.FLOAT
            } else if (first == 'I') {
                if (strType.equals("IDSTAMP")) return CFTypes.IDSTAMP else if (strType.equals("INTEGER")) return Types.INTEGER else if (strType.equals("INT")) return Types.INTEGER
            } else if (first == 'L') {
                // if(strType.equals("LONG"))return Types.INTEGER;
                if (strType.equals("LONGVARBINARY")) return Types.LONGVARBINARY else if (strType.equals("LONGVARCHAR")) return Types.LONGVARCHAR else if (strType.equals("LONGNVARCHAR")) return Types.LONGNVARCHAR
            } else if (first == 'M') {
                if (strType.equals("MONEY")) return Types.DOUBLE else if (strType.equals("MONEY4")) return Types.DOUBLE
            } else if (first == 'N') {
                if (strType.equals("NUMERIC")) return Types.NUMERIC else if (strType.equals("NUMBER")) return Types.NUMERIC else if (strType.equals("NULL")) return Types.NULL else if (strType.equals("NCHAR")) return Types.NCHAR else if (strType.equals("NCLOB")) return Types.NCLOB else if (strType.equals("NVARCHAR")) return Types.NVARCHAR
            } else if (first == 'O') {
                if (strType.equals("OTHER")) return Types.OTHER else if ("OBJECT".equals(strType)) return Types.OTHER
            } else if (first == 'R') {
                if (strType.equals("REAL")) return Types.REAL else if (strType.equals("REF")) return Types.REF else if (strType.equals("REFCURSOR")) return CFTypes.CURSOR
            } else if (first == 'S') {
                if (strType.equals("SMALLINT")) return Types.SMALLINT else if (strType.equals("STRUCT")) return Types.STRUCT else if (strType.equals("STRING")) return Types.VARCHAR else if (strType.equals("SQLXML")) return Types.SQLXML
            } else if (first == 'T') {
                if (strType.equals("TEXT")) return Types.VARCHAR else if (strType.equals("TIME")) return Types.TIME else if (strType.equals("TIMESTAMP")) return Types.TIMESTAMP else if (strType.equals("TINYINT")) return Types.TINYINT
            } else if (first == 'V') {
                if (strType.equals("VARBINARY")) return Types.VARBINARY else if (strType.equals("VARCHAR")) return Types.VARCHAR else if (strType.equals("VARCHAR2")) return CFTypes.VARCHAR2
            }
        }
        throw DatabaseException("invalid CF SQL Type [$strType]", null, null, null)
    }

    fun toCFType(sqlType: Int, defaultValue: Short): Short {
        return when (sqlType) {
            Types.ARRAY -> tachyon.commons.lang.CFTypes.TYPE_ARRAY
            Types.BIGINT -> tachyon.commons.lang.CFTypes.TYPE_NUMERIC
            Types.LONGVARBINARY, Types.VARBINARY, Types.BLOB, Types.BINARY -> tachyon.commons.lang.CFTypes.TYPE_BINARY
            Types.BOOLEAN, Types.BIT -> tachyon.commons.lang.CFTypes.TYPE_BOOLEAN
            Types.LONGVARCHAR, Types.NVARCHAR, CFTypes.VARCHAR2, Types.VARCHAR, Types.CLOB, Types.CHAR, Types.NCLOB, Types.LONGNVARCHAR, Types.NCHAR -> tachyon.commons.lang.CFTypes.TYPE_STRING
            Types.SQLXML -> tachyon.commons.lang.CFTypes.TYPE_XML
            Types.TIME, Types.TIMESTAMP, Types.DATE -> tachyon.commons.lang.CFTypes.TYPE_DATETIME
            Types.INTEGER, Types.SMALLINT, Types.TINYINT, Types.FLOAT, Types.REAL, Types.DOUBLE, Types.DECIMAL, Types.NUMERIC -> tachyon.commons.lang.CFTypes.TYPE_NUMERIC
            else -> defaultValue
        }
    }
}