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

class TryFinallyVisitor(onFinally: OnFinally?, fcf: FlowControlFinal?) : Opcodes {
    private var beforeTry: Label? = null
    private var afterTry: Label? = null
    private var beforeFinally: Label? = null
    private var afterFinally: Label? = null
    private var lThrow = 0
    private val onFinally: OnFinally?
    private val fcf: FlowControlFinal?
    fun visitTryBegin(bc: BytecodeContext?) {
        val ga: GeneratorAdapter = bc.getAdapter()
        bc.pushOnFinally(onFinally)
        beforeTry = Label()
        afterTry = Label()
        beforeFinally = Label()
        afterFinally = Label()
        ga.visitLabel(beforeTry)
    }

    @Throws(TransformerException::class)
    fun visitTryEnd(bc: BytecodeContext?) {
        val ga: GeneratorAdapter = bc.getAdapter()
        bc.popOnFinally()
        ga.visitJumpInsn(GOTO, beforeFinally)
        ga.visitLabel(afterTry)
        lThrow = ga.newLocal(Types.THROWABLE)
        ga.storeLocal(lThrow)
        onFinally!!.writeOut(bc)
        ga.loadLocal(lThrow)
        ga.visitInsn(ATHROW)
        ga.visitLabel(beforeFinally)
        onFinally!!.writeOut(bc)
        if (fcf != null && fcf.getAfterFinalGOTOLabel() != null) {
            val _end = Label()
            ga.visitJumpInsn(Opcodes.GOTO, _end) // ignore when coming not from break/continue
            ASMUtil.visitLabel(ga, fcf.getFinalEntryLabel())
            onFinally!!.writeOut(bc)
            ga.visitJumpInsn(Opcodes.GOTO, fcf.getAfterFinalGOTOLabel())
            ga.visitLabel(_end)
        }
        ga.visitLabel(afterFinally)
        ga.visitTryCatchBlock(beforeTry, afterTry, afterTry, null)
    }

    init {
        this.onFinally = onFinally
        this.fcf = fcf
    }
}