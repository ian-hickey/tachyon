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
package lucee.transformer.bytecode.visitor

import org.objectweb.asm.Label

class ForVisitor : Opcodes, LoopVisitor {
    private val l0: Label? = Label()
    private val l1: Label? = Label()
    private val l2: Label? = Label()
    private val l3: Label? = Label()
    private var i = 0
    private val lend: Label? = Label()
    private val lbegin: Label? = Label()
    fun visitBegin(adapter: GeneratorAdapter?, start: Int, isLocal: Boolean): Int {
        adapter.visitLabel(l0)
        forInit(adapter, start, isLocal)
        adapter.visitLabel(l1)
        adapter.visitJumpInsn(GOTO, l2)
        adapter.visitLabel(l3)
        return i
    }

    fun visitEnd(bc: BytecodeContext?, end: Int, isLocal: Boolean, startline: Position?) {
        val adapter: GeneratorAdapter = bc.getAdapter()
        adapter.visitLabel(lbegin)
        forUpdate(adapter)
        ExpressionUtil.visitLine(bc, startline)
        adapter.visitLabel(l2)
        adapter.visitVarInsn(ILOAD, i)
        if (isLocal) adapter.loadLocal(end) else adapter.push(end)
        adapter.visitJumpInsn(IF_ICMPLE, l3)
        adapter.visitLabel(lend)

        // adapter.visitLocalVariable("i", "I", null, l1, lend, i);
    }

    private fun forUpdate(adapter: GeneratorAdapter?) {
        adapter.visitIincInsn(i, 1)
    }

    private fun forInit(adapter: GeneratorAdapter?, start: Int, isLocal: Boolean) {
        i = adapter.newLocal(Types.INT_VALUE)
        if (isLocal) adapter.loadLocal(start) else adapter.push(start)
        adapter.visitVarInsn(ISTORE, i)
    }

    /**
     *
     * @see lucee.transformer.bytecode.visitor.LoopVisitor.visitContinue
     */
    @Override
    override fun visitContinue(bc: BytecodeContext?) {
        bc.getAdapter().visitJumpInsn(Opcodes.GOTO, lbegin)
    }

    /**
     *
     * @see lucee.transformer.bytecode.visitor.LoopVisitor.visitBreak
     */
    @Override
    override fun visitBreak(bc: BytecodeContext?) {
        bc.getAdapter().visitJumpInsn(Opcodes.GOTO, lend)
    }

    /**
     *
     * @see lucee.transformer.bytecode.visitor.LoopVisitor.getContinueLabel
     */
    @Override
    override fun getContinueLabel(): Label? {
        return lbegin
    }

    /**
     *
     * @see lucee.transformer.bytecode.visitor.LoopVisitor.getBreakLabel
     */
    @Override
    override fun getBreakLabel(): Label? {
        return lend
    }
}