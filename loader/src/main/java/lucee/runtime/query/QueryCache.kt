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
package lucee.runtime.query

import java.io.IOException

/**
 * interface for a query cache
 */
interface QueryCache {
    /**
     * clear expired queries from cache
     * @param pc page context
     * @throws IOException IO Exception
     */
    @Throws(IOException::class)
    fun clearUnused(pc: PageContext?)

    /**
     * returns a Query from Query Cache or null if no match found
     *
     * @param pc page context
     * @param sql sql
     * @param datasource datasource
     * @param username username
     * @param password password
     * @param cacheAfter cache after
     * @return Query
     */
    fun getQuery(pc: PageContext?, sql: SQL?, datasource: String?, username: String?, password: String?, cacheAfter: Date?): Query?

    /**
     * sets a Query to Cache
     *
     * @param pc page context
     * @param sql sql
     * @param datasource datasource
     * @param username username
     * @param password password
     * @param value value
     * @param cacheBefore cache before
     */
    operator fun set(pc: PageContext?, sql: SQL?, datasource: String?, username: String?, password: String?, value: Object?, cacheBefore: Date?)

    /**
     * clear the cache
     *
     * @param pc page context
     */
    fun clear(pc: PageContext?)

    /**
     * clear the cache
     *
     * @param pc page context
     * @param filter filter
     */
    fun clear(pc: PageContext?, filter: QueryCacheFilter?)

    /**
     * removes query from cache
     *
     * @param pc page context
     * @param sql sql
     * @param datasource datasource
     * @param username username
     * @param password password
     */
    fun remove(pc: PageContext?, sql: SQL?, datasource: String?, username: String?, password: String?)
    operator fun get(pc: PageContext?, sql: SQL?, datasource: String?, username: String?, password: String?, cachedafter: Date?): Object?
    fun size(pc: PageContext?): Int
}