/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
 * Copyright (c) 2016, Lucee Association Switzerland. All rights reserved.
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
package lucee.transformer.bytecode.statement

import org.objectweb.asm.Type

class PrintOut(expr: Expression?, start: Position?, end: Position?) : StatementBaseNoFinal(expr.getFactory(), start, end) {
    var expr: Expression?
    private var checkPSQ = false
    private var encodeFor: Expression? = null

    /**
     * @see lucee.transformer.bytecode.Statement._writeOut
     */
    @Override
    @Throws(TransformerException::class)
    override fun _writeOut(bc: BytecodeContext?) {
        val doEncode = !checkPSQ && encodeFor != null
        val adapter: GeneratorAdapter = bc.getAdapter()
        adapter.loadArg(0)
        if (doEncode) adapter.checkCast(Types.PAGE_CONTEXT_IMPL) // FUTURE keyword:encodefore remove
        val es: ExprString = bc.getFactory().toExprString(expr)
        val usedExternalizer = false
        if (!usedExternalizer) es.writeOut(bc, Expression.MODE_REF)
        if (doEncode) {
            /*
			 * if(encodeForIsInt) { encodeFor.writeOut(bc, Expression.MODE_VALUE);
			 * adapter.visitInsn(Opcodes.I2S);
			 * adapter.invokeVirtual(Types.PAGE_CONTEXT_IMPL,METHOD_WRITE_ENCODE_SHORT); // FUTURE
			 * keyword:encodefore remove _IMPL } else {
			 */
            encodeFor.writeOut(bc, Expression.MODE_REF)
            adapter.invokeVirtual(Types.PAGE_CONTEXT_IMPL, METHOD_WRITE_ENCODE_STRING) // FUTURE keyword:encodefore remove _IMPL
            // }
        } else adapter.invokeVirtual(Types.PAGE_CONTEXT, if (checkPSQ) METHOD_WRITE_PSQ else METHOD_WRITE)
    }

    /**
     * @return the expr
     */
    fun getExpr(): Expression? {
        return expr
    }

    /**
     * @param expr the expr to set
     */
    fun setExpr(expr: Expression?) {
        this.expr = expr
    }

    /**
     * @param preserveSingleQuote the preserveSingleQuote to set
     */
    fun setCheckPSQ(checkPSQ: Boolean) {
        this.checkPSQ = checkPSQ
    }

    fun setEncodeFor(encodeFor: Expression?) {
        this.encodeFor = expr.getFactory().toExprString(encodeFor)
    }

    companion object {
        // void write (String)
        private val METHOD_WRITE: Method? = Method("write", Types.VOID, arrayOf<Type?>(Types.STRING))

        // void writePSQ (Object) TODO muss param 1 wirklich objekt sein
        private val METHOD_WRITE_PSQ: Method? = Method("writePSQ", Types.VOID, arrayOf<Type?>(Types.OBJECT))
        private val METHOD_WRITE_ENCODE_STRING: Method? = Method("writeEncodeFor", Types.VOID, arrayOf<Type?>(Types.STRING, Types.STRING))
    }

    /**
     * constructor of the class
     *
     * @param expr
     * @param line
     */
    init {
        this.expr = expr.getFactory().toExprString(expr)
    }
}