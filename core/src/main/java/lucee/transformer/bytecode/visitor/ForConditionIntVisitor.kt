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

@Deprecated
@Deprecated("replaced with ForIntVisitor")
class ForConditionIntVisitor : Opcodes, LoopVisitor {
    private var l0: Label? = null
    private var l1: Label? = null
    private var l2: Label? = null
    private var l3: Label? = null
    private var i = 0
    private var lend: Label? = null
    private var lbegin: Label? = null
    fun visitBegin(adapter: GeneratorAdapter?, start: Int, isLocal: Boolean): Int {
        lend = Label()
        lbegin = Label()
        i = adapter.newLocal(Types.INT_VALUE)
        l0 = Label()
        adapter.visitLabel(l0)
        if (isLocal) adapter.loadLocal(start, Types.INT_VALUE) else adapter.push(start)
        // mv.visitInsn(ICONST_1);
        adapter.visitVarInsn(ISTORE, i)
        l1 = Label()
        adapter.visitLabel(l1)
        l2 = Label()
        adapter.visitJumpInsn(GOTO, l2)
        l3 = Label()
        adapter.visitLabel(l3)
        return i
    }

    fun visitEndBeforeCondition(bc: BytecodeContext?, step: Int, isLocal: Boolean, startline: Position?) {
        val adapter: GeneratorAdapter = bc.getAdapter()
        adapter.visitLabel(lbegin)
        if (isLocal) {
            adapter.visitVarInsn(ILOAD, i)
            // adapter.loadLocal(i);
            adapter.loadLocal(step)
            adapter.visitInsn(IADD)
            // adapter.dup();
            adapter.visitVarInsn(ISTORE, i)
        } else adapter.visitIincInsn(i, step)
        ExpressionUtil.visitLine(bc, startline)
        adapter.visitLabel(l2)
    }

    fun visitEndAfterCondition(bc: BytecodeContext?) {
        val adapter: GeneratorAdapter = bc.getAdapter()
        adapter.ifZCmp(Opcodes.IFNE, l3)
        adapter.visitLabel(lend)
        adapter.visitLocalVariable("i", "I", null, l1, lend, i)
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