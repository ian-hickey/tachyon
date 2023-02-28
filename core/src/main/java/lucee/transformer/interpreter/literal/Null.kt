package lucee.transformer.interpreter.literal

import lucee.runtime.type.scope.Scope

class Null(f: Factory?, start: Position?, end: Position?) : ExpressionBase(f, start, end) {
    @Override
    fun _writeOut(ic: InterpreterContext?, mode: Int): Class<*>? {
        ic.stack(null as Object?)
        return Object::class.java
    }

    fun toVariable(): Variable? {
        val v: Variable = getFactory().createVariable(Scope.SCOPE_UNDEFINED, getStart(), getEnd())
        v.addMember(getFactory().createDataMember(getFactory().createLitString("null")))
        return v
    }
}