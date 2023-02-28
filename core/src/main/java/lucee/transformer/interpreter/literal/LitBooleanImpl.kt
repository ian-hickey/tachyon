package lucee.transformer.interpreter.literal

import java.math.BigDecimal

/**
 * Literal Boolean
 */
class LitBooleanImpl
/**
 * constructor of the class
 *
 * @param b
 * @param line
 */(f: Factory?, private val b: Boolean, start: Position?, end: Position?) : ExpressionBase(f, start, end), LitBoolean, ExprBoolean {
    @Override
    fun _writeOut(ic: InterpreterContext?, mode: Int): Class<*>? {
        if (mode == MODE_REF) {
            ic.stack(if (b) Boolean.TRUE else Boolean.FALSE)
            return Boolean::class.java
        }
        ic.stack(b)
        return Boolean::class.javaPrimitiveType
    }

    /**
     * @return return value as double value
     */
    fun getDoubleValue(): Double {
        return Caster.toDoubleValue(b)
    }

    /**
     * @return return value as Double Object
     */
    fun getDouble(): Double? {
        return Caster.toDouble(b)
    }

    /**
     * @see lucee.transformer.expression.literal.Literal.getString
     */
    @Override
    fun getString(): String? {
        return Caster.toString(b)
    }

    /**
     * @return return value as a Boolean Object
     */
    fun getBoolean(): Boolean? {
        return Caster.toBoolean(b)
    }

    /**
     * @return return value as a boolean value
     */
    @Override
    fun getBooleanValue(): Boolean {
        return b
    }

    @Override
    fun getNumber(defaultValue: Number?): Number? {
        return if (AppListenerUtil.getPreciseMath(null, null)) if (b) BigDecimal.ONE else BigDecimal.ZERO else Caster.toDouble(b)
    }

    @Override
    fun getBoolean(defaultValue: Boolean?): Boolean? {
        return getBoolean()
    }

    /**
     * @see java.lang.Object.toString
     */
    @Override
    override fun toString(): String {
        return b.toString() + ""
    }
}