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
package lucee.runtime.debug

import java.io.Serializable

/**
 * a single query entry
 */
interface QueryEntry : Serializable {
    /**
     * @return return the query execution time in mili seconds
     */
    @get:Deprecated("use instead <code>getExecutionTime()</code>")
    @get:Deprecated
    val exe: Int

    /**
     * @return return the query execution time in nanoseconds
     */
    val executionTime: Long

    /**
     * @return Returns the query.
     */
    val sQL: SQL?

    /**
     * return the query of this entry (can be null, if the query has not produced a resultset)
     *
     * @return Returns the query.
     */
    val qry: Query?

    /**
     * @return Returns the src.
     */
    val src: String?

    /**
     * @return Returns the name.
     */
    val name: String?

    /**
     * @return Returns the recordcount.
     */
    val recordcount: Int

    /**
     * @return Returns the datasource.
     */
    val datasource: String?
    val startTime: Long
    val cacheType: String?
}