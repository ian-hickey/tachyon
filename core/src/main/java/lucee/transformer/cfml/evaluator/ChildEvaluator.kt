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
package lucee.transformer.cfml.evaluator

import lucee.transformer.bytecode.statement.tag.Tag

/**
 * checks the if a child tag is inside his parent
 */
class ChildEvaluator(private val parentName: String?) : EvaluatorSupport() {
    @Override
    @Throws(EvaluatorException::class)
    override fun evaluate(tag: Tag?, libTag: TagLibTag?) {

        // check parent
        val ns: String = libTag.getTagLib().getNameSpaceAndSeparator()
        val name = ns + parentName
        if (!ASMUtil.hasAncestorTag(tag, name)) throw EvaluatorException("Wrong Context, tag [" + libTag.getFullName().toString() + "] must be inside a [" + name + "] tag")
    }
}