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
package tachyon.runtime.type.ref

import java.math.BigDecimal

/**
 * represent a reference to a variable
 */
class VariableReference : Reference {
    private var coll: Collection? = null
    private var key: Collection.Key? = null

    /**
     * constructor of the class
     *
     * @param coll Collection where variable is
     * @param key key to the value inside the collection
     */
    constructor(coll: Collection?, key: String?) {
        this.coll = coll
        this.key = KeyImpl.init(key)
    }

    /**
     * constructor of the class
     *
     * @param coll Collection where variable is
     * @param key key to the value inside the collection
     */
    constructor(coll: Collection?, key: Collection.Key?) {
        this.coll = coll
        this.key = key
    }

    /**
     * constructor of the class
     *
     * @param o Object will be casted to Collection
     * @param key key to the value inside the collection
     * @throws PageException
     */
    constructor(o: Object?, key: String?) : this(Caster.toCollection(o), key) {}

    /**
     * constructor of the class
     *
     * @param o Object will be casted to Collection
     * @param key key to the value inside the collection
     * @throws PageException
     */
    constructor(o: Object?, key: Collection.Key?) : this(Caster.toCollection(o), key) {}

    @Override
    @Throws(PageException::class)
    operator fun get(pc: PageContext?): Object? {
        return get()
    }

    @Throws(PageException::class)
    private fun get(): Object? {
        return if (coll is Query) {
            (coll as Query?).getColumn(key)
        } else coll.get(key)
    }

    @Override
    operator fun get(pc: PageContext?, defaultValue: Object?): Object? {
        return get(defaultValue)
    }

    private operator fun get(defaultValue: Object?): Object? {
        if (coll is Query) {
            val rtn: Object = (coll as Query?).getColumn(key, null)
            return if (rtn != null) rtn else defaultValue
        }
        return coll.get(key, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    operator fun set(pc: PageContext?, value: Object?): Object? {
        return coll.set(key, value)
    }

    @Throws(PageException::class)
    fun set(value: Double) {
        coll.set(key, Caster.toDouble(value))
    }

    @Throws(PageException::class)
    fun set(value: BigDecimal?) {
        coll.set(key, value)
    }

    @Throws(PageException::class)
    fun set(value: Number?) {
        coll.set(key, value)
    }

    @Override
    fun setEL(pc: PageContext?, value: Object?): Object? {
        return coll.setEL(key, value)
    }

    @Override
    @Throws(PageException::class)
    fun touch(pc: PageContext?): Object? {
        val o: Object
        if (coll is Query) {
            o = (coll as Query?).getColumn(key, null)
            return if (o != null) o else set(pc, StructImpl())
        }
        o = coll.get(key, null)
        return if (o != null) o else set(pc, StructImpl())
    }

    @Override
    fun touchEL(pc: PageContext?): Object? {
        val o: Object
        if (coll is Query) {
            o = (coll as Query?).getColumn(key, null)
            return if (o != null) o else setEL(pc, StructImpl())
        }
        o = coll.get(key, null)
        return if (o != null) o else setEL(pc, StructImpl())
    }

    @Override
    @Throws(PageException::class)
    fun remove(pc: PageContext?): Object? {
        return coll.remove(key)
    }

    @Override
    fun removeEL(pc: PageContext?): Object? {
        return coll.removeEL(key)
    }

    @get:Override
    val parent: Object?
        get() = coll

    /**
     * @return return the parent as Collection
     */
    val collection: Collection?
        get() = coll

    @get:Override
    val keyAsString: String?
        get() = key.getString()

    @Override
    fun getKey(): Collection.Key? {
        return key
    }

    @Override
    override fun toString(): String {
        return try {
            Caster.toString(get())
        } catch (e: PageException) {
            super.toString()
        }
    }
}