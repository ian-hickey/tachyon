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
package tachyon.transformer.bytecode.op

import java.math.BigDecimal

class OpNegateNumber private constructor(expr: Expression?, start: Position?, end: Position?) : ExpressionBase(expr.getFactory(), start, end), ExprNumber {
    private val expr: ExprNumber?
    @Override
    @Throws(TransformerException::class)
    fun _writeOut(bc: BytecodeContext?, mode: Int): Type? {
        val adapter: GeneratorAdapter = bc.getAdapter()
        if (mode == MODE_VALUE) {
            expr.writeOut(bc, MODE_VALUE)
            adapter.visitInsn(Opcodes.DNEG)
            return Types.DOUBLE_VALUE
        }
        expr.writeOut(bc, MODE_REF)
        adapter.invokeStatic(Types.CASTER, Methods.METHOD_NEGATE_NUMBER)
        return Types.NUMBER
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

    // public static final int PLUS = 0;
    // public static final int MINUS = 1;
    init {
        this.expr = expr.getFactory().toExprNumber(expr)
    }
}