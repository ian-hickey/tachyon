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
package lucee.runtime.util

import lucee.runtime.PageContext

/**
 * Query Stack
 */
interface QueryStack {
    /**
     * adds a Query to the Stack
     *
     * @param query Query
     */
    fun addQuery(query: Query?)

    /**
     * removes a Query from Stack
     */
    fun removeQuery()

    /**
     * @return returns if stack is empty or not
     */
    val isEmpty: Boolean

    /**
     * loop over all Queries and return value at first occurrence
     *
     * @param pc Page Context
     * @param key column name of the value to get
     * @param defaultValue default value
     * @return value
     */
    fun getDataFromACollection(pc: PageContext?, key: Key?, defaultValue: Object?): Object?

    /**
     * loop over all Queries and return value as QueryColumn at first occurrence
     *
     * @param key column name of the value to get
     * @return value
     */
    fun getColumnFromACollection(key: Collection.Key?): QueryColumn?

    /**
     * clear the collection stack
     */
    fun clear()

    /**
     * @return returns all queries in the stack
     */
    val queries: Array<Any?>?
    fun duplicate(deepCopy: Boolean): QueryStack?
}