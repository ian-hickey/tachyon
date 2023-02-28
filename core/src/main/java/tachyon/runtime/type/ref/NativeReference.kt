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

/**
 * represent a reference to an Object
 */
class NativeReference private constructor(o: Object?, key: String?) : Reference {
    private val o: Object?
    private val key: Collection.Key?

    @get:Override
    val parent: Object?
        get() = o

    @Override
    fun getKey(): Collection.Key? {
        return key
    }

    @get:Override
    val keyAsString: String?
        get() = key.getString()

    @Override
    @Throws(PageException::class)
    operator fun get(pc: PageContext?): Object? {
        return pc.getCollection(o, key)
    }

    @Override
    operator fun get(pc: PageContext?, defaultValue: Object?): Object? {
        return pc.getCollection(o, key, null)
    }

    @Override
    @Throws(PageException::class)
    fun touch(pc: PageContext?): Object? {
        val rtn: Object = pc.getCollection(o, key, null)
        return if (rtn != null) rtn else pc.set(o, key, StructImpl())
    }

    @Override
    fun touchEL(pc: PageContext?): Object? {
        val rtn: Object = pc.getCollection(o, key, null)
        return if (rtn != null) rtn else try {
            pc.set(o, key, StructImpl())
        } catch (e: PageException) {
            null
        }
    }

    @Override
    @Throws(PageException::class)
    operator fun set(pc: PageContext?, value: Object?): Object? {
        return pc.set(o, key, value)
    }

    @Override
    fun setEL(pc: PageContext?, value: Object?): Object? {
        return try {
            pc.set(o, key, value)
        } catch (e: PageException) {
            null
        }
    }

    @Override
    @Throws(PageException::class)
    fun remove(pc: PageContext?): Object? {
        return pc.getVariableUtil().remove(o, key)
    }

    @Override
    fun removeEL(pc: PageContext?): Object? {
        return pc.getVariableUtil().removeEL(o, key)
    }

    companion object {
        /**
         * returns a Reference Instance
         *
         * @param o
         * @param key
         * @return Reference Instance
         */
        fun getInstance(o: Object?, key: String?): Reference? {
            if (o is Reference) {
                return ReferenceReference(o as Reference?, key)
            }
            val coll: Collection = Caster.toCollection(o, null)
            return if (coll != null) VariableReference(coll, key) else NativeReference(o, key)
        }
    }

    /**
     * Constructor of the class
     *
     * @param o
     * @param key
     */
    init {
        this.o = o
        this.key = KeyImpl.init(key)
    }
}