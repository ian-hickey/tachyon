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
package tachyon.runtime.type.trace

import java.util.Iterator

abstract class TOCollection protected constructor(debugger: Debugger?, coll: Collection?, type: Int, category: String?, text: String?) : TOObjects(debugger, coll, type, category, text), Collection, Cloneable {
    private val coll: Collection?
    @Override
    fun clone(): Object {
        return duplicate(true)
    }

    @Override
    fun keyIterator(): Iterator<Collection.Key?>? {
        log()
        return coll.keyIterator()
    }

    @Override
    fun keysAsStringIterator(): Iterator<String?>? {
        log()
        return coll.keysAsStringIterator()
    }

    @Override
    fun entryIterator(): Iterator<Entry<Key?, Object?>?>? {
        log()
        return coll.entryIterator()
    }

    @Override
    fun valueIterator(): Iterator<Object?>? {
        log()
        return coll.valueIterator()
    }

    @Override
    @Throws(PageException::class)
    override fun castToString(): String? {
        log()
        return coll.castToString()
    }

    @Override
    override fun castToString(defaultValue: String?): String? {
        log()
        return coll.castToString(defaultValue)
    }

    @Override
    @Throws(PageException::class)
    override fun castToBooleanValue(): Boolean {
        log()
        return coll.castToBooleanValue()
    }

    @Override
    override fun castToBoolean(defaultValue: Boolean?): Boolean? {
        log()
        return coll.castToBoolean(defaultValue)
    }

    @Override
    @Throws(PageException::class)
    override fun castToDoubleValue(): Double {
        log()
        return coll.castToDoubleValue()
    }

    @Override
    override fun castToDoubleValue(defaultValue: Double): Double {
        log()
        return coll.castToDoubleValue(defaultValue)
    }

    @Override
    @Throws(PageException::class)
    override fun castToDateTime(): DateTime? {
        log()
        return TODateTime(debugger, coll.castToDateTime(), type, category, text)
    }

    @Override
    override fun castToDateTime(defaultValue: DateTime?): DateTime? {
        log()
        return TODateTime(debugger, coll.castToDateTime(defaultValue), type, category, text)
    }

    @Override
    @Throws(PageException::class)
    override operator fun compareTo(str: String?): Int {
        log()
        return coll.compareTo(str)
    }

    @Override
    @Throws(PageException::class)
    override operator fun compareTo(b: Boolean): Int {
        log()
        return coll.compareTo(b)
    }

    @Override
    @Throws(PageException::class)
    override operator fun compareTo(d: Double): Int {
        log()
        return coll.compareTo(d)
    }

    @Override
    @Throws(PageException::class)
    override operator fun compareTo(dt: DateTime?): Int {
        log()
        return coll.compareTo(dt)
    }

    @Override
    fun size(): Int {
        log()
        return coll.size()
    }

    @Override
    fun keys(): Array<Key?>? {
        log()
        return coll.keys()
    }

    @Override
    @Throws(PageException::class)
    fun remove(key: Key?): Object? {
        log(key.getString())
        return coll.remove(key)
        // return TraceObjectSupport.toTraceObject(debugger,coll.remove(key),type,category,text);
    }

    @Override
    fun remove(key: Key?, defaultValue: Object?): Object? {
        log(key.getString())
        return coll.remove(key, defaultValue)
        // return TraceObjectSupport.toTraceObject(debugger,coll.remove(key),type,category,text);
    }

    @Override
    fun removeEL(key: Key?): Object? {
        log(key.getString())
        return coll.removeEL(key)
        // return TraceObjectSupport.toTraceObject(debugger,coll.removeEL(key),type,category,text);
    }

    @Override
    fun clear() {
        log()
        coll.clear()
    }

    @Override
    @Throws(PageException::class)
    operator fun get(key: String?): Object? {
        log(key)
        return coll.get(KeyImpl.init(key))
        // return TraceObjectSupport.toTraceObject(debugger,coll.get(key),type,category,text);
    }

    @Override
    @Throws(PageException::class)
    operator fun get(key: Key?): Object? {
        log(key.getString())
        return coll.get(key)
        // return TraceObjectSupport.toTraceObject(debugger,coll.get(key),type,category,text);
    }

    @Override
    override operator fun get(key: String?, defaultValue: Object?): Object? {
        log(key)
        return coll.get(key, defaultValue)
        // return TraceObjectSupport.toTraceObject(debugger,coll.get(key, defaultValue),type,category,text);
    }

    @Override
    override operator fun get(key: Key?, defaultValue: Object?): Object? {
        log(key.getString())
        return coll.get(key, defaultValue)
        // return TraceObjectSupport.toTraceObject(debugger,coll.get(key,defaultValue),type,category,text);
    }

    @Override
    @Throws(PageException::class)
    operator fun set(key: String?, value: Object?): Object? {
        log(key, value)
        return coll.set(key, value)
        // return TraceObjectSupport.toTraceObject(debugger,coll.set(key, value),type,category,text);
    }

    @Override
    @Throws(PageException::class)
    operator fun set(key: Key?, value: Object?): Object? {
        log(key.getString(), value)
        return coll.set(key, value)
        // return TraceObjectSupport.toTraceObject(debugger,coll.set(key, value),type,category,text);
    }

    @Override
    fun setEL(key: String?, value: Object?): Object? {
        log(key, value)
        return coll.setEL(key, value)
        // return TraceObjectSupport.toTraceObject(debugger,coll.setEL(key, value),type,category,text);
    }

    @Override
    fun setEL(key: Key?, value: Object?): Object? {
        log(key.getString(), value)
        return coll.setEL(key, value)
        // return TraceObjectSupport.toTraceObject(debugger,coll.setEL(key, value),type,category,text);
    }

    @Override
    fun containsKey(key: String?): Boolean {
        log(key)
        return coll.containsKey(key)
    }

    @Override
    fun containsKey(key: Key?): Boolean {
        log(key.getString())
        return coll.containsKey(key)
    }

    @Override
    override fun toDumpData(pageContext: PageContext?, maxlevel: Int, properties: DumpProperties?): DumpData? {
        log()
        return coll.toDumpData(pageContext, maxlevel, properties)
    }

    companion object {
        private const val serialVersionUID = -6006915508424163880L
    }

    init {
        this.coll = coll
    }
}