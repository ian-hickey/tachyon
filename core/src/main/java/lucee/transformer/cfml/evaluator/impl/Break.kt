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

import lucee.commons.lang.ExceptionUtil

/**
 * Prueft den Kontext des Tag break. Das Tag `break` darf nur innerhalb des Tag
 * `loop, while, foreach` liegen.
 */
class Break : EvaluatorSupport() {
    @Override
    @Throws(EvaluatorException::class)
    fun evaluate(tag: Tag?, libTag: TagLibTag?) {
        val ns: String = libTag.getTagLib().getNameSpaceAndSeparator()
        val loopName = ns + "loop"
        val whileName = ns + "while"

        // label
        var label: String? = null
        val attrLabel: Attribute = tag.getAttribute("label")
        if (attrLabel != null) {
            val tb: TagBreak? = tag as TagBreak?
            label = variableToString(tag, attrLabel, null)
            if (label != null) {
                tb.setLabel(label.trim().also { label = it })
                tag.removeAttribute("label")
            } else if (ASMUtil.isLiteralAttribute(tag, attrLabel, ASMUtil.TYPE_STRING, false, true)) {
                val ls: LitString = tag.getFactory().toExprString(tag.getAttribute("label").getValue()) as LitString
                label = ls.getString()
                if (!StringUtil.isEmpty(label, true)) {
                    tb.setLabel(label.trim().also { label = it })
                    tag.removeAttribute("label")
                } else label = null
            }
        }

        // no base tag found
        if (!ASMUtil.hasAncestorBreakFCStatement(tag, label)) {
            if (tag.isScriptBase()) {
                if (StringUtil.isEmpty(label)) throw EvaluatorException("Wrong Context, " + libTag.getName().toString() + " must be inside a looping statement or tag")
                throw EvaluatorException("Wrong Context, [" + libTag.getName().toString() + "] must be inside a looping statement or tag with the label [" + label.toString() + "]")
            }
            if (StringUtil.isEmpty(label)) throw EvaluatorException("Wrong Context, tag [" + libTag.getFullName().toString() + "] must be inside a [" + loopName + "] or [" + whileName + "] tag")
            throw EvaluatorException("Wrong Context, tag [" + libTag.getFullName().toString() + "] must be inside a [" + loopName + "] or [" + whileName + "] tag with the label [" + label.toString() + "]")
        }
    }

    companion object {
        fun variableToString(tag: Tag?, attrLabel: Attribute?, defaultValue: String?): String? {
            var value: Expression = attrLabel.getValue()
            while (value is Cast) value = (value as Cast).getExpr()
            if (value is Variable) {
                val `var`: Variable = value as Variable
                try {
                    return VariableString.variableToString(null, `var`, true)
                } catch (t: Throwable) {
                    ExceptionUtil.rethrowIfNecessary(t)
                }
            }
            return defaultValue
        }
    }
}