package tachyon.runtime.type.scope.util

import java.util.Iterator

class SystemPropStruct : AbsSystemStruct() {
    @Override
    fun size(): Int {
        return System.getProperties().size()
    }

    @Override
    fun clear() {
        System.getProperties().clear()
    }

    @Override
    fun removeEL(key: Key?): Object? {
        val k: Object? = getKey(key)
        return if (k != null) System.getProperties().remove(key) else null
    }

    @Override
    @Throws(PageException::class)
    operator fun get(key: Key?): Object? {
        return get(null as PageContext?, key)
    }

    @Override
    @Throws(PageException::class)
    operator fun get(pc: PageContext?, key: Key?): Object? {
        val it: Iterator<Entry<Object?, Object?>?> = System.getProperties().entrySet().iterator()
        var e: Entry<Object?, Object?>?
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
        val it: Iterator<Entry<Object?, Object?>?> = System.getProperties().entrySet().iterator()
        var e: Entry<Object?, Object?>?
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
        val k: Object = getKey(key) ?: return System.setProperty(key.getString(), Caster.toString(value))
        return System.setProperty(Caster.toString(k), Caster.toString(value))
    }

    @Override
    fun setEL(key: Key?, value: Object?): Object? {
        val k: Object = getKey(key)
                ?: return System.setProperty(key.getString(), Caster.toString(value, value.toString()))
        return System.setProperty(k.toString(), Caster.toString(value, value.toString()))
    }

    @Override
    fun containsKey(key: Key?): Boolean {
        return getKey(key) != null
    }

    @Override
    fun containsKey(pc: PageContext?, key: Key?): Boolean {
        return getKey(key) != null
    }

    private fun getKey(key: Key?): Object? {
        val it: Iterator<Object?> = System.getProperties().keySet().iterator()
        var k: Object
        while (it.hasNext()) {
            if (key.equals(it.next().also { k = it })) return k
        }
        return null
    }

    @Override
    fun keys(): Array<Key?>? {
        val set: Set<Object?> = System.getProperties().keySet()
        val it: Iterator<Object?> = set.iterator()
        val keys: Array<Key?> = arrayOfNulls<Key?>(set.size())
        var index = 0
        var k: Object?
        while (it.hasNext()) {
            k = it.next()
            keys[index++] = KeyImpl.toKey(k, KeyImpl.init(k.toString()))
        }
        return keys
    }

    @Override
    fun getType(): Int {
        return Struct.TYPE_REGULAR
    }

    companion object {
        private val instance: SystemPropStruct? = SystemPropStruct()
        fun getInstance(): SystemPropStruct? {
            return instance
        }
    }
}