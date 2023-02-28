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
package lucee.transformer.bytecode.visitor

import org.objectweb.asm.Label

// TODO testen wurde noch nicht getestet
class DoWhileVisitor : LoopVisitor {
    private var begin: Label? = null
    private var end: Label? = null
    private var beforeEnd: Label? = null
    fun visitBeginBody(mv: GeneratorAdapter?) {
        end = Label()
        beforeEnd = Label()
        begin = Label()
        mv.visitLabel(begin)
    }

    fun visitEndBodyBeginExpr(mv: GeneratorAdapter?) {
        mv.visitLabel(beforeEnd)
    }

    fun visitEndExpr(mv: GeneratorAdapter?) {
        mv.ifZCmp(Opcodes.IFNE, begin)
        mv.visitLabel(end)
    }

    /**
     * @see lucee.transformer.bytecode.visitor.LoopVisitor.getBreakLabel
     */
    @Override
    override fun getBreakLabel(): Label? {
        return end
    }

    /**
     * @see lucee.transformer.bytecode.visitor.LoopVisitor.getContinueLabel
     */
    @Override
    override fun getContinueLabel(): Label? {
        return beforeEnd
    }

    /**
     *
     * @see lucee.transformer.bytecode.visitor.LoopVisitor.visitContinue
     */
    @Override
    override fun visitContinue(bc: BytecodeContext?) {
        bc.getAdapter().visitJumpInsn(Opcodes.GOTO, getContinueLabel())
    }

    /**
     *
     * @see lucee.transformer.bytecode.visitor.LoopVisitor.visitBreak
     */
    @Override
    override fun visitBreak(bc: BytecodeContext?) {
        bc.getAdapter().visitJumpInsn(Opcodes.GOTO, getBreakLabel())
    }
}