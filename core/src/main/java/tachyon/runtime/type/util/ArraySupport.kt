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

import java.util.AbstractList

abstract class ArraySupport : AbstractList(), ArrayPro, List, Objects, Cloneable {
    @Override
    fun add(index: Int, element: Object?) {
        try {
            insert(index + 1, element)
        } catch (e: PageException) {
            throw IndexOutOfBoundsException("can't insert value to List at position " + index + ", " + "valid values are from 0 to " + (size() - 1) + ", size is " + size())
        }
    }

    @Override
    fun addAll(c: Collection<*>?): Boolean {
        val it: Iterator = c!!.iterator()
        while (it.hasNext()) {
            add(it.next())
        }
        return true
    }

    @Override
    fun remove(o: Object?): Boolean {
        val index: Int = indexOf(o)
        if (index == -1) return false
        try {
            removeE(index + 1)
        } catch (e: PageException) {
            return false
        }
        return true
    }

    @Override
    fun removeAll(c: Collection<*>?): Boolean {
        val it: Iterator = c!!.iterator()
        var rtn = false
        while (it.hasNext()) {
            if (remove(it.next())) rtn = true
        }
        return rtn
    }

    @Override
    fun retainAll(c: Collection<*>?): Boolean {
        var modified = false
        val keys: Array<Key?> = CollectionUtil.keys(this)
        var k: Key?
        for (i in keys.indices.reversed()) {
            k = keys[i]
            if (!c!!.contains(get(k, null))) {
                removeEL(k)
                modified = true
            }
        }
        return modified
    }

    @Override
    fun toArray(a: Array<Object?>?): Array<Object?>? {
        if (a == null) return toArray()
        val trgClass: Class = a.getClass().getComponentType()
        var type = TYPE_OBJECT
        if (trgClass === Boolean::class.java) type = TYPE_BOOLEAN else if (trgClass === Byte::class.java) type = TYPE_BYTE else if (trgClass === Short::class.java) type = TYPE_SHORT else if (trgClass === Integer::class.java) type = TYPE_INT else if (trgClass === Long::class.java) type = TYPE_LONG else if (trgClass === Float::class.java) type = TYPE_FLOAT else if (trgClass === Double::class.java) type = TYPE_DOUBLE else if (trgClass === Character::class.java) type = TYPE_CHARACTER else if (trgClass === String::class.java) type = TYPE_STRING
        val it: Iterator = iterator()
        var i = 0
        var o: Object
        try {
            while (it.hasNext()) {
                o = it.next()
                when (type) {
                    TYPE_BOOLEAN -> o = Caster.toBoolean(o)
                    TYPE_BYTE -> o = Caster.toByte(o)
                    TYPE_CHARACTER -> o = Caster.toCharacter(o)
                    TYPE_DOUBLE -> o = Caster.toDouble(o)
                    TYPE_FLOAT -> o = Caster.toFloat(o)
                    TYPE_INT -> o = Caster.toInteger(o)
                    TYPE_LONG -> o = Caster.toLong(o)
                    TYPE_SHORT -> o = Caster.toShort(o)
                    TYPE_STRING -> o = Caster.toString(o)
                }
                a[i++] = o
            }
        } catch (pe: PageException) {
            throw PageRuntimeException(pe)
        }
        return a
    }

    @Override
    operator fun get(index: Int): Object? {
        if (index < 0) throw IndexOutOfBoundsException("invalid index definition [" + index + "], " + "index should be a number between [0 - " + (size() - 1) + "], size is " + size())
        if (index >= size()) throw IndexOutOfBoundsException("invalid index [" + index + "] definition, " + "index should be a number between [0 - " + (size() - 1) + "], size is " + size())
        return get(index + 1, null)
    }

    @Override
    fun remove(index: Int): Object? {
        if (index < 0) throw IndexOutOfBoundsException("invalid index definition [" + index + "], " + "index should be a number between [0 - " + (size() - 1) + "], size is " + size())
        if (index >= size()) throw IndexOutOfBoundsException("invalid index [" + index + "] definition, " + "index should be a number between [0 - " + (size() - 1) + "], size is " + size())
        return removeEL(index + 1)
    }

    @Override
    fun remove(key: Collection.Key?, defaultValue: Object?): Object? {
        return try {
            remove(key)
        } catch (e: PageException) {
            defaultValue
        }
    }

    @Override
    operator fun set(index: Int, element: Object?): Object? {
        val o: Object? = get(index)
        setEL(index + 1, element)
        return o
    }

    @Override
    fun containsKey(key: String?): Boolean {
        return get(KeyImpl.init(key), null) != null
    }

    @Override
    fun containsKey(key: Collection.Key?): Boolean {
        return get(key, null) != null
    }

    @Override
    fun containsKey(key: Int): Boolean {
        return get(key, null) != null
    }

    @Override
    override fun toString(): String {
        return LazyConverter.serialize(this)
    }

    @Override
    fun clone(): Object {
        return duplicate(true)
    }

    @Override
    @Throws(PageException::class)
    fun castToString(): String? {
        throw ExpressionException("Can't cast Complex Object Type Array to String", "Use Built-In-Function \"serialize(Array):String\" to create a String from Array")
    }

    @Override
    fun castToString(defaultValue: String?): String? {
        return defaultValue
    }

    @Override
    @Throws(PageException::class)
    fun castToBooleanValue(): Boolean {
        throw ExpressionException("Can't cast Complex Object Type Array to a boolean value")
    }

    @Override
    fun castToBoolean(defaultValue: Boolean?): Boolean? {
        return defaultValue
    }

    @Override
    @Throws(PageException::class)
    fun castToDoubleValue(): Double {
        throw ExpressionException("Can't cast Complex Object Type Array to a number value")
    }

    @Override
    fun castToDoubleValue(defaultValue: Double): Double {
        return defaultValue
    }

    @Override
    @Throws(PageException::class)
    fun castToDateTime(): DateTime? {
        throw ExpressionException("Can't cast Complex Object Type Array to a Date")
    }

    @Override
    fun castToDateTime(defaultValue: DateTime?): DateTime? {
        return defaultValue
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(b: Boolean): Int {
        throw ExpressionException("can't compare Complex Object Type Array with a boolean value")
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(dt: DateTime?): Int {
        throw ExpressionException("can't compare Complex Object Type Array with a DateTime Object")
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(d: Double): Int {
        throw ExpressionException("can't compare Complex Object Type Array with a numeric value")
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(str: String?): Int {
        throw ExpressionException("can't compare Complex Object Type Array with a String")
    }

    @Override
    fun toList(): List? {
        return this
    }

    @Override
    fun valueIterator(): Iterator<Object?>? {
        return iterator()
    }

    /*
	 * @Override public Object get(PageContext pc, Key key, Object defaultValue) { return get(key,
	 * defaultValue); }
	 * 
	 * @Override public Object get(PageContext pc, Key key) throws PageException { return get(key); }
	 */
    @Override
    @Throws(PageException::class)
    operator fun set(pc: PageContext?, propertyName: Key?, value: Object?): Object? {
        return set(propertyName, value)
    }

    @Override
    fun setEL(pc: PageContext?, propertyName: Key?, value: Object?): Object? {
        return setEL(propertyName, value)
    }

    @Override
    @Throws(PageException::class)
    fun call(pc: PageContext?, methodName: Key?, args: Array<Object?>?): Object? {
        return MemberUtil.call(pc, this, methodName, args, shortArrayOf(CFTypes.TYPE_ARRAY), arrayOf<String?>("array"))
    }

    @Override
    @Throws(PageException::class)
    fun callWithNamedValues(pc: PageContext?, methodName: Key?, args: Struct?): Object? {
        return MemberUtil.callWithNamedValues(pc, this, methodName, args, CFTypes.TYPE_ARRAY, "array")
    }

    @get:Override
    val iterator: Iterator<Any?>?
        get() = valueIterator()

    @Override
    @Throws(PageException::class)
    fun sort(sortType: String?, sortOrder: String?) {
        if (getDimension() > 1) throw ExpressionException("only 1 dimensional arrays can be sorted")
        sortIt(ArrayUtil.toComparator(null, sortType, sortOrder, false))
    }

    @Override
    override fun equals(obj: Object?): Boolean {
        return if (obj !is Collection) false else CollectionUtil.equals(this, obj as Collection?)
    }

    /*
	 * @Override public int hashCode() { return CollectionUtil.hashCode(this); }
	 */
    @Override
    fun entryArrayIterator(): Iterator<Entry<Integer?, Object?>?>? {
        return EntryArrayIterator(this, intKeys())
    }

    companion object {
        const val TYPE_OBJECT: Short = 0
        const val TYPE_BOOLEAN: Short = 1
        const val TYPE_BYTE: Short = 2
        const val TYPE_SHORT: Short = 3
        const val TYPE_INT: Short = 4
        const val TYPE_LONG: Short = 5
        const val TYPE_FLOAT: Short = 6
        const val TYPE_DOUBLE: Short = 7
        const val TYPE_CHARACTER: Short = 8
        const val TYPE_STRING: Short = 9
    }
}