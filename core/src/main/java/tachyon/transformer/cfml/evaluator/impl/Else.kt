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

import tachyon.transformer.bytecode.statement.tag.Tag

/**
 *
 * Prueft den Kontext des Tag else. Das Tag `else` darf nur direkt innerhalb des Tag
 * `if` liegen. Dem Tag `else` darf, innerhalb des Tag `if`, kein
 * Tag `if` nachgestellt sein. Das Tag darf auch nur einmal vorkommen innerhalb des Tag
 * if.
 */
class Else : EvaluatorSupport() {
    @Override
    @Throws(EvaluatorException::class)
    fun evaluate(tag: Tag?, libTag: TagLibTag?) {
        val ns: String = libTag.getTagLib().getNameSpaceAndSeparator()
        val ifName = ns + "if"

        // check if tag is direct inside if
        if (!ASMUtil.isParentTag(tag, TagIf::class.java)) {
            throw EvaluatorException("Wrong Context, tag [" + libTag.getFullName().toString() + "] must be direct inside a [" + ifName + "] tag")
        }

        // check if is there an elseif tag after this tag
        if (ASMUtil.hasSisterTagAfter(tag, "elseif")) throw EvaluatorException("Wrong Context, tag [cfelseif] can't be after tag [else]")
        // check if tag else is unique
        if (ASMUtil.hasSisterTagWithSameName(tag)) throw EvaluatorException("Wrong Context, tag [else] must be once inside the tag [if]")
    }
}