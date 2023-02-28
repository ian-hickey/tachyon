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
package lucee.runtime.functions.xml
//import org.apache.xpath.XPathAPI;
//import org.apache.xpath.objects.XObject;
import java.lang.ref.SoftReference

/**
 * Implements the CFML Function xmlsearch
 */
object XmlSearch : Function {
    /*
	 * static { System.setProperty("-Dorg.apache.xml.dtm.DTMManager",
	 * "org.apache.xml.dtm.ref.DTMManagerDefault"); System.setProperty("org.apache.xml.dtm.DTMManager",
	 * "org.apache.xml.dtm.ref.DTMManagerDefault");
	 * System.setProperty("-Dcom.sun.org.apache.xml.internal.dtm.DTMManager",
	 * "com.sun.org.apache.xml.internal.dtm.ref.DTMManagerDefault");
	 * System.setProperty("com.sun.org.apache.xml.internal.dtm.DTMManager",
	 * "com.sun.org.apache.xml.internal.dtm.ref.DTMManagerDefault"); }
	 */
    private const val serialVersionUID = 5770611088309897382L
    private val operators: List<String?>? = ArrayList<String?>()
    private val exprs: Map<String?, SoftReference<Tmp?>?>? = ConcurrentHashMap<String?, SoftReference<Tmp?>?>(10, 0.75f)
    private var factory: XPathFactory? = null
    @Throws(PageException::class)
    fun call(pc: PageContext?, node: Node?, expr: String?): Object? {
        var node: Node? = node
        var caseSensitive = true
        if (node is XMLObject) {
            caseSensitive = (node as XMLObject?).getCaseSensitive()
        }
        if (node is XMLStruct) {
            node = (node as XMLStruct?).toNode()
        }
        return _call(node, expr, caseSensitive)
    }

    @Throws(PageException::class)
    fun _call(node: Node?, strExpr: String?, caseSensitive: Boolean): Object? {
        var strExpr = strExpr
        if (StringUtil.endsWith(strExpr, '/')) strExpr = strExpr.substring(0, strExpr!!.length() - 1)

        // compile
        var tmp: Tmp? = null
        run {
            try {
                if (factory == null) factory = XPathFactory.newInstance()
                val doc: Document = XMLUtil.getDocument(node)
                val t: SoftReference<Tmp?>? = exprs!![strExpr]
                tmp = if (t == null) null else t.get()
                if (tmp == null) {
                    tmp = Tmp()
                    val path: XPath = factory.newXPath()
                    path.setNamespaceContext(UniversalNamespaceResolver(doc).also { tmp!!.unr = it })
                    tmp!!.expr = path.compile(strExpr)
                    if (exprs.size() > 100) exprs.clear()
                    exprs.put(strExpr, SoftReference<Tmp?>(tmp))
                } else {
                    tmp!!.unr!!.setDocument(doc)
                }
            } catch (e: Exception) {
                throw Caster.toPageException(e)
            }
        }

        // evaluate
        return try {
            val obj: Object = tmp!!.expr.evaluate(node, XPathConstants.NODESET)
            nodelist(obj as NodeList, caseSensitive)
        } catch (e: XPathExpressionException) {
            var msg: String = e.getMessage()
            if (msg == null) msg = ""
            try {
                if (msg.indexOf("#BOOLEAN") !== -1) return Caster.toBoolean(tmp!!.expr.evaluate(node, XPathConstants.BOOLEAN)) else if (msg.indexOf("#NUMBER") !== -1) return Caster.toDouble(tmp!!.expr.evaluate(node, XPathConstants.NUMBER)) else if (msg.indexOf("#STRING") !== -1) return Caster.toString(tmp!!.expr.evaluate(node, XPathConstants.STRING))
                // TODO XObject.CLASS_NULL ???
            } catch (ee: XPathExpressionException) {
                throw Caster.toPageException(ee)
            }
            if (msg.equals("java.lang.NullPointerException")) {
                throw RuntimeException("Failed to parse XML with XPathExpressionException which threw a "
                        + "java.lang.NullPointerException, possibly due to security restrictions set by XMLFeatures", e)
            }
            throw Caster.toPageException(e)
        } catch (e: TransformerException) {
            throw Caster.toPageException(e)
        } finally {
            tmp!!.unr!!.setDocument(null) // we remove the doc to keep the cache size small
        }
    }

    @Throws(TransformerException::class, PageException::class)
    private fun nodelist(list: NodeList?, caseSensitive: Boolean): Array? {
        // NodeList list = rs.nodelist();
        val len: Int = list.getLength()
        val rtn: Array = ArrayImpl()
        for (i in 0 until len) {
            val n: Node = list.item(i)
            if (n != null) rtn.append(XMLCaster.toXMLStruct(n, caseSensitive))
        }
        return rtn
    }

    private class Tmp {
        val expr: XPathExpression? = null
        val unr: UniversalNamespaceResolver? = null
    }

    private class UniversalNamespaceResolver(document: Document?) : NamespaceContext {
        // the delegate
        private var sourceDocument: Document?
        fun setDocument(document: Document?) {
            sourceDocument = document
        }

        /**
         * The lookup for the namespace uris is delegated to the stored document.
         *
         * @param prefix to search for
         * @return uri
         */
        @Override
        fun getNamespaceURI(prefix: String?): String? {
            return if (prefix!!.equals(XMLConstants.DEFAULT_NS_PREFIX)) {
                sourceDocument.lookupNamespaceURI(null)
            } else {
                sourceDocument.lookupNamespaceURI(prefix)
            }
        }

        /**
         * This method is not needed in this context, but can be implemented in a similar way.
         */
        @Override
        fun getPrefix(namespaceURI: String?): String? {
            return sourceDocument.lookupPrefix(namespaceURI)
        }

        @Override
        fun getPrefixes(namespaceURI: String?): Iterator? {
            // not implemented yet
            return null
        }

        /**
         * This constructor stores the source document to search the namespaces in it.
         *
         * @param document source document
         */
        init {
            sourceDocument = document
        }
    }

    init {
        operators.add("=")
        operators.add("<>")
    }
}