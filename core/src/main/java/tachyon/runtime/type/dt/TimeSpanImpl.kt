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

import java.io.Serializable

/**
 * TimeSpan Object, represent a timespan
 */
class TimeSpanImpl : TimeSpan, Serializable {
    private var value: Double

    @get:Override
    var millis: Long
        private set
    var dayAsLong: Long
        private set

    @get:Override
    var hour: Int
        private set

    @get:Override
    var minute: Int
        private set

    @get:Override
    var second: Int
        private set
    private var milli = 0

    private constructor(valueDays: Double) : this((valueDays * 86400000.0).toLong()) {}
    private constructor(valueMillis: Long) {
        millis = valueMillis
        value = valueMillis / 86400000.0
        var tmp = valueMillis
        dayAsLong = valueMillis / 86400000L
        tmp -= dayAsLong * 86400000L
        hour = (tmp / 3600000L).toInt()
        tmp -= hour * 3600000L
        minute = (tmp / 60000L).toInt()
        tmp -= minute * 60000L
        second = (tmp / 1000L).toInt()
        tmp -= second * 1000L
        milli = tmp.toInt()
    }

    /**
     * constructor of the timespan class
     *
     * @param day
     * @param hour
     * @param minute
     * @param second
     */
    constructor(day: Int, hour: Int, minute: Int, second: Int) {
        dayAsLong = day.toLong()
        this.hour = hour
        this.minute = minute
        this.second = second
        value = day + hour.toDouble() / 24 + minute.toDouble() / 24 / 60 + second.toDouble() / 24 / 60 / 60
        millis = (second + minute * 60L + hour * 3600L + day * 3600L * 24L) * 1000
    }

    /**
     * constructor of the timespan class
     *
     * @param day
     * @param hour
     * @param minute
     * @param second
     */
    constructor(day: Int, hour: Int, minute: Int, second: Int, millisecond: Int) {
        dayAsLong = day.toLong()
        this.hour = hour
        this.minute = minute
        this.second = second
        milli = millisecond
        value = day + hour.toDouble() / 24 + minute.toDouble() / 24 / 60 + second.toDouble() / 24 / 60 / 60 + millisecond.toDouble() / 24 / 60 / 60 / 1000
        millis = (second + minute * 60L + hour * 3600L + day * 3600L * 24L) * 1000 + millisecond
    }

    @Override
    fun castToString(): String? {
        return Caster.toString(value)
    }

    @Override
    fun castToString(defaultValue: String?): String? {
        return Caster.toString(value)
    }

    @Override
    fun castToBooleanValue(): Boolean {
        return value != 0.0
    }

    @Override
    fun castToBoolean(defaultValue: Boolean?): Boolean? {
        return value != 0.0
    }

    @Override
    fun castToDoubleValue(): Double {
        return value
    }

    @Override
    fun castToDoubleValue(defaultValue: Double): Double {
        return value
    }

    @Override
    @Throws(ExpressionException::class)
    fun castToDateTime(): DateTime? {
        return DateCaster.toDateSimple(value, null)
    }

    @Override
    fun castToDateTime(defaultValue: DateTime?): DateTime? {
        return DateCaster.toDateSimple(value, null)
    }

    @Override
    operator fun compareTo(b: Boolean): Int {
        return OpUtil.compare(ThreadLocalPageContext.get(), if (castToBooleanValue()) Boolean.TRUE else Boolean.FALSE, if (b) Boolean.TRUE else Boolean.FALSE)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(dt: DateTime?): Int {
        return OpUtil.compare(ThreadLocalPageContext.get(), castToDateTime() as java.util.Date?, dt as java.util.Date?)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(d: Double): Int {
        return OpUtil.compare(ThreadLocalPageContext.get(), Double.valueOf(value), Double.valueOf(d))
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(str: String?): Int {
        return OpUtil.compare(ThreadLocalPageContext.get(), Double.valueOf(value), str)
    }

    @Override
    fun toDumpData(pageContext: PageContext?, maxlevel: Int, dp: DumpProperties?): DumpData? {
        val table = DumpTable("timespan", "#ff9900", "#ffcc00", "#000000")
        if (milli > 0) table.appendRow(1, SimpleDumpData("Timespan"), SimpleDumpData("createTimeSpan(" + dayAsLong + "," + hour + "," + minute + "," + second + "," + milli + ")")) else table.appendRow(1, SimpleDumpData("Timespan"), SimpleDumpData("createTimeSpan(" + dayAsLong + "," + hour + "," + minute + "," + second + ")"))
        return table
    }

    fun getMilli(): Long {
        return milli.toLong()
    }

    @get:Override
    val seconds: Long
        get() = millis / 1000

    @Override
    override fun toString(): String {
        return if (milli > 0) "createTimeSpan(" + dayAsLong + "," + hour + "," + minute + "," + second + "," + milli + ")" else "createTimeSpan(" + dayAsLong + "," + hour + "," + minute + "," + second + ")"
    }

    @Override
    fun getDay(): Int {
        return if (Integer.MAX_VALUE > dayAsLong) dayAsLong.toInt() else Integer.MAX_VALUE
    }

    @Override
    override fun equals(obj: Object?): Boolean {
        if (this === obj) return true
        return if (obj !is TimeSpan) false else millis == (obj as TimeSpan?).getMillis()
    }

    companion object {
        fun fromDays(value: Double): TimeSpan? {
            return TimeSpanImpl(value)
        }

        fun fromMillis(value: Long): TimeSpan? {
            return TimeSpanImpl(value)
        }
    }
}