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
package lucee.runtime.text.xml

import java.util.ArrayList

/**
 * represent a Struct and a NamedNodeMap
 */
class XMLAttributes(parent: Node?, caseSensitive: Boolean) : StructSupport(), Struct, NamedNodeMap {
    private val nodeMap: NamedNodeMap?
    private val owner: Document?
    private val parent: Node?
    private val caseSensitive: Boolean
    @Override
    fun toDumpData(pageContext: PageContext?, maxlevel: Int, dp: DumpProperties?): DumpData? {
        var maxlevel = maxlevel
        val keys: Array<Collection.Key?>? = keys()
        maxlevel--
        val table = DumpTable("xml", "#999966", "#cccc99", "#000000")
        table.setTitle("Struct (XML Attributes)")
        val maxkeys: Int = dp.getMaxKeys()
        var index = 0
        var k: Collection.Key?
        for (i in keys.indices) {
            k = keys!![i]
            if (DumpUtil.keyValid(dp, maxlevel, k)) {
                if (maxkeys <= index++) break
                table.appendRow(1, SimpleDumpData(k.getString()), DumpUtil.toDumpData(get(k.getString(), null), pageContext, maxlevel, dp))
            }
        }
        return table
    }

    @Override
    fun size(): Int {
        return nodeMap.getLength()
    }

    @Override
    fun keys(): Array<Collection.Key?>? {
        val len: Int = nodeMap.getLength()
        val list: ArrayList<Collection.Key?> = ArrayList<Collection.Key?>()
        for (i in 0 until len) {
            val item: Node = nodeMap.item(i)
            if (item is Attr) list.add(KeyImpl.init((item as Attr).getName()))
        }
        return list.toArray(arrayOfNulls<Collection.Key?>(list.size()))
    }

    @Override
    @Throws(PageException::class)
    fun remove(k: Collection.Key?): Object? {
        val key: String = k.getString()
        var rtn: Node? = null
        if (!caseSensitive) {
            val len: Int = nodeMap.getLength()
            var nn: String
            for (i in len - 1 downTo 0) {
                nn = nodeMap.item(i).getNodeName()
                if (key.equalsIgnoreCase(nn)) rtn = nodeMap.removeNamedItem(nn)
            }
        } else rtn = nodeMap.removeNamedItem(toName(key))
        if (rtn != null) return rtn.getNodeValue()
        throw ExpressionException("can't remove element with name [$key], element doesn't exist")
    }

    @Override
    fun removeEL(k: Collection.Key?): Object? {
        val key: String = k.getString()
        var rtn: Node? = null
        if (!caseSensitive) {
            val len: Int = nodeMap.getLength()
            var nn: String
            for (i in len - 1 downTo 0) {
                nn = nodeMap.item(i).getNodeName()
                if (key.equalsIgnoreCase(nn)) rtn = nodeMap.removeNamedItem(nn)
            }
        } else rtn = nodeMap.removeNamedItem(toName(key))
        return if (rtn != null) rtn.getNodeValue() else null
    }

    @Override
    fun clear() {
        val keys: Array<Collection.Key?>? = keys()
        for (i in keys.indices) {
            nodeMap.removeNamedItem(keys!![i].getString())
        }
    }

    @Override
    @Throws(ExpressionException::class)
    operator fun get(key: Collection.Key?): Object? {
        return get(null as PageContext?, key)
    }

    @Override
    @Throws(ExpressionException::class)
    operator fun get(pc: PageContext?, key: Collection.Key?): Object? {
        val rtn: Node = nodeMap.getNamedItem(key.getString())
        if (rtn != null) return rtn.getNodeValue()
        val keys: Array<Collection.Key?>? = keys()
        for (i in keys.indices) {
            if (key.equalsIgnoreCase(keys!![i])) return nodeMap.getNamedItem(keys[i].getString()).getNodeValue()
        }
        throw ExpressionException("No Attribute " + key.getString().toString() + " defined for tag", "attributes are [" + ListUtil.arrayToList(keys, ", ").toString() + "]")
    }

    @Override
    operator fun get(key: Collection.Key?, defaultValue: Object?): Object? {
        return get(null as PageContext?, key, defaultValue)
    }

    @Override
    operator fun get(pc: PageContext?, key: Collection.Key?, defaultValue: Object?): Object? {
        val rtn: Node = nodeMap.getNamedItem(key.getString())
        if (rtn != null) return rtn.getNodeValue()
        val keys: Array<Collection.Key?>? = keys()
        for (i in keys.indices) {
            if (key.equalsIgnoreCase(keys!![i])) return nodeMap.getNamedItem(keys[i].getString()).getNodeValue()
        }
        return defaultValue
    }

    @Override
    @Throws(PageException::class)
    operator fun set(key: Collection.Key?, value: Object?): Object? {
        if (owner == null) return value
        try {
            val attr: Attr = owner.createAttribute(toName(key.getString()))
            attr.setValue(Caster.toString(value))
            nodeMap.setNamedItem(attr)
        } catch (de: DOMException) {
            throw XMLException(de)
        }
        return value
    }

    private fun toName(name: String?): String? {
        return toName(name, name)
    }

    private fun toName(name: String?, defaultValue: String?): String? {
        if (caseSensitive) return name
        val n: Node = nodeMap.getNamedItem(name)
        if (n != null) return n.getNodeName()
        val len: Int = nodeMap.getLength()
        var nn: String
        for (i in 0 until len) {
            nn = nodeMap.item(i).getNodeName()
            if (name.equalsIgnoreCase(nn)) return nn
        }
        return defaultValue
    }

    @Override
    fun setEL(key: Collection.Key?, value: Object?): Object? {
        if (owner == null) return value
        try {
            val attr: Attr = owner.createAttribute(toName(key.getString()))
            attr.setValue(Caster.toString(value))
            nodeMap.setNamedItem(attr)
        } catch (e: Exception) {
            return null
        }
        return value
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

    @get:Override
    val length: Int
        get() = nodeMap.getLength()

    @Override
    fun item(index: Int): Node? {
        return nodeMap.item(index)
    }

    @Override
    fun getNamedItem(name: String?): Node? {
        return nodeMap.getNamedItem(name)
    }

    @Override
    @Throws(DOMException::class)
    fun removeNamedItem(name: String?): Node? {
        return nodeMap.removeNamedItem(name)
    }

    @Override
    @Throws(DOMException::class)
    fun setNamedItem(arg: Node?): Node? {
        return nodeMap.setNamedItem(arg)
    }

    @Override
    @Throws(DOMException::class)
    fun setNamedItemNS(arg: Node?): Node? {
        return nodeMap.setNamedItemNS(arg)
    }

    @Override
    fun getNamedItemNS(namespaceURI: String?, localName: String?): Node? {
        return nodeMap.getNamedItemNS(namespaceURI, localName)
    }

    @Override
    @Throws(DOMException::class)
    fun removeNamedItemNS(namespaceURI: String?, localName: String?): Node? {
        return nodeMap.removeNamedItemNS(namespaceURI, localName)
    }

    @Override
    fun duplicate(deepCopy: Boolean): Collection? {
        return XMLAttributes(parent.cloneNode(deepCopy), caseSensitive)
    }

    /**
     * @return returns named Node map
     */
    fun toNamedNodeMap(): NamedNodeMap? {
        return nodeMap
    }

    @Override
    fun containsKey(key: Collection.Key?): Boolean {
        return get(key, null) != null
    }

    @Override
    fun containsKey(pc: PageContext?, key: Collection.Key?): Boolean {
        return get(pc, key, null) != null
    }

    @Override
    @Throws(ExpressionException::class)
    fun castToString(): String? {
        throw ExpressionException("Can't cast XML NamedNodeMap to String")
    }

    @Override
    fun castToString(defaultValue: String?): String? {
        return defaultValue
    }

    @Override
    @Throws(ExpressionException::class)
    fun castToBooleanValue(): Boolean {
        throw ExpressionException("Can't cast XML NamedNodeMap to a boolean value")
    }

    @Override
    fun castToBoolean(defaultValue: Boolean?): Boolean? {
        return defaultValue
    }

    @Override
    @Throws(ExpressionException::class)
    fun castToDoubleValue(): Double {
        throw ExpressionException("Can't cast XML NamedNodeMap to a number value")
    }

    @Override
    fun castToDoubleValue(defaultValue: Double): Double {
        return defaultValue
    }

    @Override
    @Throws(ExpressionException::class)
    fun castToDateTime(): DateTime? {
        throw ExpressionException("Can't cast XML NamedNodeMap to a date value")
    }

    @Override
    fun castToDateTime(defaultValue: DateTime?): DateTime? {
        return defaultValue
    }

    @Override
    @Throws(ExpressionException::class)
    operator fun compareTo(b: Boolean): Int {
        throw ExpressionException("can't compare XML NamedNodeMap with a boolean value")
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(dt: DateTime?): Int {
        throw ExpressionException("can't compare XML NamedNodeMap with a DateTime Object")
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(d: Double): Int {
        throw ExpressionException("can't compare XML NamedNodeMap with a numeric value")
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(str: String?): Int {
        throw ExpressionException("can't compare XML NamedNodeMap with a String")
    }

    @get:Override
    val type: Int
        get() = Struct.TYPE_LINKED

    /**
     * constructor of the class (readonly)
     *
     * @param nodeMap
     */
    init {
        owner = XMLUtil.getDocument(parent)
        this.parent = parent
        nodeMap = parent.getAttributes()
        this.caseSensitive = caseSensitive
    }
}