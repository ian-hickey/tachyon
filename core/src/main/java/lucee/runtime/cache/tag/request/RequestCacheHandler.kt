package lucee.runtime.cache.tag.request

import java.util.HashMap

class RequestCacheHandler : MapCacheHandler(), CacheHandlerPro {
    @Override
    protected fun map(): Map<String?, CacheItem?>? {
        return data.get()
    }

    @Override
    fun acceptCachedWithin(cachedWithin: Object?): Boolean {
        return Caster.toString(cachedWithin, "").equalsIgnoreCase("request")
    }

    @Override
    fun pattern(): String? {
        return "request"
    }

    @Override
    @Throws(PageException::class)
    operator fun get(pc: PageContext?, cacheId: String?, cachePolicy: Object?): CacheItem? {
        return get(pc, cacheId)
    }

    companion object {
        private val data: ThreadLocal<Map<String?, CacheItem?>?>? = object : ThreadLocal<Map<String?, CacheItem?>?>() {
            @Override
            protected fun initialValue(): Map<String?, CacheItem?>? {
                return HashMap<String?, CacheItem?>()
            }
        }
    }
}