package lucee.transformer.interpreter.op

import lucee.runtime.exp.PageException

class OpString private constructor(left: Expression?, right: Expression?) : ExpressionBase(left.getFactory(), left.getStart(), right.getEnd()), ExprString {
    private val right: ExprString?
    private val left: ExprString?
    @Override
    @Throws(PageException::class)
    fun _writeOut(ic: InterpreterContext?, mode: Int): Class<*>? {
        ic.stack(ic.getValueAsString(left).concat(ic.getValueAsString(right)))
        return String::class.java
    }

    companion object {
        private const val MAX_SIZE = 65535
        fun toExprString(left: Expression?, right: Expression?, concatStatic: Boolean): ExprString? {
            if (concatStatic && left is Literal && right is Literal) {
                val l: String = (left as Literal?).getString()
                val r: String = (right as Literal?).getString()
                if (l.length() + r.length() <= MAX_SIZE) return left.getFactory().createLitString(l.concat(r), left.getStart(), right.getEnd())
            }
            return OpString(left, right)
        }
    }

    init {
        this.left = left.getFactory().toExprString(left)
        this.right = left.getFactory().toExprString(right)
    }
}