package lucee.runtime.type.scope.util

import java.util.Iterator

class EnvStruct : AbsSystemStruct() {
    @Override
    fun size(): Int {
        return System.getenv().size()
    }

    @Override
    fun clear() {
        System.getenv().clear()
    }

    @Override
    fun removeEL(key: Key?): Object? {
        val k = getKey(key)
        return if (k != null) System.getenv().remove(key) else null
    }

    @Override
    @Throws(PageException::class)
    operator fun get(key: Key?): Object? {
        return get(null as PageContext?, key)
    }

    @Override
    @Throws(PageException::class)
    operator fun get(pc: PageContext?, key: Key?): Object? {
        val it: Iterator<Entry<String?, String?>?> = System.getenv().entrySet().iterator()
        var e: Entry<String?, String?>?
        if (key == null) throw StructSupport.invalidKey(null, this, key, null)
        while (it.hasNext()) {
            e = it.next()
            if (key.equals(e.getKey())) return e.getValue()
        }
        throw StructSupport.invalidKey(null, this, key, null)
    }

    @Override
    operator fun get(key: Key?, defaultValue: Object?): Object? {
        return get(null as PageContext?, key, defaultValue)
    }

    @Override
    operator fun get(pc: PageContext?, key: Key?, defaultValue: Object?): Object? {
        val it: Iterator<Entry<String?, String?>?> = System.getenv().entrySet().iterator()
        var e: Entry<String?, String?>?
        if (key == null) return defaultValue
        while (it.hasNext()) {
            e = it.next()
            if (key.equals(e.getKey())) return e.getValue()
        }
        return defaultValue
    }

    @Override
    @Throws(PageException::class)
    operator fun set(key: Key?, value: Object?): Object? {
        val k = getKey(key) ?: return System.getenv().put(key.getString(), Caster.toString(value))
        return System.getenv().put(k, Caster.toString(value))
    }

    @Override
    fun setEL(key: Key?, value: Object?): Object? {
        val k = getKey(key) ?: return System.getenv().put(key.getString(), Caster.toString(value, value.toString()))
        return System.getenv().put(k, Caster.toString(value, value.toString()))
    }

    @Override
    fun containsKey(key: Key?): Boolean {
        return getKey(key) != null
    }

    @Override
    fun containsKey(pc: PageContext?, key: Key?): Boolean {
        return getKey(key) != null
    }

    private fun getKey(key: Key?): String? {
        val it: Iterator<String?> = System.getenv().keySet().iterator()
        var k: String
        while (it.hasNext()) {
            if (key.equals(it.next().also { k = it })) return k
        }
        return null
    }

    @Override
    fun keys(): Array<Key?>? {
        val set: Set<String?> = System.getenv().keySet()
        val it = set.iterator()
        val keys: Array<Key?> = arrayOfNulls<Key?>(set.size())
        var index = 0
        var k: String?
        while (it.hasNext()) {
            k = it.next()
            keys[index++] = KeyImpl.toKey(k, KeyImpl.init(k))
        }
        return keys
    }

    @Override
    fun getType(): Int {
        return StructUtil.getType(System.getenv())
    }

    companion object {
        private val instance: EnvStruct? = EnvStruct()
        fun getInstance(): EnvStruct? {
            return instance
        }
    }
}