package tachyon.transformer.interpreter.literal

import tachyon.runtime.config.NullSupportHelper

class NullConstant(f: Factory?, start: Position?, end: Position?) : ExpressionBase(f, start, end) {
    @Override
    @Throws(PageException::class)
    fun _writeOut(ic: InterpreterContext?, mode: Int): Class<*>? {
        if (NullSupportHelper.full(ic.getPageContext())) {
            ic.stack(null as Object?)
        } else {
            ic.stack(ic.getPageContext().undefinedScope().get(KeyConstants._NULL))
        }
        return Object::class.java
    }

    fun toVariable(): Variable? {
        val v: Variable = getFactory().createVariable(Scope.SCOPE_UNDEFINED, getStart(), getEnd())
        v.addMember(getFactory().createDataMember(getFactory().createLitString("null")))
        return v
    }
}