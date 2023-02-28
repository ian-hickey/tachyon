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
package lucee.transformer.bytecode.op

import org.objectweb.asm.Label

class OpBool private constructor(left: Expression?, right: Expression?, operation: Int) : ExpressionBase(left.getFactory(), left.getStart(), right.getEnd()), ExprBoolean {
    private val left: ExprBoolean?
    private val right: ExprBoolean?
    private val operation: Int

    /**
     *
     * @see lucee.transformer.bytecode.expression.ExpressionBase._writeOut
     */
    @Override
    @Throws(TransformerException::class)
    fun _writeOut(bc: BytecodeContext?, mode: Int): Type? {
        val adapter: GeneratorAdapter = bc.getAdapter()
        if (mode == MODE_REF) {
            _writeOut(bc, MODE_VALUE)
            adapter.invokeStatic(Types.CASTER, Methods.METHOD_TO_BOOLEAN_FROM_BOOLEAN)
            return Types.BOOLEAN
        }
        val doFalse = Label()
        val end = Label()
        if (operation == Factory.OP_BOOL_AND) {
            left.writeOut(bc, MODE_VALUE)
            adapter.ifZCmp(Opcodes.IFEQ, doFalse)
            right.writeOut(bc, MODE_VALUE)
            adapter.ifZCmp(Opcodes.IFEQ, doFalse)
            adapter.push(true)
            adapter.visitJumpInsn(Opcodes.GOTO, end)
            adapter.visitLabel(doFalse)
            adapter.push(false)
            adapter.visitLabel(end)
        }
        if (operation == Factory.OP_BOOL_OR) {
            left.writeOut(bc, MODE_VALUE)
            adapter.ifZCmp(Opcodes.IFNE, doFalse)
            right.writeOut(bc, MODE_VALUE)
            adapter.ifZCmp(Opcodes.IFNE, doFalse)
            adapter.push(false)
            adapter.visitJumpInsn(Opcodes.GOTO, end)
            adapter.visitLabel(doFalse)
            adapter.push(true)
            adapter.visitLabel(end)
        } else if (operation == Factory.OP_BOOL_XOR) {
            left.writeOut(bc, MODE_VALUE)
            right.writeOut(bc, MODE_VALUE)
            adapter.visitInsn(Opcodes.IXOR)
        } else if (operation == Factory.OP_BOOL_EQV) {
            adapter.loadArg(0)
            left.writeOut(bc, MODE_REF)
            right.writeOut(bc, MODE_REF)
            adapter.invokeStatic(Types.OP_UTIL, Methods_Operator.OPERATOR_EQV_PC_B_B)
        } else if (operation == Factory.OP_BOOL_IMP) {
            adapter.loadArg(0)
            left.writeOut(bc, MODE_REF)
            right.writeOut(bc, MODE_REF)
            adapter.invokeStatic(Types.OP_UTIL, Methods_Operator.OPERATOR_IMP_PC_B_B)
        }
        return Types.BOOLEAN_VALUE
    }

    @Override
    override fun toString(): String {
        return left.toString() + " " + toStringOperation() + " " + right
    }

    private fun toStringOperation(): String? {
        if (Factory.OP_BOOL_AND === operation) return "and"
        if (Factory.OP_BOOL_OR === operation) return "or"
        if (Factory.OP_BOOL_XOR === operation) return "xor"
        if (Factory.OP_BOOL_EQV === operation) return "eqv"
        return if (Factory.OP_BOOL_IMP === operation) "imp" else operation.toString() + ""
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
        fun toExprBoolean(left: Expression?, right: Expression?, operation: Int): ExprBoolean? {
            if (left is Literal && right is Literal) {
                val l: Boolean = (left as Literal?).getBoolean(null)
                val r: Boolean = (right as Literal?).getBoolean(null)
                if (l != null && r != null) {
                    when (operation) {
                        Factory.OP_BOOL_AND -> return left.getFactory().createLitBoolean(l.booleanValue() && r.booleanValue(), left.getStart(), right.getEnd())
                        Factory.OP_BOOL_OR -> return left.getFactory().createLitBoolean(l.booleanValue() || r.booleanValue(), left.getStart(), right.getEnd())
                        Factory.OP_BOOL_XOR -> return left.getFactory().createLitBoolean(l.booleanValue() xor r.booleanValue(), left.getStart(), right.getEnd())
                    }
                }
            }
            return OpBool(left, right, operation)
        }
    }

    init {
        this.left = left.getFactory().toExprBoolean(left)
        this.right = left.getFactory().toExprBoolean(right)
        this.operation = operation
    }
}