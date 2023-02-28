/**
 * Copyright (c) 2023, TachyonCFML.org
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

import java.io.ByteArrayInputStream

// MUST change behavor of mltiple headers now is an array, it das so?
/**
 * Lets you execute HTTP POST and GET operations on files. Using cfhttp, you can execute standard
 * GET operations and create a query object from a text file. POST operations lets you upload MIME
 * file types to a server, or post cookie, formfield, URL, file, or CGI variables directly to a
 * specified server.
 *
 *
 *
 *
 */
class Http : BodyTagImpl() {
    private val params: ArrayList<HttpParamBean?>? = ArrayList<HttpParamBean?>()

    /** When required by a server, a valid password.  */
    private var password: String? = null

    /** Required for creating a query. Options are a tab or comma. Default is a comma.  */
    private var delimiter = ','

    /**
     * Yes or No. Default is No. For GET and POST operations, if Yes, page reference returned into the
     * fileContent internal variable has its internal URLs fully resolved, including port number, so
     * that links remain intact.
     */
    private var resolveurl = false

    /** A value, in seconds. When a URL timeout is specified in the browser  */
    private var timeout: TimeSpan? = null

    /** Host name or IP address of a proxy server.  */
    private var proxyserver: String? = null

    /**
     * The filename to be used for the file that is accessed. For GET operations, defaults to the name
     * pecified in url. Enter path information in the path attribute.
     */
    private var strFile: String? = null

    /**
     * The path to the directory in which a file is to be stored. If a path is not specified in a POST
     * or GET operation, a variable is created (cfhttp.fileContent) that you can use to display the
     * results of the POST operation in a cfoutput.
     */
    private var strPath: String? = null

    /**
     * Boolean indicating whether to throw an exception that can be caught by using the cftry and
     * cfcatch tags. The default is NO.
     */
    private var throwonerror = false

    /** set the charset for the call.  */
    private var charset: String? = null

    /**
     * The port number on the proxy server from which the object is requested. Default is 80. When used
     * with resolveURL, the URLs of retrieved documents that specify a port number are automatically
     * resolved to preserve links in the retrieved document.
     */
    private var proxyport = 80

    /** Specifies the column names for a query when creating a query as a result of a cfhttp GET.  */
    private var columns: Array<String?>?

    /**
     * The port number on the server from which the object is requested. Default is 80. When used with
     * resolveURL, the URLs of retrieved documents that specify a port number are automatically resolved
     * to preserve links in the retrieved document. If a port number is specified in the url attribute,
     * the port value overrides the value of the port attribute.
     */
    private var port = -1

    /** User agent request header.  */
    private var useragent: String? = Constants.NAME.toString() + " (CFML Engine)"

    /**
     * Required for creating a query. Indicates the start and finish of a column. Should be
     * appropriately escaped when embedded in a column. For example, if the qualifier is a double
     * quotation mark, it should be escaped as """". If there is no text qualifier in the file, specify
     * it as " ". Default is the double quotation mark (").
     */
    private var textqualifier = '"'

    /** When required by a server, a valid username.  */
    private var username: String? = null

    /**
     * Full URL of the host name or IP address of the server on which the file resides. The URL must be
     * an absolute URL, including the protocol (http or https) and hostname. It may optionally contain a
     * port number. Port numbers specified in the url attribute override the port attribute.
     */
    private var url: String? = null

    /** Boolean indicating whether to redirect execution or stop execution.  */
    private var redirect = true

    /** The name to assign to a query if the a query is constructed from a file.  */
    private var name: String? = null

    /**
     * GET or POST. Use GET to download a text or binary file or to create a query from the contents of
     * a text file. Use POST to send information to a server page or a CGI program for processing. POST
     * requires the use of a cfhttpparam tag.
     */
    private var method = METHOD_GET

    // private boolean hasBody=false;
    private var firstrowasheaders = true
    private var proxyuser: String? = null
    private var proxypassword: String? = ""
    private var multiPart = false
    private var multiPartType = MULTIPART_FORM_DATA
    private var getAsBinary = GET_AS_BINARY_NO
    private var result: String? = "cfhttp"
    private var addtoken = false
    private var encode = true
    private var authType = AUTH_TYPE_BASIC
    private var workStation: String? = null
    private var domain: String? = null
    private var preauth = true
    private var encoded = ENCODED_AUTO
    private var compression = true
    private var cachedWithin: Object? = null
    private var usePool = true

    /** The full path to a PKCS12 format file that contains the client certificate for the request.  */
    private var clientCert: String? = null

    /** Password used to decrypt the client certificate.  */
    private var clientCertPassword: String? = null
    @Override
    fun release() {
        super.release()
        params.clear()
        password = null
        delimiter = ','
        resolveurl = false
        timeout = null
        proxyserver = null
        proxyport = 80
        proxyuser = null
        proxypassword = ""
        strFile = null
        throwonerror = false
        charset = null
        columns = null
        port = -1
        useragent = Constants.NAME.toString() + " (CFML Engine)"
        textqualifier = '"'
        username = null
        url = null
        redirect = true
        strPath = null
        name = null
        method = METHOD_GET
        // hasBody=false;
        firstrowasheaders = true
        getAsBinary = GET_AS_BINARY_NO
        multiPart = false
        multiPartType = MULTIPART_FORM_DATA
        result = "cfhttp"
        addtoken = false
        encode = true
        authType = AUTH_TYPE_BASIC
        workStation = null
        domain = null
        preauth = true
        encoded = ENCODED_AUTO
        compression = true
        clientCert = null
        clientCertPassword = null
        cachedWithin = null
        usePool = true
    }

    /**
     * @param firstrowasheaders
     */
    fun setFirstrowasheaders(firstrowasheaders: Boolean) {
        this.firstrowasheaders = firstrowasheaders
    }

    fun setEncodeurl(encoded: Boolean) {
        this.encoded = if (encoded) ENCODED_YES else ENCODED_NO
    }

    /**
     * set the value password When required by a server, a valid password.
     *
     * @param password value to set
     */
    fun setPassword(password: String?) {
        this.password = password
    }

    /**
     * set the value password When required by a proxy server, a valid password.
     *
     * @param proxypassword value to set
     */
    fun setProxypassword(proxypassword: String?) {
        this.proxypassword = proxypassword
    }

    /**
     * set the value delimiter Required for creating a query. Options are a tab or comma. Default is a
     * comma.
     *
     * @param delimiter value to set
     */
    fun setDelimiter(delimiter: String?) {
        this.delimiter = if (delimiter!!.length() === 0) ',' else delimiter.charAt(0)
    }

    /**
     * set the value resolveurl Yes or No. Default is No. For GET and POST operations, if Yes, page
     * reference returned into the fileContent internal variable has its internal URLs fully resolved,
     * including port number, so that links remain intact.
     *
     * @param resolveurl value to set
     */
    fun setResolveurl(resolveurl: Boolean) {
        this.resolveurl = resolveurl
    }

    fun setPreauth(preauth: Boolean) {
        this.preauth = preauth
    }

    /**
     * set the value timeout
     *
     * @param timeout value to set
     * @throws ExpressionException
     */
    @Throws(PageException::class)
    fun setTimeout(timeout: Object?) {
        if (timeout is TimeSpan) this.timeout = timeout as TimeSpan? else {
            val i: Int = Caster.toIntValue(timeout)
            if (i < 0) throw ApplicationException("invalid value [$i] for attribute timeout, value must be a positive integer greater or equal than 0")
            this.timeout = TimeSpanImpl(0, 0, 0, i)
        }
    }

    /**
     * set the value proxyserver Host name or IP address of a proxy server.
     *
     * @param proxyserver value to set
     */
    fun setProxyserver(proxyserver: String?) {
        this.proxyserver = proxyserver
    }

    /**
     * set the value proxyport The port number on the proxy server from which the object is requested.
     * Default is 80. When used with resolveURL, the URLs of retrieved documents that specify a port
     * number are automatically resolved to preserve links in the retrieved document.
     *
     * @param proxyport value to set
     */
    fun setProxyport(proxyport: Double) {
        this.proxyport = proxyport.toInt()
    }

    /**
     * set the value file The filename to be used for the file that is accessed. For GET operations,
     * defaults to the name pecified in url. Enter path information in the path attribute.
     *
     * @param file value to set
     */
    fun setFile(file: String?) {
        strFile = file
    }

    /**
     * set the value throwonerror Boolean indicating whether to throw an exception that can be caught by
     * using the cftry and cfcatch tags. The default is NO.
     *
     * @param throwonerror value to set
     */
    fun setThrowonerror(throwonerror: Boolean) {
        this.throwonerror = throwonerror
    }

    /**
     * set the value charset set the charset for the call.
     *
     * @param charset value to set
     */
    fun setCharset(charset: String?) {
        this.charset = charset
    }

    /**
     * set the value columns
     *
     * @param columns value to set
     * @throws PageException
     */
    @Throws(PageException::class)
    fun setColumns(columns: String?) {
        this.columns = ListUtil.toStringArray(ListUtil.listToArrayRemoveEmpty(columns, ","))
    }

    /**
     * set the value port The port number on the server from which the object is requested. Default is
     * 80. When used with resolveURL, the URLs of retrieved documents that specify a port number are
     * automatically resolved to preserve links in the retrieved document. If a port number is specified
     * in the url attribute, the port value overrides the value of the port attribute.
     *
     * @param port value to set
     */
    fun setPort(port: Double) {
        this.port = port.toInt()
    }

    /**
     * set the value useragent User agent request header.
     *
     * @param useragent value to set
     */
    fun setUseragent(useragent: String?) {
        this.useragent = useragent
    }

    /**
     * set the value textqualifier Required for creating a query. Indicates the start and finish of a
     * column. Should be appropriately escaped when embedded in a column. For example, if the qualifier
     * is a double quotation mark, it should be escaped as """". If there is no text qualifier in the
     * file, specify it as " ". Default is the double quotation mark (").
     *
     * @param textqualifier value to set
     */
    fun setTextqualifier(textqualifier: String?) {
        this.textqualifier = if (textqualifier!!.length() === 0) '"' else textqualifier.charAt(0)
    }

    /**
     * set the value username When required by a proxy server, a valid username.
     *
     * @param proxyuser value to set
     */
    fun setProxyuser(proxyuser: String?) {
        this.proxyuser = proxyuser
    }

    /**
     * set the value username When required by a server, a valid username.
     *
     * @param username value to set
     */
    fun setUsername(username: String?) {
        this.username = username
    }

    /**
     * set the value url Full URL of the host name or IP address of the server on which the file
     * resides. The URL must be an absolute URL, including the protocol (http or https) and hostname. It
     * may optionally contain a port number. Port numbers specified in the url attribute override the
     * port attribute.
     *
     * @param url value to set
     */
    fun setUrl(url: String?) {
        this.url = url
    }

    /**
     * set the value redirect
     *
     * @param redirect value to set
     */
    fun setRedirect(redirect: Boolean) {
        this.redirect = redirect
    }

    /**
     * set the value path The path to the directory in which a file is to be stored. If a path is not
     * specified in a POST or GET operation, a variable is created (cfhttp.fileContent) that you can use
     * to display the results of the POST operation in a cfoutput.
     *
     * @param path value to set
     */
    fun setPath(path: String?) {
        strPath = path
    }

    /**
     * set the value name The name to assign to a query if the a query is constructed from a file.
     *
     * @param name value to set
     */
    fun setName(name: String?) {
        this.name = name
    }

    @Throws(ExpressionException::class)
    fun setAuthtype(strAuthType: String?) {
        var strAuthType = strAuthType
        if (StringUtil.isEmpty(strAuthType, true)) return
        strAuthType = strAuthType.trim()
        authType = if ("basic".equalsIgnoreCase(strAuthType)) AUTH_TYPE_BASIC else if ("ntlm".equalsIgnoreCase(strAuthType)) AUTH_TYPE_NTLM else throw ExpressionException("invalid value [$strAuthType] for attribute authType, value must be one of the following [basic,ntlm]")
    }

    fun setWorkstation(workStation: String?) {
        this.workStation = workStation
    }

    fun setDomain(domain: String?) {
        this.domain = domain
    }

    /**
     * set the value method GET or POST. Use GET to download a text or binary file or to create a query
     * from the contents of a text file. Use POST to send information to a server page or a CGI program
     * for processing. POST requires the use of a cfhttpparam tag.
     *
     * @param method value to set
     * @throws ApplicationException
     */
    @Throws(ApplicationException::class)
    fun setMethod(method: String?) {
        var method = method
        method = method.toUpperCase().trim()
        val idx = methods!!.indexOf(method).toShort()
        if (idx < 0) throw ApplicationException("invalid method type [" + method + "], valid types are [" + methods.toString() + "]")
        this.method = idx
    }

    @Throws(ApplicationException::class)
    fun setCompression(strCompression: String?) {
        if (StringUtil.isEmpty(strCompression, true)) return
        val b: Boolean = Caster.toBoolean(strCompression, null)
        if (b != null) compression = b.booleanValue() else if (strCompression.trim().equalsIgnoreCase("none")) compression = false else throw ApplicationException("invalid value for attribute compression [$strCompression], valid values are: [true, false or none]")
    }

    fun setCachedwithin(cachedwithin: Object?) {
        if (StringUtil.isEmpty(cachedwithin)) return
        cachedWithin = cachedwithin
    }

    fun setPooling(usePool: Boolean) {
        this.usePool = usePool
    }

    @Override
    @Throws(PageException::class)
    fun doStartTag(): Int {
        if (addtoken) {
            setParam(HttpParamBean.TYPE_COOKIE, "cfid", pageContext.getCFID())
            setParam(HttpParamBean.TYPE_COOKIE, "cftoken", pageContext.getCFToken())
            val jsessionid: String = pageContext.getJSessionId()
            if (jsessionid != null) setParam(HttpParamBean.TYPE_COOKIE, "jsessionid", jsessionid)
        }

        // cache within
        if (StringUtil.isEmpty(cachedWithin)) {
            val tmp: Object = (pageContext as PageContextImpl?).getCachedWithin(ConfigWeb.CACHEDWITHIN_HTTP)
            if (tmp != null) setCachedwithin(tmp)
        }
        return EVAL_BODY_INCLUDE
    }

    private fun setParam(type: Int, name: String?, value: String?) {
        val hpb = HttpParamBean()
        hpb.setType(type)
        hpb.setName(name)
        hpb!!.setValue(value)
        setParam(hpb)
    }

    @Override
    @Throws(PageException::class)
    fun doEndTag(): Int {

        // because commons
        return try {
            _doEndTag()
            EVAL_PAGE
        } catch (e: IOException) {
            throw Caster.toPageException(e)
        }
    }

    @Throws(PageException::class, IOException::class)
    private fun _doEndTag() {
        val start: Long = System.nanoTime()
        val safeToMemory: Boolean = !StringUtil.isEmpty(result, true)
        val builder: HttpClientBuilder = HTTPEngine4Impl.getHttpClientBuilder()
        HTTPEngine4Impl.setConnectionManager(builder, usePool, clientCert, clientCertPassword)

        // redirect
        if (redirect) builder.setRedirectStrategy(DefaultRedirectStrategy.INSTANCE) else builder.disableRedirectHandling()

        // cookies
        val cookieStore = BasicCookieStore()
        builder.setDefaultCookieStore(cookieStore)
        val cw: ConfigWeb = pageContext.getConfig()
        var req: HttpRequestBase? = null
        var httpContext: HttpContext? = null
        var cacheHandler: CacheHandler? = null
        var cacheId: String? = null

        // HttpRequestBase req = init(pageContext.getConfig(),this,client,params,url,port);
        run {
            if (StringUtil.isEmpty(charset, true)) charset = (pageContext as PageContextImpl?).getWebCharset().name() else charset = charset.trim()

            // check if has fileUploads
            var doUploadFile = false
            for (i in 0 until params.size()) {
                if (params.get(i).getType() === HttpParamBean.TYPE_FILE) {
                    doUploadFile = true
                    break
                }
            }

            // parse url (also query string)
            val len: Int = params.size()
            val sbQS = StringBuilder()
            for (i in 0 until len) {
                val param: HttpParamBean = params.get(i)
                val type: Int = param.getType()
                // URL
                if (type == HttpParamBean.TYPE_URL) {
                    val enc: Short = param.getEncoded()
                    if (sbQS.length() > 0) sbQS.append('&')
                    sbQS.append(if (enc != ENCODED_NO) urlenc(param.getName(), charset, enc == ENCODED_AUTO) else param.getName())
                    sbQS.append('=')
                    sbQS.append(if (param.getEncoded() !== ENCODED_NO) urlenc(param.getValueAsString(), charset, enc == ENCODED_AUTO) else param.getValueAsString())
                }
            }
            var host: String? = null
            val httpHost: HttpHost?
            try {
                val _url: URL = HTTPUtil.toURL(url, port, encoded)
                httpHost = HttpHost(_url.getHost(), _url.getPort(), _url.getProtocol())
                host = _url.getHost()
                url = _url.toExternalForm()
                if (sbQS.length() > 0) {
                    // no existing QS
                    url += if (StringUtil.isEmpty(_url.getQuery())) {
                        "?$sbQS"
                    } else {
                        "&$sbQS"
                    }
                }
            } catch (mue: MalformedURLException) {
                throw Caster.toPageException(mue)
            }

            // cache
            if (cachedWithin != null) {
                cacheId = createCacheId()
                cacheHandler = pageContext.getConfig().getCacheHandlerCollection(Config.CACHE_TYPE_HTTP, null).getInstanceMatchingObject(cachedWithin, null)
                if (cacheHandler is CacheHandlerPro) {
                    val cacheItem: CacheItem = (cacheHandler as CacheHandlerPro?).get(pageContext, cacheId, cachedWithin)
                    if (cacheItem is HTTPCacheItem) {
                        logHttpRequest(pageContext, (cacheItem as HTTPCacheItem).getData(), url, getMethodAsVerb(method), System.nanoTime() - start, true)
                        pageContext.setVariable(result, (cacheItem as HTTPCacheItem).getData())
                        return
                    }
                } else if (cacheHandler != null) { // TODO this else block can be removed when all cache handlers implement CacheHandlerPro
                    val cacheItem: CacheItem = cacheHandler.get(pageContext, cacheId)
                    if (cacheItem is HTTPCacheItem) {
                        logHttpRequest(pageContext, (cacheItem as HTTPCacheItem).getData(), url, getMethodAsVerb(method), System.nanoTime() - start, true)
                        pageContext.setVariable(result, (cacheItem as HTTPCacheItem).getData())
                        return
                    }
                }
            }

            // cache not found, process and cache result if needed

            // select best matching method (get,post, post multpart (file))
            var isBinary = false
            val doMultiPart = doUploadFile || multiPart
            var eeReqPost: HttpEntityEnclosingRequest? = null
            var eeReq: HttpEntityEnclosingRequest? = null
            if (method == METHOD_GET) {
                req = HttpGetWithBody(url)
                eeReq = req as HttpEntityEnclosingRequest?
            } else if (method == METHOD_HEAD) {
                req = HttpHead(url)
            } else if (method == METHOD_DELETE) {
                isBinary = true
                req = HttpDeleteWithBody(url)
                eeReq = req as HttpEntityEnclosingRequest?
            } else if (method == METHOD_PUT) {
                isBinary = true
                val put = HttpPut(url)
                eeReqPost = put
                req = put
                eeReq = put
            } else if (method == METHOD_TRACE) {
                isBinary = true
                req = HttpTrace(url)
            } else if (method == METHOD_OPTIONS) {
                isBinary = true
                req = HttpOptions(url)
            } else if (method == METHOD_PATCH) {
                isBinary = true
                eeReq = HttpPatch(url)
                req = eeReq as HttpRequestBase?
            } else {
                isBinary = true
                eeReqPost = HttpPost(url)
                req = eeReqPost as HttpPost?
                eeReq = eeReqPost
            }
            var hasForm = false
            var hasBody = false
            var hasContentType = false
            // Set http params
            val parts: ArrayList<FormBodyPart?> = ArrayList<FormBodyPart?>()
            val acceptEncoding = StringBuilder()
            val postParam: MutableList<NameValuePair?>? = if (eeReqPost != null) ArrayList<NameValuePair?>() else null
            for (i in 0 until len) {
                val param: HttpParamBean = params.get(i)
                val type: Int = param.getType()

                // URL
                if (type == HttpParamBean.TYPE_URL) {
                    // listQS.add(new BasicNameValuePair(translateEncoding(param.getName(),
                    // http.charset),translateEncoding(param.getValueAsString(),
                    // http.charset)));
                } else if (type == HttpParamBean.TYPE_FORM) {
                    hasForm = true
                    if (method == METHOD_GET) throw ApplicationException("httpparam with type formfield can only be used when the method attribute of the parent http tag is set to post")
                    if (eeReqPost != null) {
                        if (doMultiPart) {
                            parts.add(FormBodyPart(param.getName(), StringBody(param.getValueAsString(), CharsetUtil.toCharset(charset))))
                        } else {
                            postParam!!.add(BasicNameValuePair(param.getName(), param.getValueAsString()))
                        }
                    }
                    // else if(multi!=null)multi.addParameter(param.getName(),param.getValueAsString());
                } else if (type == HttpParamBean.TYPE_CGI) {
                    if (param.getEncoded() !== ENCODED_NO) {
                        val isAuto = param.getEncoded() === ENCODED_AUTO
                        req.addHeader(urlenc(param.getName(), charset, isAuto), urlenc(param.getValueAsString(), charset, isAuto))
                    } else req.addHeader(param.getName(), param.getValueAsString())
                } else if (type == HttpParamBean.TYPE_HEADER) {
                    if (param.getName().equalsIgnoreCase("content-type")) hasContentType = true
                    if (param.getName().equalsIgnoreCase("Content-Length")) {
                    } else if (param.getName().equalsIgnoreCase("Accept-Encoding")) {
                        acceptEncoding.append(headerValue(param.getValueAsString()))
                        acceptEncoding.append(", ")
                    } else req.addHeader(param.getName(), headerValue(param.getValueAsString()))
                } else if (type == HttpParamBean.TYPE_COOKIE) {
                    HTTPEngine4Impl.addCookie(cookieStore, host, param.getName(), param.getValueAsString(), "/", charset)
                } else if (type == HttpParamBean.TYPE_FILE) {
                    hasForm = true
                    if (method == METHOD_GET) throw ApplicationException("httpparam type file can't only be used, when method of the tag http equal post")
                    // if(param.getFile()==null) throw new ApplicationException("httpparam type file can't only be used,
                    // when method of the tag http equal
                    // post");
                    val strCT = getContentType(param)
                    val ct: ContentType = HTTPUtil.toContentType(strCT, null)
                    var mt = "text/xml"
                    if (ct != null && !StringUtil.isEmpty(ct.getMimeType(), true)) mt = ct.getMimeType()
                    var cs = charset
                    if (ct != null && !StringUtil.isEmpty(ct.getCharset(), true)) cs = ct.getCharset()
                    if (doMultiPart) {
                        try {
                            val res: Resource = param.getFile()
                            parts.add(FormBodyPart(param.getName(), ResourceBody(res, mt, res.getName(), cs)))
                            // parts.add(new ResourcePart(param.getName(),new
                            // ResourcePartSource(param.getFile()),getContentType(param),_charset));
                        } catch (e: FileNotFoundException) {
                            throw ApplicationException("can't upload file, path is invalid", e.getMessage())
                        }
                    }
                } else if (type == HttpParamBean.TYPE_XML) {
                    val ct: ContentType = HTTPUtil.toContentType(param.getMimeType(), null)
                    var mt = "text/xml"
                    if (ct != null && !StringUtil.isEmpty(ct.getMimeType(), true)) mt = ct.getMimeType()
                    var cs = charset
                    if (ct != null && !StringUtil.isEmpty(ct.getCharset(), true)) cs = ct.getCharset()
                    hasBody = true
                    hasContentType = true
                    req.addHeader("Content-type", "$mt; charset=$cs")
                    if (eeReq == null) throw ApplicationException("type xml is only supported for methods get, delete, post, and put")
                    HTTPEngine4Impl.setBody(eeReq, param.getValueAsString(), mt, cs)
                } else if (type == HttpParamBean.TYPE_BODY) {
                    val ct: ContentType = HTTPUtil.toContentType(param.getMimeType(), null)
                    var mt: String? = null
                    if (ct != null && !StringUtil.isEmpty(ct.getMimeType(), true)) mt = ct.getMimeType()
                    var cs = charset
                    if (ct != null && !StringUtil.isEmpty(ct.getCharset(), true)) cs = ct.getCharset()
                    hasBody = true
                    if (eeReq == null) throw ApplicationException("type body is only supported for methods get, delete, post, and put")
                    HTTPEngine4Impl.setBody(eeReq, param!!.getValue(), mt, cs)
                } else {
                    throw ApplicationException("invalid type [$type]")
                }
            }

            // post params
            if (postParam != null && postParam.size() > 0) eeReqPost.setEntity(UrlEncodedFormEntity(postParam, charset))
            if (compression) {
                acceptEncoding.append("gzip")
            } else {
                acceptEncoding.append("deflate;q=0")
                req.setHeader("TE", "deflate;q=0")
            }
            req.setHeader("Accept-Encoding", acceptEncoding.toString())

            // multipart
            if (doMultiPart && eeReq != null) {
                hasContentType = true
                var doIt = true
                if (!multiPart && parts.size() === 1) {
                    val body: ContentBody = parts.get(0).getBody()
                    if (body is StringBody) {
                        val sb: StringBody = body as StringBody
                        try {
                            val ct: org.apache.http.entity.ContentType = org.apache.http.entity.ContentType.create(sb.getMimeType(), sb.getCharset())
                            val str: String = IOUtil.toString(sb.getReader())
                            val entity = StringEntity(str, ct)
                            eeReq.setEntity(entity)
                        } catch (e: IOException) {
                            throw Caster.toPageException(e)
                        }
                        doIt = false
                    }
                }
                if (doIt) {
                    val mpeBuilder: MultipartEntityBuilder = MultipartEntityBuilder.create().setStrictMode()

                    // enabling the line below will append charset=... to the Content-Type header
                    // if (!StringUtil.isEmpty(charset, true))
                    // mpeBuilder.setCharset(CharsetUtil.toCharset(charset));
                    val it: Iterator<FormBodyPart?> = parts.iterator()
                    while (it.hasNext()) {
                        val part: FormBodyPart? = it.next()
                        mpeBuilder.addPart(part)
                    }
                    eeReq.setEntity(mpeBuilder.build())
                }
                // eem.setRequestEntity(new MultipartRequestEntityFlex(parts.toArray(new Part[parts.size()]),
                // eem.getParams(),http.multiPartType));
            }
            if (hasBody && hasForm) throw ApplicationException("mixing httpparam  type file/formfield and body/XML is not allowed")
            if (!hasContentType) {
                if (isBinary) {
                    if (hasBody) req.addHeader("Content-type", "application/octet-stream") else req.addHeader("Content-type", "application/x-www-form-urlencoded; charset=$charset")
                } else {
                    if (hasBody) req.addHeader("Content-type", "text/html; charset=$charset")
                }
            }

            // set User Agent
            if (!hasHeaderIgnoreCase(req, "User-Agent")) req.setHeader("User-Agent", useragent)

            // set timeout
            setTimeout(builder, checkRemainingTimeout())

            // set Username and Password
            if (username != null) {
                if (password == null) password = ""
                if (AUTH_TYPE_NTLM == authType) {
                    if (StringUtil.isEmpty(workStation, true)) throw ApplicationException("attribute workstation is required when authentication type is [NTLM]")
                    if (StringUtil.isEmpty(domain, true)) throw ApplicationException("attribute domain is required when authentication type is [NTLM]")
                    HTTPEngine4Impl.setNTCredentials(builder, username, password, workStation, domain)
                } else {
                    httpContext = HTTPEngine4Impl.setCredentials(builder, httpHost, username, password, preauth)
                }
            }

            // set Proxy
            var proxy: ProxyData? = null
            if (!StringUtil.isEmpty(proxyserver)) {
                proxy = ProxyDataImpl.validate(ProxyDataImpl.getInstance(proxyserver, proxyport, proxyuser, proxypassword), host)
            }
            if (proxy == null) {
                proxy = ProxyDataImpl.validate((pageContext as PageContextImpl?).getProxyData(), host)
            }
            HTTPEngine4Impl.setProxy(host, builder, req, proxy)
        }
        var client: CloseableHttpClient? = null
        try {
            if (httpContext == null) httpContext = BasicHttpContext()
            val cfhttp: Struct = StructImpl()
            cfhttp.setEL(ERROR_DETAIL, "")
            if (safeToMemory) pageContext.setVariable(result, cfhttp)

            /////////////////////////////////////////// EXECUTE
            /////////////////////////////////////////// /////////////////////////////////////////////////
            client = builder.build()
            val e = Executor4(pageContext, this, client, httpContext, req, redirect)
            var rsp: HTTPResponse4Impl? = null
            if (timeout == null || timeout.getMillis() <= 0) {
                rsp = try {
                    e.execute(httpContext)
                } catch (t: Throwable) {
                    ExceptionUtil.rethrowIfNecessary(t)
                    if (!throwonerror) {
                        if (t is SocketTimeoutException) setRequestTimeout(cfhttp) else setUnknownHost(cfhttp, t)
                        logHttpRequest(pageContext, cfhttp, url, req.getMethod(), System.nanoTime() - start, false)
                        return
                    }
                    throw toPageException(t, rsp)
                }
            } else {
                e.start()
                try {
                    synchronized(this) { // print.err(timeout);
                        this.wait(timeout.getMillis())
                    }
                } catch (ie: InterruptedException) {
                    throw Caster.toPageException(ie)
                }
                if (e.t != null) {
                    if (!throwonerror) {
                        setUnknownHost(cfhttp, e.t)
                        logHttpRequest(pageContext, cfhttp, url, req.getMethod(), System.nanoTime() - start, false)
                        return
                    }
                    throw toPageException(e.t, rsp)
                }
                rsp = e.response
                if (!e.done) {
                    req.abort()
                    if (throwonerror) throw HTTPException("408 Request Time-out", "a timeout occurred in tag http", 408, "Time-out", if (rsp == null) null else rsp.getURL())
                    setRequestTimeout(cfhttp)
                    logHttpRequest(pageContext, cfhttp, url, req.getMethod(), System.nanoTime() - start, false)
                    return
                    // throw new ApplicationException("timeout");
                }
            }

            /////////////////////////////////////////// EXECUTE
            /////////////////////////////////////////// /////////////////////////////////////////////////
            var statCode = 0
            // Write Response Scope
            // String rawHeader=httpMethod.getStatusLine().toString();
            var mimetype: String? = null
            var contentEncoding: String? = null
            var rspCharset: String? = null

            // status code
            cfhttp.set(STATUSCODE, (rsp.getStatusCode().toString() + " " + rsp.getStatusText()).trim())
            cfhttp.set(STATUS_CODE, Double.valueOf(rsp.getStatusCode().also { statCode = it }))
            cfhttp.set(STATUS_TEXT, rsp.getStatusText())
            cfhttp.set(HTTP_VERSION, rsp.getProtocolVersion())
            val locations: Array = rsp.getLocations()
            if (locations != null) cfhttp.set(LOCATIONS, locations)

            // responseHeader
            val headers: Array<tachyon.commons.net.http.Header?> = rsp.getAllHeaders()
            val raw = StringBuffer(rsp.getStatusLine().toString() + " ")
            val responseHeader: Struct = StructImpl()
            var cookie: Struct
            val setCookie: Array = ArrayImpl()
            val cookies: Query = QueryImpl(arrayOf<String?>("name", "value", "path", "domain", "expires", "secure", "httpOnly", "samesite"), 0, "cookies")
            for (i in headers.indices) {
                val header: tachyon.commons.net.http.Header? = headers[i]
                // print.ln(header);
                raw.append(header.toString().toString() + " ")
                if (header.getName().equalsIgnoreCase("Set-Cookie")) {
                    setCookie.append(header.getValue())
                    parseCookie(cookies, header.getValue(), charset)
                } else {
                    // print.ln(header.getName()+"-"+header.getValue());
                    val value: Object = responseHeader.get(KeyImpl.init(header.getName()), null)
                    if (value == null) responseHeader.set(KeyImpl.init(header.getName()), header.getValue()) else {
                        var arr: Array? = null
                        if (value is Array) {
                            arr = value
                        } else {
                            arr = ArrayImpl()
                            responseHeader.set(KeyImpl.init(header.getName()), arr)
                            arr.appendEL(value)
                        }
                        arr.appendEL(header.getValue())
                    }
                }

                // Content-Type
                if (header.getName().equalsIgnoreCase("Content-Type")) {
                    mimetype = header.getValue()
                    if (mimetype == null) mimetype = NO_MIMETYPE
                }

                // Content-Encoding
                if (header.getName().equalsIgnoreCase("Content-Encoding")) {
                    contentEncoding = header.getValue()
                }
            }
            val tmpCharset: Array<String?> = HTTPUtil.splitMimeTypeAndCharset(mimetype, null)
            rspCharset = if (tmpCharset != null) tmpCharset[1] else null
            cfhttp.set(RESPONSEHEADER, responseHeader)
            cfhttp.set(KeyConstants._cookies, cookies)
            responseHeader.set(STATUS_CODE, Double.valueOf(rsp.getStatusCode().also { statCode = it }))
            responseHeader.set(EXPLANATION, rsp.getStatusText())
            if (setCookie.size() > 0) responseHeader.set(SET_COOKIE, setCookie)

            // is text
            if (mimetype == null || mimetype === NO_MIMETYPE) {
            }
            var _isText: Boolean? = null
            if (mimetype != null && mimetype !== NO_MIMETYPE) {
                _isText = HTTPUtil.isTextMimeType(mimetype)
            }

            // is multipart
            val isMultipart: Boolean = MultiPartResponseUtils.isMultipart(mimetype)

            // we still don't know the mime type
            var barr: ByteArray? = null
            if (Boolean.TRUE !== _isText && safeToMemory && !isMultipart) {
                barr = contentAsBinary(rsp, contentEncoding)
                if (_isText == null) {
                    val mt: String = IOUtil.getMimeType(barr, null)
                    if (mt != null) {
                        _isText = HTTPUtil.isTextMimeType(mt)
                        mimetype = mt
                    }
                }
            }
            if (safeToMemory && _isText != null) cfhttp.set(KeyConstants._text, _isText)

            // mimetype charset
            // boolean responseProvideCharset=false;
            if (!StringUtil.isEmpty(mimetype, true)) {
                if (Boolean.TRUE === _isText) {
                    val types: Array<String?> = HTTPUtil.splitMimeTypeAndCharset(mimetype, null)
                    if (types[0] != null) cfhttp.set(KeyConstants._mimetype, types[0])
                    if (types[1] != null) rspCharset = types[1] // only text types have charset
                } else cfhttp.set(KeyConstants._mimetype, mimetype)
            } else cfhttp.set(KeyConstants._mimetype, NO_MIMETYPE)

            // charset
            cfhttp.set(CHARSET, rspCharset ?: "")

            // File
            var file: Resource? = null
            if (strFile != null && strPath != null) {
                file = ResourceUtil.toResourceNotExisting(pageContext, strPath).getRealResource(strFile)
            } else if (strFile != null) {
                file = ResourceUtil.toResourceNotExisting(pageContext, strFile)
            } else if (strPath != null) {
                file = ResourceUtil.toResourceNotExisting(pageContext, strPath)
                // Resource dir = file.getParentResource();
                if (file.isDirectory()) {
                    file = file.getRealResource(req.getURI().getPath()) // TODO was getName()
                    // ->http://hc.apache.org/httpclient-3.x/apidocs/org/apache/commons/httpclient/URI.html#getName()
                }
            }
            if (file != null) pageContext.getConfig().getSecurityManager().checkFileLocation(file)

            // filecontent
            if (Boolean.TRUE === _isText && getAsBinary != GET_AS_BINARY_YES && safeToMemory) {
                val tmp: String = rsp.getCharset()
                val responseCharset: Charset? = if (StringUtil.isEmpty(tmp, true)) null else CharsetUtil.toCharset(tmp)
                // store to memory
                val str: String?
                str = if (barr == null) contentAsString(rsp, responseCharset, contentEncoding, e) else IOUtil.toString(barr, responseCharset)
                cfhttp.set(KeyConstants._filecontent, str)

                // store to file
                if (file != null) {
                    IOUtil.write(file, str, (pageContext as PageContextImpl?).getWebCharset(), false)
                }

                // store to variable
                if (name != null) {
                    val qry: Query = CSVParser.toQuery(str, delimiter, textqualifier, columns, firstrowasheaders)
                    pageContext.setVariable(name, qry)
                }
            } else {
                if (barr == null && safeToMemory) barr = contentAsBinary(rsp, contentEncoding)

                // IF Multipart response get file content and parse parts
                if (barr != null) {
                    if (isMultipart) {
                        cfhttp.set(KeyConstants._filecontent, MultiPartResponseUtils.getParts(barr, mimetype))
                    } else {
                        cfhttp.set(KeyConstants._filecontent, barr)
                    }
                }

                // store to file
                if (file != null) {
                    try {
                        if (barr != null) IOUtil.copy(ByteArrayInputStream(barr), file, true) else storeTo(rsp, contentEncoding, file)
                    } catch (ioe: IOException) {
                        throw Caster.toPageException(ioe)
                    }
                }
            }

            // header
            cfhttp.set(KeyConstants._header, raw.toString())
            if (!isStatusOK(rsp.getStatusCode())) {
                val msg: String = rsp.getStatusCode().toString() + " " + rsp.getStatusText()
                cfhttp.setEL(ERROR_DETAIL, msg)
                if (throwonerror) {
                    throw HTTPException(msg, null, rsp.getStatusCode(), rsp.getStatusText(), rsp.getURL())
                }
            }

            // TODO: check if we can use statCode instead of rsp.getStatusCode() everywhere and cleanup the code
            if (cacheHandler != null && rsp.getStatusCode() === 200) {
                // add to cache
                cacheHandler.set(pageContext, cacheId, cachedWithin, HTTPCacheItem(cfhttp, url, System.nanoTime() - start))
            }
            logHttpRequest(pageContext, cfhttp, url, req.getMethod(), System.nanoTime() - start, false)
        } finally {
            if (client != null) client.close()
        }
    }

    @Throws(PageException::class, IOException::class)
    private fun contentAsBinary(rsp: HTTPResponse4Impl?, contentEncoding: String?): ByteArray? {
        var barr: ByteArray? = null
        var `is`: InputStream? = null
        if (isGzipEncoded(contentEncoding)) {
            if (method != METHOD_HEAD) {
                `is` = rsp.getContentAsStream()
                `is` = if (rsp.getStatusCode() !== 200) CachingGZIPInputStream(`is`) else GZIPInputStream(`is`)
            }
            barr = try {
                try {
                    if (`is` == null) ByteArray(0) else IOUtil.toBytes(`is`)
                } catch (eof: EOFException) {
                    if (`is` is CachingGZIPInputStream) IOUtil.toBytes((`is` as CachingGZIPInputStream?).getRawData()) else throw eof
                }
            } catch (t: IOException) {
                throw Caster.toPageException(t)
            } finally {
                IOUtil.close(`is`)
            }
        } else {
            barr = try {
                if (method != METHOD_HEAD) rsp.getContentAsByteArray() else ByteArray(0)
            } catch (t: IOException) {
                throw Caster.toPageException(t)
            }
        }
        return barr
    }

    @Throws(PageException::class, IOException::class)
    private fun storeTo(rsp: HTTPResponse4Impl?, contentEncoding: String?, target: Resource?) {
        var `is`: InputStream? = null
        if (target != null && isGzipEncoded(contentEncoding)) {
            if (method != METHOD_HEAD) {
                `is` = rsp.getContentAsStream()
                `is` = if (rsp.getStatusCode() !== 200) CachingGZIPInputStream(`is`) else GZIPInputStream(`is`)
            }
            try {
                if (`is` != null) IOUtil.copy(`is`, target, true)
            } catch (eof: EOFException) {
                if (`is` is CachingGZIPInputStream) {
                    IOUtil.copy((`is` as CachingGZIPInputStream?).getRawData(), target, true)
                } else throw eof
            }
        } else {
            if (method != METHOD_HEAD) {
                IOUtil.copy(rsp.getContentAsStream(), target, true)
            }
        }
    }

    @Throws(PageException::class)
    private fun contentAsString(rsp: HTTPResponse4Impl?, responseCharset: Charset?, contentEncoding: String?, e: Executor4?): String? {
        var str: String
        var `is`: InputStream? = null
        try {

            // read content
            if (method != METHOD_HEAD) {
                `is` = rsp.getContentAsStream()
                if (`is` != null && isGzipEncoded(contentEncoding)) `is` = if (rsp.getStatusCode() !== 200) CachingGZIPInputStream(`is`) else GZIPInputStream(`is`)
            }
            try {
                try {
                    str = if (`is` == null) "" else IOUtil.toString(`is`, responseCharset, checkRemainingTimeout().getMillis())
                } catch (eof: EOFException) {
                    if (`is` is CachingGZIPInputStream) {
                        str = IOUtil.toString((`is` as CachingGZIPInputStream?).getRawData().also { `is` = it }, responseCharset, checkRemainingTimeout().getMillis())
                    } else throw eof
                }
            } catch (uee: UnsupportedEncodingException) {
                str = IOUtil.toString(`is`, null as Charset?, checkRemainingTimeout().getMillis())
            }
        } catch (ioe: IOException) {
            throw Caster.toPageException(ioe)
        } finally {
            try {
                IOUtil.close(`is`)
            } catch (ioe: IOException) {
                throw Caster.toPageException(ioe)
            }
        }
        if (str == null) str = ""
        if (resolveurl) {
            // if(e.redirectURL!=null)url=e.redirectURL.toExternalForm();
            str = URLResolver().transform(str, e!!.response.getTargetURL(), false)
        }
        return str
    }

    @Throws(RequestTimeoutException::class)
    private fun checkRemainingTimeout(): TimeSpan? {
        val remaining: TimeSpan = PageContextUtil.remainingTime(pageContext, true)
        if (timeout == null || timeout.getSeconds() as Int <= 0 || timeout.getSeconds() > remaining.getSeconds()) { // not set
            timeout = remaining
        }
        return timeout
    }

    private fun createCacheId(): String? {
        return CacheHandlerCollectionImpl.createId(url, if (addtoken) pageContext.getURLToken() else "", method, params, username, password, port, proxyserver, proxyport, proxyuser,
                proxypassword, useragent)
    }

    fun dec(str: String?): String? {
        return dec(str, charset)
    }

    private fun toPageException(t: Throwable?, rsp: HTTPResponse4Impl?): PageException? {
        if (t is SocketTimeoutException) {
            val he = HTTPException("408 Request Time-out", "a timeout occurred in tag http", 408, "Time-out", if (rsp == null) null else rsp.getURL())
            val merged: List<StackTraceElement?> = ArrayUtil.merge(t.getStackTrace(), he.getStackTrace())
            val traces: Array<StackTraceElement?> = arrayOfNulls<StackTraceElement?>(merged.size())
            val it: Iterator<StackTraceElement?> = merged.iterator()
            var index = 0
            while (it.hasNext()) {
                traces[index++] = it.next()
            }
            he.setStackTrace(traces)
            return he
        }
        val pe: PageException = Caster.toPageException(Exception(t))
        if (pe is NativeException) {
            (pe as NativeException).setAdditional(KeyConstants._url, url)
        }
        return pe
    }

    private fun setUnknownHost(cfhttp: Struct?, t: Throwable?) {
        cfhttp.setEL(CHARSET, "")
        cfhttp.setEL(ERROR_DETAIL, "Unknown host: " + t.getMessage())
        cfhttp.setEL(KeyConstants._filecontent, "Connection Failure")
        cfhttp.setEL(KeyConstants._header, "")
        cfhttp.setEL(KeyConstants._mimetype, "Unable to determine MIME type of file.")
        cfhttp.setEL(RESPONSEHEADER, StructImpl())
        cfhttp.setEL(STATUSCODE, "Connection Failure. Status code unavailable.")
        cfhttp.setEL(STATUS_CODE, Double.valueOf(0))
        cfhttp.setEL(STATUS_TEXT, "Connection Failure")
        cfhttp.setEL(KeyConstants._text, Boolean.TRUE)
    }

    private fun setRequestTimeout(cfhttp: Struct?) {
        cfhttp.setEL(CHARSET, "")
        cfhttp.setEL(ERROR_DETAIL, "")
        cfhttp.setEL(KeyConstants._filecontent, "Connection Timeout")
        cfhttp.setEL(KeyConstants._header, "")
        cfhttp.setEL(KeyConstants._mimetype, "Unable to determine MIME type of file.")
        cfhttp.setEL(RESPONSEHEADER, StructImpl())
        cfhttp.setEL(STATUSCODE, "408 Request Time-out")
        cfhttp.setEL(STATUS_CODE, Double.valueOf(408))
        cfhttp.setEL(STATUS_TEXT, "Request Time-out")
        cfhttp.setEL(KeyConstants._text, Boolean.TRUE)
    }

    @Override
    fun doInitBody() {
    }

    @Override
    fun doAfterBody(): Int {
        return SKIP_BODY
    }

    /**
     * sets if has body or not
     *
     * @param hasBody
     */
    fun hasBody(hasBody: Boolean) {}

    /**
     * @param param
     */
    fun setParam(param: HttpParamBean?) {
        params.add(param)
    }

    /**
     * @param getAsBinary The getasbinary to set.
     */
    fun setGetasbinary(getAsBinary: String?) {
        // TODO support never, wird das verwendet?
        var getAsBinary = getAsBinary
        getAsBinary = getAsBinary.toLowerCase().trim()
        if (getAsBinary.equals("yes") || getAsBinary.equals("true")) this.getAsBinary = GET_AS_BINARY_YES else if (getAsBinary.equals("no") || getAsBinary.equals("false")) this.getAsBinary = GET_AS_BINARY_NO else if (getAsBinary.equals("auto")) this.getAsBinary = GET_AS_BINARY_AUTO
    }

    /**
     * @param multiPart The multipart to set.
     */
    fun setMultipart(multiPart: Boolean) {
        this.multiPart = multiPart
    }

    /**
     * @param multiPartType The multipart to set.
     * @throws ApplicationException
     */
    @Throws(ApplicationException::class)
    fun setMultiparttype(multiPartType: String?) {
        var multiPartType = multiPartType
        if (StringUtil.isEmpty(multiPartType)) return
        multiPartType = multiPartType.trim().toLowerCase()
        if ("form-data".equals(multiPartType)) this.multiPartType = MULTIPART_FORM_DATA else throw ApplicationException("invalid value for attribute multiPartType [$multiPartType]", "attribute must have one of the following values [form-data]")
    }

    /**
     * @param result The result to set.
     */
    fun setResult(result: String?) {
        this.result = result
    }

    /**
     * @param addtoken the addtoken to set
     */
    fun setAddtoken(addtoken: Boolean) {
        this.addtoken = addtoken
    }

    /**
     * @param encode encode the URL
     */
    fun setEncode(encode: Boolean) {
        this.encode = encode
    }

    /**
     * @param clientCert the clientCert to set
     */
    fun setClientcert(clientCert: String?) {
        this.clientCert = clientCert
    }

    /**
     * @param clientCertPassword the clientCertPassword to set
     */
    fun setClientcertpassword(clientCertPassword: String?) {
        this.clientCertPassword = clientCertPassword
    }

    companion object {
        val MULTIPART_RELATED: String? = "multipart/related"
        val MULTIPART_FORM_DATA: String? = "multipart/form-data"

        /**
         * Maximum redirect count (5)
         */
        const val MAX_REDIRECT: Short = 15

        /**
         * Constant value for HTTP Status Code "moved Permanently 301"
         */
        const val STATUS_REDIRECT_MOVED_PERMANENTLY = 301

        /**
         * Constant value for HTTP Status Code "Found 302"
         */
        const val STATUS_REDIRECT_FOUND = 302

        /**
         * Constant value for HTTP Status Code "see other 303"
         */
        const val STATUS_REDIRECT_SEE_OTHER = 303
        const val STATUS_REDIRECT_TEMPORARY_REDIRECT = 307
        private const val METHOD_GET: Short = 0
        private const val METHOD_POST: Short = 1
        private const val METHOD_HEAD: Short = 2
        private const val METHOD_PUT: Short = 3
        private const val METHOD_DELETE: Short = 4
        private const val METHOD_OPTIONS: Short = 5
        private const val METHOD_TRACE: Short = 6
        private const val METHOD_PATCH: Short = 7
        private val methods: List<String?>? = Arrays.asList(arrayOf<String?>("GET", "POST", "HEAD", "PUT", "DELETE", "OPTIONS", "TRACE", "PATCH"))
        private val NO_MIMETYPE: String? = "Unable to determine MIME type of file."
        private const val GET_AS_BINARY_NO: Short = 0
        private const val GET_AS_BINARY_YES: Short = 1
        private const val GET_AS_BINARY_AUTO: Short = 2
        private val STATUSCODE: Key? = KeyConstants._statuscode
        private val CHARSET: Key? = KeyConstants._charset
        private val ERROR_DETAIL: Key? = KeyImpl.getInstance("errordetail")
        private val STATUS_CODE: Key? = KeyImpl.getInstance("status_code")
        private val STATUS_TEXT: Key? = KeyImpl.getInstance("status_text")
        private val HTTP_VERSION: Key? = KeyImpl.getInstance("http_version")
        private val LOCATIONS: Key? = KeyImpl.getInstance("locations")
        private val EXPLANATION: Key? = KeyImpl.getInstance("explanation")
        private val RESPONSEHEADER: Key? = KeyImpl.getInstance("responseheader")
        private val SET_COOKIE: Key? = KeyImpl.getInstance("set-cookie")
        private const val AUTH_TYPE_BASIC: Short = 0
        private const val AUTH_TYPE_NTLM: Short = 1
        val ENCODED_AUTO: Short = HTTPUtil.ENCODED_AUTO
        val ENCODED_YES: Short = HTTPUtil.ENCODED_YES
        val ENCODED_NO: Short = HTTPUtil.ENCODED_NO
        @Throws(ApplicationException::class)
        private fun getMethodAsVerb(method: Short): String? {
            if (method < 0 || method > methods!!.size() - 1) throw ApplicationException("invalid method [" + method + "], valid types are [" + methods.toString() + "]") // never
            // will
            // reach
            // this, due
            // to above
            return methods!![method.toInt()]
        }

        fun parseCookie(cookies: Struct?, raw: String?, charset: String?) {
            val arr: Array<String?> = ListUtil.trimItems(ListUtil.trim(ListUtil.listToStringArray(raw, ';')))
            if (arr.size == 0) return
            val item: String?
            val index: Int
            var n: String
            var v: String
            // name/value
            if (arr.size > 0) {
                item = arr[0]
                index = item.indexOf('=')
                if (index == -1) {
                    cookies.setEL(dec(item, charset), "")
                } else { // name and value
                    cookies.setEL(dec(item.substring(0, index), charset), dec(item.substring(index + 1), charset))
                }
            }
        }

        fun parseCookie(cookies: Query?, raw: String?, charset: String?) {
            val arr: Array<String?> = ListUtil.trimItems(ListUtil.trim(ListUtil.listToStringArray(raw, ';')))
            if (arr.size == 0) return
            val row: Int = cookies.addRow()
            var item: String?
            var index: Int
            // name/value
            if (arr.size > 0) {
                item = arr[0]
                index = item.indexOf('=')
                if (index == -1) // only name
                    cookies.setAtEL(KeyConstants._name, row, dec(item, charset)) else { // name and value
                    cookies.setAtEL(KeyConstants._name, row, dec(item.substring(0, index), charset))
                    cookies.setAtEL(KeyConstants._value, row, dec(item.substring(index + 1), charset))
                }
            }
            var n: String?
            var v: String?
            cookies.setAtEL("secure", row, Boolean.FALSE)
            cookies.setAtEL("httpOnly", row, Boolean.FALSE)
            for (i in 1 until arr.size) {
                item = arr[i]
                index = item.indexOf('=')
                if (index == -1) // only name
                    cookies.setAtEL(dec(item, charset), row, Boolean.TRUE) else { // name and value
                    n = dec(item.substring(0, index), charset)
                    v = dec(item.substring(index + 1), charset)
                    if (n.equalsIgnoreCase("expires")) {
                        val d: DateTime = Caster.toDate(v, false, null, null)
                        if (d != null) {
                            cookies.setAtEL(n, row, d)
                            continue
                        }
                    }
                    cookies.setAtEL(n, row, v)
                }
            }
        }

        fun dec(str: String?, charset: String?): String? {
            return ReqRspUtil.decode(str, charset, false)
        }

        fun isStatusOK(statusCode: Int): Boolean {
            return statusCode >= 200 && statusCode <= 299
        }

        private fun hasHeaderIgnoreCase(req: HttpRequestBase?, name: String?): Boolean {
            val headers: Array<org.apache.http.Header?> = req.getAllHeaders() ?: return false
            for (i in headers.indices) {
                if (name.equalsIgnoreCase(headers[i].getName())) return true
            }
            return false
        }

        private fun headerValue(value: String?): String? {
            var value: String? = value ?: return null
            value = value.trim()
            value = value.replace('\n', ' ')
            value = value.replace('\r', ' ')
            /*
		 * int len=value.length(); char c; for(int i=0;i<len;i++){ c=value.charAt(i); if(c=='\n' || c=='\r')
		 * return value.substring(0,i); }
		 */return value
        }

        private fun toQueryString(qsPairs: Array<NameValuePair?>?): String? {
            val sb = StringBuffer()
            for (i in qsPairs.indices) {
                if (sb.length() > 0) sb.append('&')
                sb.append(qsPairs!![i].getName())
                if (qsPairs[i].getValue() != null) {
                    sb.append('=')
                    sb.append(qsPairs[i].getValue())
                }
            }
            return sb.toString()
        }

        @Throws(UnsupportedEncodingException::class)
        private fun urlenc(str: String?, charset: String?, checkIfNeeded: Boolean): String? {
            return if (checkIfNeeded && !ReqRspUtil.needEncoding(str, false)) str else URLEncoder.encode(str, CharsetUtil.toCharset(charset))
        }

        @Throws(PageException::class)
        private fun logHttpRequest(pc: PageContext?, data: Struct?, url: String?, method: String?, executionTimeNS: Long, cached: Boolean) {
            val log: Log = ThreadLocalPageContext.getLog(pc, "application")
            if (log != null) log.log(Log.LEVEL_TRACE, "cftrace", "httpRequest [" + method + "] to [" + url + "], returned [" + data.get(STATUSCODE) + "] in "
                    + executionTimeNS / 1000000 + "ms, " + (if (cached) "(cached response)" else "") + " at " + CallStackGet.call(pc, "text"))
        }

        /**
         * checks if status code is a redirect
         *
         * @param status
         * @return is redirect
         */
        fun isRedirect(status: Int): Boolean {
            return status == STATUS_REDIRECT_FOUND || status == STATUS_REDIRECT_MOVED_PERMANENTLY || status == STATUS_REDIRECT_SEE_OTHER || status == STATUS_REDIRECT_TEMPORARY_REDIRECT
        }

        /**
         * merge to pathes to one
         *
         * @param current
         * @param realPath
         * @return
         * @throws MalformedURLException
         */
        @Throws(MalformedURLException::class)
        fun mergePath(current: String?, realPath: String?): String? {

            // get current directory
            var realPath = realPath
            var currDir: String?
            if (current == null || current.indexOf('/') === -1) currDir = "/" else if (current.endsWith("/")) currDir = current else currDir = current.substring(0, current.lastIndexOf('/') + 1)

            // merge together
            val path: String?
            if (realPath.startsWith("./")) path = currDir + realPath.substring(2) else if (realPath.startsWith("/")) path = realPath else if (!realPath.startsWith("../")) path = currDir + realPath else {
                while (realPath.startsWith("../") || currDir!!.length() === 0) {
                    realPath = realPath.substring(3)
                    currDir = currDir.substring(0, currDir!!.length() - 1)
                    val index: Int = currDir.lastIndexOf('/')
                    if (index == -1) throw MalformedURLException("invalid realpath definition for URL")
                    currDir = currDir.substring(0, index + 1)
                }
                path = currDir + realPath
            }
            return path
        }

        private fun getContentType(param: HttpParamBean?): String? {
            var mimeType: String = param.getMimeType()
            if (StringUtil.isEmpty(mimeType, true)) {
                mimeType = ResourceUtil.getMimeType(param.getFile(), null)
            }
            return mimeType
        }

        fun isGzipEncoded(contentEncoding: String?): Boolean {
            return !StringUtil.isEmpty(contentEncoding) && StringUtil.indexOfIgnoreCase(contentEncoding, "gzip") !== -1
        }

        fun getOutput(`is`: InputStream?, contentType: String?, contentEncoding: String?, closeIS: Boolean): Object? {
            var `is`: InputStream? = `is`
            var contentType = contentType
            if (StringUtil.isEmpty(contentType)) contentType = "text/html"

            // Gzip
            if (isGzipEncoded(contentEncoding)) {
                try {
                    `is` = GZIPInputStream(`is`)
                } catch (e: IOException) {
                }
            }
            try {
                // text
                if (HTTPUtil.isTextMimeType(contentType) === Boolean.TRUE) {
                    val tmp: Array<String?> = HTTPUtil.splitMimeTypeAndCharset(contentType, null)
                    val cs: Charset? = getCharset(tmp[1])
                    try {
                        return IOUtil.toString(`is`, cs)
                    } catch (e: IOException) {
                    }
                } else {
                    try {
                        return IOUtil.toBytes(`is`)
                    } catch (e: IOException) {
                    }
                }
            } finally {
                if (closeIS) IOUtil.closeEL(`is`)
            }
            return ""
        }

        fun locationURL(req: HttpUriRequest?, rsp: HttpResponse?): URL? {
            var url: URL? = null
            url = try {
                req.getURI().toURL()
            } catch (e1: MalformedURLException) {
                return null
            }
            val h: Header = HTTPResponse4Impl.getLastHeaderIgnoreCase(rsp, "location")
            if (h != null) {
                val str: String = h.getValue()
                return try {
                    URL(str)
                } catch (e: MalformedURLException) {
                    try {
                        URL(url.getProtocol(), url.getHost(), url.getPort(), mergePath(url.getFile(), str))
                    } catch (e1: MalformedURLException) {
                        null
                    }
                }
            }
            return null
        }

        fun getCharset(strCharset: String?): Charset? {
            return if (!StringUtil.isEmpty(strCharset, true)) CharsetUtil.toCharset(strCharset) else CharsetUtil.getWebCharset()
        }

        fun setTimeout(builder: HttpClientBuilder?, timeout: TimeSpan?) {
            if (timeout == null || timeout.getMillis() <= 0) return
            var ms = timeout.getMillis() as Int
            if (ms < 0) ms = Integer.MAX_VALUE

            // builder.setConnectionTimeToLive(ms, TimeUnit.MILLISECONDS);
            val sc: SocketConfig = SocketConfig.custom().setSoTimeout(ms).build()
            builder.setDefaultSocketConfig(sc)
        }
    }
}

internal class Executor4(pc: PageContext?, val http: Http?, client: CloseableHttpClient?, context: HttpContext?, req: HttpRequestBase?, redirect: Boolean) : PageContextThread(pc) {
    private val client: CloseableHttpClient?
    val redirect: Boolean
    var t: Throwable? = null
    var done = false

    // URL redirectURL;
    var response: HTTPResponse4Impl? = null
    private val req: HttpRequestBase?
    private val context: HttpContext?
    @Override
    fun run(pc: PageContext?) {
        try {
            response = execute(context)
            done = true
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            this.t = t
        } finally {
            SystemUtil.notify(http)
        }
    }

    @Throws(IOException::class)
    fun execute(context: HttpContext?): HTTPResponse4Impl? {
        return HTTPResponse4Impl(null, context, req, client.execute(req, context)).also { response = it }
    }

    init {
        this.client = client
        this.context = context
        this.redirect = redirect
        this.req = req
    }
}

internal class HttpDeleteWithBody(uri: String?) : HttpEntityEnclosingRequestBase() {
    companion object {
        @get:Override
        val method: String? = "DELETE"
            get() = Companion.field
    }

    init {
        setURI(URI.create(uri))
    }
}

internal class HttpGetWithBody(uri: String?) : HttpEntityEnclosingRequestBase() {
    companion object {
        @get:Override
        val method: String? = "GET"
            get() = Companion.field
    }

    init {
        setURI(URI.create(uri))
    }
}