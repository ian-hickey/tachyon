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

/**
 * Recordcount Query Column
 */
class QueryColumnRef(query: Query?, columnName: Collection.Key?, type: Int) : QueryColumn, Cloneable {
    private val query: Query?
    private val columnName: Collection.Key?
    private val type: Int
    @Override
    @Throws(DatabaseException::class)
    fun remove(row: Int): Object? {
        throw DatabaseException("can't remove $columnName at row $row value from Query", null, null, null)
    }

    @Override
    fun removeEL(row: Int): Object? {
        return query.getAt(columnName, row, null)
    }

    @Override
    @Throws(PageException::class)
    operator fun get(row: Int): Object? {
        return query.getAt(columnName, row)
    }

    /**
     * touch a value, means if key dosent exist, it will created
     *
     * @param row
     * @return matching value or created value
     * @throws PageException
     */
    @Throws(PageException::class)
    fun touch(row: Int): Object? {
        val _null: Object = NullSupportHelper.NULL()
        val o: Object = query.getAt(columnName, row, _null)
        return if (o !== _null) o else query.setAt(columnName, row, StructImpl())
    }

    fun touchEL(row: Int): Object? {
        val _null: Object = NullSupportHelper.NULL()
        val o: Object = query.getAt(columnName, row, _null)
        return if (o !== _null) o else query.setAtEL(columnName, row, StructImpl())
    }

    @Override
    operator fun get(row: Int, defaultValue: Object?): Object? {
        return query.getAt(columnName, row, defaultValue)
    }

    @Override
    @Throws(DatabaseException::class)
    operator fun set(row: Int, value: Object?): Object? {
        throw DatabaseException("can't change $columnName value from Query", null, null, null)
    }

    @Override
    fun setEL(row: Int, value: Object?): Object? {
        return query.getAt(columnName, row, null)
    }

    @Override
    fun add(value: Object?) {
    }

    @Override
    fun addRow(count: Int) {
    }

    @Override
    fun getType(): Int {
        return type
    }

    @Override
    fun getTypeAsString(): String? {
        return QueryImpl.getColumTypeName(getType())
    }

    @Override
    fun cutRowsTo(maxrows: Int) {
    }

    @Override
    fun size(): Int {
        return query.size()
    }

    @Override
    fun keys(): Array<Collection.Key?>? {
        val k: Array<Collection.Key?> = arrayOfNulls<Collection.Key?>(size())
        for (i in 1..k.size) {
            k[i - 1] = KeyImpl.init(Caster.toString(i))
        }
        return k
    }

    @Override
    @Throws(PageException::class)
    fun remove(key: Collection.Key?): Object? {
        throw DatabaseException("can't remove $key from Query", null, null, null)
    }

    @Override
    fun removeEL(key: Collection.Key?): Object? {
        return get(key, null)
    }

    @Override
    fun remove(key: Collection.Key?, defaultValue: Object?): Object? {
        return get(key, defaultValue)
    }

    @Override
    fun clear() {
    }

    @Override
    @Throws(PageException::class)
    operator fun get(key: String?): Object? {
        return get(Caster.toIntValue(key))
    }

    @Override
    @Throws(PageException::class)
    operator fun get(key: Collection.Key?): Object? {
        return get(Caster.toIntValue(key.getString()))
    }

    @Override
    operator fun get(key: String?, defaultValue: Object?): Object? {
        return get(Caster.toIntValue(key, query.getCurrentrow(ThreadLocalPageContext.get().getId())), defaultValue)
    }

    @Override
    operator fun get(key: Collection.Key?, defaultValue: Object?): Object? {
        return get(Caster.toIntValue(key, query.getCurrentrow(ThreadLocalPageContext.get().getId())), defaultValue)
    }

    @Override
    @Throws(PageException::class)
    operator fun set(key: String?, value: Object?): Object? {
        return set(Caster.toIntValue(key), value)
    }

    @Override
    @Throws(PageException::class)
    operator fun set(key: Collection.Key?, value: Object?): Object? {
        return set(Caster.toIntValue(key), value)
    }

    @Override
    fun setEL(key: String?, value: Object?): Object? {
        return setEL(Caster.toIntValue(key, query.getCurrentrow(ThreadLocalPageContext.get().getId())), value)
    }

    @Override
    fun setEL(key: Collection.Key?, value: Object?): Object? {
        return setEL(Caster.toIntValue(key, query.getCurrentrow(ThreadLocalPageContext.get().getId())), value)
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
        return query.getColumn(columnName, null).valueIterator()
    }

    @Override
    fun containsKey(key: String?): Boolean {
        val _null: Object = NullSupportHelper.NULL()
        return get(key, _null) !== _null
    }

    @Override
    fun containsKey(key: Collection.Key?): Boolean {
        val _null: Object = NullSupportHelper.NULL()
        return get(key, _null) !== _null
    }

    @Override
    fun toDumpData(pageContext: PageContext?, maxlevel: Int, dp: DumpProperties?): DumpData? {
        return DumpUtil.toDumpData(get(query.getCurrentrow(pageContext.getId()), null), pageContext, maxlevel, dp)
    }

    @Override
    @Throws(PageException::class)
    fun castToString(): String? {
        return Caster.toString(get(query.getCurrentrow(ThreadLocalPageContext.get().getId())))
    }

    @Override
    fun castToString(defaultValue: String?): String? {
        val _null: Object = NullSupportHelper.NULL()
        val value: Object = get(query.getCurrentrow(ThreadLocalPageContext.get().getId()), _null)
        return if (value === _null) defaultValue else Caster.toString(value, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun castToBooleanValue(): Boolean {
        return Caster.toBooleanValue(get(query.getCurrentrow(ThreadLocalPageContext.get().getId())))
    }

    @Override
    fun castToBoolean(defaultValue: Boolean?): Boolean? {
        val _null: Object = NullSupportHelper.NULL()
        val value: Object = get(query.getCurrentrow(ThreadLocalPageContext.get().getId()), _null)
        return if (value === _null) defaultValue else Caster.toBoolean(value, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun castToDoubleValue(): Double {
        return Caster.toDoubleValue(get(query.getCurrentrow(ThreadLocalPageContext.get().getId())))
    }

    @Override
    fun castToDoubleValue(defaultValue: Double): Double {
        val _null: Object = NullSupportHelper.NULL()
        val value: Object = get(query.getCurrentrow(ThreadLocalPageContext.get().getId()), _null)
        return if (value === _null) defaultValue else Caster.toDoubleValue(value, true, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun castToDateTime(): DateTime? {
        return Caster.toDate(get(query.getCurrentrow(ThreadLocalPageContext.get().getId())), null)
    }

    @Override
    fun castToDateTime(defaultValue: DateTime?): DateTime? {
        val _null: Object = NullSupportHelper.NULL()
        val value: Object = get(query.getCurrentrow(ThreadLocalPageContext.get().getId()), _null)
        return if (value === _null) defaultValue else DateCaster.toDateAdvanced(value, DateCaster.CONVERTING_TYPE_OFFSET, null, defaultValue)
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
    @Throws(PageException::class)
    fun getKeyAsString(): String? {
        return columnName.toString()
    }

    @Override
    @Throws(PageException::class)
    fun getKey(): Collection.Key? {
        return columnName
    }

    @Override
    @Throws(PageException::class)
    operator fun get(pc: PageContext?): Object? {
        return get(query.getCurrentrow(pc.getId()))
    }

    @Override
    operator fun get(pc: PageContext?, defaultValue: Object?): Object? {
        return get(query.getCurrentrow(pc.getId()), defaultValue)
    }

    @Override
    @Throws(DatabaseException::class)
    fun removeRow(row: Int): Object? {
        throw DatabaseException("can't remove row from Query", null, null, null)
    }

    @Override
    @Throws(PageException::class)
    fun touch(pc: PageContext?): Object? {
        return touch(query.getCurrentrow(pc.getId()))
    }

    @Override
    fun touchEL(pc: PageContext?): Object? {
        return touchEL(query.getCurrentrow(pc.getId()))
    }

    @Override
    @Throws(PageException::class)
    operator fun set(pc: PageContext?, value: Object?): Object? {
        return set(query.getCurrentrow(pc.getId()), value)
    }

    @Override
    fun setEL(pc: PageContext?, value: Object?): Object? {
        return setEL(query.getCurrentrow(pc.getId()), value)
    }

    @Override
    @Throws(PageException::class)
    fun remove(pc: PageContext?): Object? {
        return remove(query.getCurrentrow(pc.getId()))
    }

    @Override
    fun removeEL(pc: PageContext?): Object? {
        return removeEL(query.getCurrentrow(pc.getId()))
    }

    @Override
    fun getParent(): Object? {
        return query
    }

    @Override
    fun clone(): Object {
        return QueryColumnRef(query, columnName, type)
    }

    @Override
    fun duplicate(deepCopy: Boolean): Collection? {
        // MUST muss deepCopy checken
        return QueryColumnRef(query, columnName, type)
    }

    @Override
    fun getIterator(): Iterator<String?>? {
        return keysAsStringIterator()
    }

    @Override
    override fun equals(obj: Object?): Boolean {
        return if (obj !is Collection) false else CollectionUtil.equals(this, obj as Collection?)
    }

    /**
     * This method was added for ACF compatibility per LDEV-1142 and should be avoided if cross engine
     * code is not required. Use instead Query.columnArray() or Query.columnList().listToArray().
     *
     * @return an Array of the names of columns
     * @throws PageException
     */
    @Throws(PageException::class)
    fun listToArray(): Array? {
        if (query is QueryImpl) return ListUtil.listToArray((query as QueryImpl?)!!.getColumnlist(false, ", "), ",")
        throw ApplicationException("Query is not of type QueryImpl. Use instead Query.columnArray() or Query.columnList().listToArray().")
    } /*
	 * @Override public int hashCode() { return CollectionUtil.hashCode(this); }
	 */

    /**
     * Constructor of the class
     *
     * @param query
     * @param columnName
     * @param type
     */
    init {
        this.query = query
        this.columnName = columnName
        this.type = type
    }
}