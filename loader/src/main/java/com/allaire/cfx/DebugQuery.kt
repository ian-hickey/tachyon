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

import tachyon.loader.engine.CFMLEngineFactory

/**
 * Implementation of the DebugQuery
 */
class DebugQuery : QueryWrap {
    /**
     * Constructor of the DebugQuery
     *
     * @param name query name
     * @param columns column names
     * @param data query data
     * @throws IllegalArgumentException thrown when arguments are invalid
     */
    constructor(name: String, columns: Array<String>, data: Array<Array<String>>) : super(toQuery(name, columns, data), name) {}

    /**
     * Constructor of the DebugQuery
     *
     * @param name query name
     * @param columns column names
     * @throws IllegalArgumentException thrown when arguments are invalid
     */
    constructor(name: String, columns: Array<String>) : super(toQuery(name, columns, 0), name) {}

    companion object {
        private fun toQuery(name: String, columns: Array<String>, data: Array<Array<String>>): tachyon.runtime.type.Query {
            val query: tachyon.runtime.type.Query = toQuery(name, columns, data.size)
            for (row in data.indices) {
                val len = if (data[row].length > columns.size) columns.size else data[row].length
                for (col in 0 until len) try {
                    query.setAt(columns[col], row + 1, data[row][col])
                } catch (e: Exception) {
                }
            }
            return query
        }

        private fun toQuery(name: String, columns: Array<String>, rows: Int): tachyon.runtime.type.Query {
            return CFMLEngineFactory.getInstance().getCreationUtil().createQuery(columns, rows, name)
        }
    }
}