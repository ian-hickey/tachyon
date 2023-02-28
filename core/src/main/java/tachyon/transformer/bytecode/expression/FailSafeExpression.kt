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
package tachyon.transformer.bytecode.expression

import org.objectweb.asm.Label

class FailSafeExpression(expr: Expression?, defaultValue: Expression?) : ExpressionBase(expr.getFactory(), expr.getStart(), expr.getEnd()), Opcodes {
    private val expr: Expression?
    private val defaultValue: Expression?

    @Override
    @Throws(TransformerException::class)
    override fun _writeOut(bc: BytecodeContext?, mode: Int): Type? {
        val mv: GeneratorAdapter = bc.getAdapter()
        val local: Int = mv.newLocal(Types.OBJECT)
        run {
            val begin = Label()
            val onSuccess = Label()
            val onFail = Label()
            val end = Label()
            mv.visitTryCatchBlock(begin, onSuccess, onFail, "java/lang/Throwable")
            mv.visitLabel(begin)
            expr.writeOut(bc, MODE_REF)
            mv.storeLocal(local)
            mv.visitLabel(onSuccess)
            mv.visitJumpInsn(GOTO, end)
            mv.visitLabel(onFail)
            // mv.visitVarInsn(ASTORE, 2);
            defaultValue.writeOut(bc, MODE_REF)
            mv.storeLocal(local)
            mv.visitLabel(end)
            mv.loadLocal(local)
        }
        return Types.OBJECT
    }

    init {
        this.expr = expr
        this.defaultValue = defaultValue
    }
}