package tachyon.transformer

import tachyon.runtime.op.Caster

abstract class FactoryBase : Factory() {
    @Override
    override fun createLiteral(obj: Object?, defaultValue: Literal?): Literal? {
        if (obj is Boolean) return createLitBoolean((obj as Boolean?).booleanValue())
        if (obj is Number) {
            return if (obj is Integer) createLitInteger((obj as Integer?).intValue()) else if (obj is Long) createLitLong((obj as Long?).longValue()) else createLitNumber(obj as Number?)
        }
        val str: String = Caster.toString(obj, null)
        return str?.let { createLitString(it) } ?: defaultValue
    }
}