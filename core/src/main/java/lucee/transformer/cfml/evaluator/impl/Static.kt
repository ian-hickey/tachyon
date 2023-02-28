/**
 *
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
 * Prueft den Kontext des Tag case. Das Tag `httpparam` darf nur innerhalb des Tag
 * `http` liegen.
 */
class Static : EvaluatorSupport() {
    @Override
    @Throws(EvaluatorException::class)
    fun evaluate(tag: Tag?, libTag: TagLibTag?) {

        // check parent
        var body: Body? = null
        val compName: String = Property.getComponentName(tag)
        var isCompChild = false
        val p: Tag = ASMUtil.getParentTag(tag)
        if (p != null && (p is TagComponent || getFullname(p, "").equalsIgnoreCase(compName))) {
            isCompChild = true
            body = p.getBody()
        }
        val pp: Tag? = if (p != null) ASMUtil.getParentTag(p) else null
        if (!isCompChild && pp != null && (p is TagComponent || getFullname(pp, "").equalsIgnoreCase(compName))) {
            isCompChild = true
            body = pp.getBody()
        }
        if (!isCompChild) {
            throw EvaluatorException("Wrong Context for the the static constructor, " + "a static constructor must inside a component body.")
        }

        // Body body=(Body) tag.getParent();
        val children: List<Statement?> = tag.getBody().getStatements()

        // remove that tag from parent
        ASMUtil.remove(tag)
        val sb: StaticBody? = getStaticBody(body)
        ASMUtil.addStatements(sb, children)
    }

    private fun getFullname(tag: Tag?, defaultValue: String?): String? {
        if (tag != null) {
            var fn: String = tag.getFullname()
            if (StringUtil.isEmpty(fn)) fn = tag.getTagLibTag().getFullName()
            if (!StringUtil.isEmpty(fn)) return fn
        }
        return defaultValue
    }

    companion object {
        fun getStaticBody(body: Body?): StaticBody? {
            val it: Iterator<Statement?> = body.getStatements().iterator()
            var s: Statement?
            while (it.hasNext()) {
                s = it.next()
                if (s is StaticBody) return s as StaticBody?
            }
            val sb = StaticBody(body.getFactory())
            body.addStatement(sb)
            return sb
        }
    }
}