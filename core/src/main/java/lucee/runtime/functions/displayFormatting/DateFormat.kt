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
package lucee.runtime.functions.displayFormatting

import java.util.Locale

/**
 * Implements the CFML Function dateformat
 */
class DateFormat : BIF() {
    // public static String call(PageContext pc , Object object, String mask,TimeZone tz) throws
    // PageException {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 1) return call(pc, args[0])
        if (args.size == 2) return call(pc, args[0], Caster.toString(args[1]))
        if (args.size == 3) return call(pc, args[0], Caster.toString(args[1]), Caster.toTimeZone(args[2]))
        throw FunctionException(pc, "DateFormat", 1, 3, args.size)
    }

    companion object {
        @Throws(PageException::class)
        fun call(pc: PageContext?, `object`: Object?): String? {
            return _call(pc, `object`, "dd-mmm-yy", ThreadLocalPageContext.getTimeZone(pc))
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, `object`: Object?, mask: String?): String? {
            return _call(pc, `object`, mask, ThreadLocalPageContext.getTimeZone(pc))
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, `object`: Object?, mask: String?, tz: TimeZone?): String? {
            return _call(pc, `object`, mask, if (tz == null) ThreadLocalPageContext.getTimeZone(pc) else tz)
        }

        @Throws(PageException::class)
        private fun _call(pc: PageContext?, `object`: Object?, mask: String?, tz: TimeZone?): String? {
            val locale: Locale = Locale.US
            val datetime: DateTime = DateCaster.toDateAdvanced(`object`, tz, null)
            // Caster.toDate(object,true,tz,null);
            if (datetime == null) {
                if (StringUtil.isEmpty(`object`, true)) return ""
                throw CasterException(`object`, "datetime")
                // if(!Decision.isSimpleValue(object))
                // throw new ExpressionException("can't convert object of type "+Type.getName(object)+" to a
                // datetime value");
                // throw new ExpressionException("can't convert value "+object+" to a datetime value");
            }
            return DateFormat(locale).format(datetime, mask, tz)
        }
    }
}