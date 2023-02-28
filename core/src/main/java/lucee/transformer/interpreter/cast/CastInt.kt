package lucee.transformer.interpreter.cast

import lucee.runtime.exp.PageException

/**
 * cast an Expression to a Double
 */
class CastInt private constructor(expr: Expression?) : ExpressionBase(expr.getFactory(), expr.getStart(), expr.getEnd()), ExprInt, Cast {
    private val expr: Expression?
    @Override
    @Throws(PageException::class)
    fun _writeOut(ic: InterpreterContext?, mode: Int): Class<*>? {
        if (mode == MODE_VALUE) {
            ic.stack(ic.getValueAsIntValue(expr))
            return Int::class.javaPrimitiveType
        }
        ic.stack(ic.getValueAsInteger(expr))
        return Integer::class.java
    }

    @Override
    fun getExpr(): Expression? {
        return expr
    }

    companion object {
        /**
         * Create a String expression from an Expression
         *
         * @param expr
         * @return String expression
         * @throws TemplateException
         */
        fun toExprInt(expr: Expression?): ExprInt? {
            if (expr is ExprInt) return expr as ExprInt?
            if (expr is Literal) {
                val n: Number = (expr as Literal?).getNumber(null)
                if (n != null) return expr.getFactory().createLitInteger(n.intValue(), expr.getStart(), expr.getEnd())
            }
            return CastInt(expr)
        }
    }

    init {
        this.expr = expr
    }
}