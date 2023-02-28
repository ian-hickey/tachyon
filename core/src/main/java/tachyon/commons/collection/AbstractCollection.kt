/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Tachyon Assosication Switzerland
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
/*
 * Copyright (c) 1997, 2010, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */
package tachyon.commons.collection

import java.util.Arrays

abstract class AbstractCollection<E>
/**
 * Sole constructor. (For invocation by subclass constructors, typically implicit.)
 */
protected constructor() : Collection<E> {
    // Query Operations
    /**
     * Returns an iterator over the elements contained in this collection.
     *
     * @return an iterator over the elements contained in this collection
     */
    @Override
    abstract override fun iterator(): Iterator<E>
    @Override
    abstract fun size(): Int

    /**
     * {@inheritDoc}
     *
     *
     *
     * This implementation returns <tt>size() == 0</tt>.
     */
    @Override
    override fun isEmpty(): Boolean {
        return size() == 0
    }

    /**
     * {@inheritDoc}
     *
     *
     *
     * This implementation iterates over the elements in the collection, checking each element in turn
     * for equality with the specified element.
     *
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     */
    @Override
    override fun contains(o: Object?): Boolean {
        val it: Iterator<E?> = iterator()
        if (o == null) {
            while (it.hasNext()) if (it.next() == null) return true
        } else {
            while (it.hasNext()) if (o.equals(it.next())) return true
        }
        return false
    }

    /**
     * {@inheritDoc}
     *
     *
     *
     * This implementation returns an array containing all the elements returned by this collection's
     * iterator, in the same order, stored in consecutive elements of the array, starting with index
     * `0`. The length of the returned array is equal to the number of elements returned by the
     * iterator, even if the size of this collection changes during iteration, as might happen if the
     * collection permits concurrent modification during iteration. The `size` method is called
     * only as an optimization hint; the correct result is returned even if the iterator returns a
     * different number of elements.
     *
     *
     *
     * This method is equivalent to:
     *
     * <pre>
     * {
     * &#64;code
     * List<E> list = new ArrayList<E>(size());
     * for (E e: this)
     * list.add(e);
     * return list.toArray();
     * }
    </E></E></pre> *
     */
    @Override
    fun toArray(): Array<Object?> {
        // Estimate size of array; be prepared to see more or fewer elements
        val r: Array<Object?> = arrayOfNulls<Object>(size())
        val it = iterator()
        for (i in r.indices) {
            if (!it.hasNext()) // fewer elements than expected
                return Arrays.copyOf(r, i)
            r[i] = it.next()
        }
        return if (it.hasNext()) finishToArray<Object?>(r, it) else r
    }

    /**
     * {@inheritDoc}
     *
     *
     *
     * This implementation returns an array containing all the elements returned by this collection's
     * iterator in the same order, stored in consecutive elements of the array, starting with index
     * `0`. If the number of elements returned by the iterator is too large to fit into the
     * specified array, then the elements are returned in a newly allocated array with length equal to
     * the number of elements returned by the iterator, even if the size of this collection changes
     * during iteration, as might happen if the collection permits concurrent modification during
     * iteration. The `size` method is called only as an optimization hint; the correct result is
     * returned even if the iterator returns a different number of elements.
     *
     *
     *
     * This method is equivalent to:
     *
     * <pre>
     * {
     * &#64;code
     * List<E> list = new ArrayList<E>(size());
     * for (E e: this)
     * list.add(e);
     * return list.toArray(a);
     * }
    </E></E></pre> *
     *
     * @throws ArrayStoreException {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     */
    @Override
    fun <T> toArray(a: Array<T?>): Array<T?> {
        // Estimate size of array; be prepared to see more or fewer elements
        val size = size()
        val r: Array<T?> = if (a.size >= size) a else java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), size)
        val it = iterator()
        for (i in r.indices) {
            if (!it.hasNext()) { // fewer elements than expected
                if (a != r) return Arrays.copyOf(r, i)
                r[i] = null // null-terminate
                return r
            }
            r[i] = it.next() as T
        }
        return if (it.hasNext()) finishToArray(r, it) else r
    }
    // Modification Operations
    /**
     * {@inheritDoc}
     *
     *
     *
     * This implementation always throws an <tt>UnsupportedOperationException</tt>.
     *
     * @throws UnsupportedOperationException {@inheritDoc}
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     * @throws IllegalArgumentException {@inheritDoc}
     * @throws IllegalStateException {@inheritDoc}
     */
    @Override
    fun add(e: E): Boolean {
        throw UnsupportedOperationException()
    }

    /**
     * {@inheritDoc}
     *
     *
     *
     * This implementation iterates over the collection looking for the specified element. If it finds
     * the element, it removes the element from the collection using the iterator's remove method.
     *
     *
     *
     * Note that this implementation throws an <tt>UnsupportedOperationException</tt> if the iterator
     * returned by this collection's iterator method does not implement the <tt>remove</tt> method and
     * this collection contains the specified object.
     *
     * @throws UnsupportedOperationException {@inheritDoc}
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     */
    @Override
    fun remove(o: Object?): Boolean {
        val it: Iterator<E?> = iterator()
        if (o == null) {
            while (it.hasNext()) {
                if (it.next() == null) {
                    it.remove()
                    return true
                }
            }
        } else {
            while (it.hasNext()) {
                if (o.equals(it.next())) {
                    it.remove()
                    return true
                }
            }
        }
        return false
    }

    // Bulk Operations
    @Override
    fun containsAll(c: Collection<*>): Boolean {
        for (e in c) if (!contains(e)) return false
        return true
    }

    @Override
    fun addAll(c: Collection<E>): Boolean {
        var modified = false
        for (e in c) if (add(e)) modified = true
        return modified
    }

    @Override
    fun removeAll(c: Collection<*>): Boolean {
        var modified = false
        val it: Iterator<*> = iterator()
        while (it.hasNext()) {
            if (c.contains(it.next())) {
                it.remove()
                modified = true
            }
        }
        return modified
    }

    @Override
    fun retainAll(c: Collection<*>): Boolean {
        var modified = false
        val it = iterator()
        while (it.hasNext()) {
            if (!c.contains(it.next())) {
                it.remove()
                modified = true
            }
        }
        return modified
    }

    /**
     * {@inheritDoc}
     *
     *
     *
     * This implementation iterates over this collection, removing each element using the
     * <tt>Iterator.remove</tt> operation. Most implementations will probably choose to override this
     * method for efficiency.
     *
     *
     *
     * Note that this implementation will throw an <tt>UnsupportedOperationException</tt> if the
     * iterator returned by this collection's <tt>iterator</tt> method does not implement the
     * <tt>remove</tt> method and this collection is non-empty.
     *
     * @throws UnsupportedOperationException {@inheritDoc}
     */
    @Override
    fun clear() {
        val it = iterator()
        while (it.hasNext()) {
            it.next()
            it.remove()
        }
    }
    // String conversion
    /**
     * Returns a string representation of this collection. The string representation consists of a list
     * of the collection's elements in the order they are returned by its iterator, enclosed in square
     * brackets (<tt>"[]"</tt>). Adjacent elements are separated by the characters <tt>", "</tt> (comma
     * and space). Elements are converted to strings as by [String.valueOf].
     *
     * @return a string representation of this collection
     */
    @Override
    override fun toString(): String {
        val it = iterator()
        if (!it.hasNext()) return "[]"
        val sb = StringBuilder()
        sb.append('[')
        while (true) {
            val e = it.next()
            sb.append(if (e === this) "(this Collection)" else e)
            if (!it.hasNext()) return sb.append(']').toString()
            sb.append(',').append(' ')
        }
    }

    companion object {
        /**
         * The maximum size of array to allocate. Some VMs reserve some header words in an array. Attempts
         * to allocate larger arrays may result in OutOfMemoryError: Requested array size exceeds VM limit
         */
        private val MAX_ARRAY_SIZE: Int = Integer.MAX_VALUE - 8

        /**
         * Reallocates the array being used within toArray when the iterator returned more elements than
         * expected, and finishes filling it from the iterator.
         *
         * @param r the array, replete with previously stored elements
         * @param it the in-progress iterator over this collection
         * @return array containing the elements in the given array, plus any further elements returned by
         * the iterator, trimmed to size
         */
        private fun <T> finishToArray(r: Array<T?>, it: Iterator<*>): Array<T?> {
            var r = r
            var i = r.size
            while (it.hasNext()) {
                val cap = r.size
                if (i == cap) {
                    var newCap = cap + (cap shr 1) + 1
                    // overflow-conscious code
                    if (newCap - MAX_ARRAY_SIZE > 0) newCap = hugeCapacity(cap + 1)
                    r = Arrays.copyOf(r, newCap)
                }
                r[i++] = it.next() as T
            }
            // trim if overallocated
            return if (i == r.size) r else Arrays.copyOf(r, i)
        }

        private fun hugeCapacity(minCapacity: Int): Int {
            if (minCapacity < 0) throw OutOfMemoryError("Required array size too large")
            return if (minCapacity > MAX_ARRAY_SIZE) Integer.MAX_VALUE else MAX_ARRAY_SIZE
        }
    }
}