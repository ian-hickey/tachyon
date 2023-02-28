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
package tachyon.runtime.functions.xml

import org.xml.sax.InputSource

/**
 *
 */
object XmlValidate : Function {
    private const val serialVersionUID = 3566454779506863837L
    @Throws(PageException::class)
    fun call(pc: PageContext?, strXml: String?): Struct? {
        return call(pc, strXml, null)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, strXml: String?, objValidator: Object?): Struct? {
        return try {

            // no validator
            if (StringUtil.isEmpty(objValidator)) {
                val xml: InputSource = XMLUtil.toInputSource(pc, strXml.trim())
                return XMLUtil.validate(xml, null, null, null)
            }

            // single validator
            if (!Decision.isArray(objValidator)) {
                val xml: InputSource = XMLUtil.toInputSource(pc, strXml.trim())
                val strValidator: String = Caster.toString(objValidator)
                return XMLUtil.validate(xml, XMLUtil.toInputSource(pc, strValidator), strValidator, null)
            }

            // multiple validators
            var result: Struct? = null
            val strValidators: Array<String?> = ListUtil.toStringArray(Caster.toArray(objValidator))
            for (strValidator in strValidators) {
                val xml: InputSource = XMLUtil.toInputSource(pc, strXml.trim())
                result = XMLUtil.validate(xml, XMLUtil.toInputSource(pc, strValidator), strValidator, result)
            }
            result
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
    }
}