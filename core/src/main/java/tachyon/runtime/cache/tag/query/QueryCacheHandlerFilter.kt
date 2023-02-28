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
package tachyon.runtime.cache.tag.query

import tachyon.commons.io.res.util.WildCardFilter

class QueryCacheHandlerFilter(wildcard: String?, ignoreCase: Boolean) : CacheHandlerFilter {
    private val filter: WildCardFilter?
    @Override
    fun accept(obj: Object?): Boolean {
        val qry: Query
        qry = if (obj !is Query) {
            if (obj is QueryCacheItem) {
                (obj as QueryCacheItem?)!!.getQuery()
            } else return false
        } else obj as Query?
        val sql: String = qry.getSql().toString()
        val sb = StringBuilder()
        val text: CharArray = sql.toCharArray()
        for (i in text.indices) {
            if (text[i] == '\n' || text[i] == '\r') {
                sb.append(' ')
            } else sb.append(text[i])
        }
        return filter.accept(sb.toString())
    }

    init {
        filter = WildCardFilter(wildcard!!, ignoreCase)
    }
}