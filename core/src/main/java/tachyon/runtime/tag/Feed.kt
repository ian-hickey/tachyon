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
package tachyon.runtime.tag

import java.io.IOException

class Feed : TagImpl() {
    private var action = ACTION_READ
    private var columnMap: Struct? = null
    private var enclosureDir: Resource? = null
    private var ignoreEnclosureError = false
    private var name: Object? = null
    private var outputFile: Resource? = null
    private var overwrite = false
    private var overwriteEnclosure = false
    private var properties: Object? = null
    private var query: Object? = null
    private var source: Resource? = null
    private var timeout = -1
    private var type = TYPE_AUTO
    private var userAgent: String? = null
    private var xmlVar: String? = null
    private var proxyPassword: String? = null
    private var proxyPort = 80
    private var proxyServer: String? = null
    private var proxyUser: String? = null
    private var charset: String? = null
    @Override
    fun release() {
        charset = null
        action = ACTION_READ
        columnMap = null
        enclosureDir = null
        ignoreEnclosureError = false
        name = null
        outputFile = null
        overwrite = false
        overwriteEnclosure = false
        properties = null
        query = null
        source = null
        timeout = -1
        userAgent = null
        xmlVar = null
        proxyPassword = null
        proxyPort = 80
        proxyServer = null
        proxyUser = null
        type = TYPE_AUTO
        super.release()
    }

    /**
     * set the value charset Character set name for the file contents.
     *
     * @param charset value to set
     */
    fun setCharset(charset: String?) {
        this.charset = charset.trim()
    }

    /**
     * @param action the action to set
     * @throws ApplicationException
     */
    @Throws(ApplicationException::class)
    fun setAction(strAction: String?) {
        var strAction = strAction
        strAction = StringUtil.toLowerCase(strAction.trim())
        action = if ("read".equals(strAction)) ACTION_READ else if ("create".equals(strAction)) ACTION_CREATE else throw ApplicationException("invalid action definition [$strAction], valid action definitions are [create,read]")
    }

    @Throws(ApplicationException::class)
    fun setType(strType: String?) {
        var strType = strType
        strType = StringUtil.toLowerCase(strType.trim())
        type = if ("rss".equals(strType)) TYPE_RSS else if ("atom".equals(strType)) TYPE_ATOM else throw ApplicationException("invalid type definition [$strType], valid type definitions are [atom,rss]")
    }

    /**
     * @param columnMap the columnMap to set
     */
    fun setColumnmap(columnMap: Struct?) {
        this.columnMap = columnMap
    }

    /**
     * @param enclosureDir the enclosureDir to set
     * @throws ExpressionException
     */
    @Throws(ExpressionException::class)
    fun setEnclosuredir(strEnclosureDir: String?) {
        enclosureDir = ResourceUtil.toResourceExisting(pageContext, strEnclosureDir)
    }

    /**
     * @param ignoreEnclosureError the ignoreEnclosureError to set
     */
    fun setIgnoreenclosureerror(ignoreEnclosureError: Boolean) {
        this.ignoreEnclosureError = ignoreEnclosureError
    }

    /**
     * @param name the name to set
     */
    fun setName(name: Object?) {
        this.name = name
    }

    /**
     * @param outputFile the outputFile to set
     * @throws ExpressionException
     */
    @Throws(ExpressionException::class)
    fun setOutputfile(strOutputFile: String?) {
        outputFile = ResourceUtil.toResourceExistingParent(pageContext, strOutputFile)
    }

    /**
     * @param overwrite the overwrite to set
     */
    fun setOverwrite(overwrite: Boolean) {
        this.overwrite = overwrite
    }

    /**
     * @param overwriteEnclosure the overwriteEnclosure to set
     */
    fun setOverwriteenclosure(overwriteEnclosure: Boolean) {
        this.overwriteEnclosure = overwriteEnclosure
    }

    /**
     * @param properties the properties to set
     */
    fun setProperties(properties: Object?) {
        this.properties = properties
    }

    /**
     * @param query the query to set
     */
    fun setQuery(query: Object?) {
        this.query = query
    }

    /**
     * @param source the source to set
     * @throws ExpressionException
     */
    @Throws(ExpressionException::class)
    fun setSource(strSource: String?) {
        // when using toExistingResource execution fails because proxy is missed at this time
        source = ResourceUtil.toResourceNotExisting(pageContext, strSource)
    }

    /**
     * @param timeout the timeout to set
     */
    fun setTimeout(timeout: Double) {
        this.timeout = timeout.toInt()
    }

    /**
     * @param userAgent the userAgent to set
     */
    fun setUseragent(userAgent: String?) {
        this.userAgent = userAgent
    }

    /**
     * @param xmlVar the xmlVar to set
     */
    fun setXmlvar(xmlVar: String?) {
        this.xmlVar = xmlVar
    }

    /**
     * @param proxyPassword the proxyPassword to set
     */
    fun setProxypassword(proxyPassword: String?) {
        this.proxyPassword = proxyPassword
    }

    /**
     * @param proxyPort the proxyPort to set
     */
    fun setProxyport(proxyPort: Double) {
        this.proxyPort = proxyPort.toInt()
    }

    /**
     * @param proxyServer the proxyServer to set
     */
    fun setProxyserver(proxyServer: String?) {
        this.proxyServer = proxyServer
    }

    /**
     * @param proxyUser the proxyUser to set
     */
    fun setProxyuser(proxyUser: String?) {
        this.proxyUser = proxyUser
    }

    @Override
    @Throws(PageException::class)
    fun doStartTag(): Int {
        if (source is HTTPResource) {
            val httpSource: HTTPResource? = source as HTTPResource?
            if (!StringUtil.isEmpty(proxyServer, true)) {
                val data: ProxyData = ProxyDataImpl(proxyServer, proxyPort, proxyUser, proxyPassword)
                httpSource.setProxyData(data)
            }
            if (!StringUtil.isEmpty(userAgent)) httpSource.setUserAgent(userAgent)
            if (timeout > -1) httpSource.setTimeout(timeout * 1000)
        }
        try {
            if (ACTION_CREATE == action) doActionCreate() else if (ACTION_READ == action) doActionRead()
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
        return SKIP_BODY
    }

    @Throws(PageException::class)
    private fun doActionCreate() {

        // name
        val qry: Query
        val props: Struct
        var splitString = true
        if (name != null) {
            val data: Struct
            data = if (name is String) {
                Caster.toStruct(pageContext.getVariable(Caster.toString(name)))
            } else Caster.toStruct(name, false)
            qry = FeedQuery.toQuery(data, false)
            props = FeedProperties.toProperties(data)
            splitString = false
        } else if (query != null && properties != null) {
            qry = FeedQuery.toQuery(Caster.toQuery(query))
            props = FeedProperties.toProperties(Caster.toStruct(properties, false))
        } else {
            throw ApplicationException("missing attribute [name] or attributes [query] and [properties]")
        }
        val xml = StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
        if (type == TYPE_AUTO) {
            val version: String = Caster.toString(props.get("version", "rss"), "rss")
            type = if (StringUtil.startsWithIgnoreCase(version, "rss")) TYPE_RSS else TYPE_ATOM
        }
        if (type == TYPE_RSS) {
            createRSS(xml, qry, props, splitString)
        } else {
            createAtom(xml, qry, props, splitString)
        }

        // variable
        if (!StringUtil.isEmpty(xmlVar)) {
            pageContext.setVariable(xmlVar, xml)
        }
        // file
        if (outputFile != null) {
            if (outputFile.exists() && !overwrite) throw ApplicationException("destination file [$outputFile] already exists")
            if (StringUtil.isEmpty(charset)) charset = (pageContext as PageContextImpl?).getResourceCharset().name()
            try {
                IOUtil.write(outputFile, xml.toString(), charset, false)
            } catch (e: IOException) {
                throw Caster.toPageException(e)
            }
        }

        /*
		 * <cffeed action = "create" name = "#structure#" One or both of the following: outputFile = "path"
		 * xmlVar = "variable name" optional overwrite = "no|yes">
		 * 
		 * <cffeed action = "create" properties = "#metadata structure#" query =
		 * "#items/entries query name#" One or both of the following: outputFile = "path" xmlVar =
		 * "variable name" optional columnMap = "mapping structure" overwrite = "no|yes">
		 */
    }

    @Throws(PageException::class)
    private fun createAtom(xml: StringBuffer?, query: Query?, props: Struct?, splitString: Boolean) {
        val rows: Int = query.getRowCount()
        append(xml, 0, "<feed xmlns=\"http://www.w3.org/2005/Atom\">")
        propTag(props, xml, 1, arrayOf("title"), "title", arrayOf(arrayOf("type", "type")))
        propTag(props, xml, 1, arrayOf("subtitle"), "subtitle", arrayOf(arrayOf("type", "type")))
        propTag(props, xml, 1, arrayOf("updated"), "updated", null)
        propTag(props, xml, 1, arrayOf("id"), "id", null)
        propTag(props, xml, 1, arrayOf("link"), "link", arrayOf(arrayOf("rel", "rel"), arrayOf("type", "type"), arrayOf("hreflang", "hreflang"), arrayOf("href", "href")))
        propTag(props, xml, 1, arrayOf("rights"), "rights", null)
        propTag(props, xml, 1, arrayOf("generator"), "generator", arrayOf(arrayOf("uri", "uri"), arrayOf("version", "version")))

        // items
        for (row in 1..rows) {
            append(xml, 1, "<entry>")
            tag(xml, 2, Pair<String?, Object?>("title", query.getAt(getItemColumn(FeedQuery.TITLE), row, null)), arrayOf<Pair?>(Pair<String?, Object?>("type", query.getAt(getItemColumn(FeedQuery.TITLETYPE), row, null))), false, splitString)
            tag(xml, 2, Pair<String?, Object?>("link", null), arrayOf<Pair?>(Pair<String?, Object?>("href", query.getAt(getItemColumn(FeedQuery.LINKHREF), row, null)),
                    Pair<String?, Object?>("hreflang", query.getAt(getItemColumn(FeedQuery.LINKHREFLANG), row, null)),
                    Pair<String?, Object?>("length", query.getAt(getItemColumn(FeedQuery.LINKLENGTH), row, null)),
                    Pair<String?, Object?>("rel", query.getAt(getItemColumn(FeedQuery.LINKREL), row, null)),
                    Pair<String?, Object?>("title", query.getAt(getItemColumn(FeedQuery.LINKTITLE), row, null)),
                    Pair<String?, Object?>("type", query.getAt(getItemColumn(FeedQuery.LINKTYPE), row, null))),
                    false, splitString)
            tag(xml, 2, Pair<String?, Object?>("id", query.getAt(getItemColumn(FeedQuery.ID), row, null)), null, true, false)
            tag(xml, 2, Pair<String?, Object?>("updated", query.getAt(getItemColumn(FeedQuery.UPDATEDDATE), row, null)), null, true, false)
            tag(xml, 2, Pair<String?, Object?>("published", query.getAt(getItemColumn(FeedQuery.PUBLISHEDDATE), row, null)), null, true, false)
            tag(xml, 2, Pair<String?, Object?>("author", null), arrayOf<Pair?>(Pair<String?, Object?>("email", query.getAt(getItemColumn(FeedQuery.AUTHOREMAIL), row, null)),
                    Pair<String?, Object?>("name", query.getAt(getItemColumn(FeedQuery.AUTHORNAME), row, null)),
                    Pair<String?, Object?>("uri", query.getAt(getItemColumn(FeedQuery.AUTHORURI), row, null))),
                    false, splitString)
            tag(xml, 2, Pair<String?, Object?>("category", null), arrayOf<Pair?>(Pair<String?, Object?>("label", query.getAt(getItemColumn(FeedQuery.CATEGORYLABEL), row, null)),
                    Pair<String?, Object?>("scheme", query.getAt(getItemColumn(FeedQuery.CATEGORYSCHEME), row, null)),
                    Pair<String?, Object?>("term", query.getAt(getItemColumn(FeedQuery.CATEGORYTERM), row, null))),
                    false, splitString)
            tag(xml, 2, Pair<String?, Object?>("contributor", null), arrayOf<Pair?>(Pair<String?, Object?>("email", query.getAt(getItemColumn(FeedQuery.CONTRIBUTOREMAIL), row, null)),
                    Pair<String?, Object?>("name", query.getAt(getItemColumn(FeedQuery.CONTRIBUTORNAME), row, null)),
                    Pair<String?, Object?>("uri", query.getAt(getItemColumn(FeedQuery.CONTRIBUTORURI), row, null))),
                    false, splitString)
            tag(xml, 2, Pair<String?, Object?>("content", query.getAt(getItemColumn(FeedQuery.CONTENT), row, null)), arrayOf<Pair?>(Pair<String?, Object?>("src", query.getAt(getItemColumn(FeedQuery.CONTENTSRC), row, null)),
                    Pair<String?, Object?>("type", query.getAt(getItemColumn(FeedQuery.CONTENTTYPE), row, null))),
                    false, splitString)
            tag(xml, 2, Pair<String?, Object?>("rights", query.getAt(getItemColumn(FeedQuery.RIGHTS), row, null)), null, true, false)
            tag(xml, 2, Pair<String?, Object?>("summary", query.getAt(getItemColumn(FeedQuery.SUMMARY), row, null)), arrayOf<Pair?>(Pair<String?, Object?>("type", query.getAt(getItemColumn(FeedQuery.SUMMARYTYPE), row, null))), false, splitString)
            append(xml, 1, "</entry>")
        }
        append(xml, 0, "</feed>")
    }

    @Throws(PageException::class)
    private fun createRSS(xml: StringBuffer?, query: Query?, props: Struct?, splitString: Boolean) {
        val rows: Int = query.getRowCount()
        append(xml, 0,
                "<rss xmlns:taxo=\"http://purl.org/rss/1.0/modules/taxonomy/\" xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:itunes=\"http://www.itunes.com/dtds/podcast-1.0.dtd\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" version=\"2.0\">")
        append(xml, 1, "<channel>")

        // title
        propTag(props, xml, 2, arrayOf("title"), "title", null)
        propTag(props, xml, 2, arrayOf("link"), "link", null)
        propTag(props, xml, 2, arrayOf("description", "subtitle"), "description", null)
        propTag(props, xml, 2, arrayOf("language"), "language", null)
        propTag(props, xml, 2, arrayOf("copyright"), "copyright", null)
        propTag(props, xml, 2, arrayOf("managingEditor"), "managingEditor", null)
        propTag(props, xml, 2, arrayOf("webMaster"), "webMaster", null)
        propTag(props, xml, 2, arrayOf("pubDate"), "pubDate", null)
        propTag(props, xml, 2, arrayOf("lastBuildDate"), "lastBuildDate", null)
        propTag(props, xml, 2, arrayOf("category"), "category", arrayOf(arrayOf("domain", "domain")))
        propTag(props, xml, 2, arrayOf("generator"), "generator", null)
        propTag(props, xml, 2, arrayOf("docs"), "docs", null)
        propTag(props, xml, 2, arrayOf("cloud"), "cloud", arrayOf(arrayOf("domain", "domain"), arrayOf("port", "port"), arrayOf("path", "path"), arrayOf("registerProcedure", "registerProcedure"), arrayOf("protocol", "protocol")))
        propTag(props, xml, 2, arrayOf("ttl"), "ttl", null)
        propTag(props, xml, 2, arrayOf("image"), "image", arrayOf(arrayOf("url", "url"), arrayOf("title", "title"), arrayOf("link", "link"), arrayOf("width", "width"), arrayOf("height", "height"), arrayOf("description", "description")), true)
        propTag(props, xml, 2, arrayOf("textInput"), "textInput", arrayOf(arrayOf("title", "title"), arrayOf("description", "description"), arrayOf("name", "name"), arrayOf("link", "link")), true)
        propTag(props, xml, 2, arrayOf("skipHours"), "skipHours", null)
        propTag(props, xml, 2, arrayOf("skipDays"), "skipDays", null)

        // items
        for (row in 1..rows) {
            append(xml, 2, "<item>")
            tag(xml, 3, Pair<String?, Object?>("title", query.getAt(getItemColumn(FeedQuery.TITLE), row, null)), null, true, false)
            tag(xml, 3, Pair<String?, Object?>("description", query.getAt(getItemColumn(FeedQuery.CONTENT), row, null)), null, true, false)
            tag(xml, 3, Pair<String?, Object?>("link", query.getAt(getItemColumn(FeedQuery.RSSLINK), row, null)), null, false, false)
            tag(xml, 3, Pair<String?, Object?>("author", query.getAt(getItemColumn(FeedQuery.AUTHOREMAIL), row, null)), null, false, false)
            tag(xml, 3, Pair<String?, Object?>("category", query.getAt(getItemColumn(FeedQuery.CATEGORYLABEL), row, null)), arrayOf<Pair?>(Pair<String?, Object?>("domain", query.getAt(getItemColumn(FeedQuery.CATEGORYSCHEME), row, null))), false, splitString)
            tag(xml, 3, Pair<String?, Object?>("comments", query.getAt(getItemColumn(FeedQuery.COMMENTS), row, null)), null, false, false)
            tag(xml, 3, Pair<String?, Object?>("enclosure", null), arrayOf<Pair?>(Pair<String?, Object?>("url", query.getAt(getItemColumn(FeedQuery.LINKHREF), row, null)),
                    Pair<String?, Object?>("length", query.getAt(getItemColumn(FeedQuery.LINKLENGTH), row, null)),
                    Pair<String?, Object?>("type", query.getAt(getItemColumn(FeedQuery.LINKTYPE), row, null))),
                    false, splitString)
            tag(xml, 3, Pair<String?, Object?>("guid", query.getAt(getItemColumn(FeedQuery.ID), row, null)), arrayOf<Pair?>(Pair<String?, Object?>("isPermaLink", query.getAt(getItemColumn(FeedQuery.IDPERMALINK), row, null))), false, splitString)
            tag(xml, 3, Pair<String?, Object?>("pubDate", query.getAt(getItemColumn(FeedQuery.PUBLISHEDDATE), row, null)), null, false, splitString)
            tag(xml, 3, Pair<String?, Object?>("source", query.getAt(getItemColumn(FeedQuery.SOURCE), row, null)), arrayOf<Pair?>(Pair<String?, Object?>("url", query.getAt(getItemColumn(FeedQuery.SOURCEURL), row, null))), false, false)
            append(xml, 2, "</item>")
        }
        append(xml, 1, "</channel>")
        append(xml, 0, "</rss>")
    }

    @Throws(PageException::class)
    private fun propTag(props: Struct?, xml: StringBuffer?, count: Int, srcNames: Array<String?>?, trgName: String?, attrNames: Array<Array<String?>?>?) {
        propTag(props, xml, count, srcNames, trgName, attrNames, false)
    }

    @Throws(PageException::class)
    private fun propTag(props: Struct?, xml: StringBuffer?, count: Int, srcNames: Array<String?>?, trgName: String?, attrNames: Array<Array<String?>?>?, childrenAsTag: Boolean) {
        var value: Object
        for (i in srcNames.indices) {
            value = props.get(srcNames!![i], null)
            if (value is Array) {
                val arr: Array = value
                val size: Int = arr.size()
                for (y in 1..size) {
                    propTag(xml, count, arr.get(y, null), trgName, attrNames, childrenAsTag)
                }
                break
            }
            if (value != null) {
                propTag(xml, count, value, trgName, attrNames, childrenAsTag)
                break
            }
        }
    }

    @Throws(PageException::class)
    private fun propTag(xml: StringBuffer?, count: Int, value: Object?, trgName: String?, attrNames: Array<Array<String?>?>?, childrenAsTag: Boolean): Boolean {
        if (!StringUtil.isEmpty(value)) {
            val attrs: Array<Pair?>?
            if (value is Struct && attrNames != null) {
                val sct: Struct? = value as Struct?
                var attrValue: Object
                val al: ArrayList<Pair?> = ArrayList<Pair?>()
                for (i in attrNames.indices) {
                    attrValue = sct.get(attrNames[i]!![0], null)
                    if (attrValue != null) {
                        al.add(Pair<String?, Object?>(attrNames[i]!![1], attrValue))
                    }
                }
                attrs = al.toArray(arrayOfNulls<Pair?>(al.size()))
            } else attrs = null
            tag(xml, count, Pair<String?, Object?>(trgName, FeedQuery.getValue(value)), attrs, false, false, childrenAsTag)
            return true
        }
        return false
    }

    @Throws(PageException::class)
    private fun tag(xml: StringBuffer?, count: Int, tag: Pair<String?, Object?>?, attrs: Array<Pair<String?, Object?>?>?, required: Boolean, splitString: Boolean) {
        tag(xml, count, tag, attrs, required, splitString, false)
    }

    @Throws(PageException::class)
    private fun tag(xml: StringBuffer?, count: Int, tag: Pair<String?, Object?>?, attrs: Array<Pair<String?, Object?>?>?, required: Boolean, splitString: Boolean, childrenAsTag: Boolean) {
        if (!required && StringUtil.isEmpty(tag.getValue())) {
            if (attrs == null || attrs.size == 0) return
            var c = 0
            for (i in attrs.indices) {
                if (!StringUtil.isEmpty(attrs[i].getValue())) c++
            }
            if (c == 0) return
        }
        if (tag.getValue() is Array) {
            val arr: Array = tag.getValue()
            val len: Int = arr.size()
            for (i in 1..len) {
                _tag(xml, tag.getName(), arr.get(i, null), attrs, count, i, false, childrenAsTag)
            }
            return
        }
        if (splitString && tag.getValue() is String) {
            val strValue = tag.getValue() as String
            val arr: Array = ListUtil.listToArray(strValue, ',')
            if (arr.size() > 1) {
                val len: Int = arr.size()
                for (i in 1..len) {
                    _tag(xml, tag.getName(), arr.get(i, null), attrs, count, i, true, childrenAsTag)
                }
                return
            }
        }
        _tag(xml, tag.getName(), tag.getValue(), attrs, count, 0, false, childrenAsTag)
    }

    @Throws(PageException::class)
    private fun _tag(xml: StringBuffer?, tagName: String?, tagValue: Object?, attrs: Array<Pair<String?, Object?>?>?, count: Int, index: Int, splitString: Boolean, childrenAsTag: Boolean) {
        for (i in 0 until count) xml.append("\t")
        xml.append('<')
        xml.append(tagName)
        var attrValue: Object?
        if (attrs != null && !childrenAsTag) {
            for (i in attrs.indices) {
                attrValue = attrs[i].getValue()
                if (index > 0) {
                    if (attrValue is Array) attrValue = (attrValue as Array?)!!.get(index, null) else if (splitString && attrValue is String) {
                        val arr: Array = ListUtil.listToArray(attrValue as String?, ',')
                        attrValue = arr.get(index, null)
                    }
                }
                if (StringUtil.isEmpty(attrValue)) continue
                xml.append(' ')
                xml.append(attrs[i].getName())
                xml.append("=\"")
                xml.append(XMLUtil.escapeXMLString(toString(attrValue)))
                xml.append("\"")
            }
        }
        xml.append('>')
        xml.append(toString(tagValue))
        if (attrs != null && attrs.size > 0 && childrenAsTag) {
            xml.append('\n')
            for (i in attrs.indices) {
                attrValue = attrs[i].getValue()
                if (index > 0) {
                    if (attrValue is Array) attrValue = (attrValue as Array?)!!.get(index, null) else if (splitString && attrValue is String) {
                        val arr: Array = ListUtil.listToArray(attrValue as String?, ',')
                        attrValue = arr.get(index, null)
                    }
                }
                if (StringUtil.isEmpty(attrValue)) continue
                for (y in 0 until count + 1) xml.append("\t")
                xml.append('<')
                xml.append(attrs[i].getName())
                xml.append('>')
                // xml.append(XMLUtil.escapeXMLString(toString(attrValue)));
                xml.append(toString(attrValue))
                xml.append("</")
                xml.append(attrs[i].getName())
                xml.append(">\n")
            }
            for (y in 0 until count) xml.append("\t")
        }
        xml.append("</")
        xml.append(tagName)
        xml.append(">\n")
    }

    @Throws(PageException::class)
    private fun toString(value: Object?): String? {
        return if (Decision.isDateAdvanced(value, false)) GetHttpTimeString.invoke(Caster.toDatetime(value, pageContext.getTimeZone())) else XMLUtil.escapeXMLString(Caster.toString(value))
    }

    /**
     * @param columm which might be translated to a different column, when a matching columnMap entry is
     * found
     */
    private fun getItemColumn(column: Key?): String? {
        if (columnMap != null) {
            val col: Object = columnMap.get(column, null)
            if (col != null) return col.toString()
        }
        return column.toString()
    }

    @Throws(IOException::class, SAXException::class, PageException::class)
    private fun doActionRead() {
        required("Feed", "read", "source", source)
        if (outputFile != null && outputFile.exists() && !overwrite) throw ApplicationException("outputFile file [$outputFile] already exists")
        val charset: String? = null

        // plain output
        // xmlVar
        if (outputFile != null) {
            IOUtil.copy(source, outputFile)
        }
        // outputFile
        var strFeed: String? = null
        if (!StringUtil.isEmpty(xmlVar)) {
            strFeed = IOUtil.toString(if (outputFile != null) outputFile else source, charset)
            pageContext.setVariable(xmlVar, strFeed)
        }
        // Input Source
        var `is`: InputSource? = null
        var r: Reader? = null
        if (strFeed != null) `is` = InputSource(StringReader(strFeed)) else if (outputFile != null) `is` = InputSource(IOUtil.getReader(outputFile, charset).also { r = it }) else `is` = InputSource(IOUtil.getReader(source, charset).also { r = it })
        `is`.setSystemId(source.getPath())
        try {
            val feed = FeedHandler(source)
            val data: Struct = feed.getData()
            // print.e(data.keys());
            // print.e(data);
            // properties
            if (properties != null) {
                val strProp: String = Caster.toString(properties, null)
                        ?: throw ApplicationException("attribute [properties] should be of type string")
                pageContext.setVariable(strProp, FeedProperties.toProperties(data))
            }

            // query or enclosure
            var qry: tachyon.runtime.type.Query? = null
            if (query != null || enclosureDir != null) {
                qry = FeedQuery.toQuery(data, feed.hasDC())
            }

            // query
            if (query != null) {
                val strQuery: String = Caster.toString(query, null)
                        ?: throw ApplicationException("attribute [query] should be of type string")
                pageContext.setVariable(strQuery, qry)
            }
            // enclosure
            if (enclosureDir != null) {
                val rows: Int = qry.getRowCount()
                var strUrl: String? = null
                var src: Resource
                var dest: Resource
                for (row in 1..rows) {
                    strUrl = Caster.toString(qry.getAt(FeedQuery.LINKHREF, row, null), null)
                    if (!StringUtil.isEmpty(strUrl)) {
                        src = ResourceUtil.toResourceNotExisting(pageContext, strUrl)
                        dest = enclosureDir.getRealResource(src.getName())
                        if (!ignoreEnclosureError && !overwriteEnclosure && dest.exists()) throw ApplicationException("enclosure file [$dest] already exists")
                        try {
                            IOUtil.copy(src, dest)
                        } catch (ioe: IOException) {
                            if (!ignoreEnclosureError) throw ioe
                        }
                    }
                }
            }

            // name
            if (name != null) {
                val strName: String = Caster.toString(name, null)
                        ?: throw ApplicationException("attribute [name] should be of type string")
                pageContext.setVariable(strName, data)
            }
        } finally {
            IOUtil.close(r)
        }
    }

    companion object {
        private const val ACTION_READ = 0
        private const val ACTION_CREATE = 1
        private const val TYPE_AUTO = 0
        private const val TYPE_RSS = 1
        private const val TYPE_ATOM = 2
        private fun append(xml: StringBuffer?, count: Int, value: String?) {
            for (i in 0 until count) xml.append("\t")
            xml.append(value)
            xml.append("\n")
        }
    }
}