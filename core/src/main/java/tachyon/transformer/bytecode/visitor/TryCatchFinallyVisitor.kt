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
package tachyon.transformer.bytecode.visitor

import org.objectweb.asm.Label

class TryCatchFinallyVisitor(onFinally: OnFinally?, fcf: FlowControlFinal?) : Opcodes {
    private val onFinally: OnFinally?
    private var beginTry: Label? = null
    private var endTry: Label? = null
    private var endTry2: Label? = null
    private var l3: Label? = null
    private var l4: Label? = null
    private var l5: Label? = null
    private var l6: Label? = null
    private val type: Type? = Types.THROWABLE
    private val fcf: FlowControlFinal?
    fun visitTryBegin(bc: BytecodeContext?) {
        val ga: GeneratorAdapter = bc.getAdapter()
        beginTry = Label()
        endTry = Label()
        endTry2 = Label()
        l3 = Label()
        l4 = Label()
        bc.pushOnFinally(onFinally)
        ga.visitLabel(beginTry)
    }

    fun visitTryEndCatchBeging(bc: BytecodeContext?): Int {
        val ga: GeneratorAdapter = bc.getAdapter()
        ga.visitTryCatchBlock(beginTry, endTry, endTry2, type.getInternalName())
        ga.visitLabel(endTry)
        l5 = Label()
        ga.visitJumpInsn(GOTO, l5)
        ga.visitLabel(endTry2)
        val lThrow: Int = ga.newLocal(type)
        ga.storeLocal(lThrow)
        // mv.visitVarInsn(ASTORE, 1);
        l6 = Label()
        ga.visitLabel(l6)
        return lThrow
    }

    @Throws(TransformerException::class)
    fun visitCatchEnd(bc: BytecodeContext?) {
        val end = Label()
        val ga: GeneratorAdapter = bc.getAdapter()
        bc.popOnFinally()
        ga.visitLabel(l3)
        ga.visitJumpInsn(GOTO, l5)
        ga.visitLabel(l4)
        val lThrow: Int = ga.newLocal(Types.THROWABLE)
        ga.storeLocal(lThrow)
        // mv.visitVarInsn(ASTORE, 2);
        val l8 = Label()
        ga.visitLabel(l8)
        onFinally!!.writeOut(bc)
        ga.loadLocal(lThrow)
        ga.visitInsn(ATHROW)
        ga.visitLabel(l5)
        onFinally!!.writeOut(bc)
        if (fcf != null && fcf.getAfterFinalGOTOLabel() != null) {
            val _end = Label()
            ga.visitJumpInsn(Opcodes.GOTO, _end) // ignore when coming not from break/continue
            ASMUtil.visitLabel(ga, fcf.getFinalEntryLabel())
            onFinally!!.writeOut(bc)
            ga.visitJumpInsn(Opcodes.GOTO, fcf.getAfterFinalGOTOLabel())
            ga.visitLabel(_end)
        }
        ga.visitLabel(end)
        ga.visitTryCatchBlock(beginTry, l3, l4, null)
    }

    init {
        this.onFinally = onFinally
        this.fcf = fcf
    }
}