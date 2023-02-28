/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Tachyon Assosication Switzerland
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.  If not, see <http:></http:>//www.gnu.org/licenses/>.
 *
 */
package tachyon.runtime.type

import org.apache.commons.collections4.map.AbstractReferenceMap.ReferenceStrength.HARD

/**
 * CFML data type struct
 */
class StructImpl @JvmOverloads constructor(type: Int = TYPE_UNDEFINED, initialCapacity: Int = DEFAULT_INITIAL_CAPACITY) : StructSupport() {
    private var map: Map<Collection.Key?, Object?>? = null
    private val type: Int
    @Override
    fun getType(): Int {
        return type
    }

    @Override
    fun toDumpData(pageContext: PageContext?, maxlevel: Int, properties: DumpProperties?): DumpData? {
        val type = getType()
        val subject = if (type == Struct.TYPE_REGULAR || type == Struct.TYPE_SYNC || type == Struct.TYPE_UNDEFINED) "Struct" else "Struct (" + StructUtil.toType(type, "").toString() + ")"
        return StructUtil.toDumpTable(this, subject, pageContext, maxlevel, properties)
    }

    @Override
    operator fun get(key: Collection.Key?, defaultValue: Object?): Object? {
        val `val`: Object = map.getOrDefault(key, CollectionUtil.NULL)
        if (`val` === CollectionUtil.NULL) return defaultValue
        return if (`val` == null && !NullSupportHelper.full()) defaultValue else `val`
    }

    @Override
    operator fun get(pc: PageContext?, key: Collection.Key?, defaultValue: Object?): Object? {
        val `val`: Object = map.getOrDefault(key, CollectionUtil.NULL)
        if (`val` === CollectionUtil.NULL) return defaultValue
        return if (`val` == null && !NullSupportHelper.full(pc)) defaultValue else `val`
    }

    fun g(key: Collection.Key?, defaultValue: Object?): Object? {
        return map.getOrDefault(key, defaultValue)
    }

    @Throws(PageException::class)
    fun g(key: Collection.Key?): Object? {
        val res: Object = map.getOrDefault(key, NULL)
        if (res !== NULL) return res
        throw StructSupport.invalidKey(null, this, key, null)
    }

    @Override
    @Throws(PageException::class)
    operator fun get(key: Collection.Key?): Object? {
        val res: Object = map.getOrDefault(key, NULL)
        if (res === NULL) throw StructSupport.invalidKey(null, this, key, null)
        if (res == null && !NullSupportHelper.full()) {
            throw StructSupport.invalidKey(null, this, key, null)
        }
        return res
    }

    @Override
    @Throws(PageException::class)
    operator fun get(pc: PageContext?, key: Key?): Object? {
        val res: Object = map.getOrDefault(key, NULL)
        if (res === NULL) throw StructSupport.invalidKey(null, this, key, null)
        if (res == null && !NullSupportHelper.full(pc)) {
            throw StructSupport.invalidKey(null, this, key, null)
        }
        return res
    }

    @Override
    @Throws(PageException::class)
    operator fun set(key: Collection.Key?, value: Object?): Object? {
        map.put(key, value)
        return value
    }

    @Override
    fun setEL(key: Collection.Key?, value: Object?): Object? {
        map.put(key, value)
        return value
    }

    @Override
    fun size(): Int {
        return map!!.size()
    }

    @Override
    fun keys(): Array<Collection.Key?>? {
        return try {
            map.keySet().toArray(arrayOfNulls<Key?>(map!!.size()))
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            val old: Map<Key?, Object?>? = map
            try {
                map = Collections.synchronizedMap(map)
                val set: Set<Key?> = map.keySet()
                val keys: Array<Collection.Key?> = arrayOfNulls<Collection.Key?>(set.size())
                val it: Iterator<Key?> = set.iterator()
                var count = 0
                while (it.hasNext() && keys.size > count) {
                    keys[count++] = KeyImpl.toKey(it.next(), null)
                }
                keys
            } finally {
                map = old
            }
        }
    }

    @Override
    @Throws(PageException::class)
    fun remove(key: Collection.Key?): Object? {
        if (!map!!.containsKey(key)) throw ExpressionException("can't remove key [$key] from struct, key does not exist")
        val res: Object = map.remove(key)
        if (res != null || NullSupportHelper.full()) return res
        throw ExpressionException("can't remove key [$key] from struct, key value is [null] what is equal do not existing in case full null support is not enabled")
    }

    @Override
    fun removeEL(key: Collection.Key?): Object? {
        return map.remove(key)
    }

    @Override
    fun remove(key: Collection.Key?, defaultValue: Object?): Object? {
        if (!map!!.containsKey(key)) return defaultValue
        val res: Object = map.remove(key)
        return if (res != null || NullSupportHelper.full()) res else defaultValue
    }

    @Override
    fun clear() {
        map.clear()
    }

    @Override
    fun duplicate(deepCopy: Boolean): Collection? {
        val sct: Struct = StructImpl(getType())
        copy(this, sct, deepCopy)
        return sct
    }

    @Override
    fun keyIterator(): Iterator<Collection.Key?>? {
        return map.keySet().iterator()
    }

    @Override
    fun keysAsStringIterator(): Iterator<String?>? {
        return StringIterator(keys())
    }

    @Override
    fun entryIterator(): Iterator<Entry<Key?, Object?>?>? {
        return map.entrySet().iterator()
    }

    @Override
    fun valueIterator(): Iterator<Object?>? {
        return map!!.values().iterator()
    }

    @Override
    fun containsKey(key: Collection.Key?): Boolean {
        return map!!.containsKey(key)
    }

    @Override
    fun containsKey(pc: PageContext?, key: Collection.Key?): Boolean {
        return map!!.containsKey(key)
    }

    @Override
    fun containsValue(value: Object?): Boolean {
        return map!!.containsValue(value)
    }

    @Override
    fun values(): Collection<Object?>? {
        return map!!.values()
    }

    @Override
    override fun hashCode(): Int {
        return super.hashCode()
    }

    @Override
    override fun equals(obj: Object?): Boolean {
        if (obj === this) return true
        if (obj !is StructImpl) return false
        val other = obj as StructImpl?
        if (other!!.size() != size()) return false
        var a: Array<Key?>?
        var b: Array<Key?>?
        Arrays.sort(other.keys().also { a = it })
        Arrays.sort(keys().also { b = it })
        if (!ListUtil.arrayToList(a, ",").equalsIgnoreCase(ListUtil.arrayToList(b, ","))) return false
        for (k in a!!) {
            if (!other[k, ""].equals(get(k, ""))) return false
        }
        return true
    }

    companion object {
        private const val serialVersionUID = 1421746759512286393L
        private const val TYPE_LINKED_NOT_SYNC = 100
        const val TYPE_LINKED_CASESENSITIVE = 256
        const val TYPE_CASESENSITIVE = 512
        private const val DEFAULT_INITIAL_CAPACITY = 32
        val NULL: Object? = Object()
        fun copy(src: Struct?, trg: Struct?, deepCopy: Boolean) {
            val inside: Boolean = ThreadLocalDuplication.set(src, trg)
            try {
                val it: Iterator<Entry<Key?, Object?>?> = src.entryIterator()
                var e: Entry<Key?, Object?>?
                while (it.hasNext()) {
                    e = it.next()
                    if (!deepCopy) trg.setEL(e.getKey(), e.getValue()) else trg.setEL(e.getKey(), Duplicator.duplicate(e.getValue(), deepCopy))
                }
            } finally {
                if (!inside) ThreadLocalDuplication.reset()
            }
        }
    }
    /**
     * This implementation spares its clients from the unspecified, generally chaotic ordering provided
     * by normally Struct , without incurring the increased cost associated with TreeMap. It can be used
     * to produce a copy of a map that has the same order as the original
     *
     * @param type
     * @param initialCapacity initial capacity - MUST be a power of two.
     */
    /**
     * default constructor
     */
    /**
     * This implementation spares its clients from the unspecified, generally chaotic ordering provided
     * by normally Struct , without incurring the increased cost associated with TreeMap. It can be used
     * to produce a copy of a map that has the same order as the original
     *
     * @param type
     */
    init {
        if (type == TYPE_WEAKED) map = Collections.synchronizedMap(WeakHashMap<Collection.Key?, Object?>(initialCapacity)) else if (type == TYPE_SOFT) map = Collections.synchronizedMap(ReferenceMap<Collection.Key?, Object?>(HARD, SOFT, initialCapacity, 0.75f)) else if (type == TYPE_LINKED) map = Collections.synchronizedMap(LinkedHashMap<Collection.Key?, Object?>(initialCapacity)) else if (type == TYPE_LINKED_NOT_SYNC) map = LinkedHashMap<Collection.Key?, Object?>(initialCapacity) else map = MapFactory.getConcurrentMap(initialCapacity)
        this.type = type
    }
}