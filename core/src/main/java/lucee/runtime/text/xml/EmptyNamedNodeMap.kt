/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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
package lucee.runtime.text.xml

import org.w3c.dom.DOMException

class EmptyNamedNodeMap : NamedNodeMap {
    @get:Override
    val length: Int
        get() = 0

    @Override
    fun getNamedItem(name: String?): Node? {
        return null
    }

    @Override
    fun getNamedItemNS(namespaceURI: String?, name: String?): Node? {
        return null
    }

    @Override
    fun item(arg0: Int): Node? {
        return null
    }

    @Override
    @Throws(DOMException::class)
    fun removeNamedItem(key: String?): Node? {
        throw DOMException(DOMException.NOT_FOUND_ERR, "NodeMap is empty")
    }

    @Override
    @Throws(DOMException::class)
    fun removeNamedItemNS(arg0: String?, arg1: String?): Node? {
        throw DOMException(DOMException.NOT_FOUND_ERR, "NodeMap is empty")
    }

    @Override
    @Throws(DOMException::class)
    fun setNamedItem(arg0: Node?): Node? {
        throw DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, "NodeMap is read-only")
    }

    @Override
    @Throws(DOMException::class)
    fun setNamedItemNS(arg0: Node?): Node? {
        throw DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, "NodeMap is read-only")
    }
}