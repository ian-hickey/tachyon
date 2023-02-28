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
class StructImplString : StructImpl, Struct {
    private var map: Map<Collection.Key?, Object?>? = null
    // private static int scount=0;
    // private static int kcount=0;
    /**
     * default constructor
     */
    constructor() {
        map = HashMap<Collection.Key?, Object?>()
    }

    /**
     * This implementation spares its clients from the unspecified, generally chaotic ordering provided
     * by normally Struct , without incurring the increased cost associated with TreeMap. It can be used
     * to produce a copy of a map that has the same order as the original
     *
     * @param doubleLinked
     */
    constructor(type: Int) {
        if (type == TYPE_LINKED) map = LinkedHashMap<Collection.Key?, Object?>() else if (type == TYPE_WEAKED) map = WeakHashMap<Collection.Key?, Object?>() else if (type == TYPE_SYNC) map = MapFactory.Key, Object>getConcurrentMap<Collection.Key?, Object?>() else map = HashMap<Collection.Key?, Object?>()
    }

    /**
     * @see tachyon.runtime.type.Collection.get
     */
    @Override
    override operator fun get(key: Collection.Key?, defaultValue: Object?): Object? {
        val rtn: Object? = map!![key.getLowerString()]
        return if (rtn != null) rtn else defaultValue
    }

    @Override
    @Throws(PageException::class)
    override operator fun get(key: Collection.Key?): Object? {
        val rtn: Object? = map!![key.getLowerString()]
        if (rtn != null) return rtn
        throw invalidKey(key.getString())
    }

    @Override
    @Throws(PageException::class)
    override operator fun get(pc: PageContext?, key: Collection.Key?): Object? {
        val rtn: Object? = map!![key.getLowerString()]
        if (rtn != null) return rtn
        throw invalidKey(key.getString())
    }

    /**
     * @see tachyon.runtime.type.Collection.set
     */
    @Override
    @Throws(PageException::class)
    override operator fun set(key: Collection.Key?, value: Object?): Object? {
        map.put(key, value)
        return value
    }

    /**
     * @see tachyon.runtime.type.Collection.setEL
     */
    @Override
    override fun setEL(key: Collection.Key?, value: Object?): Object? {
        map.put(key, value)
        return value
    }

    /**
     * @see tachyon.runtime.type.Collection.size
     */
    @Override
    override fun size(): Int {
        return map!!.size()
    }

    @Override
    override fun keys(): Array<Collection.Key?>? {
        val it: Iterator<Key?> = map.keySet().iterator()
        val keys: Array<Collection.Key?> = arrayOfNulls<Collection.Key?>(size())
        var count = 0
        while (it.hasNext()) {
            keys[count++] = it.next()
        }
        return keys
    }

    /**
     * @see tachyon.runtime.type.Collection.remove
     */
    @Override
    @Throws(PageException::class)
    override fun remove(key: Collection.Key?): Object? {
        return map.remove(key.getLowerString())
                ?: throw ExpressionException("can't remove key [$key] from struct, key doesn't exist")
    }

    /**
     *
     * @see tachyon.runtime.type.Collection.removeEL
     */
    @Override
    override fun removeEL(key: Collection.Key?): Object? {
        return map.remove(key.getLowerString())
    }

    /**
     * @see tachyon.runtime.type.Collection.clear
     */
    @Override
    override fun clear() {
        map.clear()
    }

    /**
     *
     * @see tachyon.runtime.dump.Dumpable.toDumpData
     */
    @Override
    override fun toDumpData(pageContext: PageContext?, maxlevel: Int, dp: DumpProperties?): DumpData? {
        return StructUtil.toDumpTable(this, "struct", pageContext, maxlevel, dp)
        /*
		 * Iterator it=map.keySet().iterator();
		 * 
		 * DumpTable table = new DumpTable("struct","#9999ff","#ccccff","#000000");
		 * table.setTitle("Struct"); maxlevel--; while(it.hasNext()) { Object key=it.next();
		 * if(DumpUtil.keyValid(dp, maxlevel,key.toString())) table.appendRow(1,new
		 * SimpleDumpData(key.toString()),DumpUtil.toDumpData(map.get(key), pageContext,maxlevel,dp)); }
		 * return table;
		 */
    }

    /**
     * throw exception for invalid key
     *
     * @param key Invalid key
     * @return returns an invalid key Exception
     */
    protected fun invalidKey(key: String?): ExpressionException? {
        return ExpressionException("key [$key] doesn't exist in struct")
    }

    /**
     * @see tachyon.runtime.type.Collection.duplicate
     */
    @Override
    override fun duplicate(deepCopy: Boolean): Collection? {
        val sct: Struct = StructImplString()
        copy(this, sct, deepCopy)
        return sct
    }

    /**
     * @see tachyon.runtime.type.Collection.keyIterator
     */
    @Override
    override fun keyIterator(): Iterator<Collection.Key?>? {
        return map.keySet().iterator()
    }

    /**
     * @see tachyon.runtime.type.Iteratorable.iterator
     */
    @Override
    override fun valueIterator(): Iterator? {
        return map!!.values().iterator()
    }

    @Override
    override fun containsKey(key: Collection.Key?): Boolean {
        return map!!.containsKey(key.getLowerString())
    }

    @Override
    override fun containsKey(pc: PageContext?, key: Collection.Key?): Boolean {
        return map!!.containsKey(key.getLowerString())
    }

    /**
     * @see tachyon.runtime.op.Castable.castToString
     */
    @Override
    @Throws(ExpressionException::class)
    fun castToString(): String? {
        throw ExpressionException("Can't cast Complex Object Type Struct to String", "Use Built-In-Function \"serialize(Struct):String\" to create a String from Struct")
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
        throw ExpressionException("can't cast Complex Object Type Struct to a boolean value")
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
        throw ExpressionException("can't cast Complex Object Type Struct to a number value")
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
        throw ExpressionException("can't cast Complex Object Type Struct to a Date")
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
        throw ExpressionException("can't compare Complex Object Type Struct with a boolean value")
    }

    /**
     * @see tachyon.runtime.op.Castable.compareTo
     */
    @Override
    @Throws(PageException::class)
    operator fun compareTo(dt: DateTime?): Int {
        throw ExpressionException("can't compare Complex Object Type Struct with a DateTime Object")
    }

    /**
     * @see tachyon.runtime.op.Castable.compareTo
     */
    @Override
    @Throws(PageException::class)
    operator fun compareTo(d: Double): Int {
        throw ExpressionException("can't compare Complex Object Type Struct with a numeric value")
    }

    /**
     * @see tachyon.runtime.op.Castable.compareTo
     */
    @Override
    @Throws(PageException::class)
    operator fun compareTo(str: String?): Int {
        throw ExpressionException("can't compare Complex Object Type Struct with a String")
    }

    @Override
    override fun containsValue(value: Object?): Boolean {
        return map!!.containsValue(value)
    }

    @Override
    override fun values(): Collection<*>? {
        return map!!.values()
    }

    companion object {
        const val TYPE_WEAKED = 0
        const val TYPE_LINKED = 1
        const val TYPE_SYNC = 2
        const val TYPE_REGULAR = 3
        fun copy(src: Struct?, trg: Struct?, deepCopy: Boolean) {
            val it: Iterator<Entry<Key?, Object?>?> = src.entryIterator()
            var e: Entry<Key?, Object?>?
            val inside: Boolean = ThreadLocalDuplication.set(src, trg)
            try {
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