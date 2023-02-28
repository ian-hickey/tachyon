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
class XMLAttrStruct(attr: Attr?, caseSensitive: Boolean) : XMLNodeStruct(attr, caseSensitive), Attr {
    private val attr: Attr?

    @get:Override
    val name: String?
        get() = attr.getName()

    @get:Override
    val ownerElement: Element?
        get() = XMLElementStruct(attr.getOwnerElement(), caseSensitive)

    @get:Override
    val specified: Boolean
        get() = attr.getSpecified()

    @get:Override
    @set:Throws(DOMException::class)
    @set:Override
    var value: String?
        get() = attr.getValue()
        set(arg0) {
            attr.setValue(arg0)
        }

    // used only with java 7, do not set @Override
    val schemaTypeInfo: TypeInfo?
        get() = null

    // used only with java 7, do not set @Override
    val isId: Boolean
        get() = false

    @Override
    override fun duplicate(deepCopy: Boolean): Collection? {
        return XMLAttrStruct(attr.cloneNode(deepCopy) as Attr, caseSensitive)
    }

    @Override
    override fun cloneNode(deep: Boolean): Node? {
        return XMLAttrStruct(attr.cloneNode(deep) as Attr, caseSensitive)
    }

    /**
     * constructor of the class
     *
     * @param section
     * @param caseSensitive
     */
    init {
        this.attr = attr
    }
}