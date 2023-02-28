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
 * Implements the CFML Function xmlelemnew
 */
package tachyon.runtime.functions.xml

import org.w3c.dom.Document

object XmlElemNew : Function {
    private const val serialVersionUID = -2601887739406776466L
    @Throws(FunctionException::class)
    fun call(pc: PageContext?, node: Node?, childname: String?): Element? {
        return call(pc, node, null, childname)
    }

    @Throws(FunctionException::class)
    fun call(pc: PageContext?, node: Node?, namespace: String?, childname: String?): Element? {
        var namespace = namespace
        var childname = childname
        val doc: Document = XMLUtil.getDocument(node)
        if (StringUtil.isEmpty(childname)) {
            if (!StringUtil.isEmpty(namespace)) {
                childname = namespace
                namespace = null
            } else throw FunctionException(pc, "XmlElemNew", 3, "childname", "argument is required")
        }
        var el: Element? = null

        // without namespace
        if (StringUtil.isEmpty(namespace)) {
            if (childname.indexOf(':') !== -1) {
                val parts: Array<String?> = ListUtil.listToStringArray(childname, ':')
                childname = parts[1]
                val prefix = parts[0]
                namespace = getNamespaceForPrefix(doc.getDocumentElement(), prefix)
                if (StringUtil.isEmpty(namespace)) {
                    el = doc.createElement(childname)
                } else {
                    el = doc.createElementNS(namespace, childname)
                    el.setPrefix(prefix)
                }
            } else {
                el = doc.createElement(childname)
            }
        } else {
            el = doc.createElementNS(namespace, childname)
        }
        return XMLStructFactory.newInstance(el, false) as Element
    }

    private fun getNamespaceForPrefix(node: Node?, prefix: String?): String? {
        if (node == null) return null
        val atts: NamedNodeMap = node.getAttributes()
        if (atts != null) {
            var currLocalName: String
            var currPrefix: String
            val len: Int = atts.getLength()
            for (i in 0 until len) {
                val currAttr: Node = atts.item(i)
                currLocalName = currAttr.getLocalName()
                currPrefix = currAttr.getPrefix()
                if (prefix!!.equals(currLocalName) && "xmlns".equals(currPrefix)) {
                    return currAttr.getNodeValue()
                } else if (StringUtil.isEmpty(prefix) && "xmlns".equals(currLocalName) && StringUtil.isEmpty(currPrefix)) {
                    return currAttr.getNodeValue()
                }
            }
        }
        return null
    }
}