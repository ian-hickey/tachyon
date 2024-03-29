/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Tachyon Assosication Switzerland
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

import tachyon.runtime.op.Caster

/**
 *
 * Prueft den Kontext des Tag argument. Das Tag `argument` darf nur direkt innerhalb des
 * Tag `function` liegen. Dem Tag `argument` muss als erstes im tag function
 * vorkommen
 */
class Argument : EvaluatorSupport() {
    @Override
    @Throws(EvaluatorException::class)
    fun evaluate(tag: Tag?, libTag: TagLibTag?) {
        val ns: String = libTag.getTagLib().getNameSpaceAndSeparator()
        val functionName = ns + "function"
        ASMUtil.isLiteralAttribute(tag, "type", ASMUtil.TYPE_STRING, false, true)
        ASMUtil.isLiteralAttribute(tag, "name", ASMUtil.TYPE_STRING, false, true)
        // ASMUtil.isLiteralAttribute(tag,"hint",ASMUtil.TYPE_STRING,false,true);
        // ASMUtil.isLiteralAttribute(tag,"displayname",ASMUtil.TYPE_STRING,false,true);

        // check if default can be converted to a literal value that match the type declration.
        checkDefaultValue(tag)

        // check attribute passby
        val attrPassBy: Attribute = tag.getAttribute("passby")
        if (attrPassBy != null) {
            val expr: ExprString = tag.getFactory().toExprString(attrPassBy.getValue()) as? LitString
                    ?: throw EvaluatorException("Attribute [passby] of the tag [Argument], must be a literal string")
            val lit: LitString = expr as LitString
            val passBy: String = lit.getString().toLowerCase().trim()
            if (!"value".equals(passBy) && !"ref".equals(passBy) && !"reference".equals(passBy)) throw EvaluatorException("Attribute [passby] of the tag [Argument] has an invalid value [$passBy], valid values are [reference,value]")
        }

        // check if tag is direct inside function
        if (!ASMUtil.isParentTag(tag, functionName)) {
            val parent: Tag = ASMUtil.getParentTag(tag)
            val addText = if (parent != null) "but tag [" + libTag.getFullName().toString() + "] is inside tag [" + parent.getFullname().toString() + "]" else "but tag [" + libTag.getFullName().toString() + "] has no parent"
            throw EvaluatorException("Wrong Context, tag [" + libTag.getFullName().toString() + "] must be direct inside a [" + functionName + "] tag, " + addText)
        }
        // TODO check if there is a tag other than argument and text before
    }

    companion object {
        fun checkDefaultValue(tag: Tag?) {
            val _type: Attribute = tag.getAttribute("type")
            if (_type != null) {
                val typeValue: ExprString = tag.getFactory().toExprString(_type.getValue())
                if (typeValue is LitString) {
                    val strType: String = (typeValue as LitString).getString()
                    val _default: Attribute = tag.getAttribute("default")
                    if (_default != null) {
                        val defaultValue: Expression = _default.getValue()
                        if (defaultValue is LitString) {
                            val strDefault: String = (defaultValue as LitString).getString()

                            // check for boolean
                            if ("boolean".equalsIgnoreCase(strType)) {
                                if ("true".equalsIgnoreCase(strDefault) || "yes".equalsIgnoreCase(strDefault)) tag.addAttribute(Attribute(_default.isDynamicType(), _default.getName(), tag.getFactory().TRUE(), _default.getType()))
                                if ("false".equalsIgnoreCase(strDefault) || "no".equalsIgnoreCase(strDefault)) tag.addAttribute(Attribute(_default.isDynamicType(), _default.getName(), tag.getFactory().FALSE(), _default.getType()))
                            }

                            // check for numbers
                            if ("number".equalsIgnoreCase(strType) || "numeric".equalsIgnoreCase(strType)) {
                                val dbl: Double = Caster.toDouble(strDefault, null)
                                if (dbl != null) {
                                    tag.addAttribute(
                                            Attribute(_default.isDynamicType(), _default.getName(), tag.getFactory().createLitNumber(dbl.doubleValue()), _default.getType()))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}