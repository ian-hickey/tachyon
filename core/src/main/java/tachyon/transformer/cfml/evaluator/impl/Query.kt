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

import java.util.ArrayList

/**
 * sign print outs for preserver
 */
class Query : EvaluatorSupport() {
    @Override
    @Throws(EvaluatorException::class)
    fun evaluate(tag: Tag?) {
        val body: Body = tag.getBody()
        val attr: Attribute = tag.getAttribute("sql")
        if (body == null && attr == null) throw EvaluatorException("You need to define the attribute [SQL] or define the SQL in the body of the tag.")

        // we do not check if both are defined here because the body could be an expression holding an empty
        // string
        if (body != null) {
            val stats: List<Statement?> = body.getStatements()
            if (stats != null) translateChildren(body.getStatements().iterator())
        }
    }

    private fun translateChildren(it: Iterator<Statement?>?) {
        var stat: Statement?
        while (it!!.hasNext()) {
            stat = it.next()
            if (stat is PrintOut) {
                val printOut: PrintOut? = stat as PrintOut?
                val e: Expression = printOut.getExpr()
                if (e !is Literal) {
                    val expr: Expression = printOut.getFactory().removeCastString(e)
                    if (expr is Variable) {
                        // do not preserve BIF PreserveSingleQuotes return value
                        var member: Member = (expr as Variable).getFirstMember()
                        if (member is BIF) {
                            val bif: BIF = member as BIF
                            if (bif.getClassDefinition().getClassName().equals(PreserveSingleQuotes::class.java.getName())) {
                                printOut.setExpr(bif.getArguments().get(0).getValue())
                                continue
                            } else if (bif.getClassDefinition().getClassName().equals(ListQualify::class.java.getName())) {
                                val args: Array<Argument?> = bif.getArguments()
                                val arr: List<Argument?> = ArrayList<Argument?>()

                                // first get existing arguments
                                arr.add(args[0])
                                arr.add(args[1])
                                if (args.size >= 3) arr.add(args[2]) else arr.add(Argument(expr.getFactory().createLitString(","), "string"))
                                if (args.size >= 4) arr.add(args[3]) else arr.add(Argument(expr.getFactory().createLitString("all"), "string"))
                                if (args.size >= 5) arr.add(args[4]) else arr.add(Argument(expr.getFactory().createLitBoolean(false), "boolean"))

                                // PSQ-BIF DO NOT REMOVE THIS COMMENT
                                arr.add(Argument(expr.getFactory().createLitBoolean(true), "boolean"))
                                bif.setArguments(arr.toArray(arrayOfNulls<Argument?>(arr.size())))
                                continue
                            } else if (bif.getClassDefinition().getClassName().equals(QuotedValueList::class.java.getName())
                                    || bif.getClassDefinition().getClassName().equals(ValueList::class.java.getName())) {
                                // printOut.setPreserveSingleQuote(false);
                                continue
                            }
                        }

                        // do not preserve UDF return value
                        member = (expr as Variable).getLastMember()
                        if (member is UDF) continue
                    }
                    printOut.setCheckPSQ(true)
                    if (e !== expr) printOut.setExpr(expr)
                }
            } else if (stat is Tag) {
                val b: Body = (stat as Tag?).getBody()
                if (b != null) translateChildren(b.getStatements().iterator())
            } else if (stat is Body) {
                translateChildren((stat as Body?).getStatements().iterator())
            }
        }
    }
}