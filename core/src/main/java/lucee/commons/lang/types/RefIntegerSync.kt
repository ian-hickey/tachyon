/**
 *
 * Copyright (c) 2016, Lucee Assosication Switzerland
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
class RefIntegerSync : RefInteger, Castable {
    private var value = 0

    /**
     * @param value
     */
    constructor(value: Int) {
        this.value = value
    }

    constructor() {}

    /**
     * @param value
     */
    @Override
    @Synchronized
    fun setValue(value: Int) {
        this.value = value
    }

    /**
     * operation plus
     *
     * @param value
     */
    @Override
    @Synchronized
    operator fun plus(value: Int) {
        this.value += value
    }

    /**
     * operation minus
     *
     * @param value
     */
    @Override
    @Synchronized
    operator fun minus(value: Int) {
        this.value -= value
    }

    /**
     * @return returns value as integer
     */
    @Override
    @Synchronized
    fun toInteger(): Integer {
        return Integer.valueOf(value)
    }

    /**
     * @return returns value as integer
     */
    @Override
    @Synchronized
    fun toDouble(): Double {
        return Double.valueOf(value)
    }

    @Override
    @Synchronized
    fun toDoubleValue(): Double {
        return value.toDouble()
    }

    @Override
    @Synchronized
    fun toInt(): Int {
        return value
    }

    @Override
    @Synchronized
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