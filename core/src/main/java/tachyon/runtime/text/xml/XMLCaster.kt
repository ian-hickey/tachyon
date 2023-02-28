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
package tachyon.runtime.text.xml

import java.io.IOException

/**
 * Cast Objects to XML Objects of different types
 */
object XMLCaster {
    /**
     * casts a value to a XML Text
     *
     * @param doc XML Document
     * @param o Object to cast
     * @return XML Text Object
     * @throws PageException
     */
    @Throws(PageException::class)
    fun toText(doc: Document?, o: Object?): Text? {
        if (o is Text) return o as Text? else if (o is CharacterData) return doc.createTextNode((o as CharacterData?).getData())
        return doc.createTextNode(Caster.toString(o))
    }

    @Throws(PageException::class)
    fun toCDATASection(doc: Document?, o: Object?): Text? {
        if (o is CDATASection) return o as CDATASection? else if (o is CharacterData) return doc.createCDATASection((o as CharacterData?).getData())
        return doc.createCDATASection(Caster.toString(o))
    }

    /**
     * casts a value to a XML Text Array
     *
     * @param doc XML Document
     * @param o Object to cast
     * @return XML Text Array
     * @throws PageException
     */
    @Throws(PageException::class)
    fun toTextArray(doc: Document?, o: Object?): Array<Text?>? {
        // Node[]
        if (o is Array<Node>) {
            val nodes: Array<Node?>? = o
            if (_isAllOfSameType(nodes, Node.TEXT_NODE)) return nodes
            val textes: Array<Text?> = arrayOfNulls<Text?>(nodes!!.size)
            for (i in nodes.indices) {
                textes[i] = toText(doc, nodes!![i])
            }
            return textes
        } else if (o is Collection) {
            val it: Iterator<Object?> = o.valueIterator()
            val textes: List<Text?> = ArrayList<Text?>()
            while (it.hasNext()) {
                textes.add(toText(doc, it.next()))
            }
            return textes.toArray(arrayOfNulls<Text?>(textes.size()))
        }
        // Node Map and List
        val nodes: Array<Node?>? = _toNodeArray(doc, o)
        return if (nodes != null) toTextArray(doc, nodes) else try {
            arrayOf<Text?>(toText(doc, o))
        } catch (e: ExpressionException) {
            throw XMLException("can't cast Object of type " + Caster.toClassName(o).toString() + " to a XML Text Array", e)
        }
        // Single Text Node
    }

    /**
     * casts a value to a XML Attribute Object
     *
     * @param doc XML Document
     * @param o Object to cast
     * @return XML Comment Object
     * @throws PageException
     */
    @Throws(PageException::class)
    fun toAttr(doc: Document?, o: Object?): Attr? {
        if (o is Attr) return o as Attr?
        if (o is Struct && (o as Struct?).size() === 1) {
            val sct: Struct? = o as Struct?
            val e: Entry<Key?, Object?> = sct.entryIterator().next()
            val attr: Attr = doc.createAttribute(e.getKey().getString())
            attr.setValue(Caster.toString(e.getValue()))
            return attr
        }
        throw XMLException("can't cast Object of type " + Caster.toClassName(o).toString() + " to a XML Attribute")
    }

    /**
     * casts a value to a XML Attr Array
     *
     * @param doc XML Document
     * @param o Object to cast
     * @return XML Attr Array
     * @throws PageException
     */
    @Throws(PageException::class)
    fun toAttrArray(doc: Document?, o: Object?): Array<Attr?>? {
        // Node[]
        if (o is Array<Node>) {
            val nodes: Array<Node?>? = o
            if (_isAllOfSameType(nodes, Node.ATTRIBUTE_NODE)) return nodes
            val attres: Array<Attr?> = arrayOfNulls<Attr?>(nodes!!.size)
            for (i in nodes.indices) {
                attres[i] = toAttr(doc, nodes!![i])
            }
            return attres
        } else if (o is Collection) {
            val it: Iterator<Entry<Key?, Object?>?> = o.entryIterator()
            var e: Entry<Key?, Object?>?
            val attres: List<Attr?> = ArrayList<Attr?>()
            var attr: Attr
            var k: Collection.Key
            while (it.hasNext()) {
                e = it.next()
                k = e.getKey()
                attr = doc.createAttribute(if (Decision.isNumber(k.getString())) "attribute-" + k.getString() else k.getString())
                attr.setValue(Caster.toString(e.getValue()))
                attres.add(attr)
            }
            return attres.toArray(arrayOfNulls<Attr?>(attres.size()))
        }
        // Node Map and List
        val nodes: Array<Node?>? = _toNodeArray(doc, o)
        return if (nodes != null) toAttrArray(doc, nodes) else try {
            arrayOf<Attr?>(toAttr(doc, o))
        } catch (e: ExpressionException) {
            throw XMLException("can't cast Object of type " + Caster.toClassName(o).toString() + " to a XML Attributes Array", e)
        }
        // Single Text Node
    }

    /**
     * casts a value to a XML Comment Object
     *
     * @param doc XML Document
     * @param o Object to cast
     * @return XML Comment Object
     * @throws PageException
     */
    @Throws(PageException::class)
    fun toComment(doc: Document?, o: Object?): Comment? {
        if (o is Comment) return o as Comment? else if (o is CharacterData) return doc.createComment((o as CharacterData?).getData())
        return doc.createComment(Caster.toString(o))
    }

    /**
     * casts a value to a XML Comment Array
     *
     * @param doc XML Document
     * @param o Object to cast
     * @return XML Comment Array
     * @throws PageException
     */
    @Throws(PageException::class)
    fun toCommentArray(doc: Document?, o: Object?): Array<Comment?>? {
        // Node[]
        if (o is Array<Node>) {
            val nodes: Array<Node?>? = o
            if (_isAllOfSameType(nodes, Node.COMMENT_NODE)) return nodes
            val comments: Array<Comment?> = arrayOfNulls<Comment?>(nodes!!.size)
            for (i in nodes.indices) {
                comments[i] = toComment(doc, nodes!![i])
            }
            return comments
        } else if (o is Collection) {
            val it: Iterator<Object?> = o.valueIterator()
            val comments: List<Comment?> = ArrayList<Comment?>()
            while (it.hasNext()) {
                comments.add(toComment(doc, it.next()))
            }
            return comments.toArray(arrayOfNulls<Comment?>(comments.size()))
        }
        // Node Map and List
        val nodes: Array<Node?>? = _toNodeArray(doc, o)
        return if (nodes != null) toCommentArray(doc, nodes) else try {
            arrayOf<Comment?>(toComment(doc, o))
        } catch (e: ExpressionException) {
            throw XMLException("can't cast Object of type " + Caster.toClassName(o).toString() + " to a XML Comment Array", e)
        }
        // Single Text Node
    }

    /**
     * casts a value to a XML Element
     *
     * @param doc XML Document
     * @param o Object to cast
     * @return XML Element Object
     * @throws PageException
     */
    @Throws(PageException::class)
    fun toElement(doc: Document?, o: Object?): Element? {
        if (o is Element) return o as Element? else if (o is Node) throw ExpressionException("Object " + Caster.toClassName(o).toString() + " must be a XML Element")
        return doc.createElement(Caster.toString(o))
    }

    /**
     * casts a value to a XML Element Array
     *
     * @param doc XML Document
     * @param o Object to cast
     * @return XML Comment Array
     * @throws PageException
     */
    @Throws(PageException::class)
    fun toElementArray(doc: Document?, o: Object?): Array<Element?>? {
        // Node[]
        if (o is Array<Node>) {
            val nodes: Array<Node?>? = o
            if (_isAllOfSameType(nodes, Node.ELEMENT_NODE)) return nodes
            val elements: Array<Element?> = arrayOfNulls<Element?>(nodes!!.size)
            for (i in nodes.indices) {
                elements[i] = toElement(doc, nodes!![i])
            }
            return elements
        } else if (o is Collection) {
            val it: Iterator<Object?> = o.valueIterator()
            val elements: List<Element?> = ArrayList<Element?>()
            while (it.hasNext()) {
                elements.add(toElement(doc, it.next()))
            }
            return elements.toArray(arrayOfNulls<Element?>(elements.size()))
        }
        // Node Map and List
        val nodes: Array<Node?>? = _toNodeArray(doc, o)
        return if (nodes != null) toElementArray(doc, nodes) else try {
            arrayOf<Element?>(toElement(doc, o))
        } catch (e: ExpressionException) {
            throw XMLException("can't cast Object of type " + Caster.toClassName(o).toString() + " to a XML Element Array", e)
        }
        // Single Text Node
    }

    /**
     * casts a value to a XML Node
     *
     * @param doc XML Document
     * @param o Object to cast
     * @return XML Element Object
     * @throws PageException
     */
    @Deprecated
    @Deprecated("replaced with toRawNode")
    @Throws(PageException::class)
    fun toNode(o: Object?): Node? {
        if (o is XMLStruct) return (o as XMLStruct?).toNode()
        if (o is Node) return o as Node?
        throw CasterException(o, "node")
    }

    /**
     * remove tachyon node wraps (XMLStruct) from node
     *
     * @param node
     * @return raw node (without wrap)
     */
    fun toRawNode(node: Node?): Node? {
        return if (node is XMLStruct) (node as XMLStruct?).toNode() else node
    }

    @Throws(PageException::class)
    fun toNode(doc: Document?, o: Object?, clone: Boolean): Node? {
        var n: Node? = null
        if (o is XMLStruct) n = (o as XMLStruct?).toNode() else if (o is Node) n = o as Node?
        if (n != null) return if (clone) n.cloneNode(true) else n
        var nodeName: String = Caster.toString(o)
        if (nodeName.length() === 0) nodeName = "Empty"
        return doc.createElement(nodeName)
    }

    /**
     * casts a value to a XML Element Array
     *
     * @param doc XML Document
     * @param o Object to cast
     * @return XML Comment Array
     * @throws PageException
     */
    @Throws(PageException::class)
    fun toNodeArray(doc: Document?, o: Object?): Array<Node?>? {
        if (o is Node) return arrayOf<Node?>(o as Node?)
        // Node[]
        if (o is Array<Node>) {
            return o
        } else if (o is Collection) {
            val it: Iterator<Object?> = o.valueIterator()
            val nodes: List<Node?> = ArrayList<Node?>()
            while (it.hasNext()) {
                nodes.add(toNode(doc, it.next(), false))
            }
            return nodes.toArray(arrayOfNulls<Node?>(nodes.size()))
        }
        // Node Map and List
        val nodes: Array<Node?>? = _toNodeArray(doc, o)
        return nodes
                ?: try {
                    arrayOf<Node?>(toNode(doc, o, false))
                } catch (e: ExpressionException) {
                    throw XMLException("can't cast Object of type " + Caster.toClassName(o).toString() + " to a XML Node Array", e)
                }
        // Single Text Node
    }

    /**
     * casts a value to a XML Object defined by type parameter
     *
     * @param doc XML Document
     * @param o Object to cast
     * @param type type to cast to
     * @return XML Text Object
     * @throws PageException
     */
    @Throws(PageException::class)
    fun toNode(doc: Document?, o: Object?, type: Short): Node? {
        if (Node.TEXT_NODE === type) toText(doc, o) else if (Node.ATTRIBUTE_NODE === type) toAttr(doc, o) else if (Node.COMMENT_NODE === type) toComment(doc, o) else if (Node.ELEMENT_NODE === type) toElement(doc, o)
        throw ExpressionException("invalid node type definition")
    }

    /**
     * casts a value to a XML Object Array defined by type parameter
     *
     * @param doc XML Document
     * @param o Object to cast
     * @param type type to cast to
     * @return XML Node Array Object
     * @throws PageException
     */
    @Throws(PageException::class)
    fun toNodeArray(doc: Document?, o: Object?, type: Short): Array<Node?>? {
        if (Node.TEXT_NODE === type) toTextArray(doc, o) else if (Node.ATTRIBUTE_NODE === type) toAttrArray(doc, o) else if (Node.COMMENT_NODE === type) toCommentArray(doc, o) else if (Node.ELEMENT_NODE === type) toElementArray(doc, o)
        throw ExpressionException("invalid node type definition")
    }

    /*
	 * * cast a xml node to a String
	 * 
	 * @param node
	 * 
	 * @return xml node as String
	 * 
	 * @throws ExpressionException / public static String toString(Node node) throws ExpressionException
	 * { //Transformer tf; try { OutputFormat format = new OutputFormat();
	 * 
	 * StringWriter writer = new StringWriter(); XMLSerializer serializer = new XMLSerializer(writer,
	 * format); if(node instanceof Element)serializer.serialize((Element)node); else
	 * serializer.serialize(XMLUtil.getDocument(node)); return writer.toString();
	 * 
	 * } catch (Exception e) { throw ExpressionException.newInstance(e); } }
	 * 
	 * public static String toString(Node node,String defaultValue) { //Transformer tf; try {
	 * OutputFormat format = new OutputFormat();
	 * 
	 * StringWriter writer = new StringWriter(); XMLSerializer serializer = new XMLSerializer(writer,
	 * format); if(node instanceof Element)serializer.serialize((Element)node); else
	 * serializer.serialize(XMLUtil.getDocument(node)); return writer.toString();
	 * 
	 * } catch (Exception e) { return defaultValue; } }
	 */
    @Throws(ExpressionException::class)
    fun toHTML(node: Node?): String? {
        if (Node.DOCUMENT_NODE === node.getNodeType()) return toHTML(XMLUtil.getRootElement(node, true))
        val sb = StringBuilder()
        toHTML(node, sb)
        return sb.toString()
    }

    @Throws(ExpressionException::class)
    private fun toHTML(node: Node?, sb: StringBuilder?) {
        val type: Short = node.getNodeType()
        if (Node.ELEMENT_NODE === type) {
            val el: Element? = node as Element?
            val tagName: String = el.getTagName()
            sb.append('<')
            sb.append(tagName)
            val attrs: NamedNodeMap = el.getAttributes()
            var attr: Attr
            var len: Int = attrs.getLength()
            for (i in 0 until len) {
                attr = attrs.item(i) as Attr
                sb.append(' ')
                sb.append(attr.getName())
                sb.append("=\"")
                sb.append(attr.getValue())
                sb.append('"')
            }
            val children: NodeList = el.getChildNodes()
            len = children.getLength()
            val doEndTag = len != 0 || tagName.length() === 4 && (tagName.equalsIgnoreCase("head") || tagName.equalsIgnoreCase("body"))
            if (!doEndTag) sb.append(" />") else sb.append('>')
            for (i in 0 until len) {
                toHTML(children.item(i), sb)
            }
            if (doEndTag) {
                sb.append("</")
                sb.append(el.getTagName())
                sb.append('>')
            }
        } else if (node is CharacterData) {
            sb.append(HTMLEntities.escapeHTML(node.getNodeValue()))
        }
    }

    /**
     * write a xml Dom to a file
     *
     * @param node
     * @param file
     * @throws PageException
     */
    @Throws(PageException::class)
    fun writeTo(node: Node?, file: Resource?) {
        writeTo(node, file, null)
    }

    @Throws(PageException::class)
    fun writeTo(node: Node?, file: Resource?, charset: Charset?) {
        var charset: Charset? = charset
        if (charset == null) charset = CharsetUtil.UTF8
        var w: Writer? = null
        try {
            // os = IOUtil.toBufferedOutputStream(file.getOutputStream());
            w = IOUtil.getWriter(file, charset)
            writeTo(node, StreamResult(w), false, false, null, null, null)
            w.flush()
        } catch (ioe: IOException) {
            throw Caster.toPageException(ioe)
        } finally {
            try {
                IOUtil.close(w)
            } catch (ioe: IOException) {
                throw Caster.toPageException(ioe)
            }
        }
    }

    @Throws(PageException::class)
    fun toString(node: Node?): String? {
        val sw = StringWriter()
        try {
            writeTo(node, StreamResult(sw), false, false, null, null, null)
        } finally {
            try {
                IOUtil.close(sw)
            } catch (e: IOException) {
                throw Caster.toPageException(e)
            }
        }
        return sw.getBuffer().toString()
    }

    @Throws(PageException::class)
    fun toString(node: Node?, omitXMLDecl: Boolean, indent: Boolean): String? {
        val sw = StringWriter()
        try {
            writeTo(node, StreamResult(sw), omitXMLDecl, indent, null, null, null)
        } finally {
            try {
                IOUtil.close(sw)
            } catch (ioe: IOException) {
                throw Caster.toPageException(ioe)
            }
        }
        return sw.getBuffer().toString()
    }

    @Throws(PageException::class)
    fun toString(node: Node?, omitXMLDecl: Boolean, indent: Boolean, publicId: String?, systemId: String?, encoding: String?): String? {
        val sw = StringWriter()
        try {
            writeTo(node, StreamResult(sw), omitXMLDecl, indent, publicId, systemId, encoding)
        } finally {
            try {
                IOUtil.close(sw)
            } catch (ioe: IOException) {
                throw Caster.toPageException(ioe)
            }
        }
        return sw.getBuffer().toString()
    }

    @Throws(PageException::class)
    fun toString(nodes: NodeList?, omitXMLDecl: Boolean, indent: Boolean): String? {
        val sw = StringWriter()
        try {
            val len: Int = nodes.getLength()
            for (i in 0 until len) {
                writeTo(nodes.item(i), StreamResult(sw), omitXMLDecl, indent, null, null, null)
            }
        } finally {
            try {
                IOUtil.close(sw)
            } catch (ioe: IOException) {
                throw Caster.toPageException(ioe)
            }
        }
        return sw.getBuffer().toString()
    }

    fun toString(node: Node?, defaultValue: String?): String? {
        val sw = StringWriter()
        try {
            writeTo(node, StreamResult(sw), false, false, null, null, null)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            return defaultValue
        } finally {
            IOUtil.closeEL(sw)
        }
        return sw.getBuffer().toString()
    }

    @Throws(PageException::class)
    fun writeTo(node: Node?, res: Result?, omitXMLDecl: Boolean, indent: Boolean, publicId: String?, systemId: String?, encoding: String?) {
        try {
            val t: Transformer = XMLUtil.getTransformerFactory().newTransformer()
            t.setOutputProperty(OutputKeys.INDENT, if (indent) "yes" else "no")
            t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, if (omitXMLDecl) "yes" else "no")

            // optional properties
            if (!StringUtil.isEmpty(publicId, true)) t.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, publicId)
            if (!StringUtil.isEmpty(systemId, true)) t.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, systemId)
            if (!StringUtil.isEmpty(encoding, true)) t.setOutputProperty(OutputKeys.ENCODING, encoding)
            t.transform(DOMSource(node), res)
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
    }

    /**
     * Casts a XML Node to a HTML Presentation
     *
     * @param node
     * @param pageContext
     * @return html output
     */
    fun toDumpData(node: Node?, pageContext: PageContext?, maxlevel: Int, props: DumpProperties?): DumpData? {
        var maxlevel = maxlevel
        if (maxlevel <= 0) {
            return DumpUtil.MAX_LEVEL_REACHED
        }
        maxlevel--
        // Document
        if (node is Document) {
            val table = DumpTable("xml", "#cc9999", "#ffffff", "#000000")
            table.setTitle("XML Document")
            table.appendRow(1, SimpleDumpData("XmlComment"), SimpleDumpData(XMLUtil.getProperty(node, XMLUtil.XMLCOMMENT, null).toString()))
            table.appendRow(1, SimpleDumpData("XmlRoot"), DumpUtil.toDumpData(XMLUtil.getProperty(node, XMLUtil.XMLROOT, null), pageContext, maxlevel, props))
            return table
        }
        // Element
        if (node is Element) {
            val table = DumpTable("xml", "#cc9999", "#ffffff", "#000000")
            table.setTitle("XML Element")
            table.appendRow(1, SimpleDumpData("xmlName"), SimpleDumpData(XMLUtil.getProperty(node, XMLUtil.XMLNAME, null).toString()))
            table.appendRow(1, SimpleDumpData("XmlNsPrefix"), SimpleDumpData(XMLUtil.getProperty(node, XMLUtil.XMLNSPREFIX, null).toString()))
            table.appendRow(1, SimpleDumpData("XmlNsURI"), SimpleDumpData(XMLUtil.getProperty(node, XMLUtil.XMLNSURI, null).toString()))
            table.appendRow(1, SimpleDumpData("XmlText"), DumpUtil.toDumpData(XMLUtil.getProperty(node, XMLUtil.XMLTEXT, null), pageContext, maxlevel, props))
            table.appendRow(1, SimpleDumpData("XmlComment"), SimpleDumpData(XMLUtil.getProperty(node, XMLUtil.XMLCOMMENT, null).toString()))
            table.appendRow(1, SimpleDumpData("XmlAttributes"), DumpUtil.toDumpData(XMLUtil.getProperty(node, XMLUtil.XMLATTRIBUTES, null), pageContext, maxlevel, props))
            table.appendRow(1, SimpleDumpData("XmlChildren"), DumpUtil.toDumpData(XMLUtil.getProperty(node, XMLUtil.XMLCHILDREN, null), pageContext, maxlevel, props))
            return table
        }
        // Text
        if (node is Text) {
            val table = DumpTable("xml", "#cc9999", "#ffffff", "#000000")
            table.setTitle("XML Text")
            val txt: Text? = node as Text?
            table.appendRow(1, SimpleDumpData("XmlText"), SimpleDumpData(txt.getData()))
            return table
        }
        // Attr
        if (node is Attr) {
            val table = DumpTable("xml", "#cc9999", "#ffffff", "#000000")
            table.setTitle("XML Attr")
            table.appendRow(1, SimpleDumpData("xmlName"), SimpleDumpData(XMLUtil.getProperty(node, XMLUtil.XMLNAME, null).toString()))
            table.appendRow(1, SimpleDumpData("XmlValue"), DumpUtil.toDumpData((node as Attr?).getValue(), pageContext, maxlevel, props))
            table.appendRow(1, SimpleDumpData("XmlType"), SimpleDumpData(XMLUtil.getTypeAsString(node, true)))
            return table
        }
        // Node
        val table = DumpTable("xml", "#cc9999", "#ffffff", "#000000")
        table.setTitle("XML Node (" + ListUtil.last(node.getClass().getName(), ".", true).toString() + ")")
        table.appendRow(1, SimpleDumpData("xmlName"), SimpleDumpData(XMLUtil.getProperty(node, XMLUtil.XMLNAME, null).toString()))
        table.appendRow(1, SimpleDumpData("XmlNsPrefix"), SimpleDumpData(XMLUtil.getProperty(node, XMLUtil.XMLNSPREFIX, null).toString()))
        table.appendRow(1, SimpleDumpData("XmlNsURI"), SimpleDumpData(XMLUtil.getProperty(node, XMLUtil.XMLNSURI, null).toString()))
        table.appendRow(1, SimpleDumpData("XmlText"), DumpUtil.toDumpData(XMLUtil.getProperty(node, XMLUtil.XMLTEXT, null), pageContext, maxlevel, props))
        table.appendRow(1, SimpleDumpData("XmlComment"), SimpleDumpData(XMLUtil.getProperty(node, XMLUtil.XMLCOMMENT, null).toString()))
        table.appendRow(1, SimpleDumpData("XmlAttributes"), DumpUtil.toDumpData(XMLUtil.getProperty(node, XMLUtil.XMLATTRIBUTES, null), pageContext, maxlevel, props))
        table.appendRow(1, SimpleDumpData("XmlChildren"), DumpUtil.toDumpData(XMLUtil.getProperty(node, XMLUtil.XMLCHILDREN, null), pageContext, maxlevel, props))
        table.appendRow(1, SimpleDumpData("XmlType"), SimpleDumpData(XMLUtil.getTypeAsString(node, true)))
        return table
    }

    /**
     * casts a value to a XML named Node Map
     *
     * @param doc XML Document
     * @param o Object to cast
     * @return XML named Node Map Object
     */
    private fun _toNodeArray(doc: Document?, o: Object?): Array<Node?>? {
        if (o is Node) return arrayOf<Node?>(o as Node?)
        // Node[]
        if (o is Array<Node>) return o else if (o is NamedNodeMap) {
            val map: NamedNodeMap? = o as NamedNodeMap?
            val len: Int = map.getLength()
            val nodes: Array<Node?> = arrayOfNulls<Node?>(len)
            for (i in 0 until len) {
                nodes[i] = map.item(i)
            }
            return nodes
        } else if (o is XMLAttributes) {
            return _toNodeArray(doc, (o as XMLAttributes?)!!.toNamedNodeMap())
        } else if (o is NodeList) {
            val list: NodeList? = o as NodeList?
            val len: Int = list.getLength()
            val nodes: Array<Node?> = arrayOfNulls<Node?>(len)
            for (i in nodes.indices) {
                nodes[i] = list.item(i)
            }
            return nodes
        }
        return null
    }

    /**
     * Check if all Node are of the type defnined by para,meter
     *
     * @param nodes nodes to check
     * @param type to compare
     * @return are all of the same type
     */
    private fun _isAllOfSameType(nodes: Array<Node?>?, type: Short): Boolean {
        for (i in nodes.indices) {
            if (nodes!![i].getNodeType() !== type) return false
        }
        return true
    }

    /**
     * creates a XMLCollection Object from a Node
     *
     * @param node
     * @param caseSensitive
     * @return xmlstruct from node
     */
    fun toXMLStruct(node: Node?, caseSensitive: Boolean): XMLStruct? { // do not change, this method is used in the flex,axis extension
        return XMLStructFactory.newInstance(node, caseSensitive)
    }

    fun toRawElement(value: Object?, defaultValue: Element?): Element? {
        if (value is Node) {
            val node: Node? = toRawNode(value as Node?)
            if (node is Document) return (node as Document?).getDocumentElement()
            return if (node is Element) node as Element? else defaultValue
        }
        return try {
            XMLUtil.parse(InputSource(StringReader(Caster.toString(value))), null, false).getDocumentElement()
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            defaultValue
        }
    }
}