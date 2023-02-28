/**
 * Copyright (c) 2023, TachyonCFML.org
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
package tachyon.runtime

import java.lang.ref.SoftReference

/**
 * pool to handle pages
 */
class PageSourcePool
/**
 * constructor of the class
 */
    : Dumpable {
    // TODO must not be thread safe, is used in sync block only
    private val pageSources: Map<String?, SoftReference<PageSource?>?>? = ConcurrentHashMap<String?, SoftReference<PageSource?>?>()

    // timeout timeout for files
    private val timeout: Long = 10000

    // max size of the pool cache
    private val maxSize = 1000

    /**
     * return pages matching to key
     *
     * @param key key for the page
     * @param updateAccesTime define if do update access time
     * @return page
     */
    fun getPageSource(key: String?, updateAccesTime: Boolean): PageSource? { // DO NOT CHANGE INTERFACE (used by Argus Monitor)
        val tmp: SoftReference<PageSource?>? = pageSources!![key.toLowerCase()]
        val ps: PageSource = (if (tmp == null) null else tmp.get()) ?: return null
        if (updateAccesTime) ps.setLastAccessTime()
        return ps
    }

    /**
     * sts a page object to the page pool
     *
     * @param key key reference to store page object
     * @param ps pagesource to store
     */
    fun setPage(key: String?, ps: PageSource?) {
        ps.setLastAccessTime()
        pageSources.put(key.toLowerCase(), SoftReference<PageSource?>(ps))
    }

    /**
     * returns if page object exists
     *
     * @param key key reference to a page object
     * @return has page object or not
     */
    fun exists(key: String?): Boolean {
        return pageSources!!.containsKey(key.toLowerCase())
    }

    /**
     * @return returns an array of all keys in the page pool
     */
    fun keys(): Array<String?>? {
        if (pageSources == null) return arrayOfNulls<String?>(0)
        val set: Set<String?> = pageSources.keySet()
        return set.toArray(arrayOfNulls<String?>(set.size()))
    }

    /**
     * removes a page from the page pool
     *
     * @param key key reference to page object
     * @return page object matching to key reference
     */
    /*
	 * private boolean remove(String key) {
	 * 
	 * if (pageSources.remove(key.toLowerCase()) != null) return true;
	 * 
	 * Set<String> set = pageSources.keySet(); String[] keys = set.toArray(new String[set.size()]); //
	 * done this way to avoid ConcurrentModificationException SoftReference<PageSource> tmp; PageSource
	 * ps; for (String k: keys) { tmp = pageSources.get(k); ps = tmp == null ? null : tmp.get(); if (ps
	 * != null && key.equalsIgnoreCase(ps.getClassName())) { pageSources.remove(k); return true; } }
	 * return false; }
	 */
    fun flushPage(key: String?): Boolean {
        val tmp: SoftReference<PageSource?>? = pageSources!![key.toLowerCase()]
        var ps: PageSource? = if (tmp == null) null else tmp.get()
        if (ps != null) {
            (ps as PageSourceImpl?)!!.flush()
            return true
        }
        val it: Iterator<SoftReference<PageSource?>?> = pageSources.values().iterator()
        while (it.hasNext()) {
            ps = it.next().get()
            if (key.equalsIgnoreCase(ps.getClassName())) {
                (ps as PageSourceImpl?)!!.flush()
                return true
            }
        }
        return false
    }

    /**
     * @return returns the size of the pool
     */
    fun size(): Int {
        return pageSources!!.size()
    }

    /**
     * @return returns if pool is empty or not
     */
    fun isEmpty(): Boolean {
        return pageSources!!.isEmpty()
    }

    /**
     * clear unused pages from page pool
     */
    fun clearUnused(config: Config?) {
        if (size() > maxSize) {
            LogUtil.log(config, Log.LEVEL_INFO, PageSourcePool::class.java.getName(), "PagePool size [" + size() + "] has exceeded max size [" + maxSize + "]. Clearing unused...")
            val keys = keys()
            val list = LongKeyList()
            for (i in keys.indices) {
                val ps: PageSource? = getPageSource(keys!![i], false)
                val updateTime: Long = ps.getLastAccessTime()
                if (updateTime + timeout < System.currentTimeMillis()) {
                    var add: Long = (ps.getAccessCount() - 1) * 10000
                    if (add > timeout) add = timeout
                    list.add(updateTime + add, keys[i])
                }
            }
            while (size() > maxSize) {
                val key: Object = list.shift() ?: break
                // remove(key.toString());
            }
            LogUtil.log(config, Log.LEVEL_INFO, PageSourcePool::class.java.getName(), "New pagePool size [" + size() + "].")
        }
    }

    @Override
    fun toDumpData(pageContext: PageContext?, maxlevel: Int, dp: DumpProperties?): DumpData? {
        var maxlevel = maxlevel
        maxlevel--
        val it: Iterator<SoftReference<PageSource?>?> = pageSources!!.values().iterator()
        val table = DumpTable("#FFCC00", "#FFFF00", "#000000")
        table.setTitle("Page Source Pool")
        table.appendRow(1, SimpleDumpData("Count"), SimpleDumpData(pageSources.size()))
        while (it.hasNext()) {
            val ps: PageSource = it.next().get()
            val inner = DumpTable("#FFCC00", "#FFFF00", "#000000")
            inner.setWidth("100%")
            inner.appendRow(1, SimpleDumpData("source"), SimpleDumpData(ps.getDisplayPath()))
            inner.appendRow(1, SimpleDumpData("last access"), DumpUtil.toDumpData(DateTimeImpl(pageContext, ps.getLastAccessTime(), false), pageContext, maxlevel, dp))
            inner.appendRow(1, SimpleDumpData("access count"), SimpleDumpData(ps.getAccessCount()))
            table.appendRow(1, SimpleDumpData("Sources"), inner)
        }
        return table
    }

    /**
     * remove all Page from Pool using this classloader
     *
     * @param cl
     */
    fun clearPages(cl: ClassLoader?) {
        val it: Iterator<SoftReference<PageSource?>?> = pageSources!!.values().iterator()
        var psi: PageSourceImpl?
        var sr: SoftReference<PageSource?>?
        while (it.hasNext()) {
            sr = it.next()
            psi = if (sr == null) null else sr.get()
            if (psi == null) continue
            if (cl != null) psi.clear(cl) else psi.clear()
        }
    }

    fun resetPages(cl: ClassLoader?) {
        val it: Iterator<SoftReference<PageSource?>?> = pageSources!!.values().iterator()
        var psi: PageSourceImpl?
        var sr: SoftReference<PageSource?>?
        while (it.hasNext()) {
            sr = it.next()
            psi = if (sr == null) null else sr.get()
            if (psi == null) continue
            if (cl != null) psi.clear(cl) else psi.resetLoaded()
        }
    }

    fun clear() {
        clearPages(null)
        // pageSources.clear();
    }

    fun getMaxSize(): Int {
        return maxSize
    }
}