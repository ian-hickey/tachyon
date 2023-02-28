/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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
 * Implements the CFML Function dayofweek
 */
package lucee.runtime.functions.international

import java.util.Locale

class LSDayOfWeek : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 1) return call(pc, Caster.toDatetime(args[0], pc.getTimeZone()))
        return if (args.size == 2) call(pc, Caster.toDatetime(args[0], pc.getTimeZone()), Caster.toLocale(args[1])) else call(pc, Caster.toDatetime(args[0], pc.getTimeZone()), Caster.toLocale(args[1]), Caster.toTimeZone(args[2]))
    }

    companion object {
        private const val serialVersionUID = -9002250869621547151L
        fun call(pc: PageContext?, date: DateTime?): Double {
            return _call(pc, date, pc.getLocale(), pc.getTimeZone())
        }

        fun call(pc: PageContext?, date: DateTime?, locale: Locale?): Double {
            return _call(pc, date, locale, pc.getTimeZone())
        }

        fun call(pc: PageContext?, date: DateTime?, locale: Locale?, tz: TimeZone?): Double {
            return _call(pc, date, if (locale == null) pc.getLocale() else locale, if (tz == null) pc.getTimeZone() else tz)
        }

        private fun _call(pc: PageContext?, date: DateTime?, locale: Locale?, tz: TimeZone?): Double {
            return DateTimeUtil.getInstance().getDayOfWeek(locale, tz, date)
        }
    }
}