/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.  If not, see <http:></http:>//www.gnu.org/licenses/>.
 *
 */
package lucee.transformer.bytecode.cast

import org.objectweb.asm.Type

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

    /**
     * @see lucee.transformer.expression.Expression.writeOut
     */
    @Override
    @Throws(TransformerException::class)
    fun _writeOut(bc: BytecodeContext?, mode: Int): Type? {
        val adapter: GeneratorAdapter = bc.getAdapter()
        if (expr is ExprNumber) {
            expr.writeOut(bc, mode)
            if (mode == MODE_VALUE) adapter.invokeStatic(Types.CASTER, Methods.METHOD_TO_BOOLEAN_VALUE_FROM_DOUBLE_VALUE) else adapter.invokeStatic(Types.CASTER, Methods.METHOD_TO_BOOLEAN_FROM_NUMBER)
        } else if (expr is ExprString) {
            expr.writeOut(bc, MODE_REF)
            if (mode == MODE_VALUE) adapter.invokeStatic(Types.CASTER, Methods.METHOD_TO_BOOLEAN_VALUE_FROM_STRING) else adapter.invokeStatic(Types.CASTER, Methods.METHOD_TO_BOOLEAN_FROM_STRING)
        } else {
            val rtn: Type = (expr as ExpressionBase?).writeOutAsType(bc, mode)
            if (mode == MODE_VALUE) {
                if (!Types.isPrimitiveType(rtn)) {
                    adapter.invokeStatic(Types.CASTER, Methods.METHOD_TO_BOOLEAN_VALUE)
                } else if (Types.BOOLEAN_VALUE.equals(rtn)) {
                } else if (Types.DOUBLE_VALUE.equals(rtn)) {
                    adapter.invokeStatic(Types.CASTER, Methods.METHOD_TO_BOOLEAN_VALUE_FROM_DOUBLE_VALUE)
                } else {
                    adapter.invokeStatic(Types.CASTER, Method("toRef", Types.toRefType(rtn), arrayOf<Type?>(rtn)))
                    adapter.invokeStatic(Types.CASTER, Methods.METHOD_TO_BOOLEAN_VALUE)
                }
                // return Types.BOOLEAN_VALUE;
            } else {
                if (Types.BOOLEAN.equals(rtn)) {
                } else adapter.invokeStatic(Types.CASTER, Methods.METHOD_TO_BOOLEAN)
            }
        }
        return if (mode == MODE_VALUE) Types.BOOLEAN_VALUE else Types.BOOLEAN
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