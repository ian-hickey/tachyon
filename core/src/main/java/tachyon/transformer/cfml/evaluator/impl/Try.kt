/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
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
 * Prueft den Kontext des Tag `try`. Innerhalb des Tag try muss sich am Schluss 1 bis n
 * Tags vom Typ catch befinden.
 */
class Try : EvaluatorSupport() {
    /**
     * @see tachyon.transformer.cfml.evaluator.EvaluatorSupport.evaluate
     */
    @Override
    @Throws(EvaluatorException::class)
    fun evaluate(tag: Tag?) {
        val body: Body = tag.getBody()
        var catchCount = 0
        var noCatchCount = 0
        var finallyCount = 0

        // count catch tag and other in body
        if (body != null) {
            val stats: List = body.getStatements()
            val it: Iterator = stats.iterator()
            var stat: Statement
            var t: Tag
            var name: String
            while (it.hasNext()) {
                stat = it.next() as Statement
                if (stat is Tag) {
                    t = stat as Tag
                    name = t.getTagLibTag().getName()
                    if (name.equals("finally")) {
                        finallyCount++
                        noCatchCount++
                    } else if (name.equals("catch")) catchCount++ else noCatchCount++
                } else noCatchCount++
            }
        }
        // check if has Content
        if (catchCount == 0 && finallyCount == 0) throw EvaluatorException("Wrong Context, tag cftry must have at least one tag cfcatch inside or a cffinally tag.")
        if (finallyCount > 1) throw EvaluatorException("Wrong Context, tag cftry can have only one tag cffinally inside.")
        // check if no has Content
        if (noCatchCount == 0) {
            ASMUtil.remove(tag)
        }
    }
}