package tachyon.commons.io.cache.complex

import java.io.Serializable

class CacheComplexData(value: Object, idle: Long?, until: Long?) : Serializable {
    val value: Object
    val lastModified: Long
    val idle: Long?
    val until: Long?
    val hitCount = 0 // TODO
    override fun toString(): String {
        return value.toString()
    }

    companion object {
        private const val serialVersionUID = 6401384011421058561L
    }

    init {
        this.value = value
        lastModified = System.currentTimeMillis()
        this.idle = idle
        this.until = until
    }
}