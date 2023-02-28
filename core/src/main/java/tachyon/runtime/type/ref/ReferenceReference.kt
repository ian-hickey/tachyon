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
 * Handle a Reference
 */
class ReferenceReference(reference: Reference?, key: Collection.Key?) : Reference {
    private val reference: Reference? = null
    private val key: Collection.Key? = null

    /**
     * @param reference
     * @param key
     */
    constructor(reference: Reference?, key: String?) : this(reference, KeyImpl.init(key)) {}

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
        return pc.getCollection(reference.get(pc), key)
    }

    @Override
    operator fun get(pc: PageContext?, defaultValue: Object?): Object? {
        return pc.getCollection(reference.get(pc, null), key, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    operator fun set(pc: PageContext?, value: Object?): Object? {
        return pc.set(reference.touch(pc), key, value)
    }

    @Override
    fun setEL(pc: PageContext?, value: Object?): Object? {
        return try {
            set(pc, value)
        } catch (e: PageException) {
            null
        }
    }

    @Override
    @Throws(PageException::class)
    fun touch(pc: PageContext?): Object? {
        val parent: Object = reference.touch(pc)
        val o: Object = pc.getCollection(parent, key, null)
        return if (o != null) o else pc.set(parent, key, StructImpl())
    }

    @Override
    fun touchEL(pc: PageContext?): Object? {
        val parent: Object = reference.touchEL(pc)
        val o: Object = pc.getCollection(parent, key, null)
        return if (o != null) o else try {
            pc.set(parent, key, StructImpl())
        } catch (e: PageException) {
            null
        }
    }

    @Override
    @Throws(PageException::class)
    fun remove(pc: PageContext?): Object? {
        return pc.getVariableUtil().remove(reference.get(pc), key)
    }

    @Override
    fun removeEL(pc: PageContext?): Object? {
        return pc.getVariableUtil().removeEL(reference.get(pc, null), key)
    }

    @get:Override
    val parent: Object?
        get() = reference

    @Override
    override fun toString(): String {
        return "java.util.ReferenceReference(reference:$reference;key:$key)"
    }

    init {
        this.reference = reference
        this.key = key
    }
}