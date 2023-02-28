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
package tachyon.transformer.bytecode.statement.tag

import org.objectweb.asm.Label

class TagSilent(f: Factory?, start: Position?, end: Position?) : TagBase(f, start, end) {
    private var fcf: FlowControlFinalImpl? = null

    /**
     *
     * @see tachyon.transformer.bytecode.statement.tag.TagBase._writeOut
     */
    @Override
    @Throws(TransformerException::class)
    override fun _writeOut(bc: BytecodeContext?) {
        val adapter: GeneratorAdapter = bc.getAdapter()
        val silentMode: Int = adapter.newLocal(Types.BOOLEAN_VALUE)

        // boolean silentMode= pc.setSilent();
        adapter.loadArg(0)
        adapter.invokeVirtual(Types.PAGE_CONTEXT, SET_SILENT)
        adapter.storeLocal(silentMode)

        // call must be
        val tfv = TryFinallyVisitor(object : OnFinally() {
            @Override
            fun _writeOut(bc: BytecodeContext?) {
                // if(fcf!=null &&
                // fcf.getAfterFinalGOTOLabel()!=null)ASMUtil.visitLabel(adapter,fcf.getFinalEntryLabel());
                // if(!silentMode)pc.unsetSilent();
                val _if = Label()
                adapter.loadLocal(silentMode)
                NotVisitor.visitNot(bc)
                adapter.ifZCmp(Opcodes.IFEQ, _if)
                adapter.loadArg(0)
                adapter.invokeVirtual(Types.PAGE_CONTEXT, UNSET_SILENT)
                adapter.pop()
                adapter.visitLabel(_if)
                /*
				 * if(fcf!=null) { Label l = fcf.getAfterFinalGOTOLabel();
				 * if(l!=null)adapter.visitJumpInsn(Opcodes.GOTO, l); }
				 */
            }
        }, getFlowControlFinal())
        tfv.visitTryBegin(bc)
        getBody().writeOut(bc)
        tfv.visitTryEnd(bc)
    }

    @Override
    fun getFlowControlFinal(): FlowControlFinal? {
        if (fcf == null) fcf = FlowControlFinalImpl()
        return fcf
    }

    companion object {
        // boolean setSilent()
        private val SET_SILENT: Method? = Method("setSilent", Types.BOOLEAN_VALUE, arrayOf<Type?>())

        // boolean unsetSilent();
        private val UNSET_SILENT: Method? = Method("unsetSilent", Types.BOOLEAN_VALUE, arrayOf<Type?>())
    }
}