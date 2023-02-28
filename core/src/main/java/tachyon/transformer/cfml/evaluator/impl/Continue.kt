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

import tachyon.commons.lang.StringUtil

/**
 * Prueft den Kontext des Tag continue. Das Tag `break` darf nur innerhalb des Tag
 * `loop, while, foreach` liegen.
 */
class Continue : EvaluatorSupport() {
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
            val tc: TagContinue? = tag as TagContinue?
            label = Break.variableToString(tag, attrLabel, null)
            if (label != null) {
                tc.setLabel(label.trim().also { label = it })
                tag.removeAttribute("label")
            } else if (ASMUtil.isLiteralAttribute(tag, attrLabel, ASMUtil.TYPE_STRING, false, true)) {
                val ls: LitString = tag.getFactory().toExprString(tag.getAttribute("label").getValue()) as LitString
                label = ls.getString()
                if (!StringUtil.isEmpty(label, true)) {
                    tc.setLabel(label.trim().also { label = it })
                    tag.removeAttribute("label")
                } else label = null
            }
        }
        if (ASMUtil.isLiteralAttribute(tag, "label", ASMUtil.TYPE_STRING, false, true)) {
            val ls: LitString = tag.getFactory().toExprString(tag.getAttribute("label").getValue()) as LitString
            val tc: TagContinue? = tag as TagContinue?
            label = ls.getString()
            if (!StringUtil.isEmpty(label, true)) {
                tc.setLabel(label.trim().also { label = it })
                tag.removeAttribute("label")
            } else label = null
        }
        if (!ASMUtil.hasAncestorContinueFCStatement(tag, label)) {
            if (tag.isScriptBase()) {
                if (StringUtil.isEmpty(label)) throw EvaluatorException("Wrong Context, [" + libTag.getName().toString() + "] must be inside a loop (for,while,loop ...)")
                throw EvaluatorException("Wrong Context, [" + libTag.getName().toString() + "] must be inside a loop (for,while,loop ...) with the label [" + label.toString() + "]")
            }
            if (StringUtil.isEmpty(label)) throw EvaluatorException("Wrong Context, tag [" + libTag.getFullName().toString() + "] must be inside a [" + loopName + "] or [" + whileName + "] tag")
            throw EvaluatorException("Wrong Context, tag [" + libTag.getFullName().toString() + "] must be inside a [" + loopName + "] or [" + whileName + "] tag with the label [" + label.toString() + "]")
        }
    }
}