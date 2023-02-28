package tachyon.commons.io.cache.complex

import java.io.IOException

class CacheComplex(cc: CacheConnection, cache: Cache) : Cache {
    private val cache: Cache
    private val cc: CacheConnection
    @Override
    @Throws(IOException::class)
    fun entries(): List<CacheEntry> {
        return entries(cache.entries())
    }

    @Override
    @Throws(IOException::class)
    fun entries(filter: CacheKeyFilter?): List<CacheEntry> {
        return entries(cache.entries(filter))
    }

    @Override
    @Throws(IOException::class)
    fun entries(filter: CacheEntryFilter?): List<CacheEntry> {
        return entries(cache.entries(filter))
    }

    private fun entries(entries: List<CacheEntry>?): List<CacheEntry>? {
        if (entries == null || entries.size() === 0) return entries
        val it: Iterator<CacheEntry> = entries.iterator()
        val list: ArrayList<CacheEntry> = ArrayList<CacheEntry>(entries.size())
        var entry: CacheEntry
        while (it.hasNext()) {
            entry = it.next()
            if (entry != null) list.add(CacheComplexEntry(this, entry))
        }
        return list
    }

    @Override
    @Throws(IOException::class)
    fun getCacheEntry(key: String?): CacheEntry? {
        val entry: CacheEntry = cache.getCacheEntry(key)
        return if (entry == null) entry else CacheComplexEntry(this, entry)
    }

    @Override
    fun getCacheEntry(key: String?, defaultValue: CacheEntry): CacheEntry? {
        val entry: CacheEntry = cache.getCacheEntry(key, defaultValue)
        return if (entry == null || entry === defaultValue) entry else CacheComplexEntry(this, entry)
    }

    @get:Throws(IOException::class)
    @get:Override
    val customInfo: Struct
        get() = CacheUtil.getInfo(cache.getCustomInfo(), cache)

    @Override
    @Throws(IOException::class)
    fun getValue(key: String?): Object {
        val value: Object = cache.getValue(key)
        return if (value is CacheComplexData) (value as CacheComplexData).value else value
    }

    @Override
    operator fun getValue(key: String?, defaultValue: Object?): Object {
        val value: Object = cache.getValue(key, defaultValue)
        return if (value is CacheComplexData) (value as CacheComplexData).value else value
    }

    @Override
    @Throws(IOException::class)
    fun hitCount(): Long {
        return cache.hitCount()
    }

    @Override
    @Throws(IOException::class)
    fun missCount(): Long {
        return cache.missCount()
    }

    @Override
    @Throws(IOException::class)
    fun put(key: String?, value: Object?, idle: Long?, until: Long?) {
        cache.put(key, if (value == null) null else CacheComplexData(value, idle, until), idle, until)
    }

    @Override
    @Throws(IOException::class)
    fun remove(filter: CacheEntryFilter?): Int {
        return cache.remove(filter)
    }

    @Override
    @Throws(IOException::class)
    fun keys(filter: CacheEntryFilter?): List<String> {
        return cache.keys(filter)
    }

    @Override
    @Throws(IOException::class)
    fun values(): List<Object> {
        return values(cache.values())
    }

    @Override
    @Throws(IOException::class)
    fun values(filter: CacheKeyFilter?): List<Object> {
        return values(cache.values(filter))
    }

    @Override
    @Throws(IOException::class)
    fun values(filter: CacheEntryFilter?): List<Object> {
        return values(cache.values(filter))
    }

    @Throws(IOException::class)
    fun values(values: List<Object>?): List<Object>? {
        if (values == null || values.size() === 0) return values
        val list: ArrayList<Object> = ArrayList<Object>()
        val it: Iterator<Object> = values.iterator()
        var v: Object
        while (it.hasNext()) {
            v = it.next()
            if (v is CacheComplexData) list.add((v as CacheComplexData).value) else list.add(v)
        }
        return list
    }

    /////////////////////////////////////////////////////////////////////////////////////
    @Override
    @Throws(IOException::class)
    operator fun contains(key: String?): Boolean {
        return cache.contains(key)
    }

    @Override
    @Throws(IOException::class)
    fun init(config: Config?, arg1: String?, arg2: Struct?) {
        cache.init(config, arg1, arg2)
    }

    @Override
    @Throws(IOException::class)
    fun keys(): List<String> {
        return cache.keys()
    }

    @Override
    @Throws(IOException::class)
    fun keys(filter: CacheKeyFilter?): List<String> {
        return cache.keys(filter)
    }

    @Override
    @Throws(IOException::class)
    fun remove(key: String?): Boolean {
        return cache.remove(key)
    }

    @Override
    @Throws(IOException::class)
    fun remove(filter: CacheKeyFilter?): Int {
        return cache.remove(filter)
    }

    init {
        this.cc = cc
        this.cache = cache
    }
}