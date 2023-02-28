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

class DecisionDoubleVisitor {
    private var operation = 0
    fun visitBegin() {}
    fun visitMiddle(operation: Int) {
        this.operation = operation
    }

    fun visitGT() {
        operation = GT
    }

    fun visitGTE() {
        operation = GTE
    }

    fun visitLT() {
        operation = LT
    }

    fun visitLTE() {
        operation = LTE
    }

    fun visitEQ() {
        operation = EQ
    }

    fun visitNEQ() {
        operation = NEQ
    }

    fun visitEnd(bc: BytecodeContext?) {
        val adapter: GeneratorAdapter = bc.getAdapter()
        val l1 = Label()
        val l2 = Label()
        adapter.visitInsn(Opcodes.DCMPL)
        adapter.visitJumpInsn(operation, l1)
        adapter.visitInsn(Opcodes.ICONST_1)
        adapter.visitJumpInsn(Opcodes.GOTO, l2)
        adapter.visitLabel(l1)
        adapter.visitInsn(Opcodes.ICONST_0)
        adapter.visitLabel(l2)
    }

    companion object {
        /*
	 * public static final int GT=Opcodes.IF_ICMPGT; public static final int GTE=Opcodes.IF_ICMPGE;
	 * public static final int LT=Opcodes.IF_ICMPLT; public static final int LTE=Opcodes.IF_ICMPLE;
	 * public static final int EQ=Opcodes.IF_ICMPEQ; public static final int NEQ=Opcodes.IF_ICMPNE;
	 */
        val GT: Int = Opcodes.IFLE
        val GTE: Int = Opcodes.IFLT
        val LT: Int = Opcodes.IFGE
        val LTE: Int = Opcodes.IFGT
        val EQ: Int = Opcodes.IFNE
        val NEQ: Int = Opcodes.IFEQ
    }
}