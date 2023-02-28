package lucee.transformer.interpreter.literal

import lucee.runtime.config.NullSupportHelper

class Empty(f: Factory?, start: Position?, end: Position?) : ExpressionBase(f, start, end) {
    @Override
    fun _writeOut(ic: InterpreterContext?, mode: Int): Class<*>? {
        ic.stack(NullSupportHelper.empty(ic.getPageContext()))
        return Object::class.java
    }
}