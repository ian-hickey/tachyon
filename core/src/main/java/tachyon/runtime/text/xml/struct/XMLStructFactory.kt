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
package tachyon.runtime.text.xml.struct

import org.w3c.dom.Attr

/**
 *
 */
object XMLStructFactory {
    /**
     * @param node
     * @param caseSensitive
     * @return XMLStruct instance
     */
    fun newInstance(node: Node?, caseSensitive: Boolean): XMLStruct? {
        // TODO set Case Sensitive
        if (node is XMLStruct) return node
        return if (node is Document) XMLDocumentStruct(node as Document?, caseSensitive) else if (node is Text) XMLTextStruct(node as Text?, caseSensitive) else if (node is CDATASection) XMLCDATASectionStruct(node as CDATASection?, caseSensitive) else if (node is Element) XMLElementStruct(node as Element?, caseSensitive) else if (node is Attr) XMLAttrStruct(node as Attr?, caseSensitive) else XMLNodeStruct(node, caseSensitive)
    }
}