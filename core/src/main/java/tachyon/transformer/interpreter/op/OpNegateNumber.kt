package tachyon.transformer.interpreter.op

import java.math.BigDecimal

class OpNegateNumber private constructor(expr: Expression?, start: Position?, end: Position?) : ExpressionBase(expr.getFactory(), start, end), ExprNumber {
    private val expr: ExprNumber?
    @Override
    @Throws(PageException::class)
    fun _writeOut(ic: InterpreterContext?, mode: Int): Class<*>? {
        if (mode == MODE_VALUE) {
            ic.stack(-ic.getValueAsDouble(expr))
            return Double::class.javaPrimitiveType
        }
        ic.stack(Double.valueOf(-ic.getValueAsDouble(expr)))
        return Double::class.java
    }

    companion object {
        /**
         * Create a String expression from an Expression
         *
         * @param left
         * @param right
         *
         * @return String expression
         * @throws TemplateException
         */
        fun toExprNumber(expr: Expression?, start: Position?, end: Position?): ExprNumber? {
            if (expr is Literal) {
                val n: Number = (expr as Literal?).getNumber(null)
                if (n != null) {
                    return if (n is BigDecimal) expr.getFactory().createLitNumber((n as BigDecimal).negate(), start, end) else expr.getFactory().createLitNumber(BigDecimal.valueOf(-n.doubleValue()), start, end)
                }
            }
            return OpNegateNumber(expr, start, end)
        }

        fun toExprNumber(expr: Expression?, operation: Int, start: Position?, end: Position?): ExprNumber? {
            return if (operation == Factory.OP_NEG_NBR_MINUS) toExprNumber(expr, start, end) else expr.getFactory().toExprNumber(expr)
        }
    }

    init {
        this.expr = expr.getFactory().toExprNumber(expr)
    }
}