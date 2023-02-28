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

import java.io.IOException

// DO NOT CHANGE interface used by extension axis
class CacheHandlerCollectionImpl(cw: ConfigWeb?, cacheType: Int) : CacheHandlerCollection {
    private val cw: ConfigWeb?
    var handlers: Map<String?, CacheHandler?>? = HashMap<String?, CacheHandler?>()
    @Override
    fun getInstanceMatchingObject(cachedWithin: Object?, defaultValue: CacheHandler?): CacheHandler? {
        val it: Iterator<CacheHandler?> = handlers!!.values().iterator()
        var ch: CacheHandler?
        while (it.hasNext()) {
            ch = it.next()
            if (ch.acceptCachedWithin(cachedWithin)) return ch
        }
        return defaultValue
    }

    fun getTimespanInstance(defaultValue: CacheHandler?): CacheHandler? {
        val it: Iterator<CacheHandler?> = handlers!!.values().iterator()
        var ch: CacheHandler?
        while (it.hasNext()) {
            ch = it.next()
            if (ch is TimespanCacheHandler) return ch
        }
        return defaultValue
    }

    @Override
    fun getInstance(cacheHandlerId: String?, defaultValue: CacheHandler?): CacheHandler? {

        // return cache handler matching
        var ch: CacheHandler? = handlers!![cacheHandlerId]
        if (ch != null) return ch
        ch = handlers!![cacheHandlerId.toLowerCase().trim()]
        return if (ch != null) ch else defaultValue
    }

    @Override
    @Throws(PageException::class)
    fun size(pc: PageContext?): Int {
        var size = 0
        val it: Iterator<CacheHandler?> = handlers!!.values().iterator()
        while (it.hasNext()) {
            size += it.next().size(pc)
        }
        return size
    }

    @Override
    @Throws(PageException::class)
    fun clear(pc: PageContext?) {
        val it: Iterator<CacheHandler?> = handlers!!.values().iterator()
        while (it.hasNext()) {
            it.next().clear(pc)
        }
    }

    @Override
    @Throws(PageException::class)
    fun clear(pc: PageContext?, filter: CacheHandlerFilter?) {
        val it: Iterator<CacheHandler?> = handlers!!.values().iterator()
        while (it.hasNext()) {
            it.next().clear(pc, filter)
        }
    }

    @Override
    @Throws(PageException::class)
    fun clean(pc: PageContext?) {
        val it: Iterator<CacheHandler?> = handlers!!.values().iterator()
        while (it.hasNext()) {
            it.next().clean(pc)
        }
    }

    @Override
    @Throws(PageException::class)
    fun remove(pc: PageContext?, id: String?) {
        val it: Iterator<CacheHandler?> = handlers!!.values().iterator()
        while (it.hasNext()) {
            it.next().remove(pc, id)
        }
    }

    @Override
    fun getPatterns(): List<String?>? {
        val patterns: List<String?> = ArrayList<String?>()
        val it: Iterator<CacheHandler?> = handlers!!.values().iterator()
        while (it.hasNext()) {
            patterns.add(it.next().pattern())
        }
        return patterns
    }

    @Override
    @Throws(PageException::class)
    fun release(pc: PageContext?) {
        val it: Iterator<CacheHandler?> = handlers!!.values().iterator()
        while (it.hasNext()) {
            it.next().release(pc)
        }
    }

    companion object {
        const val CACHE_DEL = ';'
        const val CACHE_DEL2 = ':'
        @Throws(PageException::class)
        fun createId(sources: Array<PageSource?>?): String? {
            val str: String
            str = if (sources!!.size == 1) {
                sources[0].getDisplayPath()
            } else {
                val sb = StringBuilder()
                for (i in sources.indices) {
                    if (i > 0) sb.append(";")
                    sb.append(sources[i].getDisplayPath())
                }
                sb.toString()
            }
            return try {
                CacheUtil.key(KeyGenerator.createKey(str))
            } catch (e: IOException) {
                throw Caster.toPageException(e)
            }
        }

        @Throws(PageException::class)
        fun createId(sql: SQL?, datasource: String?, username: String?, password: String?, returnType: Int, maxRows: Int): String? {
            return try {
                CacheUtil.key(KeyGenerator.createKey(sql.toHashString() + datasource + username + password + returnType + maxRows))
            } catch (e: IOException) {
                throw Caster.toPageException(e)
            }
        }

        fun createId(res: Resource?, binary: Boolean): String? {
            val sb: StringBuilder = StringBuilder().append(res.getAbsolutePath()).append(CACHE_DEL).append(binary).append(CACHE_DEL)
            return HashUtil.create64BitHashAsString(sb, Character.MAX_RADIX)
        }

        fun createId(wsdlUrl: String?, username: String?, password: String?, proxyData: ProxyData?, methodName: String?, arguments: Array<Object?>?, namedArguments: Struct?): String? {
            val sb: StringBuilder = StringBuilder().append(wsdlUrl).append(CACHE_DEL).append(username).append(CACHE_DEL).append(password).append(CACHE_DEL).append(proxyData)
                    .append(CACHE_DEL).append(methodName)
            createIdArgs(null, sb, arguments, namedArguments)
            return HashUtil.create64BitHashAsString(sb, Character.MAX_RADIX)
        }

        fun createId(udf: UDFImpl?, args: Array<Object?>?, values: Struct?): String? {
            val src: String = udf.getSource()
            val sb: StringBuilder = StringBuilder().append(src
                    ?: "").append(CACHE_DEL).append(udf.properties.getStartLine()).append(CACHE_DEL)
                    .append(udf.getFunctionName()).append(CACHE_DEL)
            createIdArgs(udf, sb, args, values)
            return HashUtil.create64BitHashAsString(sb, Character.MAX_RADIX)
        }

        private fun createIdArgs(udf: UDFImpl?, sb: StringBuilder?, args: Array<Object?>?, namedArgs: Struct?) {
            if (namedArgs != null) {
                // argumentCollection
                var sct: Struct?
                if (namedArgs.size() === 1 && Caster.toStruct(namedArgs.get(KeyConstants._argumentCollection, null), null).also { sct = it } != null) {
                    _create(sct, sb)
                } else _create(namedArgs, sb)
            } else if (args != null) {
                val _args: Array<FunctionArgument?>? = if (udf == null) null else udf.getFunctionArguments()
                sb.append('{')
                for (i in args.indices) {
                    if (_args != null && _args.size > i) sb.append(_args[i].getName().getLowerString()).append(':')
                    sb.append(_createId(args[i])).append(',')
                }
                sb.append('}')
            }
        }

        private fun _create(sct: Struct?, sb: StringBuilder?) {
            val it: Iterator<Entry<Key?, Object?>?> = sct.entryIterator()
            var e: Entry<Key?, Object?>?
            sb.append('{')
            while (it.hasNext()) {
                e = it.next()
                sb.append(e.getKey().getLowerString()).append(':').append(_createId(e.getValue())).append(',')
            }
            sb.append('}')
        }

        private fun _createId(values: Object?): String? {
            return UDFArgConverter.serialize(values)
        }

        fun createId(url: String?, urlToken: String?, method: Short, params: ArrayList<HttpParamBean?>?, username: String?, password: String?, port: Int, proxyserver: String?,
                     proxyport: Int, proxyuser: String?, proxypassword: String?, useragent: String?): String? {
            val sb: StringBuilder = StringBuilder().append(url).append(CACHE_DEL).append(urlToken).append(CACHE_DEL).append(method).append(CACHE_DEL).append(username).append(CACHE_DEL)
                    .append(password).append(CACHE_DEL).append(port).append(CACHE_DEL).append(proxyserver).append(CACHE_DEL).append(proxyport).append(CACHE_DEL).append(proxyuser)
                    .append(CACHE_DEL).append(proxypassword).append(CACHE_DEL).append(proxypassword).append(CACHE_DEL).append(useragent).append(CACHE_DEL)
            var hpb: HttpParamBean
            val it: Iterator<HttpParamBean?> = params.iterator()
            while (it.hasNext()) {
                hpb = it.next()
                sb.append(hpb.getEncoded()).append(CACHE_DEL).append(hpb.getMimeType()).append(CACHE_DEL).append(hpb.getName()).append(CACHE_DEL).append(hpb.getType())
                        .append(CACHE_DEL).append(toString(hpb.getValue())).append(CACHE_DEL).append(toString(hpb.getFile())).append(CACHE_DEL)
            }
            return HashUtil.create64BitHashAsString(sb.toString())
        }

        private fun toString(value: Object?): Object? {
            return Caster.toString(value, null)
        }

        private fun toString(file: Resource?): Object? {
            return if (file == null) "" else file.getAbsolutePath()
        }

        fun toStringCacheName(type: Int, defaultValue: String?): String? {
            when (type) {
                ConfigPro.CACHE_TYPE_FUNCTION -> return "function"
                ConfigPro.CACHE_TYPE_INCLUDE -> return "include"
                ConfigPro.CACHE_TYPE_OBJECT -> return "object"
                ConfigPro.CACHE_TYPE_QUERY -> return "query"
                ConfigPro.CACHE_TYPE_RESOURCE -> return "resource"
                ConfigPro.CACHE_TYPE_TEMPLATE -> return "template"
                ConfigPro.CACHE_TYPE_HTTP -> return "http"
                ConfigPro.CACHE_TYPE_FILE -> return "file"
                ConfigPro.CACHE_TYPE_WEBSERVICE -> return "webservice"
            }
            return defaultValue
        }

        fun toCacheItem(value: Object?, defaultValue: CacheItem?): CacheItem? {
            return if (value is CacheItem) value as CacheItem? else defaultValue
        }

        fun clear(pc: PageContext?, cache: Cache?, filter: CacheHandlerFilter?) {
            try {
                val it: Iterator<CacheEntry?> = cache.entries().iterator()
                var ce: CacheEntry?
                var obj: Object
                while (it.hasNext()) {
                    ce = it.next()
                    if (filter == null) {
                        cache.remove(ce.getKey())
                        continue
                    }
                    obj = ce.getValue()
                    if (obj is QueryCacheItem) obj = (obj as QueryCacheItem).getQuery()
                    if (filter.accept(obj)) cache.remove(ce.getKey())
                }
            } catch (e: IOException) {
            }
        }
    }
    // private final CacheHandler rch;
    // private final CacheHandler sch;
    // private final CacheHandler tch;
    /**
     *
     * @param cw config object this Factory is related
     * @param cacheType type of the cache, see Config.CACHE_TYPE_XXX
     * @throws PageException
     */
    init {
        this.cw = cw
        val it: Iterator<Entry<String?, Class<CacheHandler?>?>?> = (cw as ConfigWebPro?).getCacheHandlers()
        var e: Entry<String?, Class<CacheHandler?>?>?
        var ch: CacheHandler
        while (it.hasNext()) {
            e = it.next()
            try {
                ch = e.getValue().newInstance()
                ch.init(cw, e.getKey(), cacheType)
                handlers.put(e.getKey(), ch)
            } catch (pe: Exception) {
                ThreadLocalPageContext.getLog(cw, "application").error("cache-handler:" + e.getKey(), pe)
                throw PageRuntimeException(Caster.toPageException(pe))
            }
        }
    }
}