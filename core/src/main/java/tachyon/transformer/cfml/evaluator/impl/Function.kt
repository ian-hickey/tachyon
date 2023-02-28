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
package tachyon.transformer.cfml.evaluator.impl

import java.util.Iterator

/**
 * Prueft den Kontext des Tag function. Das Attribute `argument` darf nur direkt
 * innerhalb des Tag `function` liegen. Dem Tag `argument` muss als erstes im
 * tag function vorkommen
 */
class Function : EvaluatorSupport() {
    @Override
    @Throws(EvaluatorException::class)
    fun evaluate(tag: Tag?, libTag: TagLibTag?, flibs: Array<FunctionLib?>?) {
        // Body p=(Body) tag.getParent();
        // Statement pp = p.getParent();
        var isCI = true
        try {
            isCI = ASMUtil.getAncestorPage(null, tag).isComponent() || ASMUtil.getAncestorPage(null, tag).isInterface()
        } catch (e: TransformerException) {
        }
        val attrName: Attribute = tag.getAttribute("name")
        if (attrName != null) {
            val expr: Expression = attrName.getValue()
            var ps: PageSource? = null
            if (expr is LitString && !isCI) {
                val p: Page = ASMUtil.getAncestorPage(tag, null)
                if (p != null) {
                    val sc: SourceCode = p.getSourceCode()
                    if (sc is PageSourceCode) {
                        val psc: PageSourceCode = sc as PageSourceCode
                        ps = psc.getPageSource()
                    }
                }
                checkFunctionName((expr as LitString).getString(), flibs, ps)
            }
        }
        // attribute modifier
        var isStatic = false
        run {
            val attrModifier: Attribute = tag.getAttribute("modifier")
            if (attrModifier != null) {
                val expr: ExprString = tag.getFactory().toExprString(attrModifier.getValue()) as? Literal
                        ?: throw EvaluatorException("Attribute modifier of the Tag Function, must be one of the following literal string values: [abstract,final,static]")
                val modifier: String = StringUtil.emptyIfNull((expr as Literal).getString()).trim()
                if (!StringUtil.isEmpty(modifier) && !"abstract".equalsIgnoreCase(modifier) && !"final".equalsIgnoreCase(modifier) && !"static".equalsIgnoreCase(modifier)) throw EvaluatorException("Attribute modifier of the Tag Function, must be one of the following literal string values: [abstract,final,static]")
                isStatic = "static".equalsIgnoreCase(modifier)
                val abstr: Boolean = "abstract".equalsIgnoreCase(modifier)
                if (abstr) throwIfNotEmpty(tag)
            }
        }

        // cachedWithin
        run {
            val attrCachedWithin: Attribute = tag.getAttribute("cachedwithin")
            if (attrCachedWithin != null) {
                val `val`: Expression = attrCachedWithin.getValue()
                tag.addAttribute(Attribute(attrCachedWithin.isDynamicType(), attrCachedWithin.getName(), ASMUtil.cachedWithinValue(`val`), attrCachedWithin.getType()))
            }
        }

        // Attribute localMode
        run {
            val attrLocalMode: Attribute = tag.getAttribute("localmode")
            if (attrLocalMode != null) {
                val expr: Expression = attrLocalMode.getValue()
                val str: String = ASMUtil.toString(null, expr, null)
                if (!StringUtil.isEmpty(str) && AppListenerUtil.toLocalMode(str, -1) === -1) throw EvaluatorException("Attribute localMode of the Tag Function, must be a literal value (modern, classic, true or false)")
                // boolean output = ((LitBoolean)expr).getBooleanValue();
                // if(!output) ASMUtil.removeLiterlChildren(tag, true);
            }
        }

        // Attribute Output
        run {
            val attrOutput: Attribute = tag.getAttribute("output")
            if (attrOutput != null) {
                val expr: Expression = tag.getFactory().toExprBoolean(attrOutput.getValue()) as? LitBoolean
                        ?: throw EvaluatorException("Attribute output of the Tag Function, must be a literal boolean value (true or false, yes or no)")
            }
        }

        // Buffer output
        run {
            val attrBufferOutput: Attribute = tag.getAttribute("bufferoutput")
            if (attrBufferOutput != null) {
                val expr: Expression = tag.getFactory().toExprBoolean(attrBufferOutput.getValue()) as? LitBoolean
                        ?: throw EvaluatorException("Attribute bufferOutput of the Tag Function, must be a literal boolean value (true or false, yes or no)")
            }
        }

        // check attribute values
        val attrs: Map<String?, Attribute?> = tag.getAttributes()
        val it: Iterator<Attribute?> = attrs.values().iterator()
        while (it.hasNext()) {
            checkAttributeValue(tag, it.next())
        }

        // add to static scope
        if (isStatic) {
            // remove that tag from parent
            ASMUtil.remove(tag)
            val body: Body = tag.getParent() as Body
            val sb: StaticBody = Static.getStaticBody(body)
            sb.addStatement(tag)
        }
    }

    @Throws(EvaluatorException::class)
    private fun checkAttributeValue(tag: Tag?, attr: Attribute?) {
        if (attr.getValue() !is Literal) throw EvaluatorException("Attribute [" + attr.getName().toString() + "] of the Tag [" + tag.getFullname().toString() + "] must be a literal/constant value")
    }

    companion object {
        @Throws(EvaluatorException::class)
        fun checkFunctionName(name: String?, flibs: Array<FunctionLib?>?, ps: PageSource?) {
            var flf: FunctionLibFunction
            for (i in flibs.indices) {
                flf = flibs!![i].getFunction(name)
                if (flf != null && flf.getFunctionClassDefinition().getClazz(null) !== CFFunction::class.java) {
                    var path: String? = null
                    if (ps != null) {
                        path = ps.getDisplayPath()
                        path = path.replace('\\', '/')
                    }
                    if (path == null || path.indexOf("/library/function/") === -1) throw EvaluatorException("The name [$name] is already used by a built in Function")
                }
            }
        }

        @Throws(EvaluatorException::class)
        fun throwIfNotEmpty(tag: Tag?) {
            val body: Body = tag.getBody()
            val statments: List<Statement?> = body.getStatements()
            var stat: Statement
            val it: Iterator<Statement?> = statments.iterator()
            var tlt: TagLibTag
            while (it.hasNext()) {
                stat = it.next()
                if (stat is Tag) {
                    tlt = (stat as Tag).getTagLibTag()
                    if (!tlt.getTagClassDefinition().isClassNameEqualTo("tachyon.runtime.tag.Argument")) throw EvaluatorException("tag [" + tlt.getFullName().toString() + "] is not allowed inside a function declaration")
                }
                /*
			 * else if(stat instanceof PrintOut) { //body.remove(stat); }
			 */
            }
        }
    }
}