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

import java.util.ArrayList

class TagIf(f: Factory?, start: Position?, end: Position?) : TagBaseNoFinal(f, start, end) {
    @Override
    @Throws(TransformerException::class)
    override fun _writeOut(bc: BytecodeContext?) {
        val adapter: GeneratorAdapter = bc.getAdapter()
        val end = Label()
        val tmp: List<Statement?> = ArrayList<Statement?>()
        val it: Iterator<Statement?> = getBody().getStatements().iterator()
        var t: Tag?
        var endIf: Label? = writeOutElseIfStart(bc, this)
        var hasElse = false
        while (it.hasNext()) {
            val stat: Statement? = it.next()
            if (!hasElse && stat is Tag) {
                t = stat
                if (t!!.getTagLibTag().getTagClassDefinition().isClassNameEqualTo("lucee.runtime.tag.ElseIf")) {
                    __writeOut(bc, tmp)
                    writeOutElseIfEnd(adapter, endIf, end)
                    endIf = writeOutElseIfStart(bc, t)
                    continue
                } else if (t!!.getTagLibTag().getTagClassDefinition().isClassNameEqualTo("lucee.runtime.tag.Else")) {
                    __writeOut(bc, tmp)
                    ExpressionUtil.visitLine(bc, t.getStart())
                    hasElse = true
                    writeOutElseIfEnd(adapter, endIf, end)
                    continue
                }
            }
            tmp.add(stat)
            // ExpressionUtil.writeOut(stat, bc);
        }
        __writeOut(bc, tmp)
        if (!hasElse) writeOutElseIfEnd(adapter, endIf, end)
        adapter.visitLabel(end)
    }

    @Throws(TransformerException::class)
    private fun __writeOut(bc: BytecodeContext?, statements: List<Statement?>?) {
        if (statements!!.size() > 0) {
            BodyBase.writeOut(bc, statements)
            statements.clear()
        }
    }

    companion object {
        @Throws(TransformerException::class)
        private fun writeOutElseIfStart(bc: BytecodeContext?, tag: Tag?): Label? {
            val adapter: GeneratorAdapter = bc.getAdapter()
            val cont: ExprBoolean = bc.getFactory().toExprBoolean(tag!!.getAttribute("condition")!!.getValue())
            val endIf = Label()
            ExpressionUtil.visitLine(bc, tag.getStart())
            cont.writeOut(bc, Expression.MODE_VALUE)
            adapter.ifZCmp(Opcodes.IFEQ, endIf)
            return endIf
        }

        private fun writeOutElseIfEnd(adapter: GeneratorAdapter?, endIf: Label?, end: Label?) {
            adapter.visitJumpInsn(Opcodes.GOTO, end)
            adapter.visitLabel(endIf)
        }
    }
}