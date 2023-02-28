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
package tachyon.commons.collection

import java.util.Collection

class SetMaxSize<E> private constructor(private val maxSize: Int, map: LinkedHashMapMaxSize<E, String>) : Set<E>, Cloneable {
    private val map: LinkedHashMapMaxSize<E, String>

    constructor(maxSize: Int) : this(maxSize, LinkedHashMapMaxSize<E, String>(maxSize)) {}

    @Override
    override fun iterator(): Iterator<E> {
        return map.keySet().iterator()
    }

    @Override
    fun size(): Int {
        return map.size()
    }

    @Override
    override fun isEmpty(): Boolean {
        return map.isEmpty()
    }

    @Override
    override fun contains(o: Object?): Boolean {
        return map.containsKey(o)
    }

    @Override
    fun add(e: E): Boolean {
        map.put(e, "")
        return true
    }

    @Override
    fun remove(o: Object?): Boolean {
        return map.remove(o) != null
    }

    @Override
    fun clear() {
        map.clear()
    }

    @Override
    fun clone(): Object {
        return SetMaxSize<E>(maxSize, map.clone() as LinkedHashMapMaxSize<E, String>)
    }

    @Override
    override fun equals(o: Object): Boolean {
        if (o !is SetMaxSize<*>) return false
        val other = o as SetMaxSize<*>
        return (o as SetMaxSize<*>).map.equals(map)
    }

    @Override
    override fun hashCode(): Int {
        return super.hashCode()
    }

    @Override
    fun removeAll(c: Collection<*>?): Boolean {
        throw UnsupportedOperationException()
    }

    @Override
    fun toArray(): Array<Object> {
        return map.keySet().toArray()
    }

    @Override
    fun <T> toArray(a: Array<T>?): Array<T> {
        return map.keySet().toArray(a)
    }

    @Override
    fun containsAll(c: Collection<*>?): Boolean {
        throw UnsupportedOperationException()
    }

    @Override
    fun addAll(c: Collection<E>?): Boolean {
        throw UnsupportedOperationException()
    }

    @Override
    fun retainAll(c: Collection<*>?): Boolean {
        throw UnsupportedOperationException()
    }

    @Override
    override fun toString(): String {
        return map.keySet().toString()
    }

    init {
        this.map = map
    }
}