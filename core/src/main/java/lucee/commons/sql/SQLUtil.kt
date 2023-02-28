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
package lucee.commons.sql

import java.sql.Blob

object SQLUtil {
    private const val ESCAPE_CHARS = "\\{}[]^$*.?+"
    fun pattern(pstr: String, ignoreCase: Boolean): Pattern {
        val carr: CharArray = pstr.toCharArray()
        var c: Char
        val sb = StringBuilder()
        for (i in carr.indices) {
            c = carr[i]
            if (ESCAPE_CHARS.indexOf(c) !== -1) {
                sb.append('\\')
                sb.append(c)
            } else if (c == '%') {
                sb.append(".*")
            } else if (c == '_') {
                sb.append(".")
            } else {
                if (ignoreCase) {
                    sb.append('[')
                    sb.append(Character.toLowerCase(c))
                    sb.append('|')
                    sb.append(Character.toUpperCase(c))
                    sb.append(']')
                } else sb.append(c)
            }
        }
        return Pattern.compile(sb.toString())
    }

    fun match(pattern: Pattern, string: String?): Boolean {
        return pattern.matcher(string).matches()
    }

    fun removeLiterals(sql: String): String {
        return if (StringUtil.isEmpty(sql)) sql else removeLiterals(ParserString(sql), true)
    }

    private fun removeLiterals(ps: ParserString, escapeMysql: Boolean): String {
        val sb = StringBuilder()
        var c: Char
        var p = 0.toChar()
        var inside = false
        do {
            c = ps.getCurrent()
            if (c == '\'') {
                if (inside) {
                    if (escapeMysql && p == '\\') {
                    } else if (ps.hasNext() && ps.getNext() === '\'') ps.next() else inside = false
                } else {
                    inside = true
                }
            } else {
                if (!inside && c != '*' && c != '=' && c != '?') sb.append(c)
            }
            p = c
            ps.next()
        } while (!ps.isAfterLast())
        if (inside && escapeMysql) {
            ps.setPos(0)
            return removeLiterals(ps, false)
        }
        return sb.toString()
    }

    /**
     * create a blog Object
     *
     * @param conn
     * @param value
     * @return
     * @throws PageException
     * @throws SQLException
     */
    @Throws(PageException::class, SQLException::class)
    fun toBlob(conn: Connection, value: Object?): Blob? {
        if (value is Blob) return value as Blob?

        // Java >= 1.6
        if (SystemUtil.JAVA_VERSION >= SystemUtil.JAVA_VERSION_6) {
            return try {
                val blob: Blob = conn.createBlob()
                blob.setBytes(1, Caster.toBinary(value))
                blob
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
                BlobImpl.toBlob(value)
            }
        }

        // Java < 1.6
        if (isOracle(conn)) {
            val blob: Blob = OracleBlob.createBlob(conn, Caster.toBinary(value), null)
            if (blob != null) return blob
        }
        return BlobImpl.toBlob(value)
    }

    /**
     * create a clob Object
     *
     * @param conn
     * @param value
     * @return
     * @throws PageException
     * @throws SQLException
     */
    @Throws(PageException::class, SQLException::class)
    fun toClob(conn: Connection, value: Object?): Clob? {
        if (value is Clob) return value as Clob?
        // Java >= 1.6
        if (SystemUtil.JAVA_VERSION >= SystemUtil.JAVA_VERSION_6) {
            val clob: Clob = conn.createClob()
            clob.setString(1, Caster.toString(value))
            return clob
        }

        // Java < 1.6
        if (isOracle(conn)) {
            val clob: Clob = OracleClob.createClob(conn, Caster.toString(value), null)
            if (clob != null) return clob
        }
        return ClobImpl.toClob(value)
    }

    fun isOracle(conn: Connection): Boolean {
        var conn: Connection = conn
        if (conn is ConnectionProxy) conn = (conn as ConnectionProxy).getConnection()
        return StringUtil.indexOfIgnoreCase(conn.getClass().getName(), "oracle") !== -1
    }

    fun isTeradata(conn: Connection): Boolean {
        var conn: Connection = conn
        if (conn is ConnectionProxy) conn = (conn as ConnectionProxy).getConnection()
        return StringUtil.indexOfIgnoreCase(conn.getClass().getName(), "teradata") !== -1
    }

    fun closeEL(stat: Statement?) {
        if (stat != null) {
            try {
                stat.close()
            } catch (e: SQLException) {
            }
        }
    }

    fun closeEL(conn: Connection?) {
        if (conn != null) {
            try {
                conn.close()
            } catch (e: SQLException) {
            }
        }
    }

    fun closeEL(rs: ResultSet?) {
        if (rs != null) {
            try {
                rs.close()
            } catch (e: SQLException) {
            }
        }
    }

    fun connectionStringTranslatedPatch(config: Config?, connStr: String?): String? {
        if (connStr == null || !StringUtil.startsWithIgnoreCase(connStr, "jdbc:mysql://")) return connStr

        // MySQL
        if (StringUtil.indexOfIgnoreCase(connStr, "serverTimezone=") !== -1) {
            return connStr
        }
        val del = if (connStr.indexOf('?') !== -1) '&' else '?'
        return connStr + del + "serverTimezone=" + TimeZoneUtil.toString(ThreadLocalPageContext.getTimeZone(config))
    }
}