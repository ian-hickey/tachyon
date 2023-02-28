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
package tachyon.commons.lang.types

import java.util.Date

/**
 * Integer Type that can be modified
 */
class RefLongImpl : RefLong, Castable {
    private var value: Long = 0

    /**
     * Constructor of the class
     *
     * @param value
     */
    constructor(value: Long) {
        this.value = value
    }

    /**
     * Constructor of the class
     */
    constructor() {}

    @Override
    fun setValue(value: Long) {
        this.value = value
    }

    @Override
    operator fun plus(value: Long) {
        this.value += value
    }

    @Override
    operator fun minus(value: Long) {
        this.value -= value
    }

    @Override
    fun toLong(): Long {
        return Long.valueOf(value)
    }

    @Override
    fun toLongValue(): Long {
        return value
    }

    @Override
    override fun toString(): String {
        return String.valueOf(value)
    }

    @Override
    fun castToBoolean(defaultValue: Boolean?): Boolean {
        return Caster.toBoolean(value)
    }

    @Override
    fun castToBooleanValue(): Boolean {
        return Caster.toBooleanValue(value)
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
        return toString()
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