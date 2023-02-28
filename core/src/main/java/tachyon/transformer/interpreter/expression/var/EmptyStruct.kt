package tachyon.transformer.interpreter.expression.`var`

import tachyon.runtime.type.Struct

class EmptyStruct(factory: Factory?) : ExpressionBase(factory, null, null) {
    fun _writeOut(ic: InterpreterContext?, mode: Int): Class<*>? {
        ic.stack(StructImpl())
        return Struct::class.java
    }
}