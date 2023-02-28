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
package lucee.runtime.type.cfc

import java.util.Iterator

class ComponentEntryIterator(cfc: Component?, keys: Array<Key?>?, access: Int) : EntryIterator(cfc, keys), Iterator<Entry<Key?, Object?>?> {
    private val cfc: Component?
    private val access: Int

    @Override
    override fun next(): Entry<Key?, Object?>? {
        val key: Key = keys.get(pos++) ?: return null
        return CAEntryImpl(cfc, key, access)
    }

    inner class CAEntryImpl(cfc: Component?, key: Key?, access: Int) : EntryImpl(cfc, key), Entry<Key?, Object?> {
        private val cfc: Component?
        private val access: Int

        @get:Override
        val value: Object?
            get() = cfc.get(access, key, null)

        @Override
        fun setValue(value: Object?): Object? {
            return cfc.setEL(key, value)
        }

        init {
            this.cfc = cfc
            this.access = access
        }
    }

    init {
        this.cfc = cfc
        this.access = access
    }
}