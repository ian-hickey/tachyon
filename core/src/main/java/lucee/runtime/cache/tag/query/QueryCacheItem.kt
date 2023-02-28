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
package lucee.runtime.cache.tag.query

import lucee.commons.digest.HashUtil

class QueryCacheItem(query: Query?, tags: Array<String?>?, datasourceName: String?, cacheTime: Long) : QueryResultCacheItem(query as QueryResult?, tags, datasourceName, cacheTime) {
    val query: Query?
    private var hash: String? = null

    constructor(query: Query?, tags: Array<String?>?, datasourceName: String?) : this(query, tags, datasourceName, System.currentTimeMillis()) {}

    @Override
    fun getHashFromValue(): String? {
        // TODO faster impl
        if (hash == null) hash = toString(HashUtil.create64BitHash(UDFArgConverter.serialize(query)))
        return hash
    }

    fun getQuery(): Query? {
        return query
    }

    @Override
    fun duplicate(deepCopy: Boolean): Object? {
        return QueryCacheItem(query.duplicate(true) as Query, getTags(), getDatasourceName(), getCreationDate())
    }

    companion object {
        private const val serialVersionUID = 7327671003736543783L
    }

    init {
        this.query = query
    }
}