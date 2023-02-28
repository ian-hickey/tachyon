/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Tachyon Assosication Switzerland
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
package tachyon.transformer.bytecode.statement.tag

import java.util.Iterator

class TagSwitch
/**
 * Constructor of the class
 *
 * @param sl
 * @param el
 */
(f: Factory?, start: Position?, end: Position?) : TagBaseNoFinal(f, start, end) {
    /**
     *
     * @see tachyon.transformer.bytecode.statement.tag.TagBase._writeOut
     */
    @Override
    @Throws(TransformerException::class)
    override fun _writeOut(bc: BytecodeContext?) {
        val adapter: GeneratorAdapter = bc.getAdapter()

        // expression
        val expression: Int = adapter.newLocal(Types.STRING)
        getAttribute("expression")!!.getValue()!!.writeOut(bc, Expression.MODE_REF)
        adapter.storeLocal(expression)
        val statements: List = getBody().getStatements()
        var stat: Statement
        var tag: Tag
        val cv = ConditionVisitor()
        cv.visitBefore()

        // cases
        val it: Iterator = statements.iterator()
        var def: Tag? = null
        while (it.hasNext()) {
            stat = it.next() as Statement
            if (stat is Tag) {
                tag = stat
                if (tag!!.getTagLibTag().getTagClassDefinition().isClassNameEqualTo("tachyon.runtime.tag.Case")) {
                    addCase(bc, cv, tag, expression)
                    continue
                } else if (tag!!.getTagLibTag().getTagClassDefinition().isClassNameEqualTo("tachyon.runtime.tag.Defaultcase")) {
                    if (def != null) throw TransformerException(bc, "multiple defaultcases are not allowed", getStart())
                    def = tag
                    // setDefaultCase(bc,cv,tag);
                    // break;
                }
            }
        }

        // default
        if (def != null) setDefaultCase(bc, cv, def)
        cv.visitAfter(bc)
    }

    @Throws(TransformerException::class)
    private fun setDefaultCase(bc: BytecodeContext?, cv: ConditionVisitor?, tag: Tag?) {
        cv.visitOtherviseBeforeBody()
        BodyBase.writeOut(bc, tag!!.getBody())
        // tag.getBody().writeOut(bc);
        cv.visitOtherviseAfterBody()
    }

    @Throws(TransformerException::class)
    private fun addCase(bc: BytecodeContext?, cv: ConditionVisitor?, tag: Tag?, expression: Int) {
        val adapter: GeneratorAdapter = bc.getAdapter()
        cv.visitWhenBeforeExpr()
        val div = DecisionIntVisitor()
        div.visitBegin()
        // List.listFindNoCase(case.value,expression,del);
        tag!!.getAttribute("value")!!.getValue()!!.writeOut(bc, Expression.MODE_REF)
        adapter.loadLocal(expression)
        val attr: Attribute = tag!!.getAttribute("delimiters")
        if (attr != null) attr.getValue()!!.writeOut(bc, Expression.MODE_REF) else adapter.push(",")
        adapter.invokeStatic(Types.LIST_UTIL, LIST_FIND_NO_CASE)
        div.visitNEQ()
        adapter.push(-1)
        div.visitEnd(bc)
        cv.visitWhenAfterExprBeforeBody(bc)
        BodyBase.writeOut(bc, tag!!.getBody())
        // tag.getBody().writeOut(bc);
        cv.visitWhenAfterBody(bc)

        /*
		 * if(List.listFindNoCase(case.value,expression,delimiters)!=-1) { <xsl:apply-templates
		 * select="./body/ *"/> }
		 */
    }

    companion object {
        // int listFindNoCase(String list, String value, String delimiter)
        private val LIST_FIND_NO_CASE: Method? = Method("listFindForSwitch", Types.INT_VALUE, arrayOf<Type?>(Types.STRING, Types.STRING, Types.STRING))
    }
}