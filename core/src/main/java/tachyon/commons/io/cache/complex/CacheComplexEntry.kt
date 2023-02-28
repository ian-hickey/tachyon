package tachyon.commons.io.cache.complex

import java.util.Date

class CacheComplexEntry(cache: CacheComplex, entry: CacheEntry?) : CacheEntry {
    private val entry: CacheEntry?
    private val cache: CacheComplex
    private var data: CacheComplexData? = null
    private var value: Object? = null
    @Override
    fun created(): Date {
        return lastModified()
    }

    @get:Override
    val customInfo: Struct
        get() = CacheUtil.getInfo(entry.getCustomInfo(), this)

    @get:Override
    val key: String
        get() = entry.getKey()

    @Override
    fun getValue(): Object? {
        getData()
        return value
    }

    fun getData(): CacheComplexData? {
        if (data != null) return data
        val v: Object = entry.getValue()
        if (v is CacheComplexData) {
            data = v
            value = data!!.value
        } else if (v != null) {
            value = v
        }
        return null
    }

    @Override
    fun hitCount(): Int {
        val d: CacheComplexData? = getData()
        return if (d != null) d.hitCount else 0
    }

    @Override
    fun idleTimeSpan(): Long {
        val i: Long = entry.idleTimeSpan()
        if (i > 0) return i
        val d: CacheComplexData? = getData()
        return if (d != null && d.idle != null && d.idle.longValue() > 0) d.idle.longValue() else 0
    }

    @Override
    fun liveTimeSpan(): Long {
        val l: Long = entry.liveTimeSpan()
        if (l > 0) return l
        val d: CacheComplexData? = getData()
        return if (d != null && d.until != null && d.until.longValue() > 0) d.until.longValue() else 0
    }

    @Override
    fun lastHit(): Date {
        val d: Date = entry.lastHit()
        return if (d != null) d else lastModified()
    }

    @Override
    fun lastModified(): Date {
        val d: Date = entry.lastModified()
        if (d != null) return d
        val ccd: CacheComplexData? = getData()
        return if (ccd != null && ccd.lastModified > 0) DateTimeImpl(ccd.lastModified, false) else DateTimeImpl(0, false)
    }

    @Override
    fun size(): Long {
        val s: Long = entry.size()
        if (s > 0) return s
        val v: Object? = getValue()
        return if (v != null && Decision.isSimpleValue(v)) Caster.toString(v, "").length() else 0
    }

    init {
        this.cache = cache
        this.entry = entry
    }
}