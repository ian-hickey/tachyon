/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Tachyon Assosication Switzerland
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
package tachyon.runtime.op

import java.awt.image.BufferedImage

/**
 * This class can cast object of one type to another by CFML rules
 */
object Caster {
    // static Map calendarsMap=new ReferenceMap(ReferenceMap.SOFT,ReferenceMap.SOFT);
    private const val NUMBERS_MIN = 0
    private const val NUMBERS_MAX = 999
    private val NUMBERS = arrayOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23",
            "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51",
            "52", "53", "54", "55", "56", "57", "58", "59", "60", "61", "62", "63", "64", "65", "66", "67", "68", "69", "70", "71", "72", "73", "74", "75", "76", "77", "78", "79",
            "80", "81", "82", "83", "84", "85", "86", "87", "88", "89", "90", "91", "92", "93", "94", "95", "96", "97", "98", "99", "100", "101", "102", "103", "104", "105", "106",
            "107", "108", "109", "110", "111", "112", "113", "114", "115", "116", "117", "118", "119", "120", "121", "122", "123", "124", "125", "126", "127", "128", "129", "130",
            "131", "132", "133", "134", "135", "136", "137", "138", "139", "140", "141", "142", "143", "144", "145", "146", "147", "148", "149", "150", "151", "152", "153", "154",
            "155", "156", "157", "158", "159", "160", "161", "162", "163", "164", "165", "166", "167", "168", "169", "170", "171", "172", "173", "174", "175", "176", "177", "178",
            "179", "180", "181", "182", "183", "184", "185", "186", "187", "188", "189", "190", "191", "192", "193", "194", "195", "196", "197", "198", "199", "200", "201", "202",
            "203", "204", "205", "206", "207", "208", "209", "210", "211", "212", "213", "214", "215", "216", "217", "218", "219", "220", "221", "222", "223", "224", "225", "226",
            "227", "228", "229", "230", "231", "232", "233", "234", "235", "236", "237", "238", "239", "240", "241", "242", "243", "244", "245", "246", "247", "248", "249", "250",
            "251", "252", "253", "254", "255", "256", "257", "258", "259", "260", "261", "262", "263", "264", "265", "266", "267", "268", "269", "270", "271", "272", "273", "274",
            "275", "276", "277", "278", "279", "280", "281", "282", "283", "284", "285", "286", "287", "288", "289", "290", "291", "292", "293", "294", "295", "296", "297", "298",
            "299", "300", "301", "302", "303", "304", "305", "306", "307", "308", "309", "310", "311", "312", "313", "314", "315", "316", "317", "318", "319", "320", "321", "322",
            "323", "324", "325", "326", "327", "328", "329", "330", "331", "332", "333", "334", "335", "336", "337", "338", "339", "340", "341", "342", "343", "344", "345", "346",
            "347", "348", "349", "350", "351", "352", "353", "354", "355", "356", "357", "358", "359", "360", "361", "362", "363", "364", "365", "366", "367", "368", "369", "370",
            "371", "372", "373", "374", "375", "376", "377", "378", "379", "380", "381", "382", "383", "384", "385", "386", "387", "388", "389", "390", "391", "392", "393", "394",
            "395", "396", "397", "398", "399", "400", "401", "402", "403", "404", "405", "406", "407", "408", "409", "410", "411", "412", "413", "414", "415", "416", "417", "418",
            "419", "420", "421", "422", "423", "424", "425", "426", "427", "428", "429", "430", "431", "432", "433", "434", "435", "436", "437", "438", "439", "440", "441", "442",
            "443", "444", "445", "446", "447", "448", "449", "450", "451", "452", "453", "454", "455", "456", "457", "458", "459", "460", "461", "462", "463", "464", "465", "466",
            "467", "468", "469", "470", "471", "472", "473", "474", "475", "476", "477", "478", "479", "480", "481", "482", "483", "484", "485", "486", "487", "488", "489", "490",
            "491", "492", "493", "494", "495", "496", "497", "498", "499", "500", "501", "502", "503", "504", "505", "506", "507", "508", "509", "510", "511", "512", "513", "514",
            "515", "516", "517", "518", "519", "520", "521", "522", "523", "524", "525", "526", "527", "528", "529", "530", "531", "532", "533", "534", "535", "536", "537", "538",
            "539", "540", "541", "542", "543", "544", "545", "546", "547", "548", "549", "550", "551", "552", "553", "554", "555", "556", "557", "558", "559", "560", "561", "562",
            "563", "564", "565", "566", "567", "568", "569", "570", "571", "572", "573", "574", "575", "576", "577", "578", "579", "580", "581", "582", "583", "584", "585", "586",
            "587", "588", "589", "590", "591", "592", "593", "594", "595", "596", "597", "598", "599", "600", "601", "602", "603", "604", "605", "606", "607", "608", "609", "610",
            "611", "612", "613", "614", "615", "616", "617", "618", "619", "620", "621", "622", "623", "624", "625", "626", "627", "628", "629", "630", "631", "632", "633", "634",
            "635", "636", "637", "638", "639", "640", "641", "642", "643", "644", "645", "646", "647", "648", "649", "650", "651", "652", "653", "654", "655", "656", "657", "658",
            "659", "660", "661", "662", "663", "664", "665", "666", "667", "668", "669", "670", "671", "672", "673", "674", "675", "676", "677", "678", "679", "680", "681", "682",
            "683", "684", "685", "686", "687", "688", "689", "690", "691", "692", "693", "694", "695", "696", "697", "698", "699", "700", "701", "702", "703", "704", "705", "706",
            "707", "708", "709", "710", "711", "712", "713", "714", "715", "716", "717", "718", "719", "720", "721", "722", "723", "724", "725", "726", "727", "728", "729", "730",
            "731", "732", "733", "734", "735", "736", "737", "738", "739", "740", "741", "742", "743", "744", "745", "746", "747", "748", "749", "750", "751", "752", "753", "754",
            "755", "756", "757", "758", "759", "760", "761", "762", "763", "764", "765", "766", "767", "768", "769", "770", "771", "772", "773", "774", "775", "776", "777", "778",
            "779", "780", "781", "782", "783", "784", "785", "786", "787", "788", "789", "790", "791", "792", "793", "794", "795", "796", "797", "798", "799", "800", "801", "802",
            "803", "804", "805", "806", "807", "808", "809", "810", "811", "812", "813", "814", "815", "816", "817", "818", "819", "820", "821", "822", "823", "824", "825", "826",
            "827", "828", "829", "830", "831", "832", "833", "834", "835", "836", "837", "838", "839", "840", "841", "842", "843", "844", "845", "846", "847", "848", "849", "850",
            "851", "852", "853", "854", "855", "856", "857", "858", "859", "860", "861", "862", "863", "864", "865", "866", "867", "868", "869", "870", "871", "872", "873", "874",
            "875", "876", "877", "878", "879", "880", "881", "882", "883", "884", "885", "886", "887", "888", "889", "890", "891", "892", "893", "894", "895", "896", "897", "898",
            "899", "900", "901", "902", "903", "904", "905", "906", "907", "908", "909", "910", "911", "912", "913", "914", "915", "916", "917", "918", "919", "920", "921", "922",
            "923", "924", "925", "926", "927", "928", "929", "930", "931", "932", "933", "934", "935", "936", "937", "938", "939", "940", "941", "942", "943", "944", "945", "946",
            "947", "948", "949", "950", "951", "952", "953", "954", "955", "956", "957", "958", "959", "960", "961", "962", "963", "964", "965", "966", "967", "968", "969", "970",
            "971", "972", "973", "974", "975", "976", "977", "978", "979", "980", "981", "982", "983", "984", "985", "986", "987", "988", "989", "990", "991", "992", "993", "994",
            "995", "996", "997", "998", "999")

    /**
     * cast a boolean value to a boolean value (do nothing)
     *
     * @param b boolean value to cast
     * @return casted boolean value
     */
    fun toBooleanValue(b: Boolean): Boolean {
        return b
    }

    /**
     * cast an int value to a boolean value (primitive value type)
     *
     * @param i int value to cast
     * @return casted boolean value
     */
    fun toBooleanValue(i: Int): Boolean {
        return i != 0
    }

    /**
     * cast a long value to a boolean value (primitive value type)
     *
     * @param l long value to cast
     * @return casted boolean value
     */
    fun toBooleanValue(l: Long): Boolean {
        return l != 0L
    }

    /**
     * cast a double value to a boolean value (primitive value type)
     *
     * @param d double value to cast
     * @return casted boolean value
     */
    fun toBooleanValue(d: Double): Boolean {
        return d != 0.0
    }

    fun toBooleanValue(n: Number): Boolean {
        return n.intValue() !== 0
    }

    /**
     * cast a double value to a boolean value (primitive value type)
     *
     * @param c char value to cast
     * @return casted boolean value
     */
    fun toBooleanValue(c: Char): Boolean {
        return c.toInt() != 0
    }

    /**
     * cast an Object to a boolean value (primitive value type)
     *
     * @param o Object to cast
     * @return casted boolean value
     * @throws PageException
     */
    @Throws(PageException::class)
    fun toBooleanValue(o: Object?): Boolean {
        if (o is Boolean) return (o as Boolean?).booleanValue() else if (o is Number) return toBooleanValue((o as Number?).doubleValue()) else if (o is String) return toBooleanValue(o as String?) else if (o is Castable) return (o as Castable?).castToBooleanValue() else if (o == null) return toBooleanValue("") else if (o is ObjectWrap) return toBooleanValue((o as ObjectWrap).getEmbededObject())
        throw CasterException(o, "boolean")
    }

    /**
     * tranlate a Boolean object to a boolean value
     *
     * @param b
     * @return
     */
    fun toBooleanValue(b: Boolean): Boolean {
        return b.booleanValue()
    }

    /**
     * cast an Object to a boolean value (primitive value type)
     *
     * @param str String to cast
     * @return casted boolean value
     * @throws PageException
     */
    @Throws(PageException::class)
    fun toBooleanValue(str: String?): Boolean {
        val b = toBoolean(str, null)
        if (b != null) return b.booleanValue()
        throw CasterException("Can't cast String [" + CasterException.crop(str).toString() + "] to a boolean")
    }

    fun toBoolean(str: String?, defaultValue: Boolean): Boolean {
        if (str == null) return defaultValue
        val i = stringToBooleanValueEL(str)
        if (i != -1) return if (i == 1) Boolean.TRUE else Boolean.FALSE
        val d = toDoubleValue(str, Double.NaN)
        return if (!Double.isNaN(d)) toBoolean(d) else defaultValue
    }

    /**
     * cast an Object to a Double Object (reference Type)
     *
     * @param f Object to cast
     * @return casted Double Object
     * @throws PageException
     */
    fun toDouble(f: Float): Double {
        return Double.valueOf(f)
    }

    fun toDouble(f: Float): Double {
        return Double.valueOf(f.doubleValue())
    }

    /**
     * cast an Object to a Double Object (reference Type)
     *
     * @param o Object to cast
     * @return casted Double Object
     * @throws PageException
     */
    @Throws(PageException::class)
    fun toDouble(o: Object?): Double? {
        return if (o is Double) o else Double.valueOf(toDoubleValue(o))
    }

    @Throws(PageException::class)
    fun toNumber(pc: PageContext?, o: Object?): Number? {
        if (o is Number) return o
        return if (AppListenerUtil.getPreciseMath(pc, null)) toBigDecimal(o) else Double.valueOf(toDoubleValue(o))
    }

    @Throws(PageException::class)
    fun toNumber(o: Object?): Number? {
        if (o is Number) return o
        return if (AppListenerUtil.getPreciseMath(null, null)) toBigDecimal(o) else Double.valueOf(toDoubleValue(o))
    }

    @Throws(PageException::class)
    fun toNumber(b: Boolean): Number {
        return if (AppListenerUtil.getPreciseMath(null, null)) if (b) BigDecimal.ONE else BigDecimal.ZERO else Double.valueOf(if (b) 1.0 else 0.0)
    }

    @Throws(PageException::class)
    fun toNumber(d: Double): Number {
        return if (AppListenerUtil.getPreciseMath(null, null)) BigDecimal.valueOf(d) else Double.valueOf(d)
    }

    @Throws(PageException::class)
    fun toNumber(str: String?): Number {
        return if (AppListenerUtil.getPreciseMath(null, null)) toBigDecimal(str) else toDouble(str)
    }

    @Throws(PageException::class)
    fun toNumber(pc: PageContext?, str: String?): Number {
        return if (AppListenerUtil.getPreciseMath(pc, null)) toBigDecimal(str) else toDouble(str)
    }

    /**
     * cast an Object to a Double Object (reference Type)
     *
     * @param str string to cast
     * @return casted Double Object
     * @throws PageException
     */
    @Throws(PageException::class)
    fun toDouble(str: String?): Double {
        return Double.valueOf(toDoubleValue(str))
    }

    /**
     * cast an Object to a Double Object (reference Type)
     *
     * @param o Object to cast
     * @param defaultValue
     * @return casted Double Object
     */
    fun toDouble(o: Object, defaultValue: Double): Double {
        if (o is Double) return o
        val dbl: Double = toDoubleValue(o, true, Double.NaN)
        return if (Double.isNaN(dbl)) defaultValue else Double.valueOf(dbl)
    }

    /**
     * cast a double value to a Double Object (reference Type)
     *
     * @param d double value to cast
     * @return casted Double Object
     */
    private const val MAX_SMALL_DOUBLE = 10000
    private val smallDoubles = arrayOfNulls<Double>(MAX_SMALL_DOUBLE)
    private val DEFAULT: Object = Object()
    fun toDouble(d: Double): Double? {
        if (d < MAX_SMALL_DOUBLE && d >= 0) {
            var i: Int
            if (d.toInt().also { i = it }.toDouble() == d) return smallDoubles[i]
        }
        return Double.valueOf(d)
    }

    fun toDouble(n: Number): Double {
        return n.doubleValue()
    }

    /**
     * cast a boolean value to a Double Object (reference Type)
     *
     * @param b boolean value to cast
     * @return casted Double Object
     */
    fun toDouble(b: Boolean): Double {
        return Double.valueOf(if (b) 1 else 0)
    }

    fun toDouble(b: Boolean): Double {
        return Double.valueOf(if (b) 1 else 0)
    }

    /**
     * cast an Object to a double value (primitive value Type)
     *
     * @param o Object to cast
     * @return casted double value
     * @throws PageException
     */
    @Throws(PageException::class)
    fun toDoubleValue(o: Object?): Double {
        if (o is Number) {
            return (o as Number?).doubleValue()
        } else if (o is Boolean) return if ((o as Boolean?).booleanValue()) 1 else 0 else if (o is CharSequence) return toDoubleValue(o.toString(), true) else if (o is Castable) return (o as Castable?).castToDoubleValue() else if (o == null) return 0 // toDoubleValue("");
        else if (o is ObjectWrap) return toDoubleValue((o as ObjectWrap).getEmbededObject()) else if (o is Date) return DateTimeUtil.getInstance().toDoubleValue((o as Date).getTime()) else if (o is Calendar) return DateTimeUtil.getInstance().toDoubleValue((o as Calendar).getTimeInMillis()) else if (o is Character) return (o as Character).charValue()
        throw CasterException(o, "number")
    }

    fun toDoubleValue(d: Double?): Double {
        return if (d == null) 0 else d.doubleValue()
    }

    /**
     * cast an Object to a double value (primitive value Type)
     *
     * @param str String to cast
     * @return casted double value
     * @throws CasterException
     */
    @Throws(CasterException::class)
    fun toDoubleValue(str: String?): Double {
        return toDoubleValue(str, true)
    }

    fun toDoubleValue(strNumber: String, radix: Int, alsoFromDate: Boolean, defaultValue: Double): Double {
        var strNumber = strNumber
        var radix = radix
        strNumber = strNumber.trim()
        if (StringUtil.startsWithIgnoreCase(strNumber, "0x")) {
            radix = 16
            strNumber = strNumber.substring(2)
        }
        if (radix == 10) return toDoubleValue(strNumber, 0.0) else if (strNumber.indexOf('.') !== -1 && radix != 10) return defaultValue // throw new CasterException("the radix con only be [dec] for floating point numbers");
        return Integer.parseInt(strNumber, radix)
    }

    @Throws(CasterException::class)
    fun toDoubleValue(str: String?, alsoFromDate: Boolean): Double {
        var str = str ?: return 0
        // throw new CasterException("can't cast empty string to a number value");
        str = str.trim()
        var rtn = 0.0
        // double rtn_=0;
        // double _rtn=0;
        val eCount = 0
        var deep = 1.0
        var pos = 0
        val len: Int = str.length()
        if (len == 0) throw CasterException("can't cast empty string to a number value")
        var curr: Char = str.charAt(pos)
        var isMinus = false
        if (curr == '+') {
            if (len == ++pos) throw CasterException("can't cast [+] string to a number value")
        }
        if (curr == '-') {
            if (len == ++pos) throw CasterException("can't cast [-] string to a number value")
            isMinus = true
        }
        var hasDot = false
        // boolean hasExp=false;
        do {
            curr = str.charAt(pos)
            if (curr < '0') {
                hasDot = if (curr == '.') {
                    if (hasDot) {
                        if (!alsoFromDate) throw CasterException("cannot cast [$str] string to a number value")
                        return toDoubleValueViaDate(str)
                    }
                    true
                } else {
                    if (pos == 0 && Decision.isBoolean(str)) return if (toBooleanValue(str, false)) 1.0 else 0.0
                    if (!alsoFromDate) throw CasterException("cannot cast [$str] string to a number value")
                    return toDoubleValueViaDate(str)
                    // throw new CasterException("can't cast ["+str+"] string to a number value");
                }
            } else if (curr > '9') {
                if (curr == 'e' || curr == 'E') {
                    return try {
                        Double.parseDouble(str)
                    } catch (e: NumberFormatException) {
                        if (!alsoFromDate) throw CasterException("cannot cast [$str] string to a number value")
                        toDoubleValueViaDate(str)
                        // throw new CasterException("can't cast ["+str+"] string to a number value");
                    }
                }
                // else {
                if (pos == 0 && Decision.isBoolean(str)) return if (toBooleanValue(str, false)) 1.0 else 0.0
                if (!alsoFromDate) throw CasterException("cannot cast [$str] string to a number value")
                return toDoubleValueViaDate(str)
                // throw new CasterException("can't cast ["+str+"] string to a number value");
                // }
            } else {
                rtn *= 10.0
                rtn += toDigit(curr).toDouble()
                if (hasDot) {
                    deep *= 10.0
                    if (deep > 1000000000000000000000.0) return Double.parseDouble(str) // patch for LDEV-2654
                }
            }
        } while (++pos < len)
        if (deep > 1) {
            rtn /= deep
        }
        if (isMinus) rtn = -rtn
        if (eCount > 0) for (i in 0 until eCount) rtn *= 10.0
        // print.e("here:"+rtn_);
        return rtn
    }

    @Throws(CasterException::class)
    private fun toDoubleValueViaDate(str: String): Double {
        val date: DateTime = DateCaster.toDateSimple(str, DateCaster.CONVERTING_TYPE_NONE, false, null, null)
                ?: throw CasterException("can't cast [$str] string to a number value") // not advanced here, neo also only support simple
        return date.castToDoubleValue(0)
    }

    private fun toDoubleValueViaDate(str: String, defaultValue: Double): Double {
        val date: DateTime = DateCaster.toDateSimple(str, DateCaster.CONVERTING_TYPE_NONE, false, null, null)
                ?: return defaultValue // not advanced here, neo also only support simple
        return date.castToDoubleValue(0)
    }
    /**
     * cast an Object to a double value (primitive value Type)
     *
     * @param o Object to cast
     * @param defaultValue if can't cast return this value
     * @return casted double value / public static double toDoubleValue(Object o,double defaultValue) {
     * return toDoubleValue(o, true, defaultValue); }
     */
    /**
     * cast an Object to a double value (primitive value Type)
     *
     * @param o Object to cast
     * @param defaultValue if can't cast return this value
     * @return casted double value
     */
    fun toDoubleValue(o: Object, alsoFromDate: Boolean, defaultValue: Double): Double {
        if (o is Number) return (o as Number).doubleValue() else if (o is Boolean) return if ((o as Boolean).booleanValue()) 1 else 0 else if (o is CharSequence) return toDoubleValue(o.toString(), alsoFromDate, defaultValue) else if (o is Castable) {
            return (o as Castable).castToDoubleValue(defaultValue)
        } else if (o is ObjectWrap) return toDoubleValue((o as ObjectWrap).getEmbededObject(Double.valueOf(defaultValue)), true, defaultValue) else if (o is Date) return DateTimeUtil.getInstance().toDoubleValue((o as Date).getTime()) else if (o is Calendar) return DateTimeUtil.getInstance().toDoubleValue((o as Calendar).getTimeInMillis()) else if (o is Character) return (o as Character).charValue()
        return defaultValue
    }

    /**
     * cast an Object to a double value (primitive value Type), if can't return Double.NaN
     *
     * @param str String to cast
     * @param defaultValue if can't cast return this value
     * @return casted double value
     */
    fun toDoubleValue(str: String?, defaultValue: Double): Double {
        return toDoubleValue(str, true, defaultValue)
    }

    fun toDoubleValue(str: String?, alsoFromDate: Boolean, defaultValue: Double): Double {
        var str = str ?: return defaultValue
        str = str.trim()
        val len: Int = str.length()
        if (len == 0) return defaultValue
        var rtn = 0.0
        val eCount = 0
        // double deep=10;
        var deep = 1.0
        var pos = 0
        var curr: Char = str.charAt(pos)
        var isMinus = false
        if (curr == '+') {
            if (len == ++pos) return defaultValue
        } else if (curr == '-') {
            if (len == ++pos) return defaultValue
            isMinus = true
        }
        var hasDot = false
        // boolean hasExp=false;
        do {
            curr = str.charAt(pos)
            if (curr < '0') {
                hasDot = if (curr == '.') {
                    if (hasDot) {
                        return if (!alsoFromDate) defaultValue else toDoubleValueViaDate(str, defaultValue)
                    }
                    true
                } else {
                    if (pos == 0 && Decision.isBoolean(str)) return if (toBooleanValue(str, false)) 1.0 else 0.0
                    return if (!alsoFromDate) defaultValue else toDoubleValueViaDate(str, defaultValue)
                }
            } else if (curr > '9') {
                if (curr == 'e' || curr == 'E') {
                    return try {
                        Double.parseDouble(str)
                    } catch (e: NumberFormatException) {
                        if (!alsoFromDate) defaultValue else toDoubleValueViaDate(str, defaultValue)
                    }
                }
                // else {
                if (pos == 0 && Decision.isBoolean(str)) return if (toBooleanValue(str, false)) 1.0 else 0.0
                return if (!alsoFromDate) defaultValue else toDoubleValueViaDate(str, defaultValue)
                // }
            } else {
                rtn *= 10.0
                rtn += toDigit(curr).toDouble()
                if (hasDot) deep *= 10.0
            }
        } while (++pos < len)
        if (deep > 1) {
            rtn /= deep
        }
        if (isMinus) rtn = -rtn
        if (eCount > 0) for (i in 0 until eCount) rtn *= 10.0
        return rtn
    }

    private fun toDigit(c: Char): Int {
        return c.toInt() - 48
    }

    /**
     * cast a double value to a double value (do nothing)
     *
     * @param d double value to cast
     * @return casted double value
     */
    fun toDoubleValue(d: Double): Double {
        return d
    }

    fun toDoubleValue(n: Number): Double {
        return n.doubleValue()
    }

    fun toDoubleValue(f: Float): Double {
        return f.toDouble()
    }

    fun toDoubleValue(f: Float): Double {
        return f.doubleValue()
    }

    /**
     * cast a boolean value to a double value (primitive value type)
     *
     * @param b boolean value to cast
     * @return casted double value
     */
    fun toDoubleValue(b: Boolean): Double {
        return if (b) 1 else 0
    }

    fun toDoubleValue(b: Boolean): Double {
        return if (b) 1 else 0
    }

    /**
     * cast a char value to a double value (primitive value type)
     *
     * @param c char value to cast
     * @return casted double value
     */
    fun toDoubleValue(c: Char): Double {
        return c.toDouble()
    }

    /**
     * cast an Object to an int value (primitive value type)
     *
     * @param o Object to cast
     * @return casted int value
     * @throws PageException
     */
    @Throws(PageException::class)
    fun toIntValue(o: Object?): Int {
        if (o is Number) return (o as Number?).intValue() else if (o is Boolean) return if ((o as Boolean?).booleanValue()) 1 else 0 else if (o is CharSequence) return toIntValue(o.toString().trim()) else if (o is Character) return (o as Character?).charValue() else if (o is Castable) return (o as Castable?).castToDoubleValue() else if (o is Date) return DateTimeImpl(o as Date?).castToDoubleValue()
        if (o is String) throw ExpressionException("Can't cast String [" + CasterException.crop(o).toString() + "] to a number") else if (o is ObjectWrap) return toIntValue((o as ObjectWrap?).getEmbededObject())
        throw CasterException(o, "number")
    }

    /**
     * cast an Object to an int value (primitive value type)
     *
     * @param o Object to cast
     * @param defaultValue
     * @return casted int value
     */
    fun toIntValue(o: Object, defaultValue: Int): Int {
        if (o is Number) return (o as Number).intValue() else if (o is Boolean) return if ((o as Boolean).booleanValue()) 1 else 0 else if (o is CharSequence) return toIntValue(o.toString().trim(), defaultValue) else if (o is Character) return (o as Character).charValue() else if (o is Castable) {
            return (o as Castable).castToDoubleValue(defaultValue)
        } else if (o is Date) return DateTimeImpl(o as Date).castToDoubleValue() else if (o is ObjectWrap) return toIntValue((o as ObjectWrap).getEmbededObject(Integer.valueOf(defaultValue)), defaultValue)
        return defaultValue
    }

    fun toIntValue(i: Integer?, defaultValue: Int): Int {
        return if (i == null) defaultValue else i.intValue()
    }

    /**
     * cast a String to an int value (primitive value type)
     *
     * @param str String to cast
     * @return casted int value
     * @throws ExpressionException
     */
    @Throws(ExpressionException::class)
    fun toIntValue(str: String?): Int {
        return toDoubleValue(str, false).toInt()
    }

    /**
     * cast an Object to a double value (primitive value Type), if can't return Integer.MIN_VALUE
     *
     * @param str String to cast
     * @param defaultValue
     * @return casted double value
     */
    fun toIntValue(str: String?, defaultValue: Int): Int {
        return toDoubleValue(str, false, defaultValue.toDouble()) as Int
    }

    /**
     * cast a double value to an int value (primitive value type)
     *
     * @param d double value to cast
     * @return casted int value
     */
    fun toIntValue(d: Double): Int {
        return d.toInt()
    }

    fun toIntValue(n: Number): Int {
        return n.intValue()
    }

    /**
     * cast an int value to an int value (do nothing)
     *
     * @param i int value to cast
     * @return casted int value
     */
    fun toIntValue(i: Int): Int {
        return i
    }

    /**
     * cast a boolean value to an int value (primitive value type)
     *
     * @param b boolean value to cast
     * @return casted int value
     */
    fun toIntValue(b: Boolean): Int {
        return if (b) 1 else 0
    }

    fun toIntValue(b: Boolean): Int {
        return if (b) 1 else 0
    }

    /**
     * cast a char value to an int value (primitive value type)
     *
     * @param c char value to cast
     * @return casted int value
     */
    fun toIntValue(c: Char): Int {
        return c.toInt()
    }

    /**
     * cast a double to a decimal value (String:xx.xx)
     *
     * @param value Object to cast
     * @return casted decimal value
     */
    fun toDecimal(value: Boolean): String {
        return if (value) "1.00" else "0.00"
    }

    /**
     * cast a double to a decimal value (String:xx.xx)
     *
     * @param value Object to cast
     * @return casted decimal value
     * @throws PageException
     */
    @Throws(PageException::class)
    fun toDecimal(value: Object?): String {
        return toDecimal(toDoubleValue(value))
    }

    @Throws(PageException::class)
    fun toDecimal(value: Object?, separator: Boolean): String {
        return toDecimal(toDoubleValue(value), separator)
    }

    /**
     * cast a double to a decimal value (String:xx.xx)
     *
     * @param value Object to cast
     * @return casted decimal value
     * @throws PageException
     */
    @Throws(PageException::class)
    fun toDecimal(value: String?): String {
        return toDecimal(toDoubleValue(value))
    }

    @Throws(PageException::class)
    fun toDecimal(value: String?, separator: Boolean): String {
        return toDecimal(toDoubleValue(value), separator)
    }

    /**
     * cast a double to a decimal value (String:xx.xx)
     *
     * @param value Object to cast
     * @param defaultValue
     * @return casted decimal value
     */
    fun toDecimal(value: Object?, defaultValue: String): String {
        val res: Double = toDoubleValue(value, true, Double.NaN)
        return if (Double.isNaN(res)) defaultValue else toDecimal(res)
    }

    fun toDecimal(value: Object?, separator: Boolean, defaultValue: String?): String? {
        val res: Double = toDoubleValue(value, true, Double.NaN)
        return if (Double.isNaN(res)) defaultValue else toDecimal(res, separator)
    }

    /**
     * cast an Object to a decimal value (String:xx.xx)
     *
     * @param value Object to cast
     * @return casted decimal value
     */
    fun toDecimal(value: Double): String {
        return toDecimal(value, '.', ',')
    }

    fun toDecimal(value: Double, separator: Boolean): String {
        return toDecimal(value, '.', if (separator) ',' else 0.toChar())
    }

    private fun toDecimal(value: Double, decDel: Char, thsDel: Char): String {
        // TODO Caster toDecimal bessere impl.
        val str: String = toBigDecimal(StrictMath.round(value * 100) / 100.0).toString()
        // str=toDouble(value).toString();
        val arr: Array<String> = str.split("\\.")

        // right value
        var rightValue: String
        if (arr.size == 1) {
            rightValue = "00"
        } else {
            rightValue = arr[1]
            rightValue = StrictMath.round(toDoubleValue("0.$rightValue", 0.0) * 100).toString() + ""
            if (rightValue.length() < 2) rightValue = 0 + rightValue
        }

        // left value
        var leftValue = arr[0]
        val leftValueLen: Int = leftValue.length()
        val ends = if (StringUtil.startsWith(str, '-')) 1 else 0
        if (leftValueLen > 3) {
            val tmp = StringBuffer()
            var i: Int
            i = leftValueLen - 3
            while (i > 0) {
                tmp.insert(0, leftValue.substring(i, i + 3))
                if (i != ends && thsDel > 0.toChar()) tmp.insert(0, thsDel)
                i -= 3
            }
            tmp.insert(0, leftValue.substring(0, i + 3))
            leftValue = tmp.toString()
        }
        return leftValue + decDel + rightValue
    }

    /**
     * cast a boolean value to a Boolean Object(reference type)
     *
     * @param b boolean value to cast
     * @return casted Boolean Object
     */
    fun toBoolean(b: Boolean): Boolean {
        return if (b) Boolean.TRUE else Boolean.FALSE
    }

    fun toBoolean(b: Boolean): Boolean {
        return b
    }

    /**
     * cast a char value to a Boolean Object(reference type)
     *
     * @param c char value to cast
     * @return casted Boolean Object
     */
    fun toBoolean(c: Char): Boolean {
        return if (c.toInt() != 0) Boolean.TRUE else Boolean.FALSE
    }

    /**
     * cast an int value to a Boolean Object(reference type)
     *
     * @param i int value to cast
     * @return casted Boolean Object
     */
    fun toBoolean(i: Int): Boolean {
        return if (i != 0) Boolean.TRUE else Boolean.FALSE
    }

    fun toBoolean(n: Number): Boolean {
        return if (n.intValue() !== 0) Boolean.TRUE else Boolean.FALSE
    }

    /**
     * cast a long value to a Boolean Object(reference type)
     *
     * @param l long value to cast
     * @return casted Boolean Object
     */
    fun toBoolean(l: Long): Boolean {
        return if (l != 0L) Boolean.TRUE else Boolean.FALSE
    }

    /**
     * cast a double value to a Boolean Object(reference type)
     *
     * @param d double value to cast
     * @return casted Boolean Object
     */
    fun toBoolean(d: Double): Boolean {
        return if (d != 0.0) Boolean.TRUE else Boolean.FALSE
    }

    /**
     * cast an Object to a Boolean Object(reference type)
     *
     * @param o Object to cast
     * @return casted Boolean Object
     * @throws PageException
     */
    @Throws(PageException::class)
    fun toBoolean(o: Object?): Boolean? {
        if (o is Boolean) return o
        return if (toBooleanValue(o)) Boolean.TRUE else Boolean.FALSE
    }

    /**
     * cast an Object to a Boolean Object(reference type)
     *
     * @param str String to cast
     * @return casted Boolean Object
     * @throws PageException
     */
    @Throws(PageException::class)
    fun toBoolean(str: String?): Boolean {
        return if (toBooleanValue(str)) Boolean.TRUE else Boolean.FALSE
    }

    /**
     * cast an Object to a boolean value (primitive value type), Exception Less
     *
     * @param o Object to cast
     * @param defaultValue
     * @return casted boolean value
     */
    fun toBooleanValue(o: Object?, defaultValue: Boolean): Boolean {
        if (o is Boolean) return (o as Boolean?).booleanValue() else if (o is Double) return toBooleanValue((o as Double?).doubleValue()) else if (o is Number) return toBooleanValue((o as Number?).doubleValue()) else if (o is String) {
            val b = toBoolean(o.toString(), null)!!
            if (b != null) return b
        } else if (o is Castable) {
            return (o as Castable?).castToBoolean(toBoolean(defaultValue)).booleanValue()
        } else if (o == null) return toBooleanValue("", defaultValue) else if (o is ObjectWrap) return toBooleanValue((o as ObjectWrap).getEmbededObject(toBoolean(defaultValue)), defaultValue)
        return defaultValue
    }

    /**
     * cast an Object to a boolean value (refrence type), Exception Less
     *
     * @param o Object to cast
     * @param defaultValue default value
     * @return casted boolean reference
     */
    fun toBoolean(o: Object?, defaultValue: Boolean?): Boolean? {
        if (o is Boolean) return o else if (o is Number) return if ((o as Number?).intValue() === 0) Boolean.FALSE else Boolean.TRUE else if (o is String) {
            val rtn = stringToBooleanValueEL(o.toString())
            if (rtn == 1) return Boolean.TRUE else if (rtn == 0) return Boolean.FALSE else {
                val dbl = toDoubleValue(o.toString(), Double.NaN)
                if (!Double.isNaN(dbl)) return if (toBooleanValue(dbl)) Boolean.TRUE else Boolean.FALSE
            }
        } else if (o is Castable) {
            return (o as Castable?).castToBoolean(defaultValue)
        } else if (o is ObjectWrap) return toBoolean((o as ObjectWrap?).getEmbededObject(defaultValue), defaultValue) else if (o == null) return toBoolean("", defaultValue)
        return defaultValue
    }

    /**
     * cast a boolean value to a char value
     *
     * @param b boolean value to cast
     * @return casted char value
     */
    fun toCharValue(b: Boolean): Char {
        return (if (b) 1 else 0).toChar()
    }

    fun toCharValue(b: Boolean): Char {
        return (if (b.booleanValue()) 1 else 0).toChar()
    }

    /**
     * cast a double value to a char value (primitive value type)
     *
     * @param d double value to cast
     * @return casted char value
     */
    fun toCharValue(d: Double): Char {
        return d.toChar()
    }

    fun toCharValue(n: Number): Char {
        return n.intValue() as Char
    }

    /**
     * cast a char value to a char value (do nothing)
     *
     * @param c char value to cast
     * @return casted char value
     */
    fun toCharValue(c: Char): Char {
        return c
    }

    /**
     * cast an Object to a char value (primitive value type)
     *
     * @param o Object to cast
     * @return casted char value
     * @throws PageException
     */
    @Throws(PageException::class)
    fun toCharValue(o: Object?): Char {
        if (o is Character) return (o as Character?).charValue() else if (o is Boolean) return (if ((o as Boolean?).booleanValue()) 1 else 0).toChar() else if (o is Double) return (o as Double?).doubleValue() as Char else if (o is Number) return (o as Number?).doubleValue() as Char else if (o is String) {
            val str: String = o.toString()
            if (str.length() > 0) return str.charAt(0)
            throw ExpressionException("can't cast empty string to a char")
        } else if (o is ObjectWrap) {
            return toCharValue((o as ObjectWrap?).getEmbededObject())
        } else if (o == null) return toCharValue("")
        throw CasterException(o, "char")
    }

    /**
     * cast an Object to a char value (primitive value type)
     *
     * @param str Object to cast
     * @return casted char value
     * @throws PageException
     */
    @Throws(PageException::class)
    fun toCharValue(str: String): Char {
        if (str.length() > 0) return str.charAt(0)
        throw ExpressionException("can't cast empty string to a char")
    }

    /**
     * cast an Object to a char value (primitive value type)
     *
     * @param o Object to cast
     * @param defaultValue
     * @return casted char value
     */
    fun toCharValue(o: Object?, defaultValue: Char): Char {
        if (o is Character) return (o as Character?).charValue() else if (o is Boolean) return (if ((o as Boolean?).booleanValue()) 1 else 0).toChar() else if (o is Double) return (o as Double?).doubleValue() as Char else if (o is Number) return (o as Number?).doubleValue() as Char else if (o is String) {
            val str: String = o.toString()
            return if (str.length() > 0) str.charAt(0) else defaultValue
        } else if (o is ObjectWrap) {
            return toCharValue((o as ObjectWrap?).getEmbededObject(toCharacter(defaultValue)), defaultValue)
        } else if (o == null) return toCharValue("", defaultValue)
        return defaultValue
    }

    /**
     * cast a boolean value to a Character Object(reference type)
     *
     * @param b boolean value to cast
     * @return casted Character Object
     */
    fun toCharacter(b: Boolean): Character {
        return Character.valueOf(toCharValue(b))
    }

    fun toCharacter(b: Boolean?): Character {
        return Character.valueOf(toCharValue(b))
    }

    /**
     * cast a char value to a Character Object(reference type)
     *
     * @param c char value to cast
     * @return casted Character Object
     */
    fun toCharacter(c: Char): Character {
        return Character.valueOf(toCharValue(c))
    }

    /**
     * cast a double value to a Character Object(reference type)
     *
     * @param d double value to cast
     * @return casted Character Object
     */
    fun toCharacter(d: Double): Character {
        return Character.valueOf(toCharValue(d))
    }

    fun toCharacter(n: Number?): Character {
        return Character.valueOf(toCharValue(n))
    }

    /**
     * cast an Object to a Character Object(reference type)
     *
     * @param o Object to cast
     * @return casted Character Object
     * @throws PageException
     */
    @Throws(PageException::class)
    fun toCharacter(o: Object?): Character? {
        return if (o is Character) o as Character? else Character.valueOf(toCharValue(o))
    }

    /**
     * cast an Object to a Character Object(reference type)
     *
     * @param str Object to cast
     * @return casted Character Object
     * @throws PageException
     */
    @Throws(PageException::class)
    fun toCharacter(str: String?): Character {
        return Character.valueOf(toCharValue(str))
    }

    /**
     * cast an Object to a Character Object(reference type)
     *
     * @param o Object to cast
     * @param defaultValue
     * @return casted Character Object
     */
    fun toCharacter(o: Object?, defaultValue: Character?): Character? {
        if (o is Character) return o as Character?
        if (defaultValue != null) return Character.valueOf(toCharValue(o, defaultValue.charValue()))
        val res = toCharValue(o, Character.MIN_VALUE)
        return if (res == Character.MIN_VALUE) defaultValue else Character.valueOf(res)
    }

    /**
     * cast a boolean value to a byte value
     *
     * @param b boolean value to cast
     * @return casted byte value
     */
    fun toByteValue(b: Boolean): Byte {
        return (if (b) 1 else 0).toByte()
    }

    fun toByteValue(b: Boolean): Byte {
        return (if (b.booleanValue()) 1 else 0).toByte()
    }

    /**
     * cast a double value to a byte value (primitive value type)
     *
     * @param d double value to cast
     * @return casted byte value
     */
    fun toByteValue(d: Double): Byte {
        return d.toByte()
    }

    fun toByteValue(n: Number): Byte {
        return n.byteValue()
    }

    /**
     * cast a char value to a byte value (do nothing)
     *
     * @param c char value to cast
     * @return casted byte value
     */
    fun toByteValue(c: Char): Byte {
        return c.toByte()
    }

    /**
     * cast an Object to a byte value (primitive value type)
     *
     * @param o Object to cast
     * @return casted byte value
     * @throws PageException
     * @throws CasterException
     */
    @Throws(PageException::class)
    fun toByteValue(o: Object): Byte {
        if (o is Byte) return (o as Byte).byteValue()
        if (o is Character) return (o as Character).charValue() else if (o is Boolean) return (if ((o as Boolean).booleanValue()) 1 else 0).toByte() else if (o is Number) return (o as Number).byteValue() else if (o is String) return toDoubleValue(o.toString()).toByte() else if (o is ObjectWrap) {
            return toByteValue((o as ObjectWrap).getEmbededObject())
        }
        throw CasterException(o, "byte")
    }

    /**
     * cast an Object to a byte value (primitive value type)
     *
     * @param str Object to cast
     * @return casted byte value
     * @throws PageException
     * @throws CasterException
     */
    @Throws(PageException::class)
    fun toByteValue(str: String): Byte {
        return toDoubleValue(str) as Byte
    }

    /**
     * cast an Object to a byte value (primitive value type)
     *
     * @param o Object to cast
     * @param defaultValue
     * @return casted byte value
     */
    fun toByteValue(o: Object, defaultValue: Byte): Byte {
        if (o is Byte) return (o as Byte).byteValue()
        if (o is Character) return (o as Character).charValue() else if (o is Boolean) return (if ((o as Boolean).booleanValue()) 1 else 0).toByte() else if (o is Number) return (o as Number).byteValue() else if (o is String) return toDoubleValue(o.toString(), defaultValue) as Byte else if (o is ObjectWrap) {
            return toByteValue((o as ObjectWrap).getEmbededObject(toByte(defaultValue.toDouble())), defaultValue)
        }
        return defaultValue
    }

    /**
     * cast a boolean value to a Byte Object(reference type)
     *
     * @param b boolean value to cast
     * @return casted Byte Object
     */
    fun toByte(b: Boolean): Byte {
        return Byte.valueOf(toByteValue(b))
    }

    fun toByte(b: Boolean?): Byte {
        return Byte.valueOf(toByteValue(b))
    }

    /**
     * cast a char value to a Byte Object(reference type)
     *
     * @param c char value to cast
     * @return casted Byte Object
     */
    fun toByte(c: Char): Byte {
        return Byte.valueOf(toByteValue(c))
    }

    /**
     * cast a double value to a Byte Object(reference type)
     *
     * @param d double value to cast
     * @return casted Byte Object
     */
    fun toByte(d: Double): Byte {
        return Byte.valueOf(toByteValue(d))
    }

    fun toByte(n: Number?): Byte {
        return Byte.valueOf(toByteValue(n))
    }

    /**
     * cast an Object to a Byte Object(reference type)
     *
     * @param o Object to cast
     * @return casted Byte Object
     * @throws PageException
     */
    @Throws(PageException::class)
    fun toByte(o: Object): Byte {
        return if (o is Byte) o else Byte.valueOf(toByteValue(o))
    }

    /**
     * cast an Object to a Byte Object(reference type)
     *
     * @param str String to cast
     * @return casted Byte Object
     * @throws PageException
     */
    @Throws(PageException::class)
    fun toByte(str: String?): Byte {
        return Byte.valueOf(toByteValue(str))
    }

    /**
     * cast an Object to a Byte Object(reference type)
     *
     * @param o Object to cast
     * @param defaultValue
     * @return casted Byte Object
     */
    fun toByte(o: Object, defaultValue: Byte?): Byte? {
        if (o is Byte) return o
        if (defaultValue != null) return Byte.valueOf(toByteValue(o, defaultValue.byteValue()))
        val res = toByteValue(o, Byte.MIN_VALUE)
        return if (res == Byte.MIN_VALUE) defaultValue else Byte.valueOf(res)
    }

    /**
     * cast a boolean value to a long value
     *
     * @param b boolean value to cast
     * @return casted long value
     */
    fun toLongValue(b: Boolean): Long {
        return if (b) 1L else 0L
    }

    fun toLongValue(b: Boolean): Long {
        return if (b) 1L else 0L
    }

    /**
     * cast a double value to a long value (primitive value type)
     *
     * @param d double value to cast
     * @return casted long value
     */
    fun toLongValue(d: Double): Long {
        return d.toLong()
    }

    fun toLongValue(n: Number): Long {
        return n.longValue()
    }

    /**
     * cast a char value to a long value (do nothing)
     *
     * @param c char value to cast
     * @return casted long value
     */
    fun toLongValue(c: Char): Long {
        return c.toLong()
    }

    /**
     * cast an Object to a long value (primitive value type)
     *
     * @param o Object to cast
     * @return casted long value
     * @throws PageException
     */
    @Throws(PageException::class)
    fun toLongValue(o: Object?): Long {
        if (o is Boolean) return if ((o as Boolean?).booleanValue()) 1L else 0L else if (o is Number) return (o as Number?).longValue() else if (o is CharSequence) {
            val str: String = o.toString()
            return try {
                Long.parseLong(str)
            } catch (nfe: NumberFormatException) {
                toDoubleValue(str) as Long
            }
        } else if (o is Character) return (o as Character?).charValue() else if (o is Castable) return (o as Castable?).castToDoubleValue() else if (o is ObjectWrap) return toLongValue((o as ObjectWrap?).getEmbededObject())
        throw CasterException(o, "long")
    }

    /**
     * cast an Object to a long value (primitive value type)
     *
     * @param str Object to cast
     * @return casted long value
     * @throws PageException
     */
    @Throws(PageException::class)
    fun toLongValue(str: String): Long {
        var bi: BigInteger? = null
        try {
            bi = BigInteger(str)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
        }
        if (bi != null) {
            if (bi.bitLength() < 64) return bi.longValue()
            throw ApplicationException("number [" + str + "] cannot be casted to a long value, number is to long (" + (bi.bitLength() + 1) + " bit)")
        }
        return toDoubleValue(str) as Long
    }

    /**
     * returns a number Object, this can be a BigDecimal,BigInteger,Long, Double, depending on the
     * input.
     *
     * @param str
     * @return
     * @throws PageException
     */
    fun toNumber(str: String, defaultValue: Number): Number {
        return try {
            // float
            if (str.indexOf('.') !== -1) {
                return toBigDecimal(str)
            }
            // integer
            val bi = BigInteger(str)
            val l: Int = bi.bitLength()
            if (l < 32) return Integer.valueOf(bi.intValue())
            if (l < 64) Long.valueOf(bi.longValue()) else bi
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            defaultValue
        }
    }

    /**
     * cast an Object to a long value (primitive value type)
     *
     * @param o Object to cast
     * @param defaultValue
     * @return casted long value
     */
    fun toLongValue(o: Object, defaultValue: Long): Long {
        if (o is Character) return (o as Character).charValue() else if (o is Boolean) return if ((o as Boolean).booleanValue()) 1L else 0L else if (o is Number) return (o as Number).longValue() else if (o is CharSequence) return toDoubleValue(o.toString(), defaultValue) as Long else if (o is Castable) {
            return (o as Castable).castToDoubleValue(defaultValue)
        } else if (o is Character) return (o as Character).charValue() else if (o is ObjectWrap) return toLongValue((o as ObjectWrap).getEmbededObject(toLong(defaultValue)), defaultValue)
        return defaultValue
    }

    /**
     * cast a boolean value to a Long Object(reference type)
     *
     * @param b boolean value to cast
     * @return casted Long Object
     */
    fun toLong(b: Boolean): Long {
        return Long.valueOf(toLongValue(b))
    }

    fun toLong(b: Boolean): Long {
        return Long.valueOf(toLongValue(b.booleanValue()))
    }

    /**
     * cast a char value to a Long Object(reference type)
     *
     * @param c char value to cast
     * @return casted Long Object
     */
    fun toLong(c: Char): Long {
        return Long.valueOf(toLongValue(c))
    }

    /**
     * cast a double value to a Long Object(reference type)
     *
     * @param d double value to cast
     * @return casted Long Object
     */
    fun toLong(d: Double): Long {
        return Long.valueOf(toLongValue(d))
    }

    fun toLong(n: Number): Long {
        return Long.valueOf(n.longValue())
    }

    /**
     * cast an Object to a Long Object(reference type)
     *
     * @param o Object to cast
     * @return casted Long Object
     * @throws PageException
     */
    @Throws(PageException::class)
    fun toLong(o: Object?): Long? {
        return if (o is Long) o else Long.valueOf(toLongValue(o))
    }

    /**
     * cast an Object to a Long Object(reference type)
     *
     * @param str Object to cast
     * @return casted Long Object
     * @throws PageException
     */
    @Throws(PageException::class)
    fun toLong(str: String?): Long {
        return Long.valueOf(toLongValue(str))
    }

    /**
     * cast a long to a Long Object(reference type)
     *
     * @param l long to cast
     * @return casted Long Object
     */
    fun toLong(l: Long): Long {
        return Long.valueOf(l)
    }

    /**
     * cast an Object to a Long Object(reference type)
     *
     * @param o Object to cast
     * @param defaultValue
     * @return casted Long Object
     */
    fun toLong(o: Object, defaultValue: Long?): Long? {
        if (o is Long) return o
        if (defaultValue != null) return Long.valueOf(toLongValue(o, defaultValue.longValue()))
        val res = toLongValue(o, Long.MIN_VALUE)
        return if (res == Long.MIN_VALUE) defaultValue else Long.valueOf(res)
    }

    /**
     * cast a boolean value to a Float Object(reference type)
     *
     * @param b boolean value to cast
     * @return casted Float Object
     */
    fun toFloat(b: Boolean): Float {
        return Float.valueOf(toFloatValue(b))
    }

    fun toFloat(b: Boolean): Float {
        return Float.valueOf(toFloatValue(b.booleanValue()))
    }

    /**
     * cast a char value to a Float Object(reference type)
     *
     * @param c char value to cast
     * @return casted Float Object
     */
    fun toFloat(c: Char): Float {
        return Float.valueOf(toFloatValue(c))
    }

    /**
     * cast a double value to a Float Object(reference type)
     *
     * @param d double value to cast
     * @return casted Float Object
     */
    fun toFloat(d: Double): Float {
        return Float.valueOf(toFloatValue(d))
    }

    fun toFloat(n: Number): Float {
        return n.floatValue()
    }

    fun toFloatValue(n: Number): Float {
        return Float.valueOf(n.floatValue())
    }

    /**
     * cast an Object to a Float Object(reference type)
     *
     * @param o Object to cast
     * @return casted Float Object
     * @throws PageException
     */
    @Throws(PageException::class)
    fun toFloat(o: Object): Float {
        return if (o is Float) o else Float.valueOf(toFloatValue(o))
    }

    /**
     * cast an Object to a Float Object(reference type)
     *
     * @param str Object to cast
     * @return casted Float Object
     * @throws PageException
     */
    @Throws(PageException::class)
    fun toFloat(str: String?): Float {
        return Float.valueOf(toFloatValue(str))
    }

    /**
     * cast an Object to a Float Object(reference type)
     *
     * @param o Object to cast
     * @param defaultValue
     * @return casted Float Object
     */
    fun toFloat(o: Object, defaultValue: Float?): Float? {
        if (o is Float) return o
        if (defaultValue != null) return Float.valueOf(toFloatValue(o, defaultValue.floatValue()))
        val res = toFloatValue(o, Float.MIN_VALUE)
        return if (res == Float.MIN_VALUE) defaultValue else Float.valueOf(res)
    }

    /**
     * cast a boolean value to a float value
     *
     * @param b boolean value to cast
     * @return casted long value
     */
    fun toFloatValue(b: Boolean): Float {
        return if (b) 1f else 0f
    }

    fun toFloatValue(b: Boolean): Float {
        return if (b) 1f else 0f
    }

    /**
     * cast a double value to a long value (primitive value type)
     *
     * @param d double value to cast
     * @return casted long value
     */
    fun toFloatValue(d: Double): Float {
        return d.toFloat()
    }

    /**
     * cast a char value to a long value (do nothing)
     *
     * @param c char value to cast
     * @return casted long value
     */
    fun toFloatValue(c: Char): Float {
        return c.toFloat()
    }

    /**
     * cast an Object to a long value (primitive value type)
     *
     * @param o Object to cast
     * @return casted long value
     * @throws PageException
     */
    @Throws(PageException::class)
    fun toFloatValue(o: Object): Float {
        if (o is Boolean) return if ((o as Boolean).booleanValue()) 1f else 0f else if (o is Number) return (o as Number).floatValue() else if (o is CharSequence) return toDoubleValue(o.toString()).toFloat() else if (o is Character) return (o as Character).charValue() else if (o is Castable) return (o as Castable).castToDoubleValue() else if (o is ObjectWrap) return toFloatValue((o as ObjectWrap).getEmbededObject())
        throw CasterException(o, "float")
    }

    /**
     * cast an Object to a long value (primitive value type)
     *
     * @param str Object to cast
     * @return casted long value
     * @throws PageException
     */
    @Throws(PageException::class)
    fun toFloatValue(str: String?): Float {
        return toDoubleValue(str) as Float
    }

    /**
     * cast an Object to a float value (primitive value type)
     *
     * @param o Object to cast
     * @param defaultValue
     * @return casted float value
     */
    fun toFloatValue(o: Object, defaultValue: Float): Float {
        if (o is Character) return (o as Character).charValue() else if (o is Boolean) return if ((o as Boolean).booleanValue()) 1f else 0f else if (o is Number) return (o as Number).floatValue() else if (o is CharSequence) return toDoubleValue(o.toString(), defaultValue) as Float else if (o is Character) return (o as Character).charValue() else if (o is Castable) {
            return (o as Castable).castToDoubleValue(defaultValue)
        } else if (o is ObjectWrap) return toFloatValue((o as ObjectWrap).getEmbededObject(toFloat(defaultValue.toDouble())), defaultValue)
        return defaultValue
    }

    /**
     * cast a boolean value to a short value
     *
     * @param b boolean value to cast
     * @return casted short value
     */
    fun toShortValue(b: Boolean): Short {
        return (if (b) 1 else 0).toShort()
    }

    fun toShortValue(b: Boolean): Short {
        return (if (b) 1 else 0).toShort()
    }

    /**
     * cast a double value to a short value (primitive value type)
     *
     * @param d double value to cast
     * @return casted short value
     */
    fun toShortValue(d: Double): Short {
        return d.toShort()
    }

    fun toShortValue(n: Number): Short {
        return n.shortValue()
    }

    /**
     * cast a char value to a short value (do nothing)
     *
     * @param c char value to cast
     * @return casted short value
     */
    fun toShortValue(c: Char): Short {
        return c.toShort()
    }

    /**
     * cast an Object to a short value (primitive value type)
     *
     * @param o Object to cast
     * @return casted short value
     * @throws PageException
     */
    @Throws(PageException::class)
    fun toShortValue(o: Object?): Short {
        if (o is Short) return (o as Short?).shortValue() else if (o is CharSequence) return toDoubleValue(o.toString()).toShort() else if (o is Character) return (o as Character?).charValue() else if (o is Boolean) return (if ((o as Boolean?).booleanValue()) 1 else 0).toShort() else if (o is Number) return (o as Number?).shortValue() else if (o is Castable) return (o as Castable?).castToDoubleValue() else if (o is ObjectWrap) return toShortValue((o as ObjectWrap?).getEmbededObject())
        throw CasterException(o, "short")
    }

    /**
     * cast an Object to a short value (primitive value type)
     *
     * @param str Object to cast
     * @return casted short value
     * @throws PageException
     */
    @Throws(PageException::class)
    fun toShortValue(str: String?): Short {
        return toDoubleValue(str) as Short
    }

    /**
     * cast an Object to a short value (primitive value type)
     *
     * @param o Object to cast
     * @param defaultValue
     * @return casted short value
     */
    fun toShortValue(o: Object, defaultValue: Short): Short {
        if (o is Short) return (o as Short).shortValue() else if (o is Boolean) return (if ((o as Boolean).booleanValue()) 1 else 0).toShort() else if (o is Number) return (o as Number).shortValue() else if (o is CharSequence) return toDoubleValue(o.toString(), defaultValue) as Short else if (o is Character) return (o as Character).charValue() else if (o is Castable) {
            return (o as Castable).castToDoubleValue(defaultValue)
        } else if (o is ObjectWrap) return toShortValue((o as ObjectWrap).getEmbededObject(toShort(defaultValue.toDouble())), defaultValue)
        return defaultValue
    }

    /**
     * cast a boolean value to a Short Object(reference type)
     *
     * @param b boolean value to cast
     * @return casted Short Object
     */
    fun toShort(b: Boolean): Short {
        return Short.valueOf(toShortValue(b))
    }

    fun toShort(b: Boolean): Short {
        return Short.valueOf(toShortValue(b.booleanValue()))
    }

    /**
     * cast a char value to a Short Object(reference type)
     *
     * @param c char value to cast
     * @return casted Short Object
     */
    fun toShort(c: Char): Short {
        return Short.valueOf(toShortValue(c))
    }

    /**
     * cast a double value to a Byte Object(reference type)
     *
     * @param d double value to cast
     * @return casted Byte Object
     */
    fun toShort(d: Double): Short {
        return Short.valueOf(toShortValue(d))
    }

    fun toShort(n: Number): Short {
        return Short.valueOf(n.shortValue())
    }

    /**
     * cast an Object to a Byte Object(reference type)
     *
     * @param o Object to cast
     * @return casted Byte Object
     * @throws PageException
     */
    @Throws(PageException::class)
    fun toShort(o: Object?): Short? {
        return if (o is Short) o else Short.valueOf(toShortValue(o))
    }

    /**
     * cast an Object to a Byte Object(reference type)
     *
     * @param str Object to cast
     * @return casted Byte Object
     * @throws PageException
     */
    @Throws(PageException::class)
    fun toShort(str: String?): Short {
        return Short.valueOf(toShortValue(str))
    }

    /**
     * cast an Object to a Byte Object(reference type)
     *
     * @param o Object to cast
     * @param defaultValue
     * @return casted Byte Object
     */
    fun toShort(o: Object, defaultValue: Short?): Short? {
        if (o is Short) return o
        if (defaultValue != null) return Short.valueOf(toShortValue(o, defaultValue.shortValue()))
        val res = toShortValue(o, Short.MIN_VALUE)
        return if (res == Short.MIN_VALUE) defaultValue else Short.valueOf(res)
    }

    /**
     * cast a String to a boolean value (primitive value type)
     *
     * @param str String to cast
     * @return casted boolean value
     * @throws ExpressionException
     */
    @Throws(ExpressionException::class)
    fun stringToBooleanValue(str: String): Boolean {
        var str = str
        str = StringUtil.toLowerCase(str.trim())
        if (str.equals("yes") || str.equals("true")) return true else if (str.equals("no") || str.equals("false")) return false
        throw CasterException("Can't cast String [" + CasterException.crop(str).toString() + "] to boolean")
    }

    /**
     * cast a String to a boolean value (primitive value type), return 1 for true, 0 for false and -1 if
     * can't cast to a boolean type
     *
     * @param str String to cast
     * @return casted boolean value
     */
    fun stringToBooleanValueEL(str: String): Int {
        if (str.length() < 2) return -1
        when (str.charAt(0)) {
            't', 'T' -> return if (str.equalsIgnoreCase("true")) 1 else -1
            'f', 'F' -> return if (str.equalsIgnoreCase("false")) 0 else -1
            'y', 'Y' -> return if (str.equalsIgnoreCase("yes")) 1 else -1
            'n', 'N' -> return if (str.equalsIgnoreCase("no")) 0 else -1
        }
        return -1
    }

    /**
     * cast an Object to a String
     *
     * @param o Object to cast
     * @return casted String
     * @throws PageException
     */
    @Throws(PageException::class)
    fun toString(o: Object?): String? {
        if (o is String) return o else if (o is Number) return toString(o as Number?) else if (o is Boolean) return toString((o as Boolean?).booleanValue()) else if (o is Castable) return (o as Castable?).castToString() else if (o is Date) {
            return if (o is DateTime) (o as DateTime?).castToString() else DateTimeImpl(o as Date?).castToString()
        } else if (o is Clob) return toString(o as Clob?) else if (o is Locale) return toString(o as Locale?) else if (o is TimeZone) return toString(o as TimeZone?) else if (o is Node) return XMLCaster.toString(o as Node?) else if (o is Reader) {
            var r: Reader? = null
            return try {
                IOUtil.toString(o as Reader?. also { r = it })
            } catch (e: IOException) {
                throw toPageException(e)
            } finally {
                try {
                    IOUtil.close(r)
                } catch (e: IOException) {
                    throw toPageException(e)
                }
            }
        } else if (o is Throwable) {
            return toString(o as Throwable?, true)
        } else if (o is InputStream) {
            val pc: PageContextImpl = ThreadLocalPageContext.get() as PageContextImpl
            var r: InputStream? = null
            return try {
                IOUtil.toString(o as InputStream?. also { r = it }, pc.getWebCharset())
            } catch (e: IOException) {
                throw toPageException(e)
            } finally {
                try {
                    IOUtil.close(r)
                } catch (e: IOException) {
                    throw toPageException(e)
                }
            }
        } else if (o is ByteArray) {
            val pc: PageContextImpl = ThreadLocalPageContext.get() as PageContextImpl
            return try {
                String(o as ByteArray?, pc.getWebCharset())
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
                String(o as ByteArray?)
            }
        } else if (o is CharArray) return String(o as CharArray?) else if (o is ObjectWrap) return toString((o as ObjectWrap?).getEmbededObject()) else if (o is Calendar) return toString((o as Calendar?).getTime()) else if (o == null) return ""

        // INFO Collection is new of type Castable
        if (o is Map || o is List || o is Function) throw CasterException(o, "string")
        /*
		 * if((x instanceof Query) || (x instanceof RowSet) || (x instanceof coldfusion.runtime.Array) || (x
		 * instanceof JavaProxy) || (x instanceof FileStreamWrapper))
		 */return o.toString()
    }

    /**
     * cast a String to a String (do Nothing)
     *
     * @param str
     * @return casted String
     * @throws PageException
     */
    fun toString(str: String?): String? {
        return str
    }

    @Throws(PageException::class)
    fun toStringBuffer(obj: Object?): StringBuffer? {
        return if (obj is StringBuffer) obj as StringBuffer? else StringBuffer(toString(obj))
    }

    @Throws(CasterException::class)
    fun toKey(o: Object?): Collection.Key? {
        return KeyImpl.toKey(o)
    }

    fun toKey(o: Object?, defaultValue: Collection.Key?): Collection.Key? {
        return KeyImpl.toKey(o, defaultValue)
    }

    /**
     * cast an Object to a String dont throw an exception, if can't cast to a string return an empty
     * string
     *
     * @param o Object to cast
     * @param defaultValue
     * @return casted String
     */
    fun toString(o: Object?, defaultValue: String?): String? {
        return toString(o, true, defaultValue)
    }

    fun toString(o: Object?, executeDefaultToStringMethod: Boolean, defaultValue: String?): String? {
        if (o is String) return o else if (o is Boolean) return toString((o as Boolean?).booleanValue()) else if (o is Number) return toString(o as Number?) else if (o is Castable) return (o as Castable?).castToString(defaultValue) else if (o is Date) {
            return if (o is DateTime) {
                (o as DateTime?).castToString(defaultValue)
            } else DateTimeImpl(o as Date?).castToString(defaultValue)
        } else if (o is Clob) {
            return try {
                toString(o as Clob?)
            } catch (e: ExpressionException) {
                defaultValue
            }
        } else if (o is Node) {
            return try {
                XMLCaster.toString(o as Node?)
            } catch (e: PageException) {
                defaultValue
            }
        } else if (o is Map || o is List || o is Function) return defaultValue else if (o == null) return "" else if (o is ObjectWrap) return toString((o as ObjectWrap?).getEmbededObject(defaultValue), defaultValue)
        return if (executeDefaultToStringMethod) o.toString() else defaultValue
        /// TODO diese methode ist nicht gleich wie toString(Object)
    }

    @Throws(ExpressionException::class)
    private fun toString(clob: Clob?): String? {
        return try {
            val `in`: Reader = clob.getCharacterStream()
            val buf = StringBuffer()
            var c: Int = `in`.read()
            while (c != -1) {
                buf.append(c.toChar())
                c = `in`.read()
            }
            buf.toString()
        } catch (e: Exception) {
            throw ExpressionException.newInstance(e)
        }
    }

    fun toString(l: Locale?): String? {
        return LocaleFactory.toString(l)
    }

    @Throws(PageException::class)
    fun toLocale(obj: Object?): Locale? {
        return if (obj is Locale) obj as Locale? else LocaleFactory.getLocale(toString(obj))
    }

    fun toLocale(obj: Object?, defaultValue: Locale?): Locale? {
        if (obj is Locale) return obj as Locale?
        val str = toString(obj, null) ?: return defaultValue
        return toLocale(str, defaultValue)
    }

    fun toString(tz: TimeZone?): String? {
        return TimeZoneUtil.toString(tz)
    }

    fun toString(t: Throwable?, addMessage: Boolean): String? {
        return ExceptionUtil.getStacktrace(t, addMessage)
    }

    @Throws(PageException::class)
    fun toTimeZone(obj: Object?): TimeZone? {
        return if (obj is TimeZone) obj as TimeZone? else TimeZoneUtil.toTimeZone(toString(obj))
    }

    /**
     * cast a double value to a String
     *
     * @param d double value to cast
     * @return casted String
     */
    fun toString3(d: Double): String? {
        val l = d.toLong()
        if (l.toDouble() == d) return toString(l)
        val str = Double.toString(d)
        var pos: Int
        return if (str.indexOf('E').also { pos = it } != -1 && pos == str.length() - 2) {
            StringBuffer(pos + 2).append(str.charAt(0)).append(str.substring(2, toDigit(str.charAt(pos + 1)) + 2)).append('.')
                    .append(str.substring(toDigit(str.charAt(pos + 1)) + 2, pos)).toString()
        } else str
    }

    private val df: DecimalFormat? = DecimalFormat.getInstance(Locale.US) as DecimalFormat // ("#.###########");
    fun toString(d: Double): String? {
        val l = d.toLong()
        if (l.toDouble() == d) return toString(l)
        if (d > l && d - l < 0.000000000001) return toString(l)
        return if (l > d && l - d < 0.000000000001) toString(l) else df.format(d)
    }

    fun toString(n: Number?): String? {
        if (n is BigDecimal) return df.format(n)
        val d: Double = n.doubleValue()
        val l = d.toLong()
        if (l.toDouble() == d) return toString(l)
        if (d > l && d - l < 0.000000000001) return toString(l)
        if (l > d && l - d < 0.000000000001) return toString(l)
        return if (n is Double) toString(n.doubleValue()) else n.toString()
        // return df.format(d);
    }

    fun toString(bd: BigDecimal?): String? {
        return df.format(bd)
    }

    /**
     * cast a long value to a String
     *
     * @param l long value to cast
     * @return casted String
     */
    fun toString(l: Long): String? {
        return if (l < NUMBERS_MIN || l > NUMBERS_MAX) {
            Long.toString(l, 10)
        } else NUMBERS[l.toInt()]
    }

    /**
     * cast an int value to a String
     *
     * @param i int value to cast
     * @return casted String
     */
    fun toString(i: Int): String? {
        return if (i < NUMBERS_MIN || i > NUMBERS_MAX) Integer.toString(i, 10) else NUMBERS[i]
    }

    /**
     * cast a boolean value to a String
     *
     * @param b boolean value to cast
     * @return casted String
     */
    fun toString(b: Boolean): String? {
        return if (b) "true" else "false"
    }

    fun toString(b: Boolean?): String? {
        return if (b!!) "true" else "false"
    }

    @Throws(PageException::class)
    fun toFunction(o: Object?): UDF? {
        if (o is UDF) return o as UDF? else if (o is ObjectWrap) {
            return toFunction((o as ObjectWrap?).getEmbededObject())
        }
        throw CasterException(o, "function")
    }

    fun toFunction(o: Object?, defaultValue: UDF?): UDF? {
        if (o is UDF) return o as UDF? else if (o is ObjectWrap) {
            return toFunction((o as ObjectWrap?).getEmbededObject(defaultValue), defaultValue)
        }
        return defaultValue
    }

    /**
     * cast an Object to an Array Object
     *
     * @param o Object to cast
     * @return casted Array
     * @throws PageException
     */
    @Throws(PageException::class)
    fun toList(o: Object?): List? {
        return toList(o, false)
    }

    /**
     * cast an Object to an Array Object
     *
     * @param o Object to cast
     * @param defaultValue
     * @return casted Array
     */
    fun toList(o: Object?, defaultValue: List?): List? {
        return toList(o, false, defaultValue)
    }

    /**
     * cast an Object to an Array Object
     *
     * @param o Object to cast
     * @param duplicate
     * @param defaultValue
     * @return casted Array
     */
    fun toList(o: Object?, duplicate: Boolean, defaultValue: List?): List? {
        return try {
            toList(o, duplicate)
        } catch (e: PageException) {
            defaultValue
        }
    }

    /**
     * cast an Object to an Array Object
     *
     * @param o Object to cast
     * @param duplicate
     * @return casted Array
     * @throws PageException
     */
    @Throws(PageException::class)
    fun toList(o: Object?, duplicate: Boolean): List? {
        var ex: PageException? = null
        if (o is List) {
            if (duplicate) {
                val src: List? = o
                val size: Int = src.size()
                val trg = ArrayList()
                for (i in 0 until size) {
                    trg.add(i, src.get(i))
                }
                return trg
            }
            return o
        } else if (o is Array<Object>) {
            val list = ArrayList()
            val arr: Array<Object?>? = o
            for (i in arr.indices) list.add(i, arr!![i])
            return list
        } else if (o is Array) {
            if (!duplicate) return ArrayAsList.toList(o as Array?)
            val list = ArrayList()
            val arr: Array? = o
            for (i in 0 until arr.size()) list.add(i, arr.get(i + 1, null))
            return list
        } else if (o is Iterator) {
            val it: Iterator? = o
            val list = ArrayList()
            while (it.hasNext()) {
                list.add(it.next())
            }
            return list
        } else if (o is XMLStruct) {
            val sct: XMLStruct? = o as XMLStruct?
            if (sct is XMLMultiElementStruct) return toList(XMLMultiElementArray(o as XMLMultiElementStruct?))
            val list = ArrayList()
            list.add(sct)
            return list
        } else if (o is ObjectWrap) {
            return toList((o as ObjectWrap?).getEmbededObject())
        } else if (o is Struct) {
            if (o is Component) {
                try {
                    val tmp: Object = Reflector.componentToClass(ThreadLocalPageContext.get(), o as Component?, List::class.java)
                    if (tmp is List) return tmp
                } catch (e: PageException) {
                    ex = e
                }
            }
            val sct: Struct? = o as Struct?
            val arr = ArrayList()
            val it: Iterator<Entry<Key?, Object?>?> = sct.entryIterator()
            var e: Entry<Key?, Object?>? = null
            try {
                while (it.hasNext()) {
                    e = it.next()
                    arr.add(toIntValue(e.getKey().getString()), e.getValue())
                }
            } catch (ee: ExpressionException) {
                throw ExpressionException("can't cast struct to an array, key [" + (if (e != null) e.getKey() else "") + "] is not a number")
            }
            return arr
        } else if (o is BooleanArray) return toList(ArrayUtil.toReferenceType(o as BooleanArray?)) else if (o is ByteArray) return toList(ArrayUtil.toReferenceType(o as ByteArray?)) else if (o is CharArray) return toList(ArrayUtil.toReferenceType(o as CharArray?)) else if (o is ShortArray) return toList(ArrayUtil.toReferenceType(o as ShortArray?)) else if (o is IntArray) return toList(ArrayUtil.toReferenceType(o as IntArray?)) else if (o is LongArray) return toList(ArrayUtil.toReferenceType(o as LongArray?)) else if (o is FloatArray) return toList(ArrayUtil.toReferenceType(o as FloatArray?)) else if (o is DoubleArray) return toList(ArrayUtil.toReferenceType(o as DoubleArray?))
        if (ex != null) throw ex
        throw CasterException(o, "List")
    }

    /**
     * cast an Object to an Array Object
     *
     * @param o Object to cast
     * @return casted Array
     * @throws PageException
     */
    @Throws(PageException::class)
    fun toArray(o: Object?): Array? {
        if (o is Array) return o else if (o is Array<Object>) {
            return ArrayImpl(o as Array<Object?>?)
        } else if (o is List) {
            return ListAsArray.toArray(o as List?) // new ArrayImpl(((List) o).toArray());
        } else if (o is Set) {
            return toArray((o as Set?).toArray()) // new ArrayImpl(((List) o).toArray());
        } else if (o is XMLStruct) {
            val xmes: XMLMultiElementStruct?
            if (o is XMLMultiElementStruct) {
                xmes = o as XMLMultiElementStruct?
            } else {
                val sct: XMLStruct? = o as XMLStruct?
                val a: Array = ArrayImpl()
                a.append(o)
                xmes = XMLMultiElementStruct(a, sct.getCaseSensitive())
            }
            return XMLMultiElementArray(xmes)
        } else if (o is ObjectWrap) {
            return toArray((o as ObjectWrap?).getEmbededObject())
        } else if (o is Struct) {

            // function _toArray
            if (o is Component) {
                val c: Component? = o as Component?
                val pc: PageContext = ThreadLocalPageContext.get()
                if (pc != null) {
                    val member: Member = c.getMember(Component.ACCESS_PRIVATE, KeyConstants.__toArray, false, false)
                    // Object o = get(pc,"_toString",null);
                    if (member is UDF) {
                        val udf: UDF = member as UDF
                        if (udf.getReturnType() === CFTypes.TYPE_ARRAY && udf.getFunctionArguments().length === 0) {
                            return toArray(c.call(pc, KeyConstants.__toArray, arrayOfNulls<Object?>(0)))
                        }
                    }
                }
            }
            return StructAsArray.toArray(o as Struct?)
            /*
			 * Struct sct=(Struct) o; Array arr=new ArrayImpl();
			 * 
			 * Iterator<Entry<Key, Object>> it = sct.entryIterator(); Entry<Key, Object> e=null; try {
			 * while(it.hasNext()) { e = it.next(); arr.setE(toIntValue(e.getKey().getString()),e.getValue()); }
			 * } catch (ExpressionException ee) { throw new
			 * ExpressionException("can't cast struct to an array, key ["+e.getKey().getString()
			 * +"] is not a number"); } return arr;
			 */
        } else if (o is BooleanArray) return ArrayImpl(ArrayUtil.toReferenceType(o as BooleanArray?)) else if (o is ByteArray) return ArrayImpl(ArrayUtil.toReferenceType(o as ByteArray?)) else if (o is CharArray) return ArrayImpl(ArrayUtil.toReferenceType(o as CharArray?)) else if (o is ShortArray) return ArrayImpl(ArrayUtil.toReferenceType(o as ShortArray?)) else if (o is IntArray) return ArrayImpl(ArrayUtil.toReferenceType(o as IntArray?)) else if (o is LongArray) return ArrayImpl(ArrayUtil.toReferenceType(o as LongArray?)) else if (o is FloatArray) return ArrayImpl(ArrayUtil.toReferenceType(o as FloatArray?)) else if (o is DoubleArray) return ArrayImpl(ArrayUtil.toReferenceType(o as DoubleArray?))
        throw CasterException(o, "Array")
    }

    fun toNativeArray(o: Object?, defaultValue: Array<Object?>?): Array<Object?>? {
        if (o is Array<Object>) {
            return o
        } else if (o is Array) {
            val arr: Array? = o
            val objs: Array<Object?> = arrayOfNulls<Object?>(arr.size())
            for (i in objs.indices) {
                objs[i] = arr.get(i + 1, null)
            }
            return objs
        } else if (o is List) {
            return (o as List?).toArray()
        } else if (o is XMLStruct) {
            val sct: XMLStruct? = o as XMLStruct?
            // if(sct instanceof XMLMultiElementStruct) return toNativeArray((sct));
            val a: Array<Object?> = arrayOfNulls<Object?>(1)
            a[0] = sct
            return a
        } else if (o is ObjectWrap) {
            return toNativeArray((o as ObjectWrap?).getEmbededObject(defaultValue), defaultValue)
        } else if (o is Struct) {
            val sct: Struct? = o as Struct?
            val arr: Array = ArrayImpl()
            val it: Iterator<Entry<Key?, Object?>?> = sct.entryIterator()
            var e: Entry<Key?, Object?>? = null
            try {
                while (it.hasNext()) {
                    e = it.next()
                    arr.setEL(toIntValue(e.getKey().getString()), e.getValue())
                }
            } catch (ee: ExpressionException) {
                return defaultValue
            }
            return toNativeArray(arr, defaultValue)
        } else if (o is BooleanArray) return ArrayUtil.toReferenceType(o as BooleanArray?) else if (o is ByteArray) return ArrayUtil.toReferenceType(o as ByteArray?) else if (o is CharArray) return ArrayUtil.toReferenceType(o as CharArray?) else if (o is ShortArray) return ArrayUtil.toReferenceType(o as ShortArray?) else if (o is IntArray) return ArrayUtil.toReferenceType(o as IntArray?) else if (o is LongArray) return ArrayUtil.toReferenceType(o as LongArray?) else if (o is FloatArray) return ArrayUtil.toReferenceType(o as FloatArray?) else if (o is DoubleArray) return ArrayUtil.toReferenceType(o as DoubleArray?)
        return defaultValue
    }

    @Throws(PageException::class)
    fun toNativeArray(o: Object?): Array<Object?>? {
        if (o is Array<Object>) {
            return o
        } else if (o is Array) {
            val arr: Array? = o
            val objs: Array<Object?> = arrayOfNulls<Object?>(arr.size())
            for (i in objs.indices) {
                objs[i] = arr.get(i + 1, null)
            }
            return objs
        } else if (o is List) {
            return (o as List?).toArray()
        } else if (o is XMLStruct) {
            val sct: XMLStruct? = o as XMLStruct?
            // if(sct instanceof XMLMultiElementStruct) return toNativeArray((sct));
            val a: Array<Object?> = arrayOfNulls<Object?>(1)
            a[0] = sct
            return a
        } else if (o is ObjectWrap) {
            return toNativeArray((o as ObjectWrap?).getEmbededObject())
        } else if (o is Struct) {
            val sct: Struct? = o as Struct?
            val arr: Array = ArrayImpl()
            val it: Iterator<Entry<Key?, Object?>?> = sct.entryIterator()
            var e: Entry<Key?, Object?>? = null
            try {
                while (it.hasNext()) {
                    e = it.next()
                    arr.setE(toIntValue(e.getKey().getString()), e.getValue())
                }
            } catch (ee: ExpressionException) {
                throw ExpressionException("can't cast struct to an array, key [" + e.getKey().toString() + "] is not a number")
            }
            return toNativeArray(arr)
        } else if (o is BooleanArray) return ArrayUtil.toReferenceType(o as BooleanArray?) else if (o is ByteArray) return ArrayUtil.toReferenceType(o as ByteArray?) else if (o is CharArray) return ArrayUtil.toReferenceType(o as CharArray?) else if (o is ShortArray) return ArrayUtil.toReferenceType(o as ShortArray?) else if (o is IntArray) return ArrayUtil.toReferenceType(o as IntArray?) else if (o is LongArray) return ArrayUtil.toReferenceType(o as LongArray?) else if (o is FloatArray) return ArrayUtil.toReferenceType(o as FloatArray?) else if (o is DoubleArray) return ArrayUtil.toReferenceType(o as DoubleArray?)
        throw CasterException(o, "Array")
    }

    /**
     * cast an Object to an Array Object
     *
     * @param o Object to cast
     * @param defaultValue
     * @return casted Array
     */
    fun toArray(o: Object?, defaultValue: Array?): Array? {
        if (o is Array) return o else if (o is Array<Object>) {
            return ArrayImpl(o as Array<Object?>?)
        } else if (o is List) {
            return ArrayImpl((o as List?).toArray())
        } else if (o is Set) {
            return ArrayImpl((o as Set?).toArray())
        } else if (o is XMLStruct) {
            val arr: Array = ArrayImpl()
            arr.appendEL(o)
            return arr
        } else if (o is ObjectWrap) {
            return toArray((o as ObjectWrap?).getEmbededObject(defaultValue), defaultValue)
            // if(io!=null)return toArray(io,defaultValue);
        } else if (o is Struct) {
            return StructAsArray.toArray(o as Struct?, defaultValue)

            /*
			 * Struct sct=(Struct) o; Array arr=new ArrayImpl();
			 * 
			 * Iterator<Entry<Key, Object>> it = sct.entryIterator(); Entry<Key, Object> e=null; try {
			 * while(it.hasNext()) { e=it.next(); arr.setEL(toIntValue(e.getKey().getString()),e.getValue()); }
			 * } catch (ExpressionException ee) { return defaultValue; } return arr;
			 */
        } else if (o is BooleanArray) return ArrayImpl(ArrayUtil.toReferenceType(o as BooleanArray?)) else if (o is ByteArray) return ArrayImpl(ArrayUtil.toReferenceType(o as ByteArray?)) else if (o is CharArray) return ArrayImpl(ArrayUtil.toReferenceType(o as CharArray?)) else if (o is ShortArray) return ArrayImpl(ArrayUtil.toReferenceType(o as ShortArray?)) else if (o is IntArray) return ArrayImpl(ArrayUtil.toReferenceType(o as IntArray?)) else if (o is LongArray) return ArrayImpl(ArrayUtil.toReferenceType(o as LongArray?)) else if (o is FloatArray) return ArrayImpl(ArrayUtil.toReferenceType(o as FloatArray?)) else if (o is DoubleArray) return ArrayImpl(ArrayUtil.toReferenceType(o as DoubleArray?))
        return defaultValue
    }

    /**
     * cast an Object to a Map Object
     *
     * @param o Object to cast
     * @return casted Struct
     * @throws PageException
     */
    @Throws(PageException::class)
    fun toMap(o: Object?): Map? {
        return toMap(o, false)
    }

    /**
     * cast an Object to a Map Object
     *
     * @param o Object to cast
     * @param defaultValue
     * @return casted Struct
     */
    fun toMap(o: Object?, defaultValue: Map?): Map? {
        return toMap(o, false, defaultValue)
    }

    /**
     * cast an Object to a Map Object
     *
     * @param o Object to cast
     * @param duplicate
     * @param defaultValue
     * @return casted Struct
     */
    fun toMap(o: Object?, duplicate: Boolean, defaultValue: Map?): Map? {
        return try {
            toMap(o, duplicate)
        } catch (e: PageException) {
            defaultValue
        }
    }

    /**
     * cast an Object to a Map Object
     *
     * @param o Object to cast
     * @param duplicate
     * @return casted Struct
     * @throws PageException
     */
    @Throws(PageException::class)
    fun toMap(o: Object?, duplicate: Boolean): Map? {
        if (o is Struct) {
            if (o is Component) {
                try {
                    val tmp: Object = Reflector.componentToClass(ThreadLocalPageContext.get(), o as Component?, Map::class.java)
                    if (tmp is Map) return tmp
                } catch (e: PageException) {
                }
            }
            return if (duplicate) Duplicator.duplicate(o, false) else o as Struct?
        } else if (o is Map) {
            return if (duplicate) Duplicator.duplicate(o, false) else o
        } else if (o is Node) {
            return if (duplicate) {
                toMap(XMLCaster.toXMLStruct(o as Node?, false), duplicate)
            } else XMLCaster.toXMLStruct(o as Node?, false)
        } else if (o is ObjectWrap) {
            return toMap((o as ObjectWrap?).getEmbededObject(), duplicate)
        }
        throw CasterException(o, "Map")
    }

    fun toStruct(qry: Query?, row: Int): Struct? {
        val names: Array<Key?> = qry.getColumnNames()
        val sct: Struct = StructImpl()
        for (i in names.indices) {
            sct.setEL(names[i], qry.getAt(names[i], row, null))
        }
        return sct
    }

    /**
     * cast an Object to a Struct Object
     *
     * @param o Object to cast
     * @param defaultValue
     * @return casted Struct
     */
    fun toStruct(o: Object?, defaultValue: Struct?, caseSensitive: Boolean): Struct? {
        if (o is Struct) return o as Struct? else if (o is Map) {
            return MapAsStruct.toStruct(o as Map?, caseSensitive)
        } else if (o is Node) return XMLCaster.toXMLStruct(o as Node?, false) else if (o is ObjectWrap) {
            return toStruct((o as ObjectWrap?).getEmbededObject(defaultValue), defaultValue, caseSensitive)
        }
        return defaultValue
    }

    /**
     * cast an Object to a Struct Object
     *
     * @param o Object to cast
     * @return casted Struct
     */
    @Throws(PageException::class)
    fun toStruct(o: Object?): Struct? {
        return toStruct(o, true)
    }

    fun toStruct(o: Object?, defaultValue: Struct?): Struct? {
        return toStruct(o, defaultValue, true)
    }

    @Throws(PageException::class)
    fun toStruct(o: Object?, caseSensitive: Boolean): Struct? {
        if (o is Struct) return o as Struct? else if (o is Map) return MapAsStruct.toStruct(o as Map?, caseSensitive) // _toStruct((Map)o,caseSensitive);
        else if (o is Node) return XMLCaster.toXMLStruct(o as Node?, false) else if (o is ObjectWrap) {
            if (o is JavaObject) {
                val sct: Struct? = toStruct((o as JavaObject?).getEmbededObject(null), null, caseSensitive)
                if (sct != null) return sct
                val jo: JavaObject? = o as JavaObject?
                return ObjectStruct(jo)
            }
            return toStruct((o as ObjectWrap?).getEmbededObject(), caseSensitive)
        }
        if (Decision.isSimpleValue(o) || Decision.isArray(o)) throw CasterException(o, "Struct")
        if (o is Collection) return CollectionStruct(o as Collection?)
        if (o == null) throw CasterException("null can not be casted to a Struct")
        return ObjectStruct(o)
    }
    /*
	 * private static Struct _toStruct(Map map) { Struct sct = new StructImpl(); Iterator
	 * it=map.keySet().iterator(); while(it.hasNext()) { Object key=it.next();
	 * sct.set(StringUtil.toLowerCase(Caster.toString(key)),map.get(key)); } return sct; }
	 */
    /**
     * cast an Object to a Binary
     *
     * @param o Object to cast
     * @return casted Binary
     * @throws PageException
     */
    @Throws(PageException::class)
    fun toBinary(o: Object?): ByteArray? {
        if (o is ByteArray) return o else if (o is ObjectWrap) return toBinary((o as ObjectWrap?).getEmbededObject("")) else if (o is InputStream) {
            val barr = ByteArrayOutputStream()
            try {
                IOUtil.copy(o as InputStream?, barr, false, true)
            } catch (e: IOException) {
                throw ExpressionException.newInstance(e)
            }
            return barr.toByteArray()
        } else if (o != null && o.getClass().getName().equals("org.tachyon.extension.image.Image")) {
            return ImageUtil.getImageBytes(o, null)
        } else if (o is BufferedImage) {
            return ImageUtil.getImageBytes(o as BufferedImage?)
        } else if (o is ByteArrayOutputStream) {
            return (o as ByteArrayOutputStream?).toByteArray()
        } else if (o is Blob) {
            var `is`: InputStream? = null
            return try {
                `is` = (o as Blob?).getBinaryStream()
                IOUtil.toBytes(`is`)
            } catch (e: Exception) {
                throw toPageException(e)
            } finally {
                try {
                    IOUtil.close(`is`)
                } catch (e: IOException) {
                    throw toPageException(e)
                }
            }
        }
        return try {
            Base64Encoder.decode(toString(o), false)
        } catch (e: PageException) {
            throw CasterException(o, "binary")
        } catch (e: CoderException) {
            val ce = CasterException(e.getMessage())
            ce.initCause(e)
            throw ce
        }
    }

    /**
     * cast an Object to a Binary
     *
     * @param o Object to cast
     * @param defaultValue
     * @return casted Binary
     */
    fun toBinary(o: Object?, defaultValue: ByteArray?): ByteArray? {
        return try {
            toBinary(o)
        } catch (e: PageException) {
            defaultValue
        }
    }

    @Throws(PageException::class)
    fun toCreditCard(o: Object?): Object? {
        return ValidateCreditCard.toCreditcard(toString(o))
    }

    fun toCreditCard(o: Object?, defaultValue: String?): Object? {
        // print.out("enter");
        val str = toString(o, null) ?: return defaultValue
        // print.out("enter:"+str+":"+ValidateCreditCard.toCreditcard(str,defaultValue));
        return ValidateCreditCard.toCreditcard(str, defaultValue)
    }

    @Throws(PageException::class)
    fun toBase64(o: Object?): String? {
        return toBase64(o, "UTF-8", null) ?: throw CasterException(o, "base 64")
    }

    /**
     * cast an Object to a Base64 value
     *
     * @param o Object to cast
     * @return to Base64 String
     * @throws PageException
     */
    @Throws(PageException::class)
    fun toBase64(o: Object?, charset: String?): String? {
        return toBase64(o, charset, null) ?: throw CasterException(o, "base 64")
    }

    /**
     * cast an Object to a Base64 value
     *
     * @param o Object to cast
     * @param defaultValue
     * @return to Base64 String
     */
    fun toBase64(o: Object?, charset: String?, defaultValue: String?): String? {
        return if (o is ByteArray) toB64(o as ByteArray?, defaultValue) else if (o is String) toB64(o as String?, charset, defaultValue) else if (o is Number) toB64(toString(o as Number?), charset, defaultValue) else if (o is ObjectWrap) {
            toBase64((o as ObjectWrap?).getEmbededObject(defaultValue), charset, defaultValue)
        } else if (o == null) {
            toBase64("", charset, defaultValue)
        } else {
            val b = toBinary(o, null)
            if (b != null) toB64(b, defaultValue) else {
                val str = toString(o, null)
                if (str != null) toBase64(str, charset, defaultValue) else defaultValue
            }
        }
    }

    @Throws(UnsupportedEncodingException::class)
    fun toB64(str: String?, charset: String?): String? {
        return toB64(str.getBytes(charset))
    }

    fun toB64(b: ByteArray?): String? {
        return Base64Coder.encode(b)
    }

    fun toB64(str: String?, charset: String?, defaultValue: String?): String? {
        var charset = charset
        if (StringUtil.isEmpty(charset, true)) charset = "UTF-8"
        return try {
            Base64Coder.encodeFromString(str, charset)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            defaultValue
        }
    }

    fun toB64(b: ByteArray?, defaultValue: String?): String? {
        return try {
            Base64Coder.encode(b)
        } catch (e: Exception) {
            defaultValue
        }
    }

    /**
     * cast a boolean to a DateTime Object
     *
     * @param b boolean to cast
     * @param tz
     * @return casted DateTime Object
     */
    fun toDate(b: Boolean, tz: TimeZone?): DateTime? {
        return DateCaster.toDateSimple(b, tz)
    }

    fun toDate(b: Boolean?, tz: TimeZone?): DateTime? {
        return DateCaster.toDateSimple(b, tz)
    }

    /**
     * cast a char to a DateTime Object
     *
     * @param c char to cast
     * @param tz
     * @return casted DateTime Object
     */
    fun toDate(c: Char, tz: TimeZone?): DateTime? {
        return DateCaster.toDateSimple(c, tz)
    }

    /**
     * cast a double to a DateTime Object
     *
     * @param d double to cast
     * @param tz
     * @return casted DateTime Object
     */
    fun toDate(d: Double, tz: TimeZone?): DateTime? {
        return DateCaster.toDateSimple(d, tz)
    }

    fun toDate(n: Number?, tz: TimeZone?): DateTime? {
        return DateCaster.toDateSimple(n.doubleValue(), tz)
    }

    /**
     * cast an Object to a DateTime Object
     *
     * @param o Object to cast
     * @param tz
     * @return casted DateTime Object
     * @throws PageException
     */
    @Throws(PageException::class)
    fun toDate(o: Object?, tz: TimeZone?): DateTime? {
        return DateCaster.toDateAdvanced(o, tz)
    }

    /**
     * cast an Object to a DateTime Object
     *
     * @param str String to cast
     * @param tz
     * @return casted DateTime Object
     * @throws PageException
     */
    @Throws(PageException::class)
    fun toDate(str: String?, tz: TimeZone?): DateTime? {
        return DateCaster.toDateAdvanced(str, tz)
    }

    @Throws(PageException::class)
    fun toDate(o: Object?): DateTime? {
        return DateCaster.toDateAdvanced(o, DateCaster.CONVERTING_TYPE_OFFSET, ThreadLocalPageContext.getTimeZone())
    }

    /**
     * cast an Object to a DateTime Object
     *
     * @param o Object to cast
     * @param alsoNumbers define if also numbers will casted to a datetime value
     * @param tz
     * @return casted DateTime Object
     * @throws PageException
     */
    @Throws(PageException::class)
    fun toDate(o: Object?, alsoNumbers: Boolean, tz: TimeZone?): DateTime? {
        return DateCaster.toDateAdvanced(o, if (alsoNumbers) DateCaster.CONVERTING_TYPE_OFFSET else DateCaster.CONVERTING_TYPE_NONE, tz)
    }

    /**
     * cast an Object to a DateTime Object
     *
     * @param o Object to cast
     * @param alsoNumbers define if also numbers will casted to a datetime value
     * @param tz
     * @param defaultValue
     * @return casted DateTime Object
     */
    fun toDate(o: Object?, alsoNumbers: Boolean, tz: TimeZone?, defaultValue: DateTime?): DateTime? {
        return DateCaster.toDateAdvanced(o, if (alsoNumbers) DateCaster.CONVERTING_TYPE_OFFSET else DateCaster.CONVERTING_TYPE_NONE, tz, defaultValue)
    }

    /**
     * cast an Object to a DateTime Object
     *
     * @param str String to cast
     * @param alsoNumbers define if also numbers will casted to a datetime value
     * @param tz
     * @param defaultValue
     * @return casted DateTime Object
     */
    fun toDate(str: String?, alsoNumbers: Boolean, tz: TimeZone?, defaultValue: DateTime?): DateTime? {
        return DateCaster.toDateAdvanced(str, if (alsoNumbers) DateCaster.CONVERTING_TYPE_OFFSET else DateCaster.CONVERTING_TYPE_NONE, tz, defaultValue)
    }

    /**
     * cast an Object to a DateTime Object
     *
     * @param o Object to cast
     * @param tz
     * @return casted DateTime Object
     * @throws PageException
     */
    @Throws(PageException::class)
    fun toDateTime(o: Object?, tz: TimeZone?): DateTime? {
        return DateCaster.toDateAdvanced(o, tz)
    }

    /**
     * cast an Object to a DateTime Object (alias for toDateTime)
     *
     * @param o Object to cast
     * @param tz
     * @return casted DateTime Object
     * @throws PageException
     */
    @Throws(PageException::class)
    fun toDatetime(o: Object?, tz: TimeZone?): DateTime? {
        return DateCaster.toDateAdvanced(o, tz)
    }

    /**
     * cast an Object to a Query Object
     *
     * @param o Object to cast
     * @return casted Query Object
     * @throws PageException
     */
    @Throws(PageException::class)
    fun toQuery(o: Object?): Query? {
        if (o is Query) return o as Query?
        if (o is ObjectWrap) {
            return toQuery((o as ObjectWrap?).getEmbededObject())
        }
        if (o is ResultSet) return QueryImpl(o as ResultSet?, "query", ThreadLocalPageContext.getTimeZone())
        if (o is Component) {
            val member: Member = (o as Component?).getMember(Component.ACCESS_PRIVATE, KeyConstants.__toQuery, false, false)
            if (member is UDF) {
                val udf: UDF = member as UDF
                if (udf.getReturnType() === CFTypes.TYPE_QUERY && udf.getFunctionArguments().length === 0) {
                    return toQuery((o as Component?).call(ThreadLocalPageContext.get(), KeyConstants.__toQuery, arrayOf<Object?>()))
                }
            }
        }
        throw CasterException(o, "query")
    }

    /**
     * converts an Object to a QueryColumn, if possible
     *
     * @param o
     * @return
     * @throws PageException
     */
    @Throws(PageException::class)
    fun toQueryColumn(o: Object?): QueryColumn? {
        if (o is QueryColumn) return o as QueryColumn?
        throw CasterException(o, "querycolumn")
    }

    fun toQueryColumn(o: Object?, defaultValue: QueryColumn?): QueryColumn? {
        return if (o is QueryColumn) o as QueryColumn? else defaultValue
    }

    /**
     * converts an Object to a QueryColumn, if possible, also variable declarations are allowed. this
     * method is used within the generated bytecode
     *
     * @param o
     * @return
     * @throws PageException
     * @info used in bytecode generation
     */
    @Throws(PageException::class)
    fun toQueryColumn(o: Object?, pc: PageContext?): QueryColumn? {
        var o: Object? = o
        if (o is QueryColumn) return o as QueryColumn?
        if (o is String) {
            o = VariableInterpreter.getVariableAsCollection(pc, o as String?)
            if (o is QueryColumn) return o as QueryColumn?
        }
        throw CasterException(o, "querycolumn")
    }

    /**
     * cast an Object to a Query Object
     *
     * @param o Object to cast
     * @param defaultValue
     * @return casted Query Object
     */
    fun toQuery(o: Object?, defaultValue: Query?): Query? {
        if (o is Query) return o as Query? else if (o is ObjectWrap) {
            return toQuery((o as ObjectWrap?).getEmbededObject(defaultValue), defaultValue)
        }
        return defaultValue
    }

    /**
     * cast an Object to a Query Object
     *
     * @param o Object to cast
     * @param duplicate duplicate the object or not
     * @param defaultValue
     * @return casted Query Object
     */
    fun toQuery(o: Object?, duplicate: Boolean, defaultValue: Query?): Query? {
        return try {
            toQuery(o, duplicate)
        } catch (e: PageException) {
            defaultValue
        }
    }

    /**
     * cast an Object to a Query Object
     *
     * @param o Object to cast
     * @param duplicate duplicate the object or not
     * @return casted Query Object
     * @throws PageException
     */
    @Throws(PageException::class)
    fun toQuery(o: Object?, duplicate: Boolean): Query? {
        if (o is Query) {
            if (duplicate) {
                val src: Query? = o as Query?
                val trg: Query = QueryImpl(src.getColumnNames(), src.getRowCount(), "query")
                var keys: Array<Collection.Key?> = src.getColumnNames()
                val columnsSrc: Array<QueryColumn?> = arrayOfNulls<QueryColumn?>(keys.size)
                for (i in columnsSrc.indices) {
                    columnsSrc[i] = src.getColumn(keys[i])
                }
                keys = trg.getColumnNames()
                val columnsTrg: Array<QueryColumn?> = arrayOfNulls<QueryColumn?>(keys.size)
                for (i in columnsTrg.indices) {
                    columnsTrg[i] = trg.getColumn(keys[i])
                }
                var i: Int
                for (row in trg.getRecordcount() downTo 1) {
                    i = 0
                    while (i < columnsTrg.size) {
                        columnsTrg[i].set(row, columnsSrc[i].get(row, null))
                        i++
                    }
                }
                return trg
            }
            return o as Query?
        } else if (o is ObjectWrap) {
            return toQuery((o as ObjectWrap?).getEmbededObject(), duplicate)
        }
        throw CasterException(o, "query")
    }

    /**
     * cast an Object to a UUID
     *
     * @param o Object to cast
     * @return casted Query Object
     * @throws PageException
     */
    @Throws(PageException::class)
    fun toUUId(o: Object?): Object? {
        val str: String = toString(o)
        if (!Decision.isUUId(str)) throw ExpressionException("can't cast [$str] to uuid value")
        return str
    }

    /**
     * cast an Object to a UUID
     *
     * @param o Object to cast
     * @param defaultValue
     * @return casted Query Object
     */
    fun toUUId(o: Object?, defaultValue: Object?): Object? {
        val str = toString(o, null) ?: return defaultValue
        return if (!Decision.isUUId(str)) defaultValue else str
    }

    /**
     * cast an Object to a GUID
     *
     * @param o Object to cast
     * @return casted Query Object
     * @throws PageException
     */
    @Throws(PageException::class)
    fun toGUId(o: Object?): Object? {
        val str: String = toString(o)
        if (!Decision.isGUId(str)) throw ExpressionException("can't cast [$str] to guid value")
        return str
    }

    /**
     * cast an Object to a GUID
     *
     * @param o Object to cast
     * @param defaultValue
     * @return casted Query Object
     */
    fun toGUId(o: Object?, defaultValue: Object?): Object? {
        val str = toString(o, null) ?: return defaultValue
        return if (!Decision.isGUId(str)) defaultValue else str
    }

    /**
     * cast an Object to a Variable Name
     *
     * @param o Object to cast
     * @return casted Variable Name
     * @throws PageException
     */
    @Throws(PageException::class)
    fun toVariableName(o: Object?): String? {
        val str: String = toString(o)
        if (!Decision.isVariableName(str)) throw ExpressionException("can't cast [$str] to variable name value")
        return str
    }

    /**
     * cast an Object to a Variable Name
     *
     * @param o Object to cast
     * @param defaultValue
     * @return casted Variable Name
     */
    fun toVariableName(o: Object?, defaultValue: String?): String? {
        val str = toString(o, null)
        return if (str == null || !Decision.isVariableName(str)) defaultValue else str
    }

    /**
     * cast an Object to a TimeSpan Object
     *
     * @param o Object to cast
     * @return casted TimeSpan Object
     * @throws PageException
     */
    @Throws(PageException::class)
    fun toTimeSpan(o: Object?): TimeSpan? {
        return toTimespan(o)
    }

    /**
     * cast an Object to a TimeSpan Object (alias for toTimeSpan)
     *
     * @param o Object to cast
     * @param defaultValue
     * @return casted TimeSpan Object
     */
    fun toTimespan(o: Object?, defaultValue: TimeSpan?): TimeSpan? {
        if (o is TimeSpan) return o as TimeSpan? else if (o is String) {
            val arr: Array<String?> = o.toString().split(",")
            if (arr.size == 4) {
                val values = IntArray(4)
                try {
                    for (i in arr.indices) {
                        values[i] = toIntValue(arr[i])
                    }
                    return TimeSpanImpl(values[0], values[1], values[2], values[3])
                } catch (e: ExpressionException) {
                }
            }
        } else if (o is ObjectWrap) {
            val embeded: Object = (o as ObjectWrap?).getEmbededObject(DEFAULT)
            return if (embeded === DEFAULT) defaultValue else toTimespan(embeded, defaultValue)
        }
        val dbl: Double = toDoubleValue(o, true, Double.NaN)
        return if (!Double.isNaN(dbl)) TimeSpanImpl.fromDays(dbl) else defaultValue
    }

    /**
     * cast an Object to a TimeSpan Object (alias for toTimeSpan)
     *
     * @param o Object to cast
     * @return casted TimeSpan Object
     * @throws PageException
     */
    @Throws(PageException::class)
    fun toTimespan(o: Object?): TimeSpan? {
        val ts: TimeSpan? = toTimespan(o, null)
        if (ts != null) return ts
        throw CasterException(o, "timespan")
    }

    /**
     * cast a Throwable Object to a PageException Object
     *
     * @param t Throwable to cast
     * @return casted PageException Object
     */
    fun toPageException(t: Throwable?): PageException? {
        return toPageException(t, true)
    }

    fun toPageRuntimeException(t: Throwable?): PageRuntimeException? {
        return if (t is PageRuntimeException) t as PageRuntimeException? else PageRuntimeException(toPageException(t, true))
    }

    fun toPageException(t: Throwable?, rethrowIfNecessary: Boolean): PageException? {
        if (t is PageException) {
            return t as PageException?
        }
        if (t is PageExceptionBox) {
            return (t as PageExceptionBox?).getPageException()
        }
        if (t is InvocationTargetException) {
            return toPageException((t as InvocationTargetException?).getTargetException())
        }
        if (t is ExceptionInInitializerError) {
            return toPageException((t as ExceptionInInitializerError?).getCause())
        }
        if (t is ExecutionException) {
            return toPageException((t as ExecutionException?).getCause())
        }
        if (t is InterruptedException) {
            val pc: PageContext = ThreadLocalPageContext.get()
            if (pc is PageContextImpl) {
                val pci: PageContextImpl = pc as PageContextImpl
                val tst: Array<StackTraceElement?> = pci.getTimeoutStackTrace()
                if (tst != null) {
                    return RequestTimeoutException(pc, tst)
                }
            }
        }
        if (t is OutOfMemoryError) {
            ThreadLocalPageContext.getConfig().checkPermGenSpace(true)
        }
        // Throwable cause = t.getCause();
        // if(cause!=null && cause!=t) return toPageException(cause);
        return NativeException.newInstance(t, rethrowIfNecessary)
    }

    /**
     * return the type name of an object (string, boolean, int aso.), type is not same like class name
     *
     * @param o Object to get type from
     * @return type of the object
     */
    fun toTypeName(o: Object?): String? {
        if (o == null) return "null" else if (o is String) return "string" else if (o is Boolean) return "boolean" else if (o is Number) return "int" else if (o is Array) return "array" else if (o is Component) return "component" else if (o is Struct) return "struct" else if (o is Query) return "query" else if (o is DateTime) return "datetime" else if (o is ByteArray) return "binary" else if (o is ObjectWrap) {
            return toTypeName((o as ObjectWrap?).getEmbededObject(null))
        }
        val clazz: Class = o.getClass()
        val className: String = clazz.getName()
        return if (className.startsWith("java.lang.")) {
            className.substring(10)
        } else toClassName(clazz)
    }

    fun toTypeName(clazz: Class?): String? {
        if (Reflector.isInstaneOf(clazz, String::class.java, false)) return "string"
        if (Reflector.isInstaneOf(clazz, Boolean::class.java, false)) return "boolean"
        if (Reflector.isInstaneOf(clazz, Number::class.java, false)) return "numeric"
        if (Reflector.isInstaneOf(clazz, Array::class.java, false)) return "array"
        if (Reflector.isInstaneOf(clazz, Struct::class.java, false)) return "struct"
        if (Reflector.isInstaneOf(clazz, Query::class.java, false)) return "query"
        if (Reflector.isInstaneOf(clazz, DateTime::class.java, false)) return "datetime"
        if (Reflector.isInstaneOf(clazz, ByteArray::class.java, false)) return "binary"
        val className: String = clazz.getName()
        return if (className.startsWith("java.lang.")) {
            className.substring(10)
        } else toClassName(clazz)
    }

    fun toClassName(o: Object?): String? {
        if (o == null) return "null"
        if (o is ObjectWrap) {
            try {
                return toClassName((o as ObjectWrap?).getEmbededObject())
            } catch (e: PageException) {
            }
        }
        return toClassName(o.getClass())
    }

    fun toClassName(clazz: Class?): String? {
        return if (clazz.isArray()) {
            toClassName(clazz.getComponentType()).toString() + "[]"
        } else clazz.getName()
    }

    @Throws(PageException::class)
    fun cfTypeToClass(type: String?): Class? {
        // TODO weitere typen siehe bytecode.cast.Cast
        var type = type
        type = type.trim()
        val lcType: String = StringUtil.toLowerCase(type)
        if (lcType.length() > 2) {
            val first: Char = lcType.charAt(0)
            when (first) {
                'a' -> if (lcType.equals("any")) {
                    return Object::class.java
                } else if (lcType.equals("array")) {
                    return Array::class.java
                }
                'b' -> if (lcType.equals("boolean") || lcType.equals("bool")) {
                    return Boolean::class.java
                } else if (lcType.equals("binary")) {
                    return ByteArray::class.java
                } else if (lcType.equals("base64")) {
                    return String::class.java
                } else if (lcType.equals("byte")) {
                    return Byte::class.java
                }
                'c' -> if (lcType.equals("creditcard")) {
                    return String::class.java
                } else if (lcType.equals("component") || lcType.equals("class")) {
                    return Component::class.java
                }
                'd' -> if (lcType.equals("date")) {
                    return Date::class.java
                } else if (lcType.equals("datetime")) {
                    return Date::class.java
                }
                'g' -> if (lcType.equals("guid")) {
                    return Object::class.java
                }
                'n' -> if (lcType.equals("numeric")) {
                    return Double::class.java
                } else if (lcType.equals("number")) {
                    return Double::class.java
                } else if (lcType.equals("node")) {
                    return Node::class.java
                }
                'o' -> if (lcType.equals("object")) {
                    return Object::class.java
                }
                'q' -> if (lcType.equals("query")) {
                    return Query::class.java
                }
                's' -> if (lcType.equals("string")) {
                    return String::class.java
                } else if (lcType.equals("struct")) {
                    return Struct::class.java
                }
                't' -> if (lcType.equals("timespan")) {
                    return TimeSpan::class.java
                }
                'u' -> if (lcType.equals("uuid")) {
                    return Object::class.java
                }
                'v' -> {
                    if (lcType.equals("variablename")) {
                        return Object::class.java
                    }
                    if (lcType.equals("void")) {
                        return Object::class.java
                    }
                }
                'x' -> if (lcType.equals("xml")) {
                    return Node::class.java
                }
            }
        }
        // array
        if (type.endsWith("[]")) {
            var clazz: Class? = cfTypeToClass(type.substring(0, type!!.length() - 2))
            clazz = ClassUtil.toArrayClass(clazz)
            return clazz
        }
        // check for argument
        val clazz: Class<*>
        clazz = try {
            otherTypeToClass(type)
        } catch (e: ClassException) {
            throw toPageException(e)
        }
        return clazz
    }

    @Throws(PageException::class, ClassException::class)
    private fun otherTypeToClass(type: String?): Class<*>? {
        val pc: PageContext = ThreadLocalPageContext.get()
        var pe: PageException? = null
        // try to load as cfc
        if (pc != null) {
            pe = try {
                val c: Component = pc.loadComponent(type)
                return ComponentUtil.getComponentPropertiesClass(pc, c)
            } catch (e: PageException) {
                e
            }
        }
        // try to load as class
        return try {
            ClassUtil.loadClass(type)
        } catch (ce: ClassException) {
            if (pe != null) throw pe
            throw ce
        }
    }

    @Throws(PageException::class)
    fun castTo(type: String?, o: Object?): Object? {
        return castTo(ThreadLocalPageContext.get(), type, o, false)
    }

    /**
     * cast a value to a value defined by type argument
     *
     * @param pc
     * @param type type of the returning Value
     * @param o Object to cast
     * @return casted Value
     * @throws PageException
     */
    @Throws(PageException::class)
    fun castTo(pc: PageContext?, type: String?, o: Object?, alsoPattern: Boolean): Object? {
        var type = type
        type = type.trim()
        val lctype: String = StringUtil.toLowerCase(type)
        if (lctype.length() > 2) {
            val first: Char = lctype.charAt(0)
            when (first) {
                'a' -> if (lctype.equals("any")) {
                    return o
                } else if (lctype.equals("array")) {
                    return toArray(o)
                }
                'b' -> if (lctype.equals("boolean") || lctype.equals("bool")) {
                    return toBoolean(o)
                } else if (lctype.equals("binary")) {
                    return toBinary(o)
                } else if (lctype.equals("byte[]")) {
                    return toBinary(o)
                } else if (lctype.equals("base64")) {
                    return toBase64(o, null)
                } else if (lctype.equals("bigdecimal") || lctype.equals("big_decimal")) {
                    return toBigDecimal(o)
                } else if (lctype.equals("biginteger") || lctype.equals("big_integer")) {
                    return toBigInteger(o)
                }
                'c' -> if (alsoPattern && lctype.equals("creditcard")) {
                    return toCreditCard(o)
                }
                'd' -> if (lctype.equals("date")) {
                    return DateCaster.toDateAdvanced(o, pc.getTimeZone())
                } else if (lctype.equals("datetime")) {
                    return DateCaster.toDateAdvanced(o, pc.getTimeZone())
                } else if (lctype.equals("double")) {
                    return toDouble(o)
                } else if (lctype.equals("decimal")) {
                    return toDecimal(o)
                }
                'e' -> if (lctype.equals("eurodate")) {
                    return DateCaster.toEuroDate(o, pc.getTimeZone())
                } else if (alsoPattern && lctype.equals("email")) {
                    return toEmail(o)
                }
                'f' -> if (lctype.equals("float")) {
                    return toDouble(o)
                } else if (lctype.equals("function")) {
                    return toFunction(o)
                }
                'g' -> if (lctype.equals("guid")) {
                    return toGUId(o)
                }
                'i' -> if (lctype.equals("integer") || lctype.equals("int")) {
                    return toInteger(o)
                }
                'l' -> if (lctype.equals("long")) {
                    return toLong(o)
                }
                'n' -> if (lctype.equals("numeric")) {
                    return toDouble(o)
                } else if (lctype.equals("number")) {
                    return toDouble(o)
                } else if (lctype.equals("node")) {
                    return toXML(o)
                }
                'o' -> if (lctype.equals("object")) {
                    return o
                } else if (lctype.equals("other")) {
                    return o
                } else if (lctype.equals("org.w3c.dom.element")) {
                    return toElement(o)
                }
                'p' -> if (alsoPattern && lctype.equals("phone")) {
                    return toPhone(o)
                }
                'q' -> if (lctype.equals("query")) {
                    return toQuery(o)
                }
                's' -> if (lctype.equals("string")) {
                    return toString(o)
                } else if (lctype.equals("struct")) {
                    return toStruct(o)
                } else if (lctype.equals("short")) {
                    return toShort(o)
                } else if (alsoPattern && (lctype.equals("ssn") || lctype.equals("social_security_number"))) {
                    return toSSN(o)
                }
                't' -> {
                    if (lctype.equals("timespan")) {
                        return toTimespan(o)
                    }
                    if (lctype.equals("time")) {
                        return DateCaster.toDateAdvanced(o, pc.getTimeZone())
                    }
                    if (alsoPattern && lctype.equals("telephone")) {
                        return toPhone(o)
                    }
                }
                'u' -> {
                    if (lctype.equals("uuid")) {
                        return toUUId(o)
                    }
                    if (alsoPattern && lctype.equals("url")) {
                        return toURL(o)
                    }
                    if (lctype.equals("usdate")) {
                        return DateCaster.toUSDate(o, pc.getTimeZone())
                        // return DateCaster.toDate(o,pc.getTimeZone());
                    }
                }
                'v' -> if (lctype.equals("variablename")) {
                    return toVariableName(o)
                } else if (lctype.equals("void")) {
                    return toVoid(o)
                } else if (lctype.equals("variable_name")) {
                    return toVariableName(o)
                } else if (lctype.equals("variable-name")) {
                    return toVariableName(o)
                }
                'x' -> {
                    if (lctype.equals("xml")) {
                        return toXML(o)
                    }
                    if (alsoPattern && (lctype.equals("zip") || lctype.equals("zipcode"))) {
                        return toZip(o)
                    }
                }
                'z' -> if (alsoPattern && (lctype.equals("zip") || lctype.equals("zipcode"))) {
                    return toZip(o)
                }
            }
        }

        // <type>[]
        if (lctype.endsWith("[]")) {
            val componentType: String = lctype.substring(0, lctype.length() - 2)
            val src: Array<Object?>? = toNativeArray(o)
            val trg: Array = ArrayImpl()
            for (i in src.indices) {
                if (src!![i] == null) {
                    continue
                }
                trg.setE(i + 1, castTo(pc, componentType, src[i], alsoPattern))
            }
            return trg
        }
        return _castTo(pc, type, o)
    }

    @Throws(PageException::class)
    fun toZip(o: Object?): String? {
        val str: String = toString(o)
        if (Decision.isZipCode(str)) return str
        throw ExpressionException("can't cast value [$str] to a zip code")
    }

    fun toZip(o: Object?, defaultValue: String?): String? {
        val str = toString(o, null) ?: return defaultValue
        return if (Decision.isZipCode(str)) str else defaultValue
    }

    @Throws(PageException::class)
    fun toURL(o: Object?): String? {
        val str: String = toString(o)
        return if (Decision.isURL(str)) str else try {
            HTTPUtil.toURL(str, HTTPUtil.ENCODED_AUTO).toExternalForm()
        } catch (e: MalformedURLException) {
            throw ExpressionException("can't cast value [$str] to a URL", e.getMessage())
        }
    }

    fun toURL(o: Object?, defaultValue: String?): String? {
        val str = toString(o, null) ?: return defaultValue
        return if (Decision.isURL(str)) str else try {
            HTTPUtil.toURL(str, HTTPUtil.ENCODED_AUTO).toExternalForm()
        } catch (e: MalformedURLException) {
            defaultValue
        }
    }

    @Throws(PageException::class)
    fun toPhone(o: Object?): String? {
        val str: String = toString(o)
        if (Decision.isPhone(str)) return str
        throw ExpressionException("can't cast value [$str] to a telephone number")
    }

    fun toPhone(o: Object?, defaultValue: String?): String? {
        val str = toString(o, null) ?: return defaultValue
        return if (Decision.isPhone(str)) str else defaultValue
    }

    @Throws(PageException::class)
    fun toSSN(o: Object?): String? {
        val str: String = toString(o)
        if (Decision.isSSN(str)) return str
        throw ExpressionException("can't cast value [$str] to a U.S. social security number")
    }

    fun toSSN(o: Object?, defaultValue: String?): String? {
        val str = toString(o, null) ?: return defaultValue
        return if (Decision.isSSN(str)) str else defaultValue
    }

    @Throws(PageException::class)
    fun toEmail(o: Object?): String? {
        val str: String = toString(o)
        if (Decision.isEmail(str)) return str
        throw ExpressionException("can't cast value [$str] to an E-Mail Address")
    }

    fun toEmail(o: Object?, defaultValue: String?): String? {
        val str = toString(o, null) ?: return defaultValue
        return if (Decision.isEmail(str)) str else defaultValue
    }

    fun castTo(pc: PageContext?, type: Short, strType: String?, o: Object?, defaultValue: Object?): Object? {
        // TODO weitere typen siehe bytecode.cast.Cast
        var res: Object? = null
        if (type == CFTypes.TYPE_ANY) return o else if (type == CFTypes.TYPE_ARRAY) res = toArray(o, null) else if (type == CFTypes.TYPE_BOOLEAN) res = toBoolean(o, null) else if (type == CFTypes.TYPE_BINARY) res = toBinary(o, null) else if (type == CFTypes.TYPE_DATETIME) res = DateCaster.toDateAdvanced(o, pc.getTimeZone(), null) else if (type == CFTypes.TYPE_NUMERIC) res = toDouble(o, null) else if (type == CFTypes.TYPE_QUERY) res = toQuery(o, null) else if (type == CFTypes.TYPE_QUERY_COLUMN) res = toQueryColumn(o, null as QueryColumn?) else if (type == CFTypes.TYPE_STRING) res = toString(o, null) else if (type == CFTypes.TYPE_STRUCT) res = toStruct(o, null) else if (type == CFTypes.TYPE_TIMESPAN) res = toTimespan(o, null) else if (type == CFTypes.TYPE_UUID) res = toUUId(o, null) else if (type == CFTypes.TYPE_GUID) res = toGUId(o, null) else if (type == CFTypes.TYPE_VARIABLE_NAME) res = toVariableName(o, null) else if (type == CFTypes.TYPE_VOID) res = toVoid(o, null) else if (type == CFTypes.TYPE_XML) res = toXML(o, null) else if (type == CFTypes.TYPE_FUNCTION) res = toFunction(o, null) else if (type == CFTypes.TYPE_LOCALE) res = toLocale(o, null) else if (type == CFTypes.TYPE_TIMEZONE) res = toTimeZone(o, null)
        return if (res != null) res else try {
            _castTo(pc, strType, o)
        } catch (e: PageException) {
            defaultValue
        }
    }

    /**
     * cast a value to a value defined by type argument
     *
     * @param pc
     * @param type type of the returning Value
     * @param strType type as String
     * @param o Object to cast
     * @return casted Value
     * @throws PageException
     */
    @Throws(PageException::class)
    fun castTo(pc: PageContext?, type: Short, strType: String?, o: Object?): Object? {
        // TODO weitere typen siehe bytecode.cast.Cast
        if (type == CFTypes.TYPE_ANY) return o else if (type == CFTypes.TYPE_ARRAY) return toArray(o) else if (type == CFTypes.TYPE_BOOLEAN) return toBoolean(o) else if (type == CFTypes.TYPE_BINARY) return toBinary(o) else if (type == CFTypes.TYPE_DATETIME) return DateCaster.toDateAdvanced(o, pc.getTimeZone()) else if (type == CFTypes.TYPE_NUMERIC) return toDouble(o) else if (type == CFTypes.TYPE_QUERY) return toQuery(o) else if (type == CFTypes.TYPE_QUERY_COLUMN) return toQueryColumn(o) else if (type == CFTypes.TYPE_STRING) return toString(o) else if (type == CFTypes.TYPE_STRUCT) return toStruct(o) else if (type == CFTypes.TYPE_TIMESPAN) return toTimespan(o) else if (type == CFTypes.TYPE_UUID) return toUUId(o) else if (type == CFTypes.TYPE_GUID) return toGUId(o) else if (type == CFTypes.TYPE_VARIABLE_NAME) return toVariableName(o) else if (type == CFTypes.TYPE_VOID) return toVoid(o) else if (type == CFTypes.TYPE_XML) return toXML(o) else if (type == CFTypes.TYPE_FUNCTION) return toFunction(o) else if (type == CFTypes.TYPE_LOCALE) return toLocale(o) else if (type == CFTypes.TYPE_TIMEZONE) return toTimeZone(o)
        return _castTo(pc, strType, o)
    }

    @Throws(PageException::class)
    private fun _castTo(pc: PageContext?, strType: String?, o: Object?): Object? {
        if (o is Component) {
            val comp: Component? = o as Component?
            if (comp.instanceOf(strType)) return o
            try {
                val trgClass: Class<*> = ClassUtil.loadClass(strType)
                if (trgClass.isInterface()) {
                    return Reflector.componentToClass(pc, comp, trgClass)
                }
            } catch (ce: ClassException) {
                throw toPageException(ce)
            }
            throw ExpressionException("can't cast Component of Type [" + comp.getAbsName().toString() + "] to [" + strType.toString() + "]")
        }
        if (o is UDF) {
            try {
                val trgClass: Class<*> = ClassUtil.loadClass(strType)
                if (trgClass.isInterface()) {
                    return Reflector.udfToClass(pc, o as UDF?, trgClass)
                }
            } catch (ce: ClassException) {
                throw toPageException(ce)
            }
        }
        if (o is Pojo) {
            val cfc: Component? = toComponent(pc, o as Pojo?, strType, null)
            if (cfc != null) return cfc
            throw ExpressionException("can't cast Pojo of Type [" + o.getClass().getName().toString() + "] to [" + strType.toString() + "]")
        }
        if (strType.endsWith("[]") && Decision.isArray(o)) {
            val _strType: String = strType.substring(0, strType!!.length() - 2)
            val _type: Short = CFTypes.toShort(_strType, false, (-1).toShort())
            val arr: Array? = toArray(o, null)
            if (arr != null) {

                // convert the values
                val it: Iterator<Entry<Key?, Object?>?> = arr.entryIterator()
                val _arr: Array = ArrayImpl()
                var e: Entry<Key?, Object?>?
                var src: Object
                var trg: Object?
                var hasChanged = false
                while (it.hasNext()) {
                    e = it.next()
                    src = e.getValue()
                    trg = castTo(pc, _type, _strType, src)
                    _arr.setEL(e.getKey(), trg)
                    if (src !== trg) hasChanged = true
                }
                return if (!hasChanged) arr else _arr
            }
        }
        throw CasterException(o, strType)
    }

    fun toComponent(pc: PageContext?, pojo: Pojo?, compPath: String?, defaultValue: Component?): Component? {
        try {
            val cfc: Component = pc.loadComponent(compPath)
            val props: Array<Property?> = cfc.getProperties(false, true, false, false)
            val it: tachyon.runtime.net.rpc.PojoIterator = PojoIterator(pojo)
            // only when the same amount of properties
            if (props.size == it!!.size()) {
                val propMap: Map<Collection.Key?, Property?>? = propToMap(props)
                var p: Property?
                var pair: Pair<Collection.Key?, Object?>
                val scope: ComponentScope = cfc.getComponentScope()
                while (it!!.hasNext()) {
                    pair = it!!.next()
                    p = propMap!![pair.getName()]
                    if (p == null) return defaultValue
                    var `val`: Object? = null
                    try {
                        `val` = castTo(pc, p.getType(), pair.getValue(), false)
                    } catch (e: PageException) {
                    }

                    // store in variables and this scope
                    scope.setEL(pair.getName(), `val`)
                    cfc.setEL(pair.getName(), `val`)
                }
                return cfc
            }
        } catch (e: PageException) {
        }
        return defaultValue
    }

    private fun propToMap(props: Array<Property?>?): Map<Key?, Property?>? {
        val map: Map<Collection.Key?, Property?> = HashMap()
        for (p in props!!) {
            map.put(KeyImpl.init(p.getName()), p)
        }
        return map
    }

    /**
     * cast a value to a value defined by type argument
     *
     * @param pc
     * @param type type of the returning Value
     * @param o Object to cast
     * @return casted Value
     * @throws PageException
     */
    @Throws(PageException::class)
    fun castTo(pc: PageContext?, type: Short, o: Object?): Object? {
        if (type == CFTypes.TYPE_ANY) return o else if (type == CFTypes.TYPE_ARRAY) return toArray(o) else if (type == CFTypes.TYPE_BOOLEAN) return toBoolean(o) else if (type == CFTypes.TYPE_BINARY) return toBinary(o) else if (type == CFTypes.TYPE_DATETIME) return DateCaster.toDateAdvanced(o, pc.getTimeZone()) else if (type == CFTypes.TYPE_NUMERIC) return toDouble(o) else if (type == CFTypes.TYPE_QUERY) return toQuery(o) else if (type == CFTypes.TYPE_QUERY_COLUMN) return toQueryColumn(o) else if (type == CFTypes.TYPE_STRING) return toString(o) else if (type == CFTypes.TYPE_STRUCT) return toStruct(o) else if (type == CFTypes.TYPE_TIMESPAN) return toTimespan(o) else if (type == CFTypes.TYPE_UUID) return toGUId(o) else if (type == CFTypes.TYPE_UUID) return toUUId(o) else if (type == CFTypes.TYPE_VARIABLE_NAME) return toVariableName(o) else if (type == CFTypes.TYPE_VOID) return toVoid(o) else if (type == CFTypes.TYPE_FUNCTION) return toFunction(o) else if (type == CFTypes.TYPE_XML) return toXML(o)
        // ext.img else if(type==CFTypes.TYPE_IMAGE) return ImageUtil.toImage(pc,o,true);
        if (type == CFTypes.TYPE_UNDEFINED) throw ExpressionException("type isn't defined (TYPE_UNDEFINED)")
        throw ExpressionException("invalid type [$type]")
    }

    /**
     * cast a value to void (Empty String)
     *
     * @param o
     * @return void value
     * @throws ExpressionException
     */
    @Throws(ExpressionException::class)
    fun toVoid(o: Object?): Object? {
        if (o == null) return null else if (o is String && o.toString().length() === 0) return null else if (o is Number && (o as Number?).intValue() === 0) return null else if (o is Boolean && (o as Boolean?).booleanValue() === false) return null else if (o is ObjectWrap) return toVoid((o as ObjectWrap?).getEmbededObject(null))
        throw CasterException(o, "void")
    }

    /**
     * cast a value to void (Empty String)
     *
     * @param o
     * @param defaultValue
     * @return void value
     */
    fun toVoid(o: Object?, defaultValue: Object?): Object? {
        if (o == null) return null else if (o is String && o.toString().length() === 0) return null else if (o is Number && (o as Number?).intValue() === 0) return null else if (o is Boolean && (o as Boolean?).booleanValue() === false) return null else if (o is ObjectWrap) return toVoid((o as ObjectWrap?).getEmbededObject(defaultValue), defaultValue)
        return defaultValue
    }

    /**
     * cast an Object to a reference type (Object), in that case this method to nothing, because an
     * Object is already a reference type
     *
     * @param o Object to cast
     * @return casted Object
     */
    fun toRef(o: Object?): Object? {
        return o
    }

    /**
     * cast a String to a reference type (Object), in that case this method to nothing, because a String
     * is already a reference type
     *
     * @param o Object to cast
     * @return casted Object
     */
    fun toRef(o: String?): String? {
        return o
    }

    /**
     * cast a Collection to a reference type (Object), in that case this method to nothing, because a
     * Collection is already a reference type
     *
     * @param o Collection to cast
     * @return casted Object
     */
    fun toRef(o: Collection?): Collection? {
        return o
    }

    /**
     * cast a char value to his (CFML) reference type String
     *
     * @param c char to cast
     * @return casted String
     */
    fun toRef(c: Char): String? {
        return "" + c
    }

    /**
     * cast a boolean value to his (CFML) reference type Boolean
     *
     * @param b boolean to cast
     * @return casted Boolean
     */
    fun toRef(b: Boolean): Boolean? {
        return if (b) Boolean.TRUE else Boolean.FALSE
    }

    /**
     * cast a byte value to his (CFML) reference type Integer
     *
     * @param b byte to cast
     * @return casted Integer
     */
    fun toRef(b: Byte): Byte? {
        return Byte.valueOf(b)
    }

    /**
     * cast an int value to his (CFML) reference type Integer
     *
     * @param i int to cast
     * @return casted Integer
     */
    fun toRef(i: Int): Integer? {
        return Integer.valueOf(i)
    }

    /**
     * cast a float value to his (CFML) reference type Float
     *
     * @param f float to cast
     * @return casted Float
     */
    fun toRef(f: Float): Float? {
        return Float.valueOf(f)
    }

    /**
     * cast a long value to his (CFML) reference type Long
     *
     * @param l long to cast
     * @return casted Long
     */
    fun toRef(l: Long): Long? {
        return Long.valueOf(l)
    }

    /**
     * cast a double value to his (CFML) reference type Double
     *
     * @param d doble to cast
     * @return casted Double
     */
    fun toRef(d: Double): Double? {
        return Double.valueOf(d)
    }

    /**
     * cast a double value to his (CFML) reference type Double
     *
     * @param s short to cast
     * @return casted Short
     */
    fun toRef(s: Short): Short? {
        return Short.valueOf(s)
    }

    /**
     * cast an Object to an Iterator or get Iterator from Object
     *
     * @param o Object to cast
     * @return casted Collection
     * @throws PageException
     */
    @Throws(PageException::class)
    fun toIterator(o: Object?): Iterator? {
        return ForEachUtil.forEach(o)
    }

    /**
     * cast an Object to a Collection
     *
     * @param o Object to cast
     * @return casted Collection
     * @throws PageException
     */
    @Throws(PageException::class)
    fun toCollection(o: Object?): Collection? {
        if (o is Collection) return o else if (o is Node) return XMLCaster.toXMLStruct(o as Node?, false) else if (o is Component) {
            try {
                val tmp: Object = Reflector.componentToClass(ThreadLocalPageContext.get(), o as Component?, Collection::class.java)
                if (tmp is Collection) return tmp
            } catch (e: PageException) {
            }
        } else if (o is Map) {
            return MapAsStruct.toStruct(o as Map?, true) // StructImpl((Map)o);
        } else if (o is ObjectWrap) {
            return toCollection((o as ObjectWrap?).getEmbededObject())
        } else if (Decision.isCastableToArray(o)) {
            return toArray(o)
        }
        throw CasterException(o, "collection")
    }

    @Throws(PageException::class)
    fun toJavaCollection(o: Object?): Collection<*>? {
        return if (o is Collection<*>) o else toList(o)
    }

    /**
     * cast an Object to a Component
     *
     * @param o Object to cast
     * @return casted Component
     * @throws PageException
     */
    @Throws(PageException::class)
    fun toComponent(o: Object?): Component? {
        if (o is Component) return o as Component? else if (o is ObjectWrap) {
            return toComponent((o as ObjectWrap?).getEmbededObject())
        }
        throw CasterException(o, "Component")
    }

    fun toComponent(o: Object?, defaultValue: Component?): Component? {
        if (o is Component) return o as Component? else if (o is ObjectWrap) {
            return toComponent((o as ObjectWrap?).getEmbededObject(defaultValue), defaultValue)
        }
        return defaultValue
    }

    /**
     * cast an Object to a Collection, if not returns null
     *
     * @param o Object to cast
     * @param defaultValue
     * @return casted Collection
     */
    fun toCollection(o: Object?, defaultValue: Collection?): Collection? {
        if (o is Collection) return o else if (o is Node) return XMLCaster.toXMLStruct(o as Node?, false) else if (o is Component) {
            try {
                val tmp: Object = Reflector.componentToClass(ThreadLocalPageContext.get(), o as Component?, Collection::class.java)
                if (tmp is Collection) return tmp
            } catch (e: PageException) {
            }
        } else if (o is Map) {
            return MapAsStruct.toStruct(o as Map?, true)
        } else if (o is ObjectWrap) {
            return toCollection((o as ObjectWrap?).getEmbededObject(defaultValue), defaultValue)
        } else if (Decision.isArray(o)) {
            return try {
                toArray(o)
            } catch (e: PageException) {
                defaultValue
            }
        }
        return defaultValue
    }

    /**
     * convert an object to a File
     *
     * @param obj
     * @return File
     * @throws PageException
     */
    @Throws(PageException::class)
    fun toFile(obj: Object?): File? {
        return if (obj is File) obj as File? else FileUtil.toFile(toString(obj))
    }

    /**
     * convert an object to a File
     *
     * @param obj
     * @param defaultValue
     * @return File
     */
    fun toFile(obj: Object?, defaultValue: File?): File? {
        if (obj is File) return obj as File?
        val str = toString(obj, null) ?: return defaultValue
        return FileUtil.toFile(str)
    }

    /**
     * convert an object array to a HashMap filled with Function value Objects
     *
     * @param args Object array to convert
     * @return hashmap containing Function values
     * @throws ExpressionException
     */
    @Throws(ExpressionException::class)
    fun toFunctionValues(args: Array<Object?>?): Struct? {
        return toFunctionValues(args, 0, args!!.size)
    }

    @Throws(ExpressionException::class)
    fun toFunctionValues(args: Array<Object?>?, offset: Int, len: Int): Struct? {
        // TODO nicht sehr optimal
        val sct: Struct = StructImpl(StructImpl.TYPE_LINKED)
        for (i in offset until offset + len) {
            if (args!![i] is FunctionValueImpl) {
                val value: FunctionValueImpl? = args[i] as FunctionValueImpl?
                sct.setEL(value.getNameAsKey(), value.getValue())
            } else throw ExpressionException(
                    "Missing argument name, when using named parameters to a function, every parameter must have a name [" + i + ":" + args[i].getClass().getName() + "].")
        }
        return sct
    }

    fun toFunctionValues(args: Struct?): Array<Object?>? {
        // TODO nicht sehr optimal
        val it: Iterator<Entry<Key?, Object?>?> = args.entryIterator()
        var e: Entry<Key?, Object?>?
        val fvalues: List<FunctionValue?> = ArrayList<FunctionValue?>()
        while (it.hasNext()) {
            e = it.next()
            fvalues.add(FunctionValueImpl(e.getKey().getString(), e.getValue()))
        }
        return fvalues.toArray(arrayOfNulls<FunctionValue?>(fvalues.size()))
    }

    /**
     * casts a string to a Locale
     *
     * @param strLocale
     * @return Locale from String
     * @throws ExpressionException
     */
    @Throws(ExpressionException::class)
    fun toLocale(strLocale: String?): Locale? {
        return LocaleFactory.getLocale(strLocale)
    }

    /**
     * casts a string to a Locale
     *
     * @param strLocale
     * @param defaultValue
     * @return Locale from String
     */
    fun toLocale(strLocale: String?, defaultValue: Locale?): Locale? {
        return LocaleFactory.getLocale(strLocale, defaultValue)
    }

    /**
     * casts a string to a TimeZone
     *
     * @param strTimeZone
     * @return TimeZone from String
     * @throws ExpressionException
     */
    @Throws(ExpressionException::class)
    fun toTimeZone(strTimeZone: String?): TimeZone? {
        return TimeZoneUtil.toTimeZone(strTimeZone)
    }

    /**
     * casts a string to a TimeZone
     *
     * @param strTimeZone
     * @param defaultValue
     * @return TimeZone from String
     */
    fun toTimeZone(strTimeZone: String?, defaultValue: TimeZone?): TimeZone? {
        return TimeZoneUtil.toTimeZone(strTimeZone, defaultValue)
    }

    fun toTimeZone(oTimeZone: Object?, defaultValue: TimeZone?): TimeZone? {
        return if (oTimeZone is TimeZone) oTimeZone as TimeZone? else TimeZoneUtil.toTimeZone(toString(oTimeZone, null), defaultValue)
    }

    /**
     * casts an Object to a Node List
     *
     * @param o Object to Cast
     * @return NodeList from Object
     * @throws PageException
     */
    @Throws(PageException::class)
    fun toNodeList(o: Object?): NodeList? {
        // print.ln("nodeList:"+o);
        if (o is NodeList) {
            return o as NodeList?
        } else if (o is ObjectWrap) {
            return toNodeList((o as ObjectWrap?).getEmbededObject())
        }
        throw CasterException(o, "NodeList")
    }

    /**
     * casts an Object to a Node List
     *
     * @param o Object to Cast
     * @param defaultValue
     * @return NodeList from Object
     */
    fun toNodeList(o: Object?, defaultValue: NodeList?): NodeList? {
        // print.ln("nodeList:"+o);
        if (o is NodeList) {
            return o as NodeList?
        } else if (o is ObjectWrap) {
            return toNodeList((o as ObjectWrap?).getEmbededObject(defaultValue), defaultValue)
        }
        return defaultValue
    }

    /**
     * casts an Object to a XML Node
     *
     * @param o Object to Cast
     * @return Node from Object
     * @throws PageException
     */
    @Throws(PageException::class)
    fun toNode(o: Object?): Node? {
        return toXML(o)
        /*
		 * if(o instanceof Node)return (Node)o; else if(o instanceof String) { try { return
		 * XMLCaster.toXMLStruct(XMLUtil.parse(o.toString(),false),false);
		 * 
		 * } catch (Exception e) { throw Caster.toPageException(e); } } else if(o instanceof ObjectWrap) {
		 * return toNode(((ObjectWrap)o).getEmbededObject()); } throw new CasterException(o,"Node");
		 */
    }

    /**
     * casts an Object to a XML Node
     *
     * @param o Object to Cast
     * @param defaultValue
     * @return Node from Object
     */
    fun toNode(o: Object?, defaultValue: Node?): Node? {
        return toXML(o, defaultValue)
        /*
		 * if(o instanceof Node)return (Node)o; else if(o instanceof String) { try { return
		 * XMLCaster.toXMLStruct(XMLUtil.parse(o.toString(),false),false);
		 * 
		 * } catch (Exception e) { return defaultValue; } } else if(o instanceof ObjectWrap) { return
		 * toNode(((ObjectWrap)o).getEmbededObject(defaultValue),defaultValue); } return defaultValue;
		 */
    }

    /**
     * casts an Object to a XML Element
     *
     * @param o Object to Cast
     * @return Element from Object
     * @throws PageException
     */
    @Throws(PageException::class)
    fun toElement(o: Object?): Element? {
        if (o is Element) return o as Element?
        throw CasterException(o, "org.w3c.dom.Element")
    }

    /**
     * casts a boolean to an Integer
     *
     * @param b
     * @return Integer from boolean
     */
    fun toInteger(b: Boolean): Integer? {
        return if (b) Constants.INTEGER_1 else Constants.INTEGER_0
    }

    fun toInteger(b: Boolean?): Integer? {
        return if (b!!) Constants.INTEGER_1 else Constants.INTEGER_0
    }

    /**
     * casts a char to an Integer
     *
     * @param c
     * @return Integer from char
     */
    fun toInteger(c: Char): Integer? {
        return Integer.valueOf(c)
    }

    /**
     * casts a double to an Integer
     *
     * @param d
     * @return Integer from double
     */
    fun toInteger(d: Double): Integer? {
        return Integer.valueOf(d.toInt())
    }

    fun toInteger(n: Number?): Integer? {
        return Integer.valueOf(n.intValue())
    }

    /**
     * casts an Object to an Integer
     *
     * @param o Object to cast to Integer
     * @return Integer from Object
     * @throws PageException
     */
    @Throws(PageException::class)
    fun toInteger(o: Object?): Integer? {
        return Integer.valueOf(toIntValue(o))
    }

    /**
     * casts an Object to an Integer
     *
     * @param str Object to cast to Integer
     * @return Integer from Object
     * @throws PageException
     */
    @Throws(PageException::class)
    fun toInteger(str: String?): Integer? {
        return Integer.valueOf(toIntValue(str))
    }

    // used in bytecode genrator
    fun toInteger(i: Int): Integer? {
        return Integer.valueOf(i)
    }

    /**
     * casts an Object to an Integer
     *
     * @param o Object to cast to Integer
     * @param defaultValue
     * @return Integer from Object
     */
    fun toInteger(o: Object?, defaultValue: Integer?): Integer? {
        if (defaultValue != null) return Integer.valueOf(toIntValue(o, defaultValue.intValue()))
        val res: Int = toIntValue(o, Integer.MIN_VALUE)
        return if (res == Integer.MIN_VALUE) defaultValue else Integer.valueOf(res)
    }

    /**
     * casts an Object to null
     *
     * @param value
     * @return to null from Object
     * @throws PageException
     */
    @Throws(PageException::class)
    fun toNull(value: Object?): Object? {
        if (value == null) return null
        if (value is String && toString(value).trim().length() === 0) return null
        if (value is Number && (value as Number?).intValue() === 0) return null
        throw CasterException(value, "null")
    }

    /**
     * casts an Object to null
     *
     * @param value
     * @param defaultValue
     * @return to null from Object
     */
    fun toNull(value: Object?, defaultValue: Object?): Object? {
        if (value == null) return null
        if (value is String && toString(value, "").trim().length() === 0) return null
        return if (value is Number && (value as Number?).intValue() === 0) null else defaultValue
    }

    /**
     * cast Object to a XML Node
     *
     * @param value
     * @param defaultValue
     * @return XML Node
     */
    fun toXML(value: Object?, defaultValue: Node?): Node? {
        return try {
            toXML(value)
        } catch (e: PageException) {
            defaultValue
        }
    }

    /**
     * cast Object to a XML Node
     *
     * @param value
     * @return XML Node
     * @throws PageException
     */
    @Throws(PageException::class)
    fun toXML(value: Object?): Node? {
        if (value is Node) return XMLCaster.toXMLStruct(value as Node?, false)
        return if (value is ObjectWrap) {
            toXML((value as ObjectWrap?).getEmbededObject())
        } else try {
            XMLCaster.toXMLStruct(XMLUtil.parse(XMLUtil.toInputSource(null, value), null, false), false)
        } catch (outer: Exception) {
            throw toPageException(outer)
        }
    }

    fun toStringForce(value: Object?, defaultValue: String?): String? {
        val rtn = toString(value, null)
        if (rtn != null) return rtn
        try {
            if (value is Struct) {
                return ScriptConverter().serialize(value)
            } else if (value is Array) {
                return ScriptConverter().serialize(value)
            }
        } catch (e: ConverterException) {
        }
        return defaultValue
    }

    @Throws(ExpressionException::class)
    fun toResource(pc: PageContext?, src: Object?, existing: Boolean): Resource? {
        return toResource(pc, src, existing, pc.getConfig().allowRealPath())
    }

    @Throws(ExpressionException::class)
    fun toResource(pc: PageContext?, src: Object?, existing: Boolean, allowRealpath: Boolean): Resource? {
        var src: Object? = src
        if (src is Resource) return src as Resource?
        if (src is File) src = src.toString()
        if (src is String) {
            return if (existing) ResourceUtil.toResourceExisting(pc, src as String?, allowRealpath) else ResourceUtil.toResourceNotExisting(pc, src as String?, allowRealpath, false)
        }
        if (src is FileStreamWrapper) return (src as FileStreamWrapper?).getResource()
        throw CasterException(src, "Resource")
    }

    @Throws(ExpressionException::class)
    fun toResource(config: Config?, src: Object?, existing: Boolean): Resource? {
        var src: Object? = src
        if (src is Resource) return src as Resource?
        if (src is File) src = src.toString()
        if (src is String) {
            return if (existing) ResourceUtil.toResourceExisting(config, src as String?) else ResourceUtil.toResourceNotExisting(config, src as String?)
        }
        if (src is FileStreamWrapper) return (src as FileStreamWrapper?).getResource()
        throw CasterException(src, "Resource")
    }

    @Throws(PageException::class)
    fun toHashtable(obj: Object?): Hashtable? {
        return if (obj is Hashtable) obj as Hashtable? else Duplicator.duplicateMap(toMap(obj, false), Hashtable(), false) as Hashtable
    }

    @Throws(PageException::class)
    fun toVetor(obj: Object?): Vector? {
        return if (obj is Vector) obj as Vector? else Duplicator.duplicateList(toList(obj, false), Vector(), false) as Vector
    }

    fun toCalendar(date: Date?, tz: TimeZone?, l: Locale?): Calendar? {
        var tz: TimeZone? = tz
        tz = ThreadLocalPageContext.getTimeZone(tz)
        val c: Calendar = if (tz == null) JREDateTimeUtil.newInstance(tz, l) else JREDateTimeUtil.newInstance(tz, l)
        c.setTime(date)
        return c
    }

    fun toCalendar(time: Long, tz: TimeZone?, l: Locale?): Calendar? {
        var tz: TimeZone? = tz
        tz = ThreadLocalPageContext.getTimeZone(tz)
        val c: Calendar = if (tz == null) JREDateTimeUtil.newInstance(tz, l) else JREDateTimeUtil.newInstance(tz, l)
        c.setTimeInMillis(time)
        return c
    }

    @Throws(CasterException::class)
    fun toSerializable(`object`: Object?): Serializable? {
        if (`object` is Serializable) return `object` as Serializable?
        throw CasterException(`object`, "Serializable")
    }

    fun toSerializable(`object`: Object?, defaultValue: Serializable?): Serializable? {
        return if (`object` is Serializable) `object` as Serializable? else defaultValue
    }

    @Throws(PageException::class)
    fun toBytes(obj: Object?, charset: Charset?): ByteArray? {
        try {
            if (obj is ByteArray) return obj
            if (obj is InputStream) return IOUtil.toBytes(obj as InputStream?)
            if (obj is Resource) return IOUtil.toBytes(obj as Resource?)
            if (obj is File) return IOUtil.toBytes(obj as File?)
            if (obj is String) return (obj as String?).getBytes(if (charset == null) SystemUtil.getCharset() else charset)
            if (obj is Blob) {
                var `is`: InputStream? = null
                return try {
                    `is` = (obj as Blob?).getBinaryStream()
                    IOUtil.toBytes(`is`)
                } finally {
                    IOUtil.close(`is`)
                }
            }
        } catch (ioe: IOException) {
            throw toPageException(ioe)
        } catch (ioe: SQLException) {
            throw toPageException(ioe)
        }
        throw CasterException(obj, ByteArray::class.java)
    }

    @Throws(PageException::class)
    fun toInputStream(obj: Object?, charset: Charset?): InputStream? {
        try {
            if (obj is InputStream) return obj as InputStream?
            if (obj is ByteArray) return ByteArrayInputStream(obj as ByteArray?)
            if (obj is Resource) return (obj as Resource?).getInputStream()
            if (obj is File) return FileInputStream(obj as File?)
            if (obj is String) return ByteArrayInputStream((obj as String?).getBytes(if (charset == null) SystemUtil.getCharset() else charset))
            if (obj is Blob) return (obj as Blob?).getBinaryStream()
        } catch (ioe: IOException) {
            throw toPageException(ioe)
        } catch (ioe: SQLException) {
            throw toPageException(ioe)
        }
        throw CasterException(obj, InputStream::class.java)
    }

    @Throws(PageException::class)
    fun toOutputStream(obj: Object?): OutputStream? {
        if (obj is OutputStream) return obj as OutputStream?
        throw CasterException(obj, OutputStream::class.java)
    }

    @Throws(PageException::class)
    fun castTo(pc: PageContext?, trgClass: Class?, obj: Object?): Object? {
        if (trgClass == null) return toNull(obj) else if (obj.getClass() === trgClass) return obj else if (trgClass === ByteArray::class.java) return toBinary(obj) else if (trgClass === Boolean::class.javaPrimitiveType) return toBoolean(obj) else if (trgClass === Byte::class.javaPrimitiveType) return toByte(obj) else if (trgClass === Short::class.javaPrimitiveType) return toShort(obj) else if (trgClass === Int::class.javaPrimitiveType) return toInteger(obj) else if (trgClass === Long::class.javaPrimitiveType) return toLong(obj) else if (trgClass === Float::class.javaPrimitiveType) return toFloat(obj) else if (trgClass === Double::class.javaPrimitiveType) return toDouble(obj) else if (trgClass === Char::class.javaPrimitiveType) return toCharacter(obj) else if (trgClass === Boolean::class.java) return toBoolean(obj) else if (trgClass === Byte::class.java) return toByte(obj) else if (trgClass === Short::class.java) return toShort(obj) else if (trgClass === Integer::class.java) return toInteger(obj) else if (trgClass === Long::class.java) return toLong(obj) else if (trgClass === Float::class.java) return toFloat(obj) else if (trgClass === Double::class.java) return toDouble(obj) else if (trgClass === Character::class.java) return toCharacter(obj) else if (trgClass === Object::class.java) return obj else if (trgClass === String::class.java) return toString(obj)
        if (Reflector.isInstaneOf(obj.getClass(), trgClass, false)) return obj
        if (obj is Component) {
            if (trgClass === Component::class.java) return obj
            val comp: Component? = obj as Component?
            if (trgClass.isInterface()) { // TODO allow not only intefaces
                return Reflector.componentToClass(pc, comp, trgClass)
            }
        }
        return castTo(pc, toClassName(trgClass), obj, false)
    }

    @Throws(PageException::class)
    fun toObjects(pc: PageContext?, obj: Object?): Objects? {
        if (obj is Objects) return obj as Objects?
        return if (obj is ObjectWrap) toObjects(pc, (obj as ObjectWrap?).getEmbededObject()) else JavaObject(pc.getVariableUtil(), obj)
    }

    @Throws(PageException::class)
    fun toBigDecimal(o: Object?): BigDecimal? {
        if (o is BigDecimal) return o as BigDecimal?
        if (o is Number) return toBigDecimal(o as Number?) else if (o is Boolean) return BigDecimal(if ((o as Boolean?).booleanValue()) 1 else 0) else if (o is CharSequence) return BigDecimal(o.toString()) else if (o is Character) return BigDecimal((o as Character?).charValue()) else if (o is Castable) return BigDecimal((o as Castable?).castToDoubleValue()) else if (o == null) return BigDecimal.ZERO else if (o is ObjectWrap) return toBigDecimal((o as ObjectWrap?).getEmbededObject())
        throw CasterException(o, "number")
    }

    fun toBigDecimal(n: Number?): BigDecimal? {
        return if (n is BigDecimal) n as BigDecimal? else BigDecimal.valueOf(n.doubleValue())
    }

    fun toBigDecimal(d: Double): BigDecimal? {
        return BigDecimal.valueOf(d)
    }

    @Throws(CasterException::class)
    fun toBigDecimal(str: String?): BigDecimal? {
        return try {
            if (Util.isEmpty(str, true)) throw CasterException("cannot convert string[$str] to a number, the string is empty")
            BigDecimal(str.trim(), MathContext.DECIMAL128)
        } catch (nfe: NumberFormatException) {
            throw CasterException("cannot convert string[" + str + "] to a number; " + nfe.getMessage())
        }
    }

    fun toBigDecimal(str: String?, defaultValue: BigDecimal?): BigDecimal? {
        try {
            return toBigDecimal(str)
        } catch (e: Exception) {
        }
        return defaultValue
    }

    @Throws(PageException::class)
    fun toBigInteger(o: Object?): BigInteger? {
        if (o is BigInteger) return o as BigInteger?
        if (o is Number) {
            return BigInteger((o as Number?).toString())
        } else if (o is Boolean) return BigInteger(if ((o as Boolean?).booleanValue()) "1" else "0") else if (o is CharSequence) return BigInteger(o.toString()) else if (o is Character) return BigInteger(String.valueOf((o as Character?).charValue() as Int)) else if (o is Castable) return BigInteger("" + toIntValue((o as Castable?).castToDoubleValue())) else if (o == null) return BigInteger.ZERO else if (o is ObjectWrap) return toBigInteger((o as ObjectWrap?).getEmbededObject())
        throw CasterException(o, "number")
    }

    @Throws(PageException::class)
    fun unwrap(value: Object?): Object? {
        if (value == null) return null
        if (value is ObjectWrap) {
            return (value as ObjectWrap?).getEmbededObject()
        }
        return if (value is JavaObject) {
            (value as JavaObject?).getEmbededObject()
        } else value
    }

    fun unwrap(value: Object?, defaultValue: Object?): Object? {
        if (value == null) return null
        if (value is ObjectWrap) {
            return (value as ObjectWrap?).getEmbededObject(defaultValue)
        }
        return if (value is JavaObject) {
            (value as JavaObject?).getEmbededObject(defaultValue)
        } else value
    }

    @Throws(PageException::class)
    fun toCharSequence(obj: Object?): CharSequence? {
        if (obj is CharSequence) return obj
        if (obj is Component) {
            try {
                val tmp: Object = Reflector.componentToClass(ThreadLocalPageContext.get(), obj as Component?, CharSequence::class.java)
                if (tmp is CharSequence) return tmp
            } catch (pe: PageException) {
            }
        }
        return toString(obj)
    }

    fun toCharSequence(obj: Object?, defaultValue: CharSequence?): CharSequence? {
        if (obj is CharSequence) return obj
        if (obj is Component) {
            try {
                val tmp: Object = Reflector.componentToClass(ThreadLocalPageContext.get(), obj as Component?, CharSequence::class.java)
                if (tmp is CharSequence) return tmp
            } catch (pe: PageException) {
            }
        }
        return toString(obj, null) ?: return defaultValue
    }

    @Throws(PageException::class)
    fun toPojo(pojo: Pojo?, comp: Component?, done: Set<Object?>?): Pojo? {
        val pc: PageContext = ThreadLocalPageContext.get()
        return try {
            _toPojo(pc, pojo, comp, done)
        } catch (e: Exception) {
            throw toPageException(e)
        }
    }

    @Throws(PageException::class)
    private fun _toPojo(pc: PageContext?, pojo: Pojo?, comp: Component?, done: Set<Object?>?): Pojo? { // print.ds();System.exit(0);
        var pojo: Pojo? = pojo
        var comp: Component? = comp
        comp = ComponentSpecificAccess.toComponentSpecificAccess(Component.ACCESS_PRIVATE, comp)
        val scope: ComponentScope = comp.getComponentScope()

        // create Pojo
        if (pojo == null) {
            pojo = try {
                ClassUtil.loadInstance(ComponentUtil.getComponentPropertiesClass(pc, comp)) as Pojo
            } catch (e: ClassException) {
                throw toPageException(e)
            }
        }

        // initialize Pojo
        val props: Array<Property?> = comp.getProperties(false, true, false, false)
        _initPojo(pc, pojo, props, scope, comp, done)
        return pojo
    }

    @Throws(PageException::class)
    fun toPojo(pojo: Pojo?, sct: Struct?, done: Set<Object?>?): Pojo? {
        val pc: PageContext = ThreadLocalPageContext.get()
        return try {
            _toPojo(pc, pojo, sct, done)
        } catch (e: Exception) {
            throw toPageException(e)
        }
    }

    @Throws(PageException::class)
    private fun _toPojo(pc: PageContext?, pojo: Pojo?, sct: Struct?, done: Set<Object?>?): Pojo? { // print.ds();System.exit(0);
        var pojo: Pojo? = pojo
        if (pojo == null) {
            pojo = try {
                val cl: PhysicalClassLoader = pc.getConfig().getRPCClassLoader(false) as PhysicalClassLoader
                ClassUtil.loadInstance(ComponentUtil.getStructPropertiesClass(pc, sct, cl)) as Pojo
            } catch (e: ClassException) {
                throw toPageException(e)
            } catch (e: IOException) {
                throw toPageException(e)
            }
        }

        // initialize
        val props: List<Property?> = ArrayList<Property?>()
        val it: Iterator<Entry<Key?, Object?>?> = sct.entryIterator()
        var e: Entry<Key?, Object?>?
        var p: PropertyImpl?
        while (it.hasNext()) {
            e = it.next()
            p = PropertyImpl()
            p.setAccess(Component.ACCESS_PUBLIC)
            p.setName(e.getKey().getString())
            p.setType(if (e.getValue() == null) "any" else toTypeName(e.getValue()))
            props.add(p)
        }
        _initPojo(pc, pojo, props.toArray(arrayOfNulls<Property?>(props.size())), sct, null, done)
        return pojo
    }

    @Throws(PageException::class)
    private fun _initPojo(pc: PageContext?, pojo: Pojo?, props: Array<Property?>?, sct: Struct?, comp: Component?, done: Set<Object?>?) {
        var p: Property?
        var v: Object?
        var k: Collection.Key?
        val interpreter = CFMLExpressionInterpreter(false)
        for (i in props.indices) {
            p = props!![i]
            k = toKey(p.getName())
            // value
            v = sct.get(k, null)
            if (v == null && comp != null) v = comp.get(k, null)

            // default
            if (v != null) v = castTo(pc, p.getType(), v, false) else {
                if (!StringUtil.isEmpty(p.getDefault())) {
                    try {
                        v = castTo(pc, p.getType(), p.getDefault(), false)
                    } catch (pe: PageException) {
                        try {
                            v = interpreter.interpret(pc, p.getDefault())
                            v = castTo(pc, p.getType(), v, false)
                        } catch (pe2: PageException) {
                            throw ExpressionException("can not use default value [" + p.getDefault().toString() + "] for property [" + p.getName().toString() + "] with type [" + p.getType().toString() + "]")
                        }
                    }
                }
            }

            // set or throw
            if (v == null) {
                if (p.isRequired()) throw ExpressionException("required property [" + p.getName().toString() + "] is not defined")
            } else {
                Reflector.callSetter(pojo, p.getName().toLowerCase(), v)
            }
        }
    }

    fun toTime(date: tachyon.runtime.type.dt.Date?, time: Time?, tz: TimeZone?): Long {
        var tz: TimeZone? = tz
        if (time == null) return date.getTime()
        tz = ThreadLocalPageContext.getTimeZone(tz)
        val c: Calendar = JREDateTimeUtil.getThreadCalendar(tz)
        c.setTimeInMillis(date.getTime())
        val y: Int = c.get(Calendar.YEAR)
        val m: Int = c.get(Calendar.MONTH)
        val d: Int = c.get(Calendar.DAY_OF_MONTH)
        c.setTimeInMillis(time.getTime())
        c.set(Calendar.YEAR, y)
        c.set(Calendar.MONTH, m)
        c.set(Calendar.DAY_OF_MONTH, d)
        return c.getTimeInMillis()
    }

    fun negate(n: Number?): Number? {
        return if (n is BigDecimal) (n as BigDecimal?).negate() else Double.valueOf(-n.doubleValue())
    }

    init {
        for (i in 0 until MAX_SMALL_DOUBLE) smallDoubles[i] = Double.valueOf(i)
    }

    init {
        df.applyLocalizedPattern("#.########################")
    }
}