package tachyon.transformer.interpreter.expression.`var`

import tachyon.runtime.type.Array

class EmptyArray(factory: Factory?) : ExpressionBase(factory, null, null) {
    fun _writeOut(ic: InterpreterContext?, mode: Int): Class<*>? {
        ic.stack(ArrayImpl())
        return Array::class.java
    }
}