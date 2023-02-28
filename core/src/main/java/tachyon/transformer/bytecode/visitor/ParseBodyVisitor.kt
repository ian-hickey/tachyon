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

import org.objectweb.asm.Type

class ParseBodyVisitor {
    private var tfv: TryFinallyVisitor? = null
    fun visitBegin(bc: BytecodeContext?) {
        val adapter: GeneratorAdapter = bc.getAdapter()
        tfv = TryFinallyVisitor(object : OnFinally() {
            @Override
            override fun _writeOut(bc: BytecodeContext?) {
                // ExpressionUtil.visitLine(bc, line);
                bc.getAdapter().loadArg(0)
                bc.getAdapter().invokeVirtual(Types.PAGE_CONTEXT, OUTPUT_END)
            }
        }, null)

        // ExpressionUtil.visitLine(bc, line);
        adapter.loadArg(0)
        adapter.invokeVirtual(Types.PAGE_CONTEXT, OUTPUT_START)
        tfv.visitTryBegin(bc)
    }

    @Throws(TransformerException::class)
    fun visitEnd(bc: BytecodeContext?) {
        tfv!!.visitTryEnd(bc)
    }

    companion object {
        // void outputStart()
        val OUTPUT_START: Method? = Method("outputStart", Types.VOID, arrayOf<Type?>())

        // void outputEnd()
        val OUTPUT_END: Method? = Method("outputEnd", Types.VOID, arrayOf<Type?>())
    }
}