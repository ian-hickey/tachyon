package tachyon.runtime.cache.tag

import tachyon.runtime.PageContext

interface CacheHandlerPro : CacheHandler {
    // FUTURE move methods to CacheHandler and delete this interface
    /**
     * This method will be used by Time-based cache handers, e.g. TimespanCacheHander, to check that the
     * cached item is still viable. If the cached item is too old then null will be returned. If 0 is
     * passed, then the cached item will be deleted.
     *
     *
     * Non-Time-based cache handlers should delegate the call to get(pc, cacheId)
     *
     * @param pc the PageContext
     * @param cacheId the key of the cached item
     * @param cachePolicy a Time-based object that will indicate the maximum lifetime of the cache
     * @return
     * @throws PageException
     */
    @Throws(PageException::class)
    operator fun get(pc: PageContext?, cacheId: String?, cachePolicy: Object?): CacheItem?
}