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
 * Implements the CFML Function xmltransform
 */
package lucee.runtime.functions.xml

import org.w3c.dom.Document

object XmlTransform : Function {
    @Throws(PageException::class)
    fun call(pc: PageContext?, oXml: Object?, xsl: String?): String? {
        return call(pc, oXml, xsl, null)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, oXml: Object?, xsl: String?, parameters: Struct?): String? {
        return try {
            val doc: Document
            doc = if (oXml is String) {
                XMLUtil.parse(XMLUtil.toInputSource(pc, oXml.toString()), null, false)
            } else if (oXml is Node) XMLUtil.getDocument(oXml as Node?) else throw XMLException("XML Object is of invalid type, must be a XML String or a XML Object", "now it is " + Caster.toClassName(oXml))
            XMLUtil.transform(doc, XMLUtil.toInputSource(pc, xsl), parameters)
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
    }
}