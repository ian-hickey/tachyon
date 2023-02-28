/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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
package lucee.commons.collection

import java.util.Collection

class QueueMaxSize<E>(private val maxSize: Int) : Queue<E> {
    private val list: LinkedList<E> = LinkedList<E>()
    @Override
    fun add(e: E): Boolean {
        if (!list.add(e)) return false
        while (size() > maxSize) {
            list.remove()
        }
        return true
    }

    @Override
    fun size(): Int {
        return list.size()
    }

    @Override
    operator fun contains(o: Object?): Boolean {
        return list.contains(o)
    }

    @Override
    operator fun iterator(): Iterator<E> {
        return list.iterator()
    }

    @Override
    fun <T> toArray(a: Array<T>?): Array<T> {
        return list.toArray(a)
    }

    @Override
    fun remove(o: Object?): Boolean {
        return list.remove(o)
    }

    @Override
    fun clear() {
        list.clear()
    }

    @Override
    fun remove(): E {
        return list.remove()
    }

    @Override
    fun poll(): E {
        return list.poll()
    }

    @Override
    fun element(): E {
        return list.element()
    }

    @Override
    fun peek(): E {
        throw UnsupportedOperationException()
    }

    @get:Override
    val isEmpty: Boolean
        get() {
            throw UnsupportedOperationException()
        }

    @Override
    fun toArray(): Array<Object> {
        throw UnsupportedOperationException()
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
    fun removeAll(c: Collection<*>?): Boolean {
        throw UnsupportedOperationException()
    }

    @Override
    fun retainAll(c: Collection<*>?): Boolean {
        throw UnsupportedOperationException()
    }

    @Override
    fun offer(e: E): Boolean {
        throw UnsupportedOperationException()
    }
}