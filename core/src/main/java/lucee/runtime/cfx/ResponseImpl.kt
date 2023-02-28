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
package lucee.runtime.cfx

import java.io.IOException

/**
 *
 */
class ResponseImpl(pc: PageContext?, debug: Boolean) : Response {
    private val pc: PageContext?
    private val debug: Boolean
    @Override
    fun addQuery(name: String?, column: Array<String?>?): Query? {
        val query: lucee.runtime.type.Query = QueryImpl(column, 0, name)
        try {
            pc.setVariable(name, query)
        } catch (e: PageException) {
        }
        return QueryWrap(query)
    }

    @Override
    fun setVariable(key: String?, value: String?) {
        try {
            pc.setVariable(key, value)
        } catch (e: PageException) {
        }
    }

    @Override
    fun write(str: String?) {
        try {
            pc.write(str)
        } catch (e: IOException) {
        }
    }

    @Override
    fun writeDebug(str: String?) {
        if (debug) write(str)
    }

    /**
     * @param pc
     * @param debug
     */
    init {
        this.pc = pc
        this.debug = debug
    }
}