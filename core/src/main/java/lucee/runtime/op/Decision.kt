/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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
package lucee.runtime.op

import java.io.ByteArrayOutputStream

/**
 * Object to test if an Object is a specific type
 */
object Decision {
    private val STRING_DEFAULT_VALUE: String? = "this is a unique string"
    private var ssnPattern: Pattern? = null
    private var phonePattern: Pattern? = null
    private var zipPattern: Pattern? = null

    /**
     * tests if value is a simple value (Number,String,Boolean,Date,Printable)
     *
     * @param value value to test
     * @return is value a simple value
     */
    fun isSimpleValue(value: Object?): Boolean {
        return (value is Number || value is Locale || value is TimeZone || value is String || value is Character
                || value is Boolean || value is Date || value is Castable && value !is Objects && value !is Collection)
    }

    fun isSimpleValueLimited(value: Object?): Boolean {
        return (value is Number || value is Locale || value is TimeZone || value is String || value is Boolean
                || value is Date)
    }

    fun isCastableToNumeric(o: Object?): Boolean {
        if (isNumber(o)) return true else if (isBoolean(o)) return true else if (isDateSimple(o, false)) return true else if (o == null) return true else if (o is ObjectWrap) return isCastableToNumeric((o as ObjectWrap?).getEmbededObject("notanumber")) else if (o is Castable) {
            return isValid((o as Castable?).castToDoubleValue(Double.NaN))
        }
        return false
    }

    fun isCastableToDate(o: Object?): Boolean {
        if (isDateAdvanced(o, true)) return true else if (isBoolean(o)) return true else if (o is ObjectWrap) return isCastableToDate((o as ObjectWrap?).getEmbededObject("notadate")) else if (o is Castable) {
            return (o as Castable?).castToDateTime(null) != null
        }
        return false
    }

    /**
     * tests if value is Numeric
     *
     * @param value value to test
     * @return is value numeric
     */
    fun isNumber(value: Object?, alsoBooleans: Boolean): Boolean {
        return if (alsoBooleans && isBoolean(value)) true else isNumber(value)
    }

    /**
     * tests if value is Numeric
     *
     * @param value value to test
     * @return is value numeric
     */
    fun isNumber(value: Object?): Boolean {
        return if (value is Number) true else if (value is CharSequence || value is Character) {
            isNumber(value.toString())
        } else false
    }

    /**
     * tests if String value is Numeric
     *
     * @param str value to test
     * @return is value numeric
     */
    fun isNumber(str: String?): Boolean {
        var str = str ?: return false
        str = str.trim()
        var pos = 0
        val len: Int = str.length()
        if (len == 0) return false
        var curr: Char = str.charAt(pos)
        var nxt: Char

        // +/- at beginning
        if (curr == '+' || curr == '-') {
            if (len == ++pos) return false
            curr = str.charAt(pos)
        }
        var hasDot = false
        var hasExp = false
        while (pos < len) {
            curr = str.charAt(pos)
            if (curr < '0') {
                hasDot = if (curr == '.') {
                    if (pos + 1 >= len || hasDot) return false
                    true
                } else return false
            } else if (curr > '9') {
                if (curr == 'e' || curr == 'E') {
                    // is it follow by +/-, that is fine
                    if (pos + 1 < len) {
                        nxt = str.charAt(pos + 1)
                        if (nxt == '+' || nxt == '-') {
                            curr = nxt
                            pos++
                        }
                    }

                    // e cannot be azt the end and not more than once
                    if (pos + 1 >= len || hasExp) return false
                    hasExp = true
                    hasDot = true
                } else return false
            }
            pos++
        }
        return if (hasExp) {
            try {
                if (Double.isInfinite(Double.parseDouble(str))) false else true
            } catch (e: NumberFormatException) {
                false
            }
        } else true
    }

    fun isInteger(value: Object?): Boolean {
        return isInteger(value, false)
    }

    fun isInteger(value: Object?, alsoBooleans: Boolean): Boolean {
        if (!alsoBooleans && isBoolean(value)) return false
        val dbl: Double = Caster.toDoubleValue(value, false, Double.NaN)
        if (!isValid(dbl)) return false
        val i = dbl.toInt()
        return i.toDouble() == dbl
    }

    /**
     * tests if String value is Hex Value
     *
     * @param str value to test
     * @return is value numeric
     */
    fun isHex(str: String?): Boolean {
        if (str == null || str.length() === 0) return false
        for (i in str.length() - 1 downTo 0) {
            var c: Char = str.charAt(i)
            if (!(c >= '0' && c <= '9')) {
                c = Character.toLowerCase(c)
                if (!(c == 'a' || c == 'b' || c == 'c' || c == 'd' || c == 'e' || c == 'f')) return false
            }
        }
        return true
    }

    /**
     * tests if String value is UUID Value
     *
     * @param obj value to test
     * @return is value numeric
     */
    fun isUUId(obj: Object?): Boolean {
        val str: String = Caster.toString(obj, null) ?: return false
        if (str.length() === 35) {
            return isHex(str.substring(0, 8)) && str.charAt(8) === '-' && isHex(str.substring(9, 13)) && str.charAt(13) === '-' && isHex(str.substring(14, 18)) && str.charAt(18) === '-' && isHex(str.substring(19))
        } else if (str.length() === 32) return isHex(str)
        return false
    }

    fun isGUId(obj: Object?): Boolean {
        val str: String = Caster.toString(obj, null) ?: return false

        // GUID
        return if (str.length() === 36) {
            isHex(str.substring(0, 8)) && str.charAt(8) === '-' && isHex(str.substring(9, 13)) && str.charAt(13) === '-' && isHex(str.substring(14, 18)) && str.charAt(18) === '-' && isHex(str.substring(19, 23)) && str.charAt(23) === '-' && isHex(str.substring(24))
        } else false
    }

    fun isGUIdSimple(obj: Object?): Boolean {
        val str: String = Caster.toString(obj, null) ?: return false

        // GUID
        return if (str.length() === 36) {
            str.charAt(8) === '-' && str.charAt(13) === '-' && str.charAt(18) === '-' && str.charAt(23) === '-'
        } else false
    }

    /**
     * tests if value is a Boolean (Numbers are not acctepeted)
     *
     * @param value value to test
     * @return is value boolean
     */
    fun isBoolean(value: Object?): Boolean {
        return if (value is Boolean) true else if (value is String) {
            isBoolean(value.toString())
        } else if (value is ObjectWrap) isBoolean((value as ObjectWrap?).getEmbededObject(null)) else false
    }

    fun isCastableToBoolean(value: Object?): Boolean {
        if (value is Boolean) return true
        return if (value is Number) true else if (value is String) {
            val str = value as String?
            isBoolean(str) || isNumber(str)
        } else if (value is Castable) {
            (value as Castable?).castToBoolean(null) != null
        } else if (value is ObjectWrap) isCastableToBoolean((value as ObjectWrap?).getEmbededObject(null)) else false
    }

    fun isBoolean(value: Object?, alsoNumbers: Boolean): Boolean {
        return if (isBoolean(value)) true else if (alsoNumbers) isNumber(value) else false
    }

    /**
     * tests if value is a Boolean
     *
     * @param str value to test
     * @return is value boolean
     */
    fun isBoolean(str: String?): Boolean {
        // str=str.trim();
        if (str!!.length() < 2) return false
        when (str.charAt(0)) {
            't', 'T' -> return str.equalsIgnoreCase("true")
            'f', 'F' -> return str.equalsIgnoreCase("false")
            'y', 'Y' -> return str.equalsIgnoreCase("yes")
            'n', 'N' -> return str.equalsIgnoreCase("no")
        }
        return false
    }

    /**
     * tests if value is DateTime Object
     *
     * @param value value to test
     * @param alsoNumbers interpret also a number as date
     * @return is value a DateTime Object
     */
    fun isDate(value: Object?, alsoNumbers: Boolean): Boolean {
        return isDateSimple(value, alsoNumbers)
    }

    fun isDateSimple(value: Object?, alsoNumbers: Boolean): Boolean {
        return isDateSimple(value, alsoNumbers, false)
    }

    fun isDateSimple(value: Object?, alsoNumbers: Boolean, alsoMonthString: Boolean): Boolean {
        // return DateCaster.toDateEL(value)!=null;
        if (value is DateTime) return true else if (value is Date) return true else if (value is String) return DateCaster.toDateSimple(value.toString(), if (alsoNumbers) DateCaster.CONVERTING_TYPE_OFFSET else DateCaster.CONVERTING_TYPE_NONE,
                alsoMonthString, TimeZone.getDefault(), null) != null else if (value is ObjectWrap) {
            return isDateSimple((value as ObjectWrap?).getEmbededObject(null), alsoNumbers)
        } else if (value is Castable) {
            return (value as Castable?).castToDateTime(null) != null
        } else if (alsoNumbers && value is Number) return true else if (value is Calendar) return true
        return false
    }

    fun isDateAdvanced(value: Object?, alsoNumbers: Boolean): Boolean {
        // return DateCaster.toDateEL(value)!=null;
        if (value is Date) return true else if (value is String) return DateCaster.toDateAdvanced(value.toString(), if (alsoNumbers) DateCaster.CONVERTING_TYPE_OFFSET else DateCaster.CONVERTING_TYPE_NONE,
                TimeZone.getDefault(), null) != null else if (value is Castable) {
            return (value as Castable?).castToDateTime(null) != null
        } else if (alsoNumbers && value is Number) return true else if (value is ObjectWrap) {
            return isDateAdvanced((value as ObjectWrap?).getEmbededObject(null), alsoNumbers)
        } else if (value is Calendar) return true
        return false
    }

    private val DATE_DEL: CharArray? = charArrayOf('.', '/', '-')
    fun isUSDate(value: Object?): Boolean {
        val str: String = Caster.toString(value, "")
        return isUSorEuroDateEuro(str, false)
    }

    fun isUSDate(str: String?): Boolean {
        return isUSorEuroDateEuro(str, false)
    }

    fun isEuroDate(value: Object?): Boolean {
        val str: String = Caster.toString(value, "")
        return isUSorEuroDateEuro(str, true)
    }

    fun isEuroDate(str: String?): Boolean {
        return isUSorEuroDateEuro(str, true)
    }

    private fun isUSorEuroDateEuro(str: String?, isEuro: Boolean): Boolean {
        if (StringUtil.isEmpty(str)) return false
        for (i in DATE_DEL.indices) {
            val arr: Array = lucee.runtime.type.util.ListUtil.listToArrayRemoveEmpty(str, DATE_DEL!![i])
            if (arr.size() !== 3) continue
            val month: Int = Caster.toIntValue(arr.get(if (isEuro) 2 else 1, Constants.INTEGER_0), Integer.MIN_VALUE)
            val day: Int = Caster.toIntValue(arr.get(if (isEuro) 1 else 2, Constants.INTEGER_0), Integer.MIN_VALUE)
            val year: Int = Caster.toIntValue(arr.get(3, Constants.INTEGER_0), Integer.MIN_VALUE)
            if (month == Integer.MIN_VALUE) continue
            if (month > 12) continue
            if (day == Integer.MIN_VALUE) continue
            if (day > 31) continue
            if (year == Integer.MIN_VALUE) continue
            if (DateTimeUtil.getInstance().toTime(null, year, month, day, 0, 0, 0, 0, Long.MIN_VALUE) === Long.MIN_VALUE) continue
            return true
        }
        return false
    }

    fun isCastableToStruct(o: Object?): Boolean {
        if (isStruct(o)) return true
        if (o == null) return false else if (o is ObjectWrap) {
            return if (o is JavaObject) true else isCastableToStruct((o as ObjectWrap?).getEmbededObject(null))
        }
        return if (isSimpleValue(o)) {
            false
        } else false
        // if(isArray(o) || isQuery(o)) return false;
    }

    /**
     * tests if object is a struct
     *
     * @param o
     * @return is struct or not
     */
    fun isStruct(o: Object?): Boolean {
        if (o is Struct) return true else if (o is Map) return true else if (o is Node) return true
        return false
    }

    /**
     * can this type be casted to an array
     *
     * @param o
     * @return
     * @throws PageException
     */
    fun isCastableToArray(o: Object?): Boolean {
        if (isArray(o)) return true else if (o is Set) return true else if (o is Struct) {
            val sct: Struct? = o as Struct?
            val it: Iterator<Key?> = sct.keyIterator()
            while (it.hasNext()) {
                if (!isInteger(it.next(), false)) return false
            }
            return true
        }
        return false
    }

    /**
     * tests if object is an array
     *
     * @param o
     * @return is array or not
     */
    fun isArray(o: Object?): Boolean {
        if (o is Array) return true
        if (o is List) return true
        if (isNativeArray(o)) return true
        return if (o is ObjectWrap) {
            isArray((o as ObjectWrap?).getEmbededObject(null))
        } else false
    }

    /**
     * tests if object is a native java array
     *
     * @param o
     * @return is a native (java) array
     */
    fun isNativeArray(o: Object?): Boolean {
        // return o.getClass().isArray();
        if (o is Array<Object>) return true else if (o is BooleanArray) return true else if (o is ByteArray) return true else if (o is CharArray) return true else if (o is ShortArray) return true else if (o is IntArray) return true else if (o is LongArray) return true else if (o is FloatArray) return true else if (o is DoubleArray) return true
        return false
    }

    /**
     * tests if object is catable to a binary
     *
     * @param object
     * @return boolean
     */
    fun isCastableToBinary(`object`: Object?, checkBase64String: Boolean): Boolean {
        if (isBinary(`object`)) return true
        if (`object` is InputStream) return true
        if (`object` is ByteArrayOutputStream) return true
        if (`object` is Blob) return true

        // Base64 String
        if (!checkBase64String) return false
        val str: String = Caster.toString(`object`, null) ?: return false
        return Base64Util.isBase64(str)
    }

    /**
     * tests if object is a binary
     *
     * @param object
     * @return boolean
     */
    fun isBinary(`object`: Object?): Boolean {
        if (`object` is ByteArray) return true
        return if (`object` is ObjectWrap) isBinary((`object` as ObjectWrap?).getEmbededObject("")) else false
    }

    /**
     * tests if object is a Component
     *
     * @param object
     * @return boolean
     */
    fun isComponent(`object`: Object?): Boolean {
        return `object` is Component
    }

    /**
     * tests if object is a Query
     *
     * @param object
     * @return boolean
     */
    fun isQuery(`object`: Object?): Boolean {
        if (`object` is Query) return true else if (`object` is ObjectWrap) {
            return isQuery((`object` as ObjectWrap?).getEmbededObject(null))
        }
        return false
    }

    fun isQueryColumn(`object`: Object?): Boolean {
        if (`object` is QueryColumn) return true else if (`object` is ObjectWrap) {
            return isQueryColumn((`object` as ObjectWrap?).getEmbededObject(null))
        }
        return false
    }

    /**
     * tests if object is a binary
     *
     * @param object
     * @return boolean
     */
    fun isUserDefinedFunction(`object`: Object?): Boolean {
        return `object` is UDF
    }

    /**
     * tests if year is a leap year
     *
     * @param year year to check
     * @return boolean
     */
    fun isLeapYear(year: Int): Boolean {
        return DateTimeUtil.getInstance().isLeapYear(year)
        // return new GregorianCalendar().isLeapYear(year);
    }

    /**
     * tests if object is a WDDX Object
     *
     * @param o Object to check
     * @return boolean
     */
    fun isWddx(o: Object?): Boolean {
        if (o !is String) return false
        val str: String = o.toString()
        if (str.indexOf("wddxPacket") <= 0) return false

        // wrong timezone but this isent importend because date will not be used
        val converter = WDDXConverter(TimeZone.getDefault(), false, true)
        try {
            converter.deserialize(Caster.toString(o), true)
        } catch (e: Exception) {
            return false
        }
        return true
    }

    /**
     * tests if object is a XML Object
     *
     * @param o Object to check
     * @return boolean
     */
    fun isXML(o: Object?): Boolean {
        if (o is Node || o is NodeList) return true
        return if (o is ObjectWrap) {
            isXML((o as ObjectWrap?).getEmbededObject(null))
        } else try {
            XMLCaster.toXMLStruct(XMLUtil.parse(XMLUtil.toInputSource(null, o), null, false), false)
            true
        } catch (outer: Exception) {
            false
        }
    }

    fun isVoid(o: Object?): Boolean {
        if (o == null) return true else if (o is String) return o.toString().length() === 0 else if (o is Number) return (o as Number?).intValue() === 0 else if (o is Boolean) return (o as Boolean?).booleanValue() === false else if (o is ObjectWrap) return isVoid((o as ObjectWrap?).getEmbededObject("isnotnull"))
        return false
    }

    /**
     * tests if object is a XML Element Object
     *
     * @param o Object to check
     * @return boolean
     */
    fun isXMLElement(o: Object?): Boolean {
        return o is Element
    }

    /**
     * tests if object is a XML Document Object
     *
     * @param o Object to check
     * @return boolean
     */
    fun isXMLDocument(o: Object?): Boolean {
        return o is Document
    }

    /**
     * tests if object is a XML Root Element Object
     *
     * @param o Object to check
     * @return boolean
     */
    fun isXMLRootElement(o: Object?): Boolean {
        if (o is Node) {
            var n: Node? = o as Node?
            if (n is XMLStruct) n = (n as XMLStruct?).toNode()
            return XMLUtil.getDocument(n) != null && XMLUtil.getDocument(n).getDocumentElement() === n
        }
        return false
    }

    /**
     * @param obj
     * @return returns if string represent a variable name
     */
    fun isVariableName(obj: Object?): Boolean {
        return if (obj is String) isVariableName(obj as String?) else false
    }

    fun isFunction(obj: Object?): Boolean {
        if (obj is UDF) return true else if (obj is ObjectWrap) {
            return isFunction((obj as ObjectWrap?).getEmbededObject(null))
        }
        return false
    }

    fun isClosure(obj: Object?): Boolean {
        if (obj is Closure) return true else if (obj is ObjectWrap) {
            return isClosure((obj as ObjectWrap?).getEmbededObject(null))
        }
        return false
    }

    fun isLambda(obj: Object?): Boolean {
        if (obj is Lambda) return true else if (obj is ObjectWrap) {
            return isLambda((obj as ObjectWrap?).getEmbededObject(null))
        }
        return false
    }

    /**
     * @param string
     * @return returns if string represent a variable name
     */
    fun isVariableName(string: String?): Boolean {
        if (string!!.length() === 0) return false
        val len: Int = string!!.length()
        var pos = 0
        while (pos < len) {
            val first: Char = string.charAt(pos)
            if (!(first >= 'a' && first <= 'z' || first >= 'A' && first <= 'Z' || first == '_')) return false
            pos++
            while (pos < len) {
                val c: Char = string.charAt(pos)
                if (!(c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0' && c <= '9' || c == '_')) break
                pos++
            }
            if (pos == len) return true
            if (string.charAt(pos) === '.') pos++
        }
        return false
    }

    /**
     * @param string
     * @return returns if string represent a variable name
     */
    fun isSimpleVariableName(string: String?): Boolean {
        if (string!!.length() === 0) return false
        val first: Char = string.charAt(0)
        if (!(first >= 'a' && first <= 'z' || first >= 'A' && first <= 'Z' || first == '_')) return false
        for (i in string!!.length() - 1 downTo 1) {
            val c: Char = string.charAt(i)
            if (!(c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0' && c <= '9' || c == '_')) return false
        }
        return true
    }

    /**
     * @param key
     * @return returns if string represent a variable name
     */
    fun isSimpleVariableName(key: Collection.Key?): Boolean {
        val strKey: String = key.getLowerString()
        if (strKey.length() === 0) return false
        val first: Char = strKey.charAt(0)
        if (!(first >= 'a' && first <= 'z' || first == '_')) return false
        for (i in strKey.length() - 1 downTo 1) {
            val c: Char = strKey.charAt(i)
            if (!(c >= 'a' && c <= 'z' || c >= '0' && c <= '9' || c == '_')) return false
        }
        return true
    }

    /**
     * returns if object is a CFML object
     *
     * @param o Object to check
     * @return is or not
     */
    fun isObject(o: Object?): Boolean {
        return if (o == null) false else isComponent(o)
                || !isArray(o) && !isQuery(o) && !isSimpleValue(o) && !isStruct(o) && !isUserDefinedFunction(o) && !isXML(o)
    }

    /**
     * @param obj
     * @return return if a String is "Empty", that means NULL or String with length 0 (whitespaces will
     * not counted)
     */
    fun isEmpty(obj: Object?): Boolean {
        return if (obj is String) StringUtil.isEmpty(obj as String?) else obj == null
    }

    /**
     * @param str
     * @return return if a String is "Empty", that means NULL or String with length 0 (whitespaces will
     * not counted)
     */
    @Deprecated
    @Deprecated("""use instead <code>StringUtil.isEmpty(String)</code>
	  """)
    fun isEmpty(str: String?): Boolean {
        return StringUtil.isEmpty(str)
    }

    /**
     * @param str
     * @param trim
     * @return return if a String is "Empty", that means NULL or String with length 0 (whitespaces will
     * not counted)
     */
    @Deprecated
    @Deprecated("""use instead <code>StringUtil.isEmpty(String)</code>
	  """)
    fun isEmpty(str: String?, trim: Boolean): Boolean {
        return StringUtil.isEmpty(str, trim)
    }

    /**
     * returns if a value is a credit card
     *
     * @param value
     * @return is credit card
     */
    fun isCreditCard(value: Object?): Boolean {
        return ValidateCreditCard.isValid(Caster.toString(value, "0"))
    }

    /**
     * returns if given object is an email
     *
     * @param value
     * @return
     */
    fun isEmail(value: Object?): Boolean {
        return MailUtil.isValidEmail(value)
    }

    /**
     * returns if given object is a social security number (usa)
     *
     * @param value
     * @return
     */
    fun isSSN(value: Object?): Boolean {
        val str: String = Caster.toString(value, null) ?: return false
        if (ssnPattern == null) ssnPattern = Pattern.compile("^[0-9]{3}[-|]{1}[0-9]{2}[-|]{1}[0-9]{4}$")
        return ssnPattern.matcher(str.trim()).matches()
    }

    /**
     * returns if given object is a phone
     *
     * @param value
     * @return
     */
    fun isPhone(value: Object?): Boolean {
        val str: String = Caster.toString(value, null) ?: return false
        if (phonePattern == null) phonePattern = Pattern
                .compile("^(\\+?1?[ \\-\\.]?([\\(]?([1-9][0-9]{2})[\\)]?))?[ ,\\-,\\.]?([^0-1]){1}([0-9]){2}[ ,\\-,\\.]?([0-9]){4}(( )((x){0,1}([0-9]){1,5}){0,1})?$")
        return phonePattern.matcher(str.trim()).matches()
    }

    /**
     * returns true if the given object is a valid URL
     *
     * @param value
     * @return
     */
    fun isURL(value: Object?): Boolean {
        var str: String = Caster.toString(value, null) ?: return false
        if (str.indexOf(':') === -1) return false
        str = str.toLowerCase().trim()
        return if (!str.startsWith("http://") && !str.startsWith("https://") && !str.startsWith("file://") && !str.startsWith("ftp://") && !str.startsWith("mailto:")
                && !str.startsWith("news:") && !str.startsWith("urn:")) false else try {
            val uri = URI(str)
            val proto: String = uri.getScheme() ?: return false
            if (proto.equals("http") || proto.equals("https") || proto.equals("file") || proto.equals("ftp")) {
                if (uri.getHost() == null) return false
                val path: String = uri.getPath()
                if (path != null) {
                    val len: Int = path.length()
                    for (i in 0 until len) {
                        if ("?<>*|\"".indexOf(path.charAt(i)) > -1) return false
                    }
                }
            }
            true
        } catch (ex: Exception) {
            false
        }
    }

    /**
     * returns if given object is a zip code
     *
     * @param value
     * @return
     */
    fun isZipCode(value: Object?): Boolean {
        val str: String = Caster.toString(value, null) ?: return false
        if (zipPattern == null) zipPattern = Pattern.compile("([0-9]{5,5})|([0-9]{5,5}[- ]{1}[0-9]{4,4})")
        return zipPattern.matcher(str.trim()).matches()
    }

    fun isString(o: Object?): Boolean {
        if (o is String) return true else if (o is Boolean) return true else if (o is Number) return true else if (o is Date) return true else if (o is Castable) {
            return (o as Castable?).castToString(STRING_DEFAULT_VALUE) !== STRING_DEFAULT_VALUE
        } else if (o is Clob) return true else if (o is Node) return true else if (o is Map || o is List || o is Function) return false else if (o == null) return true else if (o is ObjectWrap) return isString((o as ObjectWrap?).getEmbededObject(""))
        return true
    }

    fun isCastableToString(o: Object?): Boolean {
        return isString(o)
    }

    @Throws(ExpressionException::class)
    fun isValid(type: String?, value: Object?): Boolean {
        var type = type
        val pc: PageContext = ThreadLocalPageContext.get()
        type = StringUtil.toLowerCase(type.trim())
        val first: Char = type.charAt(0)
        when (first) {
            'a' -> {
                if ("any".equals(type)) return true // isSimpleValue(value);
                if ("array".equals(type)) return isArray(value)
            }
            'b' -> {
                if ("binary".equals(type)) return isBinary(value)
                if ("boolean".equals(type)) return isBoolean(value, true)
            }
            'c' -> {
                if ("creditcard".equals(type)) return isCreditCard(value)
                if ("component".equals(type)) return isComponent(value)
                if ("cfc".equals(type)) return isComponent(value)
                if ("class".equals(type)) return isComponent(value)
                if ("closure".equals(type)) return isClosure(value)
            }
            'd' -> {
                if ("date".equals(type)) return isDateAdvanced(value, true) // ist zwar nicht logisch aber ident. zu Neo
                if ("datetime".equals(type)) return isDateAdvanced(value, true) // ist zwar nicht logisch aber ident. zu Neo
                if ("double".equals(type)) return isCastableToNumeric(value)
            }
            'e' -> {
                if ("eurodate".equals(type)) return isEuroDate(value)
                if ("email".equals(type)) return isEmail(value)
            }
            'f' -> {
                if ("fileobject".equals(type)) return isFileObject(value)
                if ("float".equals(type)) return isNumber(value, true)
                if ("function".equals(type)) return isFunction(value)
            }
            'g' -> if ("guid".equals(type)) return isGUId(value)
            'i' -> {
                if ("integer".equals(type)) return isInteger(value, false)
                if ("image".equals(type)) return ImageUtil.isImage(value)
            }
            'j' -> if ("json".equals(type)) return IsJSON.call(pc, value)
            'l' -> if ("lambda".equals(type)) return isLambda(value)
            'n' -> {
                if ("numeric".equals(type)) return isCastableToNumeric(value)
                if ("number".equals(type)) return isCastableToNumeric(value)
                if ("node".equals(type)) return isXML(value)
            }
            'o' -> if ("object".equals(type)) return isObject(value)
            'p' -> if ("phone".equals(type)) return isPhone(value)
            'q' -> if ("query".equals(type)) return isQuery(value)
            's' -> {
                if ("simple".equals(type)) return isSimpleValue(value)
                if ("struct".equals(type)) return isStruct(value)
                if ("ssn".equals(type)) return isSSN(value)
                if ("social_security_number".equals(type)) return isSSN(value)
                if ("string".equals(type)) return isString(value)
            }
            't' -> {
                if ("telephone".equals(type)) return isPhone(value)
                if ("time".equals(type)) return isDateAdvanced(value, false)
            }
            'u' -> {
                if ("usdate".equals(type)) return isUSDate(value)
                if ("uuid".equals(type)) return isUUId(value)
                if ("url".equals(type)) return isURL(value)
            }
            'v' -> if ("variablename".equals(type)) return isVariableName(Caster.toString(value, ""))
            'x' -> if ("xml".equals(type)) return isXML(value) // DIFF 23
            'z' -> {
                if ("zip".equals(type)) return isZipCode(value)
                if ("zipcode".equals(type)) return isZipCode(value)
            }
        }
        throw ExpressionException("invalid type [" + type
                + "], valid types are [any,array,binary,boolean,component,creditcard,date,time,email,eurodate,float,numeric,guid,integer,query,simple,ssn,string,struct,telephone,URL,UUID,USdate,variableName,zipcode]")
    }

    /**
     * checks if a value is castable to a certain type
     *
     * @param type any,array,boolean,binary, ...
     * @param o value to check
     * @param alsoPattern also check patterns like creditcards,email,phone ...
     * @param maxlength only used for email,url, string, ignored otherwise
     * @return
     */
    fun isCastableTo(type: String?, o: Object?, alsoAlias: Boolean, alsoPattern: Boolean, maxlength: Int): Boolean {
        var type = type
        type = StringUtil.toLowerCase(type).trim()
        if (type.length() > 2) {
            val first: Char = type.charAt(0)
            when (first) {
                'a' -> if (type.equals("any")) {
                    return true
                } else if (type.equals("array")) {
                    return isCastableToArray(o)
                }
                'b' -> if (type.equals("boolean") || alsoAlias && type.equals("bool")) {
                    return isCastableToBoolean(o)
                } else if (type.equals("binary")) {
                    return isCastableToBinary(o, true)
                } else if (alsoAlias && type.equals("bigint")) {
                    return isCastableToNumeric(o)
                } else if (type.equals("base64")) {
                    return Caster.toBase64(o, null, null) != null
                }
                'c' -> {
                    if (alsoPattern && type.equals("creditcard")) {
                        return Caster.toCreditCard(o, null) != null
                    }
                    if (alsoPattern && type.equals("char")) {
                        if (maxlength > -1) {
                            val str: String = Caster.toString(o, null) ?: return false
                            return str.length() <= maxlength
                        }
                        return isCastableToString(o)
                    }
                }
                'd' -> if (type.equals("date")) {
                    return isDateAdvanced(o, true)
                } else if (type.equals("datetime")) {
                    return isDateAdvanced(o, true)
                } else if (alsoAlias && type.equals("double")) {
                    return isCastableToNumeric(o)
                } else if (alsoAlias && type.equals("decimal")) {
                    return Caster.toDecimal(o, true, null) != null
                }
                'e' -> if (alsoAlias && type.equals("eurodate")) {
                    return isDateAdvanced(o, true)
                } else if (alsoPattern && type.equals("email")) {
                    if (maxlength > -1) {
                        val str: String = Caster.toEmail(o, null) ?: return false
                        return str.length() <= maxlength
                    }
                    return Caster.toEmail(o, null) != null
                }
                'f' -> {
                    if (alsoAlias && type.equals("float")) {
                        return isCastableToNumeric(o)
                    }
                    if (type.equals("function")) {
                        return isFunction(o)
                    }
                }
                'g' -> if (type.equals("guid")) {
                    return isGUId(o)
                }
                'i' -> if (alsoAlias && (type.equals("integer") || type.equals("int"))) {
                    return isCastableToNumeric(o)
                }
                'l' -> if (alsoAlias && type.equals("long")) {
                    return isCastableToNumeric(o)
                }
                'n' -> {
                    if (type.equals("numeric")) {
                        return isCastableToNumeric(o)
                    } else if (type.equals("number")) {
                        return isCastableToNumeric(o)
                    }
                    if (alsoAlias) {
                        if (type.equals("node")) return isXML(o) else if (type.equals("nvarchar") || type.equals("nchar")) {
                            if (maxlength > -1) {
                                val str: String = Caster.toString(o, null) ?: return false
                                return str.length() <= maxlength
                            }
                            return isCastableToString(o)
                        }
                    }
                }
                'o' -> if (type.equals("object")) {
                    return true
                } else if (alsoAlias && type.equals("other")) {
                    return true
                }
                'p' -> if (alsoPattern && type.equals("phone")) {
                    return Caster.toPhone(o, null) != null
                }
                'q' -> {
                    if (type.equals("query")) {
                        return isQuery(o)
                    }
                    if (type.equals("querycolumn")) return isQueryColumn(o)
                }
                's' -> if (type.equals("string")) {
                    if (maxlength > -1) {
                        val str: String = Caster.toString(o, null) ?: return false
                        return str.length() <= maxlength
                    }
                    return isCastableToString(o)
                } else if (type.equals("struct")) {
                    return isCastableToStruct(o)
                } else if (alsoAlias && type.equals("short")) {
                    return isCastableToNumeric(o)
                } else if (alsoPattern && (type.equals("ssn") || type.equals("social_security_number"))) {
                    return Caster.toSSN(o, null) != null
                }
                't' -> {
                    if (type.equals("timespan")) {
                        return Caster.toTimespan(o, null) != null
                    }
                    if (type.equals("time")) {
                        return isDateAdvanced(o, true)
                    }
                    if (alsoPattern && type.equals("telephone")) {
                        return Caster.toPhone(o, null) != null
                    }
                    if (alsoAlias && type.equals("timestamp")) return isDateAdvanced(o, true)
                    if (alsoAlias && type.equals("text")) {
                        if (maxlength > -1) {
                            val str: String = Caster.toString(o, null) ?: return false
                            return str.length() <= maxlength
                        }
                        return isCastableToString(o)
                    }
                    if (type.equals("uuid")) {
                        return isUUId(o)
                    }
                    if (alsoAlias && type.equals("usdate")) {
                        return isDateAdvanced(o, true)
                    }
                    if (alsoPattern && type.equals("url")) {
                        if (maxlength > -1) {
                            val str: String = Caster.toURL(o, null) ?: return false
                            return str.length() <= maxlength
                        }
                        return Caster.toURL(o, null) != null
                    }
                    if (alsoAlias && type.equals("udf")) {
                        return isFunction(o)
                    }
                }
                'u' -> {
                    if (type.equals("uuid")) {
                        return isUUId(o)
                    }
                    if (alsoAlias && type.equals("usdate")) {
                        return isDateAdvanced(o, true)
                    }
                    if (alsoPattern && type.equals("url")) {
                        if (maxlength > -1) {
                            val str: String = Caster.toURL(o, null) ?: return false
                            return str.length() <= maxlength
                        }
                        return Caster.toURL(o, null) != null
                    }
                    if (alsoAlias && type.equals("udf")) {
                        return isFunction(o)
                    }
                }
                'v' -> {
                    if (type.equals("variablename")) {
                        return isVariableName(o)
                    } else if (type.equals("void")) {
                        return isVoid(o) // Caster.toVoid(o,Boolean.TRUE)!=Boolean.TRUE;
                    } else if (alsoAlias && type.equals("variable_name")) {
                        return isVariableName(o)
                    } else if (alsoAlias && type.equals("variable-name")) {
                        return isVariableName(o)
                    }
                    if (type.equals("varchar")) {
                        if (maxlength > -1) {
                            val str: String = Caster.toString(o, null) ?: return false
                            return str.length() <= maxlength
                        }
                        return isCastableToString(o)
                    }
                }
                'x' -> if (type.equals("xml")) {
                    return isXML(o)
                }
                'z' -> if (alsoPattern && (type.equals("zip") || type.equals("zipcode"))) {
                    return Caster.toZip(o, null) != null
                }
            }
        }
        return _isCastableTo(null, type, o)
    }

    private fun _isCastableTo(pcMaybeNull: PageContext?, type: String?, o: Object?): Boolean {
        var pcMaybeNull: PageContext? = pcMaybeNull
        if (o is Component) {
            val comp: Component? = o as Component?
            return comp.instanceOf(type)
        }
        if (o is Pojo) {
            pcMaybeNull = ThreadLocalPageContext.get(pcMaybeNull)
            return pcMaybeNull != null && Caster.toComponent(pcMaybeNull, o as Pojo?, type, null) != null
        }
        if (isArrayType(type) && isArray(o)) {
            val _strType: String = type.substring(0, type!!.length() - 2)
            val _type: Short = CFTypes.toShort(_strType, false, (-1).toShort())
            val arr: Array = Caster.toArray(o, null)
            if (arr != null) {
                val it: Iterator<Object?> = arr.valueIterator()
                while (it.hasNext()) {
                    val obj: Object? = it.next()
                    if (!isCastableTo(pcMaybeNull, _type, _strType, obj)) return false
                }
                return true
            }
        }
        return false
    }

    private fun isArrayType(type: String?): Boolean {
        return type.endsWith("[]")
    }

    fun isCastableTo(pc: PageContext?, type: Short, strType: String?, o: Object?): Boolean {
        when (type) {
            CFTypes.TYPE_ANY -> return true
            CFTypes.TYPE_STRING -> return isCastableToString(o)
            CFTypes.TYPE_BOOLEAN -> return isCastableToBoolean(o)
            CFTypes.TYPE_NUMERIC -> return isCastableToNumeric(o)
            CFTypes.TYPE_STRUCT -> return isCastableToStruct(o)
            CFTypes.TYPE_ARRAY -> return isCastableToArray(o)
            CFTypes.TYPE_QUERY -> return isQuery(o)
            CFTypes.TYPE_QUERY_COLUMN -> return isQueryColumn(o)
            CFTypes.TYPE_DATETIME -> return isDateAdvanced(o, true)
            CFTypes.TYPE_VOID -> return isVoid(o) // Caster.toVoid(o,Boolean.TRUE)!=Boolean.TRUE;
            CFTypes.TYPE_BINARY -> return isCastableToBinary(o, true)
            CFTypes.TYPE_TIMESPAN -> return Caster.toTimespan(o, null) != null
            CFTypes.TYPE_UUID -> return isUUId(o)
            CFTypes.TYPE_GUID -> return isGUId(o)
            CFTypes.TYPE_VARIABLE_NAME -> return isVariableName(o)
            CFTypes.TYPE_FUNCTION -> return isFunction(o)
            CFTypes.TYPE_IMAGE -> return ImageUtil.isCastableToImage(pc, o)
            CFTypes.TYPE_XML -> return isXML(o)
        }
        return _isCastableTo(pc, strType, o)
    }

    fun isDate(str: String?, locale: Locale?, tz: TimeZone?, lenient: Boolean): Boolean {
        var str = str
        var tz: TimeZone? = tz
        str = str.trim()
        tz = ThreadLocalPageContext.getTimeZone(tz)
        var df: Array<DateFormat?>

        // get Calendar
        // Calendar c=JREDateTimeUtil.getThreadCalendar(locale,tz);

        // datetime
        val pp = ParsePosition(0)
        df = FormatUtil.getDateTimeFormats(locale, tz, false) // dfc[FORMATS_DATE_TIME];
        var d: Date
        for (i in df.indices) {
            pp.setErrorIndex(-1)
            pp.setIndex(0)
            df[i].setTimeZone(tz)
            d = df[i].parse(str, pp)
            if (pp.getIndex() === 0 || d == null || pp.getIndex() < str!!.length()) continue
            return true
        }

        // date
        df = FormatUtil.getDateFormats(locale, tz, false)
        for (i in df.indices) {
            pp.setErrorIndex(-1)
            pp.setIndex(0)
            df[i].setTimeZone(tz)
            d = df[i].parse(str, pp)
            if (pp.getIndex() === 0 || d == null || pp.getIndex() < str!!.length()) continue
            return true
        }

        // time
        df = FormatUtil.getTimeFormats(locale, tz, false)
        for (i in df.indices) {
            pp.setErrorIndex(-1)
            pp.setIndex(0)
            df[i].setTimeZone(tz)
            d = df[i].parse(str, pp)
            if (pp.getIndex() === 0 || d == null || pp.getIndex() < str!!.length()) continue
            return true
        }
        return if (lenient) isDateSimple(str, false) else false
    }

    /**
     * Checks if number is valid (not infinity or NaN)
     *
     * @param dbl
     * @return
     */
    fun isValid(dbl: Double): Boolean {
        return !Double.isNaN(dbl) && !Double.isInfinite(dbl)
    }

    fun isAnyType(type: String?): Boolean {
        return StringUtil.isEmpty(type) || type.equalsIgnoreCase("object") || type.equalsIgnoreCase("any")
    }

    fun isWrapped(o: Object?): Boolean {
        return o is JavaObject || o is ObjectWrap
    }

    fun isFileObject(source: Object?): Boolean {
        val pc: PageContext = ThreadLocalPageContext.get()
        if (source is String) return false
        val file: Resource = ResourceUtil.toResourceNotExisting(pc, source.toString())
        return if (file.isFile()) true else false
    }
}