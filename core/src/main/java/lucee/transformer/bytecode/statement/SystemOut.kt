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
package lucee.transformer.bytecode.statement

import java.io.PrintStream

class SystemOut(expr: Expression?, start: Position?, end: Position?) : StatementBaseNoFinal(expr.getFactory(), start, end) {
    var expr: Expression?

    /**
     * @see lucee.transformer.bytecode.statement.StatementBase._writeOut
     */
    @Override
    @Throws(TransformerException::class)
    override fun _writeOut(bc: BytecodeContext?) {
        val adapter: GeneratorAdapter = bc.getAdapter()
        adapter.getStatic(Type.getType(System::class.java), "out", Type.getType(PrintStream::class.java))
        expr.writeOut(bc, Expression.MODE_REF)
        adapter.invokeVirtual(Type.getType(PrintStream::class.java), METHOD_PRINTLN)
    }

    companion object {
        // void println (Object)
        private val METHOD_PRINTLN: Method? = Method("println", Types.VOID, arrayOf<Type?>(Types.OBJECT))
    }

    /**
     * constructor of the class
     *
     * @param expr
     * @param line
     */
    init {
        this.expr = expr
    }
}