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

import java.util.ArrayList

/**
 * implementation of the query column
 */
class QueryColumnImpl : QueryColumnPro, Objects, Cloneable {
    var type = 0
    protected var size: AtomicInteger? = AtomicInteger(0)
    var data: Array<Object?>?
    var typeChecked = false
    protected var query: QueryImpl? = null
    protected var key: Collection.Key? = null
    private val sync: Object? = SerializableObject()

    /**
     * constructor with type
     *
     * @param query
     * @param key
     * @param type
     */
    constructor(query: QueryImpl?, key: Collection.Key?, type: Int) {
        data = arrayOfNulls<Object?>(CAPACITY)
        this.type = type
        this.key = key
        this.query = query
    }

    /**
     * constructor with array
     *
     * @param query
     * @param array
     * @param type
     */
    constructor(query: QueryImpl?, key: Collection.Key?, array: Array?, type: Int) {
        data = array.toArray()
        size = AtomicInteger(array.size())
        this.type = type
        this.query = query
        this.key = key
    }

    /**
     * @param query
     * @param type type as (java.sql.Types.XYZ) int
     * @param size
     */
    constructor(query: QueryImpl?, key: Collection.Key?, type: Int, size: Int) {
        data = arrayOfNulls<Object?>(size)
        this.type = type
        this.size = AtomicInteger(size)
        this.query = query
        this.key = key
    }

    /**
     * Constructor of the class for internal usage only
     */
    constructor() {}

    @Override
    fun size(): Int {
        return size.get()
    }

    @Override
    fun keys(): Array<Collection.Key?>? {
        val k: Array<Collection.Key?> = arrayOfNulls<Collection.Key?>(size())
        val len = k.size
        for (i in 1..len) {
            k[i - 1] = KeyImpl.init(Caster.toString(i))
        }
        return k
    }

    @Override
    @Throws(PageException::class)
    fun remove(key: Collection.Key?): Object? {
        resetTypeSync()
        return set(Caster.toIntValue(key.getString()), "")
    }

    @Override
    fun remove(key: Collection.Key?, defaultValue: Object?): Object? {
        resetTypeSync()
        return try {
            set(Caster.toIntValue(key.getString()), "")
        } catch (e: PageException) {
            defaultValue
        }
    }

    @Override
    @Throws(DatabaseException::class)
    fun remove(row: Int): Object? {
        query!!.disableIndex()
        // query.disconnectCache();
        resetTypeSync()
        return set(row, "")
    }

    @Override
    fun removeEL(key: Collection.Key?): Object? {
        query!!.disableIndex()
        // query.disconnectCache();
        resetTypeSync()
        return setEL(Caster.toIntValue(key.getString(), -1), "")
    }

    @Override
    fun removeEL(row: Int): Object? {
        query!!.disableIndex()
        // query.disconnectCache();
        resetTypeSync()
        return setEL(row, "")
    }

    @Override
    fun clear() {
        query!!.disableIndex()
        synchronized(sync) {
            resetType()
            data = arrayOfNulls<Object?>(CAPACITY)
            size.set(0)
        }
    }

    @Override
    @Throws(PageException::class)
    fun remove(pc: PageContext?): Object? {
        return remove(query!!.getCurrentrow(pc.getId()))
    }

    @Override
    fun removeEL(pc: PageContext?): Object? {
        return removeEL(query!!.getCurrentrow(pc.getId()))
    }

    @Override
    @Throws(PageException::class)
    operator fun get(key: String?): Object? {
        return get(KeyImpl.init(key))
    }

    @Override
    @Throws(PageException::class)
    operator fun get(key: Key?): Object? {
        return get(null as PageContext?, key)
    }

    @Override
    @Throws(PageException::class)
    operator fun get(pc: PageContext?, key: Collection.Key?): Object? {
        val row: Int = Caster.toIntValue(key.getString(), Integer.MIN_VALUE)
        if (row == Integer.MIN_VALUE) {
            val _null: Object = NullSupportHelper.NULL(pc)
            val child: Object? = getChildElement(pc, key, _null)
            if (child !== _null) return child
            throw DatabaseException("key [$key] not found", null, null, null)
        }
        return QueryUtil.getValue(pc, this, row)
    }

    private fun getChildElement(pc: PageContext?, key: Key?, defaultValue: Object?): Object? { // pc maybe null
        var pc: PageContext? = pc
        val coll: Collection = Caster.toCollection(QueryUtil.getValue(this, query!!.getCurrentrow(pc.getId())), null)
        if (coll != null) {
            val res: Object = coll.get(key, CollectionUtil.NULL)
            if (res !== CollectionUtil.NULL) return res
        }

        // column and query has same name
        if (key.equals(this.key)) {
            return query.get(key, defaultValue)
        }

        // get it from undefined scope
        pc = ThreadLocalPageContext.get(pc)
        if (pc != null) {
            val _null: Object = NullSupportHelper.NULL(pc)
            val undefined: Undefined = pc.undefinedScope()
            val old: Boolean = undefined.setAllowImplicidQueryCall(false)
            val sister: Object = undefined.get(this.key, _null)
            undefined.setAllowImplicidQueryCall(old)
            if (sister !== _null) {
                return try {
                    pc.get(sister, key)
                } catch (e: PageException) {
                    defaultValue
                }
            }
        }
        return defaultValue
    }

    /**
     * touch the given line on the column at given row
     *
     * @param row
     * @return new row or existing
     * @throws DatabaseException
     */
    fun touch(row: Int): Object? {
        if (row < 1 || row > size()) return if (NullSupportHelper.full()) null else ""
        val o: Object? = data!![row - 1]
        return if (o != null) o else setEL(row, StructImpl())
    }

    /**
     * touch the given line on the column at given row
     *
     * @param row
     * @return new row or existing
     * @throws DatabaseException
     */
    fun touchEL(row: Int): Object? {
        return touch(row)
    }

    @Override
    operator fun get(key: Key?, defaultValue: Object?): Object? {
        return get(null, key, defaultValue)
    }

    @Override
    operator fun get(pc: PageContext?, key: Collection.Key?, defaultValue: Object?): Object? { // pc maybe null
        val row: Int = Caster.toIntValue(key.getString(), Integer.MIN_VALUE)
        return if (row == Integer.MIN_VALUE) {
            getChildElement(pc, key, defaultValue)
        } else get(row, defaultValue)
    }

    @Override
    operator fun get(key: String?, defaultValue: Object?): Object? {
        return get(KeyImpl.init(key), defaultValue)
    }

    @Override
    @Throws(DeprecatedException::class)
    operator fun get(row: Int): Object? {
        throw DeprecatedException("this method is no longer supported, use instead get(int,Object)")
        // return QueryUtil.getValue(this,row);
    }

    @Override
    operator fun get(row: Int, emptyValue: Object?): Object? {
        if (row < 1 || row > size()) return emptyValue
        return if (data!![row - 1] == null) emptyValue else data!![row - 1]
    }

    @Override
    @Throws(PageException::class)
    operator fun set(key: String?, value: Object?): Object? {
        val row: Int = Caster.toIntValue(key, Integer.MIN_VALUE)
        return if (row == Integer.MIN_VALUE) query.set(key, value) else set(row, value)
    }

    @Override
    @Throws(PageException::class)
    operator fun set(key: Collection.Key?, value: Object?): Object? {
        val row: Int = Caster.toIntValue(key.getString(), Integer.MIN_VALUE)
        return if (row == Integer.MIN_VALUE) query.set(key, value) else set(row, value)
    }

    @Override
    @Throws(DatabaseException::class)
    operator fun set(row: Int, value: Object?): Object? {
        return set(row, value, false)
    }

    // Pass trustType=true to optimize operations such as QoQ where lots of values are being moved
    // around between query objects but we know the types are already fine and don't need to
    // redefine them every time
    @Throws(DatabaseException::class)
    operator fun set(row: Int, value: Object?, trustType: Boolean): Object? {
        var value: Object? = value
        query!!.disableIndex()
        // query.disconnectCache();
        if (row < 1) throw DatabaseException("invalid row number [$row]", "valid row numbers a greater or equal to one", null, null)
        if (row > size()) {
            if (size() == 0) throw DatabaseException("cannot set a value to an empty query, you first have to add a row", null, null, null)
            throw DatabaseException("invalid row number [$row]", "valid row numbers goes from 1 to " + size(), null, null)
        }
        if (!trustType) value = reDefineType(value)
        synchronized(sync) { data!![row - 1] = value }
        return value
    }

    @Override
    fun setEL(key: String?, value: Object?): Object? {
        val index: Int = Caster.toIntValue(key, Integer.MIN_VALUE)
        if (index == Integer.MIN_VALUE) query.setEL(key, value)
        return setEL(index, value)
    }

    @Override
    fun setEL(key: Collection.Key?, value: Object?): Object? {
        val index: Int = Caster.toIntValue(key.getString(), Integer.MIN_VALUE)
        if (index == Integer.MIN_VALUE) query.setEL(key, value)
        return setEL(index, value)
    }

    @Override
    fun setEL(row: Int, value: Object?): Object? {
        var value: Object? = value
        query!!.disableIndex()
        if (row < 1 || row > size()) return value
        synchronized(sync) {
            value = reDefineType(value)
            data!![row - 1] = value
        }
        return value
    }

    @Override
    fun add(value: Object?) {
        query!!.disableIndex()
        growTo(size() + 1)
        data!![size.incrementAndGet() - 1] = value
    }

    @Override
    fun cutRowsTo(maxrows: Int) {
        synchronized(sync) { if (maxrows > -1 && maxrows < size()) size.set(maxrows) }
    }

    @Override
    fun addRow(count: Int) {
        query!!.disableIndex()
        // Grow the column if needed.  This method will lock if it needs to
        growTo(size() + count)
        size.addAndGet(count)
    }

    @Override
    @Throws(DatabaseException::class)
    fun removeRow(row: Int): Object? {
        query!!.disableIndex()
        // query.disconnectCache();
        if (row < 1 || row > size()) throw DatabaseException("invalid row number [$row]", "valid rows goes from 1 to " + size(), null, null)
        synchronized(sync) {
            val o: Object? = data!![row - 1]
            for (i in row until size()) {
                data!![i - 1] = data!![i]
            }
            size.decrementAndGet()
            if (NullSupportHelper.full()) return o
            return if (o == null) "" else o
        }
    }

    @Override
    fun getType(): Int {
        reOrganizeType()
        return type
    }

    @Override
    fun getTypeAsString(): String? {
        return QueryImpl.getColumTypeName(getType())
    }

    @Override
    fun toDumpData(pageContext: PageContext?, maxlevel: Int, dp: DumpProperties?): DumpData? {
        return DumpUtil.toDumpData(QueryUtil.getValue(this, query!!.getCurrentrow(pageContext.getId())), pageContext, maxlevel, dp)
    }

    private fun growTo(row: Int) {
        // Require an extra buffer in case another thread is also adding a row to the query.
        // We don't want to single thread the check, but we do want to syncronize if actually growing
        if (data!!.size >= row + CAPACITY) {
            return
        }
        synchronized(sync) {

            // Double check inside the lock in case the column already grew since we last checked
            if (data!!.size >= row + CAPACITY) {
                return
            }
            // Double the current size regardless of how big we were asked to grow
            var newSize = (data!!.size + 1) * 2
            // Keep doubling if neccessary until we're over what was asked
            while (newSize <= row) {
                newSize *= 2
            }
            // Copy data over to new array
            val newData: Array<Object?> = arrayOfNulls<Object?>(newSize)
            for (i in data.indices) {
                newData[i] = data!![i]
            }
            data = newData
        }
    }

    private fun reDefineType(value: Object?): Object? {
        return QueryColumnUtil.reDefineType(this, value)
    }

    private fun resetTypeSync() {
        synchronized(sync) { QueryColumnUtil.resetType(this) }
    }

    private fun resetType() {
        QueryColumnUtil.resetType(this)
    }

    private fun reOrganizeType() {
        synchronized(sync) { QueryColumnUtil.reOrganizeType(this) }
    }

    @Override
    fun getKey(): Collection.Key? {
        return key
    }

    @Override
    override fun setKey(key: Collection.Key?) {
        query!!.disableIndex()
        this.key = key
    }

    @Override
    @Throws(PageException::class)
    fun getKeyAsString(): String? {
        return key.getLowerString() // TODO ist das OK?
    }

    @Override
    operator fun get(pc: PageContext?): Object? {
        return QueryUtil.getValue(this, query!!.getCurrentrow(pc.getId()))
    }

    @Override
    operator fun get(pc: PageContext?, defaultValue: Object?): Object? {
        return get(query!!.getCurrentrow(pc.getId()), defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun touch(pc: PageContext?): Object? {
        return touch(query!!.getCurrentrow(pc.getId()))
    }

    @Override
    fun touchEL(pc: PageContext?): Object? {
        return touchEL(query!!.getCurrentrow(pc.getId()))
    }

    @Override
    @Throws(PageException::class)
    operator fun set(pc: PageContext?, value: Object?): Object? {
        return set(query!!.getCurrentrow(pc.getId()), value)
    }

    @Override
    fun setEL(pc: PageContext?, value: Object?): Object? {
        return setEL(query!!.getCurrentrow(pc.getId()), value)
    }

    @Override
    fun getParent(): Object? {
        return query
    }

    @Override
    @Throws(PageException::class)
    fun castToString(): String? {
        return Caster.toString(get(query!!.getCurrentrow(ThreadLocalPageContext.get().getId()), null))
    }

    @Override
    fun castToString(defaultValue: String?): String? {
        val _null: Object = NullSupportHelper.NULL()
        val value: Object = get(query!!.getCurrentrow(ThreadLocalPageContext.get().getId()), _null)
        return if (value === _null) defaultValue else Caster.toString(value, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun castToBooleanValue(): Boolean {
        return Caster.toBooleanValue(get(query!!.getCurrentrow(ThreadLocalPageContext.get().getId()), null))
    }

    @Override
    fun castToBoolean(defaultValue: Boolean?): Boolean? {
        val _null: Object = NullSupportHelper.NULL()
        val value: Object = get(query!!.getCurrentrow(ThreadLocalPageContext.get().getId()), _null)
        return if (value === _null) defaultValue else Caster.toBoolean(value, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun castToDoubleValue(): Double {
        return Caster.toDoubleValue(get(query!!.getCurrentrow(ThreadLocalPageContext.get().getId()), null))
    }

    @Override
    fun castToDoubleValue(defaultValue: Double): Double {
        val _null: Object = NullSupportHelper.NULL()
        val value: Object = get(query!!.getCurrentrow(ThreadLocalPageContext.get().getId()), _null)
        return if (value === _null) defaultValue else Caster.toDoubleValue(value, true, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun castToDateTime(): DateTime? {
        return DateCaster.toDateAdvanced(get(query!!.getCurrentrow(ThreadLocalPageContext.get().getId()), null), null)
    }

    @Override
    fun castToDateTime(defaultValue: DateTime?): DateTime? {
        val _null: Object = NullSupportHelper.NULL()
        val value: Object = get(query!!.getCurrentrow(ThreadLocalPageContext.get().getId()), _null)
        return if (value === _null) defaultValue else DateCaster.toDateAdvanced(value, DateCaster.CONVERTING_TYPE_OFFSET, null, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(b: Boolean): Int {
        return tachyon.runtime.op.OpUtil.compare(ThreadLocalPageContext.get(), if (castToBooleanValue()) Boolean.TRUE else Boolean.FALSE, if (b) Boolean.TRUE else Boolean.FALSE)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(dt: DateTime?): Int {
        return tachyon.runtime.op.OpUtil.compare(ThreadLocalPageContext.get(), castToDateTime() as Date?, dt as Date?)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(d: Double): Int {
        return tachyon.runtime.op.OpUtil.compare(ThreadLocalPageContext.get(), Double.valueOf(castToDoubleValue()), Double.valueOf(d))
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(str: String?): Int {
        return tachyon.runtime.op.OpUtil.compare(ThreadLocalPageContext.get(), castToString(), str)
    }

    @Override
    fun clone(): Object {
        return cloneColumnImpl(true)
    }

    @Override
    fun duplicate(deepCopy: Boolean): Collection? {
        return cloneColumnImpl(deepCopy)
    }

    @Override
    override fun cloneColumn(deepCopy: Boolean): QueryColumnPro? {
        return cloneColumnImpl(deepCopy)
    }

    fun cloneColumnImpl(deepCopy: Boolean): QueryColumnImpl? {
        val clone = QueryColumnImpl()
        populate(clone, deepCopy)
        return clone
    }

    protected fun populate(trg: QueryColumnImpl?, deepCopy: Boolean) {
        val inside: Boolean = ThreadLocalDuplication.set(this, trg)
        try {
            trg!!.key = key
            trg.query = query
            trg.size = size
            trg.type = type
            trg.key = key
            if (trg.query != null) trg.query.disableIndex()

            // we first get data local, because length of the object cannot be changed, the safes us
            // from
            // modifications from outside
            val data: Array<Object?>? = data
            trg.data = arrayOfNulls<Object?>(data!!.size)
            for (i in data.indices) {
                trg.data!![i] = if (deepCopy) Duplicator.duplicate(data!![i], true) else data!![i]
            }
        } finally {
            if (!inside) ThreadLocalDuplication.reset()
        }
    }

    @Override
    override fun toString(): String {
        return try {
            Caster.toString(get(query!!.getCurrentrow(ThreadLocalPageContext.get().getId()), null))
        } catch (e: PageException) {
            super.toString()
        }
    }

    @Override
    fun containsKey(key: String?): Boolean {
        return containsKey(KeyImpl.init(key))
    }

    @Override
    fun containsKey(key: Collection.Key?): Boolean {
        val _null: Object = NullSupportHelper.NULL()
        return get(key, _null) !== _null
    }

    operator fun iterator(): Iterator? {
        return keyIterator()
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
        return ArrayIterator(data, 0, size())
    }

    @Override
    @Throws(PageException::class)
    fun callWithNamedValues(pc: PageContext?, methodName: Key?, args: Struct?): Object? {
        throw ExpressionException("No matching Method/Function [$methodName] for call with named arguments found")
        // return pc.getFunctionWithNamedValues(get(query.getCurrentrow()), methodName,
        // Caster.toFunctionValues(args));
    }

    @Override
    @Throws(PageException::class)
    fun call(pc: PageContext?, methodName: Key?, arguments: Array<Object?>?): Object? {
        val mi: MethodInstance = Reflector.getMethodInstanceEL(this, this.getClass(), methodName, arguments)
        return if (mi != null) {
            try {
                mi.invoke(this)
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
                try {
                    pc.getFunction(QueryUtil.getValue(this, query!!.getCurrentrow(pc.getId())), methodName, arguments)
                } catch (pe: PageException) {
                    throw Caster.toPageException(t)
                }
            }
        } else pc.getFunction(QueryUtil.getValue(this, query!!.getCurrentrow(pc.getId())), methodName, arguments)
    }

    @Override
    @Throws(PageException::class)
    operator fun set(pc: PageContext?, propertyName: Key?, value: Object?): Object? {
        return set(propertyName, value)
    }

    @Override
    fun setEL(pc: PageContext?, propertyName: Key?, value: Object?): Object? {
        return setEL(propertyName, value)
    }

    fun add(index: Int, element: Object?) {
        throwNotAllowedToAlter()
        // setEL(index+1, element);
    }

    private fun throwNotAllowedToAlter() {
        throw PageRuntimeException(DatabaseException("Query columns do not support methods that would alter the structure of a query column",
                "you must use an analogous method on the query", null, null))
    }

    fun addAll(c: Collection<Object?>?): Boolean {
        throwNotAllowedToAlter()
        return false
        /*
		 * Iterator<? extends Object> it = c.iterator(); while(it.hasNext()){ add(it.next()); } return true;
		 */
    }

    fun addAll(index: Int, c: Collection<Object?>?): Boolean {
        throwNotAllowedToAlter()
        return false
        /*
		 * Iterator<? extends Object> it = c.iterator(); while(it.hasNext()){ setEL(++index,it.next()); }
		 * return true;
		 */
    }

    operator fun contains(o: Object?): Boolean {
        return indexOf(o) != -1
    }

    fun containsAll(c: Collection<*>?): Boolean {
        val it: Iterator<Object?> = c!!.iterator()
        while (it.hasNext()) {
            if (indexOf(it.next()) == -1) return false
        }
        return true
    }

    fun indexOf(o: Object?): Int {
        for (i in 0 until size()) {
            try {
                if (tachyon.runtime.op.OpUtil.compare(ThreadLocalPageContext.get(), o, data!![i]) === 0) return i
            } catch (e: PageException) {
            }
        }
        return -1
    }

    fun lastIndexOf(o: Object?): Int {
        for (i in size() - 1 downTo 0) {
            try {
                if (tachyon.runtime.op.OpUtil.compare(ThreadLocalPageContext.get(), o, data!![i]) === 0) return i
            } catch (e: PageException) {
            }
        }
        return -1
    }

    fun isEmpty(): Boolean {
        return size() == 0
    }

    fun removeAll(c: Collection<*>?): Boolean {
        throwNotAllowedToAlter()
        return false
        /*
		 * boolean hasChanged=false; Iterator<? extends Object> it = c.iterator(); while(it.hasNext()){
		 * if(remove(it.next())) { hasChanged=true; } } return hasChanged;
		 */
    }

    fun retainAll(c: Collection<*>?): Boolean {
        throwNotAllowedToAlter()
        return false
        /*
		 * boolean hasChanged=false; Iterator it = valueIterator(); while(it.hasNext()){
		 * if(!c.contains(it.next())){ hasChanged=true; it.remove(); } } return hasChanged;
		 */
    }

    fun subList(fromIndex: Int, toIndex: Int): List<Object?>? {
        val list: ArrayList<Object?> = ArrayList<Object?>()
        for (i in fromIndex until toIndex) {
            list.add(data!![i])
        }
        return list
    }

    @Override
    fun toArray(): Array<Object?>? {
        return toArray(arrayOfNulls<Object?>(size()))
    }

    fun toArray(trg: Array<Object?>?): Array<Object?>? {
        System.arraycopy(data, 0, trg, 0, if (data!!.size > trg!!.size) trg.size else data!!.size)
        return trg
    }

    @Override
    override fun toDebugColumn(): QueryColumnPro? {
        return _toDebugColumn()
    }

    fun _toDebugColumn(): DebugQueryColumn? {
        return DebugQueryColumn(data, key, query, size, type, typeChecked)
    }

    @Override
    fun getIterator(): Iterator<String?>? {
        return keysAsStringIterator()
    }

    @Override
    override fun equals(obj: Object?): Boolean {
        return if (obj !is Collection) false else CollectionUtil.equals(this, obj as Collection?)
    }

    @Override
    fun getDimension(): Int {
        return 1
    }

    @Override
    @Throws(PageException::class)
    fun getE(row: Int): Object? {
        return get(row)
    }

    @Override
    @Throws(PageException::class)
    fun setE(key: Int, value: Object?): Object? {
        return set(key, value)
    }

    @Override
    fun intKeys(): IntArray? {
        val keys = IntArray(size())
        val len = keys.size
        for (i in 1..len) {
            keys[i - 1] = i
        }
        return keys
    }

    @Override
    @Throws(PageException::class)
    fun insert(key: Int, value: Object?): Boolean {
        throwNotAllowedToAlter()
        return false
    }

    @Override
    @Throws(PageException::class)
    fun append(o: Object?): Object? {
        throwNotAllowedToAlter()
        return o
    }

    @Override
    fun appendEL(o: Object?): Object? {
        throwNotAllowedToAlter()
        return o
    }

    @Override
    @Throws(PageException::class)
    fun prepend(o: Object?): Object? {
        throwNotAllowedToAlter()
        return o
    }

    @Override
    @Throws(PageException::class)
    fun resize(to: Int) {
        throwNotAllowedToAlter()
    }

    @Override
    @Throws(PageException::class)
    fun sort(sortType: String?, sortOrder: String?) {
        throwNotAllowedToAlter()
    }

    @Throws(PageException::class)
    fun sort(rows: IntArray?) {
        query!!.disableIndex()
        val tmp: Array<Object?> = arrayOfNulls<Object?>(data!!.size)
        for (i in 0 until size()) {
            tmp[i] = data!![rows!![i] - 1]
        }
        data = tmp
    }

    @Override
    fun sortIt(comp: Comparator?) {
        throwNotAllowedToAlter()
    }

    @Override
    fun toList(): List? {
        val it: Iterator<Object?>? = valueIterator()
        val list = ArrayList()
        while (it!!.hasNext()) {
            list.add(it.next())
        }
        return list
    }

    @Override
    @Throws(PageException::class)
    fun removeE(key: Int): Object? {
        throwNotAllowedToAlter()
        return null
    }

    @Override
    fun containsKey(key: Int): Boolean {
        val _null: Object = NullSupportHelper.NULL()
        return get(key, _null) !== _null
    } /*
	 * @Override public int hashCode() { return CollectionUtil.hashCode(this); }
	 */

    companion object {
        private const val serialVersionUID = -5544446523204021493L
        private const val CAPACITY = 32
    }
}