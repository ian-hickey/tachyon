/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Tachyon Assosication Switzerland
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

import java.lang.reflect.Method

/**
 *
 */
class XMLDocumentStruct(doc: Document?, caseSensitive: Boolean) : XMLNodeStruct(doc, caseSensitive), Document {
    private val doc: Document?

    @get:Override
    val implementation: DOMImplementation?
        get() = doc.getImplementation()

    @Override
    fun createDocumentFragment(): DocumentFragment? {
        return doc.createDocumentFragment()
    }

    @get:Override
    val doctype: DocumentType?
        get() = doc.getDoctype()

    @get:Override
    val documentElement: Element?
        get() = doc.getDocumentElement()

    @Override
    @Throws(DOMException::class)
    fun createAttribute(name: String?): Attr? {
        return doc.createAttribute(name)
    }

    @Override
    @Throws(DOMException::class)
    fun createCDATASection(data: String?): CDATASection? {
        return doc.createCDATASection(data)
    }

    @Override
    fun createComment(data: String?): Comment? {
        return doc.createComment(data)
    }

    @Override
    @Throws(DOMException::class)
    fun createElement(tagName: String?): Element? {
        return doc.createElement(tagName)
    }

    @Override
    fun getElementById(elementId: String?): Element? {
        return doc.getElementById(elementId)
    }

    @Override
    @Throws(DOMException::class)
    fun createEntityReference(name: String?): EntityReference? {
        return doc.createEntityReference(name)
    }

    @Override
    @Throws(DOMException::class)
    fun importNode(importedNode: Node?, deep: Boolean): Node? {
        return doc.importNode(importedNode, deep)
    }

    @Override
    fun getElementsByTagName(tagname: String?): NodeList? {
        return doc.getElementsByTagName(tagname)
    }

    @Override
    fun createTextNode(data: String?): Text? {
        return doc.createTextNode(data)
    }

    @Override
    @Throws(DOMException::class)
    fun createAttributeNS(namespaceURI: String?, qualifiedName: String?): Attr? {
        return doc.createAttributeNS(namespaceURI, qualifiedName)
    }

    @Override
    @Throws(DOMException::class)
    fun createElementNS(namespaceURI: String?, qualifiedName: String?): Element? {
        return doc.createElementNS(namespaceURI, qualifiedName)
    }

    @Override
    fun getElementsByTagNameNS(namespaceURI: String?, localName: String?): NodeList? {
        return doc.getElementsByTagNameNS(namespaceURI, localName)
    }

    @Override
    @Throws(DOMException::class)
    fun createProcessingInstruction(target: String?, data: String?): ProcessingInstruction? {
        if (StringUtil.isEmpty(target)) throw RuntimeException("target is empty/null")
        if (StringUtil.isEmpty(target)) throw RuntimeException("data is empty/null")
        return doc.createProcessingInstruction(target, data)
    }

    // used only with java 7, do not set @Override
    @Override
    @Throws(DOMException::class)
    fun adoptNode(arg0: Node?): Node? {
        // dynamic load to support jre 1.4 and 1.5
        return try {
            val m: Method = doc.getClass().getMethod("adoptNode", arrayOf<Class?>(arg0.getClass()))
            Caster.toNode(m.invoke(doc, arrayOf<Object?>(arg0)))
        } catch (e: Exception) {
            throw PageRuntimeException(Caster.toPageException(e))
        }
    }// dynamic load to support jre 1.4 and 1.5// dynamic load to support jre 1.4 and 1.5

    // used only with java 7, do not set @Override
    // used only with java 7, do not set @Override
    @get:Override
    @set:Override
    var documentURI: String?
        get() =// dynamic load to support jre 1.4 and 1.5
            try {
                val m: Method = doc.getClass().getMethod("getDocumentURI", arrayOf<Class?>())
                Caster.toString(m.invoke(doc, ArrayUtil.OBJECT_EMPTY))
            } catch (e: Exception) {
                throw PageRuntimeException(Caster.toPageException(e))
            }
        set(arg0) {
            // dynamic load to support jre 1.4 and 1.5
            try {
                val m: Method = doc.getClass().getMethod("setDocumentURI", arrayOf<Class?>(arg0.getClass()))
                m.invoke(doc, arrayOf(arg0))
            } catch (e: Exception) {
                throw PageRuntimeException(Caster.toPageException(e))
            }
        }// dynamic load to support jre 1.4 and 1.5

    // used only with java 7, do not set @Override
    @get:Override
    val domConfig: DOMConfiguration?
        get() =// dynamic load to support jre 1.4 and 1.5
            try {
                val m: Method = doc.getClass().getMethod("getDomConfig", arrayOf<Class?>())
                m.invoke(doc, ArrayUtil.OBJECT_EMPTY) as DOMConfiguration
            } catch (e: Exception) {
                throw PageRuntimeException(Caster.toPageException(e))
            }// dynamic load to support jre 1.4 and 1.5

    // used only with java 7, do not set @Override
    @get:Override
    val inputEncoding: String?
        get() =// dynamic load to support jre 1.4 and 1.5
            try {
                val m: Method = doc.getClass().getMethod("getInputEncoding", arrayOf<Class?>())
                Caster.toString(m.invoke(doc, ArrayUtil.OBJECT_EMPTY))
            } catch (e: Exception) {
                throw PageRuntimeException(Caster.toPageException(e))
            }// dynamic load to support jre 1.4 and 1.5// dynamic load to support jre 1.4 and 1.5

    // used only with java 7, do not set @Override
    // used only with java 7, do not set @Override
    @get:Override
    @set:Override
    var strictErrorChecking: Boolean
        get() =// dynamic load to support jre 1.4 and 1.5
            try {
                val m: Method = doc.getClass().getMethod("getStrictErrorChecking", arrayOf<Class?>())
                Caster.toBooleanValue(m.invoke(doc, ArrayUtil.OBJECT_EMPTY))
            } catch (e: Exception) {
                throw PageRuntimeException(Caster.toPageException(e))
            }
        set(arg0) {
            // dynamic load to support jre 1.4 and 1.5
            try {
                val m: Method = doc.getClass().getMethod("setStrictErrorChecking", arrayOf<Class?>(Boolean::class.javaPrimitiveType))
                m.invoke(doc, arrayOf<Object?>(Caster.toBoolean(arg0)))
            } catch (e: Exception) {
                throw PageRuntimeException(Caster.toPageException(e))
            }
        }// dynamic load to support jre 1.4 and 1.5

    // used only with java 7, do not set @Override
    @get:Override
    val xmlEncoding: String?
        get() =// dynamic load to support jre 1.4 and 1.5
            try {
                val m: Method = doc.getClass().getMethod("getXmlEncoding", arrayOf<Class?>())
                Caster.toString(m.invoke(doc, ArrayUtil.OBJECT_EMPTY))
            } catch (e: Exception) {
                throw PageRuntimeException(Caster.toPageException(e))
            }// dynamic load to support jre 1.4 and 1.5// dynamic load to support jre 1.4 and 1.5

    // used only with java 7, do not set @Override
    // used only with java 7, do not set @Override
    @get:Override
    @set:Throws(DOMException::class)
    @set:Override
    var xmlStandalone: Boolean
        get() =// dynamic load to support jre 1.4 and 1.5
            try {
                val m: Method = doc.getClass().getMethod("getXmlStandalone", arrayOf<Class?>())
                Caster.toBooleanValue(m.invoke(doc, ArrayUtil.OBJECT_EMPTY))
            } catch (e: Exception) {
                throw PageRuntimeException(Caster.toPageException(e))
            }
        set(arg0) {
            // dynamic load to support jre 1.4 and 1.5
            try {
                val m: Method = doc.getClass().getMethod("setXmlStandalone", arrayOf<Class?>(Boolean::class.javaPrimitiveType))
                m.invoke(doc, arrayOf<Object?>(Caster.toBoolean(arg0)))
            } catch (e: Exception) {
                throw PageRuntimeException(Caster.toPageException(e))
            }
        }// dynamic load to support jre 1.4 and 1.5// dynamic load to support jre 1.4 and 1.5

    // used only with java 7, do not set @Override
    // used only with java 7, do not set @Override
    @get:Override
    @set:Throws(DOMException::class)
    @set:Override
    var xmlVersion: String?
        get() =// dynamic load to support jre 1.4 and 1.5
            try {
                val m: Method = doc.getClass().getMethod("getXmlVersion", arrayOf<Class?>())
                Caster.toString(m.invoke(doc, ArrayUtil.OBJECT_EMPTY))
            } catch (e: Exception) {
                throw PageRuntimeException(Caster.toPageException(e))
            }
        set(arg0) {
            // dynamic load to support jre 1.4 and 1.5
            try {
                val m: Method = doc.getClass().getMethod("setXmlVersion", arrayOf<Class?>(arg0.getClass()))
                m.invoke(doc, arrayOf(arg0))
            } catch (e: Exception) {
                throw PageRuntimeException(Caster.toPageException(e))
            }
        }

    // used only with java 7, do not set @Override
    @Override
    fun normalizeDocument() {
        // dynamic load to support jre 1.4 and 1.5
        try {
            val m: Method = doc.getClass().getMethod("normalizeDocument", arrayOf<Class?>())
            m.invoke(doc, ArrayUtil.OBJECT_EMPTY)
        } catch (e: Exception) {
            throw PageRuntimeException(Caster.toPageException(e))
        }
    }

    // used only with java 7, do not set @Override
    @Override
    @Throws(DOMException::class)
    fun renameNode(arg0: Node?, arg1: String?, arg2: String?): Node? {
        // dynamic load to support jre 1.4 and 1.5
        return try {
            val m: Method = doc.getClass().getMethod("renameNode", arrayOf<Class?>(arg0.getClass(), arg1.getClass(), arg2.getClass()))
            Caster.toNode(m.invoke(doc, arrayOf(arg0, arg1, arg2)))
        } catch (e: Exception) {
            throw PageRuntimeException(Caster.toPageException(e))
        }
    }

    @Override
    override fun duplicate(deepCopy: Boolean): Collection? {
        return XMLDocumentStruct(doc.cloneNode(deepCopy) as Document, caseSensitive)
    }

    @Override
    override fun cloneNode(deep: Boolean): Node? {
        return XMLDocumentStruct(doc.cloneNode(deep) as Document, caseSensitive)
    }

    /**
     * @param doc
     * @param caseSensitive
     */
    init {
        this.doc = doc
    }
}