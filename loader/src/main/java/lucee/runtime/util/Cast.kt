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
package lucee.runtime.util

import java.awt.Color

/**
 * This class can cast object of one type to another by CFML rules
 */
interface Cast {
    /**
     * cast a boolean value to a boolean value (do nothing)
     *
     * @param b boolean value to cast
     * @return casted boolean value
     */
    fun toBooleanValue(b: Boolean): Boolean

    /**
     * cast a double value to a boolean value (primitive value type)
     *
     * @param d double value to cast
     * @return casted boolean value
     */
    fun toBooleanValue(d: Double): Boolean

    /**
     * cast a double value to a boolean value (primitive value type)
     *
     * @param c char value to cast
     * @return casted boolean value
     */
    fun toBooleanValue(c: Char): Boolean

    /**
     * cast an Object to a boolean value (primitive value type)
     *
     * @param o Object to cast
     * @return casted boolean value
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun toBooleanValue(o: Object?): Boolean

    /**
     * cast an Object to a Double Object (reference Type)
     *
     * @param o Object to cast
     * @return casted Double Object
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun toDouble(o: Object?): Double?

    /**
     * cast an Object to a Double Object (reference Type)
     *
     * @param o Object to cast
     * @param defaultValue Default Value
     * @return casted Double Object
     */
    fun toDouble(o: Object?, defaultValue: Double?): Double?

    /**
     * cast a String to a Double Object (reference Type)
     *
     * @param str String to cast
     * @return casted Double Object
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun toDouble(str: String?): Double?

    /**
     * cast a String to a Double Object (reference Type)
     *
     * @param str String to cast
     * @param defaultValue Default Value
     * @return casted Double Object
     */
    fun toDouble(str: String?, defaultValue: Double?): Double?

    /**
     * cast a double value to a Double Object (reference Type)
     *
     * @param d double value to cast
     * @return casted Double Object
     */
    fun toDouble(d: Double): Double?

    /**
     * cast a boolean value to a Double Object (reference Type)
     *
     * @param b boolean value to cast
     * @return casted Double Object
     */
    fun toDouble(b: Boolean): Double?

    /**
     * cast a char value to a Double Object (reference Type)
     *
     * @param c char value to cast
     * @return casted Double Object
     */
    fun toDouble(c: Char): Double?

    /**
     * cast an Object to a double value (primitive value Type)
     *
     * @param o Object to cast
     * @return casted double value
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun toDoubleValue(o: Object?): Double

    /**
     * cast an Object to a double value (primitive value Type)
     *
     * @param str String to cast
     * @return casted double value
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun toDoubleValue(str: String?): Double

    /**
     * cast an Object to a double value (primitive value Type)
     *
     * @param o Object to cast
     * @param defaultValue if can't cast return this value
     * @return casted double value
     */
    fun toDoubleValue(o: Object?, defaultValue: Double): Double

    /**
     * cast an Object to a double value (primitive value Type), if can't return Double.NaN
     *
     * @param str String to cast
     * @param defaultValue if can't cast return this value
     * @return casted double value
     */
    fun toDoubleValue(str: String?, defaultValue: Double): Double

    /**
     * cast a double value to a double value (do nothing)
     *
     * @param d double value to cast
     * @return casted double value
     */
    fun toDoubleValue(d: Double): Double

    /**
     * cast a boolean value to a double value (primitive value type)
     *
     * @param b boolean value to cast
     * @return casted double value
     */
    fun toDoubleValue(b: Boolean): Double

    /**
     * cast a char value to a double value (primitive value type)
     *
     * @param c char value to cast
     * @return casted double value
     */
    fun toDoubleValue(c: Char): Double

    /**
     * cast an Object to an int value (primitive value type)
     *
     * @param o Object to cast
     * @return casted int value
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun toIntValue(o: Object?): Int

    /**
     * cast an Object to an int value (primitive value type)
     *
     * @param o Object to cast
     * @param defaultValue Default Value
     * @return casted int value
     */
    fun toIntValue(o: Object?, defaultValue: Int): Int

    /**
     * cast a String to an int value (primitive value type)
     *
     * @param str String to cast
     * @return casted int value
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun toIntValue(str: String?): Int

    /**
     * cast an Object to a double value (primitive value Type), if can't return Integer.MIN_VALUE
     *
     * @param str String to cast
     * @param defaultValue Default Value
     * @return casted double value
     */
    fun toIntValue(str: String?, defaultValue: Int): Int

    /**
     * cast a double value to an int value (primitive value type)
     *
     * @param d double value to cast
     * @return casted int value
     */
    fun toIntValue(d: Double): Int

    /**
     * cast a boolean value to an int value (primitive value type)
     *
     * @param b boolean value to cast
     * @return casted int value
     */
    fun toIntValue(b: Boolean): Int

    /**
     * cast a char value to an int value (primitive value type)
     *
     * @param c char value to cast
     * @return casted int value
     */
    fun toIntValue(c: Char): Int

    /**
     * cast a double to a decimal value (String:xx.xx)
     *
     * @param value Object to cast
     * @return casted decimal value
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun toDecimal(value: Object?): String?

    /**
     * cast a double to a decimal value (String:xx.xx)
     *
     * @param value Object to cast
     * @param defaultValue Default Value
     * @return casted decimal value
     */
    fun toDecimal(value: Object?, defaultValue: String?): String?

    /**
     * cast a char to a decimal value (String:xx.xx)
     *
     * @param c char to cast
     * @return casted decimal value
     */
    fun toDecimal(c: Char): String?

    /**
     * cast a boolean to a decimal value (String:xx.xx)
     *
     * @param b boolean to cast
     * @return casted decimal value
     */
    fun toDecimal(b: Boolean): String?

    /**
     * cast a double to a decimal value (String:xx.xx)
     *
     * @param d double to cast
     * @return casted decimal value
     */
    fun toDecimal(d: Double): String?

    /**
     * cast a boolean value to a Boolean Object(reference type)
     *
     * @param b boolean value to cast
     * @return casted Boolean Object
     */
    fun toBoolean(b: Boolean): Boolean?

    /**
     * cast a char value to a Boolean Object(reference type)
     *
     * @param c char value to cast
     * @return casted Boolean Object
     */
    fun toBoolean(c: Char): Boolean?

    /**
     * cast a double value to a Boolean Object(reference type)
     *
     * @param d double value to cast
     * @return casted Boolean Object
     */
    fun toBoolean(d: Double): Boolean?

    /**
     * cast an Object to a Boolean Object(reference type)
     *
     * @param o Object to cast
     * @return casted Boolean Object
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun toBoolean(o: Object?): Boolean?

    /**
     * cast an Object to a Boolean Object(reference type)
     *
     * @param str String to cast
     * @return casted Boolean Object
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun toBoolean(str: String?): Boolean?

    /**
     * cast an Object to a boolean value (primitive value type), Exception Less
     *
     * @param o Object to cast
     * @param defaultValue Default Value
     * @return casted boolean value
     */
    fun toBooleanValue(o: Object?, defaultValue: Boolean): Boolean

    /**
     * cast an Object to a boolean value (reference type), Exception Less
     *
     * @param o Object to cast
     * @param defaultValue default value
     * @return casted boolean reference
     */
    fun toBoolean(o: Object?, defaultValue: Boolean?): Boolean?

    /**
     * cast an Object to a boolean value (reference type), Exception Less
     *
     * @param str String to cast
     * @param defaultValue default value
     * @return casted boolean reference
     */
    fun toBoolean(str: String?, defaultValue: Boolean?): Boolean?

    /**
     * cast a boolean value to a char value
     *
     * @param b boolean value to cast
     * @return casted char value
     */
    fun toCharValue(b: Boolean): Char

    /**
     * cast a double value to a char value (primitive value type)
     *
     * @param d double value to cast
     * @return casted char value
     */
    fun toCharValue(d: Double): Char

    /**
     * cast a char value to a char value (do nothing)
     *
     * @param c char value to cast
     * @return casted char value
     */
    fun toCharValue(c: Char): Char

    /**
     * cast an Object to a char value (primitive value type)
     *
     * @param o Object to cast
     * @return casted char value
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun toCharValue(o: Object?): Char

    /**
     * cast an Object to a char value (primitive value type)
     *
     * @param o Object to cast
     * @param defaultValue Default Value
     * @return casted char value
     */
    fun toCharValue(o: Object?, defaultValue: Char): Char

    /**
     * cast a boolean value to a Character Object(reference type)
     *
     * @param b boolean value to cast
     * @return casted Character Object
     */
    fun toCharacter(b: Boolean): Character?

    /**
     * cast a char value to a Character Object(reference type)
     *
     * @param c char value to cast
     * @return casted Character Object
     */
    fun toCharacter(c: Char): Character?

    /**
     * cast a double value to a Character Object(reference type)
     *
     * @param d double value to cast
     * @return casted Character Object
     */
    fun toCharacter(d: Double): Character?

    /**
     * cast an Object to a Character Object(reference type)
     *
     * @param o Object to cast
     * @param defaultValue Default Value
     * @return casted Character Object
     */
    fun toCharacter(o: Object?, defaultValue: Character?): Character?

    /**
     * cast an Object to a Character Object(reference type)
     *
     * @param o Object to cast
     * @return casted Character Object
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun toCharacter(o: Object?): Character?

    /**
     * cast a boolean value to a byte value
     *
     * @param b boolean value to cast
     * @return casted byte value
     */
    fun toByteValue(b: Boolean): Byte

    /**
     * cast a double value to a byte value (primitive value type)
     *
     * @param d double value to cast
     * @return casted byte value
     */
    fun toByteValue(d: Double): Byte

    /**
     * cast a char value to a byte value (do nothing)
     *
     * @param c char value to cast
     * @return casted byte value
     */
    fun toByteValue(c: Char): Byte

    /**
     * cast an Object to a byte value (primitive value type)
     *
     * @param o Object to cast
     * @return casted byte value
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun toByteValue(o: Object?): Byte

    /**
     * cast an Object to a byte value (primitive value type)
     *
     * @param o Object to cast
     * @param defaultValue Default Value
     * @return casted byte value
     */
    fun toByteValue(o: Object?, defaultValue: Byte): Byte

    /**
     * cast a boolean value to a Byte Object(reference type)
     *
     * @param b boolean value to cast
     * @return casted Byte Object
     */
    fun toByte(b: Boolean): Byte?

    /**
     * cast a char value to a Byte Object(reference type)
     *
     * @param c char value to cast
     * @return casted Byte Object
     */
    fun toByte(c: Char): Byte?

    /**
     * cast a double value to a Byte Object(reference type)
     *
     * @param d double value to cast
     * @return casted Byte Object
     */
    fun toByte(d: Double): Byte?

    /**
     * cast an Object to a Byte Object(reference type)
     *
     * @param o Object to cast
     * @return casted Byte Object
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun toByte(o: Object?): Byte?

    /**
     * cast an Object to a Byte Object(reference type)
     *
     * @param o Object to cast
     * @param defaultValue Default Value
     * @return casted Byte Object
     */
    fun toByte(o: Object?, defaultValue: Byte?): Byte?

    /**
     * cast a boolean value to a long value
     *
     * @param b boolean value to cast
     * @return casted long value
     */
    fun toLongValue(b: Boolean): Long

    /**
     * cast a double value to a long value (primitive value type)
     *
     * @param d double value to cast
     * @return casted long value
     */
    fun toLongValue(d: Double): Long

    /**
     * cast a char value to a long value (do nothing)
     *
     * @param c char value to cast
     * @return casted long value
     */
    fun toLongValue(c: Char): Long

    /**
     * cast an Object to a long value (primitive value type)
     *
     * @param o Object to cast
     * @return casted long value
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun toLongValue(o: Object?): Long

    /**
     * cast an Object to a long value (primitive value type)
     *
     * @param o Object to cast
     * @param defaultValue Default Value
     * @return casted long value
     */
    fun toLongValue(o: Object?, defaultValue: Long): Long

    /**
     * cast a boolean value to a Long Object(reference type)
     *
     * @param b boolean value to cast
     * @return casted Long Object
     */
    fun toLong(b: Boolean): Long?

    /**
     * cast a char value to a Long Object(reference type)
     *
     * @param c char value to cast
     * @return casted Long Object
     */
    fun toLong(c: Char): Long?

    /**
     * cast a double value to a Long Object(reference type)
     *
     * @param d double value to cast
     * @return casted Long Object
     */
    fun toLong(d: Double): Long?

    /**
     * cast an Object to a Long Object(reference type)
     *
     * @param o Object to cast
     * @return casted Long Object
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun toLong(o: Object?): Long?

    /**
     * cast an Object to a Long Object(reference type)
     *
     * @param o Object to cast
     * @param defaultValue Default Value
     * @return casted Long Object
     */
    fun toLong(o: Object?, defaultValue: Long?): Long?

    @Throws(PageException::class)
    fun toKey(o: Object?): Collection.Key?
    fun toKey(str: String?): Collection.Key?
    fun toKey(o: Object?, defaultValue: Collection.Key?): Collection.Key?

    /**
     * cast a boolean value to a short value
     *
     * @param b boolean value to cast
     * @return casted short value
     */
    fun toShortValue(b: Boolean): Short

    /**
     * cast a double value to a short value (primitive value type)
     *
     * @param d double value to cast
     * @return casted short value
     */
    fun toShortValue(d: Double): Short

    /**
     * cast a char value to a short value (do nothing)
     *
     * @param c char value to cast
     * @return casted short value
     */
    fun toShortValue(c: Char): Short

    /**
     * cast an Object to a short value (primitive value type)
     *
     * @param o Object to cast
     * @return casted short value
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun toShortValue(o: Object?): Short

    /**
     * cast an Object to a short value (primitive value type)
     *
     * @param o Object to cast
     * @param defaultValue Default Value
     * @return casted short value
     */
    fun toShortValue(o: Object?, defaultValue: Short): Short

    /**
     * cast a boolean value to a Short Object(reference type)
     *
     * @param b boolean value to cast
     * @return casted Short Object
     */
    fun toShort(b: Boolean): Short?

    /**
     * cast a char value to a Short Object(reference type)
     *
     * @param c char value to cast
     * @return casted Short Object
     */
    fun toShort(c: Char): Short?

    /**
     * cast a double value to a Byte Object(reference type)
     *
     * @param d double value to cast
     * @return casted Byte Object
     */
    fun toShort(d: Double): Short?

    /**
     * cast an Object to a Short Object(reference type)
     *
     * @param o Object to cast
     * @return casted Short Object
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun toShort(o: Object?): Short?

    /**
     * cast an Object to a Short Object(reference type)
     *
     * @param o Object to cast
     * @param defaultValue Default Value
     * @return casted Short Object
     */
    fun toShort(o: Object?, defaultValue: Short?): Short?

    /**
     * cast a String to a boolean value (primitive value type)
     *
     * @param str String to cast
     * @return casted boolean value
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun toBooleanValue(str: String?): Boolean

    /**
     * cast a String to a boolean value (primitive value type), return 1 for true, 0 for false and -1 if
     * can't cast to a boolean type
     *
     * @param str String to cast
     * @param defaultValue Default Value
     * @return casted boolean value
     */
    fun toBooleanValue(str: String?, defaultValue: Boolean): Boolean

    /**
     * cast an Object to a String
     *
     * @param o Object to cast
     * @return casted String
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun toString(o: Object?): String?

    /**
     * cast an Object to a String dont throw an exception, if can't cast to a string return an empty string
     *
     * @param o Object to cast
     * @param defaultValue Default Value
     * @return casted String
     */
    fun toString(o: Object?, defaultValue: String?): String?

    /**
     * cast a double value to a String
     *
     * @param d double value to cast
     * @return casted String
     */
    fun toString(d: Double): String?

    /**
     * cast a long value to a String
     *
     * @param l long value to cast
     * @return casted String
     */
    fun toString(l: Long): String?

    /**
     * cast an int value to a String
     *
     * @param i int value to cast
     * @return casted String
     */
    fun toString(i: Int): String?

    /**
     * cast a boolean value to a String
     *
     * @param b boolean value to cast
     * @return casted String
     */
    fun toString(b: Boolean): String?

    /**
     * cast an Object to an Array Object
     *
     * @param o Object to cast
     * @return casted Array
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun toList(o: Object?): List?

    /**
     * cast an Object to an Array Object
     *
     * @param o Object to cast
     * @param defaultValue Default Value
     * @return casted Array
     */
    fun toList(o: Object?, defaultValue: List?): List?

    /**
     * cast an Object to an Array Object
     *
     * @param o Object to cast
     * @param duplicate duplicate
     * @return casted Array
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun toList(o: Object?, duplicate: Boolean): List?

    /**
     * cast an Object to an Array Object
     *
     * @param o Object to cast
     * @param duplicate duplicate
     * @param defaultValue Default Value
     * @return casted Array
     */
    fun toList(o: Object?, duplicate: Boolean, defaultValue: List?): List?

    /**
     * cast an Object to an Array Object
     *
     * @param obj Object to cast
     * @return casted Array
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun toArray(obj: Object?): Array?

    /**
     * cast an Object to an Array Object
     *
     * @param obj Object to cast
     * @param defaultValue Default Value
     * @return casted Array
     */
    fun toArray(obj: Object?, defaultValue: Array?): Array?

    /**
     * cast an Object to a "native" Java Array
     *
     * @param obj Object to cast
     * @return casted Array
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun toNativeArray(obj: Object?): Array<Object?>?

    /**
     * cast an Object to a "native" Java Array
     *
     * @param obj Object to cast
     * @param defaultValue Default Value
     * @return casted Array
     */
    fun toNativeArray(obj: Object?, defaultValue: Array<Object?>?): Array<Object?>?

    /**
     * cast an Object to a Map Object
     *
     * @param o Object to cast
     * @return casted Struct
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun toMap(o: Object?): Map<*, *>?

    /**
     * cast an Object to a Map Object
     *
     * @param o Object to cast
     * @param defaultValue Default Value
     * @return casted Struct
     */
    fun toMap(o: Object?, defaultValue: Map?): Map?

    /**
     * cast an Object to a Map Object
     *
     * @param o Object to cast
     * @param duplicate duplicate
     * @return casted Struct
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun toMap(o: Object?, duplicate: Boolean): Map?

    /**
     * cast an Object to a Map Object
     *
     * @param o Object to cast
     * @param duplicate duplicate
     * @param defaultValue Default Value
     * @return casted Struct
     */
    fun toMap(o: Object?, duplicate: Boolean, defaultValue: Map?): Map?

    /**
     * cast an Object to a Struct Object
     *
     * @param o Object to cast
     * @return casted Struct
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun toStruct(o: Object?): Struct?

    /**
     * cast an Object to a Struct Object
     *
     * @param o Object to cast
     * @param defaultValue Default Value
     * @return casted Struct
     */
    fun toStruct(o: Object?, defaultValue: Struct?): Struct?
    fun toStruct(o: Object?, defaultValue: Struct?, caseSensitive: Boolean): Struct?

    /**
     * cast an Object to a Binary
     *
     * @param obj Object to cast
     * @return casted Binary
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun toBinary(obj: Object?): ByteArray?

    /**
     * cast an Object to a Binary
     *
     * @param obj Object to cast
     * @param defaultValue Default Value
     * @return casted Binary
     */
    fun toBinary(obj: Object?, defaultValue: ByteArray?): ByteArray?

    /**
     * cast an Object to a Base64 value
     *
     * @param o Object to cast
     * @return to Base64 String
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun toBase64(o: Object?): String?

    /**
     * cast an Object to a Base64 value
     *
     * @param o Object to cast
     * @param defaultValue Default Value
     * @return to Base64 String
     */
    fun toBase64(o: Object?, defaultValue: String?): String?

    /**
     * cast a boolean to a DateTime Object
     *
     * @param b boolean to cast
     * @param tz timezone
     * @return casted DateTime Object
     */
    fun toDate(b: Boolean, tz: TimeZone?): DateTime?

    /**
     * cast a char to a DateTime Object
     *
     * @param c char to cast
     * @param tz timezone
     * @return casted DateTime Object
     */
    fun toDate(c: Char, tz: TimeZone?): DateTime?

    /**
     * cast a double to a DateTime Object
     *
     * @param d double to cast
     * @param tz timezone
     * @return casted DateTime Object
     */
    fun toDate(d: Double, tz: TimeZone?): DateTime?

    /**
     * cast an Object to a DateTime Object
     *
     * @param o Object to cast
     * @param tz timezone
     * @return casted DateTime Object
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun toDate(o: Object?, tz: TimeZone?): DateTime?

    /**
     * cast an Object to a DateTime Object
     *
     * @param str String to cast
     * @param tz timezone
     * @return casted DateTime Object
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun toDate(str: String?, tz: TimeZone?): DateTime?

    /**
     * cast an Object to a DateTime Object
     *
     * @param o Object to cast
     * @param alsoNumbers define if also numbers will casted to a datetime value
     * @param tz timezone
     * @param defaultValue Default Value
     * @return casted DateTime Object
     */
    fun toDate(o: Object?, alsoNumbers: Boolean, tz: TimeZone?, defaultValue: DateTime?): DateTime?

    /**
     * cast an Object to a DateTime Object
     *
     * @param str String to cast
     * @param alsoNumbers define if also numbers will casted to a datetime value
     * @param tz timezone
     * @param defaultValue Default Value
     * @return casted DateTime Object
     */
    fun toDate(str: String?, alsoNumbers: Boolean, tz: TimeZone?, defaultValue: DateTime?): DateTime?

    /**
     * cast an Object to a DateTime Object
     *
     * @param o Object to cast
     * @param tz timezone
     * @return casted DateTime Object
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun toDateTime(o: Object?, tz: TimeZone?): DateTime?

    /**
     * cast an Object to a DateTime Object
     *
     * @param o Object to cast
     * @param tz timezone
     * @param defaultValue Default Value
     * @return casted DateTime Object
     */
    fun toDateTime(o: Object?, tz: TimeZone?, defaultValue: DateTime?): DateTime?

    /**
     * cast an Object to a DateTime Object (alias for toDateTime)
     *
     * @param o Object to cast
     * @param tz timezone
     * @return casted DateTime Object
     * @throws PageException Page Exception
     */
    @Deprecated
    @Throws(PageException::class)
    fun toDatetime(o: Object?, tz: TimeZone?): DateTime?

    /**
     * parse a string to a Datetime Object
     *
     * @param locale locale
     * @param str String representation of a locale Date
     * @param tz timezone
     * @return DateTime Object
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun toDate(locale: Locale?, str: String?, tz: TimeZone?): DateTime?

    /**
     * parse a string to a Datetime Object, returns null if can't convert
     *
     * @param locale locale
     * @param str String representation of a locale Date
     * @param tz timezone
     * @param defaultValue Default Value
     * @return datetime object
     */
    fun toDate(locale: Locale?, str: String?, tz: TimeZone?, defaultValue: DateTime?): DateTime?

    /**
     * cast an Object to a Query Object
     *
     * @param o Object to cast
     * @return casted Query Object
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun toQuery(o: Object?): Query?

    /**
     * cast an Object to a Query Object
     *
     * @param o Object to cast
     * @param defaultValue Default Value
     * @return casted Query Object
     */
    fun toQuery(o: Object?, defaultValue: Query?): Query?

    /**
     * cast an Object to a Query Object
     *
     * @param o Object to cast
     * @param duplicate duplicate the object or not
     * @return casted Query Object
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun toQuery(o: Object?, duplicate: Boolean): Query?

    /**
     * cast an Object to a Query Object
     *
     * @param o Object to cast
     * @param duplicate duplicate the object or not
     * @param defaultValue Default Value
     * @return casted Query Object
     */
    fun toQuery(o: Object?, duplicate: Boolean, defaultValue: Query?): Query?

    /**
     * cast an Object to an UUID
     *
     * @param o Object to cast
     * @return casted Query Object
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun toUUId(o: Object?): Object?

    /**
     * cast an Object to an UUID
     *
     * @param o Object to cast
     * @param defaultValue Default Value
     * @return casted Query Object
     */
    fun toUUId(o: Object?, defaultValue: Object?): Object?

    /**
     * cast an Object to a Variable Name
     *
     * @param o Object to cast
     * @return casted Variable Name
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun toVariableName(o: Object?): String?

    /**
     * cast an Object to a Variable Name
     *
     * @param obj Object to cast
     * @param defaultValue Default Value
     * @return casted Variable Name
     */
    fun toVariableName(obj: Object?, defaultValue: String?): String?

    @Deprecated
    fun toVariableName(obj: Object?, defaultValue: Object?): Object?

    /**
     * cast an Object to a TimeSpan Object (alias for toTimeSpan)
     *
     * @param o Object to cast
     * @return casted TimeSpan Object
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun toTimespan(o: Object?): TimeSpan?

    /**
     * cast an Object to a TimeSpan Object (alias for toTimeSpan)
     *
     * @param o Object to cast
     * @param defaultValue Default Value
     * @return casted TimeSpan Object
     */
    fun toTimespan(o: Object?, defaultValue: TimeSpan?): TimeSpan?

    /**
     * convert milliseconds to a timespan
     *
     * @param millis milliseconds to convert
     * @return casted TimeSpan Object
     */
    fun toTimespan(millis: Long): TimeSpan?

    /**
     * cast a Throwable Object to a PageException Object
     *
     * @param t Throwable to cast
     * @return casted PageException Object
     */
    fun toPageException(t: Throwable?): PageException?

    /**
     * cast a Throwable Object to a PageRuntimeException Object (RuntimeException)
     *
     * @param t Throwable to cast
     * @return casted PageException Object
     */
    fun toPageRuntimeException(t: Throwable?): RuntimeException?

    /**
     * return the type name of an object (string, boolean, int aso.), type is not same like class name
     *
     * @param o Object to get type from
     * @return type of the object
     */
    fun toTypeName(o: Object?): String?

    /**
     * cast a value to a value defined by type argument
     *
     * @param pc Page Context
     * @param type type of the returning Value
     * @param o Object to cast
     * @return casted Value
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun castTo(pc: PageContext?, type: String?, o: Object?): Object?

    /**
     * cast a value to a value defined by type argument
     *
     * @param pc Page Context
     * @param type type of the returning Value
     * @param o Object to cast
     * @param alsoPattern mean supporting also none real types like email or creditcard ...
     * @return casted Value
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun castTo(pc: PageContext?, type: String?, o: Object?, alsoPattern: Boolean): Object?

    /**
     * cast a value to a value defined by type argument
     *
     * @param pc Page Context
     * @param type type of the returning Value (Example: Cast.TYPE_QUERY)
     * @param strType type as String
     * @param o Object to cast
     * @return casted Value
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun castTo(pc: PageContext?, type: Short, strType: String?, o: Object?): Object?

    /**
     * cast a value to a value defined by type argument
     *
     * @param pc Page Context
     * @param type type of the returning Value (Example: Cast.TYPE_QUERY)
     * @param o Object to cast
     * @return casted Value
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun castTo(pc: PageContext?, type: Short, o: Object?): Object?

    /**
     * cast a value to a value defined by type a class
     *
     * @param pc Page Context
     * @param trgClass class to generate
     * @param obj Object to cast
     * @return casted Value
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun castTo(pc: PageContext?, trgClass: Class?, obj: Object?): Object?

    /**
     * cast a value to void (Empty String)
     *
     * @param o Object to Cast
     * @return void value
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun toVoid(o: Object?): Object?

    /**
     * cast a value to void (Empty String)
     *
     * @param o Object to cast
     * @param defaultValue Default Value
     * @return void value
     */
    fun toVoid(o: Object?, defaultValue: Object?): Object?

    /**
     * cast an Object to a reference type (Object), in that case this method to nothing, because an Object
     * is already a reference type
     *
     * @param o Object to cast
     * @return casted Object
     */
    fun toRef(o: Object?): Object?

    /**
     * cast a String to a reference type (Object), in that case this method to nothing, because a String
     * is already a reference type
     *
     * @param o Object to cast
     * @return casted Object
     */
    fun toRef(o: String?): String?

    /**
     * cast a Collection to a reference type (Object), in that case this method to nothing, because a
     * Collection is already a reference type
     *
     * @param o Collection to cast
     * @return casted Object
     */
    fun toRef(o: Collection?): Collection?

    /**
     * cast a char value to his (CFML) reference type String
     *
     * @param c char to cast
     * @return casted String
     */
    fun toRef(c: Char): String?

    /**
     * cast a boolean value to his (CFML) reference type Boolean
     *
     * @param b boolean to cast
     * @return casted Boolean
     */
    fun toRef(b: Boolean): Boolean?

    /**
     * cast a byte value to his (CFML) reference type Boolean
     *
     * @param b byte to cast
     * @return casted Boolean
     */
    fun toRef(b: Byte): Byte?

    /**
     * cast a short value to his (CFML) reference type Integer
     *
     * @param s short to cast
     * @return casted Integer
     */
    fun toRef(s: Short): Short?

    /**
     * cast an int value to his (CFML) reference type Integer
     *
     * @param i int to cast
     * @return casted Integer
     */
    fun toRef(i: Int): Integer?

    /**
     * cast a float value to his (CFML) reference type Float
     *
     * @param f float to cast
     * @return casted Float
     */
    fun toRef(f: Float): Float?

    /**
     * cast a long value to his (CFML) reference type Long
     *
     * @param l long to cast
     * @return casted Long
     */
    fun toRef(l: Long): Long?

    /**
     * cast a double value to his (CFML) reference type Double
     *
     * @param d double to cast
     * @return casted Double
     */
    fun toRef(d: Double): Double?

    /**
     * cast an Object to an Iterator or get Iterator from Object
     *
     * @param o Object to cast
     * @return casted Collection
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun toIterator(o: Object?): Iterator?

    /**
     * cast an Object to a Collection
     *
     * @param o Object to cast
     * @return casted Collection
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun toCollection(o: Object?): Collection?

    /**
     * cast to a color object
     *
     * @param o Object to cast
     * @return Casted Color object
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun toColor(o: Object?): Color?

    /**
     * cast an Object to a Collection, if not returns null
     *
     * @param o Object to cast
     * @param defaultValue Default Value
     * @return casted Collection
     */
    fun toCollection(o: Object?, defaultValue: Collection?): Collection?

    /**
     * convert an object to a Resource
     *
     * @param obj Object to Cast
     * @return File
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun toResource(obj: Object?): Resource?

    /**
     * convert an object to a Resource
     *
     * @param obj Object to Cast
     * @param defaultValue Default Value
     * @return Resource
     */
    fun toResource(obj: Object?, defaultValue: Resource?): Resource?

    /**
     * convert an object to a File
     *
     * @param obj Object to Cast
     * @return File
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun toFile(obj: Object?): File?

    /**
     * convert an object to a File
     *
     * @param obj Object to Cast
     * @param defaultValue Default Value
     * @return File
     */
    fun toFile(obj: Object?, defaultValue: File?): File?

    /**
     * casts a string to a Locale
     *
     * @param strLocale string
     * @return Locale from String
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun toLocale(strLocale: String?): Locale?

    /**
     * casts a string to a Locale
     *
     * @param strLocale string
     * @param defaultValue Default Value
     * @return Locale from String
     */
    fun toLocale(strLocale: String?, defaultValue: Locale?): Locale?
    /*
	 * * casts an Object to a Node List
	 * 
	 * @param o Object to Cast
	 * 
	 * @return NodeList from Object
	 * 
	 * @throws PageException Page Exception
	 */
    // public NodeList toNodeList(Object o) throws PageException;
    /*
	 * * casts an Object to a Node List
	 * 
	 * @param o Object to Cast
	 * 
	 * @param defaultValue Default Value
	 * 
	 * @return NodeList from Object
	 */
    // public NodeList toNodeList(Object o, NodeList defaultValue);
    /*
	 * * casts an Object to a XML Node
	 * 
	 * @param o Object to Cast
	 * 
	 * @return Node from Object
	 * 
	 * @throws PageException Page Exception
	 */
    // public Node toNode(Object o) throws PageException;
    /*
	 * * casts an Object to a XML Node
	 * 
	 * @param o Object to Cast
	 * 
	 * @param defaultValue Default Value
	 * 
	 * @return Node from Object
	 */
    // public Node toNode(Object o, Node defaultValue);
    /**
     * casts a boolean to an Integer
     *
     * @param b boolean value
     * @return Integer from boolean
     */
    fun toInteger(b: Boolean): Integer?

    /**
     * casts a char to an Integer
     *
     * @param c char value
     * @return Integer from char
     */
    fun toInteger(c: Char): Integer?

    /**
     * casts a double to an Integer
     *
     * @param d double value
     * @return Integer from double
     */
    fun toInteger(d: Double): Integer?

    /**
     * casts an Object to an Integer
     *
     * @param o Object to cast to Integer
     * @return Integer from Object
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun toInteger(o: Object?): Integer?

    /**
     * casts an Object to an Integer
     *
     * @param o Object to cast to Integer
     * @param defaultValue Default Value
     * @return Integer from Object
     */
    fun toInteger(o: Object?, defaultValue: Integer?): Integer?

    /**
     * casts an Object to null
     *
     * @param value value
     * @return to null from Object
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun toNull(value: Object?): Object?

    @Throws(PageException::class)
    fun toFloat(o: Object?): Float?
    fun toFloat(o: Object?, defaultValue: Float?): Float?

    @Throws(PageException::class)
    fun toFloatValue(o: Object?): Float
    fun toFloatValue(o: Object?, defaultValue: Float): Float

    /**
     * casts an Object to null
     *
     * @param value value
     * @param defaultValue Default Value
     * @return to null from Object
     */
    fun toNull(value: Object?, defaultValue: Object?): Object?
    /*
	 * * cast Object to a XML Node
	 * 
	 * @param value value
	 * 
	 * @return XML Node
	 * 
	 * @throws PageException Page Exception
	 */
    // public Node toXML(Object value) throws PageException;
    /*
	 * * cast Object to a XML Node
	 * 
	 * @param value value
	 * 
	 * @param defaultValue Default Value
	 * 
	 * @return XML Node
	 */
    // public Node toXML(Object value, Node defaultValue);
    /**
     * cast to given type
     *
     * @param type Object type
     * @param o Object
     * @param alsoPattern also Pattern
     * @return Object casted to Type
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun to(type: String?, o: Object?, alsoPattern: Boolean): Object?

    /**
     * cast Object to a Serializable Object
     *
     * @param obj Object to Cast
     * @return Serializable Object
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun toSerializable(obj: Object?): Serializable?

    /**
     * cast Object to a Serializable Object
     *
     * @param object Object to Cast
     * @param defaultValue Default Value
     * @return Returns a Serializable Object.
     */
    fun toSerializable(`object`: Object?, defaultValue: Serializable?): Serializable?

    @Throws(PageException::class)
    fun toCharset(str: String?): Charset?
    fun toCharset(str: String?, defaultValue: Charset?): Charset?

    @Throws(PageException::class)
    fun toBigDecimal(obj: Object?): BigDecimal?
    fun toBigDecimal(obj: Object?, defaultValue: BigDecimal?): BigDecimal?

    @Throws(PageException::class)
    fun toComponent(obj: Object?): Component?
    fun toComponent(obj: Object?, defaultValue: Component?): Component?

    @Throws(PageException::class)
    fun toTimeZone(obj: Object?): TimeZone?
    fun toTimeZone(obj: Object?, defaultValue: TimeZone?): TimeZone?
    fun toCalendar(time: Long, timeZone: TimeZone?, locale: Locale?): Calendar?
    fun toDumpTable(sct: Struct?, title: String?, pageContext: PageContext?, maxlevel: Int, dp: DumpProperties?): DumpData? // FUTURE
    // public Credentials toCredentials(String username, String password);
}