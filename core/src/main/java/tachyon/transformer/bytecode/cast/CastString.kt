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

import org.objectweb.asm.Type

/**
 * Cast to a String
 */
class CastString private constructor(expr: Expression?) : ExpressionBase(expr.getFactory(), expr.getStart(), expr.getEnd()), ExprString, Cast {
    private val expr: Expression?

    /**
     * @see tachyon.transformer.expression.Expression._writeOut
     */
    @Override
    @Throws(TransformerException::class)
    fun _writeOut(bc: BytecodeContext?, mode: Int): Type? {
        val adapter: GeneratorAdapter = bc.getAdapter()
        if (expr is ExprBoolean) {
            expr.writeOut(bc, MODE_VALUE)
            adapter.invokeStatic(Types.CASTER, Methods.METHOD_TO_STRING_FROM_BOOLEAN)
        } else if (expr is ExprNumber) {
            expr.writeOut(bc, MODE_REF)
            adapter.invokeStatic(Types.CASTER, Methods.METHOD_TO_STRING_FROM_NUMBER)
        } else {
            val rtn: Type = (expr as ExpressionBase?).writeOutAsType(bc, MODE_REF)
            if (rtn.equals(Types.STRING)) return Types.STRING
            adapter.invokeStatic(Types.CASTER, Methods.METHOD_TO_STRING)
        }
        return Types.STRING
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