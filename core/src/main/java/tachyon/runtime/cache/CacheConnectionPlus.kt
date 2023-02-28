package tachyon.runtime.cache

import tachyon.commons.io.cache.Cache

interface CacheConnectionPlus : CacheConnection {
    fun getLoadedInstance(): Cache?
}