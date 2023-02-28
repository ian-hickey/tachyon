/**
 * Copyright (c) 2015, Tachyon Assosication Switzerland. All rights reserved.
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
package tachyon.runtime.util

import java.io.IOException

interface XMLUtil {
    fun unescapeXMLString(str: String?): String?
    fun escapeXMLString(xmlStr: String?): String?
    val transformerFactory: TransformerFactory?

    /**
     * parse XML/HTML String to a XML DOM representation
     *
     * @param xml XML InputSource
     * @param validator validator
     * @param isHtml is a HTML or XML Object
     * @return parsed Document
     * @throws SAXException SAX Exception
     * @throws IOException IO Exception
     */
    @Throws(SAXException::class, IOException::class)
    fun parse(xml: InputSource?, validator: InputSource?, isHtml: Boolean): Document?
    fun replaceChild(newChild: Node?, oldChild: Node?)

    /**
     * check if given name is equal to name of the element (with and without namespace)
     *
     * @param node node to compare the name
     * @param name name to compare
     * @return is name of the given Node equal to the given name
     */
    fun nameEqual(node: Node?, name: String?): Boolean

    /**
     * return the root Element from a node
     *
     * @param node node to get root element from
     * @return Root Element
     */
    fun getRootElement(node: Node?): Element?

    /**
     * returns a new Empty XMl Document
     *
     * @return new Document
     * @throws ParserConfigurationException Parser Configuration Exception
     * @throws FactoryConfigurationError Factory Configuration Error
     */
    @Throws(ParserConfigurationException::class, FactoryConfigurationError::class)
    fun newDocument(): Document?

    /**
     * return the Owner Document of a Node List
     *
     * @param nodeList node list
     * @return XML Document
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun getDocument(nodeList: NodeList?): Document?
    fun getDocument(node: Node?): Document?

    /**
     * return all Children of a node by a defined type as Node List
     *
     * @param node node to get children from
     * @param type type of returned node
     * @param filter filter to use
     * @return all matching child node
     */
    fun getChildNodes(node: Node?, type: Short, filter: String?): ArrayList<Node?>?
    fun getChildNode(node: Node?, type: Short, filter: String?, index: Int): Node?

    /**
     * transform a XML Object to another format, with help of a XSL Stylesheet
     *
     * @param xml xml to convert
     * @param xsl xsl used to convert
     * @param parameters parameters used to convert
     * @return resulting string
     * @throws TransformerException Transformer Exception
     * @throws SAXException SAX Exception
     * @throws IOException IO Exception
     */
    @Throws(TransformerException::class, SAXException::class, IOException::class)
    fun transform(xml: InputSource?, xsl: InputSource?, parameters: Map<String?, Object?>?): String?

    /**
     * transform a XML Document to another format, with help of a XSL Stylesheet
     *
     * @param doc xml to convert
     * @param xsl xsl used to convert
     * @param parameters parameters used to convert
     * @return resulting string
     * @throws TransformerException Transformer Exception
     */
    @Throws(TransformerException::class)
    fun transform(doc: Document?, xsl: InputSource?, parameters: Map<String?, Object?>?): String?
    fun getChildWithName(name: String?, el: Element?): Element?

    @Throws(IOException::class)
    fun toInputSource(res: Resource?, cs: Charset?): InputSource?

    @Throws(IOException::class, PageException::class)
    fun toInputSource(value: Object?): InputSource?

    @Throws(PageException::class)
    fun validate(xml: InputSource?, schema: InputSource?, strSchema: String?): Struct?
    fun prependChild(parent: Element?, child: Element?)
    fun setFirst(parent: Node?, node: Node?)

    /**
     * write a xml Dom to a file
     *
     * @param node node
     * @param file Resource
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun writeTo(node: Node?, file: Resource?)

    @Throws(PageException::class)
    fun toString(node: Node?, omitXMLDecl: Boolean, indent: Boolean, publicId: String?, systemId: String?, encoding: String?): String?

    @Throws(PageException::class)
    fun toString(nodes: NodeList?, omitXMLDecl: Boolean, indent: Boolean): String?
    fun toString(node: Node?, defaultValue: String?): String?

    @Throws(PageException::class)
    fun writeTo(node: Node?, res: Result?, omitXMLDecl: Boolean, indent: Boolean, publicId: String?, systemId: String?, encoding: String?)

    @Throws(PageException::class)
    fun toNode(obj: Object?): Node?

    /**
     * creates and returns a xml Document instance
     *
     * @param file Resource
     * @param isHtml Is html
     * @return struct
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun createDocument(file: Resource?, isHtml: Boolean): Document?

    /**
     * creates and returns a xml Document instance
     *
     * @param xml XML
     * @param isHtml Is html
     * @return struct
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun createDocument(xml: String?, isHtml: Boolean): Document?

    /**
     * creates and returns a xml Document instance
     *
     * @param is Input Stream
     * @param isHtml Is html
     * @return struct
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun createDocument(`is`: InputStream?, isHtml: Boolean): Document?
}