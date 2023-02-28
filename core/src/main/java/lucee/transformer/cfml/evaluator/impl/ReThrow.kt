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
package lucee.transformer.cfml.evaluator.impl

import lucee.transformer.bytecode.statement.tag.Tag

/**
 * Prueft den Kontext des Tag rethrow. Das Tag `rethrow` darf nur innerhalb des Tag
 * `throw` liegen.
 */
class ReThrow : EvaluatorSupport() {
    @Override
    @Throws(EvaluatorException::class)
    fun evaluate(tag: Tag?, libTag: TagLibTag?) {
        val ns: String = libTag.getTagLib().getNameSpaceAndSeparator()
        val queryName = ns + "catch"
        if (!ASMUtil.hasAncestorTryStatement(tag)) {
            if (tag!!.isScriptBase()) throw EvaluatorException("Wrong Context, statement [" + libTag.getName().toString() + "] must be inside a [" + queryName + "] tag or catch statement")
            throw EvaluatorException("Wrong Context, tag [" + libTag.getFullName().toString() + "] must be inside a [" + queryName + "] tag")
        }
        // ASMUtil.replace(tag,new TagReThrow(tag));
    }
}