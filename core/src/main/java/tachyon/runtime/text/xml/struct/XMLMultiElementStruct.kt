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

import org.w3c.dom.Element

/**
 * Element that can contain more than one Element
 */
class XMLMultiElementStruct(array: Array?, caseSensitive: Boolean) : XMLElementStruct(getFirstRaw(array), caseSensitive) {
    private val array: Array?

    @Override
    override fun removeEL(key: Collection.Key?): Object? {
        val index: Int = Caster.toIntValue(key.getString(), Integer.MIN_VALUE)
        return if (index == Integer.MIN_VALUE) super.removeEL(key) else removeEL(index)
    }

    override fun removeEL(index: Int): Object? {
        val o: Object = array.removeEL(index)
        if (o is Element) {
            val el: Element = o as Element
            // try {
            val n: Node = XMLCaster.toRawNode(el)
            el.getParentNode().removeChild(n)
            // } catch (PageException e) {}
        }
        return o
    }

    @Override
    @Throws(PageException::class)
    override fun remove(key: Collection.Key?): Object? {
        val index: Int = Caster.toIntValue(key.getString(), Integer.MIN_VALUE)
        return if (index == Integer.MIN_VALUE) super.remove(key) else remove(index)
    }

    @Throws(PageException::class)
    override fun remove(index: Int): Object? {
        val o: Object = array.removeE(index)
        if (o is Element) {
            val el: Element = o as Element
            el.getParentNode().removeChild(XMLCaster.toRawNode(el))
        }
        return o
    }

    @Override
    @Throws(PageException::class)
    override operator fun get(key: Collection.Key?): Object? {
        val index: Int = Caster.toIntValue(key.getString(), Integer.MIN_VALUE)
        return if (index == Integer.MIN_VALUE) super.get(key) else get(index)
    }

    @Override
    @Throws(PageException::class)
    override operator fun get(pc: PageContext?, key: Collection.Key?): Object? {
        val index: Int = Caster.toIntValue(key.getString(), Integer.MIN_VALUE)
        return if (index == Integer.MIN_VALUE) super.get(pc, key) else get(index)
    }

    @Throws(PageException::class)
    override operator fun get(index: Int): Object? {
        return array.getE(index)
    }

    @Override
    override operator fun get(key: Collection.Key?, defaultValue: Object?): Object? {
        return get(null as PageContext?, key, defaultValue)
    }

    @Override
    override operator fun get(pc: PageContext?, key: Collection.Key?, defaultValue: Object?): Object? {
        val index: Int = Caster.toIntValue(key.getString(), Integer.MIN_VALUE)
        return if (index == Integer.MIN_VALUE) super.get(key, defaultValue) else get(index, defaultValue)
    }

    override operator fun get(index: Int, defaultValue: Object?): Object? {
        return array.get(index, defaultValue)
    }

    @Override
    override fun setEL(key: Collection.Key?, value: Object?): Object? {
        return try {
            set(key, value)
        } catch (e1: PageException) {
            null
        }
    }

    /**
     * @param index
     * @param value
     * @return
     */
    override fun setEL(index: Int, value: Object?): Object? {
        return try {
            set(index, value)
        } catch (e: PageException) {
            null
        }
    }

    @Override
    @Throws(PageException::class)
    override operator fun set(key: Collection.Key?, value: Object?): Object? {
        val index: Int = Caster.toIntValue(key.getString(), Integer.MIN_VALUE)
        return if (index == Integer.MIN_VALUE) {
            super.set(key, value)
        } else set(index, value)
    }

    @Throws(PageException::class)
    override operator fun set(index: Int, value: Object?): Object? {
        val element: Element = XMLCaster.toElement(getOwnerDocument(), value)
        val obj: Object = array.get(index, null)
        if (obj is Element) {
            val el: Element = obj as Element
            el.getParentNode().replaceChild(XMLCaster.toRawNode(element), XMLCaster.toRawNode(el))
        } else if (array.size() + 1 === index) {
            getParentNode().appendChild(XMLCaster.toRawNode(element))
        } else {
            throw ExpressionException("The index for child node is out of range", "valid range is from 1 to " + (array.size() + 1))
        }
        return array.setE(index, element)
    }

    @Override
    override fun containsKey(key: Collection.Key?): Boolean {
        return get(key, null) != null
    }

    @Override
    override fun containsKey(pc: PageContext?, key: Collection.Key?): Boolean {
        return get(pc, key, null) != null
    }

    val innerArray: Array?
        get() = array

    @Override
    override fun duplicate(deepCopy: Boolean): Collection? {
        return try {
            XMLMultiElementStruct(Duplicator.duplicate(array, deepCopy) as Array, getCaseSensitive())
        } catch (e: PageException) {
            null
        }
    }

    @Override
    override fun cloneNode(deep: Boolean): Node? {
        return try {
            XMLMultiElementStruct(Duplicator.duplicate(array, deep) as Array, getCaseSensitive())
        } catch (e: PageException) {
            null
        }
    }

    companion object {
        private const val serialVersionUID = -4921231279765525776L
        @Throws(PageException::class)
        private fun getFirstRaw(array: Array?): Element? {
            if (array.size() === 0) throw ExpressionException("Array must have one Element at least")
            var el: Element = array.getE(1) as Element
            if (el is XMLElementStruct) el = XMLCaster.toRawNode((el as XMLElementStruct)!!.getElement()) as Element
            return el
            // return (Element)XMLCaster.toRawNode(array.getE(1));
        }
    }

    /**
     * Constructor of the class
     *
     * @param array
     * @param caseSensitive
     * @throws PageException
     */
    init {
        this.array = array
        if (array.size() === 0) throw ExpressionException("Array must have one Element at least")
        val ints: IntArray = array.intKeys()
        for (i in ints.indices) {
            val o: Object = array.get(ints[i], null) as? Element
                    ?: throw ExpressionException("All Elements in the Array must be of type Element")
        }
    }
}