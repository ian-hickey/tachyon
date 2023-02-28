package tachyon.transformer.interpreter.literal

import tachyon.runtime.op.Caster

/**
 * Literal Double Value
 */
class LitNumberImpl
/**
 * constructor of the class
 *
 * @param d
 * @param line
 */(f: Factory?, // public static final LitDouble ZERO=new LitDouble(0,null,null);
    private val n: Number?, start: Position?, end: Position?) : ExpressionBase(f, start, end), LitNumber, ExprNumber {
    @Override
    fun _writeOut(ic: InterpreterContext?, mode: Int): Class<*>? {
        if (mode == MODE_REF) {
            ic.stack(n)
            return Number::class.java
        }
        ic.stack(n)
        return Double::class.javaPrimitiveType
    }

    @Override
    fun getNumber(): Number? {
        return n
    }

    @Override
    fun getNumber(defaultValue: Number?): Number? {
        return n
    }

    /**
     * @see tachyon.transformer.expression.literal.Literal.getString
     */
    @Override
    fun getString(): String? {
        return Caster.toString(n)
    }

    /**
     * @return return value as a Boolean Object
     */
    fun getBoolean(): Boolean? {
        return Caster.toBoolean(n)
    }

    /**
     * @return return value as a boolean value
     */
    fun getBooleanValue(): Boolean {
        return Caster.toBooleanValue(n)
    }

    @Override
    fun getBoolean(defaultValue: Boolean?): Boolean? {
        return getBoolean()
    }
}