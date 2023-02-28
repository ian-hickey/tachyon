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
package lucee.runtime.text.xml.struct

import java.lang.reflect.Method

/**
 *
 */
class XMLNodeStruct(node: Node?, caseSensitive: Boolean) : StructSupport(), XMLStruct {
    private val node: Node?

    /**
     * @return Returns the caseSensitive.
     */
    @get:Override
    override var caseSensitive: Boolean
        protected set

    @Override
    @Throws(PageException::class)
    fun remove(key: Key?): Object? {
        val o: Object = XMLUtil.removeProperty(node, key, caseSensitive)
        if (o != null) return o
        throw ExpressionException("node has no child with name [$key]")
    }

    @Override
    fun removeEL(key: Key?): Object? {
        return XMLUtil.removeProperty(node, key, caseSensitive)
    }

    @Override
    @Throws(PageException::class)
    operator fun get(key: Collection.Key?): Object? {
        return get(null as PageContext?, key)
    }

    @Override
    @Throws(PageException::class)
    operator fun get(pc: PageContext?, key: Collection.Key?): Object? {
        return try {
            XMLUtil.getProperty(node, key, caseSensitive)
        } catch (e: SAXException) {
            throw XMLException(e)
        }
    }

    @Override
    @Throws(PageException::class)
    operator fun set(key: Collection.Key?, value: Object?): Object? {
        return XMLUtil.setProperty(node, key, value, caseSensitive)
    }// TODO ist das false hier ok?

    /**
     * @return retun the inner map
     */
    val map: Map<String?, Any?>?
        get() {
            val elements: NodeList = XMLUtil.getChildNodes(node, Node.ELEMENT_NODE, false, null) // TODO ist das false hier ok?
            val map: Map<String?, Node?> = MapFactory.< String, Node>getConcurrentMap<String?, Node?>()
            val len: Int = elements.getLength()
            for (i in 0 until len) {
                val node: Node = elements.item(i)
                map.put(node.getNodeName(), node)
            }
            return map
        }

    @Override
    fun duplicate(deepCopy: Boolean): Collection? {
        return XMLNodeStruct(node.cloneNode(deepCopy), caseSensitive)
    }

    @Override
    fun cloneNode(deep: Boolean): Node? {
        return XMLNodeStruct(node.cloneNode(deep), caseSensitive)
    }

    @get:Override
    val nodeType: Short
        get() = node.getNodeType()

    @Override
    fun normalize() {
        node.normalize()
    }

    @Override
    fun hasAttributes(): Boolean {
        return node.hasAttributes()
    }

    @Override
    fun hasChildNodes(): Boolean {
        return node.hasChildNodes()
    }

    @get:Override
    val localName: String?
        get() = node.getLocalName()

    @get:Override
    val namespaceURI: String?
        get() = node.getNamespaceURI()

    @get:Override
    val nodeName: String?
        get() = node.getNodeName()

    @get:Throws(DOMException::class)
    @get:Override
    @set:Throws(DOMException::class)
    @set:Override
    var nodeValue: String?
        get() = node.getNodeValue()
        set(nodeValue) {
            node.setNodeValue(nodeValue)
        }

    @get:Override
    @set:Throws(DOMException::class)
    @set:Override
    var prefix: String?
        get() = node.getPrefix()
        set(prefix) {
            node.setPrefix(prefix)
        }

    @get:Override
    val ownerDocument: Document?
        get() = if (node is Document) node as Document? else node.getOwnerDocument()

    @get:Override
    val attributes: NamedNodeMap?
        get() = XMLAttributes(node, caseSensitive)

    @get:Override
    val firstChild: Node?
        get() = node.getFirstChild()

    @get:Override
    val lastChild: Node?
        get() = node.getLastChild()

    @get:Override
    val nextSibling: Node?
        get() = node.getNextSibling()

    @get:Override
    val parentNode: Node?
        get() = node.getParentNode()

    @get:Override
    val previousSibling: Node?
        get() = node.getPreviousSibling()

    @get:Override
    val childNodes: NodeList?
        get() = node.getChildNodes()

    @Override
    fun isSupported(feature: String?, version: String?): Boolean {
        return node.isSupported(feature, version)
    }

    @Override
    @Throws(DOMException::class)
    fun appendChild(newChild: Node?): Node? {
        return node.appendChild(newChild)
    }

    @Override
    @Throws(DOMException::class)
    fun removeChild(oldChild: Node?): Node? {
        return node.removeChild(XMLCaster.toRawNode(oldChild))
    }

    @Override
    @Throws(DOMException::class)
    fun insertBefore(newChild: Node?, refChild: Node?): Node? {
        return node.insertBefore(newChild, refChild)
    }

    @Override
    @Throws(DOMException::class)
    fun replaceChild(newChild: Node?, oldChild: Node?): Node? {
        return node.replaceChild(XMLCaster.toRawNode(newChild), XMLCaster.toRawNode(oldChild))
    }

    @Override
    fun size(): Int {
        val list: NodeList = node.getChildNodes()
        val len: Int = list.getLength()
        var count = 0
        for (i in 0 until len) {
            if (list.item(i) is Element) count++
        }
        return count
    }

    @Override
    fun keys(): Array<Collection.Key?>? {
        val elements: NodeList = XMLUtil.getChildNodes(node, Node.ELEMENT_NODE, false, null) // TODO ist das false hie ok
        val arr: Array<Collection.Key?> = arrayOfNulls<Collection.Key?>(elements.getLength())
        for (i in arr.indices) {
            arr[i] = KeyImpl.init(elements.item(i).getNodeName())
        }
        return arr
    }

    @Override
    fun clear() {
        /*
		 * NodeList elements=XMLUtil.getChildNodes(node,Node.ELEMENT_NODE); int len=elements.getLength();
		 * for(int i=0;i<len;i++) { node.removeChild(elements.item(i)); }
		 */
    }

    @Override
    operator fun get(key: Collection.Key?, defaultValue: Object?): Object? {
        return XMLUtil.getProperty(node, key, caseSensitive, defaultValue)
    }

    @Override
    operator fun get(pc: PageContext?, key: Collection.Key?, defaultValue: Object?): Object? {
        return XMLUtil.getProperty(node, key, caseSensitive, defaultValue)
    }

    @Override
    fun setEL(key: Key?, value: Object?): Object? {
        return XMLUtil.setProperty(node, key, value, caseSensitive, null)
    }

    @Override
    fun keyIterator(): Iterator<Collection.Key?>? {
        return KeyIterator(keys())
    }

    @Override
    fun keysAsStringIterator(): Iterator<String?>? {
        return StringIterator(keys())
    }

    @Override
    fun entryIterator(): Iterator<Entry<Key?, Object?>?>? {
        return EntryIterator(this, keys())
    }

    @Override
    fun valueIterator(): Iterator<Object?>? {
        return ValueIterator(this, keys())
    }

    @Override
    fun toDumpData(pageContext: PageContext?, maxlevel: Int, dp: DumpProperties?): DumpData? {
        return DumpUtil.toDumpData(node, pageContext, maxlevel, dp)
    }

    @Override
    override fun toNode(): Node? {
        return node
    }

    @Override
    fun containsKey(key: Collection.Key?): Boolean {
        return get(key, null) != null
    }

    @Override
    fun containsKey(pc: PageContext?, key: Collection.Key?): Boolean {
        return get(key, null) != null
    }

    @get:Override
    override val xMLNodeList: XMLNodeList?
        get() = XMLNodeList(node, caseSensitive, Node.ELEMENT_NODE)

    @Override
    @Throws(PageException::class)
    fun castToString(): String? {
        return XMLCaster.toString(node)
    }

    @Override
    fun castToString(defaultValue: String?): String? {
        return XMLCaster.toString(node, defaultValue)
    }

    @Override
    @Throws(ExpressionException::class)
    fun castToBooleanValue(): Boolean {
        throw ExpressionException("Can't cast XML Node to a boolean value")
    }

    @Override
    fun castToBoolean(defaultValue: Boolean?): Boolean? {
        return defaultValue
    }

    @Override
    @Throws(ExpressionException::class)
    fun castToDoubleValue(): Double {
        throw ExpressionException("Can't cast XML Node to a number value")
    }

    @Override
    fun castToDoubleValue(defaultValue: Double): Double {
        return defaultValue
    }

    @Override
    @Throws(ExpressionException::class)
    fun castToDateTime(): DateTime? {
        throw ExpressionException("Can't cast XML Node to a Date")
    }

    @Override
    fun castToDateTime(defaultValue: DateTime?): DateTime? {
        return defaultValue
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(b: Boolean): Int {
        return OpUtil.compare(ThreadLocalPageContext.get(), castToString(), if (b) Boolean.TRUE else Boolean.FALSE)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(dt: DateTime?): Int {
        return OpUtil.compare(ThreadLocalPageContext.get(), castToString(), dt as Date?)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(d: Double): Int {
        return OpUtil.compare(ThreadLocalPageContext.get(), castToString(), Double.valueOf(d))
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(str: String?): Int {
        return OpUtil.compare(ThreadLocalPageContext.get(), castToString(), str)
    }// not supported

    // used only with java 7, do not set @Override
    @get:Override
    val baseURI: String?
        get() =// not supported
            null

    // used only with java 7, do not set @Override
    @Override
    @Throws(DOMException::class)
    fun compareDocumentPosition(other: Node?): Short {
        // not supported
        return -1
    }

    // used only with java 7, do not set @Override
    @Override
    fun isSameNode(other: Node?): Boolean {
        return this === other
    }

    // used only with java 7, do not set @Override
    @Override
    fun lookupPrefix(namespaceURI: String?): String? {
        // TODO not supported
        return null
    }

    // used only with java 7, do not set @Override
    @Override
    fun isDefaultNamespace(namespaceURI: String?): Boolean {
        // TODO not supported
        return false
    }

    // used only with java 7, do not set @Override
    @Override
    fun lookupNamespaceURI(prefix: String?): String? {
        // TODO not supported
        return null
    }

    // used only with java 7, do not set @Override
    @Override
    fun isEqualNode(node: Node?): Boolean {
        // TODO not supported
        return this === node
    }

    // used only with java 7, do not set @Override
    @Override
    fun getFeature(feature: String?, version: String?): Object? {
        // TODO not supported
        return null
    }

    // used only with java 7, do not set @Override
    @Override
    fun getUserData(key: String?): Object? {
        // dynamic load to support jre 1.4 and 1.5
        return try {
            val m: Method = node.getClass().getMethod("getUserData", arrayOf<Class?>(key.getClass()))
            m.invoke(node, arrayOf(key))
        } catch (e: Exception) {
            throw PageRuntimeException(Caster.toPageException(e))
        }
    }// dynamic load to support jre 1.4 and 1.5// TODO not supported

    // used only with java 7, do not set @Override
    // used only with java 7, do not set @Override
    @get:Throws(DOMException::class)
    @get:Override
    @set:Throws(DOMException::class)
    @set:Override
    var textContent: String?
        get() =// dynamic load to support jre 1.4 and 1.5
            try {
                val m: Method = node.getClass().getMethod("getTextContent", arrayOf<Class?>())
                Caster.toString(m.invoke(node, ArrayUtil.OBJECT_EMPTY))
            } catch (e: Exception) {
                throw PageRuntimeException(Caster.toPageException(e))
            }
        set(textContent) {
            // TODO not supported
            throw DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, "this method is not supported")
        }

    // used only with java 7, do not set @Override
    @Override
    fun setUserData(key: String?, data: Object?, handler: UserDataHandler?): Object? {
        // dynamic load to support jre 1.4 and 1.5
        return try {
            val m: Method = node.getClass().getMethod("setUserData", arrayOf<Class?>(key.getClass(), data.getClass(), handler.getClass()))
            m.invoke(node, arrayOf(key, data, handler))
        } catch (e: Exception) {
            throw PageRuntimeException(Caster.toPageException(e))
        }
    }

    @Override
    fun isCaseSensitive(): Boolean {
        return caseSensitive
    }

    @Override
    override fun equals(obj: Object?): Boolean {
        if (obj !is XMLNodeStruct) return super.equals(obj)
        val other = obj as XMLNodeStruct?
        return caseSensitive && other!!.node.equals(node).also { other.caseSensitive = it }
    }

    @get:Override
    val type: Int
        get() = Struct.TYPE_LINKED

    /**
     * constructor of the class
     *
     * @param node Node
     * @param caseSensitive
     */
    init {
        var node: Node? = node
        if (node is XMLStruct) node = (node as XMLStruct?)!!.toNode()
        this.node = node
        this.caseSensitive = caseSensitive
    }
}