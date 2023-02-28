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

import org.w3c.dom.Node

/**
 * Implements the CFML Function xmlchildpos
 */
object XmlChildPos : Function {
    fun call(pc: PageContext?, node: Node?, name: String?, index: Double): Double {
        val xmlNodeList = XMLNodeList(node, false, Node.ELEMENT_NODE)
        val len: Int = xmlNodeList.getLength()
        // if(index<1)throw new FunctionException(pc,"XmlChildPos","second","index","attribute must be 1 or
        // greater");
        var count = 1
        for (i in 0 until len) {
            val n: Node = xmlNodeList.item(i)
            if (XMLUtil.nameEqual(n, name, XMLUtil.isCaseSensitve(n)) && count++.toDouble() == index) return i + 1
        }
        return (-1).toDouble()
    }
}