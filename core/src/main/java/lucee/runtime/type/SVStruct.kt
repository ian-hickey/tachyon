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
package lucee.runtime.type

import java.util.Date

class SVStruct(key: Collection.Key?) : StructSupport(), Reference, Struct {
    private val key: Collection.Key?
    private val parent: StructImpl? = StructImpl()
    @Override
    fun getKey(): Collection.Key? {
        return key
    }

    @Override
    fun getKeyAsString(): String? {
        return key.getString()
    }

    @Override
    @Throws(PageException::class)
    operator fun get(pc: PageContext?): Object? {
        return get(key)
    }

    @Override
    operator fun get(pc: PageContext?, defaultValue: Object?): Object? {
        return get(key, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    operator fun set(pc: PageContext?, value: Object?): Object? {
        return set(key, value)
    }

    @Override
    fun setEL(pc: PageContext?, value: Object?): Object? {
        return setEL(key, value)
    }

    @Override
    @Throws(PageException::class)
    fun remove(pc: PageContext?): Object? {
        return remove(key)
    }

    @Override
    fun removeEL(pc: PageContext?): Object? {
        return removeEL(key)
    }

    @Override
    @Throws(PageException::class)
    fun touch(pc: PageContext?): Object? {
        val o: Object = get(key, null)
        return if (o != null) o else set(key, StructImpl())
    }

    @Override
    fun touchEL(pc: PageContext?): Object? {
        val o: Object = get(key, null)
        return if (o != null) o else setEL(key, StructImpl())
    }

    @Override
    fun getParent(): Object? {
        return parent
    }

    @Override
    fun clear() {
        parent!!.clear()
    }

    @Override
    fun keyIterator(): Iterator<Collection.Key?>? {
        return parent!!.keyIterator()
    }

    @Override
    fun keysAsStringIterator(): Iterator<String?>? {
        return parent!!.keysAsStringIterator()
    }

    @Override
    fun entryIterator(): Iterator<Entry<Key?, Object?>?>? {
        return EntryIterator(this, keys())
    }

    @Override
    fun valueIterator(): Iterator<Object?>? {
        return parent!!.valueIterator()
    }

    @Override
    fun keys(): Array<Collection.Key?>? {
        return parent!!.keys()
    }

    @Override
    fun size(): Int {
        return parent!!.size()
    }

    @Override
    fun toDumpData(pageContext: PageContext?, maxlevel: Int, dp: DumpProperties?): DumpData? {
        return parent!!.toDumpData(pageContext, maxlevel, dp)
    }

    @Override
    @Throws(PageException::class)
    fun castToBooleanValue(): Boolean {
        return Caster.toBooleanValue(get(key))
    }

    @Override
    fun castToBoolean(defaultValue: Boolean?): Boolean? {
        val value: Object = get(key, defaultValue) ?: return defaultValue
        return Caster.toBoolean(value, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun castToDateTime(): DateTime? {
        return Caster.toDate(get(key), null)
    }

    @Override
    fun castToDateTime(defaultValue: DateTime?): DateTime? {
        val value: Object = get(key, defaultValue) ?: return defaultValue
        return DateCaster.toDateAdvanced(value, DateCaster.CONVERTING_TYPE_OFFSET, null, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun castToDoubleValue(): Double {
        return Caster.toDoubleValue(get(key))
    }

    @Override
    fun castToDoubleValue(defaultValue: Double): Double {
        val value: Object = get(key, null) ?: return defaultValue
        return Caster.toDoubleValue(value, true, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun castToString(): String? {
        return Caster.toString(get(key))
    }

    @Override
    fun castToString(defaultValue: String?): String? {
        val value: Object = get(key, null) ?: return defaultValue
        return Caster.toString(value, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(b: Boolean): Int {
        return OpUtil.compare(ThreadLocalPageContext.get(), if (castToBooleanValue()) Boolean.TRUE else Boolean.FALSE, if (b) Boolean.TRUE else Boolean.FALSE)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(dt: DateTime?): Int {
        return OpUtil.compare(ThreadLocalPageContext.get(), castToDateTime() as Date?, dt as Date?)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(d: Double): Int {
        return OpUtil.compare(ThreadLocalPageContext.get(), Double.valueOf(castToDoubleValue()), Double.valueOf(d))
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(str: String?): Int {
        return OpUtil.compare(ThreadLocalPageContext.get(), castToString(), str)
    }

    @Override
    fun duplicate(deepCopy: Boolean): Collection? {
        val svs = SVStruct(key)
        val inside: Boolean = ThreadLocalDuplication.set(this, svs)
        return try {
            val keys: Array<Collection.Key?>? = keys()
            for (i in keys.indices) {
                if (deepCopy) svs.setEL(keys!![i], Duplicator.duplicate(get(keys[i], null), deepCopy)) else svs.setEL(keys!![i], get(keys[i], null))
            }
            svs
        } finally {
            if (!inside) ThreadLocalDuplication.reset()
        }
    }

    @Override
    fun containsKey(key: Collection.Key?): Boolean {
        return parent!!.containsKey(key)
    }

    @Override
    fun containsKey(pc: PageContext?, key: Collection.Key?): Boolean {
        return parent!!.containsKey(pc, key)
    }

    @Override
    @Throws(PageException::class)
    operator fun get(key: Collection.Key?): Object? {
        return parent!!.get(key)
    }

    @Override
    @Throws(PageException::class)
    operator fun get(pc: PageContext?, key: Collection.Key?): Object? {
        return parent.get(pc, key)
    }

    @Override
    operator fun get(key: Collection.Key?, defaultValue: Object?): Object? {
        return parent.get(key, defaultValue)
    }

    @Override
    operator fun get(pc: PageContext?, key: Collection.Key?, defaultValue: Object?): Object? {
        return parent!!.get(pc, key, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun remove(key: Collection.Key?): Object? {
        return parent!!.remove(key)
    }

    @Override
    fun removeEL(key: Collection.Key?): Object? {
        return parent!!.removeEL(key)
    }

    @Override
    @Throws(PageException::class)
    operator fun set(key: Collection.Key?, value: Object?): Object? {
        return parent!!.set(key, value)
    }

    @Override
    fun setEL(key: Collection.Key?, value: Object?): Object? {
        return parent!!.setEL(key, value)
    }

    @Override
    fun containsValue(value: Object?): Boolean {
        return parent!!.containsValue(value)
    }

    @Override
    fun values(): Collection<*>? {
        return parent!!.values()
    }

    @Override
    fun getType(): Int {
        return parent!!.getType()
    }

    /**
     * constructor of the class
     *
     * @param key
     */
    init {
        this.key = key
    }
}