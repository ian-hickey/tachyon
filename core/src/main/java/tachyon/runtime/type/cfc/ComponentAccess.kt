/**
 * Copyright (c) 2015, Tachyon Assosication Switzerland. All rights reserved.
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
package tachyon.runtime.type.cfc

import java.util.Iterator

interface ComponentAccess : Component {
    @get:Override
    val isPersistent: Boolean

    @Override
    fun getMetaStructItem(name: Collection.Key?): Object?

    @Override
    fun keySet(access: Int): Set<Key?>?

    @Override
    @Throws(PageException::class)
    fun call(pc: PageContext?, access: Int, name: Collection.Key?, args: Array<Object?>?): Object?

    @Override
    @Throws(PageException::class)
    fun callWithNamedValues(pc: PageContext?, access: Int, name: Collection.Key?, args: Struct?): Object?

    @Override
    fun size(access: Int): Int

    @Override
    fun keys(access: Int): Array<Collection.Key?>?

    @Override
    fun keyIterator(access: Int): Iterator<Collection.Key?>?

    @Override
    fun keysAsStringIterator(access: Int): Iterator<String?>?

    @Override
    fun entryIterator(access: Int): Iterator<Entry<Key?, Object?>?>?

    @Override
    fun valueIterator(access: Int): Iterator<Object?>?

    @Override
    @Throws(PageException::class)
    operator fun get(access: Int, key: Collection.Key?): Object?

    @Override
    operator fun get(access: Int, key: Collection.Key?, defaultValue: Object?): Object?

    @Override
    fun toDumpData(pageContext: PageContext?, maxlevel: Int, dp: DumpProperties?, access: Int): DumpData?

    @Override
    fun contains(access: Int, name: Key?): Boolean

    @Override
    fun getMember(access: Int, key: Collection.Key?, dataMember: Boolean, superAccess: Boolean): Member?
    fun _base(): ComponentAccess? // TODO do better impl

    // public boolean isRest();
    @get:Override
    @set:Override
    var isEntity: Boolean
}