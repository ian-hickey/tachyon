/**
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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
package lucee.runtime.cache.tag

import lucee.runtime.PageContext

class CacheHandlerCollections(cw: ConfigWeb?) {
    val query: CacheHandlerCollectionImpl?
    val function: CacheHandlerCollectionImpl?
    val include: CacheHandlerCollectionImpl?
    val resource: CacheHandlerCollectionImpl?
    val http: CacheHandlerCollectionImpl?
    val file: CacheHandlerCollectionImpl?
    val webservice: CacheHandlerCollectionImpl?
    fun releaseCacheHandlers(pc: PageContext?) {
        try {
            query!!.release(pc)
        } catch (e: PageException) {
        }
        try {
            function!!.release(pc)
        } catch (e: PageException) {
        }
        try {
            include!!.release(pc)
        } catch (e: PageException) {
        }
        try {
            resource!!.release(pc)
        } catch (e: PageException) {
        }
        try {
            http!!.release(pc)
        } catch (e: PageException) {
        }
        try {
            file!!.release(pc)
        } catch (e: PageException) {
        }
        try {
            webservice!!.release(pc)
        } catch (e: PageException) {
        }
    }

    init {
        query = CacheHandlerCollectionImpl(cw, ConfigPro.CACHE_TYPE_QUERY)
        function = CacheHandlerCollectionImpl(cw, ConfigPro.CACHE_TYPE_FUNCTION)
        include = CacheHandlerCollectionImpl(cw, ConfigPro.CACHE_TYPE_INCLUDE)
        resource = CacheHandlerCollectionImpl(cw, ConfigPro.CACHE_TYPE_RESOURCE)
        http = CacheHandlerCollectionImpl(cw, ConfigPro.CACHE_TYPE_HTTP)
        file = CacheHandlerCollectionImpl(cw, ConfigPro.CACHE_TYPE_FILE)
        webservice = CacheHandlerCollectionImpl(cw, ConfigPro.CACHE_TYPE_WEBSERVICE)
    }
}