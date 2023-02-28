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
package tachyon.commons.date

import java.util.HashMap

object TimeZoneUtil {
    private val IDS: Map<String, Object> = HashMap<String, Object>()
    private val dn: Map<String, TimeZone> = HashMap<String, TimeZone>()
    private operator fun set(name: String, ID: String) {
        var name = name
        if (StringUtil.isEmpty(ID)) return
        name = StringUtil.replace(name.trim().toLowerCase(), " ", "", false)
        IDS.put(name.toLowerCase(), ID)
    }

    /**
     * return the string format of the Timezone
     *
     * @param timezone
     * @return
     */
    fun toString(timezone: TimeZone): String {
        return timezone.getID()
    }

    private val supportedTimeZonesAsString: String
        private get() = ListUtil.arrayToList(TimeZone.getAvailableIDs(), ", ")

    private fun getTimeZoneFromIDS(strTimezone: String): TimeZone? {
        val obj: Object = IDS[strTimezone] ?: return null
        if (obj is String) {
            val tz: TimeZone = TimeZone.getTimeZone(obj as String)
            IDS.put(strTimezone, tz)
            return tz
        }
        return obj as TimeZone
    }

    /**
     * translate timezone string format to a timezone
     *
     * @param strTimezoneTrimmed
     * @return
     */
    fun toTimeZone(strTimezone: String?, defaultValue: TimeZone?): TimeZone? {
        if (strTimezone == null) return defaultValue
        var strTimezoneTrimmed: String = StringUtil.replace(strTimezone.trim().toLowerCase(), " ", "", false)
        var tz: TimeZone? = getTimeZoneFromIDS(strTimezoneTrimmed)
        if (tz != null) return tz

        // parse GMT followd by a number
        var gmtOffset = Float.NaN
        if (strTimezoneTrimmed.startsWith("gmt")) gmtOffset = getGMTOffset(strTimezoneTrimmed.substring(3).trim(), Float.NaN) else if (strTimezoneTrimmed.startsWith("etc/gmt")) gmtOffset = getGMTOffset(strTimezoneTrimmed.substring(7).trim(), Float.NaN) else if (strTimezoneTrimmed.startsWith("utc")) gmtOffset = getGMTOffset(strTimezoneTrimmed.substring(3).trim(), Float.NaN) else if (strTimezoneTrimmed.startsWith("etc/utc")) gmtOffset = getGMTOffset(strTimezoneTrimmed.substring(7).trim(), Float.NaN)
        if (!Float.isNaN(gmtOffset)) {
            strTimezoneTrimmed = "etc/gmt" + (if (gmtOffset >= 0) "+" else "") + Caster.toString(gmtOffset)
            tz = getTimeZoneFromIDS(strTimezoneTrimmed)
            if (tz != null) return tz
        }

        // display name in all variations
        if (!StringUtil.isEmpty(strTimezoneTrimmed)) {
            tz = dn[strTimezoneTrimmed]
            if (tz != null) return tz
            val it: Iterator<Object> = IDS.values().iterator()
            var o: Object
            while (it.hasNext()) {
                o = it.next()
                if (o is TimeZone) {
                    tz = o as TimeZone
                    if (strTimezone.equalsIgnoreCase(tz.getDisplayName(true, TimeZone.SHORT, Locale.US))
                            || strTimezone.equalsIgnoreCase(tz.getDisplayName(false, TimeZone.SHORT, Locale.US))
                            || strTimezone.equalsIgnoreCase(tz.getDisplayName(true, TimeZone.LONG, Locale.US))
                            || strTimezone.equalsIgnoreCase(tz.getDisplayName(false, TimeZone.LONG, Locale.US))) {
                        dn.put(strTimezoneTrimmed, tz)
                        return tz
                    }
                }
            }
        }
        return defaultValue
    }

    private fun getGMTOffset(str: String, defaultValue: Float): Float {
        var index: Int
        var left: String? = null
        var right: String? = null
        if (str.indexOf(':').also { index = it } != -1) {
            left = str.substring(0, index)
            right = str.substring(index + 1)
        } else if (str.startsWith("-")) {
            if (str.length() >= 4 && str.indexOf('.') === -1) {
                left = str.substring(0, str.length() - 2)
                right = str.substring(str.length() - 2)
            }
        } else if (str.length() >= 3 && str.indexOf('.') === -1) {
            left = str.substring(0, str.length() - 2)
            right = str.substring(str.length() - 2)
        }
        if (left != null) {
            val l: Int = Caster.toIntValue(left, Integer.MIN_VALUE)
            val r: Int = Caster.toIntValue(right, Integer.MIN_VALUE)
            return if (l == Integer.MIN_VALUE || r == Integer.MIN_VALUE || r > 59) defaultValue else l + r / 60f
        }
        val f: Float = Caster.toFloatValue(str, Float.NaN)
        return if (Float.isNaN(f)) defaultValue else f
    }

    @Throws(ExpressionException::class)
    fun toTimeZone(strTimezone: String): TimeZone {
        val tz: TimeZone? = toTimeZone(strTimezone, null)
        if (tz != null) return tz
        throw ExpressionException("Can't cast value [$strTimezone] to a TimeZone", "supported TimeZones are: [" + supportedTimeZonesAsString + "]")
    }

    init {
        val ids: Array<String> = TimeZone.getAvailableIDs()
        for (i in tachyon.commons.date.ids.indices) {
            IDS.put(tachyon.commons.date.ids.get(i).toLowerCase(), TimeZone.getTimeZone(tachyon.commons.date.ids.get(i)))
        }
        val def: TimeZone = TimeZone.getDefault()
        if (tachyon.commons.date.def != null) IDS.put(tachyon.commons.date.def.getID(), tachyon.commons.date.def)
        IDS.put("jvm", TimeZone.getDefault())
        IDS.put("default", TimeZone.getDefault())
        IDS.put("", TimeZone.getDefault())

        // MS specific Timezone definitions
        TimeZoneUtil["Dateline Standard Time"] = "Etc/GMT+12"
        TimeZoneUtil["Samoa Standard Time"] = "Pacific/Midway"
        TimeZoneUtil["Hawaiian Standard Time"] = "HST"
        TimeZoneUtil["Alaskan Standard Time"] = "AST"
        TimeZoneUtil["Pacific Standard Time"] = "PST"
        TimeZoneUtil["Mountain Standard Time"] = "MST"
        TimeZoneUtil["Mexico Standard Time"] = "Mexico/General"
        TimeZoneUtil["Mexico Standard Time 2"] = "America/Chihuahua"
        TimeZoneUtil["U.S. Mountain Standard Time"] = "MST"
        TimeZoneUtil["Central Standard Time"] = "CST"
        TimeZoneUtil["Canada Central Standard Time"] = "Canada/Central"
        TimeZoneUtil["Central America Standard Time"] = "CST"
        TimeZoneUtil["Eastern Standard Time"] = "EST"
        TimeZoneUtil["U.S. Eastern Standard Time"] = "EST"
        TimeZoneUtil["S.A. Pacific Standard Time"] = "America/Bogota"
        TimeZoneUtil["Atlantic Standard Time"] = "Canada/Atlantic"
        TimeZoneUtil["S.A. Western Standard Time"] = "America/Antigua"
        TimeZoneUtil["Pacific S.A. Standard Time"] = "America/Santiago"
        TimeZoneUtil["Newfoundland and Labrador Standard Time"] = "CNT"
        TimeZoneUtil["E. South America Standard Time"] = "BET"
        TimeZoneUtil["S.A. Eastern Standard Time"] = "America/Argentina/Buenos_Aires"
        TimeZoneUtil["Greenland Standard Time"] = "America/Godthab"
        TimeZoneUtil["Mid-Atlantic Standard Time"] = "America/Noronha"
        TimeZoneUtil["Azores Standard Time"] = "Atlantic/Azores"
        TimeZoneUtil["Cape Verde Standard Time"] = "Atlantic/Cape_Verde"
        TimeZoneUtil["Central Europe Standard Time"] = "CET"
        TimeZoneUtil["Central European Standard Time"] = "CET"
        TimeZoneUtil["Romance Standard Time"] = "Europe/Brussels"
        TimeZoneUtil["W. Europe Standard Time"] = "CET"
        TimeZoneUtil["E. Europe Standard Time"] = "ART"
        TimeZoneUtil["Egypt Standard Time"] = "Egypt"
        TimeZoneUtil["FLE Standard Time"] = "EET"
        TimeZoneUtil["GTB Standard Time"] = "Europe/Athens"
        TimeZoneUtil["Israel Standard Time"] = "Asia/Jerusalem"
        TimeZoneUtil["South Africa Standard Time"] = "Africa/Johannesburg"
        TimeZoneUtil["Russian Standard Time"] = "Europe/Moscow"
        TimeZoneUtil["Arab Standard Time"] = "Asia/Kuwait"
        TimeZoneUtil["E. Africa Standard Time"] = "Africa/Nairobi"
        TimeZoneUtil["Arabic Standard Time"] = "Asia/Baghdad"
        TimeZoneUtil["Iran Standard Time"] = "Asia/Tehran"
        TimeZoneUtil["Arabian Standard Time"] = "Asia/Muscat"
        TimeZoneUtil["Caucasus Standard Time"] = "Asia/Yerevan"
        TimeZoneUtil["Transitional Islamic State of Afghanistan Standard Time"] = "Asia/Kabul"
        TimeZoneUtil["Ekaterinburg Standard Time"] = "Asia/Yekaterinburg"
        TimeZoneUtil["West Asia Standard Time"] = "Asia/Karachi"
        TimeZoneUtil["India Standard Time"] = "IST"
        TimeZoneUtil["Nepal Standard Time"] = "Asia/Katmandu"
        TimeZoneUtil["Central Asia Standard Time"] = "Asia/Dhaka"
        TimeZoneUtil["Sri Lanka Standard Time"] = "Asia/Colombo"
        TimeZoneUtil["N. Central Asia Standard Time"] = "Asia/Almaty"
        TimeZoneUtil["Myanmar Standard Time"] = "Asia/Rangoon"
        TimeZoneUtil["S.E. Asia Standard Time"] = "Asia/Bangkok"
        TimeZoneUtil["North Asia Standard Time"] = "Asia/Krasnoyarsk"
        TimeZoneUtil["China Standard Time"] = "CTT"
        TimeZoneUtil["Singapore Standard Time"] = "Asia/Singapore"
        TimeZoneUtil["Taipei Standard Time"] = "Asia/Taipei"
        TimeZoneUtil["W. Australia Standard Time"] = "Australia/Perth"
        TimeZoneUtil["North Asia East Standard Time"] = "Asia/Irkutsk"
        TimeZoneUtil["Korea Standard Time"] = "Asia/Seoul"
        TimeZoneUtil["Tokyo Standard Time"] = "Asia/Tokyo"
        TimeZoneUtil["Yakutsk Standard Time"] = "Asia/Yakutsk"
        TimeZoneUtil["A.U.S. Central Standard Time"] = "ACT"
        TimeZoneUtil["Cen. Australia Standard Time"] = "ACT"
        TimeZoneUtil["A.U.S. Eastern Standard Time"] = "AET"
        TimeZoneUtil["E. Australia Standard Time"] = "AET"
        TimeZoneUtil["Tasmania Standard Time"] = "Australia/Tasmania"
        TimeZoneUtil["Vladivostok Standard Time"] = "Asia/Vladivostok"
        TimeZoneUtil["West Pacific Standard Time"] = "Pacific/Guam"
        TimeZoneUtil["Central Pacific Standard Time"] = "Asia/Magadan"
        TimeZoneUtil["Fiji Islands Standard Time"] = "Pacific/Fiji"
        TimeZoneUtil["New Zealand Standard Time"] = "NZ"
        TimeZoneUtil["Tonga Standard Time"] = "Pacific/Tongatapu"
        TimeZoneUtil["CEST"] = "CET"
        TimeZoneUtil["ACDT"] = "ACT"
        TimeZoneUtil["ACST"] = "Australia/Eucla"
        TimeZoneUtil["ACST"] = "Australia/Tasmania"
        TimeZoneUtil["AEST"] = "Australia/Queensland"
        TimeZoneUtil["ET"] = "US/Eastern"
        TimeZoneUtil["EDT"] = "US/Eastern"
        TimeZoneUtil["EST"] = "US/Eastern"
        TimeZoneUtil["MT"] = "US/Mountain"
        TimeZoneUtil["MST"] = "US/Mountain"
        TimeZoneUtil["MDT"] = "US/Mountain"
        TimeZoneUtil["CT"] = "US/Central"
        TimeZoneUtil["CST"] = "US/Central"
        TimeZoneUtil["CDT"] = "US/Central"
        TimeZoneUtil["PT"] = "US/Pacific"
        TimeZoneUtil["PST"] = "US/Pacific"
        TimeZoneUtil["PDT"] = "US/Pacific"
    }
}