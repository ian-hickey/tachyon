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
package tachyon.runtime.type.ref

import tachyon.runtime.PageContext

class SimpleVarRef  // private PageContextImpl pc;
(pc: PageContextImpl?, key: String?) : Reference {
    @Override
    @Throws(PageException::class)
    operator fun get(pc: PageContext?): Object? {
        // TODO Auto-generated method stub
        return null
    }

    @Override
    operator fun get(pc: PageContext?, defaultValue: Object?): Object? {
        // TODO Auto-generated method stub
        return null
    }

    // TODO Auto-generated method stub
    @get:Throws(PageException::class)
    @get:Override
    val key: Collection.Key?
        get() =// TODO Auto-generated method stub
            null

    // TODO Auto-generated method stub
    @get:Throws(PageException::class)
    @get:Override
    val keyAsString: String?
        get() =// TODO Auto-generated method stub
            null

    // TODO Auto-generated method stub
    @get:Override
    val parent: Object?
        get() =// TODO Auto-generated method stub
            null

    @Override
    @Throws(PageException::class)
    fun remove(pc: PageContext?): Object? {
        // TODO Auto-generated method stub
        return null
    }

    @Override
    fun removeEL(pc: PageContext?): Object? {
        // TODO Auto-generated method stub
        return null
    }

    @Override
    @Throws(PageException::class)
    operator fun set(pc: PageContext?, value: Object?): Object? {
        // TODO Auto-generated method stub
        return null
    }

    @Override
    fun setEL(pc: PageContext?, value: Object?): Object? {
        // TODO Auto-generated method stub
        return null
    }

    @Override
    @Throws(PageException::class)
    fun touch(pc: PageContext?): Object? {
        // TODO Auto-generated method stub
        return null
    }

    @Override
    fun touchEL(pc: PageContext?): Object? {
        // TODO Auto-generated method stub
        return null
    }
}