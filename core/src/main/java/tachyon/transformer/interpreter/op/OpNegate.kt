package tachyon.transformer.interpreter.op

import tachyon.runtime.exp.PageException

class OpNegate private constructor(expr: Expression?, start: Position?, end: Position?) : ExpressionBase(expr.getFactory(), start, end), ExprBoolean {
    private val expr: ExprBoolean?
    @Override
    @Throws(PageException::class)
    fun _writeOut(ic: InterpreterContext?, mode: Int): Class<*>? {
        if (mode == MODE_VALUE) {
            ic.stack(!ic.getValueAsBooleanValue(expr))
            return Boolean::class.javaPrimitiveType
        }
        ic.stack(if (ic.getValueAsBooleanValue(expr)) Boolean.FALSE else Boolean.TRUE)
        return Boolean::class.java
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
        fun toExprBoolean(expr: Expression?, start: Position?, end: Position?): ExprBoolean? {
            if (expr is Literal) {
                val b: Boolean = (expr as Literal?).getBoolean(null)
                if (b != null) {
                    return expr.getFactory().createLitBoolean(!b.booleanValue(), start, end)
                }
            }
            return OpNegate(expr, start, end)
        }
    }

    init {
        this.expr = expr.getFactory().toExprBoolean(expr)
    }
}