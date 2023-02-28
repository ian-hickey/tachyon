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
package lucee.runtime.text.feed

import java.io.IOException

class RSSHandler : DefaultHandler {
    private var xmlReader: XMLReader? = null
    private var lcInside: String? = null
    private var content: StringBuffer? = StringBuffer()
    private var insideImage = false
    private var insideItem = false
    private var image: Struct? = null
    private var properties: Struct? = null
    private var items: Query? = null
    private var inside: Collection.Key? = null

    /**
     * Constructor of the class
     *
     * @param res
     * @throws IOException
     * @throws SAXException
     * @throws DatabaseException
     */
    constructor(res: Resource?) {
        var `is`: InputStream? = null
        try {
            val source = InputSource(res.getInputStream().also { `is` = it })
            source.setSystemId(res.getPath())
            init(source)
        } finally {
            IOUtil.close(`is`)
        }
    }

    /**
     * Constructor of the class
     *
     * @param stream
     * @throws IOException
     * @throws SAXException
     * @throws DatabaseException
     */
    constructor(stream: InputStream?) {
        val `is` = InputSource(IOUtil.getReader(stream, SystemUtil.getCharset()))
        init(`is`)
    }

    @Throws(SAXException::class, IOException::class, DatabaseException::class)
    private fun init(`is`: InputSource?) {
        properties = StructImpl()
        items = QueryImpl(COLUMNS, 0, "query")
        xmlReader = XMLUtil.createXMLReader()
        xmlReader.setContentHandler(this)
        xmlReader.setErrorHandler(this)

        // xmlReader.setEntityResolver(new TagLibEntityResolver());
        xmlReader.parse(`is`)

        // properties.setEL("encoding",is.getEncoding());
    }

    @Override
    fun setDocumentLocator(locator: Locator?) {
        if (locator is Locator2) {
            val locator2: Locator2? = locator as Locator2?
            properties.setEL("encoding", locator2.getEncoding())
        }
    }

    @Override
    fun startElement(uri: String?, name: String?, qName: String?, atts: Attributes?) {
        inside = KeyImpl.getInstance(qName)
        lcInside = qName.toLowerCase()
        if (lcInside!!.equals("image")) insideImage = true else if (qName!!.equals("item")) {
            items.addRow()
            insideItem = true
        } else if (lcInside!!.equals("rss")) {
            val version: String = atts.getValue("version")
            if (!StringUtil.isEmpty(version)) properties.setEL("version", "rss_$version")
        } else if (insideItem && lcInside!!.equals("enclosure")) {
            val url: String = atts.getValue("url")
            if (!StringUtil.isEmpty(url)) items.setAtEL("LINKHREF", items.getRowCount(), url)
            val length: String = atts.getValue("length")
            if (!StringUtil.isEmpty(length)) items.setAtEL("LINKLENGTH", items.getRowCount(), length)
            val type: String = atts.getValue("type")
            if (!StringUtil.isEmpty(type)) items.setAtEL("LINKTYPE", items.getRowCount(), type)
        } else if (atts.getLength() > 0) {
            val len: Int = atts.getLength()
            val sct: Struct = StructImpl()
            for (i in 0 until len) {
                sct.setEL(atts.getQName(i), atts.getValue(i))
            }
            properties.setEL(inside, sct)
        }
    }

    @Override
    fun endElement(uri: String?, name: String?, qName: String?) {
        setContent(content.toString().trim())
        content = StringBuffer()
        inside = null
        lcInside = ""
        if (qName!!.equals("image")) insideImage = false
        if (qName.equals("item")) insideItem = false
    }

    @Override
    fun characters(ch: CharArray?, start: Int, length: Int) {
        content.append(String(ch, start, length))
    }

    private fun setContent(value: String?) {
        if (StringUtil.isEmpty(lcInside)) return
        if (insideImage) {
            if (image == null) {
                image = StructImpl()
                properties.setEL("image", image)
            }
            image.setEL(inside, value)
        } else if (insideItem) {
            try {
                items.setAt(toItemColumn(inside), items.getRowCount(), value)
            } catch (e: PageException) {
                // print.err(inside);
            }
        } else {
            if (!(StringUtil.isEmpty(value, true) && properties.containsKey(inside))) properties.setEL(inside, value)
        }
    }

    private fun toItemColumn(key: Collection.Key?): Collection.Key? {
        if (key.equalsIgnoreCase(LINK)) return RSSLINK else if (key.equalsIgnoreCase(DESCRIPTION)) return CONTENT
        return key
    }

    /**
     * @return the properties
     */
    fun getProperties(): Struct? {
        return properties
    }

    /**
     * @return the items
     */
    fun getItems(): Query? {
        return items
    }

    companion object {
        private val RSSLINK: Key? = KeyImpl.getInstance("RSSLINK")
        private val CONTENT: Key? = KeyImpl.getInstance("CONTENT")
        private val LINK: Key? = KeyImpl.getInstance("LINK")
        private val DESCRIPTION: Key? = KeyImpl.getInstance("DESCRIPTION")
        private val COLUMNS: Array<Collection.Key?>? = arrayOf<Collection.Key?>(KeyImpl.getInstance("AUTHOREMAIL"), KeyImpl.getInstance("AUTHORNAME"), KeyImpl.getInstance("AUTHORURI"),
                KeyImpl.getInstance("CATEGORYLABEL"), KeyImpl.getInstance("CATEGORYSCHEME"), KeyImpl.getInstance("CATEGORYTERM"), KeyImpl.getInstance("COMMENTS"), CONTENT,
                KeyImpl.getInstance("CONTENTMODE"), KeyImpl.getInstance("CONTENTSRC"), KeyImpl.getInstance("CONTENTTYPE"), KeyImpl.getInstance("CONTRIBUTOREMAIL"),
                KeyImpl.getInstance("CONTRIBUTORNAME"), KeyImpl.getInstance("CONTRIBUTORURI"), KeyImpl.getInstance("CREATEDDATE"), KeyImpl.getInstance("EXPIRATIONDATE"),
                KeyConstants._ID, KeyImpl.getInstance("IDPERMALINK"), KeyImpl.getInstance("LINKHREF"), KeyImpl.getInstance("LINKHREFLANG"), KeyImpl.getInstance("LINKLENGTH"),
                KeyImpl.getInstance("LINKREL"), KeyImpl.getInstance("LINKTITLE"), KeyImpl.getInstance("LINKTYPE"), KeyImpl.getInstance("PUBLISHEDDATE"), KeyImpl.getInstance("RIGHTS"),
                RSSLINK, KeyImpl.getInstance("SOURCE"), KeyImpl.getInstance("SOURCEURL"), KeyImpl.getInstance("SUMMARY"), KeyImpl.getInstance("SUMMARYMODE"),
                KeyImpl.getInstance("SUMMARYSRC"), KeyImpl.getInstance("SUMMARYTYPE"), KeyImpl.getInstance("TITLE"), KeyImpl.getInstance("TITLETYPE"),
                KeyImpl.getInstance("UPDATEDDATE"), KeyImpl.getInstance("URI"), KeyImpl.getInstance("XMLBASE"))
    }
}