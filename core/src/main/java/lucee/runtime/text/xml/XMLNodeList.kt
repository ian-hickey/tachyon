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

import java.util.ArrayList

/**
 *
 */
class XMLNodeList(parent: Node?, caseSensitive: Boolean, private val type: Short, private val filter: String?) : ArraySupport(), NodeList, XMLObject, Cloneable {
    @get:Override
    val caseSensitive = false
    private val doc: Document?
    private val parent: Node? = null

    /**
     * @param parent Parent Node
     * @param caseSensitive
     */
    constructor(parent: Node?, caseSensitive: Boolean, type: Short) : this(parent, caseSensitive, type, null) {}

    @get:Override
    val length: Int
        get() = XMLUtil.childNodesLength(parent, type, caseSensitive, filter)

    @Override
    fun item(index: Int): Node? {
        return XMLCaster.toXMLStruct(getChildNode(index), caseSensitive)
    }

    @Override
    fun size(): Int {
        return length
    }

    @Override
    fun keys(): Array<Collection.Key?>? {
        val keys: Array<Collection.Key?> = arrayOfNulls<Collection.Key?>(length)
        for (i in 1..keys.size) {
            keys[i - 1] = KeyImpl.init(i.toString() + "")
        }
        return keys
    }

    @Override
    fun intKeys(): IntArray? {
        val keys = IntArray(length)
        for (i in 1..keys.size) {
            keys[i - 1] = i
        }
        return keys
    }

    @Override
    fun removeEL(key: Collection.Key?): Object? {
        return removeEL(Caster.toIntValue(key.getString(), -1))
    }

    @Override
    @Throws(PageException::class)
    fun remove(key: Collection.Key?): Object? {
        return removeE(Caster.toIntValue(key.getString()))
    }

    @Override
    fun removeEL(index: Int): Object? {
        val len = size()
        return if (index < 1 || index > len) null else try {
            XMLCaster.toXMLStruct(parent.removeChild(XMLCaster.toRawNode(item(index - 1))), caseSensitive)
        } catch (e: Exception) {
            null
        }
    }

    @Override
    @Throws(PageException::class)
    fun removeE(index: Int): Object? {
        val len = size()
        if (index < 1 || index > len) throw ExpressionException("can't remove value form XML Node List at index $index, valid indexes goes from 1 to $len")
        return XMLCaster.toXMLStruct(parent.removeChild(XMLCaster.toRawNode(item(index - 1))), caseSensitive)
    }

    @Override
    @Throws(PageException::class)
    fun pop(): Object? {
        return removeE(size())
    }

    @Override
    fun pop(defaultValue: Object?): Object? {
        return try {
            removeE(size())
        } catch (e: Exception) {
            defaultValue
        }
    }

    @Override
    @Throws(PageException::class)
    fun shift(): Object? {
        return removeE(1)
    }

    @Override
    fun shift(defaultValue: Object?): Object? {
        return try {
            removeE(1)
        } catch (e: Exception) {
            defaultValue
        }
    }

    @Override
    fun clear() {
        val nodes: Array<Node?>? = childNodesAsArray
        for (i in nodes.indices) {
            parent.removeChild(XMLCaster.toRawNode(nodes!![i]))
        }
    }

    @Override
    @Throws(ExpressionException::class)
    operator fun get(key: String?): Object? {
        return getE(Caster.toIntValue(key))
    }

    @Override
    @Throws(ExpressionException::class)
    operator fun get(key: Collection.Key?): Object? {
        return get(key.getString())
    }

    @Override
    @Throws(ExpressionException::class)
    operator fun get(pc: PageContext?, key: Collection.Key?): Object? {
        return get(key.getString())
    }

    @Override
    @Throws(ExpressionException::class)
    fun getE(key: Int): Object? {
        return getE(null, key)
    }

    @Throws(ExpressionException::class)
    fun getE(pc: PageContext?, key: Int): Object? {
        return item(key - 1)
                ?: throw ExpressionException("invalid index [" + key + "] for XML Node List , indexes goes from [0-" + size() + "]")
    }

    @Override
    operator fun get(key: String?, defaultValue: Object?): Object? {
        val index: Int = Caster.toIntValue(key, Integer.MIN_VALUE)
        return if (index == Integer.MIN_VALUE) defaultValue else get(index, defaultValue)
    }

    @Override
    operator fun get(key: Collection.Key?, defaultValue: Object?): Object? {
        return get(key.getString(), defaultValue)
    }

    @Override
    operator fun get(pc: PageContext?, key: Collection.Key?, defaultValue: Object?): Object? {
        return get(key.getString(), defaultValue)
    }

    @Override
    operator fun get(key: Int, defaultValue: Object?): Object? {
        return item(key - 1) ?: return defaultValue
    }

    @Override
    @Throws(PageException::class)
    operator fun set(key: String?, value: Object?): Object? {
        return setE(Caster.toIntValue(key), value)
    }

    @Override
    @Throws(PageException::class)
    operator fun set(key: Collection.Key?, value: Object?): Object? {
        return set(key.getString(), value)
    }

    @Override
    @Throws(PageException::class)
    fun setE(index: Int, value: Object?): Object? {
        // check min Index
        if (index < 1) throw ExpressionException("invalid index [$index] to set a child node, valid indexes start at 1")
        val nodes: Array<Node?>? = childNodesAsArray

        // if index Greater len append
        if (index > nodes!!.size) return append(value)

        // remove all children
        clear()

        // set all children before new Element
        for (i in 1 until index) {
            append(nodes[i - 1])
        }

        // set new Element
        append(XMLCaster.toNode(doc, value, true))

        // set all after new Element
        for (i in index until nodes.size) {
            append(nodes[i])
        }
        return value
    }

    @Override
    fun setEL(key: String?, value: Object?): Object? {
        val index: Int = Caster.toIntValue(key, Integer.MIN_VALUE)
        return if (index == Integer.MIN_VALUE) null else setEL(index, value)
    }

    @Override
    fun setEL(key: Collection.Key?, value: Object?): Object? {
        return setEL(key.getString(), value)
    }

    @Override
    fun setEL(index: Int, value: Object?): Object? {
        return try {
            setE(index, value)
        } catch (e: PageException) {
            null
        }
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
        val values: Array<Object?> = arrayOfNulls<Object?>(length)
        for (i in values.indices) {
            values[i] = item(i)
        }
        return ArrayIterator(values)
    }

    @Override
    fun toDumpData(pageContext: PageContext?, maxlevel: Int, dp: DumpProperties?): DumpData? {
        var maxlevel = maxlevel
        maxlevel--
        val table = DumpTable("xml", "#cc9999", "#ffffff", "#000000")
        table.setTitle("Array (XML Node List)")
        val len = size()
        for (i in 1..len) {
            table.appendRow(1, SimpleDumpData(i), DumpUtil.toDumpData(item(i - 1), pageContext, maxlevel, dp))
        }
        return table
    }

    @Override
    @Throws(PageException::class)
    fun append(o: Object?): Object? {
        return parent.appendChild(XMLCaster.toNode(doc, o, true))
    }

    @Override
    fun appendEL(o: Object?): Object? {
        return try {
            append(o)
        } catch (e: Exception) {
            null
        }
    }

    @Override
    fun clone(): Object {
        return duplicate(true)
    }

    @Override
    fun duplicate(deepCopy: Boolean): Collection? {
        return XMLNodeList(parent.cloneNode(deepCopy), caseSensitive, type)
    }

    @get:Override
    val dimension: Int
        get() = 1

    @Override
    @Throws(PageException::class)
    fun insert(index: Int, value: Object?): Boolean {
        // check min Index
        if (index < 1) throw ExpressionException("invalid index [$index] to insert a child node, valid indexes start at 1")
        val nodes: Array<Node?>? = childNodesAsArray

        // if index Greater len append
        if (index > nodes!!.size) {
            append(value)
            return true
        }

        // remove all children
        clear()

        // set all children before new Element
        for (i in 1 until index) {
            append(nodes[i - 1])
        }

        // set new Element
        append(XMLCaster.toNode(doc, value, true))

        // set all after new Element
        for (i in index..nodes.size) {
            append(nodes[i - 1])
        }
        return true
    }

    @Override
    @Throws(PageException::class)
    fun prepend(o: Object?): Object? {
        val nodes: Array<Node?>? = childNodesAsArray

        // remove all children
        clear()

        // set new Element
        append(XMLCaster.toNode(doc, o, true))

        // set all after new Element
        for (i in nodes.indices) {
            append(nodes!![i])
        }
        return o
    }

    @Override
    @Throws(ExpressionException::class)
    fun resize(to: Int) {
        if (to > size()) throw ExpressionException("can't resize a XML Node List Array with empty Elements")
    }

    @Override
    @Throws(ExpressionException::class)
    fun sort(sortType: String?, sortOrder: String?) {
        throw ExpressionException("can't sort a XML Node List Array", "sorttype:$sortType;sortorder:$sortOrder")
    }

    @Override
    fun sortIt(comp: Comparator?) {
        throw PageRuntimeException("can't sort a XML Node List Array")
    }

    @Override
    fun toArray(): Array<Object?>? {
        return childNodesAsArray
    }

    fun toArrayList(): ArrayList? {
        val arr: Array<Object?>? = toArray()
        val list = ArrayList()
        var i = 0
        while (i > arr!!.size) {
            list.add(arr[i])
            i++
        }
        return list
    }

    /**
     * @return returns an output from the content as plain Text
     */
    fun toPlain(): String? {
        val sb = StringBuffer()
        val length = size()
        for (i in 1..length) {
            sb.append(i)
            sb.append(": ")
            sb.append(get(i, null))
            sb.append("\n")
        }
        return sb.toString()
    }

    private fun getChildNode(index: Int): Node? {
        return XMLUtil.getChildNode(parent, type, caseSensitive, filter, index)
    }

    private val childNodesAsArray: Array<Any?>?
        private get() = XMLUtil.getChildNodesAsArray(parent, type, caseSensitive, filter)

    @Override
    fun containsKey(key: String?): Boolean {
        return get(key, null) != null
    }

    @Override
    fun containsKey(key: Collection.Key?): Boolean {
        return get(key, null) != null
    }

    @Override
    fun containsKey(key: Int): Boolean {
        return get(key, null) != null
    }

    @Override
    @Throws(ExpressionException::class)
    fun castToString(): String? {
        throw ExpressionException("Can't cast XML NodeList to String")
    }

    @Override
    fun castToString(defaultValue: String?): String? {
        return defaultValue
    }

    @Override
    @Throws(ExpressionException::class)
    fun castToBooleanValue(): Boolean {
        throw ExpressionException("Can't cast XML NodeList to a boolean value")
    }

    @Override
    fun castToBoolean(defaultValue: Boolean?): Boolean? {
        return defaultValue
    }

    @Override
    @Throws(ExpressionException::class)
    fun castToDoubleValue(): Double {
        throw ExpressionException("Can't cast XML NodeList to a number value")
    }

    @Override
    fun castToDoubleValue(defaultValue: Double): Double {
        return defaultValue
    }

    @Override
    @Throws(ExpressionException::class)
    fun castToDateTime(): DateTime? {
        throw ExpressionException("Can't cast XML NodeList to a Date")
    }

    @Override
    fun castToDateTime(defaultValue: DateTime?): DateTime? {
        return defaultValue
    }

    @Override
    @Throws(ExpressionException::class)
    operator fun compareTo(b: Boolean): Int {
        throw ExpressionException("can't compare XML NodeList with a boolean value")
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(dt: DateTime?): Int {
        throw ExpressionException("can't compare XML NodeList with a DateTime Object")
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(d: Double): Int {
        throw ExpressionException("can't compare XML NodeList with a numeric value")
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(str: String?): Int {
        throw ExpressionException("can't compare XML NodeList with a String")
    }

    init {
        if (parent is XMLStruct) {
            val xmlNode: XMLStruct? = parent as XMLStruct?
            this.parent = xmlNode.toNode()
            this.caseSensitive = xmlNode.getCaseSensitive()
        } else {
            this.parent = parent
            this.caseSensitive = caseSensitive
        }
        doc = XMLUtil.getDocument(this.parent)
    }
}