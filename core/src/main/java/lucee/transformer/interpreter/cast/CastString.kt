package lucee.transformer.interpreter.cast

import lucee.runtime.exp.PageException

/**
 * Cast to a String
 */
class CastString private constructor(expr: Expression?) : ExpressionBase(expr.getFactory(), expr.getStart(), expr.getEnd()), ExprString, Cast {
    private val expr: Expression?
    @Override
    @Throws(PageException::class)
    fun _writeOut(ic: InterpreterContext?, mode: Int): Class<*>? {
        ic.stack(ic.getValueAsString(expr))
        return String::class.java
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
         * @param pos
         * @return String expression
         */
        fun toExprString(expr: Expression?): ExprString? {
            if (expr is ExprString) return expr as ExprString?
            return if (expr is Literal) expr.getFactory().createLitString((expr as Literal?).getString(), expr.getStart(), expr.getEnd()) else CastString(expr)
        }
    }

    /**
     * constructor of the class
     *
     * @param expr
     */
    init {
        this.expr = expr
    }
}