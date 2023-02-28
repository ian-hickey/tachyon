package lucee.commons.lang.types

import java.util.Date

class RefStringImpl(@set:Override
                    @get:Override var value: String) : RefString, Castable {
    @Override
    fun castToBoolean(defaultValue: Boolean?): Boolean {
        return Caster.toBoolean(value, defaultValue)
    }

    @Override
    fun castToBooleanValue(): Boolean {
        return try {
            Caster.toBooleanValue(value)
        } catch (pe: PageException) {
            throw PageRuntimeException(pe)
        }
    }

    @Override
    @Throws(PageException::class)
    fun castToDateTime(): DateTime {
        return Caster.toDatetime(value, null)
    }

    @Override
    fun castToDateTime(defaultValue: DateTime?): DateTime {
        return Caster.toDate(value, false, null, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun castToDoubleValue(): Double {
        return Caster.toDoubleValue(value)
    }

    @Override
    fun castToDoubleValue(defaultValue: Double): Double {
        return Caster.toDoubleValue(value, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun castToString(): String {
        return value
    }

    @Override
    fun castToString(defaultValue: String?): String {
        return value
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(other: String?): Int {
        return OpUtil.compare(ThreadLocalPageContext.get(), castToString(), other)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(other: Boolean): Int {
        return OpUtil.compare(ThreadLocalPageContext.get(), if (castToBooleanValue()) Boolean.TRUE else Boolean.FALSE, if (other) Boolean.TRUE else Boolean.FALSE)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(other: Double): Int {
        return OpUtil.compare(ThreadLocalPageContext.get(), Double.valueOf(castToDoubleValue()), Double.valueOf(other))
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(other: DateTime?): Int {
        return OpUtil.compare(ThreadLocalPageContext.get(), castToDateTime() as Date, other as Date?)
    }
}