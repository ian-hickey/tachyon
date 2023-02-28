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
package tachyon.runtime.type.util

import java.sql.Types

/**
 * Util for diffrent methods to manipulate arrays
 */
object ArrayUtil {
    val OBJECT_EMPTY: Array<Object?>? = arrayOf<Object?>()
    @Throws(ExpressionException::class)
    fun getInstance(dimension: Int): Array? {
        return getInstance(dimension, false)
    }

    @Throws(ExpressionException::class)
    fun getInstance(dimension: Int, _synchronized: Boolean): Array? {
        return if (dimension > 1) ArrayClassic(dimension) else ArrayImpl(ArrayImpl.DEFAULT_CAP, _synchronized)
    }

    /**
     * trims all value of a String Array
     *
     * @param arr
     * @return trimmed array
     */
    fun trimItems(arr: Array<String?>?): Array<String?>? {
        for (i in arr.indices) {
            arr!![i] = arr[i].trim()
        }
        return arr
    }

    /**
     * @param list
     * @return array
     */
    fun toSortRegisterArray(list: ArrayList?): Array<SortRegister?>? {
        val arr: Array<SortRegister?> = arrayOfNulls<SortRegister?>(list.size())
        for (i in arr.indices) {
            arr[i] = SortRegister(i, list.get(i))
        }
        return arr
    }

    /**
     * @param column
     * @return array
     */
    fun toSortRegisterArray(column: QueryColumn?): Array<SortRegister?>? {
        val arr: Array<SortRegister?> = arrayOfNulls<SortRegister?>(column.size())
        val type: Int = column.getType()
        for (i in arr.indices) {
            arr[i] = SortRegister(i, toSortRegisterArray(column.get(i + 1, null), type))
        }
        return arr
    }

    private fun toSortRegisterArray(value: Object?, type: Int): Object? {
        var mod: Object? = null
        // Date
        mod = if (Types.TIMESTAMP === type) {
            Caster.toDate(value, true, null, null)
        } else if (Types.DOUBLE === type) {
            Caster.toDouble(value, null)
        } else if (Types.BOOLEAN === type) {
            Caster.toBoolean(value, null)
        } else if (Types.VARCHAR === type) {
            Caster.toString(value, null)
        } else return value
        return if (mod != null) mod else value
    }

    /**
     * swap to values of the array
     *
     * @param array
     * @param left left value to swap
     * @param right right value to swap
     * @throws ExpressionException
     */
    @Throws(ExpressionException::class)
    fun swap(array: Array?, left: Int, right: Int) {
        val len: Int = array.size()
        if (len == 0) throw ExpressionException("array is empty")
        if (left < 1 || left > len) throw ExpressionException("invalid index [$left]", "valid indexes are from 1 to $len")
        if (right < 1 || right > len) throw ExpressionException("invalid index [$right]", "valid indexes are from 1 to $len")
        try {
            val leftValue: Object = array.get(left, null)
            val rightValue: Object = array.get(right, null)
            array.setE(left, rightValue)
            array.setE(right, leftValue)
        } catch (e: PageException) {
            throw ExpressionException("can't swap values of array", e.getMessage())
        }
    }

    /**
     * find an object in array
     *
     * @param array
     * @param object object to find
     * @return position in array or 0
     */
    fun find(array: Array?, `object`: Object?): Int {
        val len: Int = array.size()
        for (i in 1..len) {
            val tmp: Object = array.get(i, null)
            try {
                if (tmp != null && tachyon.runtime.op.OpUtil.compare(ThreadLocalPageContext.get(), `object`, tmp) === 0) return i
            } catch (e: PageException) {
            }
        }
        return 0
    }

    /**
     * average of all values of the array, only work when all values are numeric
     *
     * @param array
     * @return average of all values
     * @throws ExpressionException
     */
    @Throws(ExpressionException::class)
    fun avg(array: Array?): Double {
        return if (array.size() === 0) 0 else sum(array) / array.size()
    }

    /**
     * sum of all values of an array, only work when all values are numeric
     *
     * @param array Array
     * @return sum of all values
     * @throws ExpressionException
     */
    @Throws(ExpressionException::class)
    fun sum(array: Array?): Double {
        if (array.getDimension() > 1) throw ExpressionException("can only get sum/avg from 1 dimensional arrays")
        var rtn = 0.0
        val len: Int = array.size()
        // try {
        for (i in 1..len) {
            rtn += _toDoubleValue(array, i)
        }
        /*
		 * } catch (PageException e) { throw new
		 * ExpressionException("exception while execute array operation: "+e.getMessage()); }
		 */return rtn
    }

    /**
     * median value of all items in the arrays, only works when all values are numeric
     *
     * @param array
     * @return
     * @throws ExpressionException
     */
    @Throws(ExpressionException::class)
    fun median(array: Array?): Double {
        val len: Int = array.size()
        if (len == 0) return 0
        if (array.getDimension() > 1) throw ExpressionException("Median() can only be calculated for one dimensional arrays")
        val arr = DoubleArray(len)
        for (i in 0 until len) arr[i] = _toDoubleValue(array, i + 1)
        Arrays.sort(arr)
        val result = arr[len / 2]
        return if (len % 2 == 0) {
            (result + arr[(len - 2) / 2]) / 2
        } else result
    }

    @Throws(ExpressionException::class)
    private fun _toDoubleValue(array: Array?, i: Int): Double {
        val obj: Object = array.get(i, null)
                ?: throw ExpressionException("there is no element at position [$i] or the element is null")
        val tmp: Double = Caster.toDoubleValue(obj, true, Double.NaN)
        if (Double.isNaN(tmp)) throw CasterException(obj, Double::class.java)
        return tmp
    }

    /**
     * the smallest value, of all values inside the array, only work when all values are numeric
     *
     * @param array
     * @return the smallest value
     * @throws PageException
     */
    @Throws(PageException::class)
    fun min(array: Array?): Double {
        if (array.getDimension() > 1) throw ExpressionException("can only get max value from 1 dimensional arrays")
        if (array.size() === 0) return 0
        var rtn = _toDoubleValue(array, 1)
        val len: Int = array.size()
        try {
            for (i in 2..len) {
                val v = _toDoubleValue(array, i)
                if (rtn > v) rtn = v
            }
        } catch (e: PageException) {
            throw ExpressionException("exception while execute array operation: " + e.getMessage())
        }
        return rtn
    }

    /**
     * the greatest value, of all values inside the array, only work when all values are numeric
     *
     * @param array
     * @return the greatest value
     * @throws PageException
     */
    @Throws(PageException::class)
    fun max(array: Array?): Double {
        if (array.getDimension() > 1) throw ExpressionException("can only get max value from 1 dimensional arrays")
        if (array.size() === 0) return 0
        var rtn = _toDoubleValue(array, 1)
        val len: Int = array.size()
        try {
            for (i in 2..len) {
                val v = _toDoubleValue(array, i)
                if (rtn < v) rtn = v
            }
        } catch (e: PageException) {
            throw ExpressionException("exception while execute array operation: " + e.getMessage())
        }
        return rtn
    }

    /**
     * return index of given value in Array or -1
     *
     * @param arr
     * @param value
     * @return index of position in array
     */
    fun indexOf(arr: Array<String?>?, value: String?): Int {
        for (i in arr.indices) {
            if (arr!![i]!!.equals(value)) return i
        }
        return -1
    }

    /**
     * return index of given value in Array or -1
     *
     * @param arr
     * @param value
     * @return index of position in array
     */
    fun indexOfIgnoreCase(arr: Array<String?>?, value: String?): Int {
        for (i in arr.indices) {
            if (arr!![i].equalsIgnoreCase(value)) return i
        }
        return -1
    }

    /**
     * convert a primitive array (value type) to Object Array (reference type).
     *
     * @param primArr value type Array
     * @return reference type Array
     */
    fun toReferenceType(primArr: BooleanArray?): Array<Boolean?>? {
        val refArr = arrayOfNulls<Boolean?>(primArr!!.size)
        for (i in primArr.indices) refArr[i] = Caster.toBoolean(primArr[i])
        return refArr
    }

    /**
     * convert a primitive array (value type) to Object Array (reference type).
     *
     * @param primArr value type Array
     * @return reference type Array
     */
    fun toReferenceType(primArr: ByteArray?): Array<Byte?>? {
        val refArr = arrayOfNulls<Byte?>(primArr!!.size)
        for (i in primArr.indices) refArr[i] = Byte.valueOf(primArr[i])
        return refArr
    }

    /**
     * convert a primitive array (value type) to Object Array (reference type).
     *
     * @param primArr value type Array
     * @return reference type Array
     */
    fun toReferenceType(primArr: CharArray?): Array<Character?>? {
        val refArr: Array<Character?> = arrayOfNulls<Character?>(primArr!!.size)
        for (i in primArr.indices) refArr[i] = Character.valueOf(primArr!![i])
        return refArr
    }

    /**
     * convert a primitive array (value type) to Object Array (reference type).
     *
     * @param primArr value type Array
     * @return reference type Array
     */
    fun toReferenceType(primArr: ShortArray?): Array<Short?>? {
        val refArr = arrayOfNulls<Short?>(primArr!!.size)
        for (i in primArr.indices) refArr[i] = Short.valueOf(primArr[i])
        return refArr
    }

    /**
     * convert a primitive array (value type) to Object Array (reference type).
     *
     * @param primArr value type Array
     * @return reference type Array
     */
    fun toReferenceType(primArr: IntArray?): Array<Integer?>? {
        val refArr: Array<Integer?> = arrayOfNulls<Integer?>(primArr!!.size)
        for (i in primArr.indices) refArr[i] = Integer.valueOf(primArr!![i])
        return refArr
    }

    /**
     * convert a primitive array (value type) to Object Array (reference type).
     *
     * @param primArr value type Array
     * @return reference type Array
     */
    fun toReferenceType(primArr: LongArray?): Array<Long?>? {
        val refArr = arrayOfNulls<Long?>(primArr!!.size)
        for (i in primArr.indices) refArr[i] = Long.valueOf(primArr[i])
        return refArr
    }

    /**
     * convert a primitive array (value type) to Object Array (reference type).
     *
     * @param primArr value type Array
     * @return reference type Array
     */
    fun toReferenceType(primArr: FloatArray?): Array<Float?>? {
        val refArr = arrayOfNulls<Float?>(primArr!!.size)
        for (i in primArr.indices) refArr[i] = Float.valueOf(primArr[i])
        return refArr
    }

    /**
     * convert a primitive array (value type) to Object Array (reference type).
     *
     * @param primArr value type Array
     * @return reference type Array
     */
    fun toReferenceType(primArr: DoubleArray?): Array<Double?>? {
        val refArr = arrayOfNulls<Double?>(primArr!!.size)
        for (i in primArr.indices) refArr[i] = Double.valueOf(primArr[i])
        return refArr
    }

    /**
     * gets a value of an array at defined index
     *
     * @param o
     * @param index
     * @return value at index position
     * @throws ArrayUtilException
     */
    @Throws(ArrayUtilException::class)
    operator fun get(o: Object?, index: Int): Object? {
        var o: Object? = o
        o = ArrayUtil[o, index, null]
        if (o != null) return o
        throw ArrayUtilException("Object is not an array, or index is invalid")
    }

    /**
     * gets a value of an array at defined index
     *
     * @param o
     * @param index
     * @return value of the variable
     */
    operator fun get(o: Object?, index: Int, defaultValue: Object?): Object? {
        if (index < 0) return null
        if (o is Array<Object>) {
            val arr: Array<Object?>? = o
            if (arr!!.size > index) return arr[index]
        } else if (o is BooleanArray) {
            val arr = o as BooleanArray?
            if (arr!!.size > index) return if (arr[index]) Boolean.TRUE else Boolean.FALSE
        } else if (o is ByteArray) {
            val arr = o as ByteArray?
            if (arr!!.size > index) return Byte.valueOf(arr[index])
        } else if (o is CharArray) {
            val arr = o as CharArray?
            if (arr!!.size > index) return "" + arr[index]
        } else if (o is ShortArray) {
            val arr = o as ShortArray?
            if (arr!!.size > index) return Short.valueOf(arr[index])
        } else if (o is IntArray) {
            val arr = o as IntArray?
            if (arr!!.size > index) return Integer.valueOf(arr[index])
        } else if (o is LongArray) {
            val arr = o as LongArray?
            if (arr!!.size > index) return Long.valueOf(arr[index])
        } else if (o is FloatArray) {
            val arr = o as FloatArray?
            if (arr!!.size > index) return Float.valueOf(arr[index])
        } else if (o is DoubleArray) {
            val arr = o as DoubleArray?
            if (arr!!.size > index) return Double.valueOf(arr[index])
        }
        return defaultValue
    }

    /**
     * sets a value to an array at defined index
     *
     * @param o
     * @param index
     * @param value
     * @return value setted
     * @throws ArrayUtilException
     */
    @Throws(ArrayUtilException::class)
    operator fun set(o: Object?, index: Int, value: Object?): Object? {
        if (index < 0) throw invalidIndex(index, 0)
        if (o is Array<Object>) {
            val arr: Array<Object?>? = o
            if (arr!!.size > index) return value.also { arr[index] = it }
            throw invalidIndex(index, arr.size)
        } else if (o is BooleanArray) {
            val arr = o as BooleanArray?
            if (arr!!.size > index) {
                arr[index] = Caster.toBooleanValue(value, false)
                return if (arr[index]) Boolean.TRUE else Boolean.FALSE
            }
            throw invalidIndex(index, arr.size)
        } else if (o is ByteArray) {
            val arr = o as ByteArray?
            if (arr!!.size > index) {
                val v: Double = Caster.toDoubleValue(value, true, Double.NaN)
                if (Decision.isValid(v)) {
                    return Byte.valueOf(v as Byte.also) { arr[index] = it }
                }
            }
            throw invalidIndex(index, arr.size)
        } else if (o is ShortArray) {
            val arr = o as ShortArray?
            if (arr!!.size > index) {
                val v: Double = Caster.toDoubleValue(value, true, Double.NaN)
                if (Decision.isValid(v)) {
                    return Short.valueOf(v as Short.also) { arr[index] = it }
                }
            }
            throw invalidIndex(index, arr.size)
        } else if (o is IntArray) {
            val arr = o as IntArray?
            if (arr!!.size > index) {
                val v: Double = Caster.toDoubleValue(value, true, Double.NaN)
                if (Decision.isValid(v)) {
                    return Integer.valueOf(v as Int.also) { arr[index] = it }
                }
            }
            throw invalidIndex(index, arr.size)
        } else if (o is LongArray) {
            val arr = o as LongArray?
            if (arr!!.size > index) {
                val v: Double = Caster.toDoubleValue(value, true, Double.NaN)
                if (Decision.isValid(v)) {
                    return Long.valueOf(v as Long.also) { arr[index] = it }
                }
            }
            throw invalidIndex(index, arr.size)
        } else if (o is FloatArray) {
            val arr = o as FloatArray?
            if (arr!!.size > index) {
                val v: Double = Caster.toDoubleValue(value, true, Double.NaN)
                if (Decision.isValid(v)) {
                    return Float.valueOf(v as Float.also) { arr[index] = it }
                }
            }
            throw invalidIndex(index, arr.size)
        } else if (o is DoubleArray) {
            val arr = o as DoubleArray?
            if (arr!!.size > index) {
                val v: Double = Caster.toDoubleValue(value, true, Double.NaN)
                if (Decision.isValid(v)) {
                    return Double.valueOf(v.also { arr[index] = it })
                }
            }
            throw invalidIndex(index, arr.size)
        } else if (o is CharArray) {
            val arr = o as CharArray?
            if (arr!!.size > index) {
                val str: String = Caster.toString(value, null)
                if (str != null && str.length() > 0) {
                    val c: Char = str.charAt(0)
                    arr[index] = c
                    return str
                }
            }
            throw invalidIndex(index, arr.size)
        }
        throw ArrayUtilException("Object [" + Caster.toClassName(o).toString() + "] is not an Array")
    }

    private fun invalidIndex(index: Int, length: Int): ArrayUtilException? {
        return ArrayUtilException("Invalid index [$index] for native Array call, Array has a Size of $length")
    }

    /**
     * sets a value to an array at defined index
     *
     * @param o
     * @param index
     * @param value
     * @return value setted
     */
    fun setEL(o: Object?, index: Int, value: Object?): Object? {
        return try {
            set(o, index, value)
        } catch (e: ArrayUtilException) {
            null
        }
    }

    fun isEmpty(list: List?): Boolean {
        return list == null || list.isEmpty()
    }

    fun isEmpty(array: Array<Object?>?): Boolean {
        return array == null || array.size == 0
    }

    fun isEmpty(array: BooleanArray?): Boolean {
        return array == null || array.size == 0
    }

    fun isEmpty(array: CharArray?): Boolean {
        return array == null || array.size == 0
    }

    fun isEmpty(array: DoubleArray?): Boolean {
        return array == null || array.size == 0
    }

    fun isEmpty(array: LongArray?): Boolean {
        return array == null || array.size == 0
    }

    fun isEmpty(array: IntArray?): Boolean {
        return array == null || array.size == 0
    }

    fun isEmpty(array: FloatArray?): Boolean {
        return array == null || array.size == 0
    }

    fun isEmpty(array: ByteArray?): Boolean {
        return array == null || array.size == 0
    }

    fun size(array: Array<Object?>?): Int {
        return array?.size ?: 0
    }

    fun size(array: BooleanArray?): Int {
        return array?.size ?: 0
    }

    fun size(array: CharArray?): Int {
        return array?.size ?: 0
    }

    fun size(array: DoubleArray?): Int {
        return array?.size ?: 0
    }

    fun size(array: LongArray?): Int {
        return array?.size ?: 0
    }

    fun size(array: IntArray?): Int {
        return array?.size ?: 0
    }

    fun size(array: FloatArray?): Int {
        return array?.size ?: 0
    }

    fun size(array: ByteArray?): Int {
        return array?.size ?: 0
    }

    @Throws(PageException::class)
    fun toBooleanArray(obj: Object?): BooleanArray? {
        if (obj is BooleanArray) return obj
        val arr: Array = Caster.toArray(obj)
        val tarr = BooleanArray(arr.size())
        for (i in tarr.indices) {
            tarr[i] = Caster.toBooleanValue(arr.getE(i + 1))
        }
        return tarr
    }

    @Throws(PageException::class)
    fun toByteArray(obj: Object?): ByteArray? {
        if (obj is ByteArray) return obj
        val arr: Array = Caster.toArray(obj)
        val tarr = ByteArray(arr.size())
        for (i in tarr.indices) {
            tarr[i] = Caster.toByteValue(arr.getE(i + 1))
        }
        return tarr
    }

    @Throws(PageException::class)
    fun toShortArray(obj: Object?): ShortArray? {
        if (obj is ShortArray) return obj
        val arr: Array = Caster.toArray(obj)
        val tarr = ShortArray(arr.size())
        for (i in tarr.indices) {
            tarr[i] = Caster.toShortValue(arr.getE(i + 1))
        }
        return tarr
    }

    @Throws(PageException::class)
    fun toIntArray(obj: Object?): IntArray? {
        if (obj is IntArray) return obj
        val arr: Array = Caster.toArray(obj)
        val tarr = IntArray(arr.size())
        for (i in tarr.indices) {
            tarr[i] = Caster.toIntValue(arr.getE(i + 1))
        }
        return tarr
    }

    @Throws(PageException::class)
    fun toNullArray(obj: Object?): Array<Object?>? {
        val arr: Array = Caster.toArray(obj)
        val tarr: Array<Object?> = arrayOfNulls<Object?>(arr.size())
        for (i in tarr.indices) {
            tarr[i] = Caster.toNull(arr.getE(i + 1))
        }
        return tarr
    }

    @Throws(PageException::class)
    fun toLongArray(obj: Object?): LongArray? {
        if (obj is LongArray) return obj
        val arr: Array = Caster.toArray(obj)
        val tarr = LongArray(arr.size())
        for (i in tarr.indices) {
            tarr[i] = Caster.toLongValue(arr.getE(i + 1))
        }
        return tarr
    }

    @Throws(PageException::class)
    fun toFloatArray(obj: Object?): FloatArray? {
        if (obj is FloatArray) return obj
        val arr: Array = Caster.toArray(obj)
        val tarr = FloatArray(arr.size())
        for (i in tarr.indices) {
            tarr[i] = Caster.toFloatValue(arr.getE(i + 1))
        }
        return tarr
    }

    @Throws(PageException::class)
    fun toDoubleArray(obj: Object?): DoubleArray? {
        if (obj is DoubleArray) return obj
        val arr: Array = Caster.toArray(obj)
        val tarr = DoubleArray(arr.size())
        for (i in tarr.indices) {
            tarr[i] = Caster.toDoubleValue(arr.getE(i + 1))
        }
        return tarr
    }

    @Throws(PageException::class)
    fun toCharArray(obj: Object?): CharArray? {
        if (obj is CharArray) return obj
        val arr: Array = Caster.toArray(obj)
        val tarr = CharArray(arr.size())
        for (i in tarr.indices) {
            tarr[i] = Caster.toCharValue(arr.getE(i + 1))
        }
        return tarr
    }

    fun arrayContainsIgnoreEmpty(arr: Array?, value: String?, ignoreCase: Boolean): Int {
        var count = 0
        val len: Int = arr.size()
        for (i in 1..len) {
            val item: String = Caster.toString(arr.get(i, ""), "")
            if (ignoreCase) {
                if (StringUtil.indexOfIgnoreCase(item, value) !== -1) return count
            } else {
                if (item.indexOf(value) !== -1) return count
            }
            count++
        }
        return -1
    }

    @Throws(CasterException::class)
    fun toReferenceType(obj: Object?): Array<Object?>? {
        val ref: Array<Object?>? = toReferenceType(obj, null)
        if (ref != null) return ref
        throw CasterException(obj, Array<Object>::class.java)
    }

    fun toReferenceType(obj: Object?, defaultValue: Array<Object?>?): Array<Object?>? {
        if (obj is Array<Object>) return obj else if (obj is BooleanArray) return toReferenceType(obj as BooleanArray?) else if (obj is ByteArray) return toReferenceType(obj as ByteArray?) else if (obj is CharArray) return toReferenceType(obj as CharArray?) else if (obj is ShortArray) return toReferenceType(obj as ShortArray?) else if (obj is IntArray) return toReferenceType(obj as IntArray?) else if (obj is LongArray) return toReferenceType(obj as LongArray?) else if (obj is FloatArray) return toReferenceType(obj as FloatArray?) else if (obj is DoubleArray) return toReferenceType(obj as DoubleArray?)
        return defaultValue
    }

    fun clone(src: Array<Object?>?, trg: Array<Object?>?): Array<Object?>? {
        for (i in src.indices) {
            trg!![i] = src!![i]
        }
        return trg
    }

    fun keys(map: Map?): Array<Object?>? {
        if (map == null) return arrayOfNulls<Object?>(0)
        val set: Set = map.keySet() ?: return arrayOfNulls<Object?>(0)
        return set.toArray() ?: return arrayOfNulls<Object?>(0)
    }

    fun values(map: Map?): Array<Object?>? {
        return if (map == null) arrayOfNulls<Object?>(0) else map.values().toArray()
    }

    /**
     * creates a native array out of the input list, if all values are from the same type, this type is
     * used for the array, otherwise object
     *
     * @param list
     */
    fun toArray(list: List<*>?): Array<Object?>? {
        val it = list!!.iterator()
        var clazz: Class? = null
        while (it.hasNext()) {
            val v = it.next() ?: continue
            if (clazz == null) clazz = v.getClass() else if (clazz !== v.getClass()) return list.toArray()
        }
        if (clazz === Object::class.java || clazz == null) return list.toArray()
        val arr: Object = java.lang.reflect.Array.newInstance(clazz, list.size())
        return list.toArray(arr as Array<Object?>)
    }

    @Throws(PageException::class)
    fun toComparator(pc: PageContext?, strSortType: String?, sortOrder: String?, localeSensitive: Boolean): Comparator? {

        // check order
        var isAsc = true
        isAsc = if (sortOrder.equalsIgnoreCase("asc")) true else if (sortOrder.equalsIgnoreCase("desc")) false else throw ExpressionException("invalid sort order type [$sortOrder], sort order types are [asc and desc]")

        // check type
        val sortType: Int
        sortType = if (strSortType.equalsIgnoreCase("text")) ComparatorUtil.SORT_TYPE_TEXT else if (strSortType.equalsIgnoreCase("textnocase")) ComparatorUtil.SORT_TYPE_TEXT_NO_CASE else if (strSortType.equalsIgnoreCase("numeric")) ComparatorUtil.SORT_TYPE_NUMBER else throw ExpressionException("invalid sort type [$strSortType], sort types are [text, textNoCase, numeric]")
        return ComparatorUtil.toComparator(sortType, isAsc, if (localeSensitive) ThreadLocalPageContext.getLocale(pc) else null, null)
    }

    fun <E> merge(a1: Array<E?>?, a2: Array<E?>?): List<E?>? {
        val list: List<E?> = ArrayList<E?>()
        for (i in a1.indices) {
            list.add(a1!![i])
        }
        for (i in a2.indices) {
            list.add(a2!![i])
        }
        return list
    }

    /**
     * this method efficiently copy the contents of one native array into another by using
     * System.arraycopy()
     *
     * @param dst - the array that will be modified
     * @param src - the data to be copied
     * @param dstPosition - pass -1 to append to the end of the dst array, or a valid position to add it
     * elsewhere
     * @param doPowerOf2 - if true, and the array needs to be resized, it will be resized to the next
     * power of 2 size
     * @return - either the original dst array if it had enough capacity, or a new array.
     */
    fun mergeNativeArrays(dst: Array<Object?>?, src: Array<Object?>?, dstPosition: Int, doPowerOf2: Boolean): Array<Object?>? {
        var dstPosition = dstPosition
        if (dstPosition < 0) dstPosition = dst!!.size
        val result: Array<Object?>? = resizeIfNeeded(dst, dstPosition + src!!.size, doPowerOf2)
        System.arraycopy(src, 0, result, dstPosition, src.size)
        return result
    }

    /**
     * this method returns the original array if its length is equal or greater than the minSize, or
     * create a new array and copies the data from the original array into the new one.
     *
     * @param arr - the array to check
     * @param minSize - the required minimum size
     * @param doPowerOf2 - if true, and a resize is required, the new size will be a power of 2
     * @return - either the original arr array if it had enough capacity, or a new array.
     */
    fun resizeIfNeeded(arr: Array<Object?>?, minSize: Int, doPowerOf2: Boolean): Array<Object?>? {
        var minSize = minSize
        if (arr!!.size >= minSize) return arr
        if (doPowerOf2) minSize = MathUtil.nextPowerOf2(minSize)
        val result: Array<Object?> = arrayOfNulls<Object?>(minSize)
        System.arraycopy(arr, 0, result, 0, arr.size)
        return result
    }

    fun toArray(arr1: Array<String?>?, arr2: Array<String?>?): Array<String?>? {
        val ret = arrayOfNulls<String?>(arr1!!.size + arr2!!.size)
        for (i in arr1.indices) {
            ret[i] = arr1[i]
        }
        for (i in arr2.indices) {
            ret[arr1.size + i] = arr2[i]
        }
        return ret
    }

    fun toArray(arr1: Array<String?>?, arr2: Array<String?>?, arr3: Array<String?>?): Array<String?>? {
        val ret = arrayOfNulls<String?>(arr1!!.size + arr2!!.size + arr3!!.size)
        for (i in arr1.indices) {
            ret[i] = arr1[i]
        }
        for (i in arr2.indices) {
            ret[arr1.size + i] = arr2[i]
        }
        for (i in arr3.indices) {
            ret[arr1.size + arr2.size + i] = arr3[i]
        }
        return ret
    }

    fun toArray(arr: Array<String?>?, str: String?): Array<String?>? {
        val ret = arrayOfNulls<String?>(arr!!.size + 1)
        for (i in arr.indices) {
            ret[i] = arr[i]
        }
        ret[arr.size] = str
        return ret
    }

    fun toArray(arr: Array<String?>?, str1: String?, str2: String?): Array<String?>? {
        val ret = arrayOfNulls<String?>(arr!!.size + 2)
        for (i in arr.indices) {
            ret[i] = arr[i]
        }
        ret[arr.size] = str1
        ret[arr.size + 1] = str2
        return ret
    }

    fun toArray(arr: Array<String?>?, str1: String?, str2: String?, str3: String?): Array<String?>? {
        val ret = arrayOfNulls<String?>(arr!!.size + 3)
        for (i in arr.indices) {
            ret[i] = arr[i]
        }
        ret[arr.size] = str1
        ret[arr.size + 1] = str2
        ret[arr.size + 2] = str3
        return ret
    }

    fun toArray(arr: Array<String?>?, str1: String?, str2: String?, str3: String?, str4: String?): Array<String?>? {
        val ret = arrayOfNulls<String?>(arr!!.size + 4)
        for (i in arr.indices) {
            ret[i] = arr[i]
        }
        ret[arr.size] = str1
        ret[arr.size + 1] = str2
        ret[arr.size + 2] = str3
        ret[arr.size + 3] = str4
        return ret
    }

    fun addAll(list: List?, arr: Array<Object?>?) {
        for (i in arr.indices) {
            list.add(arr!![i])
        }
    }

    fun toArrayPro(array: Array?): ArrayPro? {
        return if (array is ArrayPro) array as ArrayPro? else ArrayAsArrayPro(array)
    }

    @Throws(PageException::class)
    fun getMetaData(arr: Array?): Struct? {
        val sct: Struct = StructImpl()
        sct.set(KeyConstants._type, if (arr is ArrayImpl && (arr as ArrayImpl?).sync()) "synchronized" else "unsynchronized")
        sct.set("dimensions", arr.getDimension())
        sct.set("datatype", if (arr is ArrayTyped) (arr as ArrayTyped?).getTypeAsString() else "any")
        return sct
    }
}