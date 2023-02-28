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

import java.util.ArrayList

class XMLMultiElementArray(struct: XMLMultiElementStruct?) : ArraySupport(), Cloneable {
    private val struct: XMLMultiElementStruct?
    @Override
    @Throws(PageException::class)
    fun append(o: Object?): Object? {
        return setE(size() + 1, o)
    }

    @Override
    fun appendEL(o: Object?): Object? {
        return setEL(size() + 1, o)
    }

    @Override
    fun containsKey(key: Int): Boolean {
        return get(key, null) != null
    }

    @Override
    operator fun get(key: Int, defaultValue: Object?): Object? {
        return struct.get(key, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun getE(key: Int): Object? {
        return struct.get(key)
    }

    @get:Override
    val dimension: Int
        get() = struct.getInnerArray().getDimension()

    @Override
    @Throws(PageException::class)
    fun insert(index: Int, value: Object?): Boolean {
        val element: Element = XMLCaster.toElement(struct.getOwnerDocument(), value)
        val rtn: Boolean = struct.getInnerArray().insert(index, element)
        val obj: Object = struct.getInnerArray().get(index, null)
        if (obj is Element) {
            val el: Element = obj as Element
            el.getParentNode().insertBefore(XMLCaster.toRawNode(element), el)
        } else {
            struct.getParentNode().appendChild(XMLCaster.toRawNode(element))
        }
        return rtn
    }

    @Override
    fun intKeys(): IntArray? {
        return struct.getInnerArray().intKeys()
    }

    @Override
    @Throws(PageException::class)
    fun prepend(value: Object?): Object? {
        val element: Element = XMLCaster.toElement(struct.getOwnerDocument(), value)
        val obj: Object = struct.getInnerArray().get(1, null)
        if (obj is Element) {
            val el: Element = obj as Element
            el.getParentNode().insertBefore(XMLCaster.toRawNode(element), el)
        } else {
            struct.getParentNode().appendChild(XMLCaster.toRawNode(element))
        }
        return struct.getInnerArray().prepend(element)
    }

    @Override
    @Throws(PageException::class)
    fun removeE(key: Int): Object? {
        return struct.remove(key)
    }

    @Override
    fun removeEL(key: Int): Object? {
        return struct.removeEL(key)
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
    @Throws(PageException::class)
    fun resize(to: Int) {
        throw PageRuntimeException("resizing of xml nodelist not allowed")
    }

    @Override
    @Throws(PageException::class)
    fun setE(key: Int, value: Object?): Object? {
        return struct.set(key, value)
    }

    @Override
    fun setEL(key: Int, value: Object?): Object? {
        return struct.setEL(key, value)
    }

    @Override
    @Throws(PageException::class)
    fun sort(sortType: String?, sortOrder: String?) {
        if (size() <= 1) return
        struct.getInnerArray().sort(sortType, sortOrder)
        val nodes: Array<Object?> = struct.getInnerArray().toArray()
        var last: Node? = nodes[nodes.size - 1] as Node?
        var current: Node?
        val parent: Node = last.getParentNode()
        for (i in nodes.size - 2 downTo 0) {
            current = nodes[i] as Node?
            parent.insertBefore(current, last)
            last = current
        } // MUST testen
    }

    @Override
    fun sortIt(comp: Comparator?) {
        if (size() <= 1) return
        struct.getInnerArray().sortIt(comp)
        val nodes: Array<Object?> = struct.getInnerArray().toArray()
        var last: Node? = nodes[nodes.size - 1] as Node?
        var current: Node?
        val parent: Node = last.getParentNode()
        for (i in nodes.size - 2 downTo 0) {
            current = nodes[i] as Node?
            parent.insertBefore(current, last)
            last = current
        } // MUST testen
    }

    @Override
    fun toArray(): Array<Object?>? {
        return struct.getInnerArray().toArray()
    }

    fun toArrayList(): ArrayList? {
        return ArrayAsArrayList.toArrayList(this)
    }

    @Override
    fun clear() { // MUST
    }

    @Override
    fun containsKey(key: String?): Boolean {
        return struct!!.containsKey(key)
    }

    @Override
    fun containsKey(key: Key?): Boolean {
        return struct!!.containsKey(key)
    }

    @Override
    fun duplicate(deepCopy: Boolean): Collection? {
        return XMLMultiElementArray(Duplicator.duplicate(struct, deepCopy) as XMLMultiElementStruct)
    }

    @Override
    @Throws(PageException::class)
    operator fun get(key: String?): Object? {
        return struct!!.get(key)
    }

    @Override
    @Throws(PageException::class)
    operator fun get(key: Key?): Object? {
        return struct.get(key)
    }

    @Override
    @Throws(PageException::class)
    operator fun get(pc: PageContext?, key: Key?): Object? {
        return struct.get(key)
    }

    @Override
    operator fun get(key: String?, defaultValue: Object?): Object? {
        return struct.get(key, defaultValue)
    }

    @Override
    operator fun get(key: Key?, defaultValue: Object?): Object? {
        return struct.get(key, defaultValue)
    }

    @Override
    operator fun get(pc: PageContext?, key: Key?, defaultValue: Object?): Object? {
        return struct!!.get(pc, key, defaultValue)
    }

    @Override
    fun keys(): Array<Key?>? {
        return struct.getInnerArray().keys()
    }

    @Override
    @Throws(PageException::class)
    fun remove(key: Key?): Object? {
        return struct.remove(key)
    }

    @Override
    fun removeEL(key: Key?): Object? {
        return struct.removeEL(key)
    }

    @Override
    @Throws(PageException::class)
    operator fun set(key: String?, value: Object?): Object? {
        return struct!!.set(key, value)
    }

    @Override
    @Throws(PageException::class)
    operator fun set(key: Key?, value: Object?): Object? {
        return struct.set(key, value)
    }

    @Override
    fun setEL(key: String?, value: Object?): Object? {
        return struct!!.setEL(key, value)
    }

    @Override
    fun setEL(key: Key?, value: Object?): Object? {
        return struct.setEL(key, value)
    }

    @Override
    fun size(): Int {
        return struct.getInnerArray().size()
    }

    @Override
    fun toDumpData(pageContext: PageContext?, maxlevel: Int, dp: DumpProperties?): DumpData? {
        return struct.toDumpData(pageContext, maxlevel, dp)
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
    @Throws(PageException::class)
    fun castToBooleanValue(): Boolean {
        return struct.castToBooleanValue()
    }

    @Override
    fun castToBoolean(defaultValue: Boolean?): Boolean? {
        return struct.castToBoolean(defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun castToDateTime(): DateTime? {
        return struct.castToDateTime()
    }

    @Override
    fun castToDateTime(defaultValue: DateTime?): DateTime? {
        return struct.castToDateTime(defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun castToDoubleValue(): Double {
        return struct.castToDoubleValue()
    }

    @Override
    fun castToDoubleValue(defaultValue: Double): Double {
        return struct.castToDoubleValue(defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun castToString(): String? {
        return struct.castToString()
    }

    @Override
    fun castToString(defaultValue: String?): String? {
        return struct.castToString(defaultValue)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(str: String?): Int {
        return struct.compareTo(str)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(b: Boolean): Int {
        return struct.compareTo(b)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(d: Double): Int {
        return struct.compareTo(d)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(dt: DateTime?): Int {
        return struct.compareTo(dt)
    }

    @Override
    fun clone(): Object {
        return duplicate(true)
    }

    @Override
    fun add(o: Object?): Boolean {
        return false
    }

    companion object {
        private const val serialVersionUID = -2673749147723742450L
    }

    init {
        this.struct = struct
    }
}