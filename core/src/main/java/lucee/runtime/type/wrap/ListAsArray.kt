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
package lucee.runtime.type.wrap

import java.util.ArrayList

/**
 *
 */
class ListAsArray protected constructor(list: List?) : ArraySupport(), Array, List {
    var list: List?
    @Override
    @Throws(PageException::class)
    fun append(o: Object?): Object? {
        list.add(o)
        return o
    }

    @Override
    fun appendEL(o: Object?): Object? {
        list.add(o)
        return o
    }

    /*---@Override
	public boolean containsKey(int index) {
		super.containsKey(index);
		return get(index-1,null)!=null;
	}*/
    @Override
    operator fun get(key: Int, defaultValue: Object?): Object? {
        return get(null, key, defaultValue)
    }

    operator fun get(pc: PageContext?, key: Int, defaultValue: Object?): Object? {
        if (key <= 0) return defaultValue
        return if (key > list.size()) defaultValue else try {
            val rtn: Object = list.get(key - 1)
                    ?: return if (NullSupportHelper.full(pc)) {
                        null
                    } else defaultValue
            rtn
        } catch (e: Exception) {
            defaultValue
        }
    }

    @Override
    @Throws(PageException::class)
    fun getE(key: Int): Object? {
        return getE(null, key)
    }

    @Throws(PageException::class)
    fun getE(pc: PageContext?, key: Int): Object? {
        if (key <= 0) {
            val idx: Integer = if (list.size() + key < 0) -1 else list.size() + key
            if (idx < 0 || key == 0) {
                throw ExpressionException("Array index [" + key + "] out of range, array size is [" + list.size() + "]")
            }
            return list.get(idx)
        }
        if (key > list.size()) throw ExpressionException("Array index [" + key + "] out of range, array size is [" + list.size() + "]")
        val rtn: Object = list.get(key - 1)
        if (rtn == null) {
            if (NullSupportHelper.full(pc)) {
                return null
            }
            throw ExpressionException("Element at position [$key] does not exist in list")
        }
        return rtn
    }

    @get:Override
    val dimension: Int
        get() = 1

    @Override
    @Throws(PageException::class)
    fun insert(key: Int, value: Object?): Boolean {
        try {
            list.add(key - 1, value)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            throw ExpressionException("can't insert value to array at position " + key + ", array goes from 1 to " + size())
        }
        return true
    }

    @Override
    fun intKeys(): IntArray? {
        val lit: ListIterator = list.listIterator()
        val keys = ArrayList()
        var index = 0
        var v: Object
        while (lit.hasNext()) {
            index = lit.nextIndex() + 1
            v = lit.next()
            if (v != null) keys.add(Integer.valueOf(index))
        }
        val intKeys = IntArray(keys.size())
        val it: Iterator = keys.iterator()
        index = 0
        while (it.hasNext()) {
            intKeys[index++] = (it.next() as Integer).intValue()
        }
        return intKeys
    }

    @Override
    @Throws(PageException::class)
    fun prepend(o: Object?): Object? {
        list.add(0, o)
        return o
    }

    @Override
    @Throws(PageException::class)
    fun removeE(key: Int): Object? {
        return try {
            list.remove(key - 1)
        } catch (e: Exception) {
            val ee = ExpressionException("can not remove Element at position [$key]", e.getMessage())
            ee.setStackTrace(e.getStackTrace())
            throw ee
        }
    }

    @Override
    fun removeEL(key: Int): Object? {
        return try {
            removeE(key)
        } catch (e: PageException) {
            null
        }
    }

    fun remove(key: Int, defaultValue: Object?): Object? {
        return try {
            removeE(key)
        } catch (e: PageException) {
            defaultValue
        }
    }

    @Override
    @Throws(PageException::class)
    fun pop(): Object? {
        if (size() == 0) throw ExpressionException("can not pop Element from array, array is empty")
        return try {
            list.remove(size() - 1)
        } catch (e: Exception) {
            val ee = ExpressionException("can not pop Element from array", e.getMessage())
            ee.setStackTrace(e.getStackTrace())
            throw ee
        }
    }

    @Override
    fun pop(defaultValue: Object?): Object? {
        return if (size() == 0) defaultValue else try {
            list.remove(size() - 1)
        } catch (e: Exception) {
            defaultValue
        }
    }

    @Override
    @Throws(PageException::class)
    fun shift(): Object? {
        if (size() == 0) throw ExpressionException("can not pop Element from array, array is empty")
        return try {
            list.remove(0)
        } catch (e: Exception) {
            val ee = ExpressionException("can not pop Element from array", e.getMessage())
            ee.setStackTrace(e.getStackTrace())
            throw ee
        }
    }

    @Override
    fun shift(defaultValue: Object?): Object? {
        return if (size() == 0) defaultValue else try {
            list.remove(0)
        } catch (e: Exception) {
            defaultValue
        }
    }

    @Override
    @Throws(PageException::class)
    fun resize(to: Int) {
        while (size() < to) list.add(null)
    }

    @Override
    @Throws(PageException::class)
    fun setE(key: Int, value: Object?): Object? {
        if (key <= size()) {
            try {
                list.set(key - 1, value)
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
                throw ExpressionException("can not set Element at position [$key]", t.getMessage())
            }
        } else {
            while (size() < key - 1) list.add(null)
            list.add(value)
        }
        return value
    }

    @Override
    fun setEL(key: Int, value: Object?): Object? {
        if (key <= size()) {
            try {
                list.set(key - 1, value)
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
                return value
            }
        } else {
            while (size() < key - 1) list.add(null)
            list.add(value)
        }
        return value
    }

    /*---@Override
	public void sort(String sortType, String sortOrder) throws PageException {
		sortIt(ArrayUtil.toComparator(null, sortType, sortOrder, false));
	}*/
    @Override
    fun sortIt(comp: Comparator?) {
        if (dimension > 1) throw PageRuntimeException("only 1 dimensional arrays can be sorted")
        Collections.sort(list, comp)
    }

    @Override
    fun toArray(): Array<Object?>? {
        return list.toArray()
    }

    fun toArrayList(): ArrayList? {
        return ArrayList(list)
    }

    @Override
    fun clear() {
        list.clear()
    }

    /*---@Override
	public boolean containsKey(String key) {
		return get(key,null)!=null;
	}
	
	@Override
	public boolean containsKey(Key key) {
		super.containsKey(key)
		return get(key,null)!=null;
	}*/
    @Override
    fun duplicate(deepCopy: Boolean): Collection? {
        ArrayImpl().duplicate(deepCopy)
        return ListAsArray(Duplicator.duplicate(list, deepCopy) as List)
    }

    @Override
    @Throws(PageException::class)
    operator fun get(key: String?): Object? {
        return getE(Caster.toIntValue(key))
    }

    @Throws(PageException::class)
    operator fun get(pc: PageContext?, key: String?): Object? {
        return getE(pc, Caster.toIntValue(key))
    }

    @Override
    @Throws(PageException::class)
    operator fun get(key: Key?): Object? {
        return get(key.getString())
    }

    @Override
    @Throws(PageException::class)
    operator fun get(pc: PageContext?, key: Key?): Object? {
        return get(pc, key.getString())
    }

    @Override
    operator fun get(key: String?, defaultValue: Object?): Object? {
        return get(null, key, defaultValue)
    }

    operator fun get(pc: PageContext?, key: String?, defaultValue: Object?): Object? {
        val index: Double = Caster.toIntValue(key, Integer.MIN_VALUE)
        return if (index == Integer.MIN_VALUE) defaultValue else get(index.toInt(), defaultValue)
    }

    @Override
    operator fun get(key: Key?, defaultValue: Object?): Object? {
        return get(key.getString(), defaultValue)
    }

    @Override
    operator fun get(pc: PageContext?, key: Key?, defaultValue: Object?): Object? {
        return get(pc, key.getString(), defaultValue)
    }

    @Override
    fun keys(): Array<Key?>? {
        val intKeys = intKeys()
        val keys: Array<Collection.Key?> = arrayOfNulls<Collection.Key?>(intKeys!!.size)
        for (i in intKeys.indices) {
            keys[i] = KeyImpl.init(Caster.toString(intKeys!![i]))
        }
        return keys
    }

    @Override
    @Throws(PageException::class)
    fun remove(key: Key?): Object? {
        return removeE(Caster.toIntValue(key.getString()))
    }

    @Override
    fun removeEL(key: Key?): Object? {
        val index: Double = Caster.toIntValue(key.getString(), Integer.MIN_VALUE)
        return if (index == Integer.MIN_VALUE) null else removeEL(index.toInt())
    }

    @Override
    fun remove(key: Key?, defaultValue: Object?): Object? {
        val index: Double = Caster.toIntValue(key.getString(), Integer.MIN_VALUE)
        return if (index == Integer.MIN_VALUE) defaultValue else remove(index.toInt(), defaultValue)
    }

    @Override
    @Throws(PageException::class)
    operator fun set(key: String?, value: Object?): Object? {
        return setE(Caster.toIntValue(key), value)
    }

    @Override
    @Throws(PageException::class)
    operator fun set(key: Key?, value: Object?): Object? {
        return set(key.getString(), value)
    }

    @Override
    fun setEL(key: String?, value: Object?): Object? {
        val index: Double = Caster.toIntValue(key, Integer.MIN_VALUE)
        return if (index == Integer.MIN_VALUE) value else setEL(index.toInt(), value)
    }

    @Override
    fun setEL(key: Key?, value: Object?): Object? {
        return setEL(key.getString(), value)
    }

    @Override
    fun size(): Int {
        return list.size()
    }

    @Override
    fun toDumpData(pageContext: PageContext?, maxlevel: Int, dp: DumpProperties?): DumpData? {
        return DumpUtil.toDumpData(list, pageContext, maxlevel, dp)
    }

    @Override
    operator fun iterator(): Iterator? {
        return list.iterator()
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

    /*---@Override
	public String castToString() throws PageException {
		throw new ExpressionException("Can't cast Complex Object Type "+Caster.toClassName(list)+" to String",
	  "Use Built-In-Function \"serialize(Array):String\" to create a String from Array");
	}
	
	@Override
	public String castToString(String defaultValue) {
		return defaultValue;
	}
	
	
	@Override
	public boolean castToBooleanValue() throws PageException {
	throw new ExpressionException("Can't cast Complex Object Type "+Caster.toClassName(list)+" to a boolean value");
	}
	
	@Override
	public Boolean castToBoolean(Boolean defaultValue) {
	return defaultValue;
	}
	
	
	@Override
	public double castToDoubleValue() throws PageException {
	throw new ExpressionException("Can't cast Complex Object Type "+Caster.toClassName(list)+" to a number value");
	}
	
	@Override
	public double castToDoubleValue(double defaultValue) {
	return defaultValue;
	}
	
	
	@Override
	public DateTime castToDateTime() throws PageException {
	throw new ExpressionException("Can't cast Complex Object Type "+Caster.toClassName(list)+" to a Date");
	}
	
	@Override
	public DateTime castToDateTime(DateTime defaultValue) {
	return defaultValue;
	}
	
	@Override
	public int compareTo(boolean b) throws PageException {
		throw new ExpressionException("can't compare Complex Object Type "+Caster.toClassName(list)+" with a boolean value");
	}
	
	@Override
	public int compareTo(DateTime dt) throws PageException {
		throw new ExpressionException("can't compare Complex Object Type "+Caster.toClassName(list)+" with a DateTime Object");
	}
	
	@Override
	public int compareTo(double d) throws PageException {
		throw new ExpressionException("can't compare Complex Object Type "+Caster.toClassName(list)+" with a numeric value");
	}
	
	@Override
	public int compareTo(String str) throws PageException {
		throw new ExpressionException("can't compare Complex Object Type "+Caster.toClassName(list)+" with a String");
	}*/
    /*---@Override
	public String toString() {
		return LazyConverter.serialize(this);
	}*/
    /*---@Override
	public Object clone() {
		super.clone()
		return duplicate(true);
	}*/
    @Override
    fun add(o: Object?): Boolean {
        return list.add(o)
    }

    /*---@Override
	public void add(int index, Object element) {
		list.add(index, element);
	}
	
	@Override
	public boolean addAll(java.util.Collection c) {
		return list.addAll(c);
	}*/
    @Override
    fun addAll(index: Int, c: Collection<*>?): Boolean {
        return list.addAll(index, c)
    }

    @Override
    operator fun contains(o: Object?): Boolean {
        return list.contains(o)
    }

    @Override
    fun containsAll(c: Collection<*>?): Boolean {
        return list.containsAll(c)
    }

    /*---@Override
	public Object get(int index) {
		return list.get(index);
	}*/
    @Override
    fun indexOf(o: Object?): Int {
        return list.indexOf(o)
    }

    @get:Override
    val isEmpty: Boolean
        get() = list.isEmpty()

    @Override
    fun lastIndexOf(o: Object?): Int {
        return list.lastIndexOf(o)
    }

    @Override
    fun listIterator(): ListIterator? {
        return list.listIterator()
    }

    @Override
    fun listIterator(index: Int): ListIterator? {
        return list.listIterator(index)
    }

    /*
	 * @Override public boolean remove(Object o) { return list.remove(o); }
	 * 
	 * @Override public Object remove(int index) { return list.remove(index); }
	 * 
	 * @Override public boolean removeAll(java.util.Collection c) { return list.removeAll(c); }
	 * 
	 * @Override public boolean retainAll(java.util.Collection c) { return list.retainAll(c); }
	 * 
	 * @Override public Object set(int index, Object element) { return list.set(index, element); }
	 */
    @Override
    fun subList(fromIndex: Int, toIndex: Int): List? {
        return list.subList(fromIndex, toIndex)
    }

    /*---@Override not sure to remove it
	public Object[] toArray(Object[] a) {
		super.toArray(a)
		return list.toArray(a);
	}*/
    @Override
    fun toList(): List? {
        return this
    }

    @Override
    fun valueIterator(): Iterator<Object?>? {
        return ListIteratorImpl(list, 0)
        // return list.iterator();
    }
    // return list.iterator(); /*---@Override
    public Object get(PageContext pc, Key key, Object defaultValue)
    {
        return get(key, defaultValue)
    }

    @Override
    public Object get(PageContext pc, Key key) throws PageException
    {
        return get(key)
    }

    @Override
    public Object set(PageContext pc, Key propertyName, Object value ) throws PageException
    {
        return set(propertyName, value)
    }

    @Override
    public Object setEL(PageContext pc, Key propertyName, Object value )
    {
        return setEL(propertyName, value)
    }*/
    @get:Override
    val iterator: Iterator<Any?>?
        get() = ListIteratorImpl(list, 0)

    // return list.iterator();
    /*
	 * @Override public Object call(PageContext pc, Key methodName, Object[] args) throws PageException
	 * { return MemberUtil.call(pc, this, methodName, args, CFTypes.TYPE_ARRAY, "array"); }
	 * 
	 * @Override public Object callWithNamedValues(PageContext pc, Key methodName, Struct args) throws
	 * PageException { return MemberUtil.callWithNamedValues(pc,this,methodName,args,
	 * CFTypes.TYPE_ARRAY, "array"); }
	 */
    /*---@Override
	public java.util.Iterator<Object> getIterator() {
		return valueIterator();
	}*/
    companion object {
        fun toArray(list: List?): Array? {
            if (list is ArrayAsList) return (list as ArrayAsList?)!!.array
            return if (list is Array) list else ListAsArray(list)
        }
    }

    init {
        this.list = list
    }
}