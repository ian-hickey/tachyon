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
// TODO Time constructor muss auch noch entfernt werden und durch DateUtil methode ersetzen
package tachyon.runtime.op.date

import java.text.DateFormat

/**
 * Class to cast Strings to Date Objects
 */
object DateCaster {
    const val CONVERTING_TYPE_NONE: Short = 0
    const val CONVERTING_TYPE_YEAR: Short = 1
    const val CONVERTING_TYPE_OFFSET: Short = 2

    // private static short MODE_DAY_STR=1;
    // private static short MODE_MONTH_STR=2;
    // private static short MODE_NONE=4;
    private const val DEFAULT_VALUE = Long.MIN_VALUE
    private val util: DateTimeUtil = DateTimeUtil.getInstance()
    var classicStyle = false

    /**
     * converts an Object to a DateTime Object (Advanced but slower)
     *
     * @param o Object to Convert
     * @param timezone
     * @return Date Time Object
     * @throws PageException
     */
    @Throws(PageException::class)
    fun toDateAdvanced(o: Object, timezone: TimeZone?): DateTime {
        if (o is Date) {
            return if (o is DateTime) o as DateTime else DateTimeImpl(o as Date)
        } else if (o is Castable) return (o as Castable).castToDateTime() else if (o is String) {
            return toDateAdvanced(o as String, timezone, null)
                    ?: throw ExpressionException("can't cast [$o] to date value")
        } else if (o is Number) return util.toDateTime((o as Number).doubleValue()) else if (o is ObjectWrap) return toDateAdvanced((o as ObjectWrap).getEmbededObject(), timezone) else if (o is Calendar) {
            return DateTimeImpl(o as Calendar)
        }
        throw ExpressionException("can't cast [" + Caster.toClassName(o).toString() + "] to date value")
    }

    /**
     * converts an Object to a DateTime Object (Advanced but slower)
     *
     * @param str String to Convert
     * @param timezone
     * @return Date Time Object
     * @throws PageException
     */
    @Throws(PageException::class)
    fun toDateAdvanced(str: String, timezone: TimeZone?): DateTime {
        return toDateAdvanced(str, timezone, null)
                ?: throw ExpressionException("can't cast [$str] to date value")
    }

    /**
     * converts an Object to a DateTime Object (Advanced but slower), returns null if invalid string
     *
     * @param o Object to Convert
     * @param timeZone
     * @param defaultValue
     * @return Date Time Object
     */
    fun toDateAdvanced(o: Object, timeZone: TimeZone?, defaultValue: DateTime): DateTime {
        if (o is DateTime) return o as DateTime else if (o is Date) return DateTimeImpl(o as Date) else if (o is Castable) {
            return (o as Castable).castToDateTime(defaultValue)
        } else if (o is String) return toDateAdvanced(o.toString(), timeZone, defaultValue) else if (o is Number) return util.toDateTime((o as Number).doubleValue()) else if (o is Calendar) {
            return DateTimeImpl(o as Calendar)
        } else if (o is ObjectWrap) return toDateAdvanced((o as ObjectWrap).getEmbededObject(defaultValue), timeZone, defaultValue)
        return defaultValue
    }

    /**
     * converts a String to a DateTime Object (Advanced but slower), returns null if invalid string
     *
     * @param str String to convert
     * @param convertingType one of the following values: - CONVERTING_TYPE_NONE: number are not
     * converted at all - CONVERTING_TYPE_YEAR: integers are handled as years -
     * CONVERTING_TYPE_OFFSET: numbers are handled as offset from 1899-12-30 00:00:00 UTC
     * @param timeZone
     * @param defaultValue
     * @return Date Time Object
     */
    fun toDateAdvanced(str: String, convertingType: Short, timeZone: TimeZone?, defaultValue: DateTime?): DateTime? {
        var str = str
        var timeZone: TimeZone? = timeZone
        str = str.trim()
        if (StringUtil.isEmpty(str)) return defaultValue
        if (!hasDigits(str)) return defaultValue // every format has digits
        timeZone = ThreadLocalPageContext.getTimeZone(timeZone)
        var dt: DateTime? = toDateSimple(str, convertingType, true, timeZone, defaultValue)
        if (dt == null) {
            val formats: Array<DateFormat> = FormatUtil.getCFMLFormats(timeZone, true)
            var df: DateFormat
            var d: Date
            val pp = ParsePosition(0)
            for (i in formats.indices) {
                df = formats[i]
                pp.setErrorIndex(-1)
                pp.setIndex(0)
                synchronized(df) { d = df.parse(str, pp) }
                if (pp.getIndex() === 0 || d == null || pp.getIndex() < str.length()) continue
                dt = DateTimeImpl(d.getTime(), false)
                return dt
            }
            dt = toDateTime(Locale.US, str, timeZone, defaultValue, false)
        }
        return dt
    }

    private fun hasDigits(str: String): Boolean {
        for (i in str.length() - 1 downTo 0) {
            if (Character.isDigit(str.charAt(i))) return true
        }
        return false
    }

    /**
     * parse a string to a Datetime Object
     *
     * @param locale
     * @param str String representation of a locale Date
     * @param tz
     * @return DateTime Object
     * @throws PageException
     */
    @Throws(PageException::class)
    fun toDateTime(locale: Locale, str: String, tz: TimeZone?, useCommomDateParserAsWell: Boolean): DateTime {
        val dt: DateTime? = toDateTime(locale, str, tz, null, useCommomDateParserAsWell)
        if (dt == null) {
            val prefix: String = locale.getLanguage().toString() + "-" + locale.getCountry() + "-"
            throw ExpressionException("can't cast [$str] to date value",
                    "to add custom formats for " + LocaleFactory.toString(locale).toString() + ", create/extend on of the following files [" + prefix + "datetime.df (for date time formats), "
                            + prefix + "date.df (for date formats) or " + prefix + "time.df (for time formats)] in the following directory [<context>/tachyon/locales]." + "")

            // throw new ExpressionException("can't cast ["+str+"] to date value");
        }
        return dt
    }
    /*
	 * public static void main(String[] args) throws PageException {
	 * 
	 * Locale[] locales = Locale.getAvailableLocales(); Iterator<Locale> it =
	 * LocaleFactory.getLocales().values().iterator();
	 * 
	 * //print.e(toDateTime(new Locale("de","CH"), "06.02.2008 01:02:01 MEZ",
	 * TimeZone.getDefault(),null, false)); String str="dimanche, 6. avril 2008 01:02:03";
	 * str="06.02.2008, 01:02:01 MEZ"; str="01.02. h CEST"; str="6-apr-2008";
	 * str="Sunday, April 6, 2008 1:02:03 AM CEST"; str="Sunday, April 6, 2008 1:02:03 AM CEST";
	 * str="01:02:03 o'clock CEST"; str="1:02 Uhr MEZ"; Locale l=new Locale("fr","CH"); l=new
	 * Locale("it","CH"); l=new Locale("en","US"); l=new Locale("en","UK"); l=new Locale("de","CH");
	 * //l=new Locale("es","ES"); //l=LocaleConstant.PORTUGUESE_BRASIL;
	 * //l=LocaleConstant.DUTCH_NETHERLANDS; //l=LocaleConstant.ARABIC_ALGERIA; //l=Locale.CHINA;
	 * print.e(str); print.e(toDateTime(l, str, TimeZone.getDefault(),null, false));
	 * 
	 * 
	 * }
	 */
    /**
     * parse a string to a Datetime Object, returns null if can't convert
     *
     * @param locale
     * @param str String representation of a locale Date
     * @param tz
     * @param defaultValue
     * @return datetime object
     */
    fun toDateTime(locale: Locale?, str: String, tz: TimeZone?, defaultValue: DateTime?, useCommomDateParserAsWell: Boolean): DateTime? {
        var str = str
        var tz: TimeZone? = tz
        str = str.trim()
        tz = ThreadLocalPageContext.getTimeZone(tz)
        var df: Array<DateFormat>

        // get Calendar
        val c: Calendar = JREDateTimeUtil.getThreadCalendar(locale, tz)

        // datetime
        val pp = ParsePosition(0)
        df = FormatUtil.getDateTimeFormats(locale, tz, false) // dfc[FORMATS_DATE_TIME];
        var d: Date
        for (i in df.indices) {
            val sdf: SimpleDateFormat = df[i] as SimpleDateFormat
            // print.e(sdf.format(new Date(108,3,6,1,2,1)) + " : "+sdf.toPattern());
            pp.setErrorIndex(-1)
            pp.setIndex(0)
            sdf.setTimeZone(tz)
            d = sdf.parse(str, pp)
            if (pp.getIndex() === 0 || d == null || pp.getIndex() < str.length()) continue
            optimzeDate(c, tz, d)
            return DateTimeImpl(c.getTime())
        }

        // date
        df = FormatUtil.getDateFormats(locale, tz, false)
        for (i in df.indices) {
            val sdf: SimpleDateFormat = df[i] as SimpleDateFormat
            // print.e(sdf.format(new Date(108,3,6,1,2,1)) + " : "+sdf.toPattern());
            pp.setErrorIndex(-1)
            pp.setIndex(0)
            sdf.setTimeZone(tz)
            d = sdf.parse(str, pp)
            if (pp.getIndex() === 0 || d == null || pp.getIndex() < str.length()) continue
            optimzeDate(c, tz, d)
            return DateTimeImpl(c.getTime())
        }

        // time
        df = FormatUtil.getTimeFormats(locale, tz, false) // dfc[FORMATS_TIME];
        for (i in df.indices) {
            val sdf: SimpleDateFormat = df[i] as SimpleDateFormat
            // print.e(sdf.format(new Date(108,3,6,1,2,1))+ " : "+sdf.toPattern());
            pp.setErrorIndex(-1)
            pp.setIndex(0)
            sdf.setTimeZone(tz)
            d = sdf.parse(str, pp)
            if (pp.getIndex() === 0 || d == null || pp.getIndex() < str.length()) continue
            c.setTimeZone(tz)
            c.setTime(d)
            c.set(Calendar.YEAR, 1899)
            c.set(Calendar.MONTH, 11)
            c.set(Calendar.DAY_OF_MONTH, 30)
            c.setTimeZone(tz)
            return DateTimeImpl(c.getTime())
        }
        return if (useCommomDateParserAsWell) toDateSimple(str, CONVERTING_TYPE_NONE, true, tz, defaultValue) else defaultValue
    }

    private fun optimzeDate(c: Calendar, tz: TimeZone?, d: Date?) {
        c.setTimeZone(tz)
        c.setTime(d)
        val year: Int = c.get(Calendar.YEAR)
        if (year < 40) c.set(Calendar.YEAR, 2000 + year) else if (year < 100) c.set(Calendar.YEAR, 1900 + year)
    }

    fun toDateAdvanced(str: String?, timeZone: TimeZone?, defaultValue: DateTime?): DateTime {
        return toDateAdvanced(str, CONVERTING_TYPE_OFFSET, timeZone, defaultValue)
    }

    /**
     * converts a boolean to a DateTime Object
     *
     * @param b boolean to Convert
     * @param timeZone
     * @return coverted Date Time Object
     */
    fun toDateSimple(b: Boolean, timeZone: TimeZone?): DateTime {
        return toDateSimple(if (b) 1L else 0L, timeZone)
    }

    /**
     * converts a char to a DateTime Object
     *
     * @param c char to Convert
     * @param timeZone
     * @return coverted Date Time Object
     */
    fun toDateSimple(c: Char, timeZone: TimeZone?): DateTime {
        return toDateSimple(c.toLong(), timeZone)
    }

    /**
     * converts a double to a DateTime Object
     *
     * @param d double to Convert
     * @param timeZone
     * @return coverted Date Time Object
     */
    fun toDateSimple(d: Double, timeZone: TimeZone?): DateTime {
        return util.toDateTime(d)
    }

    /**
     * converts a double to a DateTime Object
     *
     * @param d double to Convert
     * @param timeZone
     * @return coverted Date Time Object
     */
    fun toDateSimple(l: Long, timeZone: TimeZone?): DateTime {
        return util.toDateTime(l)
    }
    /*
	 * * converts a double to a DateTime Object
	 * 
	 * @param d double to Convert
	 * 
	 * @param timeZone
	 * 
	 * @return coverted Date Time Object / public static DateTime toDateSimple(long l, TimeZone
	 * timezone) { return new DateTimeImpl(l,false); }
	 */
    /**
     * converts an Object to a DateTime Object, returns null if invalid string
     *
     * @param o Object to Convert
     * @param convertingType one of the following values: - CONVERTING_TYPE_NONE: number are not
     * converted at all - CONVERTING_TYPE_YEAR: integers are handled as years -
     * CONVERTING_TYPE_OFFSET: numbers are handled as offset from 1899-12-30 00:00:00 UTC
     * @param timeZone
     * @return coverted Date Time Object
     * @throws PageException
     */
    @Throws(PageException::class)
    fun toDateSimple(o: Object, convertingType: Short, alsoMonthString: Boolean, timeZone: TimeZone?): DateTime {
        if (o is DateTime) return o as DateTime else if (o is Date) return DateTimeImpl(o as Date) else if (o is Castable) return (o as Castable).castToDateTime() else if (o is String) return toDateSimple(o.toString(), convertingType, alsoMonthString, timeZone) else if (o is Number) return util.toDateTime((o as Number).doubleValue()) else if (o is Calendar) return DateTimeImpl(o as Calendar) else if (o is ObjectWrap) return toDateSimple((o as ObjectWrap).getEmbededObject(), convertingType, alsoMonthString, timeZone) else if (o is Calendar) {
            return DateTimeImpl(o as Calendar)
        }
        if (o is Component) throw ExpressionException("can't cast component [" + (o as Component).getAbsName().toString() + "] to date value")
        throw ExpressionException("can't cast [" + Caster.toTypeName(o).toString() + "] to date value")
    }

    /**
     *
     * @param o
     * @param convertingType one of the following values: - CONVERTING_TYPE_NONE: number are not
     * converted at all - CONVERTING_TYPE_YEAR: integers are handled as years -
     * CONVERTING_TYPE_OFFSET: numbers are handled as offset from 1899-12-30 00:00:00 UTC
     * @param alsoMonthString
     * @param timeZone
     * @param defaultValue
     * @return
     */
    fun toDateSimple(o: Object, convertingType: Short, alsoMonthString: Boolean, timeZone: TimeZone?, defaultValue: DateTime): DateTime {
        if (o is DateTime) return o as DateTime else if (o is Date) return DateTimeImpl(o as Date) else if (o is Castable) return (o as Castable).castToDateTime(defaultValue) else if (o is String) return toDateSimple(o.toString(), convertingType, alsoMonthString, timeZone, defaultValue) else if (o is Number) return util.toDateTime((o as Number).doubleValue()) else if (o is Calendar) return DateTimeImpl(o as Calendar) else if (o is ObjectWrap) {
            val eo: Object = (o as ObjectWrap).getEmbededObject(CollectionUtil.NULL)
            return if (eo === CollectionUtil.NULL) defaultValue else toDateSimple(eo, convertingType, alsoMonthString, timeZone, defaultValue)
        } else if (o is Calendar) {
            return DateTimeImpl(o as Calendar)
        }
        return defaultValue
    }

    /**
     * converts an Object to a DateTime Object, returns null if invalid string
     *
     * @param str String to Convert
     * @param timeZone
     * @return coverted Date Time Object
     * @throws PageException
     */
    @Throws(PageException::class)
    fun toDateSimple(str: String, timeZone: TimeZone?): DateTime {
        return toDateSimple(str, CONVERTING_TYPE_OFFSET, true, timeZone, null)
                ?: throw ExpressionException("can't cast [$str] to date value")
    }

    /**
     * converts an Object to a Time Object, returns null if invalid string
     *
     * @param o Object to Convert
     * @return coverted Date Time Object
     * @throws PageException
     */
    @Throws(PageException::class)
    fun toTime(timeZone: TimeZone?, o: Object): Time {
        if (o is Time) return o as Time else if (o is Date) return TimeImpl(o as Date) else if (o is Castable) return TimeImpl((o as Castable).castToDateTime()) else if (o is String) {
            return toTime(timeZone, o.toString(), null)
                    ?: throw ExpressionException("can't cast [$o] to time value")
        } else if (o is ObjectWrap) return toTime(timeZone, (o as ObjectWrap).getEmbededObject()) else if (o is Calendar) {
            // TODO check timezone offset
            return TimeImpl((o as Calendar).getTimeInMillis(), false)
        }
        throw ExpressionException("can't cast [" + Caster.toClassName(o).toString() + "] to time value")
    }

    /**
     *
     * @param o
     * @param convertingType one of the following values: - CONVERTING_TYPE_NONE: number are not
     * converted at all - CONVERTING_TYPE_YEAR: integers are handled as years -
     * CONVERTING_TYPE_OFFSET: numbers are handled as offset from 1899-12-30 00:00:00 UTC
     * @param timeZone
     * @param defaultValue
     * @return
     */
    fun toDateAdvanced(o: Object, convertingType: Short, timeZone: TimeZone, defaultValue: DateTime): DateTime {
        return _toDateAdvanced(o, convertingType, timeZone, defaultValue, true)
    }

    private fun _toDateAdvanced(o: Object, convertingType: Short, timeZone: TimeZone, defaultValue: DateTime, advanced: Boolean): DateTime {
        if (o is DateTime) return o as DateTime else if (o is Date) return DateTimeImpl(o as Date) else if (o is Castable) {
            return (o as Castable).castToDateTime(defaultValue)
        } else if (o is String) {
            return if (advanced) toDateAdvanced(o.toString(), convertingType, timeZone, defaultValue) else toDateSimple(o.toString(), convertingType, true, timeZone, defaultValue)
        } else if (o is Number) {
            return numberToDate(timeZone, (o as Number).doubleValue(), convertingType, defaultValue)
        } else if (o is ObjectWrap) {
            return _toDateAdvanced((o as ObjectWrap).getEmbededObject(defaultValue), convertingType, timeZone, defaultValue, advanced)
        } else if (o is Calendar) {
            return DateTimeImpl(o as Calendar)
        }
        return defaultValue
    }

    /**
     * converts an Object to a DateTime Object, returns null if invalid string
     *
     * @param str Stringt to Convert
     * @param convertingType one of the following values: - CONVERTING_TYPE_NONE: number are not
     * converted at all - CONVERTING_TYPE_YEAR: integers are handled as years -
     * CONVERTING_TYPE_OFFSET: numbers are handled as offset from 1899-12-30 00:00:00 UTC
     * @param timeZone
     * @return coverted Date Time Object
     * @throws PageException
     */
    @Throws(PageException::class)
    fun toDateSimple(str: String?, convertingType: Short, alsoMonthString: Boolean, timeZone: TimeZone?): DateTime {
        return toDateSimple(str, convertingType, alsoMonthString, timeZone, null)
                ?: throw ExpressionException("can't cast value to a Date Object")
    }

    @Throws(PageException::class)
    fun toDateAdvanced(o: Object, convertingType: Short, timeZone: TimeZone?): DateTime {
        val dt: DateTime = toDateAdvanced(o, convertingType, timeZone, null)
        if (dt == null) {
            if (o is CharSequence) throw ExpressionException("can't cast value [$o] to a Date Object")
            throw ExpressionException("can't cast value to a Date Object")
        }
        return dt
    }

    /**
     * converts a String to a Time Object, returns null if invalid string @param str String to
     * convert @param defaultValue @return Time Object @throws
     */
    fun toTime(timeZone: TimeZone?, str: String?, defaultValue: Time?): Time? {
        if (str == null || str.length() < 3) {
            return defaultValue
        }
        val ds = DateString(str)
        // Timestamp
        if (ds.isCurrent('{') && ds.isLast('}')) {

            // Time
            // "^\\{t '([0-9]{1,2}):([0-9]{1,2}):([0-9]{2})'\\}$"
            if (ds.fwIfNext('t')) {

                // Time
                if (!(ds.fwIfNext(' ') && ds.fwIfNext('\''))) return defaultValue
                ds.next()
                // hour
                val hour: Int = ds.readDigits()
                if (hour == -1) return defaultValue
                if (!ds.fwIfCurrent(':')) return defaultValue

                // minute
                val minute: Int = ds.readDigits()
                if (minute == -1) return defaultValue
                if (!ds.fwIfCurrent(':')) return defaultValue

                // second
                val second: Int = ds.readDigits()
                if (second == -1) return defaultValue
                if (!(ds.fwIfCurrent('\'') && ds.fwIfCurrent('}'))) return defaultValue
                if (ds.isAfterLast()) {
                    val time: Long = util.toTime(timeZone, 1899, 12, 30, hour, minute, second, 0, DEFAULT_VALUE)
                    return if (time == DEFAULT_VALUE) defaultValue else TimeImpl(time, false)
                }
                return defaultValue
            }
            return defaultValue
        }
        // Time start with int
        /*
		 * else if(ds.isDigit()) { char sec=ds.charAt(1); char third=ds.charAt(2); // 16.10.2004 (02:15)?
		 * if(sec==':' || third==':') { // hour int hour=ds.readDigits(); if(hour==-1) return defaultValue;
		 * 
		 * if(!ds.fwIfCurrent(':'))return defaultValue;
		 * 
		 * // minutes int minutes=ds.readDigits(); if(minutes==-1) return defaultValue;
		 * 
		 * if(ds.isAfterLast()) { long time=util.toTime(timeZone,1899,12,30,hour,minutes,0,0,DEFAULT_VALUE);
		 * if(time==DEFAULT_VALUE) return defaultValue;
		 * 
		 * return new TimeImpl(time,false); } //else if(!ds.fwIfCurrent(':'))return null; else
		 * if(!ds.fwIfCurrent(':')) { if(!ds.fwIfCurrent(' '))return defaultValue;
		 * 
		 * if(ds.fwIfCurrent('a') || ds.fwIfCurrent('A')) { if(ds.fwIfCurrent('m') || ds.fwIfCurrent('M')) {
		 * if(ds.isAfterLast()) { long time=util.toTime(timeZone,1899,12,30,hour,minutes,0,0,DEFAULT_VALUE);
		 * if(time==DEFAULT_VALUE) return defaultValue; return new TimeImpl(time,false); } } return
		 * defaultValue; } else if(ds.fwIfCurrent('p') || ds.fwIfCurrent('P')) { if(ds.fwIfCurrent('m') ||
		 * ds.fwIfCurrent('M')) { if(ds.isAfterLast()) { long
		 * time=util.toTime(timeZone,1899,12,30,hour<13?hour+12:hour,minutes,0,0,DEFAULT_VALUE);
		 * if(time==DEFAULT_VALUE) return defaultValue; return new TimeImpl(time,false); } } return
		 * defaultValue; } return defaultValue; }
		 * 
		 * 
		 * // seconds int seconds=ds.readDigits(); if(seconds==-1) return defaultValue;
		 * 
		 * if(ds.isAfterLast()) { long
		 * time=util.toTime(timeZone,1899,12,30,hour,minutes,seconds,0,DEFAULT_VALUE);
		 * if(time==DEFAULT_VALUE) return defaultValue; return new TimeImpl(time,false); }
		 * 
		 * } }
		 */

        // TODO bessere impl
        ds.reset()
        val rtn: DateTime? = parseTime(timeZone, intArrayOf(1899, 12, 30), ds, defaultValue, -1)
        return if (rtn === defaultValue) defaultValue else TimeImpl(rtn)

        // return defaultValue;
    }

    /**
     * converts a String to a DateTime Object, returns null if invalid string
     *
     * @param str String to convert
     * @param convertingType one of the following values: - CONVERTING_TYPE_NONE: number are not
     * converted at all - CONVERTING_TYPE_YEAR: integers are handled as years -
     * CONVERTING_TYPE_OFFSET: numbers are handled as offset from 1899-12-30 00:00:00 UTC
     * @param alsoMonthString allow that the month is an English name
     * @param timeZone
     * @param defaultValue
     * @return Date Time Object
     */
    private fun parseDateTime(str: String, ds: DateString, convertingType: Short, alsoMonthString: Boolean, timeZone: TimeZone, defaultValue: DateTime): DateTime? {
        var month = 0
        var first: Int = ds.readDigits()
        // first
        if (first == -1) {
            if (!alsoMonthString) return defaultValue
            first = ds.readMonthString()
            if (first == -1) return defaultValue
            month = 1
        }
        if (ds.isAfterLast()) return if (month == 1) defaultValue else numberToDate(timeZone, Caster.toDoubleValue(str, Double.NaN), convertingType, defaultValue)
        val del: Char = ds.current()
        if (del != '.' && del != '/' && del != '-' && del != ' ' && del != '\t') {
            return if (ds.fwIfCurrent(':')) {
                parseTime(timeZone, intArrayOf(1899, 12, 30), ds, defaultValue, first)
            } else defaultValue
        }
        ds.next()
        ds.removeWhitespace()

        // second
        var second: Int = ds.readDigits()
        if (second == -1) {
            if (!alsoMonthString || month != 0) return defaultValue
            second = ds.readMonthString()
            if (second == -1) return defaultValue
            month = 2
        }
        if (ds.isAfterLast()) {
            return toDate(month, timeZone, first, second, defaultValue)
        }
        val del2: Char = ds.current()
        if (del != del2) {
            ds.fwIfCurrent(' ')
            ds.fwIfCurrent('T')
            ds.fwIfCurrent(' ')
            return parseTime(timeZone, _toDate(timeZone, month, first, second), ds, defaultValue, -1)
        }
        ds.next()
        ds.removeWhitespace()
        val third: Int = ds.readDigits()
        if (third == -1) {
            return defaultValue
        }
        if (ds.isAfterLast()) {
            return if (classicStyle() && del == '.') toDate(month, timeZone, second, first, third, defaultValue) else toDate(month, timeZone, first, second, third, defaultValue)
        }
        ds.fwIfCurrent(' ')
        ds.fwIfCurrent('T')
        ds.fwIfCurrent(' ')
        return if (classicStyle() && del == '.') parseTime(timeZone, _toDate(month, second, first, third), ds, defaultValue, -1) else parseTime(timeZone, _toDate(month, first, second, third), ds, defaultValue, -1)
    }

    private fun classicStyle(): Boolean {
        return classicStyle
    }

    private fun parseTime(timeZone: TimeZone?, date: IntArray?, ds: DateString, defaultValue: DateTime?, hours: Int): DateTime? {
        var hours = hours
        if (date == null) return defaultValue
        ds.removeWhitespace()

        // hour
        var next = false
        if (hours == -1) {
            ds.removeWhitespace()
            hours = ds.readDigits()
            ds.removeWhitespace()
            if (hours == -1) {
                return parseOffset(ds, timeZone, date, 0, 0, 0, 0, true, defaultValue)
            }
        } else next = true
        var minutes = 0
        if (next || ds.fwIfCurrent(':')) {
            ds.removeWhitespace()
            minutes = ds.readDigits()
            ds.removeWhitespace()
            if (minutes == -1) return defaultValue
        }
        var seconds = 0
        if (ds.fwIfCurrent(':')) {
            ds.removeWhitespace()
            seconds = ds.readDigits()
            ds.removeWhitespace()
            if (seconds == -1) return defaultValue
        }
        var msSeconds = 0
        if (ds.fwIfCurrent('.')) {
            ds.removeWhitespace()
            msSeconds = ds.readDigits()
            ds.removeWhitespace()
            if (msSeconds == -1) return defaultValue
        }
        if (ds.isAfterLast()) {
            return DateTimeUtil.getInstance().toDateTime(timeZone, date[0], date[1], date[2], hours, minutes, seconds, msSeconds, defaultValue)
        }
        ds.fwIfCurrent(' ')
        if (ds.fwIfCurrent('a') || ds.fwIfCurrent('A')) {
            if (!ds.fwIfCurrent('m')) ds.fwIfCurrent('M')
            return if (ds.isAfterLast()) DateTimeUtil.getInstance().toDateTime(timeZone, date[0], date[1], date[2], if (hours < 12) hours else hours - 12, minutes, seconds, msSeconds, defaultValue) else defaultValue
        } else if (ds.fwIfCurrent('p') || ds.fwIfCurrent('P')) {
            if (!ds.fwIfCurrent('m')) ds.fwIfCurrent('M')
            if (hours > 24) return defaultValue
            return if (ds.isAfterLast()) DateTimeUtil.getInstance().toDateTime(timeZone, date[0], date[1], date[2], if (hours < 12) hours + 12 else hours, minutes, seconds, msSeconds, defaultValue) else defaultValue
        }
        ds.fwIfCurrent(' ')
        return parseOffset(ds, timeZone, date, hours, minutes, seconds, msSeconds, true, defaultValue)
    }

    private fun parseOffset(ds: DateString, timeZone: TimeZone?, date: IntArray, hours: Int, minutes: Int, seconds: Int, msSeconds: Int, checkAfterLast: Boolean,
                            defaultValue: DateTime?): DateTime? {
        if (ds.isLast() && (ds.fwIfCurrent('Z') || ds.fwIfCurrent('z'))) {
            return util.toDateTime(TimeZoneConstants.UTC, date[0], date[1], date[2], hours, minutes, seconds, msSeconds, defaultValue)
        } else if (ds.fwIfCurrent('+')) {
            val rtn: DateTime = util.toDateTime(timeZone, date[0], date[1], date[2], hours, minutes, seconds, msSeconds, defaultValue)
            return if (rtn === defaultValue) rtn else readOffset(true, timeZone, rtn, date[0], date[1], date[2], hours, minutes, seconds, msSeconds, ds, checkAfterLast, defaultValue)
        } else if (ds.fwIfCurrent('-')) {
            val rtn: DateTime = util.toDateTime(timeZone, date[0], date[1], date[2], hours, minutes, seconds, msSeconds, defaultValue)
            return if (rtn === defaultValue) rtn else readOffset(false, timeZone, rtn, date[0], date[1], date[2], hours, minutes, seconds, msSeconds, ds, checkAfterLast, defaultValue)
        }
        return defaultValue
    }

    private fun toDate(month: Int, timeZone: TimeZone, first: Int, second: Int, defaultValue: DateTime): DateTime {
        val d: IntArray = _toDate(timeZone, month, first, second)
                ?: return defaultValue
        return util.toDateTime(timeZone, d[0], d[1], d[2], 0, 0, 0, 0, defaultValue)
    }

    private fun _toDate(tz: TimeZone, month: Int, first: Int, second: Int): IntArray? {
        val YEAR = year(tz)
        if (first <= 12 && month < 2) {
            return if (util.daysInMonth(YEAR, first) >= second) intArrayOf(YEAR, first, second) else intArrayOf(util.toYear(second), first, 1)
        }
        // first>12
        return if (second <= 12) {
            if (util.daysInMonth(YEAR, second) >= first) intArrayOf(YEAR, second, first) else intArrayOf(util.toYear(first), second, 1)
        } else null
    }

    private fun year(tz: TimeZone): Int {
        return util.getYear(ThreadLocalPageContext.getTimeZone(tz), DateTimeImpl())
    }

    private fun toDate(month: Int, timeZone: TimeZone, first: Int, second: Int, third: Int, defaultValue: DateTime): DateTime {
        val d: IntArray = _toDate(month, first, second, third) ?: return defaultValue
        return util.toDateTime(timeZone, d[0], d[1], d[2], 0, 0, 0, 0, defaultValue)
    }

    private fun _toDate(month: Int, first: Int, second: Int, third: Int): IntArray? {
        var first = first
        var third = third
        if (first <= 12) {
            if (month == 2) return intArrayOf(util.toYear(third), second, first)
            return if (util.daysInMonth(util.toYear(third), first) >= second) intArrayOf(util.toYear(third), first, second) else null
        }
        if (second > 12) return null
        if (month == 2) {
            val tmp = first
            first = third
            third = tmp
        }
        return if (util.daysInMonth(util.toYear(first), second) < third) {
            if (util.daysInMonth(util.toYear(third), second) >= first) intArrayOf(util.toYear(third), second, first) else null
        } else intArrayOf(util.toYear(first), second, third)
    }

    /**
     * converts the given string to a date following simple and fast parsing rules (no international
     * formats)
     *
     * @param str
     * @param convertingType one of the following values: - CONVERTING_TYPE_NONE: number are not
     * converted at all - CONVERTING_TYPE_YEAR: integers are handled as years -
     * CONVERTING_TYPE_OFFSET: numbers are handled as offset from 1899-12-30 00:00:00 UTC
     * @param alsoMonthString allow that the month is defined as english word (jan,janauary ...)
     * @param timeZone
     * @param defaultValue
     * @return
     */
    fun toDateSimple(str: String, convertingType: Short, alsoMonthString: Boolean, timeZone: TimeZone, defaultValue: DateTime): DateTime? {
        var str = str
        str = StringUtil.trim(str, "")
        val ds = DateString(str)

        // Timestamp
        if (ds.isCurrent('{') && ds.isLast('}')) {
            return _toDateSimpleTS(ds, timeZone, defaultValue)
        }
        val res: DateTime? = parseDateTime(str, ds, convertingType, alsoMonthString, timeZone, defaultValue)
        return if (res === defaultValue && Decision.isNumber(str)) {
            numberToDate(timeZone, Caster.toDoubleValue(str, Double.NaN), convertingType, defaultValue)
        } else res
    }

    /**
     *
     * @param timeZone
     * @param d
     * @param convertingType one of the following values: - CONVERTING_TYPE_NONE: number are not
     * converted at all - CONVERTING_TYPE_YEAR: integers are handled as years -
     * CONVERTING_TYPE_OFFSET: numbers are handled as offset from 1899-12-30 00:00:00 UTC
     * @return
     */
    private fun numberToDate(timeZone: TimeZone, d: Double, convertingType: Short, defaultValue: DateTime): DateTime {
        var timeZone: TimeZone? = timeZone
        if (!Decision.isValid(d)) return defaultValue
        if (convertingType == CONVERTING_TYPE_YEAR) {
            val i = d.toInt()
            if (i.toDouble() == d) {
                timeZone = ThreadLocalPageContext.getTimeZone(timeZone)
                val c: Calendar = Calendar.getInstance(timeZone)
                c.set(Calendar.MILLISECOND, 0)
                c.set(i, 0, 1, 0, 0, 0)
                return DateTimeImpl(c)
            }
        }
        return if (convertingType == CONVERTING_TYPE_OFFSET) util.toDateTime(d) else defaultValue
    }

    private fun _toDateSimpleTS(ds: DateString, timeZone: TimeZone, defaultValue: DateTime): DateTime {
        // Date
        // "^\\{d '([0-9]{2,4})-([0-9]{1,2})-([0-9]{1,2})'\\}$"
        return if (ds.fwIfNext('d')) {
            if (!(ds.fwIfNext(' ') && ds.fwIfNext('\''))) return defaultValue
            ds.next()
            // year
            val year: Int = ds.readDigits()
            if (year == -1) return defaultValue
            if (!ds.fwIfCurrent('-')) return defaultValue

            // month
            val month: Int = ds.readDigits()
            if (month == -1) return defaultValue
            if (!ds.fwIfCurrent('-')) return defaultValue

            // day
            val day: Int = ds.readDigits()
            if (day == -1) return defaultValue
            if (!(ds.fwIfCurrent('\'') && ds.fwIfCurrent('}'))) return defaultValue
            if (ds.isAfterLast()) util.toDateTime(timeZone, year, month, day, 0, 0, 0, 0, defaultValue) else defaultValue // new DateTimeImpl(year,month,day);
        } else if (ds.fwIfNext('t')) {
            if (!(ds.fwIfNext('s') && ds.fwIfNext(' ') && ds.fwIfNext('\''))) {

                // Time
                if (!(ds.fwIfNext(' ') && ds.fwIfNext('\''))) return defaultValue
                ds.next()
                // hour
                val hour: Int = ds.readDigits()
                if (hour == -1) return defaultValue
                if (!ds.fwIfCurrent(':')) return defaultValue

                // minute
                val minute: Int = ds.readDigits()
                if (minute == -1) return defaultValue
                if (!ds.fwIfCurrent(':')) return defaultValue

                // second
                val second: Int = ds.readDigits()
                if (second == -1) return defaultValue

                // Milli Second
                var millis = 0
                if (ds.fwIfCurrent('.')) {
                    millis = ds.readDigits()
                }
                val before: Int = ds.getPos()
                val tmp: DateTime? = parseOffset(ds, timeZone, intArrayOf(1899, 12, 30), hour, minute, second, millis, false, defaultValue)
                if (tmp == null && before != ds.getPos()) return defaultValue
                if (!(ds.fwIfCurrent('\'') && ds.fwIfCurrent('}'))) return defaultValue
                if (ds.isAfterLast()) {
                    if (tmp != null) {
                        return TimeImpl(tmp.getTime(), false)
                    }
                    val time: Long = util.toTime(timeZone, 1899, 12, 30, hour, minute, second, millis, DEFAULT_VALUE)
                    return if (time == DEFAULT_VALUE) defaultValue else TimeImpl(time, false)
                }
                return defaultValue
            }
            ds.next()
            // year
            val year: Int = ds.readDigits()
            if (year == -1) return defaultValue
            if (!ds.fwIfCurrent('-')) return defaultValue

            // month
            val month: Int = ds.readDigits()
            if (month == -1) return defaultValue
            if (!ds.fwIfCurrent('-')) return defaultValue

            // day
            val day: Int = ds.readDigits()
            if (day == -1) return defaultValue
            if (!ds.fwIfCurrent(' ')) return defaultValue

            // hour
            val hour: Int = ds.readDigits()
            if (hour == -1) return defaultValue
            if (!ds.fwIfCurrent(':')) return defaultValue

            // minute
            val minute: Int = ds.readDigits()
            if (minute == -1) return defaultValue
            if (!ds.fwIfCurrent(':')) return defaultValue

            // second
            val second: Int = ds.readDigits()
            if (second == -1) return defaultValue

            // Milli Second
            var millis = 0
            if (ds.fwIfCurrent('.')) {
                millis = ds.readDigits()
            }
            val before: Int = ds.getPos()
            val tmp: DateTime? = parseOffset(ds, timeZone, intArrayOf(year, month, day), hour, minute, second, millis, false, defaultValue)
            if (tmp == null && before != ds.getPos()) return defaultValue
            if (!(ds.fwIfCurrent('\'') && ds.fwIfCurrent('}'))) return defaultValue
            if (ds.isAfterLast()) {
                return if (tmp != null) tmp else util.toDateTime(timeZone, year, month, day, hour, minute, second, millis, defaultValue)
            }
            defaultValue
        } else defaultValue
    }

    /**
     * reads an offset definition at the end of a date string
     *
     * @param timeZone
     * @param dt previous parsed date Object
     * @param ds DateString to parse
     * @param defaultValue
     * @return date Object with offset
     */
    private fun readOffset(isPlus: Boolean, timeZone: TimeZone?, dt: DateTime, years: Int, months: Int, days: Int, hours: Int, minutes: Int, seconds: Int, milliSeconds: Int,
                           ds: DateString, checkAfterLast: Boolean, defaultValue: DateTime?): DateTime? {
        // timeZone=ThreadLocalPageContext.getTimeZone(timeZone);
        if (timeZone == null) return defaultValue
        // HOUR
        var hourLength: Int = ds.getPos()
        var hour: Int = ds.readDigits()
        hourLength = ds.getPos() - hourLength
        if (hour == -1) return defaultValue

        // MINUTE
        var minute = 0
        if (!ds.isAfterLast()) {
            if (!(ds.fwIfCurrent(':') || ds.fwIfCurrent('.'))) return defaultValue
            minute = ds.readDigits()
            if (minute == -1) return defaultValue
        } else if (hourLength > 2) {
            val h = hour / 100
            minute = hour - h * 100
            hour = h
        }
        if (minute > 59) return defaultValue
        if (hour > 14 || hour == 14 && minute > 0) return defaultValue
        var offset = hour * 60L * 60L * 1000L
        offset += (minute * 60 * 1000).toLong()
        if (!checkAfterLast || ds.isAfterLast()) {
            var time: Long = util.toTime(TimeZoneConstants.UTC, years, months, days, hours, minutes, seconds, milliSeconds, 0)
            if (isPlus) time -= offset else time += offset
            return DateTimeImpl(time, false)
        }
        return defaultValue
    }

    @Throws(PageException::class)
    fun toUSDate(o: Object?, timeZone: TimeZone?): String {
        if (Decision.isUSDate(o)) return Caster.toString(o)
        val date: DateTime = toDateAdvanced(o, timeZone)
        return DateFormat(Locale.US).format(date, "mm/dd/yyyy")
    }

    @Throws(PageException::class)
    fun toEuroDate(o: Object?, timeZone: TimeZone?): String {
        if (Decision.isEuroDate(o)) return Caster.toString(o)
        val date: DateTime = toDateAdvanced(o, timeZone)
        return DateFormat(Locale.US).format(date, "dd.mm.yyyy")
    }

    fun toShortTime(time: Long): String {
        return toString(time / 1000, 36)
    }

    fun fromShortTime(str: String?): Long {
        return Long.parseLong(str, 36) * 1000
    }
}