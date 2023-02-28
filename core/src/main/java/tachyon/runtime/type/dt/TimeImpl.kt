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
package tachyon.runtime.type.dt

import java.text.SimpleDateFormat

/**
 * Printable and Castable Time Object (at the moment, same as DateTime)
 */
class TimeImpl(pc: PageContext?, utcTime: Long, addOffset: Boolean) : Time(if (addOffset) DateTimeImpl.addOffset(ThreadLocalPageContext.getConfig(pc), utcTime) else utcTime), SimpleValue {
    // private TimeZone timezone;
    constructor(utcTime: Long) : this(null, utcTime, false) {}
    constructor(addOffset: Boolean) : this(null, System.currentTimeMillis(), addOffset) {}
    constructor(utcTime: Long, addOffset: Boolean) : this(null, utcTime, addOffset) {}
    constructor(pc: PageContext?, addOffset: Boolean) : this(pc, System.currentTimeMillis(), addOffset) {}
    constructor(date: java.util.Date?) : this(date.getTime(), false) {}

    @Override
    fun castToString(): String? {
        synchronized(tachyonFormatter) {
            tachyonFormatter.setTimeZone(ThreadLocalPageContext.getTimeZone())
            return "{t '" + tachyonFormatter.format(this).toString() + "'}"
        }
    }

    @Override
    fun castToString(defaultValue: String?): String? {
        synchronized(tachyonFormatter) {
            tachyonFormatter.setTimeZone(ThreadLocalPageContext.getTimeZone())
            return "{t '" + tachyonFormatter.format(this).toString() + "'}"
        }
    }

    @Override
    fun toDumpData(pageContext: PageContext?, maxlevel: Int, dp: DumpProperties?): DumpData? {
        val str = castToString("")
        val table = DumpTable("date", "#ff9900", "#ffcc00", "#000000")
        table.appendRow(1, SimpleDumpData("Time"), SimpleDumpData(str))
        return table
    }

    @Override
    @Throws(ExpressionException::class)
    fun castToBooleanValue(): Boolean {
        return DateTimeUtil.getInstance().toBooleanValue(this)
    }

    @Override
    fun castToBoolean(defaultValue: Boolean?): Boolean? {
        return defaultValue
    }

    @Override
    fun castToDoubleValue(): Double {
        return toDoubleValue()
    }

    @Override
    fun castToDoubleValue(defaultValue: Double): Double {
        return toDoubleValue()
    }

    @Override
    fun castToDateTime(): DateTime? {
        return this
    }

    @Override
    fun castToDateTime(defaultValue: DateTime?): DateTime? {
        return this
    }

    @Override
    fun toDoubleValue(): Double {
        return DateTimeUtil.getInstance().toDoubleValue(this)
    }

    @Override
    operator fun compareTo(b: Boolean): Int {
        return OpUtil.compare(ThreadLocalPageContext.get(), Double.valueOf(castToDoubleValue()), if (b) Boolean.TRUE else Boolean.FALSE)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(dt: DateTime?): Int {
        return OpUtil.compare(ThreadLocalPageContext.get(), this as java.util.Date, dt as java.util.Date?)
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

    companion object {
        private val tachyonFormatter: SimpleDateFormat? = SimpleDateFormat("HH:mm:ss", Locale.US)
    }
}