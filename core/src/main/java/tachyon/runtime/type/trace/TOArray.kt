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

import java.util.Comparator

class TOArray(debugger: Debugger?, arr: Array?, type: Int, category: String?, text: String?) : TOCollection(debugger, arr, type, category, text), Array {
    private val arr: Array?
    @Override
    fun getDimension(): Int {
        log()
        return arr.getDimension()
    }

    @Override
    override operator fun get(key: Int, defaultValue: Object?): Object? {
        log("" + key)
        return arr.get(key, defaultValue)
        // return TraceObjectSupport.toTraceObject(debugger,arr.get(key, defaultValue),type,category,text);
    }

    @Override
    @Throws(PageException::class)
    fun getE(key: Int): Object? {
        log("" + key)
        return arr.getE(key)
        // return TraceObjectSupport.toTraceObject(debugger,arr.getE(key),type,category,text);
    }

    @Override
    override fun setEL(key: Int, value: Object?): Object? {
        log("" + key, value)
        return arr.setEL(key, value)
        // return TraceObjectSupport.toTraceObject(debugger,arr.setEL(key, value),type,category,text);
    }

    @Override
    @Throws(PageException::class)
    fun setE(key: Int, value: Object?): Object? {
        log("" + key, value)
        return arr.setEL(key, value)
        // return TraceObjectSupport.toTraceObject(debugger,arr.setEL(key, value),type,category,text);
    }

    @Override
    fun intKeys(): IntArray? {
        log()
        return arr.intKeys()
    }

    @Override
    @Throws(PageException::class)
    fun insert(key: Int, value: Object?): Boolean {
        log("" + key)
        return arr.insert(key, value)
    }

    @Override
    @Throws(PageException::class)
    fun append(o: Object?): Object? {
        log(o.toString())
        return arr.append(o)
        // return TraceObjectSupport.toTraceObject(debugger,arr.append(o),type,category,text);
    }

    @Override
    fun appendEL(o: Object?): Object? {
        log(o.toString())
        return arr.appendEL(o)
        // return TraceObjectSupport.toTraceObject(debugger,arr.appendEL(o),type,category,text);
    }

    @Override
    @Throws(PageException::class)
    fun prepend(o: Object?): Object? {
        log()
        return arr.prepend(o)
        // return TraceObjectSupport.toTraceObject(debugger,arr.prepend(o),type,category,text);
    }

    @Override
    @Throws(PageException::class)
    fun resize(to: Int) {
        log()
        arr.resize(to)
    }

    @Override
    @Throws(PageException::class)
    fun sort(sortType: String?, sortOrder: String?) {
        log()
        arr.sort(sortType, sortOrder)
    }

    @Override
    fun sortIt(comp: Comparator?) {
        log()
        arr.sortIt(comp)
    }

    @Override
    fun toArray(): Array<Object?>? {
        log()
        return arr.toArray()
    }

    @Override
    fun toList(): List? {
        log()
        return arr.toList()
    }

    @Override
    @Throws(PageException::class)
    fun removeE(key: Int): Object? {
        log("" + key)
        return arr.removeE(key)
        // return TraceObjectSupport.toTraceObject(debugger,arr.removeE(key),type,category,text);
    }

    @Override
    override fun removeEL(key: Int): Object? {
        log("" + key)
        return arr.removeEL(key)
        // return TraceObjectSupport.toTraceObject(debugger,arr.removeEL(key),type,category,text);
    }

    @Override
    override fun remove(key: Collection.Key?, defaultValue: Object?): Object? {
        log("" + key)
        return arr.remove(key, defaultValue)
    }

    @Override
    override fun containsKey(key: Int): Boolean {
        log("" + key)
        return arr.containsKey(key)
    }

    @Override
    fun duplicate(deepCopy: Boolean): Collection? {
        log()
        return TOArray(debugger, Duplicator.duplicate(arr, deepCopy) as Array, type, category, text)
    }

    @Override
    fun getIterator(): Iterator<Object?>? {
        return valueIterator()
    }

    companion object {
        private const val serialVersionUID = 5130217962217368552L
    }

    init {
        this.arr = arr
    }
}