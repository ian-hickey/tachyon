package tachyon.runtime.util

import tachyon.runtime.Component

/**
 * creates a Java Proxy for components, so you can use componets as java classes following a certain
 * interface or class
 */
interface JavaProxyUtil {
    fun call(config: ConfigWeb?, cfc: Component?, methodName: String?, vararg arguments: Object?): Object?
    fun toBoolean(obj: Object?): Boolean
    fun toFloat(obj: Object?): Float
    fun toInt(obj: Object?): Int
    fun toDouble(obj: Object?): Double
    fun toLong(obj: Object?): Long
    fun toChar(obj: Object?): Char
    fun toByte(obj: Object?): Byte
    fun toShort(obj: Object?): Short
    fun toString(obj: Object?): String?
    fun to(obj: Object?, clazz: Class<*>?): Object?
    fun to(obj: Object?, className: String?): Object?
    fun toCFML(value: Boolean): Object?
    fun toCFML(value: Byte): Object?
    fun toCFML(value: Char): Object?
    fun toCFML(value: Double): Object?
    fun toCFML(value: Float): Object?
    fun toCFML(value: Int): Object?
    fun toCFML(value: Long): Object?
    fun toCFML(value: Short): Object?
    fun toCFML(value: Object?): Object?
}