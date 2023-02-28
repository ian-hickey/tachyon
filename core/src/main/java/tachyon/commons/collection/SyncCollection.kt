/**
 * Copyright (c) 2023, TachyonCFML.org
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

import java.io.IOException

class SyncCollection<E> : Collection<E>, Serializable {
    val c // Backing Collection
            : Collection<E>
    val mutex // Object on which to synchronize
            : Object?

    internal constructor(c: Collection<E>?) {
        if (c == null) throw NullPointerException()
        this.c = c
        mutex = SerializableObject()
    }

    internal constructor(c: Collection<E>, mutex: Object?) {
        this.c = c
        this.mutex = mutex
    }

    @Override
    fun size(): Int {
        synchronized(mutex) { return c.size() }
    }

    @Override
    override fun isEmpty(): Boolean {
        synchronized(mutex) { return c.isEmpty() }
    }

    @Override
    override fun contains(o: Object): Boolean {
        synchronized(mutex) { return c.contains(o) }
    }

    @Override
    fun toArray(): Array<Object> {
        synchronized(mutex) { return c.toArray() }
    }

    @Override
    fun <T> toArray(a: Array<T>?): Array<T> {
        synchronized(mutex) { return c.toArray(a) }
    }

    @Override
    override fun iterator(): Iterator<E> {
        return c.iterator() // Must be manually synched by user!
    }

    @Override
    fun add(e: E): Boolean {
        synchronized(mutex) { return c.add(e) }
    }

    @Override
    fun remove(o: Object?): Boolean {
        synchronized(mutex) { return c.remove(o) }
    }

    @Override
    fun containsAll(coll: Collection<*>?): Boolean {
        synchronized(mutex) { return c.containsAll(coll) }
    }

    @Override
    fun addAll(coll: Collection<E>?): Boolean {
        synchronized(mutex) { return c.addAll(coll) }
    }

    @Override
    fun removeAll(coll: Collection<*>?): Boolean {
        synchronized(mutex) { return c.removeAll(coll) }
    }

    @Override
    fun retainAll(coll: Collection<*>?): Boolean {
        synchronized(mutex) { return c.retainAll(coll) }
    }

    @Override
    fun clear() {
        synchronized(mutex) { c.clear() }
    }

    @Override
    override fun toString(): String {
        synchronized(mutex) { return c.toString() }
    }

    @Throws(IOException::class)
    private fun writeObject(s: ObjectOutputStream) {
        synchronized(mutex) { s.defaultWriteObject() }
    }

    companion object {
        private const val serialVersionUID = 3053995032091335093L
    }
}