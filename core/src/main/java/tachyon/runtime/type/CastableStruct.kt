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

class CastableStruct : StructImpl {
    private var value: Object? = null

    constructor() {}
    constructor(type: Int) : super(type) {}
    constructor(value: Object?) {
        this.value = value
    }

    constructor(value: Object?, type: Int) : super(type) {
        this.value = value
    }

    /**
     * @return the value
     */
    fun getValue(): Object? {
        return value
    }

    /**
     * @param value the value to set
     */
    fun setValue(value: Object?) {
        this.value = value
    }

    @Override
    @Throws(PageException::class)
    fun castToBooleanValue(): Boolean {
        return if (value == null) super.castToBooleanValue() else Caster.toBooleanValue(value)
    }

    @Override
    fun castToBoolean(defaultValue: Boolean?): Boolean? {
        return if (value == null) super.castToBoolean(defaultValue) else Caster.toBoolean(value, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun castToDateTime(): DateTime? {
        return if (value == null) super.castToDateTime() else Caster.toDate(value, null)
    }

    @Override
    fun castToDateTime(defaultValue: DateTime?): DateTime? {
        return if (value == null) super.castToDateTime(defaultValue) else DateCaster.toDateAdvanced(value, DateCaster.CONVERTING_TYPE_OFFSET, null, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun castToDoubleValue(): Double {
        return if (value == null) super.castToDoubleValue() else Caster.toDoubleValue(value)
    }

    @Override
    fun castToDoubleValue(defaultValue: Double): Double {
        return if (value == null) super.castToDoubleValue(defaultValue) else Caster.toDoubleValue(value, true, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun castToString(): String? {
        return if (value == null) super.castToString() else Caster.toString(value)
    }

    @Override
    fun castToString(defaultValue: String?): String? {
        return if (value == null) super.castToString(defaultValue) else Caster.toString(value, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(b: Boolean): Int {
        return if (value == null) super.compareTo(b) else OpUtil.compare(ThreadLocalPageContext.get(), value, if (b) Boolean.TRUE else Boolean.FALSE)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(dt: DateTime?): Int {
        return if (value == null) super.compareTo(dt) else OpUtil.compare(ThreadLocalPageContext.get(), value, dt as Date?)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(d: Double): Int {
        return if (value == null) super.compareTo(d) else OpUtil.compare(ThreadLocalPageContext.get(), value, Double.valueOf(d))
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(str: String?): Int {
        return if (value == null) super.compareTo(str) else OpUtil.compare(ThreadLocalPageContext.get(), value, str)
    }

    @Override
    override fun duplicate(deepCopy: Boolean): Collection? {
        if (value == null) return super.duplicate(deepCopy)
        val sct: Struct = CastableStruct(if (deepCopy) Duplicator.duplicate(value, deepCopy) else value)
        copy(this, sct, deepCopy)
        return sct
    }

    @Override
    override fun toDumpData(pageContext: PageContext?, maxlevel: Int, dp: DumpProperties?): DumpData? {
        var maxlevel = maxlevel
        if (value == null) return super.toDumpData(pageContext, maxlevel, dp)
        val table = DumpTable("struct", "#9999ff", "#ccccff", "#000000")
        table.setTitle("Value Struct")
        maxlevel--
        table.appendRow(1, SimpleDumpData("value"), DumpUtil.toDumpData(value, pageContext, maxlevel, dp))
        table.appendRow(1, SimpleDumpData("struct"), super.toDumpData(pageContext, maxlevel, dp))
        return table
    }
}