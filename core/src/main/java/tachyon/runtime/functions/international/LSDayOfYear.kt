/**
 * Copyright (c) 2015, Tachyon Assosication Switzerland. All rights reserved.
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
/**
 * Implements the CFML Function dayofyear
 */
package tachyon.runtime.functions.international

import java.util.Locale

class LSDayOfYear : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size < 1 || args.size > 3) throw FunctionException(pc, "LSDayOfYear", 1, 3, args.size)
        val dt: DateTime = Caster.toDatetime(args[0], pc.getTimeZone())
        if (args.size == 1) return call(pc, dt)
        return if (args.size == 2) call(pc, dt, Caster.toLocale(args[1])) else call(pc, dt, Caster.toLocale(args[1]), Caster.toTimeZone(args[2]))
    }

    companion object {
        private const val serialVersionUID = 8136302798735384757L
        fun call(pc: PageContext?, date: DateTime?): Double {
            return _call(pc, date, null, null)
        }

        fun call(pc: PageContext?, date: DateTime?, locale: Locale?): Double {
            return _call(pc, date, locale, null)
        }

        fun call(pc: PageContext?, date: DateTime?, locale: Locale?, tz: TimeZone?): Double {
            return _call(pc, date, if (locale == null) pc.getLocale() else locale, if (tz == null) pc.getTimeZone() else tz)
        }

        private fun _call(pc: PageContext?, date: DateTime?, l: Locale?, tz: TimeZone?): Double {
            return DateTimeUtil.getInstance().getDayOfYear(l, tz, date)
        }
    }
}