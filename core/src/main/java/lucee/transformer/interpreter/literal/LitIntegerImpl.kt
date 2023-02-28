package lucee.transformer.interpreter.literal

import lucee.runtime.op.Caster

/**
 * Literal Double Value
 */
class LitIntegerImpl
/**
 * constructor of the class
 *
 * @param d
 * @param line
 */(f: Factory?, private val i: Int, start: Position?, end: Position?) : ExpressionBase(f, start, end), LitInteger, ExprInt {
    @Override
    fun _writeOut(ic: InterpreterContext?, mode: Int): Class<*>? {
        if (mode == MODE_REF) {
            ic.stack(Integer.valueOf(i))
            return Integer::class.java
        }
        ic.stack(i)
        return Int::class.javaPrimitiveType
    }

    /**
     * @return return value as int
     */
    @Override
    fun geIntValue(): Int {
        return i
    }

    /**
     * @return return value as Double Object
     */
    @Override
    fun getInteger(): Integer? {
        return Integer.valueOf(i)
    }

    @Override
    fun getNumber(): Number? {
        return getInteger()
    }

    @Override
    fun getNumber(dv: Number?): Number? {
        return getInteger()
    }

    /**
     * @see lucee.transformer.expression.literal.Literal.getString
     */
    @Override
    fun getString(): String? {
        return Caster.toString(i)
    }

    /**
     * @return return value as a Boolean Object
     */
    fun getBoolean(): Boolean? {
        return Caster.toBoolean(i)
    }

    /**
     * @return return value as a boolean value
     */
    fun getBooleanValue(): Boolean {
        return Caster.toBooleanValue(i)
    }

    private fun getDouble(): Double? {
        return Double.valueOf(i)
    }

    /**
     * @see lucee.transformer.expression.literal.Literal.getBoolean
     */
    @Override
    fun getBoolean(defaultValue: Boolean?): Boolean? {
        return getBoolean()
    }
}