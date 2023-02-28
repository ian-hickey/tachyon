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

import org.objectweb.asm.Label

class OpContional private constructor(cont: Expression?, left: Expression?, right: Expression?) : ExpressionBase(left.getFactory(), left.getStart(), right.getEnd()) {
    private val cont: ExprBoolean?
    private val left: Expression?
    private val right: Expression?

    /**
     *
     * @see tachyon.transformer.bytecode.expression.ExpressionBase._writeOut
     */
    @Override
    @Throws(TransformerException::class)
    fun _writeOut(bc: BytecodeContext?, mode: Int): Type? {
        val adapter: GeneratorAdapter = bc.getAdapter()
        val yes = Label()
        val end = Label()

        // cont
        ExpressionUtil.visitLine(bc, cont.getStart())
        cont.writeOut(bc, MODE_VALUE)
        ExpressionUtil.visitLine(bc, cont.getEnd())
        adapter.visitJumpInsn(Opcodes.IFEQ, yes)

        // left
        ExpressionUtil.visitLine(bc, left.getStart())
        left.writeOut(bc, MODE_REF)
        ExpressionUtil.visitLine(bc, left.getEnd())
        adapter.visitJumpInsn(Opcodes.GOTO, end)

        // right
        ExpressionUtil.visitLine(bc, right.getStart())
        adapter.visitLabel(yes)
        right.writeOut(bc, MODE_REF)
        ExpressionUtil.visitLine(bc, right.getEnd())
        adapter.visitLabel(end)
        return Types.OBJECT
    }

    companion object {
        fun toExpr(cont: Expression?, left: Expression?, right: Expression?): Expression? {
            return OpContional(cont, left, right)
        } /*
	 * *
	 * 
	 * @see tachyon.transformer.bytecode.expression.Expression#getType() / public int getType() { return
	 * Types._BOOLEAN; }
	 */
    }

    init {
        this.cont = left.getFactory().toExprBoolean(cont)
        this.left = left
        this.right = right
    }
}