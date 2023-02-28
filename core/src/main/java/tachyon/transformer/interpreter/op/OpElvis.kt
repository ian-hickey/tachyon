package tachyon.transformer.interpreter.op

import tachyon.transformer.TransformerException

class OpElvis private constructor(left: Variable?, right: Expression?) : ExpressionBase(left.getFactory(), left.getStart(), right.getEnd()) {
    private val left: Variable?
    private val right: Expression?
    @Override
    @Throws(TransformerException::class)
    fun _writeOut(ic: InterpreterContext?, mode: Int): Class<*>? {
        try {
            ic.stack(left.writeOut(ic, mode))
        } catch (e: Exception) {
            ic.stack(right.writeOut(ic, mode))
        }
        return Object::class.java
    }

    companion object {
        fun toExpr(left: Variable?, right: Expression?): Expression? {
            return OpElvis(left, right)
        }
    }

    init {
        this.left = left
        this.right = right
    }
}