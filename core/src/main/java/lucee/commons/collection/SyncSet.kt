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
package lucee.commons.collection

import java.util.Set

class SyncSet<E> : SyncCollection<E>, Set<E> {
    constructor(s: Set<E>?) : super(s) {}
    constructor(s: Set<E>, mutex: Object?) : super(s, mutex) {}

    @Override
    override fun equals(o: Object): Boolean {
        if (this === o) return true
        synchronized(mutex) { return c.equals(o) }
    }

    @Override
    override fun hashCode(): Int {
        synchronized(mutex) { return c.hashCode() }
    }

    companion object {
        private const val serialVersionUID = 487447009682186044L
    }
}