package tachyon.commons.lang

import java.util.Collection

class ConcurrentHashMapAsHashtable<K, V> : Hashtable<K, V>(), Cloneable {
    private val map: ConcurrentHashMap<K, V>
    @Override
    @Synchronized
    fun size(): Int {
        return map.size()
    }

    @get:Synchronized
    @get:Override
    val isEmpty: Boolean
        get() = map.isEmpty()

    @Override
    @Synchronized
    fun keys(): Enumeration<K> {
        return map.keys()
    }

    @Override
    @Synchronized
    fun elements(): Enumeration<V> {
        return map.elements()
    }

    @Override
    @Synchronized
    operator fun contains(value: Object?): Boolean {
        return map.contains(value)
    }

    @Override
    fun containsValue(value: Object?): Boolean {
        return map.containsValue(value)
    }

    @Override
    @Synchronized
    fun containsKey(key: Object?): Boolean {
        return map.containsKey(key)
    }

    @Override
    @Synchronized
    operator fun get(key: Object?): V {
        return map.get(key)
    }

    @Override
    protected fun rehash() {
        // do nothing
    }

    @Override
    @Synchronized
    fun put(key: K, value: V): V {
        if (size() > 200) clear() // TODO do a soft version instead
        return map.put(key, value)
    }

    @Override
    @Synchronized
    fun remove(key: Object?): V {
        return map.remove(key)
    }

    @Override
    @Synchronized
    fun putAll(t: Map<out K, V>?) {
        map.putAll(t)
    }

    @Override
    @Synchronized
    fun clear() {
        map.clear()
    }

    @Override
    @Synchronized
    fun clone(): Object {
        val newMap: ConcurrentHashMapAsHashtable<K, V> = ConcurrentHashMapAsHashtable<Any, Any>()
        for (e in map.entrySet()) {
            newMap.put(e.getKey(), e.getValue())
        }
        return newMap
    }

    @Override
    @Synchronized
    override fun toString(): String {
        return map.toString()
    }

    @Override
    fun keySet(): Set<K> {
        return map.keySet()
    }

    @Override
    fun entrySet(): Set<Entry<K, V>> {
        return map.entrySet()
    }

    @Override
    fun values(): Collection<V> {
        return map.values()
    }

    @Override
    @Synchronized
    override fun equals(o: Object): Boolean {
        return this === o || map.equals(o)
    }

    @Override
    @Synchronized
    override fun hashCode(): Int {
        return map.hashCode()
    }

    @Override
    @Synchronized
    fun getOrDefault(key: Object?, defaultValue: V): V {
        return map.getOrDefault(key, defaultValue)
    }

    @Override
    @Synchronized
    fun forEach(action: BiConsumer<in K, in V>?) {
        map.forEach(action)
    }

    @Override
    @Synchronized
    fun replaceAll(function: BiFunction<in K, in V, out V>?) {
        map.replaceAll(function)
    }

    @Override
    @Synchronized
    fun putIfAbsent(key: K, value: V): V {
        return map.putIfAbsent(key, value)
    }

    @Override
    @Synchronized
    fun remove(key: Object?, value: Object?): Boolean {
        return map.remove(key, value)
    }

    @Override
    @Synchronized
    fun replace(key: K, oldValue: V, newValue: V): Boolean {
        return map.replace(key, oldValue, newValue)
    }

    @Override
    @Synchronized
    fun replace(key: K, value: V): V {
        return map.replace(key, value)
    }

    @Override
    @Synchronized
    fun computeIfAbsent(key: K, mappingFunction: Function<in K, out V>?): V {
        return map.computeIfAbsent(key, mappingFunction)
    }

    @Override
    @Synchronized
    fun computeIfPresent(key: K, remappingFunction: BiFunction<in K, in V, out V>?): V {
        return map.computeIfPresent(key, remappingFunction)
    }

    @Override
    @Synchronized
    fun compute(key: K, remappingFunction: BiFunction<in K, in V, out V>?): V {
        return map.compute(key, remappingFunction)
    }

    @Override
    @Synchronized
    fun merge(key: K, value: V, remappingFunction: BiFunction<in V, in V, out V>?): V {
        return map.merge(key, value, remappingFunction)
    }

    init {
        map = ConcurrentHashMap()
    }
}