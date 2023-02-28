/**
 * Copyright (c) 2023, TachyonCFML.org
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

class Interface : Component() {
    @Override
    @Throws(EvaluatorException::class)
    override fun evaluate(tag: Tag?, libTag: TagLibTag?) {
        super.evaluate(tag, libTag)
        val body: Body = tag.getBody()
        val statments: List<Statement?> = body.getStatements()
        var stat: Statement
        val it: Iterator<Statement?> = statments.iterator()
        var t: Tag
        while (it.hasNext()) {
            stat = it.next()
            if (stat is PrintOut) {
                // body.remove(stat);
            } else if (stat is Tag) {
                t = stat as Tag
                if (stat is TagImport) {
                    // ignore
                } else if (stat is TagFunction) {
                    Function.throwIfNotEmpty(t)
                    val attr: Attribute = t.getAttribute("access")
                    if (attr != null) {
                        val expr: ExprString = t.getFactory().toExprString(attr.getValue()) as? LitString
                                ?: throw EvaluatorException("the attribute access of the tag [function] inside an interface must contain a constant value")
                        val access: String = (expr as LitString).getString().trim()
                        if (!"public".equalsIgnoreCase(access)) throw EvaluatorException(
                                "the attribute access of the tag [function] inside an interface definition can only have the value [public] not [$access]")
                    } else t.addAttribute(Attribute(false, "access", stat.getFactory().createLitString("public"), "string"))
                } else throw EvaluatorException("tag [" + libTag.getFullName().toString() + "] can only contain function definitions.")
            }
        }
    }
}