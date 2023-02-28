package tachyon.transformer.interpreter.literal

import tachyon.runtime.op.Caster

/**
 * Literal Double Value
 */
class LitLongImpl
/**
 * constructor of the class
 *
 * @param d
 * @param line
 */(f: Factory?, private val l: Long, start: Position?, end: Position?) : ExpressionBase(f, start, end), LitLong {
    @Override
    fun _writeOut(ic: InterpreterContext?, mode: Int): Class<*>? {
        if (mode == MODE_REF) {
            ic.stack(Long.valueOf(l))
            return Long::class.java
        }
        ic.stack(l)
        return Long::class.javaPrimitiveType
    }

    @Override
    fun getLongValue(): Long {
        return l
    }

    @Override
    fun getLong(): Long? {
        return Long.valueOf(l)
    }

    @Override
    fun getNumber(): Number? {
        return getLong()
    }

    @Override
    fun getNumber(dv: Number?): Number? {
        return getLong()
    }

    @Override
    fun getString(): String? {
        return Caster.toString(l)
    }

    /**
     * @return return value as a Boolean Object
     */
    fun getBoolean(): Boolean? {
        return Caster.toBoolean(l)
    }

    /**
     * @return return value as a boolean value
     */
    fun getBooleanValue(): Boolean {
        return Caster.toBooleanValue(l)
    }

    @Override
    fun getBoolean(defaultValue: Boolean?): Boolean? {
        return getBoolean()
    }
}