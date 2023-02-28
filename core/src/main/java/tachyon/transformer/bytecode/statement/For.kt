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

class For(f: Factory?, init: Expression?, condition: Expression?, update: Expression?, body: Body?, start: Position?, end: Position?, label: String?) : StatementBaseNoFinal(f, start, end), FlowControlBreak, FlowControlContinue, HasBody {
    private val init: Expression?
    private val condition: Expression?
    private val update: Expression?
    private val body: Body?

    // private static final int I=1;
    var beforeUpdate: Label? = Label()
    var end: Label? = Label()
    private val label: String?

    @Override
    @Throws(TransformerException::class)
    override fun _writeOut(bc: BytecodeContext?) {
        val adapter: GeneratorAdapter = bc.getAdapter()
        val beforeInit = Label()
        val afterInit = Label()
        val afterUpdate = Label()
        ExpressionUtil.visitLine(bc, getStart())
        adapter.visitLabel(beforeInit)
        if (init != null) {
            init.writeOut(bc, Expression.MODE_VALUE)
            adapter.pop()
        }
        adapter.visitJumpInsn(Opcodes.GOTO, afterUpdate)
        adapter.visitLabel(afterInit)
        body.writeOut(bc)
        adapter.visitLabel(beforeUpdate)
        // ExpressionUtil.visitLine(bc, getStartLine());
        if (update != null) {
            update.writeOut(bc, Expression.MODE_VALUE)
            ASMUtil.pop(adapter, update, Expression.MODE_VALUE)
        }
        // ExpressionUtil.visitLine(bc, getStartLine());
        adapter.visitLabel(afterUpdate)
        if (condition != null) condition.writeOut(bc, Expression.MODE_VALUE) else bc.getFactory().TRUE().writeOut(bc, Expression.MODE_VALUE)
        adapter.visitJumpInsn(Opcodes.IFNE, afterInit)
        // ExpressionUtil.visitLine(bc, getEndLine());
        adapter.visitLabel(end)
    }

    @Override
    override fun getBreakLabel(): Label? {
        return end
    }

    @Override
    override fun getContinueLabel(): Label? {
        return beforeUpdate
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
     * @param init
     * @param condition
     * @param update
     * @param body
     * @param line
     */
    init {
        this.init = init
        this.condition = condition
        this.update = update
        this.body = body
        this.label = label
        body.setParent(this)
    }
}