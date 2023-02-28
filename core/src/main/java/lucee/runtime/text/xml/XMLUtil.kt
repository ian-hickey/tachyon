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

import java.io.ByteArrayInputStream

/**
 *
 */
object XMLUtil {
    const val UNDEFINED_NODE: Short = -1
    val XMLCOMMENT: Collection.Key? = KeyImpl.getInstance("xmlcomment")
    val XMLTEXT: Collection.Key? = KeyImpl.getInstance("xmltext")
    val XMLCDATA: Collection.Key? = KeyImpl.getInstance("xmlcdata")
    val XMLCHILDREN: Collection.Key? = KeyImpl.getInstance("xmlchildren")
    val XMLNODES: Collection.Key? = KeyImpl.getInstance("xmlnodes")
    val XMLNSURI: Collection.Key? = KeyImpl.getInstance("xmlnsuri")
    val XMLNSPREFIX: Collection.Key? = KeyImpl.getInstance("xmlnsprefix")
    val XMLROOT: Collection.Key? = KeyImpl.getInstance("xmlroot")
    val XMLPARENT: Collection.Key? = KeyImpl.getInstance("xmlparent")
    val XMLNAME: Collection.Key? = KeyImpl.getInstance("xmlname")
    val XMLTYPE: Collection.Key? = KeyImpl.getInstance("xmltype")
    val XMLVALUE: Collection.Key? = KeyImpl.getInstance("xmlvalue")
    val XMLATTRIBUTES: Collection.Key? = KeyImpl.getInstance("xmlattributes")
    val KEY_FEATURE_SECURE: Collection.Key? = KeyConstants._secure
    val KEY_FEATURE_DISALLOW_DOCTYPE_DECL: Collection.Key? = KeyImpl.getInstance("disallowDoctypeDecl")
    val KEY_FEATURE_EXTERNAL_GENERAL_ENTITIES: Collection.Key? = KeyImpl.getInstance("externalGeneralEntities")

    // public final static String
    // DEFAULT_SAX_PARSER="org.apache.xerces.parsers.SAXParser";
    /*
	 * private static final Collection.Key = KeyImpl.getInstance(); private static final Collection.Key
	 * = KeyImpl.getInstance(); private static final Collection.Key = KeyImpl.getInstance(); private
	 * static final Collection.Key = KeyImpl.getInstance(); private static final Collection.Key =
	 * KeyImpl.getInstance(); private static final Collection.Key = KeyImpl.getInstance();
	 */
    // static DOMParser parser = new DOMParser();
    private var docBuilder: DocumentBuilder? = null

    /**
     * @return returns a singelton TransformerFactory
     */
    // private static DocumentBuilderFactory factory;
    var transformerFactory: TransformerFactory? = null
        get() {
            if (field == null) field = _newTransformerFactory()
            return field
        }
        private set

    // private static DocumentBuilderFactory documentBuilderFactory;
    private var saxParserFactory: SAXParserFactory? = null

    @get:Throws(IOException::class)
    var transformerFactoryResource: URL? = null
        get() {
            if (field == null) {
                val name = transformerFactoryName
                val localFile: Resource = SystemUtil.getTempDirectory().getRealResource(name.replace('\\', '_').replace('/', '_'))
                IOUtil.write(localFile, name.getBytes())
                field = (localFile as File).toURI().toURL()
            }
            return field
        }
        private set

    fun unescapeXMLString(str: String?): String? {
        val rtn = StringBuffer()
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

    fun unescapeXMLString2(str: String?): String? {
        val sb = StringBuffer()
        var index: Int
        var last = 0
        var indexSemi: Int
        while (str.indexOf('&', last).also { index = it } != -1) {
            sb.append(str.substring(last, index))
            indexSemi = str.indexOf(';', index + 1)
            last = if (indexSemi == -1) {
                sb.append('&')
                index + 1
            } else if (index + 1 == indexSemi) {
                sb.append("&;")
                index + 2
            } else {
                sb.append(unescapeXMLEntity(str.substring(index + 1, indexSemi)))
                indexSemi + 1
            }
        }
        sb.append(str.substring(last))
        return sb.toString()
    }

    private fun unescapeXMLEntity(str: String?): String? {
        if ("lt".equals(str)) return "<"
        if ("gt".equals(str)) return ">"
        if ("amp".equals(str)) return "&"
        if ("apos".equals(str)) return "'"
        return if ("quot".equals(str)) "\"" else "&$str;"
    }

    fun escapeXMLString(xmlStr: String?): String? {
        var c: Char
        val sb = StringBuffer()
        val len: Int = xmlStr!!.length()
        for (i in 0 until len) {
            c = xmlStr.charAt(i)
            if (c == '<') sb.append("&lt;") else if (c == '>') sb.append("&gt;") else if (c == '&') sb.append("&amp;") else if (c == '"') sb.append("&quot;") else sb.append(c)
        }
        return sb.toString()
    }

    val transformerFactoryName: String?
        get() = transformerFactory.getClass().getName()

    private fun _newTransformerFactory(): TransformerFactory? {
        Thread.currentThread().setContextClassLoader(EnvClassLoader.getInstance(ThreadLocalPageContext.getConfig() as ConfigPro))
        var factory: TransformerFactory? = null
        var clazz: Class? = null
        try {
            clazz = ClassUtil.loadClass("com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl")
        } catch (e: Exception) {
            try {
                clazz = ClassUtil.loadClass("org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl")
            } catch (ee: Exception) {
            }
        }
        if (clazz != null) {
            try {
                factory = ClassUtil.loadInstance(clazz) as TransformerFactory
            } catch (e: Exception) {
            }
        }
        if (factory == null) return TransformerFactory.newInstance().also { factory = it }
        LogUtil.log(Log.LEVEL_INFO, "application", "xml", factory.getClass().getName().toString() + " is used as TransformerFactory")
        return factory
    }

    @Throws(SAXException::class, IOException::class)
    fun parse(xml: InputSource?, validator: InputSource?, isHtml: Boolean): Document? {
        return parse(xml, validator, XMLEntityResolverDefaultHandler(validator), isHtml)
    }

    /**
     * parse XML/HTML String to a XML DOM representation
     *
     * @param xml XML InputSource
     * @param isHtml is a HTML or XML Object
     * @return parsed Document
     * @throws SAXException
     * @throws IOException
     * @throws ParserConfigurationException
     */
    @Throws(SAXException::class, IOException::class)
    fun parse(xml: InputSource?, validator: InputSource?, entRes: EntityResolver?, isHtml: Boolean): Document? {
        if (!isHtml) {
            val factory: DocumentBuilderFactory? = newDocumentBuilderFactory(validator)
            return try {
                val builder: DocumentBuilder = factory.newDocumentBuilder()
                if (entRes != null) builder.setEntityResolver(entRes)
                builder.setErrorHandler(ThrowingErrorHandler(true, true, false))
                builder.parse(xml)
            } catch (e: ParserConfigurationException) {
                throw SAXException(e)
            }
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

    private fun newDocumentBuilderFactory(validator: InputSource?): DocumentBuilderFactory? {
        val factory: DocumentBuilderFactory?
        if (validator != null) {
            factory = _newDocumentBuilderFactory() // DocumentBuilderFactory.newInstance();
            setAttributeEL(factory, XMLConstants.VALIDATION_SCHEMA, Boolean.TRUE)
            setAttributeEL(factory, XMLConstants.VALIDATION_SCHEMA_FULL_CHECKING, Boolean.TRUE)
            factory.setNamespaceAware(true)
            factory.setValidating(true)
        } else {
            factory = _newDocumentBuilderFactory() // DocumentBuilderFactory.newInstance();
            setAttributeEL(factory, XMLConstants.NON_VALIDATING_DTD_EXTERNAL, Boolean.FALSE)
            setAttributeEL(factory, XMLConstants.NON_VALIDATING_DTD_GRAMMAR, Boolean.FALSE)
            factory.setNamespaceAware(true)
            factory.setValidating(false)
        }
        val pc: PageContext = ThreadLocalPageContext.get()
        if (pc != null) {
            val ac: ApplicationContextSupport = pc.getApplicationContext() as ApplicationContextSupport
            val features: Struct? = if (ac == null) null else ac.getXmlFeatures()
            if (features != null) {
                try { // handle feature aliases, e.g. secure
                    var obj: Object
                    var featureValue: Boolean
                    obj = features.get(KEY_FEATURE_SECURE, null)
                    if (obj != null) {
                        featureValue = Caster.toBoolean(obj)
                        if (featureValue) {
                            // set features per
                            // https://cheatsheetseries.owasp.org/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.html
                            factory.setFeature(XMLConstants.FEATURE_DISALLOW_DOCTYPE_DECL, true)
                            factory.setFeature(XMLConstants.FEATURE_EXTERNAL_GENERAL_ENTITIES, false)
                            factory.setFeature(XMLConstants.FEATURE_EXTERNAL_PARAMETER_ENTITIES, false)
                            factory.setFeature(XMLConstants.FEATURE_NONVALIDATING_LOAD_EXTERNAL_DTD, false)
                            factory.setXIncludeAware(false)
                            factory.setExpandEntityReferences(false)
                            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "")
                            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "")
                        }
                        features.remove(KEY_FEATURE_SECURE)
                    }
                    obj = features.get(KEY_FEATURE_DISALLOW_DOCTYPE_DECL, null)
                    if (obj != null) {
                        featureValue = Caster.toBoolean(obj)
                        factory.setFeature(XMLConstants.FEATURE_DISALLOW_DOCTYPE_DECL, featureValue)
                        features.remove(KEY_FEATURE_DISALLOW_DOCTYPE_DECL)
                    }
                    obj = features.get(KEY_FEATURE_EXTERNAL_GENERAL_ENTITIES, null)
                    if (obj != null) {
                        featureValue = Caster.toBoolean(obj)
                        factory.setFeature(XMLConstants.FEATURE_EXTERNAL_GENERAL_ENTITIES, featureValue)
                        features.remove(KEY_FEATURE_EXTERNAL_GENERAL_ENTITIES)
                    }
                } catch (ex: PageException) {
                    throw RuntimeException(ex)
                } catch (ex: ParserConfigurationException) {
                    throw RuntimeException(ex)
                }
                features.forEach { k, v ->
                    try {
                        factory.setFeature(k.toString().toLowerCase(), Caster.toBoolean(v))
                    } catch (ex: PageException) {
                        throw RuntimeException(ex)
                    } catch (ex: ParserConfigurationException) {
                        throw RuntimeException(ex)
                    }
                }
            }
        }
        return factory
    }

    private var dbf: Class<DocumentBuilderFactory?>? = null

    @get:Throws(IOException::class)
    var documentBuilderFactoryResource: URL? = null
        get() {
            if (field == null) {
                val name = documentBuilderFactoryName
                val localFile: Resource = SystemUtil.getTempDirectory().getRealResource(name.replace('\\', '_').replace('/', '_'))
                IOUtil.write(localFile, name.getBytes())
                field = (localFile as File).toURI().toURL()
            }
            return field
        }
        private set
    private var saxParserFactoryResource: URL? = null
    private fun _newDocumentBuilderFactoryClass(): Class<DocumentBuilderFactory?>? {
        if (dbf == null) {
            Thread.currentThread().setContextClassLoader(EnvClassLoader.getInstance(ThreadLocalPageContext.getConfig() as ConfigPro))
            var clazz: Class<DocumentBuilderFactory?>? = null
            try {
                clazz = ClassUtil.loadClass("com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl")
            } catch (e: Exception) {
                try {
                    clazz = ClassUtil.loadClass("org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl")
                } catch (ee: Exception) {
                }
            }
            if (clazz != null) {
                dbf = clazz
                LogUtil.log(Log.LEVEL_INFO, "application", "xml", clazz.getName().toString() + " is used as DocumentBuilderFactory")
            }
        }
        return dbf
    }

    // TODO better impl, still used?
    val xMLParserConfigurationName: String?
        get() {
            val value = "org.apache.xerces.parsers.XIncludeAwareParserConfiguration"
            System.setProperty("org.apache.xerces.xni.parser.XMLParserConfiguration", value)
            return value // TODO better impl, still used?
        }
    val documentBuilderFactoryName: String?
        get() {
            val clazz: Class<DocumentBuilderFactory?>? = _newDocumentBuilderFactoryClass()
            return if (clazz != null) clazz.getName() else DocumentBuilderFactory.newInstance().getClass().getName()
        }

    // used by xmlbuilder! do nt change
    private fun _newDocumentBuilderFactory(): DocumentBuilderFactory? {
        val clazz: Class<DocumentBuilderFactory?>? = _newDocumentBuilderFactoryClass()
        var factory: DocumentBuilderFactory? = null
        if (clazz != null) {
            try {
                factory = ClassUtil.loadInstance(clazz) as DocumentBuilderFactory
            } catch (e: Exception) {
            }
        }
        if (factory == null) factory = DocumentBuilderFactory.newInstance()
        return factory
    }

    private fun newSAXParserFactory(): SAXParserFactory? {
        if (saxParserFactory == null) {
            Thread.currentThread().setContextClassLoader(EnvClassLoader.getInstance(ThreadLocalPageContext.getConfig() as ConfigPro))
            saxParserFactory = SAXParserFactory.newInstance()
        }
        return saxParserFactory
    }

    val sAXParserFactoryName: String?
        get() = newSAXParserFactory().getClass().getName()

    @get:Throws(IOException::class)
    val sAXParserFactoryResource: URL?
        get() {
            if (saxParserFactoryResource == null) {
                val name = sAXParserFactoryName
                val localFile: Resource = SystemUtil.getTempDirectory().getRealResource(name.replace('\\', '_').replace('/', '_'))
                IOUtil.write(localFile, name.getBytes())
                saxParserFactoryResource = (localFile as File).toURI().toURL()
            }
            return saxParserFactoryResource
        }

    @Throws(SAXException::class)
    fun createXMLReader(): XMLReader? {
        Thread.currentThread().setContextClassLoader(EnvClassLoader.getInstance(ThreadLocalPageContext.getConfig() as ConfigPro))
        try {
            return XMLReaderFactory.createXMLReader("com.sun.org.apache.xerces.internal.parsers.SAXParser")
        } catch (e: Exception) {
        }
        try {
            return XMLReaderFactory.createXMLReader("org.apache.xerces.internal.parsers.SAXParser")
        } catch (ee: Exception) {
        }
        try {
            return XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser")
        } catch (ee: Exception) {
        }
        return try {
            newSAXParserFactory().newSAXParser().getXMLReader()
        } catch (pce: ParserConfigurationException) {
            throw RuntimeException(pce)
        }
    }

    private fun setAttributeEL(factory: DocumentBuilderFactory?, name: String?, value: Object?) {
        try {
            factory.setAttribute(name, value)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
        }
    }

    /**
     * sets a node to a node (Expression Less)
     *
     * @param node
     * @param key
     * @param value
     * @return Object set
     */
    fun setPropertyEL(node: Node?, key: Collection.Key?, value: Object?): Object? {
        return try {
            setProperty(node, key, value)
        } catch (e: PageException) {
            null
        }
    }

    fun setProperty(node: Node?, key: Collection.Key?, value: Object?, caseSensitive: Boolean, defaultValue: Object?): Object? {
        return try {
            setProperty(node, key, value, caseSensitive)
        } catch (e: PageException) {
            defaultValue
        }
    }

    /**
     * sets a node to a node
     *
     * @param node
     * @param k
     * @param value
     * @return Object set
     * @throws PageException
     */
    @Throws(PageException::class)
    fun setProperty(node: Node?, k: Collection.Key?, value: Object?): Object? {
        return setProperty(node, k, value, isCaseSensitve(node))
    }

    @Throws(PageException::class)
    fun setProperty(node: Node?, k: Collection.Key?, value: Object?, caseSensitive: Boolean): Object? {
        val doc: Document = getDocument(node)
        var isXMLChildren: Boolean
        // Comment
        if (k.equals(XMLCOMMENT)) {
            removeChildren(XMLCaster.toRawNode(node), Node.COMMENT_NODE, false)
            node.appendChild(XMLCaster.toRawNode(XMLCaster.toComment(doc, value)))
        } else if (k.equals(XMLNSURI)) {
            // TODO impl
            throw ExpressionException("XML NS URI can't be set", "not implemented")
        } else if (k.equals(XMLNSPREFIX)) {
            // TODO impl
            throw ExpressionException("XML NS Prefix can't be set", "not implemented")
            // node.setPrefix(Caster.toString(value));
        } else if (k.equals(XMLROOT)) {
            doc.appendChild(XMLCaster.toNode(doc, value, false))
        } else if (k.equals(XMLPARENT)) {
            var parent: Node? = getParentNode(node, caseSensitive)
            val name: Key = KeyImpl.init(parent.getNodeName())
            parent = getParentNode(parent, caseSensitive)
            if (parent == null) throw ExpressionException("there is no parent element, you are already on the root element")
            return setProperty(parent, name, value, caseSensitive)
        } else if (k.equals(XMLNAME)) {
            throw XMLException("You can't assign a new value for the property [xmlname]")
        } else if (k.equals(XMLTYPE)) {
            throw XMLException("You can't change type of a xml node [xmltype]")
        } else if (k.equals(XMLVALUE)) {
            node.setNodeValue(Caster.toString(value))
        } else if (k.equals(XMLATTRIBUTES)) {
            val parent: Element = XMLCaster.toElement(doc, node)
            val attres: Array<Attr?> = XMLCaster.toAttrArray(doc, value)
            // print.ln("=>"+value);
            for (i in attres.indices) {
                if (attres[i] != null) {
                    parent.setAttributeNode(attres[i])
                    // print.ln(attres[i].getName()+"=="+attres[i].getValue());
                }
            }
        } else if (k.equals(XMLTEXT)) {
            removeChildCharacterData(XMLCaster.toRawNode(node), false)
            node.appendChild(XMLCaster.toRawNode(XMLCaster.toText(doc, value)))
        } else if (k.equals(XMLCDATA)) {
            removeChildCharacterData(XMLCaster.toRawNode(node), false)
            node.appendChild(XMLCaster.toRawNode(XMLCaster.toCDATASection(doc, value)))
        } else if (k.equals(XMLCHILDREN).also { isXMLChildren = it } || k.equals(XMLNODES)) {
            val nodes: Array<Node?> = XMLCaster.toNodeArray(doc, value)
            removeChildren(XMLCaster.toRawNode(node), if (isXMLChildren) Node.ELEMENT_NODE else UNDEFINED_NODE, false)
            for (i in nodes.indices) {
                if (nodes[i] === node) throw XMLException("can't assign a XML Node to himself")
                if (nodes[i] != null) node.appendChild(XMLCaster.toRawNode(nodes[i]))
            }
        } else {
            var isIndex = false
            val child: Node = XMLCaster.toNode(doc, value, false)
            if (!k.getString().equalsIgnoreCase(child.getNodeName()) && !Decision.isInteger(k).also { isIndex = it }) {
                throw XMLException("if you assign a XML Element to a XMLStruct , assignment property must have same name like XML Node Name",
                        "Property Name is " + k.getString().toString() + " and XML Element Name is " + child.getNodeName())
            }
            var n: Node

            // by index
            if (isIndex) {
                val list: NodeList? = getChildNodes(node.getParentNode(), Node.ELEMENT_NODE, true, node.getNodeName())
                val len: Int = list.getLength()
                val index: Int = Caster.toIntValue(k)
                if (index > len || index < 1) {
                    val detail = if (len > 1) "your index is $index, but there are only $len child elements" else "your index is $index, but there is only $len child element"
                    throw XMLException("index is out of range", detail)
                }
                n = list.item(index - 1)
                replaceChild(child, n)
                return value
            }
            val list: NodeList? = getChildNodes(node, Node.ELEMENT_NODE)
            val len: Int = list.getLength()

            // by name
            for (i in 0 until len) {
                n = list.item(i)
                if (nameEqual(n, k.getString(), caseSensitive)) {
                    replaceChild(child, n)
                    return value
                }
            }
            node.appendChild(XMLCaster.toRawNode(child))
        }
        return value
    }

    fun replaceChild(newChild: Node?, oldChild: Node?) {
        val nc: Node = XMLCaster.toRawNode(newChild)
        val oc: Node = XMLCaster.toRawNode(oldChild)
        val p: Node = oc.getParentNode()
        if (nc !== oc) p.replaceChild(nc, oc)
    }

    fun getProperty(node: Node?, key: Collection.Key?, defaultValue: Object?): Object? {
        return getProperty(node, key, isCaseSensitve(node), defaultValue)
    }

    /**
     * returns a property from a XMl Node (Expression Less)
     *
     * @param node
     * @param k
     * @param caseSensitive
     * @return Object matching key
     */
    fun getProperty(node: Node?, k: Collection.Key?, caseSensitive: Boolean, defaultValue: Object?): Object? {
        return try {
            getProperty(node, k, caseSensitive)
        } catch (e: SAXException) {
            defaultValue
        }
    }

    @Throws(SAXException::class)
    fun getProperty(node: Node?, key: Collection.Key?): Object? {
        return getProperty(node, key, isCaseSensitve(node))
    }

    /**
     * returns a property from a XMl Node
     *
     * @param node
     * @param k
     * @param caseSensitive
     * @return Object matching key
     * @throws SAXException
     */
    @Throws(SAXException::class)
    fun getProperty(node: Node?, k: Collection.Key?, caseSensitive: Boolean): Object? {
        // String lcKey=StringUtil.toLowerCase(key);
        var node: Node? = node
        if (k.getLowerString().startsWith("xml")) {
            // Comment
            if (k.equals(XMLCOMMENT)) {
                val sb = StringBuffer()
                val list: NodeList = node.getChildNodes()
                val len: Int = list.getLength()
                for (i in 0 until len) {
                    val n: Node = list.item(i)
                    if (n is Comment) {
                        sb.append((n as Comment).getData())
                    }
                }
                return sb.toString()
            }
            // NS URI
            if (k.equals(XMLNSURI)) {
                undefinedInRoot(k, node)
                return param(node.getNamespaceURI(), "")
            }
            // Prefix
            if (k.equals(XMLNSPREFIX)) {
                undefinedInRoot(k, node)
                return param(node.getPrefix(), "")
            } else if (k.equals(XMLROOT)) {
                val re: Element = getRootElement(node, caseSensitive)
                        ?: throw SAXException("Attribute [" + k.getString().toString() + "] not found in XML, XML is empty")
                return param(re, "")
            } else if (k.equals(XMLPARENT)) {
                val parent: Node? = getParentNode(node, caseSensitive)
                if (parent == null) {
                    if (node.getNodeType() === Node.DOCUMENT_NODE) throw SAXException("Attribute [" + k.getString().toString() + "] not found in XML, there is no parent element, you are already at the root element")
                    throw SAXException("Attribute [" + k.getString().toString() + "] not found in XML, there is no parent element")
                }
                return parent
            } else if (k.equals(XMLNAME)) {
                return node.getNodeName()
            } else if (k.equals(XMLVALUE)) {
                return StringUtil.toStringEmptyIfNull(node.getNodeValue())
            } else if (k.equals(XMLTYPE)) {
                return getTypeAsString(node, true)
            } else if (k.equals(XMLATTRIBUTES)) {
                val attr: NamedNodeMap = node.getAttributes() ?: throw undefined(k, node)
                return XMLAttributes(node, caseSensitive)
            } else if (k.equals(XMLTEXT)) {
                undefinedInRoot(k, node)
                if (node is Text || node is CDATASection) return (node as CharacterData?).getData()
                val sb = StringBuilder()
                val list: NodeList = node.getChildNodes()
                val len: Int = list.getLength()
                for (i in 0 until len) {
                    val n: Node = list.item(i)
                    if (n is Text || n is CDATASection) {
                        sb.append((n as CharacterData).getData())
                    }
                }
                return sb.toString()
            } else if (k.equals(XMLCDATA)) {
                undefinedInRoot(k, node)
                val sb = StringBuffer()
                val list: NodeList = node.getChildNodes()
                val len: Int = list.getLength()
                for (i in 0 until len) {
                    val n: Node = list.item(i)
                    if (n is Text || n is CDATASection) {
                        sb.append((n as CharacterData).getData())
                    }
                }
                return sb.toString()
            } else if (k.equals(XMLCHILDREN)) {
                return XMLNodeList(node, caseSensitive, Node.ELEMENT_NODE)
            } else if (k.equals(XMLNODES)) {
                return XMLNodeList(node, caseSensitive, UNDEFINED_NODE)
            }
        }
        if (node is Document) {
            node = (node as Document?).getDocumentElement()
            if (node == null) throw SAXException("Attribute [" + k.getString().toString() + "] not found in XML, XML is empty")

            // if((!caseSensitive && node.getNodeName().equalsIgnoreCase(k.getString())) ||
            // (caseSensitive &&
            // node.getNodeName().equals(k.getString()))) {
            if (nameEqual(node, k.getString(), caseSensitive)) {
                return XMLStructFactory.newInstance(node, caseSensitive)
            }
        } else if (node.getNodeType() === Node.ELEMENT_NODE && Decision.isInteger(k)) {
            val index: Int = Caster.toIntValue(k, 0)
            var count = 0
            val parent: Node = node.getParentNode()
            val nodeName: String = node.getNodeName()
            val children: Array<Element?>? = getChildElementsAsArray(parent)
            for (i in children.indices) {
                if (nameEqual(children!![i], nodeName, caseSensitive)) count++
                if (count == index) return XMLCaster.toXMLStruct(children[i], caseSensitive)
            }
            val detail: String
            detail = if (count == 0) "there are no Elements with this name" else if (count == 1) "there is only 1 Element with this name" else "there are only $count Elements with this name"
            throw SAXException("invalid index [" + k.getString().toString() + "] for Element with name [" + node.getNodeName().toString() + "], " + detail)
        } else {
            val children: List<Node?>? = getChildNodesAsList(node, Node.ELEMENT_NODE, caseSensitive, null)
            val len: Int = children!!.size()
            var array: Array? = null // new ArrayImpl();
            var el: Element?
            var sct: XMLStruct? = null
            var first: XMLStruct? = null
            for (i in 0 until len) {
                el = children[i] as Element? // XMLCaster.toXMLStruct(getChildNode(index),caseSensitive);
                if (nameEqual(el, k.getString(), caseSensitive)) {
                    sct = XMLCaster.toXMLStruct(el, caseSensitive)
                    if (array != null) {
                        array.appendEL(sct)
                    } else if (first != null) {
                        array = ArrayImpl()
                        array.appendEL(first)
                        array.appendEL(sct)
                    } else {
                        first = sct
                    }
                }
            }
            if (array != null) {
                try {
                    return XMLMultiElementStruct(array, false)
                } catch (e: PageException) {
                }
            }
            if (first != null) return first
        }
        throw SAXException("Attribute [" + k.getString().toString() + "] not found")
    }

    private fun undefined(key: Key?, node: Node?): SAXException? {
        return if (node.getNodeType() === Node.DOCUMENT_NODE) SAXException(
                "you cannot address [$key] on the Document Object, to address [$key]  from the root Node use [{variable-name}.xmlRoot.$key]") else SAXException(key.toString() + " is undefined")
    }

    @Throws(SAXException::class)
    private fun undefinedInRoot(key: Key?, node: Node?) {
        if (node.getNodeType() === Node.DOCUMENT_NODE) throw undefined(key, node)
    }

    /**
     * check if given name is equal to name of the element (with and without namespace)
     *
     * @param node
     * @param name
     * @param caseSensitive
     * @return
     */
    fun nameEqual(node: Node?, name: String?, caseSensitive: Boolean): Boolean {
        if (name == null) return false
        return if (caseSensitive) {
            name.equals(node.getNodeName()) || name.equals(node.getLocalName())
        } else name.equalsIgnoreCase(node.getNodeName()) || name.equalsIgnoreCase(node.getLocalName())
    }

    fun isCaseSensitve(node: Node?): Boolean {
        return if (node is XMLStruct) (node as XMLStruct?).isCaseSensitive() else true
    }

    /**
     * removes child from a node
     *
     * @param node
     * @param k
     * @param caseSensitive
     * @return removed property
     */
    fun removeProperty(node: Node?, k: Collection.Key?, caseSensitive: Boolean): Object? {
        var isXMLChildren: Boolean
        // String lcKeyx=k.getLowerString();
        if (k.getLowerString().startsWith("xml")) {
            // Comment
            if (k.equals(XMLCOMMENT)) {
                val sb = StringBuffer()
                val list: NodeList = node.getChildNodes()
                val len: Int = list.getLength()
                for (i in 0 until len) {
                    val n: Node = list.item(i)
                    if (n is Comment) {
                        sb.append((n as Comment).getData())
                        node.removeChild(XMLCaster.toRawNode(n))
                    }
                }
                return sb.toString()
            } else if (k.equals(XMLTEXT)) {
                if (node is Text || node is CDATASection) return (node as CharacterData?).getData()
                val sb = StringBuilder()
                val list: NodeList = node.getChildNodes()
                val len: Int = list.getLength()
                for (i in 0 until len) {
                    val n: Node = list.item(i)
                    if (n is Text || n is CDATASection) {
                        sb.append((n as CharacterData).getData())
                        node.removeChild(XMLCaster.toRawNode(n))
                    }
                }
                return sb.toString()
            } else if (k.equals(XMLCHILDREN).also { isXMLChildren = it } || k.equals(XMLNODES)) {
                val list: NodeList = node.getChildNodes()
                var child: Node
                for (i in list.getLength() - 1 downTo 0) {
                    child = XMLCaster.toRawNode(list.item(i))
                    if (isXMLChildren && child.getNodeType() !== Node.ELEMENT_NODE) continue
                    node.removeChild(child)
                }
                return list
            }
        }
        val nodes: NodeList = node.getChildNodes()
        val array: Array = ArrayImpl()
        for (i in nodes.getLength() - 1 downTo 0) {
            val o: Object = nodes.item(i)
            if (o is Element) {
                val el: Element = o as Element
                if (nameEqual(el, k.getString(), caseSensitive)) {
                    array.appendEL(XMLCaster.toXMLStruct(el, caseSensitive))
                    node.removeChild(XMLCaster.toRawNode(el))
                }
            }
        }
        if (array.size() > 0) {
            try {
                return XMLMultiElementStruct(array, false)
            } catch (e: PageException) {
            }
        }
        return null
    }

    private fun param(o1: Object?, o2: Object?): Object? {
        return if (o1 == null) o2 else o1
    }

    /**
     * return the root Element from a node
     *
     * @param node node to get root element from
     * @param caseSensitive
     * @return Root Element
     */
    fun getRootElement(node: Node?, caseSensitive: Boolean): Element? {
        val doc: Document = getDocument(node)
        val el: Element = doc.getDocumentElement() ?: return null
        return XMLStructFactory.newInstance(el, caseSensitive) as Element
    }

    fun getParentNode(node: Node?, caseSensitive: Boolean): Node? {
        val parent: Node = node.getParentNode() ?: return null
        return XMLStructFactory.newInstance(parent, caseSensitive)
    }

    /**
     * returns a new Empty XMl Document
     *
     * @return new Document
     * @throws ParserConfigurationException
     * @throws FactoryConfigurationError
     */
    @Throws(ParserConfigurationException::class, FactoryConfigurationError::class)
    fun newDocument(): Document? {
        if (docBuilder == null) {
            docBuilder = newDocumentBuilderFactory(null).newDocumentBuilder()
        }
        return docBuilder.newDocument()
    }

    /**
     * return the Owner Document of a Node List
     *
     * @param nodeList
     * @return XML Document
     * @throws XMLException
     */
    @Throws(XMLException::class)
    fun getDocument(nodeList: NodeList?): Document? {
        if (nodeList is Document) return nodeList as Document?
        val len: Int = nodeList.getLength()
        for (i in 0 until len) {
            val node: Node = nodeList.item(i)
            if (node != null) return node.getOwnerDocument()
        }
        throw XMLException("can't get Document from NodeList, in NoteList are no Nodes")
    }

    /**
     * return the Owner Document of a Node
     *
     * @param node
     * @return XML Document
     */
    fun getDocument(node: Node?): Document? {
        return if (node is Document) node as Document? else node.getOwnerDocument()
    }

    /**
     * removes child elements from a specific type
     *
     * @param node node to remove elements from
     * @param type Type Definition to remove (Constant value from class Node)
     * @param deep remove also in sub nodes
     */
    private fun removeChildren(node: Node?, type: Short, deep: Boolean) {
        synchronized(sync(node)) {
            val list: NodeList = node.getChildNodes()
            for (i in list.getLength() downTo 0) {
                val n: Node = list.item(i) ?: continue
                if (n.getNodeType() === type || type == UNDEFINED_NODE) node.removeChild(XMLCaster.toRawNode(n)) else if (deep) removeChildren(n, type, deep)
            }
        }
    }

    /**
     * remove children from type CharacterData from a node, this includes Text,Comment and CDataSection
     * nodes
     *
     * @param node
     * @param deep
     */
    private fun removeChildCharacterData(node: Node?, deep: Boolean) {
        synchronized(sync(node)) {
            val list: NodeList = node.getChildNodes()
            for (i in list.getLength() downTo 0) {
                val n: Node = list.item(i) ?: continue
                if (n is CharacterData) node.removeChild(XMLCaster.toRawNode(n)) else if (deep) removeChildCharacterData(n, deep)
            }
        }
    }

    /**
     * return all Children of a node by a defined type as Node List
     *
     * @param node node to get children from
     * @param type type of returned node
     * @return all matching child node
     */
    fun getChildNodes(node: Node?, type: Short): ArrayNodeList? {
        return getChildNodes(node, type, false, null)
    }

    fun childNodesLength(node: Node?, type: Short, caseSensitive: Boolean, filter: String?): Int {
        synchronized(sync(node)) {
            val nodes: NodeList = node.getChildNodes()
            val len: Int = nodes.getLength()
            var n: Node
            var count = 0
            for (i in 0 until len) {
                try {
                    n = nodes.item(i)
                    if (n != null && (type == UNDEFINED_NODE || n.getNodeType() === type)) {
                        if (filter == null || (if (caseSensitive) filter.equals(n.getLocalName()) else filter.equalsIgnoreCase(n.getLocalName()))) count++
                    }
                } catch (t: Throwable) {
                    ExceptionUtil.rethrowIfNecessary(t)
                }
            }
            return count
        }
    }

    fun sync(node: Node?): Object? {
        val d: Document = getDocument(node)
        return if (d != null) d else node
    }

    @Synchronized
    fun getChildNodes(node: Node?, type: Short, caseSensitive: Boolean, filter: String?): ArrayNodeList? {
        val rtn = ArrayNodeList()
        val nodes: NodeList? = if (node == null) null else node.getChildNodes()
        val len = if (nodes == null) 0 else nodes.getLength()
        var n: Node
        for (i in 0 until len) {
            try {
                n = nodes.item(i)
                if (n != null && (type == UNDEFINED_NODE || n.getNodeType() === type)) {
                    if (filter == null || (if (caseSensitive) filter.equals(n.getLocalName()) else filter.equalsIgnoreCase(n.getLocalName()))) rtn.add(n)
                }
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
            }
        }
        return rtn
    }

    fun getChildNodesAsList(node: Node?, type: Short, caseSensitive: Boolean, filter: String?): List<Node?>? {
        synchronized(sync(node)) {
            val rtn: List<Node?> = ArrayList<Node?>()
            val nodes: NodeList = node.getChildNodes()
            val len: Int = nodes.getLength()
            var n: Node
            for (i in 0 until len) {
                try {
                    n = nodes.item(i)
                    if (n != null && (n.getNodeType() === type || type == UNDEFINED_NODE)) {
                        if (filter == null || (if (caseSensitive) filter.equals(n.getLocalName()) else filter.equalsIgnoreCase(n.getLocalName()))) rtn.add(n)
                    }
                } catch (t: Throwable) {
                    ExceptionUtil.rethrowIfNecessary(t)
                }
            }
            return rtn
        }
    }

    fun getChildNode(node: Node?, type: Short, caseSensitive: Boolean, filter: String?, index: Int): Node? {
        synchronized(sync(node)) {
            val nodes: NodeList = node.getChildNodes()
            val len: Int = nodes.getLength()
            var n: Node
            var count = 0
            for (i in 0 until len) {
                try {
                    n = nodes.item(i)
                    if (n != null && (type == UNDEFINED_NODE || n.getNodeType() === type)) {
                        if (filter == null || (if (caseSensitive) filter.equals(n.getLocalName()) else filter.equalsIgnoreCase(n.getLocalName()))) {
                            if (count == index) return n
                            count++
                        }
                    }
                } catch (t: Throwable) {
                    ExceptionUtil.rethrowIfNecessary(t)
                }
            }
            return null
        }
    }

    /**
     * return all Children of a node by a defined type as Node Array
     *
     * @param node node to get children from
     * @param type type of returned node
     * @return all matching child node
     */
    fun getChildNodesAsArray(node: Node?, type: Short): Array<Node?>? {
        val nodeList: ArrayNodeList? = getChildNodes(node, type)
        return nodeList.toArray(arrayOfNulls<Node?>(nodeList.getLength()))
    }

    fun getChildNodesAsArray(node: Node?, type: Short, caseSensitive: Boolean, filter: String?): Array<Node?>? {
        val nodeList: ArrayNodeList? = getChildNodes(node, type, caseSensitive, filter)
        return nodeList.toArray(arrayOfNulls<Node?>(nodeList.getLength()))
    }

    /**
     * return all Element Children of a node
     *
     * @param node node to get children from
     * @return all matching child node
     */
    fun getChildElementsAsArray(node: Node?): Array<Element?>? {
        val nodeList: ArrayNodeList? = getChildNodes(node, Node.ELEMENT_NODE)
        return nodeList.toArray(arrayOfNulls<Element?>(nodeList.getLength()))
    }

    /**
     * transform a XML Object to another format, with help of a XSL Stylesheet
     *
     * @param xml xml to convert
     * @param xsl xsl used to convert
     * @return resulting string
     * @throws TransformerException
     * @throws SAXException
     * @throws IOException
     */
    @Throws(TransformerException::class, SAXException::class, IOException::class)
    fun transform(xml: InputSource?, xsl: InputSource?): String? {
        return transform(parse(xml, null, false), xsl, null)
    }

    /**
     * transform a XML Object to another format, with help of a XSL Stylesheet
     *
     * @param xml xml to convert
     * @param xsl xsl used to convert
     * @param parameters parameters used to convert
     * @return resulting string
     * @throws TransformerException
     * @throws SAXException
     * @throws IOException
     */
    @Throws(TransformerException::class, SAXException::class, IOException::class)
    fun transform(xml: InputSource?, xsl: InputSource?, parameters: Map<String?, Object?>?): String? {
        return transform(parse(xml, null, false), xsl, parameters)
    }

    /**
     * transform a XML Document to another format, with help of a XSL Stylesheet
     *
     * @param doc xml to convert
     * @param xsl xsl used to convert
     * @return resulting string
     * @throws TransformerException
     * @throws SAXException
     * @throws IOException
     */
    @Throws(TransformerException::class)
    fun transform(doc: Document?, xsl: InputSource?): String? {
        return transform(doc, xsl, null)
    }

    /**
     * transform a XML Document to another format, with help of a XSL Stylesheet
     *
     * @param doc xml to convert
     * @param xsl xsl used to convert
     * @param parameters parameters used to convert
     * @return resulting string
     * @throws TransformerException
     * @throws SAXException
     * @throws IOException
     */
    @Throws(TransformerException::class)
    fun transform(doc: Document?, xsl: InputSource?, parameters: Map<String?, Object?>?): String? {
        val sw = StringWriter()
        val factory: TransformerFactory? = transformerFactory
        factory.setErrorListener(SimpleErrorListener.THROW_FATAL)
        val transformer: Transformer = factory.newTransformer(StreamSource(xsl.getCharacterStream()))
        if (parameters != null) {
            val it: Iterator<Entry<String?, Object?>?> = parameters.entrySet().iterator()
            var e: Entry<String?, Object?>?
            while (it.hasNext()) {
                e = it.next()
                transformer.setParameter(e.getKey(), e.getValue())
            }
        }
        transformer.transform(DOMSource(doc), StreamResult(sw))
        return sw.toString()
    }

    /**
     * returns the Node Type As String
     *
     * @param node
     * @param cftype
     * @return
     */
    fun getTypeAsString(node: Node?, cftype: Boolean): String? {
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

    fun getChildWithName(name: String?, el: Element?): Element? {
        synchronized(sync(el)) {
            val children: Array<Element?>? = getChildElementsAsArray(el)
            for (i in children.indices) {
                if (name.equalsIgnoreCase(children!![i].getNodeName())) return children!![i]
            }
        }
        return null
    }

    @Throws(IOException::class)
    fun toInputSource(res: Resource?, cs: Charset?): InputSource? {
        val str: String = IOUtil.toString(res, cs)
        return InputSource(StringReader(str))
    }

    @Throws(IOException::class, ExpressionException::class)
    fun toInputSource(pc: PageContext?, value: Object?): InputSource? {
        if (value is InputSource) {
            return value as InputSource?
        }
        if (value is String) {
            return toInputSource(pc, value as String?)
        }
        if (value is StringBuffer) {
            return toInputSource(pc, value.toString())
        }
        if (value is Resource) {
            val str: String = IOUtil.toString(value as Resource?, null as Charset?)
            return InputSource(StringReader(str))
        }
        if (value is File) {
            val str: String = IOUtil.toString(ResourceUtil.toResource(value as File?), null as Charset?)
            return InputSource(StringReader(str))
        }
        if (value is InputStream) {
            val `is`: InputStream? = value as InputStream?
            return try {
                val str: String = IOUtil.toString(`is`, null as Charset?)
                InputSource(StringReader(str))
            } finally {
                IOUtil.close(`is`)
            }
        }
        if (value is Reader) {
            val reader: Reader? = value as Reader?
            return try {
                val str: String = IOUtil.toString(reader)
                InputSource(StringReader(str))
            } finally {
                IOUtil.close(reader)
            }
        }
        if (value is ByteArray) {
            return InputSource(ByteArrayInputStream(value as ByteArray?))
        }
        throw ExpressionException("can't cast object of type [" + Caster.toClassName(value).toString() + "] to an Input for xml parser")
    }

    @Throws(IOException::class, ExpressionException::class)
    fun toInputSource(pc: PageContext?, xml: String?): InputSource? {
        return toInputSource(pc, xml, true)
    }

    @Throws(IOException::class, ExpressionException::class)
    fun toInputSource(pc: PageContext?, xml: String?, canBePath: Boolean): InputSource? {
        // xml text
        var pc: PageContext? = pc
        var xml = xml
        xml = xml.trim()
        if (!canBePath || xml.startsWith("<") || xml!!.length() > 2000 || StringUtil.isEmpty(xml, true)) {
            return InputSource(StringReader(xml))
        }
        // xml link
        pc = ThreadLocalPageContext.get(pc)
        val res: Resource = ResourceUtil.toResourceExisting(pc, xml)
        return toInputSource(pc, res)
    }

    /**
     * adds a child at the first place
     *
     * @param parent
     * @param child
     */
    fun prependChild(parent: Element?, child: Element?) {
        val first: Node = parent.getFirstChild()
        if (first == null) parent.appendChild(child) else {
            parent.insertBefore(child, first)
        }
    }

    fun setFirst(parent: Node?, node: Node?) {
        val first: Node = parent.getFirstChild()
        if (first != null) parent.insertBefore(node, first) else parent.appendChild(node)
    }

    @Throws(IOException::class, XMLException::class)
    fun createDocument(res: Resource?, isHTML: Boolean): Document? {
        val `is`: InputStream? = null
        return try {
            parse(toInputSource(res, null), null, isHTML)
        } catch (saxe: SAXException) {
            val msg: String = saxe.getMessage()
            if (msg != null || StringUtil.indexOfIgnoreCase(msg, "Premature end of file.") !== -1) {
                val content: String = IOUtil.toString(res, CharsetUtil.UTF8)
                val str: String
                str = if (content.isEmpty()) "XML File [" + res.getAbsolutePath().toString() + "] is empty;" + saxe.getMessage() else if (content.length() > content.trim().length()) "XML File [" + res.getAbsolutePath().toString() + "] is invalid, it has whitespaces at start or end;" + saxe.getMessage() else "XML File [" + res.getAbsolutePath().toString() + "] is invalid;" + saxe.getMessage()
                val se = XMLException(str)
                se.setAdditional(KeyConstants._path, res.getAbsolutePath())
                se.setAdditional(KeyConstants._content, content)
                se.setStackTrace(saxe.getStackTrace())
                throw se
            }
            throw XMLException(saxe)
        } finally {
            IOUtil.close(`is`)
        }
    }

    @Throws(SAXException::class, IOException::class)
    fun createDocument(xml: String?, isHTML: Boolean): Document? {
        return parse(toInputSource(xml), null, isHTML)
    }

    @Throws(SAXException::class, IOException::class)
    fun createDocument(`is`: InputStream?, isHTML: Boolean): Document? {
        return parse(InputSource(`is`), null, isHTML)
    }

    @Throws(IOException::class)
    fun toInputSource(value: Object?): InputSource? {
        if (value is InputSource) {
            return value as InputSource?
        }
        if (value is String) {
            return toInputSource(value as String?)
        }
        if (value is StringBuffer) {
            return toInputSource(value.toString())
        }
        if (value is Resource) {
            val str: String = IOUtil.toString(value as Resource?, null as Charset?)
            return InputSource(StringReader(str))
        }
        if (value is File) {
            val fis = FileInputStream(value as File?)
            return try {
                toInputSource(fis)
            } finally {
                IOUtil.close(fis)
            }
        }
        if (value is InputStream) {
            val `is`: InputStream? = value as InputStream?
            return try {
                val str: String = IOUtil.toString(`is`, null as Charset?)
                InputSource(StringReader(str))
            } finally {
                IOUtil.close(`is`)
            }
        }
        if (value is Reader) {
            val reader: Reader? = value as Reader?
            return try {
                val str: String = IOUtil.toString(reader)
                InputSource(StringReader(str))
            } finally {
                IOUtil.close(reader)
            }
        }
        if (value is ByteArray) {
            return InputSource(ByteArrayInputStream(value as ByteArray?))
        }
        throw IOException("can't cast object of type [$value] to an Input for xml parser")
    }

    @Throws(IOException::class)
    fun toInputSource(xml: String?): InputSource? {
        return InputSource(StringReader(xml.trim()))
    }

    @Throws(XMLException::class)
    fun validate(xml: InputSource?, schema: InputSource?, strSchema: String?, result: Struct?): Struct? {
        return XMLValidator(schema, strSchema).validate(xml, result)
    }
}