package lucee.runtime.cache

import lucee.commons.io.cache.Cache

interface CacheConnectionPlus : CacheConnection {
    fun getLoadedInstance(): Cache?
}