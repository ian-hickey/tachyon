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
package tachyon.runtime.type

import java.util.Date

class CastableArray : ArrayImpl {
    private val value: Object?

    /**
     * Constructor of the class generates as string list of the array
     */
    constructor() {
        value = null
    }

    constructor(value: Object?) {
        this.value = value
    }

    @Override
    override fun duplicate(deepCopy: Boolean): Collection? {
        return duplicate(CastableArray(value), deepCopy)
    }

    @Override
    @Throws(PageException::class)
    fun castToBooleanValue(): Boolean {
        return Caster.toBooleanValue(getValue())
    }

    @Override
    fun castToBoolean(defaultValue: Boolean?): Boolean? {
        return try {
            Caster.toBoolean(getValue(), defaultValue)
        } catch (e: PageException) {
            defaultValue
        }
    }

    @Override
    @Throws(PageException::class)
    fun castToDateTime(): DateTime? {
        return Caster.toDate(getValue(), null)
    }

    @Override
    fun castToDateTime(defaultValue: DateTime?): DateTime? {
        return try {
            DateCaster.toDateAdvanced(getValue(), DateCaster.CONVERTING_TYPE_OFFSET, null, defaultValue)
        } catch (e: PageException) {
            defaultValue
        }
    }

    @Override
    @Throws(PageException::class)
    fun castToDoubleValue(): Double {
        return Caster.toDoubleValue(getValue())
    }

    @Override
    fun castToDoubleValue(defaultValue: Double): Double {
        return try {
            Caster.toDoubleValue(getValue(), true, defaultValue)
        } catch (e: PageException) {
            defaultValue
        }
    }

    @Override
    @Throws(PageException::class)
    fun castToString(): String? {
        return Caster.toString(getValue())
    }

    @Override
    fun castToString(defaultValue: String?): String? {
        return try {
            Caster.toString(getValue(), defaultValue)
        } catch (e: PageException) {
            defaultValue
        }
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(b: Boolean): Int {
        return OpUtil.compare(ThreadLocalPageContext.get(), getValue(), if (b) Boolean.TRUE else Boolean.FALSE)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(dt: DateTime?): Int {
        return OpUtil.compare(ThreadLocalPageContext.get(), getValue(), dt as Date?)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(d: Double): Int {
        return OpUtil.compare(ThreadLocalPageContext.get(), getValue(), Double.valueOf(d))
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(str: String?): Int {
        return OpUtil.compare(ThreadLocalPageContext.get(), getValue(), str)
    }

    @Throws(PageException::class)
    private fun getValue(): Object? {
        return if (value != null) value else ListUtil.arrayToList(this, ",")
    }

    @Override
    override fun toDumpData(pageContext: PageContext?, maxlevel: Int, dp: DumpProperties?): DumpData? {
        val dt: DumpTable? = super.toDumpData(pageContext, maxlevel, dp) as DumpTable?
        dt.setTitle("Castable Array")
        return dt
    }
}