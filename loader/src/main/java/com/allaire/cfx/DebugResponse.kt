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
package com.allaire.cfx

import java.util.Enumeration

class DebugResponse : Response {
    private val write: StringBuffer = StringBuffer()
    private val writeDebug: StringBuffer = StringBuffer()
    private val variables: Hashtable = Hashtable()
    private val queries: Hashtable = Hashtable()
    @Override
    fun addQuery(name: String, columns: Array<String?>?): Query {
        val query = QueryWrap(CFMLEngineFactory.getInstance().getCreationUtil().createQuery(columns, 0, name), name.toLowerCase())
        queries.put(name.toLowerCase(), query)
        return query
    }

    @Override
    fun setVariable(key: String, value: String?) {
        variables.put(key.toLowerCase(), value)
    }

    @Override
    override fun write(str: String?) {
        write.append(str)
    }

    @Override
    override fun writeDebug(str: String?) {
        writeDebug.append(str)
    }

    /**
     * print out the response
     */
    fun printResults() {
        System.out.println("[ --- Tachyon Debug Response --- ]")
        System.out.println()
        System.out.println("----------------------------")
        System.out.println("|          Output          |")
        System.out.println("----------------------------")
        System.out.println(write)
        System.out.println()
        System.out.println("----------------------------")
        System.out.println("|       Debug Output       |")
        System.out.println("----------------------------")
        System.out.println(writeDebug)
        System.out.println()
        System.out.println("----------------------------")
        System.out.println("|        Variables         |")
        System.out.println("----------------------------")
        var e: Enumeration = variables.keys()
        while (e.hasMoreElements()) {
            val key: Object = e.nextElement()
            System.out.println("[Variable:$key]")
            System.out.println(escapeString(variables.get(key).toString()))
        }
        System.out.println()
        e = queries.keys()
        while (e.hasMoreElements()) {
            printQuery(queries.get(e.nextElement()))
            System.out.println()
        }
    }

    /**
     * print out a query
     *
     * @param query query to print
     */
    fun printQuery(query: Query?) {
        if (query != null) {
            val cols: Array<String> = query.getColumns()
            val rows: Int = query.getRowCount()
            System.out.println("[Query:" + query.getName().toString() + "]")
            for (i in cols.indices) {
                if (i > 0) System.out.print(", ")
                System.out.print(cols[i])
            }
            System.out.println()
            for (row in 1..rows) {
                for (col in 1..cols.size) {
                    if (col > 1) System.out.print(", ")
                    System.out.print(escapeString(query.getData(row, col)))
                }
                System.out.println()
            }
        }
    }

    private fun escapeString(string: String): String {
        val len: Int = string.length()
        val sb = StringBuffer(len)
        for (i in 0 until len) {
            val c: Char = string.charAt(i)
            if (c == '\n') sb.append("\\n") else if (c == '\t') sb.append("\\t") else if (c == '\\') sb.append("\\\\") else if (c == '\b') sb.append("\\b") else if (c == '\r') sb.append("\\r") else if (c == '\"') sb.append("\\\"") else sb.append(c)
        }
        return "\"" + sb.toString().toString() + "\""
    }
}