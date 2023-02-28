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
package lucee.runtime.helpers

import java.io.IOException

/**
 * Sax Parse with callback to CFC Methods
 */
class XMLEventParser(pc: PageContext?, startDocument: UDF?, startElement: UDF?, body: UDF?, endElement: UDF?, endDocument: UDF?, error: UDF?) : DefaultHandler() {
    private val startDocument: UDF?
    private val startElement: UDF?
    private val body: UDF?
    private val endElement: UDF?
    private val endDocument: UDF?
    private val error: UDF?
    private val bodies: Stack<StringBuilder?>? = Stack<StringBuilder?>()
    private var pc: PageContext?
    private var att: Struct? = null

    /**
     * start execution of the parser
     *
     * @param xmlFile
     * @param saxParserCass
     * @throws PageException
     */
    @Throws(PageException::class)
    fun start(xmlFile: Resource?) {
        var `is`: InputStream? = null
        try {
            val xmlReader: XMLReader = XMLUtil.createXMLReader()
            xmlReader.setContentHandler(this)
            xmlReader.setErrorHandler(this)
            xmlReader.parse(InputSource(IOUtil.toBufferedInputStream(xmlFile.getInputStream()).also { `is` = it }))
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        } finally {
            try {
                IOUtil.close(`is`)
            } catch (e: IOException) {
                throw Caster.toPageException(e)
            }
        }
    }

    @Override
    @Throws(SAXException::class)
    fun characters(ch: CharArray?, start: Int, length: Int) {
        bodies.peek().append(ch, start, length)
    }

    @Override
    @Throws(SAXException::class)
    fun error(e: SAXParseException?) {
        error(Caster.toPageException(e))
    }

    @Override
    @Throws(SAXException::class)
    fun fatalError(e: SAXParseException?) {
        error(Caster.toPageException(e))
    }

    @Override
    @Throws(SAXException::class)
    fun startElement(uri: String?, localName: String?, qName: String?, attributes: Attributes?) {
        bodies.add(StringBuilder())
        att = toStruct(attributes)
        call(startElement, arrayOf(uri, if (StringUtil.isEmpty(localName)) qName else localName, qName, att))
    }

    @Override
    @Throws(SAXException::class)
    fun endElement(uri: String?, localName: String?, qName: String?) {
        call(body, arrayOf<Object?>(bodies.pop().toString()))
        call(endElement, arrayOf(uri, if (StringUtil.isEmpty(localName)) qName else localName, qName, att))
    }

    @Override
    @Throws(SAXException::class)
    fun startDocument() {
        call(startDocument, ArrayUtil.OBJECT_EMPTY)
    }

    @Override
    @Throws(SAXException::class)
    fun endDocument() {
        call(endDocument, ArrayUtil.OBJECT_EMPTY)
    }

    /**
     * call a user defined function
     *
     * @param udf
     * @param arguments
     */
    private fun call(udf: UDF?, arguments: Array<Object?>?) {
        try {
            udf.call(pc, arguments, false)
        } catch (pe: PageException) {
            error(pe)
        }
    }

    /**
     * call back error function if an error occours
     *
     * @param pe
     */
    private fun error(pe: PageException?) {
        if (error == null) throw PageRuntimeException(pe)
        try {
            pc = ThreadLocalPageContext.get(pc)
            error.call(pc, arrayOf<Object?>(pe.getCatchBlock(pc.getConfig())), false)
        } catch (e: PageException) {
        }
    }

    /**
     * cast an Attributes object to a Struct
     *
     * @param att
     * @return Attributes as Struct
     */
    private fun toStruct(att: Attributes?): Struct? {
        val len: Int = att.getLength()
        val sct: Struct = StructImpl()
        for (i in 0 until len) {
            sct.setEL(att.getQName(i), att.getValue(i))
        }
        return sct
    }
    /**
     * Field `DEFAULT_SAX_PARSER`
     */
    /**
     * constructor of the class
     *
     * @param pc
     * @param startDocument
     * @param startElement
     * @param body
     * @param endElement
     * @param endDocument
     * @param error
     */
    init {
        this.pc = pc
        this.startDocument = startDocument
        this.startElement = startElement
        this.body = body
        this.endElement = endElement
        this.endDocument = endDocument
        this.error = error
    }
}