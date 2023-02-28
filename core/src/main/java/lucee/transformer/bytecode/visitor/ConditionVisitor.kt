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

class ConditionVisitor {
    private var end: Label? = null
    private var endIf: Label? = null
    fun visitBefore() {
        end = Label()
    }

    fun visitWhenBeforeExpr() {}
    fun visitWhenAfterExprBeforeBody(bc: BytecodeContext?) {
        endIf = Label()
        bc.getAdapter().ifZCmp(Opcodes.IFEQ, endIf)
    }

    fun visitWhenAfterBody(bc: BytecodeContext?) {
        bc.getAdapter().visitJumpInsn(Opcodes.GOTO, end)
        bc.getAdapter().visitLabel(endIf)
    }

    fun visitOtherviseBeforeBody() {}
    fun visitOtherviseAfterBody() {}
    fun visitAfter(bc: BytecodeContext?) {
        bc.getAdapter().visitLabel(end)
    }
}