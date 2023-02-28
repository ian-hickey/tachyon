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

import org.objectweb.asm.Label

class DoWhile(expr: Expression?, body: Body?, start: Position?, end: Position?, label: String?) : StatementBaseNoFinal(expr.getFactory(), start, end), FlowControlBreak, FlowControlContinue, HasBody {
    private val expr: ExprBoolean?
    private val body: Body?
    private val begin: Label? = Label()
    private val beforeEnd: Label? = Label()
    private val end: Label? = Label()
    private val label: String?

    @Override
    @Throws(TransformerException::class)
    override fun _writeOut(bc: BytecodeContext?) {
        val adapter: GeneratorAdapter = bc.getAdapter()
        adapter.visitLabel(begin)
        body.writeOut(bc)
        adapter.visitLabel(beforeEnd)
        expr.writeOut(bc, Expression.MODE_VALUE)
        adapter.ifZCmp(Opcodes.IFNE, begin)
        adapter.visitLabel(end)
    }

    @Override
    override fun getBreakLabel(): Label? {
        return end
    }

    @Override
    override fun getContinueLabel(): Label? {
        return beforeEnd
    }

    @Override
    override fun getBody(): Body? {
        return body
    }

    @Override
    override fun getLabel(): String? {
        return label
    }

    /**
     * Constructor of the class
     *
     * @param expr
     * @param body
     * @param line
     */
    init {
        this.expr = expr.getFactory().toExprBoolean(expr)
        this.body = body
        body.setParent(this)
        this.label = label
    }
}