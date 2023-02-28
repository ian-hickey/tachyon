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
package lucee.commons.lang.types

import java.util.Date

/**
 * Integer Type that can be modified
 */
class RefBooleanImpl : RefBoolean, Castable {
    // MUST add interface Castable
    private var value = false

    constructor() {}

    /**
     * @param value
     */
    constructor(value: Boolean) {
        this.value = value
    }

    /**
     * @param value
     */
    @Override
    fun setValue(value: Boolean) {
        this.value = value
    }

    /**
     * @return returns value as Boolean Object
     */
    @Override
    fun toBoolean(): Boolean {
        return if (value) Boolean.TRUE else Boolean.FALSE
    }

    /**
     * @return returns value as boolean value
     */
    @Override
    fun toBooleanValue(): Boolean {
        return value
    }

    @Override
    override fun toString(): String {
        return if (value) "true" else "false"
    }

    @Override
    fun castToBoolean(arg0: Boolean?): Boolean {
        return toBoolean()
    }

    @Override
    fun castToBooleanValue(): Boolean {
        return toBooleanValue()
    }

    @Override
    @Throws(PageException::class)
    fun castToDateTime(): DateTime {
        return Caster.toDatetime(value, null)
    }

    @Override
    fun castToDateTime(defaultValue: DateTime?): DateTime {
        return Caster.toDate(value, false, null, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun castToDoubleValue(): Double {
        return Caster.toDoubleValue(value)
    }

    @Override
    fun castToDoubleValue(defaultValue: Double): Double {
        return Caster.toDoubleValue(value)
    }

    @Override
    @Throws(PageException::class)
    fun castToString(): String {
        return Caster.toString(value)
    }

    @Override
    fun castToString(defaultValue: String?): String {
        return toString()
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(other: String?): Int {
        return OpUtil.compare(ThreadLocalPageContext.get(), castToString(), other)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(other: Boolean): Int {
        return OpUtil.compare(ThreadLocalPageContext.get(), if (castToBooleanValue()) Boolean.TRUE else Boolean.FALSE, if (other) Boolean.TRUE else Boolean.FALSE)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(other: Double): Int {
        return OpUtil.compare(ThreadLocalPageContext.get(), Double.valueOf(castToDoubleValue()), Double.valueOf(other))
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(other: DateTime?): Int {
        return OpUtil.compare(ThreadLocalPageContext.get(), castToDateTime() as Date, other as Date?)
    }
}