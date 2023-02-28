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
/*
 * Copyright (c) 1997, 2006, Oracle and/or its affiliates. All rights reserved.
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
package lucee.commons.collection

import java.util.Collection

abstract class AbstractSet<E>
/**
 * Sole constructor. (For invocation by subclass constructors, typically implicit.)
 */
protected constructor() : AbstractCollection<E>(), Set<E> {
    // Comparison and hashing
    /**
     * Compares the specified object with this set for equality. Returns <tt>true</tt> if the given
     * object is also a set, the two sets have the same size, and every member of the given set is
     * contained in this set. This ensures that the <tt>equals</tt> method works properly across
     * different implementations of the <tt>Set</tt> interface.
     *
     *
     *
     * This implementation first checks if the specified object is this set; if so it returns
     * <tt>true</tt>. Then, it checks if the specified object is a set whose size is identical to the
     * size of this set; if not, it returns false. If so, it returns
     * <tt>containsAll((Collection) o)</tt>.
     *
     * @param o object to be compared for equality with this set
     * @return <tt>true</tt> if the specified object is equal to this set
     */
    @Override
    override fun equals(o: Object): Boolean {
        if (o === this) return true
        if (o !is Set) return false
        val c: Collection = o
        return if (c.size() !== size()) false else try {
            containsAll(c)
        } catch (unused: ClassCastException) {
            false
        } catch (unused: NullPointerException) {
            false
        }
    }

    @Override
    override fun hashCode(): Int {
        var h = 0
        val i: Iterator<E?> = iterator()
        while (i.hasNext()) {
            val obj = i.next()
            if (obj != null) h += obj.hashCode()
        }
        return h
    }

    @Override
    override fun removeAll(c: Collection<*>): Boolean {
        var modified = false
        if (size() > c.size()) {
            val i = c.iterator()
            while (i.hasNext()) {
                modified = modified or remove(i.next())
            }
        } else {
            val i: Iterator<*> = iterator()
            while (i.hasNext()) {
                if (c.contains(i.next())) {
                    i.remove()
                    modified = true
                }
            }
        }
        return modified
    }
}