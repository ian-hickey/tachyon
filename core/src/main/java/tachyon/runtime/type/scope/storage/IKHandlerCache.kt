package tachyon.runtime.type.scope.storage

import java.io.IOException

class IKHandlerCache : IKHandler {
    protected var storeEmpty: Boolean = Caster.toBooleanValue(SystemUtil.getSystemPropOrEnvVar("tachyon.store.empty", null), false)

    companion object {
        private val tokens: ConcurrentHashMap<String?, Object?>? = ConcurrentHashMap<String?, Object?>()
        private val supportsSerialisation: Map<String?, Boolean?>? = ConcurrentHashMap()
        private fun deserializeIKStorageValueSupported(cache: Cache?): Boolean {
            // FUTURE extend Cache interface to make sure it can handle serilasation
            if (cache == null) return false
            val clazz: Class<out Cache?> = cache.getClass()
            val name: String = clazz.getName()
            var supported = supportsSerialisation!![name]
            if (supported == null) {
                try {
                    supported = Caster.toBoolean(clazz.getDeclaredMethod("isObjectSerialisationSupported", arrayOf<Class?>()).invoke(cache, arrayOf<Object?>()))
                } catch (e: Exception) {
                    supported = Boolean.FALSE
                }
                supportsSerialisation.put(name, supported)
            }
            return supported.booleanValue()
        }

        @Throws(PageException::class)
        private fun getCache(pc: PageContext?, cacheName: String?): Cache? {
            return try {
                val cc: CacheConnection = CacheUtil.getCacheConnection(pc, cacheName)
                if (!cc.isStorage()) throw ApplicationException("storage usage for this cache is disabled, you can enable this in the Tachyon administrator.")
                CacheUtil.getInstance(cc, ThreadLocalPageContext.getConfig(pc)) // cc.getInstance(config);
            } catch (e: IOException) {
                throw Caster.toPageException(e)
            }
        }

        fun getKey(cfid: String?, appName: String?, type: String?): String? {
            return StringBuilder("tachyon-storage:").append(type).append(":").append(cfid).append(":").append(appName).toString().toUpperCase()
        }

        fun getToken(key: String?): Object? {
            val newLock = Object()
            var lock: Object = tokens.putIfAbsent(key, newLock)
            if (lock == null) {
                lock = newLock
            }
            return lock
        }

        init {
            supportsSerialisation.put("org.tachyon.extension.cache.eh.EHCache", Boolean.TRUE)
            supportsSerialisation.put(RamCache::class.java.getName(), Boolean.TRUE)
        }
    }

    @Override
    @Throws(PageException::class)
    override fun loadData(pc: PageContext?, appName: String?, name: String?, strType: String?, type: Int, log: Log?): IKStorageValue? {
        val cache: Cache? = getCache(pc, name)
        val key = getKey(pc.getCFID(), appName, strType)
        synchronized(getToken(key)) {
            // sync necessary?
            val `val`: Object = cache.getValue(key, null)
            if (`val` is Array<ByteArray>) {
                ScopeContext.info(log,
                        "load existing data from cache [" + name + "] to create " + strType + " scope for " + pc.getApplicationContext().getName() + "/" + pc.getCFID())
                return IKStorageValue(`val` as Array<ByteArray?>)
            } else if (`val` is IKStorageValue) {
                ScopeContext.info(log,
                        "load existing data from cache [" + name + "] to create " + strType + " scope for " + pc.getApplicationContext().getName() + "/" + pc.getCFID())
                return `val`
            } else {
                ScopeContext.info(log, "create new " + strType + " scope for " + pc.getApplicationContext().getName() + "/" + pc.getCFID() + " in cache [" + name + "]")
            }
            return null
        }
    }

    @Override
    override fun store(storageScope: IKStorageScopeSupport?, pc: PageContext?, appName: String?, name: String?, data: Map<Collection.Key?, IKStorageScopeItem?>?, log: Log?) {
        try {
            val cache: Cache? = getCache(ThreadLocalPageContext.get(pc), name)
            val key = getKey(pc.getCFID(), appName, storageScope!!.getTypeAsString())
            synchronized(getToken(key)) {
                val existingVal: Object = cache.getValue(key, null)
                if (storeEmpty || storageScope!!.hasContent()) {
                    cache.put(key,
                            if (deserializeIKStorageValueSupported(cache)) IKStorageValue(IKStorageScopeSupport.prepareToStore(data, existingVal, storageScope!!.lastModified())) else IKStorageValue.toByteRepresentation(IKStorageScopeSupport.prepareToStore(data, existingVal, storageScope!!.lastModified())),
                            Long.valueOf(storageScope!!.getTimeSpan()), null)
                } else if (existingVal != null) {
                    cache.remove(key)
                }
            }
        } catch (e: Exception) {
            ScopeContext.error(log, e)
        }
    }

    @Override
    override fun unstore(storageScope: IKStorageScopeSupport?, pc: PageContext?, appName: String?, name: String?, log: Log?) {
        try {
            val cache: Cache? = getCache(pc, name)
            val key = getKey(pc.getCFID(), appName, storageScope!!.getTypeAsString())
            synchronized(getToken(key)) { cache.remove(key) }
        } catch (pe: Exception) {
        }
    }

    @Override
    override fun getType(): String? {
        return "Cache"
    }
}