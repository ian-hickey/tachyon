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
 * CFML array object
 */
class ArrayClassic : ArraySupport {
    private var arr: Array<Object?>?
    private var dimension = 1
    private val cap = 32
    private var size = 0
    private var offset = 0
    private var offCount = 0

    /**
     * constructor with definiton of the dimension
     *
     * @param dimension dimension goes from one to 3
     * @throws ExpressionException
     */
    constructor(dimension: Int) {
        if (dimension > 3 || dimension < 1) throw ExpressionException("Array Dimension must be between 1 and 3")
        this.dimension = dimension
        arr = arrayOfNulls<Object?>(offset + cap)
    }

    /**
     * constructor with default dimesnion (1)
     */
    constructor() {
        arr = arrayOfNulls<Object?>(offset + cap)
    }

    /**
     * constructor with to data to fill
     *
     * @param objects Objects array data to fill
     */
    constructor(objects: Array<Object?>?) {
        size = objects!!.size
        arr = arrayOfNulls<Object?>(Math.max(size, cap))
        if (size > 0) arr = ArrayUtil.mergeNativeArrays(arr, objects, 0, false)
        offset = 0
    }

    /**
     * return dimension of the array
     *
     * @return dimension of the array
     */
    @Override
    fun getDimension(): Int {
        return dimension
    }

    @Override
    @Throws(ExpressionException::class)
    operator fun get(key: String?): Object? {
        return getE(Caster.toIntValue(key))
    }

    @Override
    @Throws(ExpressionException::class)
    operator fun get(key: Collection.Key?): Object? {
        return getE(Caster.toIntValue(key.getString()))
    }

    @Override
    @Throws(ExpressionException::class)
    operator fun get(pc: PageContext?, key: Collection.Key?): Object? {
        return getE(pc, Caster.toIntValue(key.getString()))
    }

    @Override
    operator fun get(key: String?, defaultValue: Object?): Object? {
        val index: Double = Caster.toIntValue(key, Integer.MIN_VALUE)
        return if (index == Integer.MIN_VALUE) defaultValue else get(index.toInt(), defaultValue)
    }

    @Override
    operator fun get(key: Collection.Key?, defaultValue: Object?): Object? {
        val index: Double = Caster.toIntValue(key.getString(), Integer.MIN_VALUE)
        return if (index == Integer.MIN_VALUE) defaultValue else get(index.toInt(), defaultValue)
    }

    @Override
    operator fun get(pc: PageContext?, key: Collection.Key?, defaultValue: Object?): Object? {
        val index: Double = Caster.toIntValue(key.getString(), Integer.MIN_VALUE)
        return if (index == Integer.MIN_VALUE) defaultValue else get(pc, index.toInt(), defaultValue)
    }

    @Override
    @Synchronized
    operator fun get(key: Int, defaultValue: Object?): Object? {
        return get(null, key, defaultValue)
    }

    @Synchronized
    operator fun get(pc: PageContext?, key: Int, defaultValue: Object?): Object? {
        if (key > size || key < 1) {
            if (dimension > 1) {
                val ai = ArrayClassic()
                ai.dimension = dimension - 1
                return setEL(key, ai)
            }
            return defaultValue
        }
        val o: Object? = arr!![offset + key - 1]
        if (o == null) {
            if (dimension > 1) {
                val ai = ArrayClassic()
                ai.dimension = dimension - 1
                return setEL(key, ai)
            }
            if (!NullSupportHelper.full(pc)) return defaultValue
        }
        return o
    }

    @Override
    @Synchronized
    @Throws(ExpressionException::class)
    fun getE(key: Int): Object? {
        return getE(null, key)
    }

    @Synchronized
    @Throws(ExpressionException::class)
    fun getE(pc: PageContext?, key: Int): Object? {
        if (key < 1) {
            throw invalidPosition(key)
        } else if (key > size) {
            if (dimension > 1) return setE(key, ArrayClassic(dimension - 1))
            throw invalidPosition(key)
        }
        val o: Object? = arr!![offset + key - 1]
        if (NullSupportHelper.full(pc)) {
            return if (o == null && dimension > 1) setE(key, ArrayClassic(dimension - 1)) else o
        }
        if (o == null) {
            if (dimension > 1) return setE(key, ArrayClassic(dimension - 1))
            throw invalidPosition(key)
        }
        return o
    }

    /**
     * Exception method if key doesn't exist at given position
     *
     * @param pos
     * @return exception
     */
    private fun invalidPosition(pos: Int): ExpressionException? {
        return ExpressionException("Element at position [$pos] doesn't exist in array")
    }

    @Override
    fun setEL(key: String?, value: Object?): Object? {
        return try {
            setEL(Caster.toIntValue(key), value)
        } catch (e: ExpressionException) {
            null
        }
    }

    @Override
    fun setEL(key: Collection.Key?, value: Object?): Object? {
        return try {
            setEL(Caster.toIntValue(key.getString()), value)
        } catch (e: ExpressionException) {
            null
        }
    }

    @Override
    @Throws(ExpressionException::class)
    operator fun set(key: String?, value: Object?): Object? {
        return setE(Caster.toIntValue(key), value)
    }

    @Override
    @Throws(ExpressionException::class)
    operator fun set(key: Collection.Key?, value: Object?): Object? {
        return setE(Caster.toIntValue(key.getString()), value)
    }

    @Override
    @Synchronized
    fun setEL(key: Int, value: Object?): Object? {
        if (offset + key > arr!!.size) enlargeCapacity(key)
        if (key > size) size = key
        arr!![offset + key - 1] = checkValueEL(value)
        return value
    }

    /**
     * set value at defined position
     *
     * @param key
     * @param value
     * @return defined value
     * @throws ExpressionException
     */
    @Override
    @Synchronized
    @Throws(ExpressionException::class)
    fun setE(key: Int, value: Object?): Object? {
        if (key < 1) throw ExpressionException("Invalid index [$key] for array. Index must be a positive integer (1, 2, 3, ...)")
        if (offset + key > arr!!.size) enlargeCapacity(key)
        if (key > size) size = key
        arr!![offset + key - 1] = checkValue(value)
        return value
    }

    @Synchronized
    fun ensureCapacity(cap: Int): Int {
        if (cap > arr!!.size) enlargeCapacity(cap)
        return arr!!.size
    }

    /**
     * !!! all methods that use this method must be sync enlarge the inner array to given size
     *
     * @param key min size of the array
     */
    private fun enlargeCapacity(key: Int) {
        val diff = offCount - offset
        val minCapacity: Int = Math.max(arr!!.size, key + offset + diff + 1)
        if (minCapacity > arr!!.size) {
            val oldCapacity = arr!!.size
            var newCapacity = oldCapacity + (oldCapacity shr 1)
            if (newCapacity - minCapacity < 0) newCapacity = minCapacity
            if (newCapacity - MAX_ARRAY_SIZE > 0) newCapacity = hugeCapacity(minCapacity)
            // minCapacity is usually close to size, so this is a win:
            arr = Arrays.copyOf(arr, newCapacity)
        }
    }

    /**
     * !!! all methods that use this method must be sync enlarge the offset if 0
     */
    private fun enlargeOffset() {
        if (offset == 0) {
            offCount = if (offCount == 0) 1 else offCount * 2
            offset = offCount
            val narr: Array<Object?> = arrayOfNulls<Object?>(arr!!.size + offset)
            for (i in 0 until size) {
                narr[offset + i] = arr!![i]
            }
            arr = narr
        }
    }

    /**
     * !!! all methods that use this method must be sync check if value is valid to insert to array (to
     * a multidimesnional array only array with one smaller dimension can be inserted)
     *
     * @param value value to check
     * @return checked value
     * @throws ExpressionException
     */
    @Throws(ExpressionException::class)
    private fun checkValue(value: Object?): Object? {
        // is a 1 > Array
        if (dimension > 1) {
            if (value is Array) {
                if ((value as Array?).getDimension() !== dimension - 1) throw ExpressionException("You can only Append an Array with " + (dimension - 1) + " Dimension",
                        "array has wrong dimension, now is " + (value as Array?).getDimension().toString() + " but it must be " + (dimension - 1))
            } else throw ExpressionException("You can only Append an Array with " + (dimension - 1) + " Dimension", "now is an object of type " + Caster.toClassName(value))
        }
        return value
    }

    /**
     * !!! all methods that use this method must be sync check if value is valid to insert to array (to
     * a multidimesnional array only array with one smaller dimension can be inserted), if value is
     * invalid return null;
     *
     * @param value value to check
     * @return checked value
     */
    private fun checkValueEL(value: Object?): Object? {
        // is a 1 > Array
        if (dimension > 1) {
            if (value is Array) {
                if ((value as Array?).getDimension() !== dimension - 1) return null
            } else return null
        }
        return value
    }

    @Override
    fun size(): Int {
        return size
    }

    @Override
    @Synchronized
    fun keys(): Array<Collection.Key?>? {
        val lst: ArrayList<Collection.Key?> = ArrayList<Collection.Key?>()
        var count = 0
        for (i in offset until offset + size) {
            val o: Object? = arr!![i]
            count++
            if (o != null) lst.add(KeyImpl.getInstance(count.toString() + ""))
        }
        return lst.toArray(arrayOfNulls<Collection.Key?>(lst.size()))
    }

    @Override
    @Synchronized
    fun intKeys(): IntArray? {
        val lst: ArrayList<Integer?> = ArrayList<Integer?>()
        var count = 0
        for (i in offset until offset + size) {
            val o: Object? = arr!![i]
            count++
            if (o != null) lst.add(Integer.valueOf(count))
        }
        val ints = IntArray(lst.size())
        for (i in ints.indices) {
            ints[i] = lst.get(i).intValue()
        }
        return ints
    }

    @Override
    @Throws(ExpressionException::class)
    fun remove(key: Collection.Key?): Object? {
        return removeE(Caster.toIntValue(key.getString()))
    }

    @Override
    fun removeEL(key: Collection.Key?): Object? {
        return removeEL(Caster.toIntValue(key.getString(), -1))
    }

    @Override
    fun remove(key: Collection.Key?, defaultValue: Object?): Object? {
        return try {
            removeE(Caster.toIntValue(key.getString(), -1))
        } catch (e: ExpressionException) {
            defaultValue
        }
    }

    @Override
    @Synchronized
    @Throws(ExpressionException::class)
    fun removeE(key: Int): Object? {
        if (key > size || key < 1) throw invalidPosition(key)
        val obj: Object = get(key, null)
        for (i in offset + key - 1 until offset + size - 1) {
            arr!![i] = arr!![i + 1]
        }
        size--
        return obj
    }

    @Override
    @Synchronized
    @Throws(ExpressionException::class)
    fun pop(): Object? {
        val key = size()
        if (key == 0) throw ExpressionException("cannot pop an element from array, the array is empty")
        val obj: Object = get(key, null)
        for (i in offset + key - 1 until offset + size - 1) {
            arr!![i] = arr!![i + 1]
        }
        size--
        return obj
    }

    @Override
    @Synchronized
    fun pop(defaultValue: Object?): Object? {
        val key = size()
        if (key == 0) return defaultValue
        val obj: Object = get(key, null)
        for (i in offset + key - 1 until offset + size - 1) {
            arr!![i] = arr!![i + 1]
        }
        size--
        return obj
    }

    @Override
    @Synchronized
    @Throws(ExpressionException::class)
    fun shift(): Object? {
        if (size() == 0) throw ExpressionException("cannot pop an element from array, the array is empty")
        val key = 1
        val obj: Object = get(key, null)
        for (i in offset + key - 1 until offset + size - 1) {
            arr!![i] = arr!![i + 1]
        }
        size--
        return obj
    }

    @Override
    @Synchronized
    fun shift(defaultValue: Object?): Object? {
        if (size() == 0) return defaultValue
        val key = 1
        val obj: Object = get(key, null)
        for (i in offset + key - 1 until offset + size - 1) {
            arr!![i] = arr!![i + 1]
        }
        size--
        return obj
    }

    @Override
    @Synchronized
    fun removeEL(key: Int): Object? {
        if (key > size || key < 1) return null
        val obj: Object = get(key, null)
        for (i in offset + key - 1 until offset + size - 1) {
            arr!![i] = arr!![i + 1]
        }
        size--
        return obj
    }

    @Override
    @Synchronized
    fun clear() {
        if (size() > 0) {
            arr = arrayOfNulls<Object?>(cap)
            size = 0
            offCount = 1
            offset = 0
        }
    }

    @Override
    @Synchronized
    @Throws(ExpressionException::class)
    fun insert(key: Int, value: Object?): Boolean {
        if (key < 1 || key > size + 1) {
            throw ExpressionException("can't insert value to array at position " + key + ", array goes from 1 to " + size())
        }
        // Left
        if (size / 2 >= key) {
            enlargeOffset()
            for (i in offset until offset + key - 1) {
                arr!![i - 1] = arr!![i]
            }
            offset--
            arr!![offset + key - 1] = checkValue(value)
            size++
        } else {
            if (offset + key > arr!!.size || size + offset >= arr!!.size) enlargeCapacity(arr!!.size + 2)
            for (i in size + offset downTo key + offset) {
                arr!![i] = arr!![i - 1]
            }
            arr!![offset + key - 1] = checkValue(value)
            size++
        }
        return true
    }

    @Override
    @Synchronized
    @Throws(ExpressionException::class)
    fun append(o: Object?): Object? {
        if (offset + size + 1 > arr!!.size) enlargeCapacity(size + 1)
        arr!![offset + size] = checkValue(o)
        size++
        return o
    }

    /**
     * append a new value to the end of the array
     *
     * @param o value to insert
     * @return inserted value
     */
    @Override
    @Synchronized
    fun appendEL(o: Object?): Object? {
        if (offset + size + 1 > arr!!.size) enlargeCapacity(size + 1)
        arr!![offset + size] = o
        size++
        return o
    }

    /**
     * append a new value to the end of the array
     *
     * @param str value to insert
     * @return inserted value
     */
    @Synchronized
    fun _append(str: String?): String? {
        if (offset + size + 1 > arr!!.size) enlargeCapacity(size + 1)
        arr!![offset + size] = str
        size++
        return str
    }

    /**
     * add a new value to the begin of the array
     *
     * @param o value to insert
     * @return inserted value
     * @throws ExpressionException
     */
    @Override
    @Throws(ExpressionException::class)
    fun prepend(o: Object?): Object? {
        insert(1, o)
        return o
    }

    /**
     * resize array to defined size
     *
     * @param to new minimum size of the array
     */
    @Override
    @Synchronized
    fun resize(to: Int) {
        if (to > size) {
            enlargeCapacity(to)
            size = to
        }
    }

    @Override
    @Synchronized
    fun sortIt(comp: Comparator?) {
        if (getDimension() > 1) throw PageRuntimeException("only 1 dimensional arrays can be sorted")
        Arrays.sort(arr, offset, offset + size, comp)
    }

    /**
     * @return return arra as native (Java) Object Array
     */
    @Override
    @Synchronized
    fun toArray(): Array<Object?>? {
        val rtn: Array<Object?> = arrayOfNulls<Object?>(size)
        var count = 0
        for (i in offset until offset + size) {
            rtn[count++] = arr!![i]
        }
        return rtn
    }

    /**
     * @return return array as ArrayList
     */
    /*
	 * public synchronized ArrayList toArrayList() { ArrayList al=new ArrayList(); for(int
	 * i=offset;i<offset+size;i++) { al.add(arr[i]); } return al; }
	 */
    @Override
    fun toDumpData(pageContext: PageContext?, maxlevel: Int, dp: DumpProperties?): DumpData? {
        val table = DumpTable("array", "#99cc33", "#ccff33", "#000000")
        table.setTitle("Array")
        val top: Int = dp.getMaxlevel()
        if (size() > top) table.setComment("Rows: " + size() + " (showing top " + top + ")") else if (size() > 10 && dp.getMetainfo()) table.setComment("Rows: " + size())
        val length = size()
        for (i in 1..length) {
            var o: Object? = null
            try {
                o = getE(i)
            } catch (e: Exception) {
            }
            table.appendRow(1, SimpleDumpData(i), DumpUtil.toDumpData(o, pageContext, maxlevel, dp))
            if (i == top) break
        }
        return table
    }

    /**
     * return code print of the array as plain text
     *
     * @return content as string
     */
    @Synchronized
    fun toPlain(): String? {
        val sb = StringBuffer()
        val length = size()
        for (i in 1..length) {
            sb.append(i)
            sb.append(": ")
            sb.append(get(i - 1, null))
            sb.append("\n")
        }
        return sb.toString()
    }

    @Override
    @Synchronized
    fun duplicate(deepCopy: Boolean): Collection? {
        return duplicate(ArrayClassic(), deepCopy)
    }

    protected fun duplicate(arr: ArrayClassic?, deepCopy: Boolean): Collection? {
        arr!!.dimension = dimension
        val it: Iterator<Entry<Key?, Object?>?>? = entryIterator()
        val inside = if (deepCopy) ThreadLocalDuplication.set(this, arr) else true
        var e: Entry<Key?, Object?>?
        try {
            while (it!!.hasNext()) {
                e = it.next()
                if (deepCopy) arr[e.getKey()] = Duplicator.duplicate(e.getValue(), deepCopy) else arr[e.getKey()] = e.getValue()
            }
        } catch (ee: ExpressionException) {
        } finally {
            if (!inside) ThreadLocalDuplication.reset()
        }
        return arr
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
    operator fun iterator(): Iterator? {
        return ListIteratorImpl(this, 0)
    }

    companion object {
        private const val serialVersionUID = -6187994169003839005L
        private val MAX_ARRAY_SIZE: Int = Integer.MAX_VALUE - 8
        private fun hugeCapacity(minCapacity: Int): Int {
            if (minCapacity < 0) throw OutOfMemoryError()
            return if (minCapacity > MAX_ARRAY_SIZE) Integer.MAX_VALUE else MAX_ARRAY_SIZE
        }
    }
}