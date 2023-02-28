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
class XMLCDATASectionStruct(section: CDATASection?, caseSensitive: Boolean) : XMLNodeStruct(section, caseSensitive), CDATASection {
    private val section: CDATASection?
    @Override
    @Throws(DOMException::class)
    fun splitText(offset: Int): Text? {
        return section.splitText(offset)
    }

    @get:Override
    val length: Int
        get() = section.getLength()

    @Override
    @Throws(DOMException::class)
    fun deleteData(offset: Int, count: Int) {
        section.deleteData(offset, count)
    }

    @get:Throws(DOMException::class)
    @get:Override
    @set:Throws(DOMException::class)
    @set:Override
    var data: String?
        get() = section.getData()
        set(data) {
            section.setData(data)
        }

    @Override
    @Throws(DOMException::class)
    fun substringData(offset: Int, count: Int): String? {
        return section.substringData(offset, count)
    }

    @Override
    @Throws(DOMException::class)
    fun replaceData(offset: Int, count: Int, arg: String?) {
        section.replaceData(offset, count, arg)
    }

    @Override
    @Throws(DOMException::class)
    fun insertData(offset: Int, arg: String?) {
        section.insertData(offset, arg)
    }

    @Override
    @Throws(DOMException::class)
    fun appendData(arg: String?) {
        section.appendData(arg)
    }// dynamic load to support jre 1.4 and 1.5

    // used only with java 7, do not set @Override
    val wholeText: String?
        get() =// dynamic load to support jre 1.4 and 1.5
            try {
                val m: Method = section.getClass().getMethod("getWholeText", arrayOf<Class?>())
                Caster.toString(m.invoke(section, ArrayUtil.OBJECT_EMPTY))
            } catch (e: Exception) {
                throw PageRuntimeException(Caster.toPageException(e))
            }// dynamic load to support jre 1.4 and 1.5

    // used only with java 7, do not set @Override
    val isElementContentWhitespace: Boolean
        get() =// dynamic load to support jre 1.4 and 1.5
            try {
                val m: Method = section.getClass().getMethod("isElementContentWhitespace", arrayOf<Class?>())
                Caster.toBooleanValue(m.invoke(section, ArrayUtil.OBJECT_EMPTY))
            } catch (e: Exception) {
                throw PageRuntimeException(Caster.toPageException(e))
            }

    // used only with java 7, do not set @Override
    @Throws(DOMException::class)
    fun replaceWholeText(arg0: String?): Text? {
        // dynamic load to support jre 1.4 and 1.5
        return try {
            val m: Method = section.getClass().getMethod("replaceWholeText", arrayOf<Class?>(arg0.getClass()))
            m.invoke(section, arrayOf(arg0)) as Text
        } catch (e: Exception) {
            throw PageRuntimeException(Caster.toPageException(e))
        }
    }

    @Override
    override fun duplicate(deepCopy: Boolean): Collection? {
        return XMLCDATASectionStruct(section.cloneNode(deepCopy) as CDATASection, caseSensitive)
    }

    @Override
    override fun cloneNode(deep: Boolean): Node? {
        return XMLCDATASectionStruct(section.cloneNode(deep) as CDATASection, caseSensitive)
    }

    /**
     * constructor of the class
     *
     * @param section
     * @param caseSensitive
     */
    init {
        this.section = section
    }
}