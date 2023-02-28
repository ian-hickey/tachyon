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
 * Implements the CFML Function lsisdate
 */
package lucee.runtime.functions.international

import java.util.Date

object LSIsDate : Function {
    private const val serialVersionUID = -8517171925554806088L
    fun call(pc: PageContext?, `object`: Object?): Boolean {
        return _call(pc, `object`, pc.getLocale(), pc.getTimeZone())
    }

    fun call(pc: PageContext?, `object`: Object?, locale: Locale?): Boolean {
        return _call(pc, `object`, locale, pc.getTimeZone())
    }

    fun call(pc: PageContext?, `object`: Object?, locale: Locale?, tz: TimeZone?): Boolean {
        return _call(pc, `object`, if (locale == null) pc.getLocale() else locale, if (tz == null) pc.getTimeZone() else tz)
    }

    private fun _call(pc: PageContext?, `object`: Object?, locale: Locale?, tz: TimeZone?): Boolean {
        if (`object` is Date) return true else if (`object` is String) {
            val str: String = `object`.toString()
            return if (str.length() < 2) false else Decision.isDate(str, locale, tz, locale.equals(Locale.US))
            // print.out(Caster.toDateTime(locale,str,pc.getTimeZone(),null));
            // return Caster.toDateTime(locale,str,pc.getTimeZone(),null)!=null;
        }
        return false
    }
}