package tachyon.transformer.interpreter.cast

import tachyon.runtime.exp.PageException

/**
 * Cast to a Boolean
 */
class CastBoolean private constructor(expr: Expression?) : ExpressionBase(expr.getFactory(), expr.getStart(), expr.getEnd()), ExprBoolean, Cast {
    /**
     * @see java.lang.Object.toString
     */
    @Override
    override fun toString(): String {
        return "(boolean)$expr"
    }

    private val expr: Expression?
    @Override
    @Throws(PageException::class)
    fun _writeOut(ic: InterpreterContext?, mode: Int): Class<*>? {
        if (mode == MODE_VALUE) {
            ic.stack(ic.getValueAsBooleanValue(expr))
            return Boolean::class.javaPrimitiveType
        }
        ic.stack(ic.getValueAsBoolean(expr))
        return Boolean::class.java
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
        fun toExprBoolean(expr: Expression?): ExprBoolean? {
            if (expr is ExprBoolean) return expr as ExprBoolean?
            if (expr is Literal) {
                val bool: Boolean = (expr as Literal?).getBoolean(null)
                if (bool != null) return expr.getFactory().createLitBoolean(bool.booleanValue(), expr.getStart(), expr.getEnd())
                // TODO throw new TemplateException("can't cast value to a boolean value");
            }
            return CastBoolean(expr)
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