/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
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

import org.objectweb.asm.Type

class OpNumber internal constructor(left: Expression?, right: Expression?, operation: Int) : ExpressionBase(left.getFactory(), left.getStart(), right.getEnd()), ExprNumber {
    private val op: Int
    private val left: Expression?
    private val right: Expression?
    fun getLeft(): Expression? {
        return left
    }

    fun getRight(): Expression? {
        return right
    }

    fun getOperation(): Int {
        return op
    }

    @Override
    @Throws(TransformerException::class)
    fun _writeOut(bc: BytecodeContext?, mode: Int): Type? {
        return writeOutNumber(bc, mode)
    }

    @Throws(TransformerException::class)
    fun writeOutNumber(bc: BytecodeContext?, mode: Int): Type? {
        val adapter: GeneratorAdapter = bc.getAdapter()
        adapter.loadArg(0)
        left.writeOut(bc, MODE_REF)
        right.writeOut(bc, MODE_REF)
        if (op == Factory.OP_DBL_EXP) {
            if (mode == MODE_VALUE) adapter.invokeStatic(Types.OP_UTIL, EXP) else adapter.invokeStatic(Types.OP_UTIL, EXP_REF)
        } else if (op == Factory.OP_DBL_DIVIDE) {
            if (mode == MODE_VALUE) adapter.invokeStatic(Types.OP_UTIL, DIV) else adapter.invokeStatic(Types.OP_UTIL, DIV_REF)
        } else if (op == Factory.OP_DBL_INTDIV) {
            if (mode == MODE_VALUE) adapter.invokeStatic(Types.OP_UTIL, INTDIV) else adapter.invokeStatic(Types.OP_UTIL, INTDIV_REF)
        } else if (op == Factory.OP_DBL_PLUS) {
            if (mode == MODE_VALUE) adapter.invokeStatic(Types.OP_UTIL, PLUS) else adapter.invokeStatic(Types.OP_UTIL, PLUS_REF)
        } else if (op == Factory.OP_DBL_MINUS) {
            if (mode == MODE_VALUE) adapter.invokeStatic(Types.OP_UTIL, MINUS) else adapter.invokeStatic(Types.OP_UTIL, MINUS_REF)
        } else if (op == Factory.OP_DBL_MODULUS) {
            if (mode == MODE_VALUE) adapter.invokeStatic(Types.OP_UTIL, MODULUS) else adapter.invokeStatic(Types.OP_UTIL, MODULUS_REF)
        } else if (op == Factory.OP_DBL_MULTIPLY) {
            if (mode == MODE_VALUE) adapter.invokeStatic(Types.OP_UTIL, MULTIPLY) else adapter.invokeStatic(Types.OP_UTIL, MULTIPLY_REF)
        }
        return if (mode == MODE_VALUE) Types.DOUBLE_VALUE else Types.NUMBER
    }

    companion object {
        // exponent
        private val EXP_REF: Method? = Method("exponentRef", Types.NUMBER, arrayOf<Type?>(Types.PAGE_CONTEXT, Types.OBJECT, Types.OBJECT))
        private val EXP: Method? = Method("exponent", Types.DOUBLE_VALUE, arrayOf<Type?>(Types.PAGE_CONTEXT, Types.OBJECT, Types.OBJECT))

        // divide
        private val DIV_REF: Method? = Method("divideRef", Types.NUMBER, arrayOf<Type?>(Types.PAGE_CONTEXT, Types.OBJECT, Types.OBJECT))
        private val DIV: Method? = Method("divide", Types.DOUBLE_VALUE, arrayOf<Type?>(Types.PAGE_CONTEXT, Types.OBJECT, Types.OBJECT))

        // divide int
        private val INTDIV_REF: Method? = Method("intdivRef", Types.NUMBER, arrayOf<Type?>(Types.PAGE_CONTEXT, Types.OBJECT, Types.OBJECT))
        private val INTDIV: Method? = Method("intdiv", Types.DOUBLE_VALUE, arrayOf<Type?>(Types.PAGE_CONTEXT, Types.OBJECT, Types.OBJECT))

        // plus
        private val PLUS_REF: Method? = Method("plusRef", Types.NUMBER, arrayOf<Type?>(Types.PAGE_CONTEXT, Types.OBJECT, Types.OBJECT))
        private val PLUS: Method? = Method("plus", Types.DOUBLE_VALUE, arrayOf<Type?>(Types.PAGE_CONTEXT, Types.OBJECT, Types.OBJECT))

        // minus
        private val MINUS_REF: Method? = Method("minusRef", Types.NUMBER, arrayOf<Type?>(Types.PAGE_CONTEXT, Types.OBJECT, Types.OBJECT))
        private val MINUS: Method? = Method("minus", Types.DOUBLE_VALUE, arrayOf<Type?>(Types.PAGE_CONTEXT, Types.OBJECT, Types.OBJECT))

        // modulus
        private val MODULUS_REF: Method? = Method("modulusRef", Types.NUMBER, arrayOf<Type?>(Types.PAGE_CONTEXT, Types.OBJECT, Types.OBJECT))
        private val MODULUS: Method? = Method("modulus", Types.DOUBLE_VALUE, arrayOf<Type?>(Types.PAGE_CONTEXT, Types.OBJECT, Types.OBJECT))

        // multiply
        private val MULTIPLY_REF: Method? = Method("multiplyRef", Types.NUMBER, arrayOf<Type?>(Types.PAGE_CONTEXT, Types.OBJECT, Types.OBJECT))
        private val MULTIPLY: Method? = Method("multiply", Types.DOUBLE_VALUE, arrayOf<Type?>(Types.PAGE_CONTEXT, Types.OBJECT, Types.OBJECT))

        /**
         * Create a String expression from an Expression
         *
         * @param left
         * @param right
         * @param operation
         *
         * @return String expression
         * @throws TemplateException
         */
        fun toExprNumber(left: Expression?, right: Expression?, operation: Int): ExprNumber? {
            return OpNumber(left, right, operation)
        }
    }

    init {
        this.left = left
        this.right = right
        op = operation
    }
}