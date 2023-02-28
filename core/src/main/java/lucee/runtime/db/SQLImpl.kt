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
package lucee.runtime.db

import java.io.Serializable

/**
 * represents a SQL Statement with his defined arguments for a prepared statement
 */
class SQLImpl : SQL, Serializable {
    @get:Override
    @set:Override
    var sQLString: String
    private var items: Array<SQLItem?>
    private var position = 0

    /**
     * Constructor only with SQL String
     *
     * @param strSQL SQL String
     */
    constructor(strSQL: String) {
        sQLString = strSQL
        items = arrayOfNulls<SQLItem>(0)
    }

    /**
     * Constructor with SQL String and SQL Items
     *
     * @param strSQL SQL String
     * @param items SQL Items
     */
    constructor(strSQL: String, items: Array<SQLItem?>?) {
        sQLString = strSQL
        this.items = items ?: arrayOfNulls<SQLItem>(0)
    }

    fun addItems(item: SQLItem?) {
        val tmp: Array<SQLItem?> = arrayOfNulls<SQLItem>(items.size + 1)
        for (i in items.indices) {
            tmp[i] = items[i]
        }
        tmp[items.size] = item
        items = tmp
    }

    @Override
    fun getItems(): Array<SQLItem?> {
        return items
    }

    @Override
    fun getPosition(): Int {
        return position
    }

    @Override
    fun setPosition(position: Int) {
        this.position = position
    }

    /**
     * populates the SQL string with values from parameters
     *
     * @return
     */
    @Override
    override fun toString(): String {
        if (items.size == 0) return sQLString
        val sb = StringBuilder()
        val sqlLen: Int = sQLString.length()
        var c: Char
        var quoteType = 0.toChar()
        var p = 0.toChar()
        var inQuotes = false
        var index = 0
        var i = 0
        while (i < sqlLen) {
            c = sQLString.charAt(i)
            if (!inQuotes && sqlLen + 1 > i) {
                // read multi line
                if (c == '/' && sQLString.charAt(i + 1) === '*') {
                    val end: Int = sQLString.indexOf("*/", i + 2)
                    if (end != -1) {
                        i = end + 2
                        if (i == sqlLen) break
                        c = sQLString.charAt(i)
                    }
                }

                // read single line
                if (c == '-' && sQLString.charAt(i + 1) === '-') {
                    val end: Int = sQLString.indexOf('\n', i + 1)
                    if (end != -1) {
                        i = end + 1
                        if (i == sqlLen) break
                        c = sQLString.charAt(i)
                    } else break
                }
            }
            if (c == '"' || c == '\'') {
                if (inQuotes) {
                    if (c == quoteType) {
                        inQuotes = false
                    }
                } else {
                    quoteType = c
                    inQuotes = true
                }
                sb.append(c)
            } else if (!inQuotes && c == '?') {
                if (index + 1 > items.size) throw RuntimeException("there are more question marks in the SQL than params defined")
                if (items[index].isNulls()) sb.append("null") else sb.append(SQLCaster.toString(items[index]))
                index++
            } else {
                sb.append(c)
            }
            p = c
            i++
        }
        return sb.toString()
    }

    @Override
    fun toHashString(): String {
        if (items.size == 0) return sQLString
        val sb = StringBuilder(sQLString)
        for (i in items.indices) {
            sb.append(';').append(items[i].toString())
        }
        return sb.toString()
    }

    fun duplicate(): SQL {
        val rtn = SQLImpl(sQLString)
        rtn.position = position
        rtn.items = arrayOfNulls<SQLItem>(items.size)
        for (i in items.indices) {
            rtn.items[i] = SQLItemImpl.duplicate(items[i])
        }
        return rtn
    }

    companion object {
        fun duplicate(sql: SQL): SQL {
            return if (sql !is SQLImpl) sql else (sql as SQLImpl).duplicate()
        }
    }
}