/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Tachyon Assosication Switzerland
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
package tachyon.transformer.bytecode.cast

import java.math.BigDecimal

/**
 * cast an Expression to a Double
 */
class CastNumber private constructor(expr: Expression?) : ExpressionBase(expr.getFactory(), expr.getStart(), expr.getEnd()), ExprNumber, Cast {
    private val expr: Expression?

    /**
     * @see tachyon.transformer.expression.Expression._writeOut
     */
    @Override
    @Throws(TransformerException::class)
    fun _writeOut(bc: BytecodeContext?, mode: Int): Type? {
        val adapter: GeneratorAdapter = bc.getAdapter()
        return if (expr is ExprBoolean) {
            expr.writeOut(bc, MODE_VALUE)
            if (mode == MODE_VALUE) adapter.invokeStatic(Types.CASTER, Methods.METHOD_TO_DOUBLE_VALUE_FROM_BOOLEAN_VALUE) else adapter.invokeStatic(Types.CASTER, Methods.METHOD_TO_NUMBER_FROM_BOOLEAN_VALUE)
            if (mode == MODE_VALUE) Types.DOUBLE_VALUE else Types.NUMBER
        } else if (expr is ExprNumber) {
            expr.writeOut(bc, mode)
            if (mode == MODE_VALUE) Types.DOUBLE_VALUE else Types.NUMBER
        } else if (expr is ExprString) {
            adapter.loadArg(0)
            expr.writeOut(bc, MODE_REF)
            if (mode == MODE_VALUE) adapter.invokeStatic(Types.CASTER, Methods.METHOD_TO_DOUBLE_VALUE_FROM_PC_STRING) else adapter.invokeStatic(Types.CASTER, Methods.METHOD_TO_NUMBER_FROM_PC_STRING)
            if (mode == MODE_VALUE) Types.DOUBLE_VALUE else Types.NUMBER
        } else {
            val rtn: Type = (expr as ExpressionBase?).writeOutAsType(bc, mode)
            if (Types.isPrimitiveType(rtn)) {
                // should never be MODE_REF here, but just to be safe we check anyway
                if (Types.DOUBLE_VALUE.equals(rtn)) {
                    if (mode == MODE_REF) adapter.invokeStatic(Types.CASTER, Methods.METHOD_TO_NUMBER_FROM_DOUBLE_VALUE)
                } else if (Types.BOOLEAN_VALUE.equals(rtn)) {
                    if (mode == MODE_VALUE) adapter.invokeStatic(Types.CASTER, Methods.METHOD_TO_DOUBLE_VALUE_FROM_BOOLEAN_VALUE) else adapter.invokeStatic(Types.CASTER, Methods.METHOD_TO_NUMBER_FROM_BOOLEAN_VALUE)
                } else {
                    adapter.invokeStatic(Types.CASTER, Method("toRef", Types.toRefType(rtn), arrayOf<Type?>(rtn)))
                    if (mode == MODE_VALUE) {
                        adapter.invokeStatic(Types.CASTER, Methods.METHOD_TO_DOUBLE_VALUE)
                    } else {
                        adapter.invokeStatic(Types.CASTER, Methods.METHOD_TO_NUMBER)
                    }
                }
                return if (mode == MODE_VALUE) Types.DOUBLE_VALUE else Types.NUMBER
            }
            if (mode == MODE_VALUE) {
                adapter.invokeStatic(Types.CASTER, Methods.METHOD_TO_DOUBLE_VALUE)
                return Types.DOUBLE_VALUE
            }
            if (Types.DOUBLE.equals(rtn)) return Types.NUMBER
            if (Types.BIG_DECIMAL.equals(rtn)) return Types.NUMBER
            if (Types.FLOAT.equals(rtn)) return Types.NUMBER
            if (Types.LONG.equals(rtn)) return Types.NUMBER
            if (Types.INTEGER.equals(rtn)) return Types.NUMBER
            if (Types.SHORT.equals(rtn)) return Types.NUMBER
            adapter.invokeStatic(Types.CASTER, Methods.METHOD_TO_NUMBER)
            Types.NUMBER
        }
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