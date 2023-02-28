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
package lucee.runtime.util

import java.io.ByteArrayInputStream

// FUTURE all this needs to come from core
/**
 *
 */
class XMLUtilImpl : XMLUtil {
    @Override
    fun unescapeXMLString(str: String): String {
        val rtn = StringBuilder()
        var posStart = -1
        var posFinish = -1
        while (str.indexOf('&', posStart).also { posStart = it } != -1) {
            val last = posFinish + 1
            posFinish = str.indexOf(';', posStart)
            if (posFinish == -1) break
            rtn.append(str.substring(last, posStart))
            if (posStart + 1 < posFinish) {
                rtn.append(unescapeXMLEntity(str.substring(posStart + 1, posFinish)))
            } else {
                rtn.append("&;")
            }
            posStart = posFinish + 1
        }
        rtn.append(str.substring(posFinish + 1))
        return rtn.toString()
    }

    @Override
    fun escapeXMLString(xmlStr: String): String {
        var c: Char
        val sb = StringBuffer()
        val len: Int = xmlStr.length()
        for (i in 0 until len) {
            c = xmlStr.charAt(i)
            if (c == '<') sb.append("&lt;") else if (c == '>') sb.append("&gt;") else if (c == '&') sb.append("&amp;") else if (c == '"') sb.append("&quot;") else sb.append(c)
        }
        return sb.toString()
    }

    @get:Override
    override val transformerFactory: TransformerFactory?
        get() = transformerFactory()

    /**
     * parse XML/HTML String to a XML DOM representation
     *
     * @param xml XML InputSource
     * @param isHtml is a HTML or XML Object
     * @return parsed Document
     * @throws SAXException SAX Exception
     * @throws IOException IO Exception
     */
    @Override
    @Throws(SAXException::class, IOException::class)
    override fun parse(xml: InputSource?, validator: InputSource?, isHtml: Boolean): Document {
        if (!isHtml) {
            val factory: DocumentBuilderFactory = newDocumentBuilderFactory()

            // print.o(factory);
            if (validator == null) {
                setAttributeEL(factory, NON_VALIDATING_DTD_EXTERNAL, Boolean.FALSE)
                setAttributeEL(factory, NON_VALIDATING_DTD_GRAMMAR, Boolean.FALSE)
            } else {
                setAttributeEL(factory, VALIDATION_SCHEMA, Boolean.TRUE)
                setAttributeEL(factory, VALIDATION_SCHEMA_FULL_CHECKING, Boolean.TRUE)
            }
            factory.setNamespaceAware(true)
            factory.setValidating(validator != null)
            return try {
                val builder: DocumentBuilder = factory.newDocumentBuilder()
                builder.setEntityResolver(XMLEntityResolverDefaultHandler(validator))
                builder.setErrorHandler(ThrowingErrorHandler(true, true, false))
                builder.parse(xml)
            } catch (e: ParserConfigurationException) {
                throw SAXException(e)
            }

            /*
			 * DOMParser parser = new DOMParser(); print.out("parse"); parser.setEntityResolver(new
			 * XMLEntityResolverDefaultHandler(validator)); parser.parse(xml); return parser.getDocument();
			 */
        }
        val reader: XMLReader = Parser()
        reader.setFeature(Parser.namespacesFeature, true)
        reader.setFeature(Parser.namespacePrefixesFeature, true)
        return try {
            val transformer: Transformer = TransformerFactory.newInstance().newTransformer()
            val result = DOMResult()
            transformer.transform(SAXSource(reader, xml), result)
            getDocument(result.getNode())
        } catch (e: Exception) {
            throw SAXException(e)
        }
    }

    private fun newDocumentBuilderFactory(): DocumentBuilderFactory {
        return DocumentBuilderFactory.newInstance()
    }

    @Override
    override fun replaceChild(newChild: Node, oldChild: Node) {
        val nc: Node = newChild
        val oc: Node = oldChild
        val p: Node = oc.getParentNode()
        if (nc !== oc) p.replaceChild(nc, oc)
    }

    @Override
    override fun nameEqual(node: Node, name: String?): Boolean {
        return if (name == null) false else name.equals(node.getNodeName()) || name.equals(node.getLocalName())
    }

    @Override
    override fun getRootElement(node: Node): Element {
        var doc: Document? = null
        doc = if (node is Document) node as Document else node.getOwnerDocument()
        return doc.getDocumentElement()
    }

    @Override
    @Throws(ParserConfigurationException::class, FactoryConfigurationError::class)
    override fun newDocument(): Document {
        if (docBuilder == null) {
            docBuilder = newDocumentBuilderFactory().newDocumentBuilder()
        }
        return docBuilder.newDocument()
    }

    @Override
    @Throws(PageException::class)
    override fun getDocument(nodeList: NodeList): Document {
        if (nodeList is Document) return nodeList as Document
        val len: Int = nodeList.getLength()
        for (i in 0 until len) {
            val node: Node = nodeList.item(i)
            if (node != null) return node.getOwnerDocument()
        }
        throw CFMLEngineFactory.getInstance().getExceptionUtil().createXMLException("can't get Document from NodeList, in NoteList are no Nodes")
    }

    @Override
    override fun getDocument(node: Node): Document {
        return if (node is Document) node as Document else node.getOwnerDocument()
    }

    @Override
    @Synchronized
    override fun getChildNodes(node: Node, type: Short, filter: String?): ArrayList<Node> {
        val rtn: ArrayList<Node> = ArrayList<Node>()
        val nodes: NodeList = node.getChildNodes()
        val len: Int = nodes.getLength()
        var n: Node
        for (i in 0 until len) {
            try {
                n = nodes.item(i)
                if (n != null && (type == UNDEFINED_NODE || n.getNodeType() === type)) {
                    if (filter == null || filter.equals(n.getLocalName())) rtn.add(n)
                }
            } catch (t: Throwable) {
                if (t is ThreadDeath) throw t as ThreadDeath
            }
        }
        return rtn
    }

    @Synchronized
    fun getChildNodesAsList(node: Node, type: Short, filter: String?): List<Node> {
        val rtn: List<Node> = ArrayList<Node>()
        val nodes: NodeList = node.getChildNodes()
        val len: Int = nodes.getLength()
        var n: Node
        for (i in 0 until len) {
            try {
                n = nodes.item(i)
                if (n != null && (n.getNodeType() === type || type == UNDEFINED_NODE)) {
                    if (filter == null || filter.equals(n.getLocalName())) rtn.add(n)
                }
            } catch (t: Throwable) {
                if (t is ThreadDeath) throw t as ThreadDeath
            }
        }
        return rtn
    }

    @Override
    @Synchronized
    override fun getChildNode(node: Node, type: Short, filter: String?, index: Int): Node? {
        val nodes: NodeList = node.getChildNodes()
        val len: Int = nodes.getLength()
        var n: Node
        var count = 0
        for (i in 0 until len) {
            try {
                n = nodes.item(i)
                if (n != null && (type == UNDEFINED_NODE || n.getNodeType() === type)) {
                    if (filter == null || filter.equals(n.getLocalName())) {
                        if (count == index) return n
                        count++
                    }
                }
            } catch (t: Throwable) {
                if (t is ThreadDeath) throw t as ThreadDeath
            }
        }
        return null
    }

    /**
     * return all Children of a node by a defined type as Node Array
     *
     * @param node node to get children from
     * @param type type of returned node
     * @return all matching child node
     */
    fun getChildNodesAsArray(node: Node, type: Short): Array<Node> {
        val nodeList: ArrayList<Node> = getChildNodes(node, type, null)
        return nodeList.toArray(arrayOfNulls<Node>(nodeList.size()))
    }

    fun getChildNodesAsArray(node: Node, type: Short, filter: String?): Array<Node> {
        val nodeList: ArrayList<Node> = getChildNodes(node, type, filter)
        return nodeList.toArray(arrayOfNulls<Node>(nodeList.size()))
    }

    /**
     * return all Element Children of a node
     *
     * @param node node to get children from
     * @return all matching child node
     */
    fun getChildElementsAsArray(node: Node): Array<Element> {
        val nodeList: ArrayList<Node> = getChildNodes(node, Node.ELEMENT_NODE, null)
        return nodeList.toArray(arrayOfNulls<Element>(nodeList.size()))
    }

    @Override
    @Throws(TransformerException::class, SAXException::class, IOException::class)
    override fun transform(xml: InputSource?, xsl: InputSource?, parameters: Map<String?, Object?>?): String {
        return transform(parse(xml, null, false), xsl, parameters)
    }

    @Override
    @Throws(TransformerException::class)
    override fun transform(doc: Document?, xsl: InputSource, parameters: Map<String?, Object?>?): String {
        val sw = StringWriter()
        val factory: TransformerFactory? = transformerFactory
        factory.setErrorListener(SimpleErrorListener.THROW_FATAL)
        val transformer: Transformer = factory.newTransformer(StreamSource(xsl.getCharacterStream()))
        if (parameters != null) {
            val it: Iterator = parameters.entrySet().iterator()
            while (it.hasNext()) {
                val e: Map.Entry = it.next()
                transformer.setParameter(e.getKey().toString(), e.getValue())
            }
        }
        transformer.transform(DOMSource(doc), StreamResult(sw))
        return sw.toString()
    }

    /**
     * returns the Node Type As String
     *
     * @param node node
     * @param cftype CF Type
     * @return Returns the Node type.
     */
    fun getTypeAsString(node: Node, cftype: Boolean): String {
        val suffix = if (cftype) "" else "_NODE"
        return when (node.getNodeType()) {
            Node.ATTRIBUTE_NODE -> "ATTRIBUTE$suffix"
            Node.CDATA_SECTION_NODE -> "CDATA_SECTION$suffix"
            Node.COMMENT_NODE -> "COMMENT$suffix"
            Node.DOCUMENT_FRAGMENT_NODE -> "DOCUMENT_FRAGMENT$suffix"
            Node.DOCUMENT_NODE -> "DOCUMENT$suffix"
            Node.DOCUMENT_TYPE_NODE -> "DOCUMENT_TYPE$suffix"
            Node.ELEMENT_NODE -> "ELEMENT$suffix"
            Node.ENTITY_NODE -> "ENTITY$suffix"
            Node.ENTITY_REFERENCE_NODE -> "ENTITY_REFERENCE$suffix"
            Node.NOTATION_NODE -> "NOTATION$suffix"
            Node.PROCESSING_INSTRUCTION_NODE -> "PROCESSING_INSTRUCTION$suffix"
            Node.TEXT_NODE -> "TEXT$suffix"
            else -> "UNKNOW$suffix"
        }
    }

    @Override
    @Synchronized
    fun getChildWithName(name: String, el: Element): Element? {
        val children: Array<Element> = getChildElementsAsArray(el)
        for (i in children.indices) {
            if (name.equalsIgnoreCase(children[i].getNodeName())) return children[i]
        }
        return null
    }

    @Override
    @Throws(IOException::class)
    override fun toInputSource(res: Resource?, cs: Charset?): InputSource {
        val str: String = CFMLEngineFactory.getInstance().getIOUtil().toString(res, cs)
        return InputSource(StringReader(str))
    }

    @Throws(IOException::class, PageException::class)
    override fun toInputSource(pc: PageContext?, value: Object): InputSource {
        if (value is InputSource) {
            return value as InputSource
        }
        if (value is String) {
            return toInputSource(pc, value as String)
        }
        if (value is StringBuffer) {
            return toInputSource(pc, value.toString())
        }
        val engine: CFMLEngine = CFMLEngineFactory.getInstance()
        val io: IO = engine.getIOUtil()
        if (value is Resource) {
            val str: String = io.toString(value as Resource, null as Charset?)
            return InputSource(StringReader(str))
        }
        if (value is File) {
            val str: String = io.toString(engine.getCastUtil().toResource(value), null as Charset?)
            return InputSource(StringReader(str))
        }
        if (value is InputStream) {
            val `is`: InputStream = value as InputStream
            return try {
                val str: String = io.toString(`is`, null as Charset?)
                InputSource(StringReader(str))
            } finally {
                io.closeSilent(`is`)
            }
        }
        if (value is Reader) {
            val reader: Reader = value as Reader
            return try {
                val str: String = io.toString(reader)
                InputSource(StringReader(str))
            } finally {
                io.closeSilent(reader)
            }
        }
        if (value is ByteArray) {
            return InputSource(ByteArrayInputStream(value as ByteArray))
        }
        throw engine.getExceptionUtil().createXMLException("can't cast object of type [$value] to an Input for xml parser")
    }

    @Throws(IOException::class, PageException::class)
    override fun toInputSource(pc: PageContext?, xml: String): InputSource {
        return toInputSource(pc, xml, true)
    }

    @Throws(IOException::class, PageException::class)
    fun toInputSource(pc: PageContext?, xml: String, canBePath: Boolean): InputSource {
        // xml text
        var pc: PageContext? = pc
        var xml = xml
        xml = xml.trim()
        if (!canBePath || xml.startsWith("<")) {
            return InputSource(StringReader(xml))
        }
        // xml link
        if (pc == null) pc = CFMLEngineFactory.getInstance().getThreadPageContext()
        val res: Resource = CFMLEngineFactory.getInstance().getResourceUtil().toResourceExisting(pc, xml)
        return toInputSource(pc, res)
    }

    @Override
    @Throws(PageException::class)
    override fun validate(xml: InputSource?, schema: InputSource?, strSchema: String?): Struct {
        return XMLValidator(schema, strSchema).validate(xml)
    }

    @Override
    override fun prependChild(parent: Element, child: Element?) {
        val first: Node = parent.getFirstChild()
        if (first == null) parent.appendChild(child) else {
            parent.insertBefore(child, first)
        }
    }

    @Override
    override fun setFirst(parent: Node, node: Node?) {
        val first: Node = parent.getFirstChild()
        if (first != null) parent.insertBefore(node, first) else parent.appendChild(node)
    }

    @Throws(SAXException::class)
    fun createXMLReader(oprionalDefaultSaxParser: String?): XMLReader {
        return try {
            XMLReaderFactory.createXMLReader(oprionalDefaultSaxParser)
        } catch (t: Throwable) {
            if (t is ThreadDeath) throw t as ThreadDeath
            XMLReaderFactory.createXMLReader()
        }
    }

    @Override
    @Throws(IOException::class, PageException::class)
    override fun toInputSource(value: Object): InputSource {
        if (value is InputSource) {
            return value as InputSource
        }
        if (value is String) {
            return toInputSource(CFMLEngineFactory.getInstance().getThreadPageContext(), value as String, true)
        }
        if (value is StringBuffer) {
            return toInputSource(CFMLEngineFactory.getInstance().getThreadPageContext(), value.toString(), true)
        }
        if (value is Resource) {
            val io: IO = CFMLEngineFactory.getInstance().getIOUtil()
            val str: String = io.toString(value as Resource, null as Charset?)
            return InputSource(StringReader(str))
        }
        if (value is File) {
            val e: CFMLEngine = CFMLEngineFactory.getInstance()
            val str: String = e.getIOUtil().toString(e.getCastUtil().toResource(value), null as Charset?)
            return InputSource(StringReader(str))
        }
        if (value is InputStream) {
            val `is`: InputStream = value as InputStream
            val io: IO = CFMLEngineFactory.getInstance().getIOUtil()
            return try {
                val str: String = io.toString(`is`, null as Charset?)
                InputSource(StringReader(str))
            } finally {
                io.closeSilent(`is`)
            }
        }
        if (value is Reader) {
            val reader: Reader = value as Reader
            val io: IO = CFMLEngineFactory.getInstance().getIOUtil()
            return try {
                val str: String = io.toString(reader)
                InputSource(StringReader(str))
            } finally {
                io.closeSilent(reader)
            }
        }
        if (value is ByteArray) {
            return InputSource(ByteArrayInputStream(value as ByteArray))
        }
        throw CFMLEngineFactory.getInstance().getExceptionUtil().createExpressionException("can't cast object of type [$value] to an Input for xml parser")
    }

    @Override
    @Throws(PageException::class)
    override fun writeTo(node: Node?, file: Resource) {
        var os: OutputStream? = null
        val e: CFMLEngine = CFMLEngineFactory.getInstance()
        val io: IO = e.getIOUtil()
        try {
            os = io.toBufferedOutputStream(file.getOutputStream())
            writeTo(node, StreamResult(os), false, false, null, null, null)
        } catch (ioe: IOException) {
            throw e.getCastUtil().toPageException(ioe)
        } finally {
            e.getIOUtil().closeSilent(os)
        }
    }

    @Override
    @Throws(PageException::class)
    override fun writeTo(node: Node?, res: Result?, omitXMLDecl: Boolean, indent: Boolean, publicId: String?, systemId: String?, encoding: String?) {
        try {
            val t: Transformer = transformerFactory.newTransformer()
            t.setOutputProperty(OutputKeys.INDENT, if (indent) "yes" else "no")
            t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, if (omitXMLDecl) "yes" else "no")
            // t.setOutputProperty("{http://xml.apache.org/xalan}indent-amount", "2");

            // optional properties
            if (!Util.isEmpty(publicId, true)) t.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, publicId)
            if (!Util.isEmpty(systemId, true)) t.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, systemId)
            if (!Util.isEmpty(encoding, true)) t.setOutputProperty(OutputKeys.ENCODING, encoding)
            t.transform(DOMSource(node), res)
        } catch (e: Exception) {
            throw CFMLEngineFactory.getInstance().getCastUtil().toPageException(e)
        }
    }

    @Override
    @Throws(PageException::class)
    override fun toString(node: Node?, omitXMLDecl: Boolean, indent: Boolean, publicId: String?, systemId: String?, encoding: String?): String {
        val sw = StringWriter()
        try {
            writeTo(node, StreamResult(sw), omitXMLDecl, indent, publicId, systemId, encoding)
        } finally {
            CFMLEngineFactory.getInstance().getIOUtil().closeSilent(sw)
        }
        return sw.getBuffer().toString()
    }

    @Override
    @Throws(PageException::class)
    override fun toString(nodes: NodeList, omitXMLDecl: Boolean, indent: Boolean): String {
        val sw = StringWriter()
        try {
            val len: Int = nodes.getLength()
            for (i in 0 until len) {
                writeTo(nodes.item(i), StreamResult(sw), omitXMLDecl, indent, null, null, null)
            }
        } finally {
            Util.closeEL(sw)
        }
        return sw.getBuffer().toString()
    }

    @Override
    fun toString(node: Node?, defaultValue: String): String {
        val sw = StringWriter()
        try {
            writeTo(node, StreamResult(sw), false, false, null, null, null)
        } catch (t: Throwable) {
            if (t is ThreadDeath) throw t as ThreadDeath
            return defaultValue
        } finally {
            Util.closeEL(sw)
        }
        return sw.getBuffer().toString()
    }

    @Override
    @Throws(PageException::class)
    override fun toNode(value: Object?): Node? {
        return if (value is Node) value as Node? else try {
            parse(toInputSource(CFMLEngineFactory.getInstance().getThreadPageContext(), value), null, false)
        } catch (outer: Exception) {
            throw CFMLEngineFactory.getInstance().getCastUtil().toPageException(outer)
        }
    }

    @Override
    @Throws(PageException::class)
    override fun createDocument(res: Resource, isHTML: Boolean): Document {
        var `is`: InputStream? = null
        return try {
            parse(InputSource(res.getInputStream().also { `is` = it }), null, isHTML)
        } catch (e: Exception) {
            throw CFMLEngineFactory.getInstance().getCastUtil().toPageException(e)
        } finally {
            Util.closeEL(`is`)
        }
    }

    @Override
    @Throws(PageException::class)
    override fun createDocument(xml: String?, isHTML: Boolean): Document {
        return try {
            parse(toInputSource(null, xml), null, isHTML)
        } catch (e: Exception) {
            throw CFMLEngineFactory.getInstance().getCastUtil().toPageException(e)
        }
    }

    @Override
    @Throws(PageException::class)
    override fun createDocument(`is`: InputStream?, isHTML: Boolean): Document {
        return try {
            parse(InputSource(`is`), null, isHTML)
        } catch (e: Exception) {
            throw CFMLEngineFactory.getInstance().getCastUtil().toPageException(e)
        }
    }

    internal class SimpleErrorListener(private val ignoreFatal: Boolean, private val ignoreError: Boolean, private val ignoreWarning: Boolean) : ErrorListener {
        @Override
        @Throws(TransformerException::class)
        fun error(te: TransformerException?) {
            if (!ignoreError) throw te
        }

        @Override
        @Throws(TransformerException::class)
        fun fatalError(te: TransformerException?) {
            if (!ignoreFatal) throw te
        }

        @Override
        @Throws(TransformerException::class)
        fun warning(te: TransformerException?) {
            if (!ignoreWarning) throw te
        }

        companion object {
            val THROW_FATAL: ErrorListener = SimpleErrorListener(false, true, true)
            val THROW_ERROR: ErrorListener = SimpleErrorListener(false, false, true)
            val THROW_WARNING: ErrorListener = SimpleErrorListener(false, false, false)
        }
    }

    internal class XMLEntityResolverDefaultHandler(entityRes: InputSource?) : DefaultHandler() {
        private val entityRes: InputSource?
        @Override
        @Throws(SAXException::class)
        fun resolveEntity(publicID: String?, systemID: String?): InputSource? {
            // if(entityRes!=null)print.out("resolveEntity("+(entityRes!=null)+"):"+publicID+":"+systemID);
            return if (entityRes != null) entityRes else try {
                val engine: CFMLEngine = CFMLEngineFactory.getInstance()
                InputSource(engine.getIOUtil().toBufferedInputStream(engine.getHTTPUtil().toURL(systemID).openStream()))
            } catch (t: Throwable) {
                if (t is ThreadDeath) throw t as ThreadDeath
                null
            }
        }

        init {
            this.entityRes = entityRes
        }
    }

    internal class ThrowingErrorHandler(private val throwFatalError: Boolean, private val throwError: Boolean, private val throwWarning: Boolean) : ErrorHandler {
        @Override
        @Throws(SAXException::class)
        fun error(e: SAXParseException?) {
            if (throwError) throw SAXException(e)
        }

        @Override
        @Throws(SAXException::class)
        fun fatalError(e: SAXParseException?) {
            if (throwFatalError) throw SAXException(e)
        }

        @Override
        @Throws(SAXException::class)
        fun warning(e: SAXParseException?) {
            if (throwWarning) throw SAXException(e)
        }
    }

    companion object {
        const val NON_VALIDATING_DTD_GRAMMAR = "http://apache.org/xml/features/nonvalidating/load-dtd-grammar"
        const val NON_VALIDATING_DTD_EXTERNAL = "http://apache.org/xml/features/nonvalidating/load-external-dtd"
        const val VALIDATION_SCHEMA = "http://apache.org/xml/features/validation/schema"
        const val VALIDATION_SCHEMA_FULL_CHECKING = "http://apache.org/xml/features/validation/schema-full-checking"
        const val UNDEFINED_NODE: Short = -1

        /*
	 * public static final Collection.Key XMLCOMMENT = KeyImpl.intern("xmlcomment"); public static final
	 * Collection.Key XMLTEXT = KeyImpl.intern("xmltext"); public static final Collection.Key XMLCDATA =
	 * KeyImpl.intern("xmlcdata"); public static final Collection.Key XMLCHILDREN =
	 * KeyImpl.intern("xmlchildren"); public static final Collection.Key XMLNODES =
	 * KeyImpl.intern("xmlnodes"); public static final Collection.Key XMLNSURI =
	 * KeyImpl.intern("xmlnsuri"); public static final Collection.Key XMLNSPREFIX =
	 * KeyImpl.intern("xmlnsprefix"); public static final Collection.Key XMLROOT =
	 * KeyImpl.intern("xmlroot"); public static final Collection.Key XMLPARENT =
	 * KeyImpl.intern("xmlparent"); public static final Collection.Key XMLNAME =
	 * KeyImpl.intern("xmlname"); public static final Collection.Key XMLTYPE =
	 * KeyImpl.intern("xmltype"); public static final Collection.Key XMLVALUE =
	 * KeyImpl.intern("xmlvalue"); public static final Collection.Key XMLATTRIBUTES =
	 * KeyImpl.intern("xmlattributes");
	 */
        // static DOMParser parser = new DOMParser();
        private var docBuilder: DocumentBuilder? = null

        // private static DocumentBuilderFactory factory;
        private var transformerFactory: TransformerFactory? = null

        /*
	 * public String unescapeXMLString2(String str) {
	 * 
	 * StringBuffer sb=new StringBuffer(); int index,last=0,indexSemi;
	 * while((index=str.indexOf('&',last))!=-1) { sb.append(str.substring(last,index));
	 * indexSemi=str.indexOf(';',index+1);
	 * 
	 * if(indexSemi==-1) { sb.append('&'); last=index+1; } else if(index+1==indexSemi) {
	 * sb.append("&;"); last=index+2; } else {
	 * sb.append(unescapeXMLEntity(str.substring(index+1,indexSemi))); last=indexSemi+1; } }
	 * sb.append(str.substring(last)); return sb.toString(); }
	 */
        private fun unescapeXMLEntity(str: String): String {
            if ("lt".equals(str)) return "<"
            if ("gt".equals(str)) return ">"
            if ("amp".equals(str)) return "&"
            if ("apos".equals(str)) return "'"
            return if ("quot".equals(str)) "\"" else "&$str;"
        }

        fun transformerFactory(): TransformerFactory? {
            if (transformerFactory == null) {
                try {
                    val clazz: Class<*> = CFMLEngineFactory.getInstance().getClassUtil().loadClass("lucee.runtime.text.xml.XMLUtil")
                    transformerFactory = clazz.getMethod("getTransformerFactory", arrayOfNulls<Class>(0)).invoke(null, arrayOfNulls<Object>(0)) as TransformerFactory
                } catch (e: Exception) {
                    e.printStackTrace()
                    transformerFactory = TransformerFactory.newInstance()
                }
            }
            return transformerFactory
        }

        private fun setAttributeEL(factory: DocumentBuilderFactory, name: String, value: Object) {
            try {
                factory.setAttribute(name, value)
            } catch (t: Throwable) {
                if (t is ThreadDeath) throw t as ThreadDeath
                // SystemOut.printDate("attribute ["+name+"] is not allowed for
                // ["+factory.getClass().getName()+"]");
            }
        }
    }
}