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
package tachyon.runtime.type

import java.util.Iterator

class CollectionStruct(coll: Collection?) : StructSupport(), ObjectWrap, Struct {
    private val coll: Collection?
    @Override
    fun clear() {
        coll.clear()
    }

    @Override
    fun containsKey(key: Key?): Boolean {
        return coll.containsKey(key)
    }

    @Override
    fun containsKey(pc: PageContext?, key: Key?): Boolean {
        return coll.containsKey(key)
    }

    @Override
    fun duplicate(deepCopy: Boolean): Collection? {
        return Duplicator.duplicate(coll, deepCopy)
    }

    @Override
    @Throws(PageException::class)
    operator fun get(key: Key?): Object? {
        return coll.get(key)
    }

    @Override
    @Throws(PageException::class)
    operator fun get(pc: PageContext?, key: Key?): Object? {
        return coll.get(key)
    }

    @Override
    operator fun get(key: Key?, defaultValue: Object?): Object? {
        return coll.get(key, defaultValue)
    }

    @Override
    operator fun get(pc: PageContext?, key: Key?, defaultValue: Object?): Object? {
        return coll.get(key, defaultValue)
    }

    @Override
    fun keys(): Array<Key?>? {
        return coll.keys()
    }

    @Override
    @Throws(PageException::class)
    fun remove(key: Key?): Object? {
        return coll.remove(key)
    }

    @Override
    fun removeEL(key: Key?): Object? {
        return coll.removeEL(key)
    }

    @Override
    @Throws(PageException::class)
    operator fun set(key: Key?, value: Object?): Object? {
        return coll.set(key, value)
    }

    @Override
    fun setEL(key: Key?, value: Object?): Object? {
        return coll.setEL(key, value)
    }

    @Override
    fun size(): Int {
        return coll.size()
    }

    @Override
    fun keyIterator(): Iterator<Collection.Key?>? {
        return coll.keyIterator()
    }

    @Override
    fun keysAsStringIterator(): Iterator<String?>? {
        return coll.keysAsStringIterator()
    }

    @Override
    fun entryIterator(): Iterator<Entry<Key?, Object?>?>? {
        return coll.entryIterator()
    }

    @Override
    fun valueIterator(): Iterator<Object?>? {
        return coll.valueIterator()
    }

    @Override
    fun toDumpData(pageContext: PageContext?, maxlevel: Int, properties: DumpProperties?): DumpData? {
        return coll.toDumpData(pageContext, maxlevel, properties)
    }

    @Override
    @Throws(PageException::class)
    fun castToBooleanValue(): Boolean {
        return coll.castToBooleanValue()
    }

    @Override
    @Throws(PageException::class)
    fun castToDoubleValue(): Double {
        return coll.castToDoubleValue()
    }

    @Override
    @Throws(PageException::class)
    fun castToDateTime(): DateTime? {
        return coll.castToDateTime()
    }

    @Override
    @Throws(PageException::class)
    fun castToString(): String? {
        return coll.castToString()
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(b: Boolean): Int {
        return coll.compareTo(b)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(dt: DateTime?): Int {
        return coll.compareTo(dt)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(d: Double): Int {
        return coll.compareTo(d)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(str: String?): Int {
        return coll.compareTo(str)
    }

    @Override
    fun getEmbededObject(defaultValue: Object?): Object? {
        return coll
    }

    @Override
    @Throws(PageException::class)
    fun getEmbededObject(): Object? {
        return coll
    }

    /**
     * @return
     */
    fun getCollection(): Collection? {
        return coll
    }

    @Override
    fun getType(): Int {
        return if (coll is StructSupport) (coll as StructSupport?).getType() else Struct.TYPE_REGULAR
    }

    init {
        this.coll = coll
    }
}