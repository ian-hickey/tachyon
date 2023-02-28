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
package lucee.runtime.text.xml.struct

import org.w3c.dom.DOMException

/**
 *
 */
class XMLTextStruct(text: Text?, caseSensitive: Boolean) : XMLNodeStruct(text, caseSensitive), Text {
    private val text: Text?
    @Override
    @Throws(DOMException::class)
    fun splitText(offset: Int): Text? {
        return text.splitText(offset)
    }

    @get:Override
    val length: Int
        get() = text.getLength()

    @Override
    @Throws(DOMException::class)
    fun deleteData(offset: Int, count: Int) {
        text.deleteData(offset, count)
    }

    @get:Throws(DOMException::class)
    @get:Override
    @set:Throws(DOMException::class)
    @set:Override
    var data: String?
        get() = text.getData()
        set(data) {
            text.setData(data)
        }

    @Override
    @Throws(DOMException::class)
    fun substringData(offset: Int, count: Int): String? {
        return text.substringData(offset, count)
    }

    @Override
    @Throws(DOMException::class)
    fun replaceData(offset: Int, count: Int, arg: String?) {
        text.replaceData(offset, count, arg)
    }

    @Override
    @Throws(DOMException::class)
    fun insertData(offset: Int, arg: String?) {
        text.insertData(offset, arg)
    }

    @Override
    @Throws(DOMException::class)
    fun appendData(arg: String?) {
        text.appendData(arg)
    }

    // used only with java 7, do not set @Override
    val isElementContentWhitespace: Boolean
        get() = text.getNodeValue().trim().length() === 0

    // used only with java 7, do not set @Override
    val wholeText: String?
        get() = text.getNodeValue()

    // used only with java 7, do not set @Override
    @Throws(DOMException::class)
    fun replaceWholeText(content: String?): Text? {
        val oldText: Text? = text
        val doc: Document = XMLUtil.getDocument(text)
        val newText: Text = doc.createTextNode(content)
        val parent: Node = oldText.getParentNode()
        parent.replaceChild(XMLCaster.toRawNode(newText), XMLCaster.toRawNode(oldText))
        return oldText
    }

    @Override
    override fun duplicate(deepCopy: Boolean): Collection? {
        return XMLTextStruct(text.cloneNode(deepCopy) as Text, caseSensitive)
    }

    @Override
    override fun cloneNode(deep: Boolean): Node? {
        return XMLTextStruct(text.cloneNode(deep) as Text, caseSensitive)
    }

    /**
     * @param text
     * @param caseSensitive
     */
    init {
        this.text = text
    }
}