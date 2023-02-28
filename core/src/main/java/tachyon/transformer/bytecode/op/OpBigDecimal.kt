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

class OpBigDecimal(left: Expression?, right: Expression?, operation: Int) : ExpressionBase(left.getFactory(), left.getStart(), right.getEnd()) {
    private val operation: Int
    private val left: Expression?
    private val right: Expression?

    /**
     *
     * @see tachyon.transformer.bytecode.expression.ExpressionBase._writeOut
     */
    @Override
    @Throws(TransformerException::class)
    fun _writeOut(bc: BytecodeContext?, mode: Int): Type? {
        return writeOutNumber(bc, mode)
    }

    @Throws(TransformerException::class)
    fun writeOutNumber(bc: BytecodeContext?, mode: Int): Type? {
        if (operation == Factory.OP_DBL_EXP) {
            return OpNumber(left, right, operation).writeOutNumber(bc, mode)
        }
        val adapter: GeneratorAdapter = bc.getAdapter()
        toBigDecimal(bc, left)
        toBigDecimal(bc, right)

        // Caster.toBigDecimal("1").add(Caster.toBigDecimal("1"));
        if (operation == Factory.OP_DBL_PLUS) {
            adapter.invokeVirtual(Types.BIG_DECIMAL, _ADD)
        } else if (operation == Factory.OP_DBL_MINUS) {
            adapter.invokeVirtual(Types.BIG_DECIMAL, _SUBSTRACT)
        } else if (operation == Factory.OP_DBL_DIVIDE) {
            adapter.push(34)
            adapter.push(BigDecimal.ROUND_HALF_EVEN)
            adapter.invokeVirtual(Types.BIG_DECIMAL, _DIVIDE)
        } else if (operation == Factory.OP_DBL_INTDIV) {
            adapter.push(0)
            adapter.push(BigDecimal.ROUND_DOWN)
            adapter.invokeVirtual(Types.BIG_DECIMAL, _DIVIDE)
        } else if (operation == Factory.OP_DBL_MULTIPLY) {
            adapter.invokeVirtual(Types.BIG_DECIMAL, _MULTIPLY)
        } else if (operation == Factory.OP_DBL_MODULUS) {
            adapter.invokeVirtual(Types.BIG_DECIMAL, _REMAINER)
        }
        return Types.BIG_DECIMAL
    }

    fun getLeft(): Expression? {
        return left
    }

    fun getRight(): Expression? {
        return right
    }

    companion object {
        private val TO_BIG_DECIMAL: Method? = Method("toBigDecimal", Types.BIG_DECIMAL, arrayOf<Type?>(Types.OBJECT))
        private val _ADD: Method? = Method("add", Types.BIG_DECIMAL, arrayOf<Type?>(Types.BIG_DECIMAL))
        private val _SUBSTRACT: Method? = Method("subtract", Types.BIG_DECIMAL, arrayOf<Type?>(Types.BIG_DECIMAL))
        private val _DIVIDE: Method? = Method("divide", Types.BIG_DECIMAL, arrayOf<Type?>(Types.BIG_DECIMAL, Types.INT_VALUE, Types.INT_VALUE))
        private val _MULTIPLY: Method? = Method("multiply", Types.BIG_DECIMAL, arrayOf<Type?>(Types.BIG_DECIMAL))
        private val _REMAINER: Method? = Method("remainder", Types.BIG_DECIMAL, arrayOf<Type?>(Types.BIG_DECIMAL))
        @Throws(TransformerException::class)
        private fun toBigDecimal(bc: BytecodeContext?, expr: Expression?) {
            expr.writeOut(bc, MODE_REF)
            if (expr is OpBigDecimal) return
            bc.getAdapter().invokeStatic(Types.CASTER, TO_BIG_DECIMAL)
        }
    }

    init {
        this.left = left
        this.right = right
        this.operation = operation
    }
}