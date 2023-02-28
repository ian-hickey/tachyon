/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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
package lucee.commons.net

import java.io.ByteArrayOutputStream

/**
 *
 */
object HTTPUtil {
    const val ENCODED_AUTO: Short = 1
    const val ENCODED_YES: Short = 2
    const val ENCODED_NO: Short = 3

    /**
     * Field `ACTION_POST`
     */
    const val ACTION_POST: Short = 0

    /**
     * Field `ACTION_GET`
     */
    const val ACTION_GET: Short = 1

    /**
     * Field `STATUS_OK`
     */
    const val STATUS_OK = 200

    // private static final String NO_MIMETYPE="Unable to determine MIME type of file.";
    const val MAX_REDIRECT = 15

    /**
     * cast a string to a url
     *
     * @param strUrl string represent a url
     * @return url from string
     * @throws MalformedURLException
     */
    @Throws(MalformedURLException::class)
    fun toURL(strUrl: String, encodeOption: Short): URL {
        return toURL(strUrl, -1, encodeOption)
    }

    fun toURL(strUrl: String, encodeOption: Short, defaultValue: URL): URL {
        return try {
            toURL(strUrl, -1, encodeOption)
        } catch (e: MalformedURLException) {
            defaultValue
        }
    }

    fun validateURL(strUrl: String, defaultValue: String): String {
        return try {
            toURL(strUrl, -1, Http.ENCODED_AUTO).toExternalForm()
        } catch (e: MalformedURLException) {
            defaultValue
        }
    }

    /**
     * cast a string to a url
     *
     * @param strUrl string represent a url
     * @return url from string
     * @throws MalformedURLException
     */
    @Throws(MalformedURLException::class)
    fun toURL(strUrl: String, port: Int, encodeOption: Short): URL {
        var url: URL
        try {
            url = URL(strUrl)
        } catch (mue: MalformedURLException) {
            url = URL("http://$strUrl")
        }
        return if (encodeOption == Http.ENCODED_NO) url else encodeURL(url, port, encodeOption == Http.ENCODED_AUTO)
    }

    @Throws(MalformedURLException::class)
    fun encodeURL(url: URL, encodeWhenNecessary: Boolean): URL {
        return encodeURL(url, -1, encodeWhenNecessary)
    }

    @Throws(MalformedURLException::class)
    fun encodeURL(url: URL, port: Int, encodeOnlyWhenNecessary: Boolean): URL {

        // file
        var port = port
        var path: String = url.getPath()
        // String file=url.getFile();
        var query: String = url.getQuery()
        val ref: String = url.getRef()
        var user: String? = url.getUserInfo()
        if (port <= 0) port = url.getPort()

        // decode path
        if (!StringUtil.isEmpty(path)) {
            val sqIndex: Int = path.indexOf(';')
            var q: String? = null
            if (sqIndex != -1) {
                q = path.substring(sqIndex + 1)
                path = path.substring(0, sqIndex)
            }
            val res = StringBuilder()
            val list: StringList = ListUtil.toListTrim(path, '/')
            var str: String
            while (list.hasNext()) {
                str = list.next()
                // str=URLDecoder.decode(str);
                if (StringUtil.isEmpty(str)) continue
                res.append("/")
                res.append(escapeQSValue(str, encodeOnlyWhenNecessary))
            }
            if (StringUtil.endsWith(path, '/')) res.append('/')
            path = res.toString()
            if (sqIndex != -1) {
                path += decodeQuery(q, ';')
            }
        }

        // decode query
        query = decodeQuery(query, '?')
        var file = path + query

        // decode ref/anchor
        if (ref != null) {
            file += "#" + escapeQSValue(ref, encodeOnlyWhenNecessary)
        }

        // user/pasword
        if (!StringUtil.isEmpty(user)) {
            val index: Int = user.indexOf(':')
            user = if (index != -1) {
                escapeQSValue(user.substring(0, index), encodeOnlyWhenNecessary).toString() + ":" + escapeQSValue(user.substring(index + 1), encodeOnlyWhenNecessary)
            } else escapeQSValue(user, encodeOnlyWhenNecessary)
            var strUrl = getProtocol(url) + "://" + user + "@" + url.getHost()
            if (port > 0) strUrl += ":$port"
            strUrl += file
            return URL(strUrl)
        }

        // port
        return if (port <= 0) URL(url.getProtocol(), url.getHost(), file) else URL(url.getProtocol(), url.getHost(), port, file)
    }

    private fun decodeQuery(query: String?, startDelimiter: Char): String {
        var query = query
        if (!StringUtil.isEmpty(query)) {
            val res = StringBuilder()
            val list: StringList = ListUtil.toList(query, '&')
            var str: String
            var index: Int
            var del = startDelimiter
            while (list.hasNext()) {
                res.append(del)
                del = '&'
                str = list.next()
                index = str.indexOf('=')
                if (index == -1) res.append(escapeQSValue(str, true)) else {
                    res.append(escapeQSValue(str.substring(0, index), true))
                    res.append('=')
                    res.append(escapeQSValue(str.substring(index + 1), true))
                }
            }
            query = res.toString()
        } else query = ""
        return query
    }

    @Throws(URISyntaxException::class)
    fun toURI(strUrl: String?): URI {
        return toURI(strUrl, -1)
    }

    @Throws(URISyntaxException::class)
    fun toURI(strUrl: String?, port: Int): URI {

        // print.o((strUrl));
        var port = port
        val uri = URI(strUrl)
        val host: String = uri.getHost()
        var fragment: String? = uri.getRawFragment()
        var path: String? = uri.getRawPath()
        var query: String = uri.getRawQuery()
        val scheme: String = uri.getScheme()
        var userInfo: String? = uri.getRawUserInfo()
        if (port <= 0) port = uri.getPort()

        // decode path
        if (!StringUtil.isEmpty(path)) {
            val sqIndex: Int = path.indexOf(';')
            var q: String? = null
            if (sqIndex != -1) {
                q = path.substring(sqIndex + 1)
                path = path.substring(0, sqIndex)
            }
            val res = StringBuilder()
            val list: StringList = ListUtil.toListTrim(path, '/')
            var str: String
            while (list.hasNext()) {
                str = list.next()
                // str=URLDecoder.decode(str);
                if (StringUtil.isEmpty(str)) continue
                res.append("/")
                res.append(escapeQSValue(str, true))
            }
            if (StringUtil.endsWith(path, '/')) res.append('/')
            path = res.toString()
            if (sqIndex != -1) {
                path += decodeQuery(q, ';')
            }
        }

        // decode query
        query = decodeQuery(query, '?')

        // decode ref/anchor
        if (!StringUtil.isEmpty(fragment)) {
            fragment = escapeQSValue(fragment, true)
        }

        // user/pasword
        if (!StringUtil.isEmpty(userInfo)) {
            val index: Int = userInfo.indexOf(':')
            userInfo = if (index != -1) {
                escapeQSValue(userInfo.substring(0, index), true).toString() + ":" + escapeQSValue(userInfo.substring(index + 1), true)
            } else escapeQSValue(userInfo, true)
        }

        /*
		 * print.o("- fragment:"+fragment); print.o("- host:"+host); print.o("- path:"+path);
		 * print.o("- query:"+query); print.o("- scheme:"+scheme); print.o("- userInfo:"+userInfo);
		 * print.o("- port:"+port); print.o("- absolute:"+uri.isAbsolute());
		 * print.o("- opaque:"+uri.isOpaque());
		 */
        val rtn = StringBuilder()
        if (scheme != null) {
            rtn.append(scheme)
            rtn.append("://")
        }
        if (userInfo != null) {
            rtn.append(userInfo)
            rtn.append("@")
        }
        if (host != null) {
            rtn.append(host)
        }
        if (port > 0) {
            rtn.append(":")
            rtn.append(port)
        }
        if (path != null) {
            rtn.append(path)
        }
        if (query != null) {
            // rtn.append("?");
            rtn.append(query)
        }
        if (fragment != null) {
            rtn.append("#")
            rtn.append(fragment)
        }
        return URI(rtn.toString())
    }

    /*
	 * private static String getProtocol(URI uri) { String p=uri.getRawSchemeSpecificPart(); if(p==null)
	 * return null; if(p.indexOf('/')==-1) return p; if(p.indexOf("https")!=-1) return "https";
	 * if(p.indexOf("http")!=-1) return "http"; return p; }
	 */
    private fun getProtocol(url: URL): String {
        val p: String = url.getProtocol().toLowerCase()
        if (p.indexOf('/') === -1) return p
        if (p.indexOf("https") !== -1) return "https"
        return if (p.indexOf("http") !== -1) "http" else p
    }

    fun escapeQSValue(str: String?, encodeOnlyWhenNecessary: Boolean): String? {
        if (encodeOnlyWhenNecessary && !ReqRspUtil.needEncoding(str, false)) return str
        val pc: PageContextImpl = ThreadLocalPageContext.get() as PageContextImpl
        if (pc != null) {
            try {
                return URLEncoder.encode(str, pc.getWebCharset())
            } catch (e: UnsupportedEncodingException) {
            }
        }
        return URLEncoder.encode(str)
    }

    fun removeUnecessaryPort(url: URL): URL {
        var port: Int = url.getPort()
        if (port == 80 && url.getProtocol().equalsIgnoreCase("http")) port = -1 else if (port == 443 && url.getProtocol().equalsIgnoreCase("https")) port = -1
        return try {
            URL(url.getProtocol(), url.getHost(), port, url.getFile())
        } catch (e: MalformedURLException) {
            url // this should never happen
        }
    }

    @Throws(MalformedURLException::class)
    fun removeUnecessaryPort(url: String?): String {
        return removeUnecessaryPort(URL(url)).toExternalForm()
    }

    /*
	 * public static URL toURL(HttpMethod httpMethod) { HostConfiguration config =
	 * httpMethod.getHostConfiguration();
	 * 
	 * try { String qs = httpMethod.getQueryString(); if(StringUtil.isEmpty(qs)) return new
	 * URL(config.getProtocol().getScheme(),config.getHost(),config.getPort(),httpMethod.getPath());
	 * return new
	 * URL(config.getProtocol().getScheme(),config.getHost(),config.getPort(),httpMethod.getPath()+"?"+
	 * qs); } catch (MalformedURLException e) { return null; } }
	 */
    fun optimizeRealPath(pc: PageContext, realPath: String): String {
        var index: Int
        var requestURI = realPath
        var queryString: String? = null
        if (realPath.indexOf('?').also { index = it } != -1) {
            requestURI = realPath.substring(0, index)
            queryString = realPath.substring(index + 1)
        }
        val ps: PageSource = PageSourceImpl.best((pc as PageContextImpl).getRelativePageSources(requestURI))
        requestURI = ps.getRealpathWithVirtual()
        return if (queryString != null) "$requestURI?$queryString" else requestURI
    }

    @Throws(ServletException::class, IOException::class)
    fun forward(pc: PageContext, realPath: String) {
        var realPath = realPath
        val context: ServletContext = pc.getServletContext()
        realPath = optimizeRealPath(pc, realPath)
        try {
            pc.getHttpServletRequest().setAttribute("lucee.forward.request_uri", realPath)
            val disp: RequestDispatcher = context.getRequestDispatcher(realPath)
                    ?: throw PageServletException(ApplicationException("Page [$realPath] not found"))

            // populateRequestAttributes();
            disp.forward(removeWrap(pc.getHttpServletRequest()), pc.getHttpServletResponse())
        } finally {
            ThreadLocalPageContext.register(pc)
        }
    }

    fun removeWrap(req: ServletRequest): ServletRequest {
        while (req is HTTPServletRequestWrap) return (req as HTTPServletRequestWrap).getOriginalRequest()
        return req
    }

    @Throws(ServletException::class, IOException::class)
    fun include(pc: PageContext, realPath: String) {
        include(pc, pc.getHttpServletRequest(), pc.getHttpServletResponse(), realPath)
    }

    @Throws(ServletException::class, IOException::class)
    fun include(pc: PageContext, req: ServletRequest?, rsp: ServletResponse?, realPath: String) {
        var realPath = realPath
        realPath = optimizeRealPath(pc, realPath)
        val inline: Boolean = HttpServletResponseWrap.get()
        // print.out(rsp+":"+pc.getResponse());
        val disp: RequestDispatcher = getRequestDispatcher(pc, realPath)
        if (inline) {
            // RequestDispatcher disp = getRequestDispatcher(pc,realPath);
            disp.include(req, rsp)
            return
        }
        try {
            val baos = ByteArrayOutputStream()
            val hsrw = HttpServletResponseWrap(pc.getHttpServletResponse(), baos)
            HttpServletResponseWrap.set(true)

            // RequestDispatcher disp = getRequestDispatcher(pc,realPath);
            disp.include(req, hsrw)
            if (!hsrw.isCommitted()) hsrw.flushBuffer()
            pc.write(IOUtil.toString(baos.toByteArray(), ReqRspUtil.getCharacterEncoding(pc, hsrw)))
        } finally {
            HttpServletResponseWrap.release()
            ThreadLocalPageContext.register(pc)
        }
    }

    @Throws(PageServletException::class)
    private fun getRequestDispatcher(pc: PageContext, realPath: String): RequestDispatcher {
        return pc.getServletContext().getRequestDispatcher(realPath)
                ?: throw PageServletException(ApplicationException("Page [$realPath] not found"))
    }

    // MUST create a copy from toURL and rename toURI and rewrite for URI, perhaps it is possible to
    // merge them somehow
    fun encode(realpath: String): String {
        val qIndex: Int = realpath.indexOf('?')
        if (qIndex == -1) return realpath
        val file: String = realpath.substring(0, qIndex)
        var query: String = realpath.substring(qIndex + 1)
        val sIndex: Int = query.indexOf('#')
        var anker: String? = null
        if (sIndex != -1) {
            // print.o(sIndex);
            anker = query.substring(sIndex + 1)
            query = query.substring(0, sIndex)
        }
        val res = StringBuilder(file)

        // query
        if (!StringUtil.isEmpty(query)) {
            val list: StringList = ListUtil.toList(query, '&')
            var str: String
            var index: Int
            var del = '?'
            while (list.hasNext()) {
                res.append(del)
                del = '&'
                str = list.next()
                index = str.indexOf('=')
                if (index == -1) res.append(escapeQSValue(str, true)) else {
                    res.append(escapeQSValue(str.substring(0, index), true))
                    res.append('=')
                    res.append(escapeQSValue(str.substring(index + 1), true))
                }
            }
        }

        // anker
        if (anker != null) {
            res.append('#')
            res.append(escapeQSValue(anker, true))
        }
        return res.toString()
    }

    fun getPort(url: URL): Int {
        if (url.getPort() !== -1) return url.getPort()
        return if ("https".equalsIgnoreCase(url.getProtocol())) 443 else 80
    }

    /**
     * return the length of a file defined by a url.
     *
     * @param dataUrl
     * @return
     * @throws IOException
     */
    @Throws(IOException::class)
    fun length(url: URL?): Long {
        val http: HTTPResponse = HTTPEngine.head(url, null, null, -1, true, null, Constants.NAME, null, null)
        val len: Long = http.getContentLength()
        HTTPEngine.closeEL(http)
        return len
    }

    /*
	 * public static ContentType getContentType(HttpMethod http) { Header[] headers =
	 * http.getResponseHeaders(); for(int i=0;i<headers.length;i++){
	 * if("Content-Type".equalsIgnoreCase(headers[i].getName())){ String[] mimeCharset =
	 * splitMimeTypeAndCharset(headers[i].getValue()); String[] typeSub =
	 * splitTypeAndSubType(mimeCharset[0]); return new
	 * ContentTypeImpl(typeSub[0],typeSub[1],mimeCharset[1]); } } return null; }
	 */
    fun parseParameterList(_str: String?, decode: Boolean, charset: String?): Map<String, String> {
        // return lucee.commons.net.HTTPUtil.toURI(strUrl,port);
        val data: Map<String, String> = HashMap<String, String>()
        val list: StringList = ListUtil.toList(_str, '&')
        var str: String
        var index: Int
        while (list.hasNext()) {
            str = list.next()
            index = str.indexOf('=')
            if (index == -1) {
                data.put(decode(str, decode), "")
            } else {
                data.put(decode(str.substring(0, index), decode), decode(str.substring(index + 1), decode))
            }
        }
        return data
    }

    private fun decode(str: String, encode: Boolean): String {
        // TODO Auto-generated method stub
        return str
    }

    fun toContentType(str: String, defaultValue: ContentType?): ContentType? {
        if (StringUtil.isEmpty(str, true)) return defaultValue
        val types: Array<String> = str.split(";")
        var ct: ContentType? = null
        if (types.size > 0) {
            ct = ContentType(types[0])
            if (types.size > 1) {
                val tmp: String = types[types.size - 1].trim()
                val index: Int = tmp.indexOf("charset=")
                if (index != -1) {
                    ct.setCharset(StringUtil.removeQuotes(tmp.substring(index + 8), true))
                }
            }
        }
        return ct
    }

    fun splitMimeTypeAndCharset(mimetype: String, defaultValue: Array<String?>): Array<String?> {
        if (StringUtil.isEmpty(mimetype, true)) return defaultValue
        val types: Array<String> = mimetype.split(";")
        val rtn = arrayOfNulls<String>(2)
        if (types.size > 0) {
            rtn[0] = types[0].trim()
            if (types.size > 1) {
                val tmp: String = types[types.size - 1].trim()
                val index: Int = tmp.indexOf("charset=")
                if (index != -1) {
                    rtn[1] = StringUtil.removeQuotes(tmp.substring(index + 8), true)
                }
            }
        }
        return rtn
    }

    fun splitTypeAndSubType(mimetype: String?): Array<String?> {
        val types: Array<String> = ListUtil.listToStringArray(mimetype, '/')
        val rtn = arrayOfNulls<String>(2)
        if (types.size > 0) {
            rtn[0] = types[0].trim()
            if (types.size > 1) {
                rtn[1] = types[1].trim()
            }
        }
        return rtn
    }

    fun isTextMimeType(mimetype: String?): Boolean? {
        var mimetype = mimetype
        mimetype = mimetype?.trim()?.toLowerCase() ?: ""
        if (StringUtil.startsWithIgnoreCase(mimetype, "audio/") || StringUtil.startsWithIgnoreCase(mimetype, "image/") || StringUtil.startsWithIgnoreCase(mimetype, "video/")) return false
        return if (StringUtil.startsWithIgnoreCase(mimetype, "text") || StringUtil.startsWithIgnoreCase(mimetype, "application/xml")
                || StringUtil.startsWithIgnoreCase(mimetype, "application/atom+xml") || StringUtil.startsWithIgnoreCase(mimetype, "application/xhtml")
                || StringUtil.startsWithIgnoreCase(mimetype, "application/json") || StringUtil.startsWithIgnoreCase(mimetype, "application/ld-json")
                || StringUtil.startsWithIgnoreCase(mimetype, "application/cfml") || StringUtil.startsWithIgnoreCase(mimetype, "application/x-www-form-urlencoded")
                || StringUtil.startsWithIgnoreCase(mimetype, "application/EDIFACT") || StringUtil.startsWithIgnoreCase(mimetype, "application/javascript")
                || StringUtil.startsWithIgnoreCase(mimetype, "message") || StringUtil.indexOfIgnoreCase(mimetype, "xml") !== -1 || StringUtil.indexOfIgnoreCase(mimetype, "json") !== -1 || StringUtil.indexOfIgnoreCase(mimetype, "rss") !== -1 || StringUtil.indexOfIgnoreCase(mimetype, "atom") !== -1 || StringUtil.indexOfIgnoreCase(mimetype, "text") !== -1) true else null
    }

    fun isTextMimeType(mimetype: MimeType?): Boolean {
        if (mimetype == null) return false
        if (MimeType.APPLICATION_JSON.same(mimetype)) return true
        if (MimeType.APPLICATION_PLAIN.same(mimetype)) return true
        if (MimeType.APPLICATION_CFML.same(mimetype)) return true
        if (MimeType.APPLICATION_WDDX.same(mimetype)) return true
        return if (MimeType.APPLICATION_XML.same(mimetype)) true else isTextMimeType(mimetype.toString())
    }

    fun isSecure(url: URL): Boolean {
        return StringUtil.indexOfIgnoreCase(url.getProtocol(), "https") !== -1
    }
}