package lucee.transformer.interpreter.cast

import java.math.BigDecimal

/**
 * cast an Expression to a Double
 */
class CastNumber private constructor(expr: Expression?) : ExpressionBase(expr.getFactory(), expr.getStart(), expr.getEnd()), ExprNumber, Cast {
    private val expr: Expression?
    @Override
    @Throws(PageException::class)
    fun _writeOut(ic: InterpreterContext?, mode: Int): Class<*>? {
        ic.stack(ic.getValueAsNumber(expr))
        return Number::class.java
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
        fun toExprNumber(expr: Expression?): ExprNumber? {
            if (expr is ExprNumber) return expr as ExprNumber?
            if (expr is Literal) {
                val n: Number = (expr as Literal?).getNumber(null)
                if (n != null) {
                    return if (n is BigDecimal) expr.getFactory().createLitNumber(n as BigDecimal, expr.getStart(), expr.getEnd()) else expr.getFactory().createLitNumber(BigDecimal.valueOf(n.doubleValue()), expr.getStart(), expr.getEnd())
                }
            }
            return CastNumber(expr)
        }
    }

    init {
        this.expr = expr
    }
}