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
package lucee.transformer.cfml.evaluator.impl

import java.util.Iterator

/**
 * Prueft den Kontext des Tag break. Das Tag `break` darf nur innerhalb des Tag
 * `loop, while, foreach` liegen.
 */
class Component : EvaluatorSupport() {
    @Override
    @Throws(EvaluatorException::class)
    fun evaluate(tag: Tag?, tlt: TagLibTag?) {
        val tc: TagCIObject? = tag as TagCIObject?
        val inline = tag is TagComponent && (tag as TagComponent?).isInline()
        var pPage: Statement? = tag.getParent()
        val page: Page?
        if (inline) {
            page = try {
                ASMUtil.getAncestorPage(null, tag)
            } catch (te: TransformerException) {
                val ee = EvaluatorException(te.getMessage())
                ee.initCause(te)
                throw ee
            }
        } else {
            // move components inside script to root
            if (pPage is Page) {
                page = pPage as Page?
            } else {
                // is in script
                val p: Tag = ASMUtil.getParentTag(tag)
                if (p.getParent().also { pPage = it } is Page && p.getTagLibTag().getName().equalsIgnoreCase(
                                if ((pPage as Page?).getSourceCode().getDialect() === CFMLEngine.DIALECT_CFML) Constants.CFML_SCRIPT_TAG_NAME else Constants.LUCEE_SCRIPT_TAG_NAME)) { // chnaged
                    page = pPage as Page?
                    // move imports from script to component body
                    val children: List<Statement?> = p.getBody().getStatements()
                    val it: Iterator<Statement?> = children.iterator()
                    var stat: Statement?
                    var t: Tag?
                    while (it.hasNext()) {
                        stat = it.next()
                        if (stat !is Tag) continue
                        t = stat as Tag?
                        if (t.getTagLibTag().getName().equals("import")) {
                            tag.getBody().addStatement(t)
                        }
                    }

                    // move to page
                    ASMUtil.move(tag, page)

                    // if(!inline)ASMUtil.replace(p, tag, false);
                } else throw EvaluatorException("Wrong Context, tag [" + tlt.getFullName().toString() + "] can't be inside other tags, tag is inside tag [" + p.getFullname().toString() + "]")
            }
        }
        val main = isMainComponent(page, tc)

        // is a full grown component or an inline component
        if (!inline && isInsideCITemplate(page) === Boolean.FALSE) {
            throw EvaluatorException("Wrong Context, [" + tlt.getFullName().toString() + "] tag must be inside a file with the extension [" + Constants.getCFMLComponentExtension()
                    .toString() + "] or [" + Constants.getLuceeComponentExtension().toString() + "], only inline components are allowed outside ")
        }
        val isComponent: Boolean = tlt.getTagClassDefinition().isClassNameEqualTo("lucee.runtime.tag.Component")
        /*
		 * boolean isInterface="lucee.runtime.tag.Interface".equals(tlt.getTagClassName()); if(main) {
		 * if(isComponent) page.setIsComponent(true); else if(isInterface) page.setIsInterface(true); }
		 */tc.setMain(main)

        // Attributes

        // Name
        var name: String? = null
        if (!main) {
            val attrs: Map<String?, Attribute?> = tag.getAttributes()
            if (attrs.size() > 0) {
                val first: Attribute = attrs.values().iterator().next()
                if (first.isDefaultValue()) {
                    name = first.getName()
                }
            }
            if (name == null) {
                val attr: Attribute = tag.getAttribute("name")
                name = if (attr != null) {
                    val expr: Expression = tag.getFactory().toExprString(attr.getValue()) as? LitString
                            ?: throw EvaluatorException("Name of the component [" + tlt.getFullName().toString() + "], must be a literal string value")
                    (expr as LitString).getString()
                } else throw EvaluatorException("Missing name of the component [" + tlt.getFullName().toString() + "]")
            }
            tc.setName(name)
        }

        // output
        // "output=true" is handled in "lucee.transformer.cfml.attributes.impl.Function"
        var attr: Attribute = tag.getAttribute("output")
        if (attr != null) {
            val expr: Expression = tag.getFactory().toExprBoolean(attr.getValue()) as? LitBoolean
                    ?: throw EvaluatorException("Attribute [output] of the tag [" + tlt.getFullName().toString() + "], must contain a static boolean value (true or false, yes or no)")
            // boolean output = ((LitBoolean)expr).getBooleanValue();
            // if(!output) ASMUtil.removeLiterlChildren(tag, true);
        }

        // extends
        attr = tag.getAttribute("extends")
        if (attr != null) {
            val expr: Expression = tag.getFactory().toExprString(attr.getValue()) as? LitString
                    ?: throw EvaluatorException("Attribute [extends] of the tag [" + tlt.getFullName().toString() + "], must contain a literal string value")
        }

        // implements
        if (isComponent) {
            attr = tag.getAttribute("implements")
            if (attr != null) {
                val expr: Expression = tag.getFactory().toExprString(attr.getValue()) as? LitString
                        ?: throw EvaluatorException("Attribute [implements] of the tag [" + tlt.getFullName().toString() + "], must contain a literal string value")
            }
        }
        // modifier
        if (isComponent) {
            attr = tag.getAttribute("modifier")
            if (attr != null) {
                val expr: Expression = tag.getFactory().toExprString(attr.getValue()) as? LitString
                        ?: throw EvaluatorException("Attribute [modifier] of the tag [" + tlt.getFullName().toString() + "], must contain a literal string value")
                val ls: LitString = expr as LitString
                val mod: Int = ComponentUtil.toModifier(ls.getString(), lucee.runtime.Component.MODIFIER_NONE, -1)
                if (mod == -1) throw EvaluatorException("Value [" + ls.getString().toString() + "] from attribute [modifier] of the tag [" + tlt.getFullName().toString() + "] is invalid, valid values are [none, abstract, final]")
            }
        }
    }

    private fun isMainComponent(page: Page?, comp: TagCIObject?): Boolean {
        // first is main
        val it: Iterator<Statement?> = page.getStatements().iterator()
        while (it.hasNext()) {
            val s: Statement? = it.next()
            if (s is TagCIObject) return s === comp
        }
        return false
    }

    /**
     * is the template ending with a component extension?
     *
     * @param page
     * @return return true if so false otherwse and null if the code is not depending on a template
     */
    private fun isInsideCITemplate(page: Page?): Boolean? {
        val sc: SourceCode = page.getSourceCode() as? PageSourceCode ?: return null
        val psc: PageSource = (sc as PageSourceCode).getPageSource()
        val src: String = psc.getDisplayPath()
        return Constants.isComponentExtension(ResourceUtil.getExtension(src, ""))
        // int pos=src.lastIndexOf(".");
        // return pos!=-1 && pos<src.length() && src.substring(pos+1).equals(Constants.COMPONENT_EXTENSION);
    }
}