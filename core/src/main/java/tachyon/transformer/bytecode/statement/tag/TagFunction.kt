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

import java.util.HashMap

class TagFunction(f: Factory?, start: Position?, end: Position?) : TagBase(f, start, end), IFunction {
    private var index = 0
    private var function: Function? = null
    @Override
    fun getType(): Int {
        return TYPE_UDF
    }

    @Override
    @Throws(TransformerException::class)
    fun writeOut(bc: BytecodeContext?, type: Int) {
        // ExpressionUtil.visitLine(bc, getStartLine());
        _writeOut(bc, type)
        // ExpressionUtil.visitLine(bc, getEndLine());
    }

    @Override
    @Throws(TransformerException::class)
    override fun _writeOut(bc: BytecodeContext?) {
        _writeOut(bc, IFunction.PAGE_TYPE_REGULAR)
    }

    @Throws(TransformerException::class)
    fun _writeOut(bc: BytecodeContext?, type: Int) {
        if (function == null) {
            function = createFunction(bc, bc.getFactory(), bc.getPage())
            index = bc.getPage().addFunction(function)
            function.setIndex(index)
        }
        function._writeOut(bc, type)
    }

    @Throws(TransformerException::class)
    private fun createFunction(bc: BytecodeContext?, f: Factory?, page: Page?): Function? {

        // private static final Expression EMPTY = LitString.toExprString("");
        val functionBody: Body = BodyBase(f)
        val isStatic: RefBoolean = RefBooleanImpl()
        val func: Function? = _createFunction(bc, page, functionBody, isStatic, page.getOutput())
        // func.setIndex(index);
        func.setParent(getParent())
        val statements: List<Statement?> = getBody().getStatements()
        var stat: Statement
        var tag: Tag

        // suppress WS between cffunction and the last cfargument
        var last: Tag? = null
        if (page.getSupressWSbeforeArg()) {
            // check if there is a cfargument at all
            var it: Iterator<Statement?> = statements.iterator()
            while (it.hasNext()) {
                stat = it.next()
                if (stat is Tag) {
                    tag = stat
                    if (tag!!.getTagLibTag().getTagClassDefinition().isClassNameEqualTo("tachyon.runtime.tag.Argument")) {
                        last = tag
                    }
                }
            }

            // check if there are only literal WS printouts
            if (last != null) {
                it = statements.iterator()
                while (it.hasNext()) {
                    stat = it.next()
                    if (stat === last) break
                    if (stat is PrintOut) {
                        val po: PrintOut = stat as PrintOut
                        val expr: Expression = po.getExpr()
                        if (expr !is LitString || !StringUtil.isWhiteSpace((expr as LitString).getString())) {
                            last = null
                            break
                        }
                    }
                }
            }
        }
        val it: Iterator<Statement?> = statements.iterator()
        var beforeLastArgument = last != null
        while (it.hasNext()) {
            stat = it.next()
            if (beforeLastArgument) {
                if (stat === last) {
                    beforeLastArgument = false
                } else if (stat is PrintOut) {
                    val po: PrintOut = stat as PrintOut
                    val expr: Expression = po.getExpr()
                    if (expr is LitString) {
                        val ls: LitString = expr as LitString
                        if (StringUtil.isWhiteSpace(ls.getString())) continue
                    }
                }
            }
            if (stat is Tag) {
                tag = stat
                if (tag!!.getTagLibTag().getTagClassDefinition().isClassNameEqualTo("tachyon.runtime.tag.Argument")) {
                    addArgument(func, tag)
                    continue
                }
            }
            functionBody.addStatement(stat)
        }
        return func
    }

    private fun addArgument(func: Function?, tag: Tag?) {
        var attr: Attribute
        // name
        val name: Expression = tag!!.removeAttribute("name")!!.getValue()

        // type
        attr = tag!!.removeAttribute("type")
        val type: Expression = if (attr == null) tag.getFactory().createLitString("any") else attr.getValue()

        // required
        attr = tag!!.removeAttribute("required")
        val required: Expression = if (attr == null) tag.getFactory().FALSE() else attr.getValue()

        // default
        attr = tag!!.removeAttribute("default")
        val defaultValue: Expression? = if (attr == null) null else attr.getValue()

        // passby
        attr = tag!!.removeAttribute("passby")
        var passByReference: LitBoolean = tag.getFactory().TRUE()
        if (attr != null) {
            // i can cast irt to LitString because he evulator check this before
            val str: String = (attr.getValue() as LitString).getString()
            if (str.trim().equalsIgnoreCase("value")) passByReference = tag.getFactory().FALSE()
        }

        // displayname
        attr = tag!!.removeAttribute("displayname")
        val displayName: Expression = if (attr == null) tag.getFactory().EMPTY() else attr.getValue()

        // hint
        attr = tag!!.removeAttribute("hint")
        if (attr == null) attr = tag!!.removeAttribute("description")
        val hint: Expression
        hint = if (attr == null) tag.getFactory().EMPTY() else attr.getValue()
        func.addArgument(name, type, required, defaultValue, passByReference, displayName, hint, tag!!.getAttributes())
    }

    @Throws(TransformerException::class)
    private fun _createFunction(bc: BytecodeContext?, page: Page?, body: Body?, isStatic: RefBoolean?, defaultOutput: Boolean): FunctionImpl? {
        var attr: Attribute?
        val ANY: LitString = page.getFactory().createLitString("any")
        val PUBLIC: LitString = page.getFactory().createLitString("public")

        // name
        val name: Expression = removeAttribute("name")!!.getValue()
        /*
		 * if(name instanceof LitString) { ((LitString)name).upperCase(); }
		 */
        // return
        attr = removeAttribute("returntype")
        val returnType: Expression = if (attr == null) ANY else attr.getValue()

        // output
        attr = removeAttribute("output")
        val output: Expression = if (attr == null) if (defaultOutput) page.getFactory().TRUE() else page.getFactory().TRUE() else attr.getValue()

        // bufferOutput
        attr = removeAttribute("bufferoutput")
        val bufferOutput: Expression? = if (attr == null) null else attr.getValue()

        // modifier
        isStatic.setValue(false)
        var modifier: Int = Component.MODIFIER_NONE
        attr = getAttribute("modifier")
        if (attr != null) {
            val `val`: Expression = attr.getValue()
            if (`val` is Literal) {
                val l: Literal = `val` as Literal
                val str: String = StringUtil.emptyIfNull(l.getString()).trim()
                if ("abstract".equalsIgnoreCase(str)) modifier = Component.MODIFIER_ABSTRACT else if ("final".equalsIgnoreCase(str)) modifier = Component.MODIFIER_FINAL else if ("static".equalsIgnoreCase(str)) isStatic.setValue(true)
            }
        }

        // access
        attr = removeAttribute("access")
        val access: Expression = if (attr == null) PUBLIC else attr.getValue()

        // dspLabel
        attr = removeAttribute("displayname")
        val displayname: Expression = if (attr == null) page.getFactory().EMPTY() else attr.getValue()

        // hint
        attr = removeAttribute("hint")
        val hint: Expression = if (attr == null) page.getFactory().EMPTY() else attr.getValue()

        // description
        attr = removeAttribute("description")
        val description: Expression = if (attr == null) page.getFactory().EMPTY() else attr.getValue()

        // returnformat
        attr = removeAttribute("returnformat")
        val returnFormat: Expression? = if (attr == null) null else attr.getValue()

        // secureJson
        attr = removeAttribute("securejson")
        val secureJson: Expression? = if (attr == null) null else attr.getValue()

        // verifyClient
        attr = removeAttribute("verifyclient")
        val verifyClient: Expression? = if (attr == null) null else attr.getValue()

        // localMode
        attr = removeAttribute("localmode")
        val localMode: Expression? = if (attr == null) null else attr.getValue()

        // cachedWithin
        var cachedWithin: Literal? = null
        attr = getAttribute("cachedwithin")
        if (attr != null) {
            cachedWithin = ASMUtil.cachedWithinValue(attr.getValue(), null)
        }
        val strAccess: String = (access as LitString).getString()
        val acc: Int = ComponentUtil.toIntAccess(strAccess, -1)
        if (acc == -1) throw TransformerException(bc, "invalid access type [$strAccess], access types are remote, public, package, private", getStart())
        val func = FunctionImpl(name, returnType, returnFormat, output, bufferOutput, acc, displayname, description, hint, secureJson, verifyClient, localMode,
                cachedWithin, modifier, body, getStart(), getEnd())
        val attrs: Map<String?, Attribute?> = getAttributes()
        val it: Iterator<Entry<String?, Attribute?>?> = attrs.entrySet().iterator()
        val metadatas: HashMap<String?, Attribute?> = HashMap<String?, Attribute?>()
        while (it.hasNext()) {
            attr = it.next().getValue()
            metadatas.put(attr.getName(), attr)
        }
        func.setMetaData(metadatas)
        return func
    }

    @Override
    fun getFlowControlFinal(): FlowControlFinal? {
        return null
    }

    /*
	 * @Override public void setIndex(int index) { this.index = index; }
	 */
    @Throws(TransformerException::class)
    fun register(factory: Factory?, page: Page?) {
        function = createFunction(null, factory, page)
        index = page.addFunction(function)
        function.setIndex(index)
    }
}