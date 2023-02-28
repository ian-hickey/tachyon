/**
 * Copyright (c) 2015, Tachyon Assosication Switzerland. All rights reserved.
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
package tachyon.runtime.cache.tag

import tachyon.runtime.PageContext

interface CacheHandler {
    @Throws(PageException::class)
    fun init(cw: ConfigWeb?, id: String?, cacheType: Int)
    fun id(): String?

    @Throws(PageException::class)
    operator fun get(pc: PageContext?, id: String?): CacheItem?

    @Throws(PageException::class)
    fun remove(pc: PageContext?, id: String?): Boolean

    @Throws(PageException::class)
    operator fun set(pc: PageContext?, id: String?, cachedwithin: Object?, value: CacheItem?)

    @Throws(PageException::class)
    fun clear(pc: PageContext?)

    @Throws(PageException::class)
    fun clear(pc: PageContext?, filter: CacheHandlerFilter?)

    @Throws(PageException::class)
    fun clean(pc: PageContext?)

    @Throws(PageException::class)
    fun size(pc: PageContext?): Int

    @Throws(PageException::class)
    fun release(pc: PageContext?)
    fun acceptCachedWithin(cachedWithin: Object?): Boolean

    /**
     * return a pattern for that handler, for example "request" or "{time-span}"
     *
     * @return Returns the pattern.
     */
    fun pattern(): String?
}