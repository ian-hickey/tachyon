/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
 * Copyright (c) 2016, Lucee Association Switzerland. All rights reserved.
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
 * Prueft den Kontext des Tag output. Das Tag output darf nicht innerhalb eines output Tag
 * verschachtelt sein, ausser das aeussere Tag besitzt ein group Attribute. Das innere Tag darf
 * jedoch kein group Attribute besitzen.
 *
 */
class Output : EvaluatorSupport() {
    @Override
    @Throws(EvaluatorException::class)
    fun evaluate(tag: Tag?, libTag: TagLibTag?, flibs: Array<FunctionLib?>?) {
        val output: TagOutput? = tag as TagOutput?

        // check if inside a query tag
        var parent: TagOutput? = output

        // encodeFor
        val encodeFor: Attribute = tag.getAttribute("encodefor")
        if (encodeFor != null) {
            val encodeForValue: Expression = tag.getFactory().toExprString(encodeFor.getValue())
            /*
			 * if(encodeForValue instanceof Literal) { Literal l=(Literal)encodeForValue; short df=(short)-1;
			 * short encType = ESAPIUtil.toEncodeType( l.getString(),df);
			 * if(encType!=df)encodeForValue=encodeForValue.getFactory().createLitInteger(encType); }
			 */addEncodeToChildren(tag.getBody().getStatements().iterator(), encodeForValue)
        }

        // query
        var hasParentWithGroup = false
        var hasParentWithQuery = false
        val hasQuery: Boolean = tag.containsAttribute("query")
        while (getParentTagOutput(parent).also { parent = it } != null) {
            if (!hasParentWithQuery) hasParentWithQuery = parent.hasQuery()
            if (!hasParentWithGroup) hasParentWithGroup = parent.hasGroup()
            if (hasParentWithQuery && hasParentWithGroup) break
        }
        if (hasQuery && hasParentWithQuery) throw EvaluatorException("Nesting of tags cfoutput with attribute query is not allowed")
        if (hasQuery) output.setType(TagOutput.TYPE_QUERY) else if (tag.containsAttribute("group") && hasParentWithQuery) output.setType(TagOutput.TYPE_GROUP) else if (hasParentWithQuery) {
            if (hasParentWithGroup) output.setType(TagOutput.TYPE_INNER_GROUP) else output.setType(TagOutput.TYPE_INNER_QUERY)
        } else output.setType(TagOutput.TYPE_NORMAL)

        // attribute maxrows and endrow not allowd at the same time
        if (tag.containsAttribute("maxrows") && tag.containsAttribute("endrow")) throw EvaluatorException("Wrong Context, you cannot use attribute maxrows and endrow at the same time.")
    }

    private fun addEncodeToChildren(it: Iterator?, encodeForValue: Expression?) {
        var stat: Statement
        while (it.hasNext()) {
            stat = it.next() as Statement
            if (stat is PrintOut) {
                val printOut: PrintOut = stat as PrintOut
                val e: Expression = encodeForValue.getFactory().removeCastString(printOut.getExpr())
                if (e !is Literal) {
                    if (e is Variable) {
                        val member: Member = (e as Variable).getFirstMember()
                        if (member is BIF) {
                            val bif: BIF = member as BIF
                            val cn: String = bif.getClassDefinition().getClassName()
                            if (cn.startsWith(ENCODE_FOR_) || cn.equals(ESAPI_ENCODE)) {
                                continue
                            }
                        }
                    }
                    printOut.setEncodeFor(encodeForValue)
                }
            } else if (stat is Tag) {
                val b: Body = (stat as Tag).getBody()
                if (b != null) addEncodeToChildren(b.getStatements().iterator(), encodeForValue)
            } else if (stat is Body) {
                addEncodeToChildren((stat as Body).getStatements().iterator(), encodeForValue)
            }
        }
    }

    companion object {
        private val ENCODE_FOR_: String? = "org.lucee.extension.esapi.functions.EncodeFor" // EncodeForCSS.class.getName();
        private val ESAPI_ENCODE: String? = "org.lucee.extension.esapi.functions.ESAPIEncode"

        /*
	 * private FunctionLibFunction getEncodeForFunction(FunctionLib[] flibs) throws EvaluatorException {
	 * FunctionLibFunction f; if(flibs!=null)for(int i=0;i<flibs.length;i++) { f =
	 * flibs[i].getFunction("ESAPIEncode"); if(f!=null) return f; } // should never happen throw new
	 * EvaluatorException("could not find function ESAPIEncode ("+(flibs==null?"null":""+flibs.length)+
	 * ")"); }
	 */
        fun getParentTagOutput(stat: TagOutput?): TagOutput? {
            var parent: Statement? = stat
            while (true) {
                parent = parent.getParent()
                if (parent == null) return null
                if (parent is TagOutput) return parent as TagOutput?
            }
        }
    }
}