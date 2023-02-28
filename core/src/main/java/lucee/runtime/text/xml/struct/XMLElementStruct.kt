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

import java.lang.reflect.Method

/**
 *
 */
class XMLElementStruct(element: Element?, caseSensitive: Boolean) : XMLNodeStruct(if (element is XMLElementStruct) (element as XMLElementStruct?)!!.getElement().also { element = it } else element, caseSensitive), Element {
    private val element: Element?

    @get:Override
    val tagName: String?
        get() = element.getTagName()

    @Override
    @Throws(DOMException::class)
    fun removeAttribute(name: String?) {
        element.removeAttribute(name)
    }

    @Override
    fun hasAttribute(name: String?): Boolean {
        return element.hasAttribute(name)
    }

    @Override
    fun getAttribute(name: String?): String? {
        return element.getAttribute(name)
    }

    @Override
    @Throws(DOMException::class)
    fun removeAttributeNS(namespaceURI: String?, localName: String?) {
        element.removeAttributeNS(namespaceURI, localName)
    }

    @Override
    @Throws(DOMException::class)
    fun setAttribute(name: String?, value: String?) {
        element.setAttribute(name, value)
    }

    @Override
    fun hasAttributeNS(namespaceURI: String?, localName: String?): Boolean {
        return element.hasAttributeNS(namespaceURI, localName)
    }

    @Override
    fun getAttributeNode(name: String?): Attr? {
        return element.getAttributeNode(name)
    }

    @Override
    @Throws(DOMException::class)
    fun removeAttributeNode(oldAttr: Attr?): Attr? {
        return element.removeAttributeNode(oldAttr)
    }

    @Override
    @Throws(DOMException::class)
    fun setAttributeNode(newAttr: Attr?): Attr? {
        return element.setAttributeNode(newAttr)
    }

    @Override
    @Throws(DOMException::class)
    fun setAttributeNodeNS(newAttr: Attr?): Attr? {
        return element.setAttributeNodeNS(newAttr)
    }

    @Override
    fun getElementsByTagName(name: String?): NodeList? {
        return element.getElementsByTagName(name)
    }

    @Override
    fun getAttributeNS(namespaceURI: String?, localName: String?): String? {
        return element.getAttributeNS(namespaceURI, localName)
    }

    @Override
    @Throws(DOMException::class)
    fun setAttributeNS(namespaceURI: String?, qualifiedName: String?, value: String?) {
        element.setAttributeNS(namespaceURI, qualifiedName, value)
    }

    @Override
    fun getAttributeNodeNS(namespaceURI: String?, localName: String?): Attr? {
        return element.getAttributeNodeNS(namespaceURI, localName)
    }

    @Override
    fun getElementsByTagNameNS(namespaceURI: String?, localName: String?): NodeList? {
        return element.getElementsByTagNameNS(namespaceURI, localName)
    }

    // used only with java 7, do not set @Override
    @Throws(DOMException::class)
    fun setIdAttribute(name: String?, isId: Boolean) {
        // dynamic load to support jre 1.4 and 1.5
        try {
            val m: Method = element.getClass().getMethod("setIdAttribute", arrayOf<Class?>(name.getClass(), Boolean::class.javaPrimitiveType))
            m.invoke(element, arrayOf(name, Caster.toBoolean(isId)))
        } catch (e: Exception) {
            throw PageRuntimeException(Caster.toPageException(e))
        }
    }

    // used only with java 7, do not set @Override
    @Throws(DOMException::class)
    fun setIdAttributeNS(namespaceURI: String?, localName: String?, isId: Boolean) {
        // dynamic load to support jre 1.4 and 1.5
        try {
            val m: Method = element.getClass().getMethod("setIdAttributeNS", arrayOf<Class?>(namespaceURI.getClass(), localName.getClass(), Boolean::class.javaPrimitiveType))
            m.invoke(element, arrayOf(namespaceURI, localName, Caster.toBoolean(isId)))
        } catch (e: Exception) {
            throw PageRuntimeException(Caster.toPageException(e))
        }
    }

    // used only with java 7, do not set @Override
    @Throws(DOMException::class)
    fun setIdAttributeNode(idAttr: Attr?, isId: Boolean) {
        // dynamic load to support jre 1.4 and 1.5
        try {
            val m: Method = element.getClass().getMethod("setIdAttributeNode", arrayOf<Class?>(idAttr.getClass(), Boolean::class.javaPrimitiveType))
            m.invoke(element, arrayOf<Object?>(idAttr, Caster.toBoolean(isId)))
        } catch (e: Exception) {
            element.setAttributeNodeNS(idAttr)
        }
    }// dynamic load to support jre 1.4 and 1.5

    // used only with java 7, do not set @Override
    val schemaTypeInfo: TypeInfo?
        get() =// dynamic load to support jre 1.4 and 1.5
            try {
                val m: Method = element.getClass().getMethod("getSchemaTypeInfo", arrayOf<Class?>())
                m.invoke(element, ArrayUtil.OBJECT_EMPTY) as TypeInfo
            } catch (e: Exception) {
                throw PageRuntimeException(Caster.toPageException(e))
            }

    /**
     * @return the element
     */
    fun getElement(): Element? {
        return element
    }

    @Override
    override fun duplicate(deepCopy: Boolean): Collection? {
        return XMLElementStruct(element.cloneNode(deepCopy) as Element, caseSensitive)
    }

    @Override
    override fun cloneNode(deep: Boolean): Node? {
        return XMLElementStruct(element.cloneNode(deep) as Element, caseSensitive)
    }

    /**
     * constructor of the class
     *
     * @param element
     * @param caseSensitive
     */
    init {
        this.element = element
    }
}