package lucee.transformer.interpreter.op

import lucee.runtime.exp.PageException

class OpContional private constructor(cont: Expression?, left: Expression?, right: Expression?) : ExpressionBase(left.getFactory(), left.getStart(), right.getEnd()) {
    private val cont: ExprBoolean?
    private val left: Expression?
    private val right: Expression?
    @Override
    @Throws(PageException::class)
    fun _writeOut(ic: InterpreterContext?, mode: Int): Class<*>? {
        ic.stack(if (ic.getValueAsBooleanValue(cont)) left else right)
        return Object::class.java
    }

    companion object {
        fun toExpr(cont: Expression?, left: Expression?, right: Expression?): Expression? {
            return OpContional(cont, left, right)
        }
    }

    init {
        this.cont = left.getFactory().toExprBoolean(cont)
        this.left = left
        this.right = right
    }
}