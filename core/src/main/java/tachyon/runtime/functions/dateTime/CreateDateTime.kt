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
 * Implements the CFML Function createdatetime
 */
package tachyon.runtime.functions.dateTime

import java.util.TimeZone

class CreateDateTime : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 8) return _call(pc, Caster.toIntValue(args[0]), Caster.toIntValue(args[1]), Caster.toIntValue(args[2]), Caster.toIntValue(args[3]),
                Caster.toIntValue(args[4]), Caster.toIntValue(args[5]), Caster.toIntValue(args[6]), Caster.toTimeZone(args[7]))
        if (args.size == 7) return _call(pc, Caster.toIntValue(args[0]), Caster.toIntValue(args[1]), Caster.toIntValue(args[2]), Caster.toIntValue(args[3]),
                Caster.toIntValue(args[4]), Caster.toIntValue(args[5]), Caster.toIntValue(args[6]), pc.getTimeZone())
        if (args.size == 6) return _call(pc, Caster.toIntValue(args[0]), Caster.toIntValue(args[1]), Caster.toIntValue(args[2]), Caster.toIntValue(args[3]),
                Caster.toIntValue(args[4]), Caster.toIntValue(args[5]), 0, pc.getTimeZone())
        if (args.size == 5) return _call(pc, Caster.toIntValue(args[0]), Caster.toIntValue(args[1]), Caster.toIntValue(args[2]), Caster.toIntValue(args[3]),
                Caster.toIntValue(args[4]), 0, 0, pc.getTimeZone())
        if (args.size == 4) return _call(pc, Caster.toIntValue(args[0]), Caster.toIntValue(args[1]), Caster.toIntValue(args[2]), Caster.toIntValue(args[3]), 0, 0, 0, pc.getTimeZone())
        if (args.size == 3) return _call(pc, Caster.toIntValue(args[0]), Caster.toIntValue(args[1]), Caster.toIntValue(args[2]), 0, 0, 0, 0, pc.getTimeZone())
        if (args.size == 2) return _call(pc, Caster.toIntValue(args[0]), Caster.toIntValue(args[1]), 1, 0, 0, 0, 0, pc.getTimeZone())
        if (args.size == 1) return _call(pc, Caster.toIntValue(args[0]), 1, 1, 0, 0, 0, 0, pc.getTimeZone())
        throw FunctionException(pc, "CreateDateTime", 1, 8, args.size)
    }

    companion object {
        private const val serialVersionUID = 2158994510749730985L
        @Throws(ExpressionException::class)
        fun call(pc: PageContext?, year: Double): DateTime? {
            return _call(pc, year.toInt(), 1, 1, 0, 0, 0, 0, pc.getTimeZone())
        }

        @Throws(ExpressionException::class)
        fun call(pc: PageContext?, year: Double, month: Double): DateTime? {
            return _call(pc, year.toInt(), month.toInt(), 1, 0, 0, 0, 0, pc.getTimeZone())
        }

        @Throws(ExpressionException::class)
        fun call(pc: PageContext?, year: Double, month: Double, day: Double): DateTime? {
            return _call(pc, year.toInt(), month.toInt(), day.toInt(), 0, 0, 0, 0, pc.getTimeZone())
        }

        @Throws(ExpressionException::class)
        fun call(pc: PageContext?, year: Double, month: Double, day: Double, hour: Double): DateTime? {
            return _call(pc, year.toInt(), month.toInt(), day.toInt(), hour.toInt(), 0, 0, 0, pc.getTimeZone())
        }

        @Throws(ExpressionException::class)
        fun call(pc: PageContext?, year: Double, month: Double, day: Double, hour: Double, minute: Double): DateTime? {
            return _call(pc, year.toInt(), month.toInt(), day.toInt(), hour.toInt(), minute.toInt(), 0, 0, pc.getTimeZone())
        }

        @Throws(ExpressionException::class)
        fun call(pc: PageContext?, year: Double, month: Double, day: Double, hour: Double, minute: Double, second: Double): DateTime? {
            return _call(pc, year.toInt(), month.toInt(), day.toInt(), hour.toInt(), minute.toInt(), second.toInt(), 0, pc.getTimeZone())
        }

        @Throws(ExpressionException::class)
        fun call(pc: PageContext?, year: Double, month: Double, day: Double, hour: Double, minute: Double, second: Double, millis: Double): DateTime? {
            return _call(pc, year.toInt(), month.toInt(), day.toInt(), hour.toInt(), minute.toInt(), second.toInt(), millis.toInt(), pc.getTimeZone())
        }

        @Throws(ExpressionException::class)
        fun call(pc: PageContext?, year: Double, month: Double, day: Double, hour: Double, minute: Double, second: Double, millis: Double, tz: TimeZone?): DateTime? {
            return _call(pc, year.toInt(), month.toInt(), day.toInt(), hour.toInt(), minute.toInt(), second.toInt(), millis.toInt(), if (tz == null) pc.getTimeZone() else tz)
        }

        @Throws(ExpressionException::class)
        private fun _call(pc: PageContext?, year: Int, month: Int, day: Int, hour: Int, minute: Int, second: Int, millis: Int, tz: TimeZone?): DateTime? {
            return DateTimeUtil.getInstance().toDateTime(tz, year, month, day, hour, minute, second, millis)
        }
    }
}