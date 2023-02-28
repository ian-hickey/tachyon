/**
 * Copyright (c) 2023, TachyonCFML.org
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
package tachyon.transformer.bytecode.visitor

import org.objectweb.asm.Label

class WhileVisitor : LoopVisitor {
    private var begin: Label? = null
    private var end: Label? = null
    fun visitBeforeExpression(bc: BytecodeContext?) {
        begin = Label()
        end = Label()
        bc.getAdapter().visitLabel(begin)
    }

    fun visitAfterExpressionBeforeBody(bc: BytecodeContext?) {
        bc.getAdapter().ifZCmp(Opcodes.IFEQ, end)
    }

    fun visitAfterBody(bc: BytecodeContext?, endline: Position?) {
        bc.getAdapter().visitJumpInsn(Opcodes.GOTO, begin)
        bc.getAdapter().visitLabel(end)
        ExpressionUtil.visitLine(bc, endline)
    }

    /**
     *
     * @see tachyon.transformer.bytecode.visitor.LoopVisitor.visitContinue
     */
    @Override
    override fun visitContinue(bc: BytecodeContext?) {
        bc.getAdapter().visitJumpInsn(Opcodes.GOTO, begin)
    }

    /**
     *
     * @see tachyon.transformer.bytecode.visitor.LoopVisitor.visitBreak
     */
    @Override
    override fun visitBreak(bc: BytecodeContext?) {
        bc.getAdapter().visitJumpInsn(Opcodes.GOTO, end)
    }

    /**
     *
     * @see tachyon.transformer.bytecode.visitor.LoopVisitor.getContinueLabel
     */
    @Override
    override fun getContinueLabel(): Label? {
        return begin
    }

    /**
     *
     * @see tachyon.transformer.bytecode.visitor.LoopVisitor.getBreakLabel
     */
    @Override
    override fun getBreakLabel(): Label? {
        return end
    }
}