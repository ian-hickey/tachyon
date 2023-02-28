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
 * Implements the CFML Function datediff
 */
package lucee.runtime.functions.dateTime

import java.util.Calendar

/**
 *
 */
class DateDiff : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 3) return call(pc, Caster.toString(args[0]), Caster.toDateTime(args[1], null), Caster.toDateTime(args[2], null))
        throw FunctionException(pc, "DateDiff", 3, 3, args.size)
    }

    companion object {
        private const val serialVersionUID = 4243793930337910884L

        // private static final int DATEPART_S = 0;
        // private static final int DATEPART_N = 1;
        // private static final int DATEPART_H = 2;
        private const val DATEPART_D = 3
        private const val DATEPART_Y = DATEPART_D
        private const val DATEPART_YYYY = 10
        private const val DATEPART_M = 11
        private const val DATEPART_WW = 12
        private const val DATEPART_Q = 20
        private const val DATEPART_WD = 21

        /**
         * @param pc
         * @param s
         * @param date
         * @param date1
         * @return
         * @throws ExpressionException
         */
        @Throws(ExpressionException::class)
        fun call(pc: PageContext?, datePart: String?, left: DateTime?, right: DateTime?): Double {
            var datePart = datePart
            val msLeft: Long = left.getTime()
            val msRight: Long = right.getTime()
            val tz: TimeZone = ThreadLocalPageContext.getTimeZone(pc)
            // Date Part
            datePart = datePart.toLowerCase().trim()
            val dp: Int
            dp = if ("l".equals(datePart)) return diffMilliSeconds(msLeft, msRight).toDouble() else if ("s".equals(datePart)) return diffSeconds(msLeft, msRight).toDouble() else if ("n".equals(datePart)) return (diffSeconds(msLeft, msRight) / 60L).toDouble() else if ("h".equals(datePart)) return (diffSeconds(msLeft, msRight) / 3600L).toDouble() else if ("d".equals(datePart)) DATEPART_D else if ("y".equals(datePart)) DATEPART_Y else if ("yyyy".equals(datePart)) DATEPART_YYYY else if ("m".equals(datePart)) DATEPART_M else if ("w".equals(datePart)) {
                // if debug enabled we are warning about using this
                DebuggerImpl.deprecated(pc, "DateDiff.DatePart",
                        "With the function DateDiff the argument [datePart] changed its meaning in other CFML Engines from [weeks] to [weekdays]. "
                                + "Lucee did not follow this change so far to avoid breaking existing code. "
                                + "Please change your code to [dateDiff(\"wd\",...)] in case you want to have weekdays and to [dateDiff(\"ww\",...)] in case you want to have weeks, to futureproof your code.")
                DATEPART_WW // weeks
            } else if ("ww".equals(datePart)) DATEPART_WW // weeks
            else if ("wd".equals(datePart)) DATEPART_WD // weekdays
            else if ("q".equals(datePart)) DATEPART_Q else throw FunctionException(pc, "dateDiff", 3, "datePart", "invalid value [$datePart], valid values has to be [l,q,s,n,h,d,m,y,yyyy,w,ww]")

            // dates
            val _cLeft: Calendar = JREDateTimeUtil.getThreadCalendar(tz)
            _cLeft.setTimeInMillis(msLeft)
            val _cRight: Calendar = JREDateTimeUtil.newInstance(tz, Locale.US)
            _cRight.setTimeInMillis(msRight)
            return if (msLeft > msRight) (-_call(pc, dp, _cRight, msRight, _cLeft, msLeft)).toDouble() else _call(pc, dp, _cLeft, msLeft, _cRight, msRight).toDouble()
            // }
        }

        fun diffSeconds(msLeft: Long, msRight: Long): Long {
            return if (msLeft > msRight) (-((msLeft - msRight) / 1000.0)).toLong() else ((msRight - msLeft) / 1000.0).toLong()
        }

        fun diffMilliSeconds(msLeft: Long, msRight: Long): Long {
            return if (msLeft > msRight) -(msLeft - msRight) else msRight - msLeft
        }

        @Throws(ExpressionException::class)
        private fun _call(pc: PageContext?, datepart: Int, cLeft: Calendar?, msLeft: Long, cRight: Calendar?, msRight: Long): Long {
            var dDiff: Long = cRight.get(Calendar.DATE) - cLeft.get(Calendar.DATE)
            val hDiff: Long = cRight.get(Calendar.HOUR_OF_DAY) - cLeft.get(Calendar.HOUR_OF_DAY)
            val nDiff: Long = cRight.get(Calendar.MINUTE) - cLeft.get(Calendar.MINUTE)
            val sDiff: Long = cRight.get(Calendar.SECOND) - cLeft.get(Calendar.SECOND)
            if (DATEPART_WD == datepart) {
                return getWorkingDaysDiff(pc, cLeft, cRight, msLeft, msRight)
            }
            if (DATEPART_D == datepart || DATEPART_WW == datepart) {
                var tmp = 0
                if (hDiff < 0) tmp = -1 else if (hDiff > 0) {
                } else if (nDiff < 0) tmp = -1 else if (nDiff > 0) {
                } else if (sDiff < 0) tmp = -1 else if (sDiff > 0) {
                }
                var rst = dayDiff(cLeft, cRight) + tmp
                if (DATEPART_WW == datepart) rst /= 7
                return rst
            }
            val yDiff: Long = cRight.get(Calendar.YEAR) - cLeft.get(Calendar.YEAR)
            val mDiff: Long = cRight.get(Calendar.MONTH) - cLeft.get(Calendar.MONTH)
            if (DATEPART_YYYY == datepart) {
                var tmp = 0
                if (mDiff < 0) tmp = -1 else if (mDiff > 0) {
                } else if (dDiff < 0) tmp = -1 else if (dDiff > 0) {
                } else if (hDiff < 0) tmp = -1 else if (hDiff > 0) {
                } else if (nDiff < 0) tmp = -1 else if (nDiff > 0) {
                } else if (sDiff < 0) tmp = -1 else if (sDiff > 0) {
                }
                return yDiff + tmp
            }
            if (DATEPART_M == datepart || DATEPART_Q == datepart) {
                var tmp = 0
                if (dDiff < 0 && isEndOfMonth(cRight)) dDiff = 0
                if (dDiff < 0) tmp = -1 else if (dDiff > 0) {
                } else if (hDiff < 0) tmp = -1 else if (hDiff > 0) {
                } else if (nDiff < 0) tmp = -1 else if (nDiff > 0) {
                } else if (sDiff < 0) tmp = -1 else if (sDiff > 0) {
                }
                var rst = mDiff + yDiff * 12 + tmp
                if (DATEPART_Q == datepart) rst /= 3
                return rst
            }
            if (DATEPART_D == datepart) {
                return dDiff
            }
            throw FunctionException(pc, "dateDiff", 3, "datePart", "invalid value, valid values has to be [q,s,n,h,d,m,y,yyyy,wd,ww]")
        }

        private fun isEndOfMonth(cal: Calendar?): Boolean {
            return cal.get(Calendar.DATE) === cal.getActualMaximum(Calendar.DATE)
        }

        private fun dayDiff(l: Calendar?, r: Calendar?): Long {
            val lYear: Int = l.get(Calendar.YEAR)
            val rYear: Int = r.get(Calendar.YEAR)
            val lDayOfYear: Int = l.get(Calendar.DAY_OF_YEAR)
            val rDayOfYear: Int = r.get(Calendar.DAY_OF_YEAR)

            // same year
            if (lYear == rYear) {
                return (rDayOfYear - lDayOfYear).toLong()
            }
            var diff = rDayOfYear.toLong()
            diff -= lDayOfYear.toLong()
            for (year in lYear until rYear) {
                diff += if (Decision.isLeapYear(year)) 366L else 365L
            }
            return diff
        }

        @Throws(ExpressionException::class)
        private fun getWorkingDaysDiff(pc: PageContext?, cLeft: Calendar?, cRight: Calendar?, msLeft: Long, msRight: Long): Long {
            val l: Calendar = cLeft.clone() as Calendar
            val r: Calendar = cRight.clone() as Calendar
            l.setFirstDayOfWeek(1)
            r.setFirstDayOfWeek(1)
            var ldw: Int = l.get(Calendar.DAY_OF_WEEK)
            var rdw: Int = r.get(Calendar.DAY_OF_WEEK)
            if (ldw == 1) {
                ldw = 6
                l.add(Calendar.DAY_OF_MONTH, -2)
            } else if (ldw == 7) {
                ldw = 6
                l.add(Calendar.DAY_OF_MONTH, -1)
            }
            if (rdw == 1) {
                rdw = 6
                r.add(Calendar.DAY_OF_MONTH, -2)
            } else if (rdw == 7) {
                rdw = 6
                r.add(Calendar.DAY_OF_MONTH, -1)
            }
            val loff = ldw - 2
            val roff = rdw - 2
            l.add(Calendar.DAY_OF_MONTH, -loff)
            r.add(Calendar.DAY_OF_MONTH, -roff)
            val days = _call(pc, DATEPART_D, l, msLeft, r, msRight)
            val weeks = _call(pc, DATEPART_WW, l, msLeft, r, msRight)
            return days - 2L * weeks + roff - loff
        }
    }
}