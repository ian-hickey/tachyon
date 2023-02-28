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
package lucee.transformer.bytecode.literal

import org.objectweb.asm.Label

class NullConstant(f: Factory?, start: Position?, end: Position?) : ExpressionBase(f, start, end) {
    @Override
    @Throws(TransformerException::class)
    fun _writeOut(bc: BytecodeContext?, mode: Int): Type? {
        val a: GeneratorAdapter = bc.getAdapter()

        // public static boolean full(PageContext pc)
        a.loadArg(0)
        bc.getAdapter().invokeStatic(Types.NULL_SUPPORT_HELPER, FULL)
        val beforeNull = Label()
        val beforeGet = Label()
        val end = Label()
        a.visitJumpInsn(Opcodes.IFNE, beforeNull)
        a.visitLabel(beforeGet)
        a.loadArg(0)
        a.invokeVirtual(Types.PAGE_CONTEXT, Page.UNDEFINED_SCOPE)
        a.getStatic(Types.KEY_CONSTANTS, "_NULL", Types.COLLECTION_KEY)
        a.invokeInterface(Types.UNDEFINED, GET)
        a.visitJumpInsn(Opcodes.GOTO, end)
        a.visitLabel(beforeNull)
        ASMConstants.NULL(bc.getAdapter())
        a.visitLabel(end)
        return Types.OBJECT
    }

    fun toVariable(): Variable? {
        val v: Variable = getFactory().createVariable(Scope.SCOPE_UNDEFINED, getStart(), getEnd())
        v.addMember(getFactory().createDataMember(getFactory().createLitString("null")))
        return v
    }

    companion object {
        private val FULL: Method? = Method("full", Types.BOOLEAN_VALUE, arrayOf<Type?>(Types.PAGE_CONTEXT))
        private val GET: Method? = Method("get", Types.OBJECT, arrayOf<Type?>(Types.COLLECTION_KEY))
    }
}