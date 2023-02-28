package lucee.runtime.op

import lucee.runtime.Component

class JavaProxyUtilImpl : JavaProxyUtil {
    @Override
    fun call(config: ConfigWeb?, cfc: Component?, methodName: String?, vararg arguments: Object?): Object? {
        return JavaProxy.call(config, cfc, methodName, arguments)
    }

    // FUTURE add to interface
    fun call(config: ConfigWeb?, udf: UDF?, methodName: String?, vararg arguments: Object?): Object? {
        return JavaProxy.call(config, udf, methodName, arguments)
    }

    @Override
    fun toBoolean(obj: Object?): Boolean {
        return JavaProxy.toBoolean(obj)
    }

    @Override
    fun toFloat(obj: Object?): Float {
        return JavaProxy.toFloat(obj)
    }

    @Override
    fun toInt(obj: Object?): Int {
        return JavaProxy.toInt(obj)
    }

    @Override
    fun toDouble(obj: Object?): Double {
        return JavaProxy.toDouble(obj)
    }

    @Override
    fun toLong(obj: Object?): Long {
        return JavaProxy.toLong(obj)
    }

    @Override
    fun toChar(obj: Object?): Char {
        return JavaProxy.toChar(obj)
    }

    @Override
    fun toByte(obj: Object?): Byte {
        return JavaProxy.toByte(obj)
    }

    @Override
    fun toShort(obj: Object?): Short {
        return JavaProxy.toShort(obj)
    }

    @Override
    fun toString(obj: Object?): String? {
        return JavaProxy.toString(obj)
    }

    @Override
    fun to(obj: Object?, clazz: Class<*>?): Object? {
        return JavaProxy.to(obj, clazz)
    }

    @Override
    fun to(obj: Object?, className: String?): Object? {
        return JavaProxy.to(obj, className)
    }

    @Override
    fun toCFML(value: Boolean): Object? {
        return JavaProxy.toCFML(value)
    }

    @Override
    fun toCFML(value: Byte): Object? {
        return JavaProxy.toCFML(value)
    }

    @Override
    fun toCFML(value: Char): Object? {
        return JavaProxy.toCFML(value)
    }

    @Override
    fun toCFML(value: Double): Object? {
        return JavaProxy.toCFML(value)
    }

    @Override
    fun toCFML(value: Float): Object? {
        return JavaProxy.toCFML(value)
    }

    @Override
    fun toCFML(value: Int): Object? {
        return JavaProxy.toCFML(value)
    }

    @Override
    fun toCFML(value: Long): Object? {
        return JavaProxy.toCFML(value)
    }

    @Override
    fun toCFML(value: Short): Object? {
        return JavaProxy.toCFML(value)
    }

    @Override
    fun toCFML(value: Object?): Object? {
        return JavaProxy.toCFML(value)
    }
}