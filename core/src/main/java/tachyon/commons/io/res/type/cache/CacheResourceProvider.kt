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
package tachyon.commons.io.res.type.cache

import java.io.IOException

/**
 * Resource Provider for ram resource
 */
class CacheResourceProvider : ResourceProviderPro {
    @get:Override
    var scheme = "ram"
        private set

    @get:Override
    var isCaseSensitive = true
    private var lockTimeout: Long = 1000
    private val lock: ResourceLockImpl = ResourceLockImpl(lockTimeout, isCaseSensitive)
    private var arguments: Map? = null
    private var defaultCache: Cache? = null
    private val inits: Set<Integer> = HashSet<Integer>()
    // private Config config;
    /**
     * initialize ram resource
     *
     * @param scheme
     * @param arguments
     * @return RamResource
     */
    @Override
    fun init(scheme: String, arguments: Map?): ResourceProvider {
        if (!StringUtil.isEmpty(scheme)) this.scheme = scheme
        if (arguments != null) {
            this.arguments = arguments
            val oCaseSensitive: Object = arguments.get("case-sensitive")
            if (oCaseSensitive != null) {
                isCaseSensitive = Caster.toBooleanValue(oCaseSensitive, true)
            }

            // lock-timeout
            val oTimeout: Object = arguments.get("lock-timeout")
            if (oTimeout != null) {
                lockTimeout = Caster.toLongValue(oTimeout, lockTimeout)
            }
        }
        lock.setLockTimeout(lockTimeout)
        lock.setCaseSensitive(isCaseSensitive)
        return this
    }

    @Override
    fun getResource(path: String): Resource {
        var path = path
        path = ResourceUtil.removeScheme(scheme, path)
        if (!StringUtil.startsWith(path, '/')) path = "/$path"
        return CacheResource(this, path)
    }

    /**
     * returns core for this path if exists, otherwise return null
     *
     * @param path
     * @return core or null
     */
    fun getCore(path: String?, name: String?): CacheResourceCore? {
        val obj: Object = cache.getValue(toKey(path, name), null)
        return if (obj is CacheResourceCore) obj else null
    }

    @Throws(IOException::class)
    fun touch(path: String?, name: String?) {
        val cache: Cache? = cache
        val ce: CacheEntry = cache.getCacheEntry(toKey(path, name), null)
        if (ce != null) {
            cache.put(ce.getKey(), ce.getValue(), ce.idleTimeSpan(), ce.liveTimeSpan())
        }
    }

    fun getMeta(path: String?, name: String?): Struct? {
        val ce: CacheEntry = cache.getCacheEntry(toKey(path, name), null)
        return if (ce != null) ce.getCustomInfo() else null
    }

    @Throws(IOException::class)
    fun getChildNames(path: String): Array<String?> {
        val list: List = cache.values(ChildrenFilter(path))
        val arr = arrayOfNulls<String>(list.size())
        val it: Iterator = list.iterator()
        var index = 0
        while (it.hasNext()) {
            arr[index++] = (it.next() as CacheResourceCore).getName()
        }
        // TODO remove none CacheResourceCore elements
        return arr
    }
    /*
	 * CacheResourceCore[] getChildren(String path) { List list = getCache().values(new
	 * ChildrenFilter(path)); CacheResourceCore[] arr = new CacheResourceCore[list.size()]; Iterator it
	 * = list.iterator(); int index=0; while(it.hasNext()){ arr[index++]=(CacheResourceCore) it.next();
	 * } // TODO remove none CacheResourceCore elements return arr; }
	 */
    /**
     * create a new core
     *
     * @param path
     * @param type
     * @return created core
     * @throws IOException
     */
    @Throws(IOException::class)
    fun createCore(path: String?, name: String?, type: Int): CacheResourceCore {
        val value = CacheResourceCore(type, path, name)
        cache.put(toKey(path, name), value, null, null)
        return value
    }

    @Throws(IOException::class)
    fun removeCore(path: String?, name: String?) {
        cache.remove(toKey(path, name))
    }

    @Override
    fun setResources(resources: Resources?) {
        // this.resources=resources;
    }

    @Override
    @Throws(IOException::class)
    fun lock(res: Resource?) {
        lock.lock(res)
    }

    @Override
    fun unlock(res: Resource?) {
        lock.unlock(res)
    }

    @Override
    @Throws(IOException::class)
    fun read(res: Resource?) {
        lock.read(res)
    }

    @get:Override
    val isAttributesSupported: Boolean
        get() = true

    @get:Override
    val isModeSupported: Boolean
        get() = true

    @Override
    fun getArguments(): Map? {
        return arguments
    }// simply ignore

    // CFMLEngineImpl e=null;
    val cache: Cache?
        get() {
            val pc: PageContext = ThreadLocalPageContext.get()
            var c: Cache? = CacheUtil.getDefault(pc, Config.CACHE_TYPE_RESOURCE, null)
            if (c == null) {
                // CFMLEngineImpl e=null;
                if (defaultCache == null) {
                    defaultCache = RamCache().init(0, 0, RamCache.DEFAULT_CONTROL_INTERVAL)
                }
                c = defaultCache
            }
            if (!inits.contains(c.hashCode())) {
                val k = toKey("null", "")
                try {
                    if (!c.contains(k)) {
                        val value = CacheResourceCore(CacheResourceCore.TYPE_DIRECTORY, null, "")
                        c.put(k, value, Constants.LONG_ZERO, Constants.LONG_ZERO)
                    }
                } catch (e: IOException) {
                    // simply ignore
                }
                inits.add(c.hashCode())
            }
            return c
        }

    private fun toKey(path: String?, name: String?): String {
        return if (isCaseSensitive) path.toString() + ":" + name else (path.toString() + ":" + name).toLowerCase()
    }

    @get:Override
    val separator: Char
        get() = '/'
}