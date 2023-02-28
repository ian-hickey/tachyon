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
package tachyon.runtime.functions.string

import java.util.Locale

/**
 * Implements the CFML Function parsedatetime
 */
class ParseDateTime : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 3) return call(pc, args[0], Caster.toString(args[1]), Caster.toTimeZone(args[2]))
        if (args.size == 2) return call(pc, args[0], Caster.toString(args[1]))
        if (args.size == 1) return call(pc, args[0])
        throw FunctionException(pc, "ParseDateTime", 1, 3, args.size)
    }

    companion object {
        private const val serialVersionUID = -2623323893206022437L
        @Throws(PageException::class)
        fun call(pc: PageContext?, oDate: Object?): tachyon.runtime.type.dt.DateTime? {
            return _call(pc, oDate, null, pc.getTimeZone())
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, oDate: Object?, popConversion: String?): tachyon.runtime.type.dt.DateTime? {
            return _call(pc, oDate, popConversion, pc.getTimeZone())
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, oDate: Object?, popConversion: String?, tz: TimeZone?): tachyon.runtime.type.dt.DateTime? {
            return _call(pc, oDate, popConversion, if (tz == null) pc.getTimeZone() else tz)
        }

        @Throws(PageException::class)
        private fun _call(pc: PageContext?, oDate: Object?, popConversion: String?, tz: TimeZone?): tachyon.runtime.type.dt.DateTime? {
            var popConversion = popConversion
            if (!StringUtil.isEmpty(popConversion) && !"standard".equalsIgnoreCase(popConversion.trim().also { popConversion = it }) && !"pop".equalsIgnoreCase(popConversion.trim())) {
                popConversion = DateTimeFormat.convertMask(popConversion)
                return LSParseDateTime.call(pc, oDate, Locale.US, tz.getID(), popConversion)
            }
            return DateCaster.toDateAdvanced(oDate, DateCaster.CONVERTING_TYPE_YEAR, tz)
        }
    }
}