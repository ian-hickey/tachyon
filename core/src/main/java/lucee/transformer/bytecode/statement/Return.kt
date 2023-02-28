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
package lucee.transformer.bytecode.statement

import java.util.Stack

/**
 * Return Statement
 */
class Return : StatementBaseNoFinal {
    var expr: Expression? = null

    /**
     * Constructor of the class
     *
     * @param line
     */
    constructor(f: Factory?, start: Position?, end: Position?) : super(f, start, end) {
        setHasFlowController(true)
        // expr=LitString.toExprString("", line);
    }

    /**
     * Constructor of the class
     *
     * @param expr
     * @param line
     */
    constructor(expr: Expression?, start: Position?, end: Position?) : super(expr.getFactory(), start, end) {
        this.expr = expr
        setHasFlowController(true)
        // if(expr==null)expr=LitString.toExprString("", line);
    }

    @Override
    @Throws(TransformerException::class)
    override fun _writeOut(bc: BytecodeContext?) {
        val adapter: GeneratorAdapter = bc.getAdapter()
        if (expr == null) ASMConstants.NULL(adapter) else expr.writeOut(bc, Expression.MODE_REF)
        val finallies: Stack<OnFinally?> = bc.getOnFinallyStack()
        val len: Int = finallies.size()
        var onFinally: OnFinally
        if (len > 0) {
            val rtn: Int = adapter.newLocal(Types.OBJECT)
            adapter.storeLocal(rtn, Types.OBJECT)
            for (i in len - 1 downTo 0) {
                onFinally = finallies.get(i)
                if (!bc.insideFinally(onFinally)) onFinally.writeOut(bc)
            }
            adapter.loadLocal(rtn, Types.OBJECT)
        }
        if (bc.getMethod().getReturnType().equals(Types.VOID)) {
            adapter.pop()
            adapter.visitInsn(Opcodes.RETURN)
        } else adapter.visitInsn(Opcodes.ARETURN)
    }

    /**
     *
     * @see lucee.transformer.bytecode.statement.StatementBase.setParent
     */
    @Override
    override fun setParent(parent: Statement?) {
        super.setParent(parent)
        parent.setHasFlowController(true)
    }
}