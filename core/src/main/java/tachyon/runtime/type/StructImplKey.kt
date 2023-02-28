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

import java.util.HashMap

/**
 * CFML data type struct
 */
class StructImplKey : StructSupport, Struct {
    private var _map: Map<Collection.Key?, Object?>? = null
    // private static int scount=0;
    // private static int kcount=0;
    /**
     * default constructor
     */
    constructor() {
        _map = HashMap<Collection.Key?, Object?>()
    }

    /**
     * This implementation spares its clients from the unspecified, generally chaotic ordering provided
     * by normally Struct , without incurring the increased cost associated with TreeMap. It can be used
     * to produce a copy of a map that has the same order as the original
     *
     * @param doubleLinked
     */
    constructor(type: Int) {
        if (type == TYPE_LINKED) _map = LinkedHashMap<Collection.Key?, Object?>() else if (type == TYPE_WEAKED) _map = WeakHashMap<Collection.Key?, Object?>() else if (type == TYPE_SYNC) _map = MapFactory.Key, Object>getConcurrentMap<Collection.Key?, Object?>() else _map = HashMap<Collection.Key?, Object?>()
    }

    @Override
    operator fun get(key: Collection.Key?, defaultValue: Object?): Object? {
        val rtn: Object? = _map!![key]
        return if (rtn != null) rtn else defaultValue
    }

    @Override
    operator fun get(pc: PageContext?, key: Collection.Key?, defaultValue: Object?): Object? {
        val rtn: Object? = _map!![key]
        return if (rtn != null) rtn else defaultValue
    }

    @Override
    @Throws(PageException::class)
    operator fun get(key: Collection.Key?): Object? { // print.out("k:"+(kcount++));
        val rtn: Object? = _map!![key]
        if (rtn != null) return rtn
        throw invalidKey(key.getString())
    }

    @Override
    @Throws(PageException::class)
    operator fun get(pc: PageContext?, key: Collection.Key?): Object? { // print.out("k:"+(kcount++));
        val rtn: Object? = _map!![key]
        if (rtn != null) return rtn
        throw invalidKey(key.getString())
    }

    /**
     * @see tachyon.runtime.type.Collection.set
     */
    @Override
    @Throws(PageException::class)
    operator fun set(key: Collection.Key?, value: Object?): Object? {
        _map.put(key, value)
        return value
    }

    /**
     * @see tachyon.runtime.type.Collection.setEL
     */
    @Override
    fun setEL(key: Collection.Key?, value: Object?): Object? {
        _map.put(key, value)
        return value
    }

    /**
     * @see tachyon.runtime.type.Collection.size
     */
    @Override
    fun size(): Int {
        return _map!!.size()
    }

    @Override
    fun keys(): Array<Collection.Key?>? { // print.out("keys");
        val it: Iterator<Key?>? = keyIterator()
        val keys: Array<Collection.Key?> = arrayOfNulls<Collection.Key?>(size())
        var count = 0
        while (it!!.hasNext()) {
            keys[count++] = it.next()
        }
        return keys
    }

    /**
     * @see tachyon.runtime.type.Collection.remove
     */
    @Override
    @Throws(PageException::class)
    fun remove(key: Collection.Key?): Object? {
        return _map.remove(key)
                ?: throw ExpressionException("Cannot remove key [" + key.getString().toString() + "] from struct, the key doesn't exist")
    }

    @Override
    fun removeEL(key: Collection.Key?): Object? {
        return _map.remove(key)
    }

    /**
     * @see tachyon.runtime.type.Collection.clear
     */
    @Override
    fun clear() {
        _map.clear()
    }

    /**
     *
     * @see tachyon.runtime.dump.Dumpable.toDumpData
     */
    @Override
    fun toDumpData(pageContext: PageContext?, maxlevel: Int, dp: DumpProperties?): DumpData? {
        var maxlevel = maxlevel
        val it: Iterator = _map.keySet().iterator()
        val table = DumpTable("struct", "#9999ff", "#ccccff", "#000000")
        table.setTitle("Struct")
        maxlevel--
        val maxkeys: Int = dp.getMaxKeys()
        var index = 0
        while (it.hasNext()) {
            val key: Object = it.next()
            if (DumpUtil.keyValid(dp, maxlevel, key.toString())) {
                if (maxkeys <= index++) break
                table.appendRow(1, SimpleDumpData(key.toString()), DumpUtil.toDumpData(_map!![key], pageContext, maxlevel, dp))
            }
        }
        return table
    }

    /**
     * throw exception for invalid key
     *
     * @param key Invalid key
     * @return returns an invalid key Exception
     */
    protected fun invalidKey(key: String?): ExpressionException? {
        return ExpressionException("Key [$key] doesn't exist in struct")
    }

    /**
     * @see tachyon.runtime.type.Collection.duplicate
     */
    @Override
    fun duplicate(deepCopy: Boolean): Collection? {
        val sct: Struct = StructImplKey()
        copy(this, sct, deepCopy)
        return sct
    }

    @Override
    fun keyIterator(): Iterator<Collection.Key?>? {
        return _map.keySet().iterator()
    }

    @Override
    fun keysAsStringIterator(): Iterator<String?>? {
        return StringIterator(keys())
    }

    @Override
    fun entryIterator(): Iterator<Entry<Key?, Object?>?>? {
        return EntryIterator(this, keys())
    }

    /**
     * @see tachyon.runtime.type.Iteratorable.iterator
     */
    @Override
    fun valueIterator(): Iterator? {
        return _map!!.values().iterator()
    }

    @Override
    fun containsKey(key: Collection.Key?): Boolean {
        return _map!!.containsKey(key)
    }

    @Override
    fun containsKey(pc: PageContext?, key: Collection.Key?): Boolean {
        return _map!!.containsKey(key)
    }

    /**
     * @see tachyon.runtime.op.Castable.castToString
     */
    @Override
    @Throws(ExpressionException::class)
    fun castToString(): String? {
        throw ExpressionException("Cannot cast [Struct] to String", "Use Built-In-Function \"serialize(Struct):String\" to create a String from Struct")
    }

    /**
     * @see tachyon.runtime.type.util.StructSupport.castToString
     */
    @Override
    fun castToString(defaultValue: String?): String? {
        return defaultValue
    }

    /**
     * @see tachyon.runtime.op.Castable.castToBooleanValue
     */
    @Override
    @Throws(ExpressionException::class)
    fun castToBooleanValue(): Boolean {
        throw ExpressionException("Cannot cast [Struct] to a boolean value")
    }

    /**
     * @see tachyon.runtime.op.Castable.castToBoolean
     */
    @Override
    fun castToBoolean(defaultValue: Boolean?): Boolean? {
        return defaultValue
    }

    /**
     * @see tachyon.runtime.op.Castable.castToDoubleValue
     */
    @Override
    @Throws(ExpressionException::class)
    fun castToDoubleValue(): Double {
        throw ExpressionException("Cannot cast [Struct] to a numeric value")
    }

    /**
     * @see tachyon.runtime.op.Castable.castToDoubleValue
     */
    @Override
    fun castToDoubleValue(defaultValue: Double): Double {
        return defaultValue
    }

    /**
     * @see tachyon.runtime.op.Castable.castToDateTime
     */
    @Override
    @Throws(ExpressionException::class)
    fun castToDateTime(): DateTime? {
        throw ExpressionException("Cannot cast [Struct] to a Date")
    }

    /**
     * @see tachyon.runtime.op.Castable.castToDateTime
     */
    @Override
    fun castToDateTime(defaultValue: DateTime?): DateTime? {
        return defaultValue
    }

    /**
     * @see tachyon.runtime.op.Castable.compare
     */
    @Override
    @Throws(ExpressionException::class)
    operator fun compareTo(b: Boolean): Int {
        throw ExpressionException("Cannot compare a [Struct] with a boolean value")
    }

    /**
     * @see tachyon.runtime.op.Castable.compareTo
     */
    @Override
    @Throws(PageException::class)
    operator fun compareTo(dt: DateTime?): Int {
        throw ExpressionException("Cannot compare a [Struct] with a DateTime Object")
    }

    /**
     * @see tachyon.runtime.op.Castable.compareTo
     */
    @Override
    @Throws(PageException::class)
    operator fun compareTo(d: Double): Int {
        throw ExpressionException("Cannot compare a [Struct] with a numeric value")
    }

    /**
     * @see tachyon.runtime.op.Castable.compareTo
     */
    @Override
    @Throws(PageException::class)
    operator fun compareTo(str: String?): Int {
        throw ExpressionException("Cannot compare a [Struct] with a String")
    }

    @Override
    fun containsValue(value: Object?): Boolean {
        return _map!!.containsValue(value)
    }

    @Override
    fun values(): Collection<*>? {
        return _map!!.values()
    }

    @Override
    fun getType(): Int {
        return StructUtil.getType(_map)
    }

    companion object {
        const val TYPE_WEAKED = 0
        const val TYPE_LINKED = 1
        const val TYPE_SYNC = 2
        const val TYPE_REGULAR = 3
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
}