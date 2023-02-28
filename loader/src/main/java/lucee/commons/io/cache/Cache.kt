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
package lucee.commons.io.cache

import java.io.IOException

interface Cache {
    /**
     * initialize the cache
     *
     * @param config Lucee configuration
     * @param cacheName name of the cache
     * @param arguments configuration arguments
     * @throws IOException thrown when fail to execute
     */
    @Throws(IOException::class)
    fun init(config: Config?, cacheName: String?, arguments: Struct?)
    // FUTURE public void release() throws IOException;
    /**
     * return cache entry that match the key, throws a CacheException when entry does not exist or is
     * stale
     *
     * @param key key name to get an entry for
     * @return matching cache entry
     * @throws IOException thrown when fail to execute
     */
    @Throws(IOException::class)
    fun getCacheEntry(key: String?): CacheEntry?

    /**
     * return value that match the key, throws a CacheException when entry does not exist or is stale
     *
     * @param key key of the value to get
     * @throws IOException in case action fails
     * @return cache entry
     */
    @Throws(IOException::class)
    fun getValue(key: String?): Object?

    /**
     * return cache entry that match the key or the defaultValue when entry does not exist
     *
     * @param key key of the cache entry to get
     * @param defaultValue returned in case there is no entry or the cache fails to reach it
     * @return cache entry
     */
    fun getCacheEntry(key: String?, defaultValue: CacheEntry?): CacheEntry?

    /**
     * return value that match the key or the defaultValue when entry does not exist
     *
     * @param key key of the value to get
     * @param defaultValue default value returned in case no value exist
     * @return value
     */
    operator fun getValue(key: String?, defaultValue: Object?): Object?

    /**
     * puts a cache entry to the cache, overwrite existing entries that already exists inside the cache
     * with the same key
     *
     * @param key name of the key
     * @param value value to add
     * @param idleTime idle time
     * @param until live time
     * @throws IOException in case action fails
     */
    @Throws(IOException::class)
    fun put(key: String?, value: Object?, idleTime: Long?, until: Long?)

    /**
     * check if there is an entry inside the cache that match the given key
     *
     * @param key name of the key
     * @return contains a value that match this key
     * @throws IOException in case action fails
     */
    @Throws(IOException::class)
    operator fun contains(key: String?): Boolean

    /**
     * remove entry that match this key
     *
     * @param key name of the key
     * @return returns if there was a removal
     * @throws IOException in case action fails
     */
    @Throws(IOException::class)
    fun remove(key: String?): Boolean

    /**
     * remove all entries that match the given filter
     *
     * @param filter filter for elements returned
     * @return returns the count of the removal or -1 if this information is not available
     * @throws IOException in case action fails
     */
    @Throws(IOException::class)
    fun remove(filter: CacheKeyFilter?): Int

    /**
     * remove all entries that match the given filter
     *
     * @param filter filter for elements returned
     * @return returns the count of the removal or -1 if this information is not available
     * @throws IOException in case action fails
     */
    @Throws(IOException::class)
    fun remove(filter: CacheEntryFilter?): Int

    /**
     *
     * Returns a List of the keys contained in this cache. The set is NOT backed by the cache, so
     * changes to the cache are NOT reflected in the set, and vice-versa.
     *
     * @return a set of the keys contained in this cache.
     * @throws IOException in case action fails
     */
    @Throws(IOException::class)
    fun keys(): List<String?>?

    /**
     *
     * Returns a List of the keys contained in this cache that match the given filter. The set is NOT
     * backed by the cache, so changes to the cache are NOT reflected in the set, and vice-versa.
     *
     * @param filter filter for elements returned
     * @return a set of the keys contained in this cache.
     * @throws IOException in case action fails
     */
    @Throws(IOException::class)
    fun keys(filter: CacheKeyFilter?): List<String?>?

    /**
     *
     * Returns a List of the keys contained in this cache that match the given filter. The set is NOT
     * backed by the cache, so changes to the cache are NOT reflected in the set, and vice-versa.
     *
     * @param filter filter for elements returned
     * @return a set of the keys contained in this cache.
     * @throws IOException in case action fails
     */
    @Throws(IOException::class)
    fun keys(filter: CacheEntryFilter?): List<String?>?

    /**
     * Returns a List of values containing in this cache. The set is NOT backed by the cache, so changes
     * to the cache are NOT reflected in the set, and vice-versa.
     *
     * @return a set of the entries contained in this cache.
     * @throws IOException in case action fails
     */
    @Throws(IOException::class)
    fun values(): List<Object?>?

    /**
     * Returns a list of values containing in this cache that match the given filter. The set is NOT
     * backed by the cache, so changes to the cache are NOT reflected in the set, and vice-versa.
     *
     * @param filter filter for elements returned
     * @return a set of the entries contained in this cache.
     * @throws IOException in case action fails
     */
    @Throws(IOException::class)
    fun values(filter: CacheKeyFilter?): List<Object?>?

    /**
     * Returns a list of values containing in this cache that match the given filter. The set is NOT
     * backed by the cache, so changes to the cache are NOT reflected in the set, and vice-versa.
     *
     * @param filter filter for elements returned
     * @return a set of the entries contained in this cache.
     * @throws IOException in case action fails
     */
    @Throws(IOException::class)
    fun values(filter: CacheEntryFilter?): List<Object?>?

    /**
     * Returns a List of entries containing in this cache Each element in the returned list is a
     * CacheEntry. The set is NOT backed by the cache, so changes to the cache are NOT reflected in the
     * set, and vice-versa.
     *
     * @return a set of the entries contained in this cache.
     * @throws IOException in case action fails
     */
    @Throws(IOException::class)
    fun entries(): List<CacheEntry?>?

    /**
     * Returns a list of entries containing in this cache that match the given filter. Each element in
     * the returned set is a CacheEntry. The set is NOT backed by the cache, so changes to the cache are
     * NOT reflected in the set, and vice-versa.
     *
     * @param filter filter for elements returned
     * @return a set of the entries contained in this cache.
     * @throws IOException in case action fails
     */
    @Throws(IOException::class)
    fun entries(filter: CacheKeyFilter?): List<CacheEntry?>?

    /**
     * Returns a list of entries containing in this cache that match the given filter. Each element in
     * the returned set is a CacheEntry. The set is NOT backed by the cache, so changes to the cache are
     * NOT reflected in the set, and vice-versa.
     *
     * @param filter filter for elements returned
     * @return a set of the entries contained in this cache.
     * @throws IOException in case action fails
     */
    @Throws(IOException::class)
    fun entries(filter: CacheEntryFilter?): List<CacheEntry?>?

    /**
     * how many time was the cache accessed? this information is optional and depends on the
     * implementation, when information is not available -1 is returned
     *
     * @return access count
     * @throws IOException in case action fails
     */
    @Throws(IOException::class)
    fun hitCount(): Long

    /**
     * how many time was the cache accessed for a record that does not exist? this information is
     * optional and depends on the implementation, when information is not available -1 is returned
     *
     * @return access count
     * @throws IOException in case action fails
     */
    @Throws(IOException::class)
    fun missCount(): Long

    /**
     * get all information data available for this cache
     *
     * @return custom info as a struct
     * @throws IOException in case action fails
     */
    @get:Throws(IOException::class)
    val customInfo: Struct?
}