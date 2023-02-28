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
package lucee.runtime.type.dt

import java.math.BigDecimal

/**
 * Printable and Castable Date Object (no visible time)
 */
class DateImpl : Date, SimpleValue {
    constructor(utcTime: Long) : this(null, utcTime) {}
    constructor(pc: PageContext?) : this(pc, System.currentTimeMillis()) {}

    // private TimeZone timezone;
    @JvmOverloads
    constructor(pc: PageContext? = null, utcTime: Long = System.currentTimeMillis()) : super(DateTimeImpl.addOffset(ThreadLocalPageContext.getConfig(pc), utcTime)) {        // this.timezone=ThreadLocalPageContext.getTimeZone(pc);
    }

    constructor(date: java.util.Date?) : super(date.getTime()) {}

    @Override
    fun castToString(): String? {
        synchronized(luceeFormatter) {
            luceeFormatter.setTimeZone(ThreadLocalPageContext.getTimeZone())
            return "{d '" + luceeFormatter.format(this).toString() + "'}"
        }
    }

    @Override
    override fun toString(): String {
        return castToString()!!
    }

    @Override
    fun castToString(defaultValue: String?): String? {
        return castToString()
    }

    @Override
    fun toDoubleValue(): Double {
        return DateTimeUtil.getInstance().toDoubleValue(this)
    }

    @Override
    fun castToDoubleValue(defaultValue: Double): Double {
        return DateTimeUtil.getInstance().toDoubleValue(this)
    }

    @Override
    fun toDumpData(pageContext: PageContext?, maxlevel: Int, dp: DumpProperties?): DumpData? {
        val str = castToString("")
        val table = DumpTable("date", "#ff9900", "#ffcc00", "#000000")
        table.appendRow(1, SimpleDumpData("Date"), SimpleDumpData(str))
        return table
    }

    @Override
    @Throws(PageException::class)
    fun castToBooleanValue(): Boolean {
        return DateTimeUtil.getInstance().toBooleanValue(this)
    }

    @Override
    fun castToBoolean(defaultValue: Boolean?): Boolean? {
        return defaultValue
    }

    @Override
    fun castToDoubleValue(): Double {
        return DateTimeUtil.getInstance().toDoubleValue(this)
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
    operator fun compareTo(b: Boolean): Int {
        return OpUtil.compare(ThreadLocalPageContext.get(), this as java.util.Date, (if (b) BigDecimal.ONE else BigDecimal.ZERO) as Number)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(dt: DateTime?): Int {
        return OpUtil.compare(ThreadLocalPageContext.get(), this as java.util.Date, dt as java.util.Date?)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(d: Double): Int {
        return OpUtil.compare(ThreadLocalPageContext.get(), this as java.util.Date, Double.valueOf(d))
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(str: String?): Int {
        return OpUtil.compare(ThreadLocalPageContext.get(), castToString(), str)
    }

    companion object {
        private val luceeFormatter: SimpleDateFormat? = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    }
}