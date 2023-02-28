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
package lucee.runtime.functions.xml

import org.w3c.dom.Node

object HtmlParse : Function {
    @Throws(PageException::class)
    fun call(pc: PageContext?, string: String?): Node? {
        return call(pc, string, false)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, strHTML: String?, caseSensitive: Boolean): Node? {
        return try {
            val xml: InputSource = XMLUtil.toInputSource(pc, strHTML, false)
            XMLCaster.toXMLStruct(XMLUtil.parse(xml, null, true), caseSensitive)
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }

        /*
		 * try { return XMLCaster.toXMLStruct(XMLUtil.parse(string,true),caseSensitive);//new
		 * XMLNodeStruct(XMLUtil.parse(string),caseSensitive); } catch (Exception e) { throw
		 * Caster.toPageException(e); }
		 */
    }
}