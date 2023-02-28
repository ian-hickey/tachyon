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

import java.awt.Color

/**
 * Implementation of the cast interface
 */
class CastImpl : Cast {
    @Override
    @Throws(PageException::class)
    fun castTo(pc: PageContext?, type: Short, o: Object?): Object? {
        return Caster.castTo(pc, type, o)
    }

    @Override
    @Throws(PageException::class)
    fun castTo(pc: PageContext?, type: Short, strType: String?, o: Object?): Object? {
        return Caster.castTo(pc, type, strType, o)
    }

    @Override
    @Throws(PageException::class)
    fun castTo(pc: PageContext?, trgClass: Class?, obj: Object?): Object? {
        return Caster.castTo(pc, trgClass, obj)
    }

    @Override
    @Throws(PageException::class)
    fun castTo(pc: PageContext?, type: String?, o: Object?): Object? {
        return Caster.castTo(pc, type, o, false)
    }

    @Override
    @Throws(PageException::class)
    fun castTo(pc: PageContext?, type: String?, o: Object?, alsoPattern: Boolean): Object? {
        return Caster.castTo(pc, type, o, alsoPattern)
    }

    @Override
    fun toArray(obj: Object?, defaultValue: Array?): Array? {
        return Caster.toArray(obj, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun toArray(obj: Object?): Array? {
        return Caster.toArray(obj)
    }

    @Override
    @Throws(PageException::class)
    fun toNativeArray(obj: Object?): Array<Object?>? {
        return Caster.toNativeArray(obj)
    }

    @Override
    fun toNativeArray(obj: Object?, defaultValue: Array<Object?>?): Array<Object?>? {
        return Caster.toNativeArray(obj, defaultValue)
    }

    @Override
    fun toBase64(o: Object?, defaultValue: String?): String? {
        return Caster.toBase64(o, null, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun toBase64(o: Object?): String? {
        return Caster.toBase64(o, null)
    }

    @Override
    fun toBinary(obj: Object?, defaultValue: ByteArray?): ByteArray? {
        return Caster.toBinary(obj, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun toBinary(obj: Object?): ByteArray? {
        return Caster.toBinary(obj)
    }

    @Override
    fun toBoolean(b: Boolean): Boolean? {
        return Caster.toBoolean(b)
    }

    @Override
    fun toBoolean(c: Char): Boolean? {
        return Caster.toBoolean(c)
    }

    @Override
    fun toBoolean(d: Double): Boolean? {
        return Caster.toBoolean(d)
    }

    @Override
    fun toBoolean(o: Object?, defaultValue: Boolean?): Boolean? {
        return Caster.toBoolean(o, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun toBoolean(o: Object?): Boolean? {
        return Caster.toBoolean(o)
    }

    @Override
    fun toBoolean(str: String?, defaultValue: Boolean?): Boolean? {
        return Caster.toBoolean(str, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun toBoolean(str: String?): Boolean? {
        return Caster.toBoolean(str)
    }

    @Override
    fun toBooleanValue(b: Boolean): Boolean {
        return Caster.toBooleanValue(b)
    }

    @Override
    fun toBooleanValue(c: Char): Boolean {
        return Caster.toBooleanValue(c)
    }

    @Override
    fun toBooleanValue(d: Double): Boolean {
        return Caster.toBooleanValue(d)
    }

    @Override
    fun toBooleanValue(o: Object?, defaultValue: Boolean): Boolean {
        return Caster.toBooleanValue(o, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun toBooleanValue(o: Object?): Boolean {
        return Caster.toBooleanValue(o)
    }

    @Override
    fun toBooleanValue(str: String?, defaultValue: Boolean): Boolean {
        return Caster.toBooleanValue(str, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun toBooleanValue(str: String?): Boolean {
        return Caster.toBooleanValue(str)
    }

    @Override
    fun toByte(b: Boolean): Byte? {
        return Caster.toByte(b)
    }

    @Override
    fun toByte(c: Char): Byte? {
        return Caster.toByte(c)
    }

    @Override
    fun toByte(d: Double): Byte? {
        return Caster.toByte(d)
    }

    @Override
    fun toByte(o: Object?, defaultValue: Byte?): Byte? {
        return Caster.toByte(o, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun toByte(o: Object?): Byte? {
        return Caster.toByte(o)
    }

    @Override
    fun toByteValue(b: Boolean): Byte {
        return Caster.toByteValue(b)
    }

    @Override
    fun toByteValue(c: Char): Byte {
        return Caster.toByteValue(c)
    }

    @Override
    fun toByteValue(d: Double): Byte {
        return Caster.toByteValue(d)
    }

    @Override
    fun toByteValue(o: Object?, defaultValue: Byte): Byte {
        return Caster.toByteValue(o, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun toByteValue(o: Object?): Byte {
        return Caster.toByteValue(o)
    }

    @Override
    fun toCharacter(b: Boolean): Character? {
        return Caster.toCharacter(b)
    }

    @Override
    fun toCharacter(c: Char): Character? {
        return Caster.toCharacter(c)
    }

    @Override
    fun toCharacter(d: Double): Character? {
        return Caster.toCharacter(d)
    }

    @Override
    fun toCharacter(o: Object?, defaultValue: Character?): Character? {
        return Caster.toCharacter(o, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun toCharacter(o: Object?): Character? {
        return Caster.toCharacter(o)
    }

    @Override
    fun toCharValue(b: Boolean): Char {
        return Caster.toCharValue(b)
    }

    @Override
    fun toCharValue(c: Char): Char {
        return Caster.toCharValue(c)
    }

    @Override
    fun toCharValue(d: Double): Char {
        return Caster.toCharValue(d)
    }

    @Override
    fun toCharValue(o: Object?, defaultValue: Char): Char {
        return Caster.toCharValue(o, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun toCharValue(o: Object?): Char {
        return Caster.toCharValue(o)
    }

    @Override
    fun toCollection(o: Object?, defaultValue: Collection?): Collection? {
        return Caster.toCollection(o, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun toCollection(o: Object?): Collection? {
        return Caster.toCollection(o)
    }

    @Override
    @Throws(PageException::class)
    fun toColor(o: Object?): Color? {
        if (o is Color) return o as Color? else if (o is CharSequence) return ColorCaster.toColor(o.toString()) else if (o is Number) return ColorCaster.toColor(Integer.toHexString((o as Number?).intValue()))
        throw CasterException(o, Color::class.java)
    }

    @Override
    fun toDate(b: Boolean, tz: TimeZone?): DateTime? {
        return Caster.toDate(b, tz)
    }

    @Override
    fun toDate(c: Char, tz: TimeZone?): DateTime? {
        return Caster.toDate(c, tz)
    }

    @Override
    fun toDate(d: Double, tz: TimeZone?): DateTime? {
        return Caster.toDate(d, tz)
    }

    @Override
    fun toDate(locale: Locale?, str: String?, tz: TimeZone?, defaultValue: DateTime?): DateTime? {
        return DateCaster.toDateTime(locale, str, tz, defaultValue, true)
    }

    @Override
    @Throws(PageException::class)
    fun toDate(locale: Locale?, str: String?, tz: TimeZone?): DateTime? {
        return DateCaster.toDateTime(locale, str, tz, true)
    }

    @Override
    fun toDate(o: Object?, alsoNumbers: Boolean, tz: TimeZone?, defaultValue: DateTime?): DateTime? {
        return Caster.toDate(o, alsoNumbers, tz, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun toDate(o: Object?, tz: TimeZone?): DateTime? {
        return Caster.toDate(o, tz)
    }

    @Override
    fun toDate(str: String?, alsoNumbers: Boolean, tz: TimeZone?, defaultValue: DateTime?): DateTime? {
        return Caster.toDate(str, alsoNumbers, tz, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun toDate(str: String?, tz: TimeZone?): DateTime? {
        return Caster.toDate(str, tz)
    }

    @Override
    @Throws(PageException::class)
    fun toDatetime(o: Object?, tz: TimeZone?): DateTime? {
        return DateCaster.toDateAdvanced(o, tz)
    }

    @Override
    @Throws(PageException::class)
    fun toDateTime(o: Object?, tz: TimeZone?): DateTime? {
        return DateCaster.toDateAdvanced(o, tz)
    }

    @Override
    fun toDateTime(o: Object?, tz: TimeZone?, defaultValue: DateTime?): DateTime? {
        return DateCaster.toDateAdvanced(o, tz, defaultValue)
    }

    @Override
    fun toDecimal(b: Boolean): String? {
        return Caster.toDecimal(b)
    }

    @Override
    fun toDecimal(c: Char): String? {
        return Caster.toDecimal(c, true)
    }

    @Override
    fun toDecimal(d: Double): String? {
        return Caster.toDecimal(d, true)
    }

    @Override
    fun toDecimal(value: Object?, defaultValue: String?): String? {
        return Caster.toDecimal(value, true, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun toDecimal(value: Object?): String? {
        return Caster.toDecimal(value, true)
    }

    @Override
    fun toDouble(b: Boolean): Double? {
        return Caster.toDouble(b)
    }

    @Override
    fun toDouble(c: Char): Double? {
        return Caster.toDouble(c)
    }

    @Override
    fun toDouble(d: Double): Double? {
        return Caster.toDouble(d)
    }

    @Override
    fun toDouble(o: Object?, defaultValue: Double?): Double? {
        return Caster.toDouble(o, defaultValue!!)
    }

    @Override
    @Throws(PageException::class)
    fun toDouble(o: Object?): Double? {
        return Caster.toDouble(o)
    }

    @Override
    fun toDouble(str: String?, defaultValue: Double?): Double? {
        return Caster.toDouble(str, defaultValue!!)
    }

    @Override
    @Throws(PageException::class)
    fun toDouble(str: String?): Double? {
        return Caster.toDouble(str)
    }

    @Override
    fun toDoubleValue(b: Boolean): Double {
        return Caster.toDoubleValue(b)
    }

    @Override
    fun toDoubleValue(c: Char): Double {
        return Caster.toDoubleValue(c)
    }

    @Override
    fun toDoubleValue(d: Double): Double {
        return Caster.toDoubleValue(d)
    }

    @Override
    fun toDoubleValue(o: Object?, defaultValue: Double): Double {
        return Caster.toDoubleValue(o, true, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun toDoubleValue(o: Object?): Double {
        return Caster.toDoubleValue(o)
    }

    @Override
    fun toDoubleValue(str: String?, defaultValue: Double): Double {
        return Caster.toDoubleValue(str, true, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun toDoubleValue(str: String?): Double {
        return Caster.toDoubleValue(str)
    }

    @Override
    fun toFile(obj: Object?, defaultValue: File?): File? {
        return Caster.toFile(obj, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun toFile(obj: Object?): File? {
        return Caster.toFile(obj)
    }

    @Override
    fun toInteger(b: Boolean): Integer? {
        return Caster.toInteger(b)
    }

    @Override
    fun toInteger(c: Char): Integer? {
        return Caster.toInteger(c)
    }

    @Override
    fun toInteger(d: Double): Integer? {
        return Caster.toInteger(d)
    }

    @Override
    fun toInteger(o: Object?, defaultValue: Integer?): Integer? {
        return Caster.toInteger(o, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun toInteger(o: Object?): Integer? {
        return Caster.toInteger(o)
    }

    @Override
    fun toIntValue(b: Boolean): Int {
        return Caster.toIntValue(b)
    }

    @Override
    fun toIntValue(c: Char): Int {
        return Caster.toIntValue(c)
    }

    @Override
    fun toIntValue(d: Double): Int {
        return Caster.toIntValue(d)
    }

    @Override
    fun toIntValue(o: Object?, defaultValue: Int): Int {
        return Caster.toIntValue(o, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun toIntValue(o: Object?): Int {
        return Caster.toIntValue(o)
    }

    @Override
    fun toIntValue(str: String?, defaultValue: Int): Int {
        return Caster.toIntValue(str, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun toIntValue(str: String?): Int {
        return Caster.toIntValue(str)
    }

    @Override
    @Throws(PageException::class)
    fun toIterator(o: Object?): Iterator? {
        return Caster.toIterator(o)
    }

    @Override
    fun toList(o: Object?, duplicate: Boolean, defaultValue: List?): List? {
        return Caster.toList(o, duplicate, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun toList(o: Object?, duplicate: Boolean): List? {
        return Caster.toList(o, duplicate)
    }

    @Override
    fun toList(o: Object?, defaultValue: List?): List? {
        return Caster.toList(o, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun toList(o: Object?): List? {
        return Caster.toList(o)
    }

    @Override
    fun toLocale(strLocale: String?, defaultValue: Locale?): Locale? {
        return Caster.toLocale(strLocale, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun toLocale(strLocale: String?): Locale? {
        return Caster.toLocale(strLocale)
    }

    @Override
    fun toLong(b: Boolean): Long? {
        return Caster.toLong(b)
    }

    @Override
    fun toLong(c: Char): Long? {
        return Caster.toLong(c)
    }

    @Override
    fun toLong(d: Double): Long? {
        return Caster.toLong(d)
    }

    @Override
    fun toLong(o: Object?, defaultValue: Long?): Long? {
        return Caster.toLong(o, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun toLong(o: Object?): Long? {
        return Caster.toLong(o)
    }

    @Override
    fun toLongValue(b: Boolean): Long {
        return Caster.toLongValue(b)
    }

    @Override
    fun toLongValue(c: Char): Long {
        return Caster.toLongValue(c)
    }

    @Override
    fun toLongValue(d: Double): Long {
        return Caster.toLongValue(d)
    }

    @Override
    fun toLongValue(o: Object?, defaultValue: Long): Long {
        return Caster.toLongValue(o, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun toLongValue(o: Object?): Long {
        return Caster.toLongValue(o)
    }

    @Override
    fun toMap(o: Object?, duplicate: Boolean, defaultValue: Map?): Map? {
        return Caster.toMap(o, duplicate, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun toMap(o: Object?, duplicate: Boolean): Map? {
        return Caster.toMap(o, duplicate)
    }

    @Override
    fun toMap(o: Object?, defaultValue: Map?): Map? {
        return Caster.toMap(o, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun toMap(o: Object?): Map? {
        return Caster.toMap(o)
    }

    // @Override
    fun toNode(o: Object?, defaultValue: Node?): Node? {
        return Caster.toNode(o, defaultValue)
    }

    // @Override
    @Throws(PageException::class)
    fun toNode(o: Object?): Node? {
        return Caster.toNode(o)
    }

    // @Override
    fun toNodeList(o: Object?, defaultValue: NodeList?): NodeList? {
        return Caster.toNodeList(o, defaultValue)
    }

    // @Override
    @Throws(PageException::class)
    fun toNodeList(o: Object?): NodeList? {
        return Caster.toNodeList(o)
    }

    @Override
    fun toNull(value: Object?, defaultValue: Object?): Object? {
        return Caster.toNull(value, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun toNull(value: Object?): Object? {
        return Caster.toNull(value)
    }

    @Override
    @Throws(PageException::class)
    fun toKey(o: Object?): Collection.Key? {
        return Caster.toKey(o)
    }

    @Override
    fun toKey(str: String?): Collection.Key? {
        return KeyImpl.init(str)
    }

    @Override
    fun toKey(o: Object?, defaultValue: Collection.Key?): Collection.Key? {
        return Caster.toKey(o, defaultValue)
    }

    @Override
    fun toPageException(t: Throwable?): PageException? {
        return Caster.toPageException(t)
    }

    @Override
    fun toQuery(o: Object?, duplicate: Boolean, defaultValue: Query?): Query? {
        return Caster.toQuery(o, duplicate, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun toQuery(o: Object?, duplicate: Boolean): Query? {
        return Caster.toQuery(o, duplicate)
    }

    @Override
    fun toQuery(o: Object?, defaultValue: Query?): Query? {
        return Caster.toQuery(o, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun toQuery(o: Object?): Query? {
        return Caster.toQuery(o)
    }

    @Override
    fun toRef(b: Boolean): Boolean? {
        return Caster.toRef(b)
    }

    @Override
    fun toRef(b: Byte): Byte? {
        return Caster.toRef(b)
    }

    @Override
    fun toRef(c: Char): String? {
        return Caster.toRef(c)
    }

    @Override
    fun toRef(o: Collection?): Collection? {
        return Caster.toRef(o)
    }

    @Override
    fun toRef(d: Double): Double? {
        return Caster.toRef(d)
    }

    @Override
    fun toRef(f: Float): Float? {
        return Caster.toRef(f)
    }

    @Override
    fun toRef(i: Int): Integer? {
        return Caster.toRef(i)
    }

    @Override
    fun toRef(l: Long): Long? {
        return Caster.toRef(l)
    }

    @Override
    fun toRef(o: Object?): Object? {
        return Caster.toRef(o)
    }

    @Override
    fun toRef(s: Short): Short? {
        return Caster.toRef(s)
    }

    @Override
    fun toRef(str: String?): String? {
        return Caster.toRef(str)
    }

    @Override
    fun toShort(b: Boolean): Short? {
        return Caster.toShort(b)
    }

    @Override
    fun toShort(c: Char): Short? {
        return Caster.toShort(c)
    }

    @Override
    fun toShort(d: Double): Short? {
        return Caster.toShort(d)
    }

    @Override
    fun toShort(o: Object?, defaultValue: Short?): Short? {
        return Caster.toShort(o, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun toShort(o: Object?): Short? {
        return Caster.toShort(o)
    }

    @Override
    fun toShortValue(b: Boolean): Short {
        return Caster.toShortValue(b)
    }

    @Override
    fun toShortValue(c: Char): Short {
        return Caster.toShortValue(c)
    }

    @Override
    fun toShortValue(d: Double): Short {
        return Caster.toShortValue(d)
    }

    @Override
    fun toShortValue(o: Object?, defaultValue: Short): Short {
        return Caster.toShortValue(o, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun toShortValue(o: Object?): Short {
        return Caster.toShortValue(o)
    }

    @Override
    fun toString(b: Boolean): String? {
        return Caster.toString(b)
    }

    @Override
    fun toString(d: Double): String? {
        return Caster.toString(d)
    }

    @Override
    fun toString(i: Int): String? {
        return Caster.toString(i)
    }

    @Override
    fun toString(l: Long): String? {
        return Caster.toString(l)
    }

    @Override
    fun toString(o: Object?, defaultValue: String?): String? {
        return Caster.toString(o, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun toString(o: Object?): String? {
        return Caster.toString(o)
    }

    @Override
    fun toStruct(o: Object?, defaultValue: Struct?, caseSensitive: Boolean): Struct? {
        return Caster.toStruct(o, defaultValue, caseSensitive)
    }

    @Override
    fun toStruct(o: Object?, defaultValue: Struct?): Struct? {
        return Caster.toStruct(o, defaultValue, true)
    }

    @Override
    @Throws(PageException::class)
    fun toStruct(o: Object?): Struct? {
        return Caster.toStruct(o)
    }

    @Override
    fun toTimespan(o: Object?, defaultValue: TimeSpan?): TimeSpan? {
        return Caster.toTimespan(o, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun toTimespan(o: Object?): TimeSpan? {
        return Caster.toTimespan(o)
    }

    @Override
    fun toTimespan(millis: Long): TimeSpan? {
        return TimeSpanImpl.fromMillis(millis)
    }

    @Override
    fun toTypeName(o: Object?): String? {
        return Caster.toTypeName(o)
    }

    @Override
    fun toUUId(o: Object?, defaultValue: Object?): Object? {
        return Caster.toUUId(o, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun toUUId(o: Object?): Object? {
        return Caster.toUUId(o)
    }

    @Override
    fun toVariableName(obj: Object?, defaultValue: Object?): Object? {
        return Caster.toVariableName(obj, null) ?: return defaultValue
    }

    @Override
    fun toVariableName(obj: Object?, defaultValue: String?): String? {
        return Caster.toVariableName(obj, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun toVariableName(o: Object?): String? {
        return Caster.toVariableName(o)
    }

    @Override
    fun toVoid(o: Object?, defaultValue: Object?): Object? {
        return Caster.toVoid(o, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun toVoid(o: Object?): Object? {
        return Caster.toVoid(o)
    }

    // @Override
    fun toXML(value: Object?, defaultValue: Node?): Node? {
        return Caster.toXML(value, defaultValue)
    }

    // @Override
    @Throws(PageException::class)
    fun toXML(value: Object?): Node? {
        return Caster.toXML(value)
    }

    @Override
    @Throws(PageException::class)
    fun toResource(obj: Object?): Resource? {
        if (obj is Resource) return obj as Resource?
        return if (obj is File) ResourceUtil.toResource(obj as File?) else ResourceUtil.toResourceNotExisting(ThreadLocalPageContext.get(), toString(obj))
    }

    @Override
    fun toResource(obj: Object?, defaultValue: Resource?): Resource? {
        if (obj is Resource) return obj as Resource?
        val path = toString(obj, null) ?: return defaultValue
        return ResourceUtil.toResourceNotExisting(ThreadLocalPageContext.get(), path)
    }

    @Override
    @Throws(PageException::class)
    fun to(type: String?, o: Object?, alsoPattern: Boolean): Object? {
        return Caster.castTo(ThreadLocalPageContext.get(), type, o, alsoPattern)
    }

    @Override
    @Throws(PageException::class)
    fun toSerializable(obj: Object?): Serializable? {
        return Caster.toSerializable(obj)
    }

    @Override
    fun toSerializable(`object`: Object?, defaultValue: Serializable?): Serializable? {
        return Caster.toSerializable(`object`, defaultValue)
    }

    @Override
    fun toCharset(strCharset: String?): Charset? {
        return CharsetUtil.toCharset(strCharset)
    }

    @Override
    fun toCharset(strCharset: String?, defaultValue: Charset?): Charset? {
        return CharsetUtil.toCharset(strCharset, defaultValue)
    }

    @Override
    fun toPageRuntimeException(t: Throwable?): RuntimeException? {
        return PageRuntimeException(toPageException(t))
    }

    @Override
    @Throws(PageException::class)
    fun toFloat(o: Object?): Float? {
        return Caster.toFloat(o)
    }

    @Override
    fun toFloat(o: Object?, defaultValue: Float?): Float? {
        return Caster.toFloat(o, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun toFloatValue(o: Object?): Float {
        return Caster.toFloatValue(o)
    }

    @Override
    fun toFloatValue(o: Object?, defaultValue: Float): Float {
        return Caster.toFloatValue(o, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun toBigDecimal(obj: Object?): BigDecimal? {
        return Caster.toBigDecimal(obj)
    }

    @Override
    fun toBigDecimal(obj: Object?, defaultValue: BigDecimal?): BigDecimal? {
        return try {
            Caster.toBigDecimal(obj)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            defaultValue
        }
    }

    @Override
    @Throws(PageException::class)
    fun toComponent(obj: Object?): Component? {
        return Caster.toComponent(obj)
    }

    @Override
    fun toComponent(obj: Object?, defaultValue: Component?): Component? {
        return Caster.toComponent(obj, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun toTimeZone(obj: Object?): TimeZone? {
        return Caster.toTimeZone(obj)
    }

    @Override
    fun toTimeZone(obj: Object?, defaultValue: TimeZone?): TimeZone? {
        return Caster.toTimeZone(obj, defaultValue)
    }

    @Override
    fun toCalendar(time: Long, tz: TimeZone?, l: Locale?): Calendar? {
        return Caster.toCalendar(time, tz, l)
    }

    @Override
    fun toDumpTable(sct: Struct?, title: String?, pageContext: PageContext?, maxlevel: Int, dp: DumpProperties?): DumpData? {
        return StructUtil.toDumpTable(sct, title, pageContext, maxlevel, dp)
    }

    // FUTURE add to interface
    fun toCredentials(username: String?, password: String?): Credentials? {
        return CredentialsImpl.toCredentials(username, password)
    }

    companion object {
        private var singelton: CastImpl? = null
        val instance: Cast?
            get() {
                if (singelton == null) singelton = CastImpl()
                return singelton
            }
    }
}