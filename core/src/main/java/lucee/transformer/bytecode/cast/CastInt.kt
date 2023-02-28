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
 * cast an Expression to a Double
 */
class CastInt private constructor(expr: Expression?) : ExpressionBase(expr.getFactory(), expr.getStart(), expr.getEnd()), ExprInt, Cast {
    private val expr: Expression?

    /**
     * @see lucee.transformer.expression.Expression._writeOut
     */
    @Override
    @Throws(TransformerException::class)
    fun _writeOut(bc: BytecodeContext?, mode: Int): Type? {
        val adapter: GeneratorAdapter = bc.getAdapter()
        if (expr is ExprString) {
            expr.writeOut(bc, MODE_REF)
            if (mode == MODE_VALUE) adapter.invokeStatic(Types.CASTER, Methods.METHOD_TO_INT_VALUE_FROM_STRING) else adapter.invokeStatic(Types.CASTER, Methods.METHOD_TO_INTEGER_FROM_STRING)
        } else {
            val rtn: Type = (expr as ExpressionBase?).writeOutAsType(bc, mode)
            if (mode == MODE_VALUE) {
                if (!Types.isPrimitiveType(rtn)) {
                    adapter.invokeStatic(Types.CASTER, Methods.METHOD_TO_INT_VALUE)
                } else if (Types.BOOLEAN_VALUE.equals(rtn)) {
                    adapter.invokeStatic(Types.CASTER, Methods.METHOD_TO_INT_VALUE_FROM_BOOLEAN_VALUE)
                } else if (Types.SHORT_VALUE.equals(rtn)) {
                    // No Cast needed
                } else if (Types.FLOAT_VALUE.equals(rtn)) {
                    adapter.cast(Types.FLOAT_VALUE, Types.INT_VALUE)
                } else if (Types.LONG_VALUE.equals(rtn)) {
                    adapter.cast(Types.LONG_VALUE, Types.INT_VALUE)
                } else if (Types.DOUBLE_VALUE.equals(rtn)) {
                    adapter.cast(Types.DOUBLE_VALUE, Types.INT_VALUE)
                } else if (Types.INT_VALUE.equals(rtn)) {
                    // No Cast needed
                } else {
                    adapter.invokeStatic(Types.CASTER, Method("toRef", Types.toRefType(rtn), arrayOf<Type?>(rtn)))
                    adapter.invokeStatic(Types.CASTER, Methods.METHOD_TO_INT_VALUE)
                }
                return Types.INT_VALUE
            } else if (Types.isPrimitiveType(rtn)) {
                if (Types.DOUBLE_VALUE.equals(rtn)) {
                    adapter.invokeStatic(Types.CASTER, Methods.METHOD_TO_INTEGER_FROM_DOUBLE_VALUE)
                } else if (Types.BOOLEAN_VALUE.equals(rtn)) {
                    adapter.invokeStatic(Types.CASTER, Methods.METHOD_TO_INTEGER_FROM_BOOLEAN_VALUE)
                } else {
                    adapter.invokeStatic(Types.CASTER, Method("toRef", Types.toRefType(rtn), arrayOf<Type?>(rtn)))
                    adapter.invokeStatic(Types.CASTER, Methods.METHOD_TO_INTEGER)
                }
                return Types.INTEGER
            }
            if (!Types.INTEGER.equals(rtn)) adapter.invokeStatic(Types.CASTER, Methods.METHOD_TO_INTEGER)
            return Types.INTEGER
        }
        return if (mode == MODE_VALUE) Types.INT_VALUE else Types.INTEGER
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