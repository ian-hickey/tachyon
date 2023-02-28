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
/**
 * Implements the CFML Function lsdateformat
 */
package tachyon.runtime.functions.international

import java.util.Locale

class LSDateFormat : BIF(), Function {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        return if (args!!.size == 1) {
            call(pc, args[0])
        } else if (args.size == 2) {
            call(pc, args[0], Caster.toString(args[1]))
        } else if (args.size == 3) {
            call(pc, args[0], Caster.toString(args[1]), if (args[2] == null) null else Caster.toLocale(args[2]))
        } else {
            call(pc, args[0], Caster.toString(args[1]), if (args[2] == null) null else Caster.toLocale(args[2]), if (args[3] == null) null else Caster.toTimeZone(args[3]))
        }
    }

    companion object {
        private const val serialVersionUID = 4720003854756942610L
        @Throws(PageException::class)
        fun call(pc: PageContext?, `object`: Object?): String? {
            return _call(pc, `object`, "medium", pc.getLocale(), pc.getTimeZone())
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, `object`: Object?, mask: String?): String? {
            return _call(pc, `object`, mask, pc.getLocale(), pc.getTimeZone())
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, `object`: Object?, mask: String?, locale: Locale?): String? {
            return _call(pc, `object`, mask, locale, pc.getTimeZone())
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, `object`: Object?, mask: String?, locale: Locale?, tz: TimeZone?): String? {
            return _call(pc, `object`, mask, if (locale == null) pc.getLocale() else locale, if (tz == null) pc.getTimeZone() else tz)
        }

        @Throws(PageException::class)
        private fun _call(pc: PageContext?, `object`: Object?, mask: String?, locale: Locale?, tz: TimeZone?): String? {
            return if (StringUtil.isEmpty(`object`)) "" else DateFormat(locale).format(toDateLS(pc, locale, tz, `object`), mask, tz)
        }

        @Throws(PageException::class)
        private fun toDateLS(pc: PageContext?, locale: Locale?, timeZone: TimeZone?, `object`: Object?): DateTime? {
            if (`object` is DateTime) return `object` as DateTime? else if (`object` is CharSequence) {
                val res: DateTime = DateCaster.toDateTime(locale, Caster.toString(`object`), timeZone, null, locale.equals(Locale.US))
                if (res != null) return res
            }
            return DateCaster.toDateAdvanced(`object`, timeZone)
        }
    }
}