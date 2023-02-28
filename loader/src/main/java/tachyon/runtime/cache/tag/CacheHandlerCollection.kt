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

import java.util.List

interface CacheHandlerCollection {
    // public static final int TYPE_TIMESPAN=1;
    // public static final int TYPE_REQUEST=2;
    // public static final int TYPE_SMART=4;
    /**
     * based on the cachedWithin Object we choose the right Cachehandler and return it
     *
     * @param cachedWithin cached within
     * @param defaultValue default value
     * @return Returns the matching Object.
     */
    fun getInstanceMatchingObject(cachedWithin: Object?, defaultValue: CacheHandler?): CacheHandler?

    /**
     *
     * @param cacheHandlerId id returned by CacheHandler.id() can be for example (request,timespan,...)
     * @param defaultValue default value
     * @return Returns the instance.
     */
    fun getInstance(cacheHandlerId: String?, defaultValue: CacheHandler?): CacheHandler?

    // public SmartCacheHandler getSmartCacheHandler();
    @Throws(PageException::class)
    fun size(pc: PageContext?): Int

    @Throws(PageException::class)
    fun clear(pc: PageContext?)

    @Throws(PageException::class)
    fun clear(pc: PageContext?, filter: CacheHandlerFilter?)

    @Throws(PageException::class)
    fun clean(pc: PageContext?)

    @Throws(PageException::class)
    fun remove(pageContext: PageContext?, id: String?)

    @Throws(PageException::class)
    fun release(pc: PageContext?)
    val patterns: List<String?>?
}