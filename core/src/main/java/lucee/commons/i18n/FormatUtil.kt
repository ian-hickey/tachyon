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
package lucee.commons.i18n

import java.lang.ref.SoftReference

object FormatUtil {
    const val FORMAT_TYPE_DATE: Short = 1
    const val FORMAT_TYPE_TIME: Short = 2
    const val FORMAT_TYPE_DATE_TIME: Short = 3
    const val FORMAT_TYPE_DATE_ALL: Short = 4
    private val formats: Map<String, SoftReference<Array<DateFormat>>> = ConcurrentHashMap<String, SoftReference<Array<DateFormat>>>()
    fun getDateTimeFormats(locale: Locale, tz: TimeZone, lenient: Boolean): Array<DateFormat?> {
        val id = "dt-" + locale.toString().toString() + "-" + tz.getID().toString() + "-" + lenient
        val tmp: SoftReference<Array<DateFormat>>? = formats[id]
        var df: Array<DateFormat>? = if (tmp == null) null else tmp.get()
        if (df == null) {
            val list: List<DateFormat> = ArrayList<DateFormat>()
            list.add(DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL, locale))
            list.add(DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.LONG, locale))
            list.add(DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.MEDIUM, locale))
            list.add(DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.SHORT, locale))
            list.add(DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.FULL, locale))
            list.add(DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, locale))
            list.add(DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.MEDIUM, locale))
            list.add(DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT, locale))
            list.add(DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.FULL, locale))
            list.add(DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.LONG, locale))
            list.add(DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, locale))
            list.add(DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, locale))
            list.add(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.FULL, locale))
            list.add(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.LONG, locale))
            list.add(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM, locale))
            list.add(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale))
            add24AndRemoveComma(list, locale, true, true)
            addCustom(list, locale, FORMAT_TYPE_DATE_TIME)
            df = list.toArray(arrayOfNulls<DateFormat>(list.size()))
            for (i in df.indices) {
                df!!.get(i).setLenient(lenient)
                df.get(i).setTimeZone(tz)
            }
            formats.put(id, SoftReference<Array<DateFormat>>(df))
        }
        return clone(df)
    }

    fun getDateFormats(locale: Locale, tz: TimeZone, lenient: Boolean): Array<DateFormat?> {
        val id = "d-" + locale.toString().toString() + "-" + tz.getID().toString() + "-" + lenient
        val tmp: SoftReference<Array<DateFormat>>? = formats[id]
        var df: Array<DateFormat>? = if (tmp == null) null else tmp.get()
        if (df == null) {
            val list: List<DateFormat> = ArrayList<DateFormat>()
            list.add(DateFormat.getDateInstance(DateFormat.FULL, locale))
            list.add(DateFormat.getDateInstance(DateFormat.LONG, locale))
            list.add(DateFormat.getDateInstance(DateFormat.MEDIUM, locale))
            list.add(DateFormat.getDateInstance(DateFormat.SHORT, locale))
            add24AndRemoveComma(list, locale, true, false)
            addCustom(list, locale, FORMAT_TYPE_DATE)
            df = list.toArray(arrayOfNulls<DateFormat>(list.size()))
            for (i in df.indices) {
                df!!.get(i).setLenient(lenient)
                df.get(i).setTimeZone(tz)
            }
            formats.put(id, SoftReference<Array<DateFormat>>(df))
        }
        return clone(df)
    }

    private fun clone(src: Array<DateFormat>?): Array<DateFormat?> {
        val trg: Array<DateFormat?> = arrayOfNulls<DateFormat>(src!!.size)
        for (i in src.indices) {
            trg[i] = (src!![i] as SimpleDateFormat).clone() as DateFormat
        }
        return trg
    }

    fun getTimeFormats(locale: Locale, tz: TimeZone, lenient: Boolean): Array<DateFormat?> {
        val id = "t-" + locale.toString().toString() + "-" + tz.getID().toString() + "-" + lenient
        val tmp: SoftReference<Array<DateFormat>>? = formats[id]
        var df: Array<DateFormat>? = if (tmp == null) null else tmp.get()
        if (df == null) {
            val list: List<DateFormat> = ArrayList<DateFormat>()
            list.add(DateFormat.getTimeInstance(DateFormat.FULL, locale))
            list.add(DateFormat.getTimeInstance(DateFormat.LONG, locale))
            list.add(DateFormat.getTimeInstance(DateFormat.MEDIUM, locale))
            list.add(DateFormat.getTimeInstance(DateFormat.SHORT, locale))
            add24AndRemoveComma(list, locale, false, true)
            addCustom(list, locale, FORMAT_TYPE_TIME)
            df = list.toArray(arrayOfNulls<DateFormat>(list.size()))
            for (i in df.indices) {
                df!!.get(i).setLenient(lenient)
                df.get(i).setTimeZone(tz)
            }
            formats.put(id, SoftReference<Array<DateFormat>>(df))
        }
        return clone(df)
    }

    private fun add24AndRemoveComma(list: List<DateFormat>, locale: Locale, isDate: Boolean, isTime: Boolean) {
        val df: Array<DateFormat> = list.toArray(arrayOfNulls<DateFormat>(list.size()))
        for (i in df.indices) {
            if (df[i] is SimpleDateFormat) {
                add24AndRemoveComma(list, df[i] as SimpleDateFormat, locale, isDate, isTime)
            }
        }
    }

    private fun add24AndRemoveComma(list: List<DateFormat>, sdf: SimpleDateFormat, locale: Locale, isDate: Boolean, isTime: Boolean) {
        val p: String
        val results: List<SimpleDateFormat> = ArrayList<SimpleDateFormat>()
        p = sdf.toPattern().toString() + ""
        // print.e("----- "+p);
        if (isDate && isTime) {
            if (check(results, p, locale, " 'um' ", " ")) {
            }
            if (check(results, p, locale, " 'à' ", " ")) {
            }
            if (check(results, p, locale, " 'at' ", " ")) {
            }
            if (check(results, p, locale, " 'de' ", " ")) {
            }
        }
        if (isTime) {
            if (check(results, p, locale, "hh:mm:ss a", "HH:mm:ss")) {
            } else if (check(results, p, locale, "h:mm:ss a", "H:mm:ss")) {
            } else if (check(results, p, locale, "hh:mm a", "HH:mm")) {
            } else if (check(results, p, locale, "h:mm a", "H:mm")) {
            } else if (check(results, p, locale, "hh:mm:ssa", "HH:mm:ss")) {
            } else if (check(results, p, locale, "h:mm:ssa", "H:mm:ss")) {
            } else if (check(results, p, locale, "hh:mma", "HH:mm")) {
            } else if (check(results, p, locale, "h:mma", "H:mm")) {
            }
        }
        if (isDate) {
            if (check(results, p, locale, "y,", "y")) {
            }
            if (check(results, p, locale, "d MMMM ", "d. MMMM ")) {
            }
            if (check(results, p, locale, "d MMM y", "d-MMM-y")) {
            }
        }
        if (results.size() > 0) {
            val it: Iterator<SimpleDateFormat> = results.iterator()
            var _sdf: SimpleDateFormat
            while (it.hasNext()) {
                _sdf = it.next()
                if (!list.contains(_sdf)) {
                    list.add(_sdf)
                    add24AndRemoveComma(list, _sdf, locale, isDate, isTime)
                }
            }
        }
    }

    private fun check(results: List<SimpleDateFormat>, orgPattern: String, locale: Locale, from: String, to: String): Boolean {
        val index: Int = orgPattern.indexOf(from)
        if (index != -1) {
            val p: String = StringUtil.replace(orgPattern, from, to, true)
            val sdf = SimpleDateFormat(p, locale)
            results.add(sdf)
            return true
        }
        return false
    }

    private fun addCustom(list: List<DateFormat>, locale: Locale, formatType: Short) {
        // get custom formats from file
        val config: Config = ThreadLocalPageContext.getConfig()
        val dir: Resource? = if (config != null) config.getConfigDir().getRealResource("locales") else null
        if (dir != null && dir.isDirectory()) {
            var appendix = "-datetime"
            if (formatType == FORMAT_TYPE_DATE) appendix = "-date"
            if (formatType == FORMAT_TYPE_TIME) appendix = "-time"
            val file: Resource = dir.getRealResource(locale.getLanguage().toString() + "-" + locale.getCountry() + appendix + ".df")
            if (file.isFile()) {
                try {
                    val content: String = IOUtil.toString(file, null as Charset?)
                    val arr: Array<String?> = lucee.runtime.type.util.ListUtil.listToStringArray(content, '\n')!!
                    var line: String?
                    var sdf: SimpleDateFormat?
                    for (i in arr.indices) {
                        line = arr[i].trim()
                        if (StringUtil.isEmpty(line)) continue
                        sdf = SimpleDateFormat(line, locale)
                        if (!list.contains(sdf)) list.add(sdf)
                    }
                } catch (t: Throwable) {
                    ExceptionUtil.rethrowIfNecessary(t)
                }
            }
        }
    }

    /**
     * CFML Supported LS Formats
     *
     * @param locale
     * @param tz
     * @param lenient
     * @return
     */
    fun getCFMLFormats(tz: TimeZone, lenient: Boolean): Array<DateFormat?> {
        val id = "cfml-" + Locale.ENGLISH.toString().toString() + "-" + tz.getID().toString() + "-" + lenient
        val tmp: SoftReference<Array<DateFormat>>? = formats[id]
        var df: Array<DateFormat>? = if (tmp == null) null else tmp.get()
        if (df == null) {
            df = arrayOf<SimpleDateFormat>(SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH), SimpleDateFormat("MMMM dd, yyyy HH:mm:ss a zzz", Locale.ENGLISH),
                    SimpleDateFormat("MMM dd, yyyy HH:mm:ss a", Locale.ENGLISH), SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.ENGLISH),
                    SimpleDateFormat("MMMM d yyyy HH:mm:ssZ", Locale.ENGLISH), SimpleDateFormat("MMMM d yyyy HH:mm:ss", Locale.ENGLISH),
                    SimpleDateFormat("MMMM d yyyy HH:mm", Locale.ENGLISH), SimpleDateFormat("EEE, MMM dd, yyyy HH:mm:ssZ", Locale.ENGLISH),
                    SimpleDateFormat("EEE, MMM dd, yyyy HH:mm:ss", Locale.ENGLISH), SimpleDateFormat("EEEE, MMMM dd, yyyy H:mm:ss a zzz", Locale.ENGLISH),
                    SimpleDateFormat("dd-MMM-yy HH:mm a", Locale.ENGLISH), SimpleDateFormat("dd-MMMM-yy HH:mm a", Locale.ENGLISH),
                    SimpleDateFormat("EE, dd-MMM-yyyy HH:mm:ss zz", Locale.ENGLISH), SimpleDateFormat("EE, dd MMM yyyy HH:mm:ss zz", Locale.ENGLISH),
                    SimpleDateFormat("EEE d, MMM yyyy HH:mm:ss zz", Locale.ENGLISH), SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH),
                    SimpleDateFormat("MMMM, dd yyyy HH:mm:ssZ", Locale.ENGLISH), SimpleDateFormat("MMMM, dd yyyy HH:mm:ss", Locale.ENGLISH),
                    SimpleDateFormat("yyyy/MM/dd HH:mm:ss zz", Locale.ENGLISH), SimpleDateFormat("dd MMM yyyy HH:mm:ss zz", Locale.ENGLISH),
                    SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss 'GMT'ZZ (z)", Locale.ENGLISH), SimpleDateFormat("dd MMM, yyyy HH:mm:ss", Locale.ENGLISH) // ,new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss",Locale.ENGLISH)
            )
            for (i in df.indices) {
                df!!.get(i).setLenient(lenient)
                df.get(i).setTimeZone(tz)
            }
            formats.put(id, SoftReference<Array<DateFormat>>(df))
        }
        return clone(df)
    }

    fun getFormats(locale: Locale, tz: TimeZone?, lenient: Boolean, formatType: Short): Array<DateFormat?> {
        if (FORMAT_TYPE_DATE_TIME == formatType) return getDateTimeFormats(locale, TimeZoneConstants.GMT, true)
        if (FORMAT_TYPE_DATE == formatType) return getDateFormats(locale, TimeZoneConstants.GMT, true)
        if (FORMAT_TYPE_TIME == formatType) return getTimeFormats(locale, TimeZoneConstants.GMT, true)
        val dt: Array<DateFormat?> = getDateTimeFormats(locale, TimeZoneConstants.GMT, true)
        val d: Array<DateFormat?> = getDateFormats(locale, TimeZoneConstants.GMT, true)
        val t: Array<DateFormat?> = getTimeFormats(locale, TimeZoneConstants.GMT, true)
        val all: Array<DateFormat?> = arrayOfNulls<DateFormat>(dt.size + d.size + t.size)
        for (i in dt.indices) {
            all[i] = dt[i]
        }
        for (i in d.indices) {
            all[i + dt.size] = d[i]
        }
        for (i in t.indices) {
            all[i + dt.size + d.size] = t[i]
        }
        return getDateTimeFormats(locale, TimeZoneConstants.GMT, true)
    }

    fun getSupportedPatterns(locale: Locale, formatType: Short): Array<String?>? {
        val _formats: Array<DateFormat?> = getFormats(locale, TimeZoneConstants.GMT, true, formatType)
        val patterns = arrayOfNulls<String>(_formats.size)
        for (i in _formats.indices) {
            if (_formats[i] !is SimpleDateFormat) return null // all or nothing
            patterns[i] = (_formats[i] as SimpleDateFormat?).toPattern()
        }
        return patterns
    }

    fun getDateFormat(locale: Locale?, tz: TimeZone?, mask: String): DateFormat {
        val df: DateFormat
        if (mask.equalsIgnoreCase("short")) df = DateFormat.getDateInstance(DateFormat.SHORT, locale) else if (mask.equalsIgnoreCase("medium")) df = DateFormat.getDateInstance(DateFormat.MEDIUM, locale) else if (mask.equalsIgnoreCase("long")) df = DateFormat.getDateInstance(DateFormat.LONG, locale) else if (mask.equalsIgnoreCase("full")) df = DateFormat.getDateInstance(DateFormat.FULL, locale) else {
            df = SimpleDateFormat(mask, locale)
        }
        df.setTimeZone(tz)
        return df
    }

    fun getTimeFormat(locale: Locale?, tz: TimeZone?, mask: String): DateFormat {
        val df: DateFormat
        if (mask.equalsIgnoreCase("short")) df = DateFormat.getTimeInstance(DateFormat.SHORT, locale) else if (mask.equalsIgnoreCase("medium")) df = DateFormat.getTimeInstance(DateFormat.MEDIUM, locale) else if (mask.equalsIgnoreCase("long")) df = DateFormat.getTimeInstance(DateFormat.LONG, locale) else if (mask.equalsIgnoreCase("full")) df = DateFormat.getTimeInstance(DateFormat.FULL, locale) else {
            df = SimpleDateFormat(mask, locale)
        }
        df.setTimeZone(tz)
        return df
    }

    fun getDateTimeFormat(locale: Locale?, tz: TimeZone?, mask: String): DateFormat {
        val df: DateFormat
        if (mask.equalsIgnoreCase("short")) df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale) else if (mask.equalsIgnoreCase("medium")) df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, locale) else if (mask.equalsIgnoreCase("long")) df = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, locale) else if (mask.equalsIgnoreCase("full")) df = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL, locale) else if (mask.equalsIgnoreCase("iso8601")) df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ") else {
            df = SimpleDateFormat(mask, locale)
        }
        df.setTimeZone(tz)
        return df

        /*
		 * if(mask!=null && StringUtil.indexOfIgnoreCase(mask, "tt")==-1 &&
		 * StringUtil.indexOfIgnoreCase(mask, "t")!=-1) { DateFormatSymbols dfs = new
		 * DateFormatSymbols(locale); dfs.setAmPmStrings(AP); sdf.setDateFormatSymbols(dfs); }
		 */
    }
}