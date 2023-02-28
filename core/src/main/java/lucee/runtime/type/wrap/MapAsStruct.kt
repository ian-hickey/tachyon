/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
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
package lucee.runtime.type.wrap

import java.util.Iterator

/**
 *
 */
class MapAsStruct private constructor(map: Map?, caseSensitive: Boolean) : StructSupport(), Struct {
    var map: Map?
    private val caseSensitive: Boolean
    @Override
    fun size(): Int {
        return map.size()
    }

    @Override
    fun keys(): Array<Collection.Key?>? {
        val set: Set = map.keySet()
        val it: Iterator = set.iterator()
        val k: Array<Collection.Key?> = arrayOfNulls<Collection.Key?>(set.size())
        var count = 0
        while (it.hasNext()) {
            k[count++] = KeyImpl.init(StringUtil.toStringNative(it.next(), ""))
        }
        return k
    }

    @Override
    @Throws(ExpressionException::class)
    fun remove(key: Collection.Key?): Object? {
        var obj: Object = map.remove(key.getString())
        if (obj == null) {
            if (map.containsKey(key.getString())) return null
            if (!caseSensitive) {
                val csKey = getCaseSensitiveKey(map, key.getString())
                if (csKey != null) obj = map.remove(csKey)
                if (obj != null) return obj
            }
            throw ExpressionException("can't remove key [" + key.getString().toString() + "] from map, key doesn't exist")
        }
        return obj
    }

    @Override
    fun removeEL(key: Collection.Key?): Object? {
        var obj: Object = map.remove(key.getString())
        if (!caseSensitive && obj == null) {
            val csKey = getCaseSensitiveKey(map, key.getString())
            if (csKey != null) obj = map.remove(csKey)
        }
        return obj
    }

    @Override
    fun clear() {
        map.clear()
    }

    @Override
    @Throws(ExpressionException::class)
    operator fun get(key: Collection.Key?): Object? {
        return get(null as PageContext?, key)
    }

    @Override
    @Throws(ExpressionException::class)
    operator fun get(pc: PageContext?, key: Collection.Key?): Object? {
        var o: Object = map.get(key.getString())
        if (o == null) {
            if (map.containsKey(key.getString())) return null
            if (!caseSensitive) {
                val csKey = getCaseSensitiveKey(map, key.getString())
                if (csKey != null) o = map.get(csKey)
                if (o != null || map.containsKey(csKey)) return o
            }
            throw ExpressionException("key " + key.getString().toString() + " doesn't exist in " + Caster.toClassName(map))
        }
        return o
    }

    @Override
    operator fun get(key: Collection.Key?, defaultValue: Object?): Object? {
        return get(null as PageContext?, key, defaultValue)
    }

    @Override
    operator fun get(pc: PageContext?, key: Collection.Key?, defaultValue: Object?): Object? {
        var obj: Object = map.get(key.getString())
        if (obj == null) {
            if (map.containsKey(key.getString())) return null
            if (!caseSensitive) {
                val csKey = getCaseSensitiveKey(map, key.getString())
                if (csKey != null) obj = map.get(csKey)
                if (obj != null || map.containsKey(csKey)) return obj
            }
            return defaultValue
        }
        return obj
    }

    @Override
    @Throws(PageException::class)
    operator fun set(key: Collection.Key?, value: Object?): Object? {
        return map.put(if (caseSensitive) key.getString() else key, value)
    }

    @Override
    fun setEL(key: Collection.Key?, value: Object?): Object? {
        return map.put(if (caseSensitive) key.getString() else key, value)
    }

    @Override
    fun keyIterator(): Iterator<Collection.Key?>? {
        return KeyIterator(keys())
    }

    @Override
    fun keysAsStringIterator(): Iterator<String?>? {
        return StringIterator(keys())
    }

    @Override
    fun entryIterator(): Iterator<Entry<Key?, Object?>?>? {
        return EntryIterator(this, keys())
    }

    @Override
    fun valueIterator(): Iterator<Object?>? {
        return ValueIterator(this, keys())
    }

    @Override
    fun toDumpData(pageContext: PageContext?, maxlevel: Int, dp: DumpProperties?): DumpData? {
        return DumpUtil.toDumpData(map, pageContext, maxlevel, dp)
    }

    @Override
    fun duplicate(deepCopy: Boolean): Collection? {
        return MapAsStruct(Duplicator.duplicateMap(map, deepCopy), caseSensitive)
    }

    @Override
    fun containsKey(key: Collection.Key?): Boolean {
        return containsKey(null, key)
    }

    @Override
    fun containsKey(pc: PageContext?, key: Collection.Key?): Boolean {
        val contains: Boolean = map.containsKey(key.getString())
        if (contains) return true
        return if (!caseSensitive) map.containsKey(getCaseSensitiveKey(map, key.getString())) else false
    }

    @Override
    @Throws(ExpressionException::class)
    fun castToString(): String? {
        throw ExpressionException("Can't cast Complex Object Type Struct [" + getClass().getName().toString() + "] to String",
                "Use Built-In-Function \"serialize(Struct):String\" to create a String from Struct")
    }

    @Override
    fun castToString(defaultValue: String?): String? {
        return defaultValue
    }

    @Override
    @Throws(ExpressionException::class)
    fun castToBooleanValue(): Boolean {
        throw ExpressionException("Can't cast Complex Object Type Struct [" + getClass().getName().toString() + "] to a boolean value")
    }

    @Override
    fun castToBoolean(defaultValue: Boolean?): Boolean? {
        return defaultValue
    }

    @Override
    @Throws(ExpressionException::class)
    fun castToDoubleValue(): Double {
        throw ExpressionException("Can't cast Complex Object Type Struct [" + getClass().getName().toString() + "] to a number value")
    }

    @Override
    fun castToDoubleValue(defaultValue: Double): Double {
        return defaultValue
    }

    @Override
    @Throws(ExpressionException::class)
    fun castToDateTime(): DateTime? {
        throw ExpressionException("Can't cast Complex Object Type Struct [" + getClass().getName().toString() + "] to a Date")
    }

    @Override
    fun castToDateTime(defaultValue: DateTime?): DateTime? {
        return defaultValue
    }

    @Override
    @Throws(ExpressionException::class)
    operator fun compareTo(b: Boolean): Int {
        throw ExpressionException("can't compare Complex Object Type Struct [" + getClass().getName().toString() + "] with a boolean value")
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(dt: DateTime?): Int {
        throw ExpressionException("can't compare Complex Object Type Struct [" + getClass().getName().toString() + "] with a DateTime Object")
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(d: Double): Int {
        throw ExpressionException("can't compare Complex Object Type Struct [" + getClass().getName().toString() + "] with a numeric value")
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(str: String?): Int {
        throw ExpressionException("can't compare Complex Object Type Struct [" + getClass().getName().toString() + "] with a String")
    }

    @Override
    fun containsValue(value: Object?): Boolean {
        return map.containsValue(value)
    }

    @Override
    fun values(): Collection<*>? {
        return map.values()
    }

    @Override
    fun getType(): Int {
        return StructUtil.getType(map)
    }

    companion object {
        fun toStruct(map: Map?): Struct? {
            return toStruct(map, false)
        }

        fun toStruct(map: Map?, caseSensitive: Boolean): Struct? {
            return if (map is Struct) map as Struct? else MapAsStruct(map, caseSensitive)
        }

        fun getCaseSensitiveKey(map: Map?, key: String?): String? {
            val it: Iterator = map.keySet().iterator()
            var strKey: String
            while (it.hasNext()) {
                strKey = Caster.toString(it.next(), "")
                if (strKey.equalsIgnoreCase(key)) return strKey
            }
            return null
        }
    }

    /**
     * constructor of the class
     *
     * @param map
     * @param caseSensitive
     */
    init {
        this.map = map
        this.caseSensitive = caseSensitive
    }
}