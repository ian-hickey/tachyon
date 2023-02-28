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
package lucee.runtime.type

import java.util.Date

/**
 * Simple Value Array, an Array that can't cast to a Simple Value
 */
class SVArray : ArrayImpl, Reference, Cloneable {
    private var position = 1

    /**
     * Constructor of the class
     */
    constructor() : super() {}

    /**
     * Constructor of the class
     *
     * @param objects
     */
    constructor(objects: Array<Object?>?) : super(objects) {}

    /**
     * @return Returns the position.
     */
    fun getPosition(): Int {
        return position
    }

    /**
     * @param position The position to set.
     */
    fun setPosition(position: Int) {
        this.position = position
    }

    @Override
    fun getKey(): Collection.Key? {
        return KeyImpl.init(Caster.toString(position))
    }

    @Override
    fun getKeyAsString(): String? {
        return Caster.toString(position)
    }

    @Override
    @Throws(PageException::class)
    operator fun get(pc: PageContext?): Object? {
        return getE(position)
    }

    @Override
    operator fun get(pc: PageContext?, defaultValue: Object?): Object? {
        return get(position, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun touch(pc: PageContext?): Object? {
        val o: Object? = get(position, null)
        return if (o != null) o else setE(position, StructImpl())
    }

    @Override
    fun touchEL(pc: PageContext?): Object? {
        val o: Object? = get(position, null)
        return if (o != null) o else setEL(position, StructImpl())
    }

    @Override
    @Throws(PageException::class)
    operator fun set(pc: PageContext?, value: Object?): Object? {
        return setE(position, value)
    }

    @Override
    fun setEL(pc: PageContext?, value: Object?): Object? {
        return setEL(position, value)
    }

    @Override
    @Throws(PageException::class)
    fun remove(pc: PageContext?): Object? {
        return removeE(position)
    }

    @Override
    fun removeEL(pc: PageContext?): Object? {
        return removeEL(position)
    }

    @Override
    fun getParent(): Object? {
        return this
    }

    @Override
    @Throws(PageException::class)
    fun castToString(): String? {
        return Caster.toString(getE(position))
    }

    @Override
    fun castToString(defaultValue: String?): String? {
        val value: Object = get(position, null) ?: return defaultValue
        return Caster.toString(value, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun castToBooleanValue(): Boolean {
        return Caster.toBooleanValue(getE(position))
    }

    @Override
    fun castToBoolean(defaultValue: Boolean?): Boolean? {
        val value: Object = get(position, defaultValue) ?: return defaultValue
        return Caster.toBoolean(value, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun castToDoubleValue(): Double {
        return Caster.toDoubleValue(getE(position))
    }

    @Override
    fun castToDoubleValue(defaultValue: Double): Double {
        val value: Object = get(position, null) ?: return defaultValue
        return Caster.toDoubleValue(value, true, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun castToDateTime(): DateTime? {
        return Caster.toDate(getE(position), null)
    }

    @Override
    fun castToDateTime(defaultValue: DateTime?): DateTime? {
        val value: Object = get(position, defaultValue) ?: return defaultValue
        return DateCaster.toDateAdvanced(value, DateCaster.CONVERTING_TYPE_OFFSET, null, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(b: Boolean): Int {
        return OpUtil.compare(ThreadLocalPageContext.get(), if (castToBooleanValue()) Boolean.TRUE else Boolean.FALSE, if (b) Boolean.TRUE else Boolean.FALSE)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(dt: DateTime?): Int {
        return OpUtil.compare(ThreadLocalPageContext.get(), castToDateTime() as Date?, dt as Date?)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(d: Double): Int {
        return OpUtil.compare(ThreadLocalPageContext.get(), Double.valueOf(castToDoubleValue()), Double.valueOf(d))
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(str: String?): Int {
        return OpUtil.compare(ThreadLocalPageContext.get(), castToString(), str)
    }

    @Override
    override fun toDumpData(pageContext: PageContext?, maxlevel: Int, dp: DumpProperties?): DumpData? {
        val table: DumpTable? = super.toDumpData(pageContext, maxlevel, dp) as DumpTable?
        table.setTitle("SV Array")
        return table
    }

    @Override
    fun clone(): Object {
        return duplicate(true)
    }

    @Override
    override fun duplicate(deepCopy: Boolean): Collection? {
        val sva = SVArray()
        duplicate(sva, deepCopy)
        sva.position = position
        return sva
    }
}