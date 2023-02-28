package tachyon.transformer.interpreter.literal

import tachyon.runtime.listener.AppListenerUtil

/**
 * A Literal String
 */
class LitStringImpl
/**
 * constructor of the class
 *
 * @param str
 * @param line
 */(f: Factory?, private var str: String?, start: Position?, end: Position?) : ExpressionBase(f, start, end), LitString, ExprString {
    private var fromBracket = false
    @Override
    fun _writeOut(ic: InterpreterContext?, mode: Int): Class<*>? {
        ic.stack(str)
        return String::class.java
    }

    @Override
    fun getString(): String? {
        return str
    }

    @Override
    fun getNumber(defaultValue: Number?): Number? {
        val res: Number
        res = if (AppListenerUtil.getPreciseMath(null, null)) Caster.toBigDecimal(str, null) else Caster.toDouble(getString(), null)
        return res ?: defaultValue
    }

    @Override
    fun getBoolean(defaultValue: Boolean?): Boolean? {
        return Caster.toBoolean(getString(), defaultValue)
    }

    @Override
    override fun equals(obj: Object?): Boolean {
        if (this === obj) return true
        return if (obj !is LitString) false else str!!.equals((obj as LitStringImpl?)!!.getString())
    }

    @Override
    override fun toString(): String {
        return str!!
    }

    @Override
    fun upperCase() {
        str = str.toUpperCase()
    }

    fun lowerCase() {
        str = str.toLowerCase()
    }

    @Override
    fun duplicate(): LitString? {
        return LitStringImpl(getFactory(), str, getStart(), getEnd())
    }

    @Override
    fun fromBracket(fromBracket: Boolean) {
        this.fromBracket = fromBracket
    }

    @Override
    fun fromBracket(): Boolean {
        return fromBracket
    }
}