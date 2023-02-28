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

import lucee.runtime.exp.TemplateException

class TagOutput(f: Factory?, start: Position?, end: Position?) : TagGroup(f, start, end) {
    private var type = 0
    fun setType(type: Int) {
        this.type = type
    }

    /**
     *
     * @see lucee.transformer.bytecode.statement.tag.TagBase._writeOut
     */
    @Override
    @Throws(TransformerException::class)
    override fun _writeOut(bc: BytecodeContext?) {
        val old: Boolean
        when (type) {
            TYPE_GROUP -> {
                old = bc.changeDoSubFunctions(false)
                TagGroupUtil.writeOutTypeGroup(this, bc)
                bc.changeDoSubFunctions(old)
            }
            TYPE_INNER_GROUP -> {
                old = bc.changeDoSubFunctions(false)
                TagGroupUtil.writeOutTypeInnerGroup(this, bc)
                bc.changeDoSubFunctions(old)
            }
            TYPE_INNER_QUERY -> {
                old = bc.changeDoSubFunctions(false)
                TagGroupUtil.writeOutTypeInnerQuery(this, bc)
                bc.changeDoSubFunctions(old)
            }
            TYPE_NORMAL -> writeOutTypeNormal(bc)
            TYPE_QUERY -> {
                old = bc.changeDoSubFunctions(false)
                TagGroupUtil.writeOutTypeQuery(this, bc)
                bc.changeDoSubFunctions(old)
            }
            else -> throw TransformerException(bc, "invalid type", getStart())
        }
    }

    /**
     * write out normal query
     *
     * @param adapter
     * @throws TemplateException
     */
    @Throws(TransformerException::class)
    private fun writeOutTypeNormal(bc: BytecodeContext?) {
        val pbv = ParseBodyVisitor()
        pbv.visitBegin(bc)
        getBody().writeOut(bc)
        pbv.visitEnd(bc)
    }

    @Override
    override fun getType(): Short {
        return TAG_OUTPUT
    }

    @Override
    fun getFlowControlFinal(): FlowControlFinal? {
        return null
    }

    companion object {
        const val TYPE_QUERY = 0
        const val TYPE_GROUP = 1
        const val TYPE_INNER_GROUP = 2
        const val TYPE_INNER_QUERY = 3
        const val TYPE_NORMAL = 4
        @Throws(TransformerException::class)
        fun getParentTagOutputQuery(bc: BytecodeContext?, stat: Statement?): TagOutput? {
            val parent: Statement = stat.getParent()
            if (parent == null) throw TransformerException(bc, "there is no parent output with query", null) else if (parent is TagOutput) {
                if ((parent as TagOutput).hasQuery()) return parent
            }
            return getParentTagOutputQuery(bc, parent)
        }
    }
}