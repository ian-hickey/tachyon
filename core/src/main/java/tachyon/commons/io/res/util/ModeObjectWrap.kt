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
package tachyon.commons.io.res.util

import tachyon.commons.io.ModeUtil

class ModeObjectWrap(res: Resource) : ObjectWrap, Castable {
    private val res: Resource
    private var mode: String? = null

    @get:Override
    val embededObject: Object
        get() = toString()

    @Override
    fun getEmbededObject(def: Object?): Object {
        return toString()
    }

    @Override
    override fun toString(): String {
        // print.dumpStack();
        if (mode == null) mode = ModeUtil.toStringMode(res.getMode())
        return mode!!
    }

    fun castString(): String {
        return toString()
    }

    @Override
    @Throws(PageException::class)
    fun castToBooleanValue(): Boolean {
        return Caster.toBooleanValue(toString())
    }

    @Override
    fun castToBoolean(defaultValue: Boolean?): Boolean {
        return Caster.toBoolean(toString(), defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun castToDateTime(): DateTime {
        return Caster.toDatetime(toString(), null)
    }

    @Override
    fun castToDateTime(defaultValue: DateTime?): DateTime {
        return DateCaster.toDateAdvanced(toString(), DateCaster.CONVERTING_TYPE_OFFSET, null, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun castToDoubleValue(): Double {
        return Caster.toDoubleValue(toString())
    }

    @Override
    fun castToDoubleValue(defaultValue: Double): Double {
        return Caster.toDoubleValue(toString(), defaultValue)
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
    operator fun compareTo(str: String?): Int {
        return OpUtil.compare(ThreadLocalPageContext.get(), toString(), str)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(b: Boolean): Int {
        return OpUtil.compare(ThreadLocalPageContext.get(), if (castToBooleanValue()) Boolean.TRUE else Boolean.FALSE, if (b) Boolean.TRUE else Boolean.FALSE)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(d: Double): Int {
        return OpUtil.compare(ThreadLocalPageContext.get(), Double.valueOf(castToDoubleValue()), Double.valueOf(d))
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(dt: DateTime): Int {
        return OpUtil.compare(ThreadLocalPageContext.get(), toString(), dt.castToString())
    }

    companion object {
        private const val serialVersionUID = -1630745501422006978L
    }

    init {
        this.res = res
    }
}