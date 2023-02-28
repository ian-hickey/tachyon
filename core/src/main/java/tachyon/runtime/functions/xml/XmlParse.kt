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
/**
 * Implements the CFML Function xmlparse
 */
package tachyon.runtime.functions.xml

import org.w3c.dom.Node

class XmlParse : BIF(), Function {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 4) return call(pc, Caster.toString(args[0]), Caster.toBooleanValue(args[1]), Caster.toString(args[2]), Caster.toBooleanValue(args[3]))
        if (args.size == 3) return call(pc, Caster.toString(args[0]), Caster.toBooleanValue(args[1]), Caster.toString(args[2]))
        if (args.size == 2) return call(pc, Caster.toString(args[0]), Caster.toBooleanValue(args[1]))
        if (args.size == 1) return call(pc, Caster.toString(args[0]))
        throw FunctionException(pc, "XmlParse", 1, 4, args.size)
    }

    companion object {
        private const val serialVersionUID = -855751130125993125L
        @Throws(PageException::class)
        fun call(pc: PageContext?, string: String?): Node? {
            return call(pc, string, false, null, false)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, string: String?, caseSensitive: Boolean): Node? {
            return call(pc, string, caseSensitive, null, false)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, string: String?, caseSensitive: Boolean, strValidator: String?): Node? {
            return call(pc, string, caseSensitive, strValidator, false)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, strXML: String?, caseSensitive: Boolean, strValidator: String?, lenient: Boolean): Node? {
            return try {
                val xml: InputSource = XMLUtil.toInputSource(pc, StringUtil.trim(strXML, true, true, ""))
                val validator: InputSource? = if (StringUtil.isEmpty(strValidator)) null else XMLUtil.toInputSource(pc, strValidator.trim())
                XMLCaster.toXMLStruct(XMLUtil.parse(xml, validator, lenient), caseSensitive)
            } catch (e: Exception) {
                throw Caster.toPageException(e)
            }
        }
    }
}