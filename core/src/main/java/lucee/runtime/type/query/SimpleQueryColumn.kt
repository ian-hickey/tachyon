/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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
package lucee.runtime.type.query

import java.io.IOException

class SimpleQueryColumn(qry: SimpleQuery?, res: ResultSet?, key: Collection.Key?, type: Int, index: Int) : QueryColumn, Cloneable {
    private val qry: SimpleQuery?
    private val key: Key?
    private val type = 0
    private val res: ResultSet?
    private var cast: Cast? = null
    private val index: Int

    @Override
    operator fun get(key: Key?, defaultValue: Object?): Object? {
        val row: Int = Caster.toIntValue(key, Integer.MIN_VALUE)
        if (row == Integer.MIN_VALUE) {
            val child: Object? = getChildElement(key, null)
            return if (child != null) child else defaultValue
        }
        return get(row, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    operator fun get(key: Key?): Object? {
        val row: Int = Caster.toIntValue(key, Integer.MIN_VALUE)
        if (row == Integer.MIN_VALUE) {
            val child: Object? = getChildElement(key, null)
            if (child != null) return child
            throw DatabaseException("key [$key] not found", null, null, null)
        }
        return get(row)
    }

    private fun getChildElement(key: Key?, defaultValue: Object?): Object? {
        val pc: PageContext = ThreadLocalPageContext.get()
        // column and query has same name
        if (key.equals(this.key)) {
            return get(qry!!.getCurrentrow(pc.getId()), defaultValue)
        }
        // get it from undefined scope
        if (pc != null) {
            val undefined: Undefined = pc.undefinedScope()
            val old: Boolean = undefined.setAllowImplicidQueryCall(false)
            val sister: Object = undefined.get(this.key, null)
            undefined.setAllowImplicidQueryCall(old)
            if (sister != null) {
                return try {
                    pc.get(sister, key)
                } catch (e: PageException) {
                    defaultValue
                }
            }
        }
        return defaultValue
    }

    @Override
    fun size(): Int {
        throw SimpleQuery.notSupported()
    }

    @Override
    fun keys(): Array<Key?>? {
        throw SimpleQuery.notSupported()
    }

    @Override
    @Throws(PageException::class)
    fun remove(key: Key?): Object? {
        throw SimpleQuery.notSupported()
    }

    @Override
    fun removeEL(key: Key?): Object? {
        throw SimpleQuery.notSupported()
    }

    @Override
    fun remove(key: Key?, defaultValue: Object?): Object? {
        throw SimpleQuery.notSupported()
    }

    @Override
    fun clear() {
        throw SimpleQuery.notSupported()
    }

    @Override
    @Throws(PageException::class)
    operator fun get(key: String?): Object? {
        return get(KeyImpl.init(key))
    }

    @Override
    operator fun get(key: String?, defaultValue: Object?): Object? {
        return get(KeyImpl.init(key), defaultValue)
    }

    @Override
    @Throws(PageException::class)
    operator fun set(key: String?, value: Object?): Object? {
        throw SimpleQuery.notSupported()
    }

    @Override
    @Throws(PageException::class)
    operator fun set(key: Key?, value: Object?): Object? {
        throw SimpleQuery.notSupported()
    }

    @Override
    fun setEL(key: String?, value: Object?): Object? {
        throw SimpleQuery.notSupported()
    }

    @Override
    fun setEL(key: Key?, value: Object?): Object? {
        throw SimpleQuery.notSupported()
    }

    @Override
    fun duplicate(deepCopy: Boolean): Collection? {
        throw SimpleQuery.notSupported()
    }

    @Override
    fun containsKey(key: String?): Boolean {
        throw SimpleQuery.notSupported()
    }

    @Override
    fun containsKey(key: Key?): Boolean {
        throw SimpleQuery.notSupported()
    }

    @Override
    fun toDumpData(pageContext: PageContext?, maxlevel: Int, properties: DumpProperties?): DumpData? {
        throw SimpleQuery.notSupported()
    }

    @Override
    fun keyIterator(): Iterator<Collection.Key?>? {
        throw SimpleQuery.notSupported()
    }

    @Override
    fun keysAsStringIterator(): Iterator<String?>? {
        throw SimpleQuery.notSupported()
    }

    @Override
    fun entryIterator(): Iterator<Entry<Key?, Object?>?>? {
        return EntryIterator(this, keys())
    }

    @Override
    fun valueIterator(): Iterator<Object?>? {
        throw SimpleQuery.notSupported()
    }

    @Override
    @Throws(PageException::class)
    fun castToString(): String? {
        // TODO Auto-generated method stub
        return Caster.toString(get(key))
    }

    @Override
    fun castToString(defaultValue: String?): String? {
        return Caster.toString(get(key, defaultValue), defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun castToBooleanValue(): Boolean {
        return Caster.toBoolean(get(key))
    }

    @Override
    fun castToBoolean(defaultValue: Boolean?): Boolean? {
        return Caster.toBoolean(get(key, defaultValue), defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun castToDoubleValue(): Double {
        return Caster.toDoubleValue(get(key))
    }

    @Override
    fun castToDoubleValue(defaultValue: Double): Double {
        return Caster.toDoubleValue(get(key, defaultValue), true, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun castToDateTime(): DateTime? {
        return Caster.toDate(get(key), false, null)
    }

    @Override
    fun castToDateTime(defaultValue: DateTime?): DateTime? {
        return Caster.toDate(get(key, defaultValue), false, null, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(str: String?): Int {
        throw SimpleQuery.notSupported()
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(b: Boolean): Int {
        throw SimpleQuery.notSupported()
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(d: Double): Int {
        throw SimpleQuery.notSupported()
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(dt: DateTime?): Int {
        throw SimpleQuery.notSupported()
    }

    @Override
    fun getKeyAsString(): String? {
        return key.getString()
    }

    @Override
    fun getKey(): Key? {
        return key
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
        throw SimpleQuery.notSupported()
    }

    @Override
    fun setEL(pc: PageContext?, value: Object?): Object? {
        throw SimpleQuery.notSupported()
    }

    @Override
    @Throws(PageException::class)
    fun remove(pc: PageContext?): Object? {
        throw SimpleQuery.notSupported()
    }

    @Override
    fun removeEL(pc: PageContext?): Object? {
        throw SimpleQuery.notSupported()
    }

    @Override
    @Throws(PageException::class)
    fun touch(pc: PageContext?): Object? {
        throw SimpleQuery.notSupported()
    }

    @Override
    fun touchEL(pc: PageContext?): Object? {
        throw SimpleQuery.notSupported()
    }

    @Override
    fun getParent(): Object? {
        return qry
    }

    @Override
    @Throws(PageException::class)
    fun remove(row: Int): Object? {
        throw SimpleQuery.notSupported()
    }

    @Override
    @Throws(PageException::class)
    fun removeRow(row: Int): Object? {
        throw SimpleQuery.notSupported()
    }

    @Override
    fun removeEL(row: Int): Object? {
        throw SimpleQuery.notSupported()
    }

    @Override
    @Synchronized
    @Throws(PageException::class)
    operator fun get(row: Int): Object? {
        return try {
            if (row != res.getRow()) {
                res.absolute(row)
            }
            _get(row)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            throw Caster.toPageException(t)
        }
    }

    @Override
    @Synchronized
    operator fun get(row: Int, defaultValue: Object?): Object? {
        return try {
            if (row != res.getRow()) {
                res.absolute(row)
            }
            _get(row)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            defaultValue
        }
    }

    @Throws(SQLException::class, IOException::class)
    private fun _get(row: Int): Object? {
        return cast.toCFType(null, res, index)
    }

    @Override
    @Throws(PageException::class)
    operator fun set(row: Int, value: Object?): Object? {
        throw SimpleQuery.notSupported()
    }

    @Override
    fun add(value: Object?) {
        throw SimpleQuery.notSupported()
    }

    @Override
    fun setEL(row: Int, value: Object?): Object? {
        throw SimpleQuery.notSupported()
    }

    @Override
    fun addRow(count: Int) {
        throw SimpleQuery.notSupported()
    }

    @Override
    fun getType(): Int {
        return type
    }

    @Override
    fun getTypeAsString(): String? {
        return QueryImpl.getColumTypeName(type)
    }

    @Override
    fun cutRowsTo(maxrows: Int) {
        throw SimpleQuery.notSupported()
    }

    @Override
    fun clone(): Object {
        throw SimpleQuery.notSupported()
    }

    fun getIndex(): Int {
        return index
    }

    @Override
    fun getIterator(): Iterator<String?>? {
        return keysAsStringIterator()
    }

    companion object {
        private const val serialVersionUID = 288731277532671308L
    }

    // private Object[] data;
    init {
        this.qry = qry
        this.res = res
        this.key = key
        this.index = index
        cast = try {
            QueryUtil.toCast(res, type)
        } catch (e: Exception) {
            throw SimpleQuery.toRuntimeExc(e)
        }
    }
}