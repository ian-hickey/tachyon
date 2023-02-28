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
package lucee.runtime.cache.ram

import java.io.IOException

class RamCache : CacheSupport() {
    private val entries: Map<String?, Ref<RamCacheEntry?>?>? = ConcurrentHashMap<String?, Ref<RamCacheEntry?>?>()
    private var missCount: Long = 0
    private var hitCount = 0
    private var idleTime: Long = 0
    private var until: Long = 0
    private var controlInterval = DEFAULT_CONTROL_INTERVAL * 1000
    private var decouple = false
    private var controller: Thread? = null
    private var outOfMemory = false
    @Override
    @Throws(IOException::class)
    fun init(config: Config?, cacheName: String?, arguments: Struct?) {
        // RamCache is also used without calling init, because of that we have this test in constructor and
        // here
        if (controller == null) {
            val engine: CFMLEngineImpl = CFMLEngineImpl.toCFMLEngineImpl(ConfigWebUtil.getEngine(config), null)
            if (engine != null) {
                controller = Controler(engine, this)
                controller.start()
            }
        }
        if (controller == null) throw IOException("was not able to start controller")

        // out of memory
        outOfMemory = Caster.toBooleanValue(arguments.get("outOfMemory", false), false)
        // until
        val until: Long = Caster.toLongValue(arguments.get("timeToLiveSeconds", Constants.LONG_ZERO), Constants.LONG_ZERO) * 1000
        val idleTime: Long = Caster.toLongValue(arguments.get("timeToIdleSeconds", Constants.LONG_ZERO), Constants.LONG_ZERO) * 1000
        var ci: Object = arguments.get("controlIntervall", null)
        if (ci == null) ci = arguments.get("controlInterval", null)
        val intervalInSeconds: Int = Caster.toIntValue(ci, DEFAULT_CONTROL_INTERVAL)
        init(until, idleTime, intervalInSeconds)
    }

    fun init(until: Long, idleTime: Long, intervalInSeconds: Int): RamCache? {
        this.until = until
        this.idleTime = idleTime
        controlInterval = intervalInSeconds * 1000
        return this
    }

    fun release() {
        entries.clear()
        missCount = 0
        hitCount = 0
        idleTime = 0
        until = 0
        controlInterval = DEFAULT_CONTROL_INTERVAL * 1000
        decouple = false
        if (controller != null && controller.isAlive()) controller.interrupt()
    }

    @Override
    operator fun contains(key: String?): Boolean {
        return _getQuiet(key, null) != null
    }

    @Override
    fun getQuiet(key: String?, defaultValue: CacheEntry?): CacheEntry? {
        val tmp: Ref<RamCacheEntry?>? = entries!![key]
        var entry: RamCacheEntry? = (if (tmp == null) null else tmp.get()) ?: return defaultValue
        if (!valid(entry)) {
            entries.remove(key)
            return defaultValue
        }
        if (decouple) entry = RamCacheEntry(entry.getKey(), decouple(entry.getValue()), entry.idleTimeSpan(), entry.liveTimeSpan())
        return entry
    }

    private fun _getQuiet(key: String?, defaultValue: CacheEntry?): CacheEntry? {
        val tmp: Ref<RamCacheEntry?>? = entries!![key]
        val entry: RamCacheEntry = (if (tmp == null) null else tmp.get()) ?: return defaultValue
        if (!valid(entry)) {
            entries.remove(key)
            return defaultValue
        }
        return entry
    }

    @Override
    fun getCacheEntry(key: String?, defaultValue: CacheEntry?): CacheEntry? {
        var ce: RamCacheEntry? = _getQuiet(key, null)
        if (ce != null) {
            if (decouple) ce = RamCacheEntry(ce.getKey(), decouple(ce.getValue()), ce.idleTimeSpan(), ce.liveTimeSpan())
            hitCount++
            return ce.read()
        }
        missCount++
        return defaultValue
    }

    @Override
    fun hitCount(): Long {
        return hitCount.toLong()
    }

    @Override
    fun missCount(): Long {
        return missCount
    }

    @Override
    fun keys(): List<String?>? {
        val list: List<String?> = ArrayList<String?>()
        val it: Iterator<Entry<String?, Ref<RamCacheEntry?>?>?> = entries.entrySet().iterator()
        var entry: Ref<RamCacheEntry?>
        while (it.hasNext()) {
            entry = it.next().getValue()
            if (entry != null && valid(entry.get())) list.add(entry.get().getKey())
        }
        return list
    }

    @Override
    fun put(key: String?, value: Object?, idleTime: Long?, until: Long?) {
        val tmp: Ref<RamCacheEntry?>? = entries!![key]
        val entry: RamCacheEntry? = if (tmp == null) null else tmp.get()
        if (entry == null) {
            val e = RamCacheEntry(key, decouple(value), if (idleTime == null) this.idleTime else idleTime.longValue(), if (until == null) this.until else until.longValue())
            entries.put(key, if (outOfMemory) HardRef<RamCacheEntry?>(e) else SoftRef<RamCacheEntry?>(e))
        } else entry.update(value)
    }

    @Override
    fun remove(key: String?): Boolean {
        val tmp: Ref<RamCacheEntry?> = entries.remove(key)
        val entry: RamCacheEntry = (if (tmp == null) null else tmp.get()) ?: return false
        return valid(entry)
    }

    @Override
    @Throws(IOException::class)
    fun clear(): Int {
        val size: Int = entries!!.size()
        entries.clear()
        return size
    }

    class Controler(engine: CFMLEngineImpl?, ramCache: RamCache?) : ParentThreasRefThread() {
        private val ramCache: RamCache?
        private val engine: CFMLEngineImpl?
        @Override
        fun run() {
            while (engine.isRunning()) {
                try {
                    SystemUtil.sleep(ramCache!!.controlInterval)
                    _run()
                } catch (e: Exception) {
                    addParentStacktrace(e)
                    LogUtil.log("application", e)
                }
            }
        }

        private fun _run() {
            if (ramCache == null) return
            val e: Map<String?, Any?> = ramCache.entries ?: return
            val v: Collection<Ref<RamCacheEntry?>?> = e.values() ?: return
            val it: Iterator<Ref<RamCacheEntry?>?> = v.iterator()
            var sr: Ref<RamCacheEntry?>?
            var rce: RamCacheEntry
            while (it.hasNext()) {
                sr = it.next()
                if (sr != null && sr.get().also { rce = it } != null) {
                    if (!CacheSupport.valid(rce)) {
                        e.remove(rce!!.getKey())
                    }
                }
            }
        }

        init {
            this.engine = engine
            this.ramCache = ramCache
        }
    }

    @Override
    @Throws(CacheException::class)
    fun verify() {
        // this cache is in memory and always ok
    }

    @Override
    fun decouple(): CachePro? {
        decouple = true
        return this
    }

    private fun decouple(value: Object?): Object? {
        return if (!decouple) value else Duplicator.duplicate(value, true)
    }

    @Override
    fun getCustomInfo(): Struct? {
        val info: Struct = super.getCustomInfo()
        info.setEL("outOfMemoryHandling", entries is ReferenceMap)
        return info
    }

    fun isObjectSerialisationSupported(): Boolean {
        return true
    }

    companion object {
        const val DEFAULT_CONTROL_INTERVAL = 60
        fun init(config: Config?, cacheNames: Array<String?>?, arguments: Array<Struct?>?) { // print.ds();
        }
    }

    // this is used by the config by reflection
    init {
        val config: Config = ThreadLocalPageContext.getConfig()
        if (config != null) {
            val engine: CFMLEngineImpl = CFMLEngineImpl.toCFMLEngineImpl(ConfigWebUtil.getEngine(config), null)
            if (engine != null) {
                controller = Controler(engine, this)
                controller.start()
            }
        }
    }
}