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

import java.text.DateFormatSymbols

/**
 * Implements the CFML Function dateformat
 */
class DateTimeFormat : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 1) return call(pc, args[0])
        return if (args.size == 2) call(pc, args[0], Caster.toString(args[1])) else call(pc, args[0], Caster.toString(args[1]), Caster.toTimeZone(args[2]))
    }

    companion object {
        private const val serialVersionUID = 134840879454373440L
        val DEFAULT_MASK: String? = "dd-MMM-yyyy HH:mm:ss" // this is already a SimpleDateFormat mask!
        private val AP: Array<String?>? = arrayOf("A", "P")
        private const val ZERO = 0.toChar()
        private const val ONE = 1.toChar()
        private val ZEROZERO: String? = StringBuilder().append(ZERO).append(ZERO).toString()

        /**
         * @param pc
         * @param object
         * @return Formated Time Object as String
         * @throws ExpressionException
         */
        @Throws(ExpressionException::class)
        fun call(pc: PageContext?, `object`: Object?): String? {
            return invoke(pc, `object`, null, Locale.US, ThreadLocalPageContext.getTimeZone(pc))
        }

        /**
         * @param pc
         * @param object
         * @param mask Characters that show how CFML displays a date:
         * @return Formated Time Object as String
         * @throws ExpressionException
         */
        @Throws(ExpressionException::class)
        fun call(pc: PageContext?, `object`: Object?, mask: String?): String? {
            return invoke(pc, `object`, mask, Locale.US, ThreadLocalPageContext.getTimeZone(pc))
        }

        @Throws(ExpressionException::class)
        fun call(pc: PageContext?, `object`: Object?, mask: String?, tz: TimeZone?): String? {
            return invoke(pc, `object`, mask, Locale.US, if (tz == null) ThreadLocalPageContext.getTimeZone(pc) else tz)
        }

        @Throws(ExpressionException::class)
        operator fun invoke(pc: PageContext?, `object`: Object?, mask: String?, locale: Locale?, tz: TimeZone?): String? {
            return invoke(`object`, mask, locale, tz) // FUTURE remove this method
        }

        @Throws(ExpressionException::class)
        operator fun invoke(`object`: Object?, mask: String?, locale: Locale?, tz: TimeZone?): String? {
            val datetime: DateTime = Caster.toDate(`object`, true, tz, null)
            if (datetime == null) {
                if (`object`.toString().trim().length() === 0) return ""
                throw ExpressionException("Can't convert value [$`object`] to a datetime value")
            }
            return invoke(datetime, mask, locale, tz)
        }

        operator fun invoke(datetime: DateTime?, mask: String?, locale: Locale?, tz: TimeZone?): String? {
            var locale: Locale? = locale
            if (locale == null) locale = Locale.US
            var format: java.text.DateFormat? = null
            if ("short".equalsIgnoreCase(mask)) format = java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.SHORT, java.text.DateFormat.SHORT, locale) else if ("medium".equalsIgnoreCase(mask)) format = java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.MEDIUM, java.text.DateFormat.MEDIUM, locale) else if ("long".equalsIgnoreCase(mask)) format = java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.LONG, java.text.DateFormat.LONG, locale) else if ("full".equalsIgnoreCase(mask)) format = java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.FULL, java.text.DateFormat.FULL, locale) else if ("iso8601".equalsIgnoreCase(mask) || "iso".equalsIgnoreCase(mask)) format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX") else if ("isoms".equalsIgnoreCase(mask) || "isoMillis".equalsIgnoreCase(mask) || "javascript".equalsIgnoreCase(mask)) format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX") else if ("epoch".equalsIgnoreCase(mask)) {
                return String.valueOf(datetime.getTime() / 1000)
            } else if ("epochms".equalsIgnoreCase(mask)) {
                return String.valueOf(datetime.getTime())
            } else {
                val sdf: SimpleDateFormat?
                sdf = SimpleDateFormat(convertMask(mask), locale)
                format = sdf
                if (mask != null && StringUtil.indexOfIgnoreCase(mask, "tt") === -1 && StringUtil.indexOfIgnoreCase(mask, "t") !== -1) {
                    val dfs = DateFormatSymbols(locale)
                    dfs.setAmPmStrings(AP)
                    sdf.setDateFormatSymbols(dfs)
                }
            }
            format.setTimeZone(tz)
            return format.format(datetime)
        }

        fun convertMask(mask: String?): String? {
            var mask = mask
            if (mask == null) return DEFAULT_MASK else if ("iso8601".equalsIgnoreCase(mask) || "iso".equalsIgnoreCase(mask)) return "yyyy-MM-dd'T'HH:mm:ssXXX" else if ("isoms".equalsIgnoreCase(mask) || "isoMillis".equalsIgnoreCase(mask) || "javascript".equalsIgnoreCase(mask)) return "yyyy-MM-dd'T'HH:mm:ss.SSSXXX"
            mask = StringUtil.replace(mask, "''", ZEROZERO, false)
            var inside = false
            val carr: CharArray = mask.toCharArray()
            val sb = StringBuilder()
            var i = 0
            while (i < carr.size) {
                when (carr[i]) {
                    'm' -> if (!inside) {
                        sb.append('M')
                    } else {
                        sb.append(carr[i])
                    }
                    'S' -> if (!inside) {
                        sb.append('s')
                    } else {
                        sb.append(carr[i])
                    }
                    't' -> if (!inside) {
                        sb.append('a')
                    } else {
                        sb.append(carr[i])
                    }
                    'T' -> if (!inside) {
                        sb.append('a')
                    } else {
                        sb.append(carr[i])
                    }
                    'n' -> if (!inside) {
                        sb.append('m')
                    } else {
                        sb.append(carr[i])
                    }
                    'N' -> if (!inside) {
                        sb.append('m')
                    } else {
                        sb.append(carr[i])
                    }
                    'l' -> if (!inside) {
                        sb.append('S')
                    } else {
                        sb.append(carr[i])
                    }
                    'L' -> if (!inside) {
                        sb.append('S')
                    } else {
                        sb.append(carr[i])
                    }
                    'Y' -> if (!inside) {
                        sb.append('y')
                    } else {
                        sb.append(carr[i])
                    }
                    'g' -> if (!inside) {
                        sb.append('G')
                    } else {
                        sb.append(carr[i])
                    }
                    'f' -> if (!inside) {
                        sb.append("'f'")
                    } else {
                        sb.append(carr[i])
                    }
                    'e' -> if (!inside) {
                        sb.append("'e'")
                    } else {
                        sb.append(carr[i])
                    }
                    'G', 'y', 'M', 'W', 'w', 'F', 'E', 'a', 'H', 'h', 'K', 'k', 'x', 'X', 'Z', 'z', 's' ->                // case '.':
                        sb.append(carr[i])
                    'D', 'd' -> {
                        val len: Int = sb.length()
                        // 2 before are D or d
                        if (len > 1 && (sb.charAt(len - 1) === 'd' || sb.charAt(len - 1) === 'D') && (sb.charAt(len - 2) === 'd' || sb.charAt(len - 2) === 'D')) {
                            sb.deleteCharAt(len - 1)
                            sb.deleteCharAt(len - 2)
                            sb.append(ONE).append(ONE).append(ONE)
                            break
                        } else if (len > 0 && sb.charAt(len - 1) === ONE) {
                            sb.append(ONE)
                            break
                        }
                        sb.append(carr[i])
                    }
                    '\'' -> {
                        if (carr.size - 1 > i) {
                            if (carr[i + 1] == '\'') {
                                i++
                                sb.append("''")
                                break
                            }
                        }
                        inside = !inside
                        sb.append(carr[i])
                    }
                    else -> {
                        val c = carr[i]
                        if (!inside && (c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z')) sb.append('\'').append(c).append('\'') else sb.append(c)
                    }
                }
                i++
            }
            var str: String? = StringUtil.replace(sb.toString(), "''", "", false)
            str = StringUtil.replace(str, ZEROZERO, "''", false)
            str = str.replace(ONE, 'E')
            str = y2yyyy(str)
            return str
        }

        fun y2yyyy(str: String?): String? {
            val carr: CharArray = str.toCharArray()
            val sb = StringBuilder()
            var inside = false
            var c: Char
            for (i in carr.indices) {
                c = carr[i]
                if (c == '\'') inside = !inside else if (!inside && c == 'y') {
                    if ((i == 0 || carr[i - 1] != 'y') && (i == carr.size - 1 || carr[i + 1] != 'y')) {
                        sb.append("yyyy")
                        continue
                    }
                }
                sb.append(c)
            }
            return sb.toString()
        }
    }
}