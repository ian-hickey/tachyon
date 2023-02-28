package lucee.runtime.cache.tag.soft

import org.apache.commons.collections4.map.AbstractReferenceMap.ReferenceStrength.HARD

class SoftReferenceCacheHandler : MapCacheHandler() {
    @Override
    protected fun map(): Map<String?, CacheItem?>? {
        return map
    }

    @Override
    fun acceptCachedWithin(cachedWithin: Object?): Boolean {
        val str: String = Caster.toString(cachedWithin, "").trim()
        return str.equalsIgnoreCase("soft")
    }

    @Override
    fun pattern(): String? {
        return "soft"
    }

    companion object {
        private val map: Map<String?, CacheItem?>? = Collections.synchronizedMap(ReferenceMap<String?, CacheItem?>(HARD, SOFT, 32, 0.75f))
    }
}