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
package lucee.transformer.bytecode.statement.tag

import org.objectweb.asm.Label

class TagWhile(f: Factory?, start: Position?, end: Position?) : TagBaseNoFinal(f, start, end), FlowControlBreak, FlowControlContinue {
    private var wv: WhileVisitor? = null
    private var label: String? = null

    /**
     * @see lucee.transformer.bytecode.statement.StatementBase._writeOut
     */
    @Override
    @Throws(TransformerException::class)
    override fun _writeOut(bc: BytecodeContext?) {
        wv = WhileVisitor()
        wv.visitBeforeExpression(bc)
        getAttribute("condition")!!.getValue()!!.writeOut(bc, Expression.MODE_VALUE)
        wv.visitAfterExpressionBeforeBody(bc)
        getBody().writeOut(bc)
        wv.visitAfterBody(bc, getEnd())
    }

    /**
     * @see lucee.transformer.bytecode.statement.FlowControl.getBreakLabel
     */
    @Override
    fun getBreakLabel(): Label? {
        return wv.getBreakLabel()
    }

    /**
     * @see lucee.transformer.bytecode.statement.FlowControl.getContinueLabel
     */
    @Override
    fun getContinueLabel(): Label? {
        return wv.getContinueLabel()
    }

    @Override
    fun getLabel(): String? {
        return label
    }

    fun setLabel(label: String?) {
        this.label = label
    }
}