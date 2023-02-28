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

class ForDoubleVisitor : Opcodes, LoopVisitor {
    var beforeInit: Label? = Label()
    var beforeExpr: Label? = Label()
    var afterExpr: Label? = Label()
    var beforeBody: Label? = Label()
    var afterBody: Label? = Label()
    var beforeUpdate: Label? = Label()
    var afterUpdate: Label? = Label()
    var i = 0
    fun visitBeforeExpression(adapter: GeneratorAdapter?, start: Int, step: Int, isLocal: Boolean): Int {
        // init
        adapter.visitLabel(beforeInit)
        forInit(adapter, start, isLocal)
        adapter.goTo(beforeExpr)

        // update
        adapter.visitLabel(beforeUpdate)
        forUpdate(adapter, step, isLocal)

        // expression
        adapter.visitLabel(beforeExpr)
        return i
    }

    fun visitAfterExpressionBeginBody(adapter: GeneratorAdapter?) {
        adapter.ifZCmp(Opcodes.IFEQ, afterBody)
    }

    fun visitEndBody(bc: BytecodeContext?, line: Position?) {
        bc.getAdapter().goTo(beforeUpdate)
        ExpressionUtil.visitLine(bc, line)
        bc.getAdapter().visitLabel(afterBody)
        // adapter.visitLocalVariable("i", "I", null, beforeInit, afterBody, i);
    }

    fun forInit(adapter: GeneratorAdapter?, start: Int, isLocal: Boolean) {
        i = adapter.newLocal(Types.DOUBLE_VALUE)
        if (isLocal) adapter.loadLocal(start, Types.DOUBLE_VALUE) else adapter.push(start.toDouble())
        adapter.visitVarInsn(DSTORE, i)
    }

    fun forUpdate(adapter: GeneratorAdapter?, step: Int, isLocal: Boolean) {
        if (isLocal) {
            adapter.visitVarInsn(DLOAD, i)
            adapter.loadLocal(step)
            adapter.visitInsn(DADD)
            adapter.visitVarInsn(DSTORE, i)
        } else adapter.visitIincInsn(i, step)
    }

    /**
     *
     * @see tachyon.transformer.bytecode.visitor.LoopVisitor.visitContinue
     */
    @Override
    override fun visitContinue(bc: BytecodeContext?) {
        bc.getAdapter().visitJumpInsn(Opcodes.GOTO, beforeUpdate)
    }

    /**
     *
     * @see tachyon.transformer.bytecode.visitor.LoopVisitor.visitBreak
     */
    @Override
    override fun visitBreak(bc: BytecodeContext?) {
        bc.getAdapter().visitJumpInsn(Opcodes.GOTO, afterBody)
    }

    /**
     *
     * @see tachyon.transformer.bytecode.visitor.LoopVisitor.getContinueLabel
     */
    @Override
    override fun getContinueLabel(): Label? {
        return beforeUpdate
    }

    /**
     *
     * @see tachyon.transformer.bytecode.visitor.LoopVisitor.getBreakLabel
     */
    @Override
    override fun getBreakLabel(): Label? {
        return afterBody
    }
}