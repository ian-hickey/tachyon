/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Tachyon Assosication Switzerland
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
package tachyon.runtime.functions.international

import java.util.Locale

object LSWeek : Function {
    private const val serialVersionUID = -4184509921145613454L
    fun call(pc: PageContext?, date: DateTime?): Double {
        return _call(pc, date, pc.getLocale(), pc.getTimeZone())
    }

    fun call(pc: PageContext?, date: DateTime?, locale: Locale?): Double {
        return _call(pc, date, if (locale == null) pc.getLocale() else locale, pc.getTimeZone())
    }

    fun call(pc: PageContext?, date: DateTime?, locale: Locale?, tz: TimeZone?): Double {
        return _call(pc, date, if (locale == null) pc.getLocale() else locale, if (tz == null) pc.getTimeZone() else tz)
    }

    private fun _call(pc: PageContext?, date: DateTime?, locale: Locale?, tz: TimeZone?): Double {
        return DateTimeUtil.getInstance().getWeekOfYear(locale, tz, date)
    }
}