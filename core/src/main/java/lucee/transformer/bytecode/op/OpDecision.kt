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
package lucee.transformer.bytecode.op

import org.objectweb.asm.Label

class OpDecision private constructor(left: Expression?, right: Expression?, operation: Int) : ExpressionBase(left.getFactory(), left.getStart(), right.getEnd()), ExprBoolean {
    /*
	 * public static final int LT=GeneratorAdapter.LT; public static final int LTE=GeneratorAdapter.LE;
	 * public static final int GTE=GeneratorAdapter.GE; public static final int GT=GeneratorAdapter.GT;
	 * public static final int EQ=GeneratorAdapter.EQ; public static final int NEQ=GeneratorAdapter.NE;
	 * public static final int CT = 1000; public static final int NCT = 1001; public static final int
	 * EEQ = 1002; public static final int NEEQ = 1003;
	 */
    private val left: Expression?
    private val right: Expression?
    private val op: Int
    @Override
    @Throws(TransformerException::class)
    fun _writeOut(bc: BytecodeContext?, mode: Int): Type? {
        val adapter: GeneratorAdapter = bc.getAdapter()
        if (mode == MODE_REF) {
            _writeOut(bc, MODE_VALUE)
            adapter.invokeStatic(Types.CASTER, Methods.METHOD_TO_BOOLEAN_FROM_BOOLEAN)
            return Types.BOOLEAN
        }
        if (op == Factory.OP_DEC_CT) {
            adapter.loadArg(0)
            left.writeOut(bc, MODE_REF)
            right.writeOut(bc, MODE_REF)
            adapter.invokeStatic(Types.OP_UTIL, Methods_Operator.OPERATOR_CT_PC_O_O)
        } else if (op == Factory.OP_DEC_NCT) {
            adapter.loadArg(0)
            left.writeOut(bc, MODE_REF)
            right.writeOut(bc, MODE_REF)
            adapter.invokeStatic(Types.OP_UTIL, Methods_Operator.OPERATOR_NCT_PC_O_O)
        } else if (op == Factory.OP_DEC_EEQ) {
            adapter.loadArg(0)
            left.writeOut(bc, MODE_REF)
            right.writeOut(bc, MODE_REF)
            adapter.invokeStatic(Types.OP_UTIL, Methods_Operator.OPERATOR_EEQ_PC_O_O)
        } else if (op == Factory.OP_DEC_NEEQ) {
            adapter.loadArg(0)
            left.writeOut(bc, MODE_REF)
            right.writeOut(bc, MODE_REF)
            adapter.invokeStatic(Types.OP_UTIL, Methods_Operator.OPERATOR_NEEQ_PC_O_O)
        } else {
            adapter.loadArg(0)
            val iLeft: Int = Methods_Operator.getType((left as ExpressionBase?).writeOutAsType(bc, MODE_REF))
            val iRight: Int = Methods_Operator.getType((right as ExpressionBase?).writeOutAsType(bc, MODE_REF))
            adapter.invokeStatic(Types.OP_UTIL, Methods_Operator.COMPARATORS.get(iLeft).get(iRight))
            // adapter.invokeStatic(Types.OPERATOR, Methods_Operator.OPERATORS[iLeft][iRight]);
            adapter.visitInsn(Opcodes.ICONST_0)
            val l1 = Label()
            val l2 = Label()
            adapter.ifCmp(Type.INT_TYPE, toASMOperation(bc, op), l1)
            // adapter.visitJumpInsn(Opcodes.IF_ICMPEQ, l1);
            adapter.visitInsn(Opcodes.ICONST_0)
            adapter.visitJumpInsn(Opcodes.GOTO, l2)
            adapter.visitLabel(l1)
            adapter.visitInsn(Opcodes.ICONST_1)
            adapter.visitLabel(l2)
        }
        return Types.BOOLEAN_VALUE
    }

    @Throws(TransformerException::class)
    private fun toASMOperation(bc: BytecodeContext?, op: Int): Int {
        if (Factory.OP_DEC_LT === op) return GeneratorAdapter.LT
        if (Factory.OP_DEC_LTE === op) return GeneratorAdapter.LE
        if (Factory.OP_DEC_GT === op) return GeneratorAdapter.GT
        if (Factory.OP_DEC_GTE === op) return GeneratorAdapter.GE
        if (Factory.OP_DEC_EQ === op) return GeneratorAdapter.EQ
        if (Factory.OP_DEC_NEQ === op) return GeneratorAdapter.NE
        throw TransformerException(bc, "cannot convert operation [$op] to an ASM Operation", left.getStart())
    }

    fun getLeft(): Expression? {
        return left
    }

    fun getRight(): Expression? {
        return right
    }

    fun getOperation(): Int {
        return op
    }

    companion object {
        // int compare (Object, Object)
        val METHOD_COMPARE: Method? = Method("compare", Types.INT_VALUE, arrayOf<Type?>(Types.OBJECT, Types.OBJECT))

        /**
         * Create a String expression from an operation
         *
         * @param left
         * @param right
         *
         * @return String expression
         */
        fun toExprBoolean(left: Expression?, right: Expression?, operation: Int): ExprBoolean? {
            return OpDecision(left, right, operation)
        }
    }

    init {
        this.left = left
        this.right = right
        op = operation
    }
}