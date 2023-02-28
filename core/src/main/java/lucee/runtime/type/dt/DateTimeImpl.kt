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
 * Printable and Castable DateTime Object
 */
class DateTimeImpl : DateTime, SimpleValue, Objects {
    constructor(pc: PageContext?) : this(pc, System.currentTimeMillis(), true) {}
    constructor(config: Config?) : this(config, System.currentTimeMillis(), true) {}
    constructor(pc: PageContext?, utcTime: Long, doOffset: Boolean) : super(if (doOffset) addOffset(ThreadLocalPageContext.getConfig(pc), utcTime) else utcTime) {}
    constructor(config: Config?, utcTime: Long, doOffset: Boolean) : super(if (doOffset) addOffset(ThreadLocalPageContext.getConfig(config), utcTime) else utcTime) {}

    @JvmOverloads
    constructor(utcTime: Long = System.currentTimeMillis(), doOffset: Boolean = true) : super(if (doOffset) addOffset(ThreadLocalPageContext.getConfig(), utcTime) else utcTime) {
    }

    /*
	 * public DateTimeImpl(Config config, long utcTime) {
	 * super(addOffset(ThreadLocalPageContext.getConfig(config),utcTime)); }
	 */
    constructor(date: Date?) : this(date.getTime(), false) {}
    constructor(calendar: Calendar?) : super(calendar.getTimeInMillis()) {        // this.timezone=ThreadLocalPageContext.getTimeZone(calendar.getTimeZone());
    }

    @Override
    fun toDumpData(pageContext: PageContext?, maxlevel: Int, dp: DumpProperties?): DumpData? {
        val str: String = castToString(pageContext.getTimeZone())
        val table = DumpTable("date", "#ff6600", "#ffcc99", "#000000")
        if (dp.getMetainfo()) table.appendRow(1, SimpleDumpData("Date Time (" + pageContext.getTimeZone().getID().toString() + ")")) else table.appendRow(1, SimpleDumpData("Date Time"))
        table.appendRow(0, SimpleDumpData(str))
        return table
    }

    @Override
    fun castToString(): String? {
        return castToString(null as TimeZone?)
    }

    @Override
    fun castToString(defaultValue: String?): String? {
        return castToString(null as TimeZone?)
    }

    fun castToString(tz: TimeZone?): String? { // MUST move to DateTimeUtil
        return DateTimeUtil.getInstance().toString(ThreadLocalPageContext.get(), this, tz, null)
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
        return OpUtil.compare(ThreadLocalPageContext.get(), this as Date, if (b) BigDecimal.ONE else BigDecimal.ZERO)
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

    @Override
    override fun toString(): String {
        return castToString()!!
        /*
		 * synchronized (javaFormatter) { javaFormatter.setTimeZone(timezone); return
		 * javaFormatter.format(this); }
		 */
    }

    @Override
    operator fun get(pc: PageContext?, key: Key?, defaultValue: Object?): Object? {
        return Reflector.getField(this, key.getString(), defaultValue)
    }

    @Override
    @Throws(PageException::class)
    operator fun get(pc: PageContext?, key: Key?): Object? {
        return Reflector.getField(this, key.getString())
    }

    @Override
    @Throws(PageException::class)
    operator fun set(pc: PageContext?, propertyName: Key?, value: Object?): Object? {
        return Reflector.setField(this, propertyName.getString(), value)
    }

    @Override
    fun setEL(pc: PageContext?, propertyName: Key?, value: Object?): Object? {
        return try {
            Reflector.setField(this, propertyName.getString(), value)
        } catch (e: PageException) {
            value
        }
    }

    @Override
    @Throws(PageException::class)
    fun call(pc: PageContext?, methodName: Key?, args: Array<Object?>?): Object? {
        return MemberUtil.call(pc, this, methodName, args, shortArrayOf(CFTypes.TYPE_DATETIME), arrayOf<String?>("datetime"))
    }

    @Override
    @Throws(PageException::class)
    fun callWithNamedValues(pc: PageContext?, methodName: Key?, args: Struct?): Object? {
        return MemberUtil.callWithNamedValues(pc, this, methodName, args, CFTypes.TYPE_DATETIME, "datetime")
    }

    companion object {
        fun addOffset(config: Config?, utcTime: Long): Long {
            return if (config != null) utcTime + config.getTimeServerOffset() else utcTime
        }
    }
}