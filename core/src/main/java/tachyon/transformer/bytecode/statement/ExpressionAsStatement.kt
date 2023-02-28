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
package tachyon.transformer.bytecode.statement

import org.objectweb.asm.Type

class ExpressionAsStatement(expr: Expression?) : StatementBaseNoFinal(expr.getFactory(), expr.getStart(), expr.getEnd()) {
    private val expr: ExpressionBase?

    /**
     *
     * @see tachyon.transformer.bytecode.statement.StatementBase._writeOut
     */
    @Override
    @Throws(TransformerException::class)
    override fun _writeOut(bc: BytecodeContext?) {
        val adapter: GeneratorAdapter = bc.getAdapter()
        val rtn: Int = bc.getReturn()
        // set rtn
        if (rtn > -1) {
            val type: Type = expr.writeOutAsType(bc, Expression.MODE_REF)
            bc.getAdapter().storeLocal(rtn)
        } else {
            if (expr !is Literal) {
                val type: Type = expr.writeOutAsType(bc, Expression.MODE_VALUE)
                if (!type.equals(Types.VOID)) {
                    ASMUtil.pop(adapter, type)
                }
            }
        }
    }

    /**
     * @return the expr
     */
    fun getExpr(): Expression? {
        return expr
    }

    /**
     * Constructor of the class
     *
     * @param expr
     */
    init {
        this.expr = expr as ExpressionBase?
    }
}