/**
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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
package lucee.runtime.type.it

import java.util.Iterator

class ComponentIterator(cfc: ComponentImpl?) : Iterator, Resetable {
    private val cfc: ComponentImpl?

    @Override
    operator fun hasNext(): Boolean {
        return try {
            Caster.toBooleanValue(cfc.call(ThreadLocalPageContext.get(), KeyConstants.__hasNext, EMPTY))
        } catch (pe: PageException) {
            throw PageRuntimeException(pe)
        }
    }

    @Override
    operator fun next(): Object? {
        return try {
            cfc.call(ThreadLocalPageContext.get(), KeyConstants.__next, EMPTY)
        } catch (pe: PageException) {
            throw PageRuntimeException(pe)
        }
    }

    @Override
    fun remove() {
        try {
            cfc.call(ThreadLocalPageContext.get(), KeyConstants.__remove, EMPTY)
        } catch (pe: PageException) {
            throw PageRuntimeException(pe)
        }
    }

    @Override
    fun reset() {
        try {
            cfc.call(ThreadLocalPageContext.get(), KeyConstants.__reset, EMPTY)
        } catch (pe: PageException) {
            throw PageRuntimeException(pe)
        }
    }

    companion object {
        private val EMPTY: Array<Object?>? = arrayOfNulls<Object?>(0)
    }

    init {
        this.cfc = cfc
    }
}