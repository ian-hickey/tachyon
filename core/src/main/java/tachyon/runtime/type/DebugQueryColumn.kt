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

/**
 * implementation of the query column
 */
class DebugQueryColumn : QueryColumnImpl, QueryColumnPro, Objects, Cloneable {
    private var used = false

    /**
     * @return the used
     */
    fun isUsed(): Boolean {
        return used
    }

    constructor(data: Array<Object?>?, key: Key?, query: QueryImpl?, size: AtomicInteger?, type: Int, typeChecked: Boolean) {
        data = data
        key = key
        query = query
        size = size
        type = type
        typeChecked = typeChecked
    }

    /**
     * Constructor of the class for internal usage only
     */
    constructor() : super() {}

    @Override
    @Throws(DeprecatedException::class)
    override operator fun get(row: Int): Object? {
        used = true
        return super.get(row)
    }

    /**
     * touch the given line on the column at given row
     *
     * @param row
     * @return new row or existing
     * @throws DatabaseException
     */
    @Override
    override fun touch(row: Int): Object? {
        used = true
        return super.touch(row)
    }

    /**
     * touch the given line on the column at given row
     *
     * @param row
     * @return new row or existing
     * @throws DatabaseException
     */
    @Override
    override fun touchEL(row: Int): Object? {
        used = true
        return super.touchEL(row)
    }

    @Override
    override operator fun get(row: Int, defaultValue: Object?): Object? {
        used = true
        return super.get(row, defaultValue)
    }

    @Override
    override fun clone(): Object {
        return cloneColumnImpl(true)
    }

    @Override
    override fun duplicate(deepCopy: Boolean): Collection? {
        return cloneColumnImpl(deepCopy)
    }

    @Override
    override fun cloneColumn(deepCopy: Boolean): QueryColumnPro? {
        return cloneColumnImpl(deepCopy)
    }

    override fun cloneColumnImpl(deepCopy: Boolean): DebugQueryColumn? {
        val clone = DebugQueryColumn()
        populate(clone, deepCopy)
        return clone
    }

    @Override
    override fun valueIterator(): Iterator<Object?>? {
        used = true
        return super.valueIterator()
    }

    @Override
    override fun indexOf(o: Object?): Int {
        used = true
        return super.indexOf(o)
    }

    @Override
    override fun lastIndexOf(o: Object?): Int {
        used = true
        return super.lastIndexOf(o)
    }

    @Override
    override fun subList(fromIndex: Int, toIndex: Int): List<Object?>? {
        used = true
        return super.subList(fromIndex, toIndex)
    }

    @Override
    override fun toArray(): Array<Object?>? {
        used = true
        return super.toArray()
    }

    @Override
    override fun toArray(trg: Array<Object?>?): Array<Object?>? {
        used = true
        return super.toArray(trg)
    }

    @Override
    override fun toDebugColumn(): QueryColumnPro? {
        return this
    }
}