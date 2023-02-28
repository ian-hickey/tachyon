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
package tachyon.runtime.cache

import java.io.IOException

object CacheUtil {
    /**
     * get the default cache for a certain type, also check definitions in application context
     * (application . cfc/cfapplication)
     *
     * @param pc current PageContext
     * @param type default type -> Config.CACHE_DEFAULT_...
     * @param defaultValue value returned when there is no default cache for this type
     * @return matching cache
     */
    fun getDefault(pc: PageContext?, type: Int, defaultValue: Cache?): Cache? {
        // get default from application conetx
        var name: String? = null
        if (pc != null && pc.getApplicationContext() != null) name = pc.getApplicationContext().getDefaultCacheName(type)
        val config: Config = ThreadLocalPageContext.getConfig(pc)
        if (!StringUtil.isEmpty(name)) {
            val cc: Cache? = getCache(pc, name, null)
            if (cc != null) return cc
        }

        // get default from config
        val cc: CacheConnection = (config as ConfigPro).getCacheDefaultConnection(type) ?: return defaultValue
        return try {
            cc.getInstance(config)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            defaultValue
        }
    }

    /**
     * get the default cache for a certain type, also check definitions in application context
     * (application . cfc/cfapplication)
     *
     * @param pc current PageContext
     * @param type default type -> Config.CACHE_DEFAULT_...
     * @return matching cache
     * @throws IOException
     */
    @Throws(IOException::class)
    fun getDefault(pc: PageContext?, type: Int): Cache? {
        // get default from application conetxt
        val name: String? = if (pc != null) pc.getApplicationContext().getDefaultCacheName(type) else null
        if (!StringUtil.isEmpty(name)) {
            val cc: Cache? = getCache(pc, name, null)
            if (cc != null) return cc
        }

        // get default from config
        val config: Config = ThreadLocalPageContext.getConfig(pc)
        val cc: CacheConnection = (config as ConfigPro).getCacheDefaultConnection(type)
                ?: throw CacheException("there is no default " + toStringType(type, "") + " cache defined, you need to define this default cache in the Tachyon Administrator")
        return cc.getInstance(config)
    }

    // do not change, used in Redis extension FUTURE add to interface
    @Throws(IOException::class)
    fun getCache(pc: PageContext?, cacheName: String?, type: Int): Cache? {
        return if (StringUtil.isEmpty(cacheName)) getDefault(pc, type) else getCache(pc, cacheName)
    }

    fun getCache(pc: PageContext?, cacheName: String?, type: Int, defaultValue: Cache?): Cache? {
        return if (StringUtil.isEmpty(cacheName)) getDefault(pc, type, defaultValue) else getCache(pc, cacheName, defaultValue)
    }

    // USED in extension
    @Throws(IOException::class)
    fun getCache(pc: PageContext?, cacheName: String?): Cache? {
        val cc: CacheConnection? = getCacheConnection(pc, cacheName)
        return cc.getInstance(ThreadLocalPageContext.getConfig(pc))
    }

    fun getCache(pc: PageContext?, cacheName: String?, defaultValue: Cache?): Cache? {
        val cc: CacheConnection = getCacheConnection(pc, cacheName, null)
                ?: return defaultValue
        val config: Config = ThreadLocalPageContext.getConfig(pc)
        return try {
            cc.getInstance(config)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            defaultValue
        }
    }

    // USED in extension
    @Throws(IOException::class)
    fun getCacheConnection(pc: PageContext?, cacheName: String?): CacheConnection? {
        var pc: PageContext? = pc
        pc = ThreadLocalPageContext.get(pc)
        if (pc != null) return (pc as PageContextImpl?).getCacheConnection(cacheName)
        val config: Config = ThreadLocalPageContext.getConfig(pc)
        return config.getCacheConnections().get(cacheName.toLowerCase().trim())
                ?: throw noCache(config, cacheName)
    }

    fun getCacheConnection(pc: PageContext?, cacheName: String?, defaultValue: CacheConnection?): CacheConnection? {
        var pc: PageContext? = pc
        pc = ThreadLocalPageContext.get(pc)
        if (pc != null) return (pc as PageContextImpl?).getCacheConnection(cacheName, null)
        val config: Config = ThreadLocalPageContext.getConfig(pc)
        return config.getCacheConnections().get(cacheName.toLowerCase().trim()) ?: return defaultValue
    }

    fun noCache(config: Config?, cacheName: String?): CacheException? {
        val sb: StringBuilder = StringBuilder("there is no cache defined with name [").append(cacheName).append("], available caches are [")
        val it: Iterator<String?> = (config as ConfigPro?).getCacheConnections().keySet().iterator()
        if (it.hasNext()) {
            sb.append(it.next())
        }
        while (it.hasNext()) {
            sb.append(", ").append(it.next())
        }
        sb.append("]")
        return CacheException(sb.toString())
    }

    fun getInfo(ce: CacheEntry?): Struct? {
        return getInfo(StructImpl(), ce)
    }

    fun getInfo(info: Struct?, ce: CacheEntry?): Struct? {
        var info: Struct? = info
        if (info == null) info = StructImpl()
        info.setEL(KeyConstants._key, ce.getKey())
        info.setEL(KeyConstants._created, ce.created())
        info.setEL("last_hit", ce.lastHit())
        info.setEL("last_modified", ce.lastModified())
        info.setEL("hit_count", Double.valueOf(ce.hitCount()))
        info.setEL(KeyConstants._size, Double.valueOf(ce.size()))
        info.setEL("idle_time_span", toTimespan(ce.idleTimeSpan()))
        info.setEL("live_time_span", toTimespan(ce.liveTimeSpan()))
        return info
    }

    fun getInfo(c: Cache?): Struct? {
        return getInfo(StructImpl(), c)
    }

    fun getInfo(info: Struct?, c: Cache?): Struct? {
        var info: Struct? = info
        if (info == null) info = StructImpl()
        try {
            val value: Long = c.hitCount()
            if (value >= 0) info.setEL("hit_count", Double.valueOf(value))
        } catch (ioe: IOException) {
            // simply ignore
        }
        try {
            val value: Long = c.missCount()
            if (value >= 0) info.setEL("miss_count", Double.valueOf(value))
        } catch (ioe: IOException) {
            // simply ignore
        }
        return info
    }

    fun toTimespan(timespan: Long): Object? {
        return if (timespan == 0L) "" else TimeSpanImpl.fromMillis(timespan) ?: return ""
    }

    fun toString(ce: CacheEntry?): String? {
        return """
     created:	${ce.created().toString()}
     last-hit:	${ce.lastHit().toString()}
     last-modified:	
     """.trimIndent() + ce.lastModified()
                .toString() + "\nidle-time:	" + ce.idleTimeSpan().toString() + "\nlive-time	:" + ce.liveTimeSpan()
                .toString() + "\nhit-count:	" + ce.hitCount().toString() + "\nsize:		" + ce.size()
    }

    fun allowAll(filter: CacheFilter?): Boolean {
        if (filter == null) return true
        val p: String = StringUtil.trim(filter.toPattern(), "")
        return p.equals("*") || p.equals("")
    }

    /**
     * in difference to the getInstance method of the CacheConnection this method produces a wrapper
     * Cache (if necessary) that creates Entry objects to make sure the Cache has meta data.
     *
     * @param cc
     * @param config
     * @return
     * @throws IOException
     */
    @Throws(IOException::class)
    fun getInstance(cc: CacheConnection?, config: Config?): Cache? {
        return cc.getInstance(config)
    }

    fun removeEL(config: ConfigWeb?, cc: CacheConnection?): Boolean {
        return try {
            remove(config, cc)
            true
        } catch (e: Throwable) {
            ExceptionUtil.rethrowIfNecessary(e)
            false
        }
    }

    @Throws(Throwable::class)
    fun remove(config: ConfigWeb?, cc: CacheConnection?) {
        val c: Cache = cc.getInstance(config)
        // FUTURE no reflection needed
        var remove: Method? = null
        remove = try {
            c.getClass().getMethod("remove", arrayOfNulls<Class?>(0))
        } catch (ioe: Exception) {
            c.remove(null as CacheEntryFilter?)
            return
        }
        try {
            remove.invoke(c, arrayOfNulls<Object?>(0))
        } catch (e: InvocationTargetException) {
            throw e.getTargetException()
        }
    }

    fun releaseEL(cc: CacheConnection?) {
        try {
            release(cc)
        } catch (e: IOException) {
        }
    }

    @Throws(IOException::class)
    fun release(cc: CacheConnection?) {
        val c: Cache = (cc as CacheConnectionPlus?)!!.getLoadedInstance() ?: return

        // FUTURE no reflection needed
        var release: Method? = null
        release = try {
            c.getClass().getMethod("release", arrayOf<Class?>())
        } catch (e: Exception) {
            return
        }
        try {
            if (release != null) release.invoke(c, arrayOf<Object?>())
        } catch (e: Exception) {
            throw ExceptionUtil.toIOException(e)
        }
    }

    fun releaseAll(config: Config?) {
        // Config
        for (cc in config.getCacheConnections().values()) {
            releaseEL(cc)
        }
    }

    fun releaseAllApplication() {
        // application defined caches (modern and classic)
        ModernApplicationContext.releaseInitCacheConnections()
    }

    private fun toStringType(type: Int, defaultValue: String?): String? {
        if (type == Config.CACHE_TYPE_OBJECT) return "object"
        if (type == Config.CACHE_TYPE_TEMPLATE) return "template"
        if (type == Config.CACHE_TYPE_QUERY) return "query"
        if (type == Config.CACHE_TYPE_RESOURCE) return "resource"
        if (type == Config.CACHE_TYPE_FUNCTION) return "function"
        if (type == Config.CACHE_TYPE_INCLUDE) return "include"
        if (type == Config.CACHE_TYPE_HTTP) return "http"
        if (type == Config.CACHE_TYPE_FILE) return "file"
        return if (type == Config.CACHE_TYPE_WEBSERVICE) "webservice" else defaultValue
    }

    fun key(key: String?): String? {
        return key.toUpperCase().trim()
    }

    fun toType(type: String?, defaultValue: Int): Int {
        var type = type
        type = type.trim().toLowerCase()
        if ("object".equals(type)) return Config.CACHE_TYPE_OBJECT
        if ("query".equals(type)) return Config.CACHE_TYPE_QUERY
        if ("resource".equals(type)) return Config.CACHE_TYPE_RESOURCE
        if ("template".equals(type)) return Config.CACHE_TYPE_TEMPLATE
        if ("function".equals(type)) return Config.CACHE_TYPE_FUNCTION
        if ("include".equals(type)) return Config.CACHE_TYPE_INCLUDE
        if ("http".equals(type)) return Config.CACHE_TYPE_HTTP
        if ("file".equals(type)) return Config.CACHE_TYPE_FILE
        return if ("webservice".equals(type)) Config.CACHE_TYPE_WEBSERVICE else defaultValue
    }

    fun toType(type: Int, defaultValue: String?): String? {
        if (Config.CACHE_TYPE_OBJECT === type) return "object"
        if (Config.CACHE_TYPE_QUERY === type) return "query"
        if (Config.CACHE_TYPE_RESOURCE === type) return "resource"
        if (Config.CACHE_TYPE_TEMPLATE === type) return "template"
        if (Config.CACHE_TYPE_FUNCTION === type) return "function"
        if (Config.CACHE_TYPE_INCLUDE === type) return "include"
        if (Config.CACHE_TYPE_HTTP === type) return "http"
        if (Config.CACHE_TYPE_FILE === type) return "file"
        return if (Config.CACHE_TYPE_WEBSERVICE === type) "webservice" else defaultValue
    }

    /**
     * returns true if the webAdminPassword matches the passed password if one is passed, or a password
     * defined in Application . cfc as this.webAdminPassword if null or empty-string is passed for
     * password
     *
     * @param pc
     * @param password
     * @return
     * @throws tachyon.runtime.exp.SecurityException
     */
    @Throws(tachyon.runtime.exp.SecurityException::class)
    fun getPassword(pc: PageContext?, password: String?, server: Boolean): Password? { // TODO: move this to a utility class in a more
        // generic package?
        // no password passed
        var password = password
        if (StringUtil.isEmpty(password, true)) {
            val appContext: ApplicationContext = pc.getApplicationContext()
            if (appContext is ModernApplicationContext) password = Caster.toString((appContext as ModernApplicationContext).getCustom(KeyConstants._webAdminPassword), "")
        } else password = password.trim()
        if (StringUtil.isEmpty(password, true)) throw SecurityException(
                "A Web Admin Password is required to manipulate Cache connections. " + "You can either pass the password as an argument to this function, or set it in "
                        + (if (pc.getRequestDialect() === CFMLEngine.DIALECT_CFML) Constants.CFML_APPLICATION_EVENT_HANDLER else Constants.LUCEE_APPLICATION_EVENT_HANDLER)
                        + " with the variable [this.webAdminPassword].")
        return PasswordImpl.passwordToCompare(pc.getConfig(), server, password)
    }
}