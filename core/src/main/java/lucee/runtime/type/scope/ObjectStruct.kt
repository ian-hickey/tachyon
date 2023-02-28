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
package lucee.runtime.type.scope

import java.util.Iterator

class ObjectStruct : StructSupport, Struct, Objects {
    private var jo: JavaObject? = null

    constructor(o: Object?) {
        if (o is JavaObject) jo = o as JavaObject? else jo = JavaObject(ThreadLocalPageContext.get().getVariableUtil(), o)
    }

    constructor(jo: JavaObject?) {
        this.jo = jo
    }

    @Override
    @Throws(PageException::class)
    fun call(pc: PageContext?, methodName: Key?, arguments: Array<Object?>?): Object? {
        return jo.call(pc, methodName, arguments)
    }

    @Override
    @Throws(PageException::class)
    fun callWithNamedValues(pc: PageContext?, methodName: Key?, args: Struct?): Object? {
        return jo.callWithNamedValues(pc, methodName, args)
    }

    @Override
    @Throws(PageException::class)
    operator fun get(pc: PageContext?, key: Key?): Object? {
        return jo.get(pc, key)
    }

    @Override
    operator fun get(pc: PageContext?, key: Key?, defaultValue: Object?): Object? {
        return jo.get(pc, key, defaultValue)
    }

    fun isInitalized(): Boolean {
        return jo.isInitalized()
    }

    @Override
    @Throws(PageException::class)
    operator fun set(pc: PageContext?, propertyName: Key?, value: Object?): Object? {
        return jo.set(pc, propertyName, value)
    }

    @Override
    fun setEL(pc: PageContext?, propertyName: Key?, value: Object?): Object? {
        return jo.setEL(pc, propertyName, value)
    }

    @Override
    fun clear() {
        // throw new PageRuntimeException(new ExpressionException("can't clear fields from object
        // ["+objects.getClazz().getName()+"]"));
    }

    @Override
    fun duplicate(deepCopy: Boolean): Collection? {
        throw PageRuntimeException(ExpressionException("can't clone object of type [" + jo.getClazz().getName().toString() + "]"))
        // return null;
    }

    @Override
    fun containsKey(key: Key?): Boolean {
        return Reflector.hasPropertyIgnoreCase(jo.getClazz(), key.getString())
    }

    @Override
    fun containsKey(pc: PageContext?, key: Key?): Boolean {
        return Reflector.hasPropertyIgnoreCase(jo.getClazz(), key.getString())
    }

    @Override
    @Throws(PageException::class)
    operator fun get(key: Key?): Object? {
        return jo.get(ThreadLocalPageContext.get(), key)
    }

    @Override
    operator fun get(key: Key?, defaultValue: Object?): Object? {
        return jo.get(ThreadLocalPageContext.get(), key, defaultValue)
    }

    @Override
    fun keys(): Array<Key?>? {
        val strKeys: Array<String?> = Reflector.getPropertyKeys(jo.getClazz())
        val keys: Array<Key?> = arrayOfNulls<Key?>(strKeys.size)
        for (i in strKeys.indices) {
            keys[i] = KeyImpl.init(strKeys[i])
        }
        return keys
    }

    @Override
    @Throws(PageException::class)
    fun remove(key: Key?): Object? {
        throw ExpressionException("can't remove field [" + key.getString().toString() + "] from object [" + jo.getClazz().getName().toString() + "]")
    }

    @Override
    fun removeEL(key: Key?): Object? {
        return null
    }

    @Override
    @Throws(PageException::class)
    operator fun set(key: Key?, value: Object?): Object? {
        return jo.set(ThreadLocalPageContext.get(), key, value)
    }

    @Override
    fun setEL(key: Key?, value: Object?): Object? {
        return jo.setEL(ThreadLocalPageContext.get(), key, value)
    }

    @Override
    fun size(): Int {
        return keys()!!.size
    }

    @Override
    fun toDumpData(pageContext: PageContext?, maxlevel: Int, dp: DumpProperties?): DumpData? {
        return jo.toDumpData(pageContext, maxlevel, dp)
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
        return ValueIterator(this, keys())
    }

    @Override
    @Throws(PageException::class)
    fun castToBooleanValue(): Boolean {
        return jo.castToBooleanValue()
    }

    @Override
    fun castToBoolean(defaultValue: Boolean?): Boolean? {
        return jo.castToBoolean(defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun castToDateTime(): DateTime? {
        return jo.castToDateTime()
    }

    @Override
    fun castToDateTime(defaultValue: DateTime?): DateTime? {
        return jo.castToDateTime(defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun castToDoubleValue(): Double {
        return jo.castToDoubleValue()
    }

    @Override
    fun castToDoubleValue(defaultValue: Double): Double {
        return jo.castToDoubleValue(defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun castToString(): String? {
        return jo.castToString()
    }

    @Override
    fun castToString(defaultValue: String?): String? {
        return jo.castToString(defaultValue)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(str: String?): Int {
        return jo.compareTo(str)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(b: Boolean): Int {
        return jo.compareTo(b)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(d: Double): Int {
        return jo.compareTo(d)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(dt: DateTime?): Int {
        return jo.compareTo(dt)
    }

    @Override
    fun getType(): Int {
        return Struct.TYPE_REGULAR
    }
}