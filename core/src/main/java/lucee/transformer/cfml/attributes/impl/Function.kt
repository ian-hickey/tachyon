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
package lucee.transformer.cfml.attributes.impl

import lucee.transformer.bytecode.statement.tag.Attribute

/**
 * Attribute Evaluator for the tag Function
 */
class Function : AttributeEvaluator {
    @Override
    @Throws(AttributeEvaluatorException::class)
    fun evaluate(tagLibTag: TagLibTag?, tag: Tag?): TagLibTag? {
        tagLibTag.setParseBody(false)
        val attrOutput: Attribute = tag.getAttribute("output") ?: return tagLibTag
        val expr: Expression = tag.getFactory().toExprBoolean(attrOutput.getValue()) as? LitBoolean
                ?: throw AttributeEvaluatorException("Attribute output of the Tag Function, must be a literal boolean value (true or false)")
        val output: Boolean = (expr as LitBoolean).getBooleanValue()
        if (output) tagLibTag.setParseBody(true)
        return tagLibTag
    }
}