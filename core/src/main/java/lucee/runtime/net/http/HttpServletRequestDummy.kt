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
package lucee.runtime.net.http

import java.io.BufferedReader

class HttpServletRequestDummy(contextRoot: Resource?, serverName: String?, scriptName: String?, queryString: String?, cookies: Array<Cookie?>?, headers: Array<Pair?>?, parameters: Array<Pair?>?,
                              attributes: Struct?, session: HttpSession?, inputData: ByteArray?) : HttpServletRequest, Serializable {
    /**
     * sets an array containing all of the Cookie objects the client sent with this request. This method
     * returns null if no cookies were sent.
     *
     * @param cookies
     */
    @get:Override
    var cookies: Array<Cookie?>?

    /**
     * sets the name of the authentication scheme used to protect the servlet. All servlet containers
     * support basic, form and client certificate authentication, and may additionally support digest
     * authentication.
     *
     * @param authType authentication type
     */
    @get:Override
    var authType: String? = null
    private var headers: Array<Pair<String?, Object?>?>? = arrayOfNulls<Pair?>(0)
    private var parameters: Array<Pair<String?, Object?>?>? = arrayOfNulls<Pair?>(0)
    private var attributes: Struct? = StructImpl()

    /**
     * sets the request method
     *
     * @param method
     */
    @get:Override
    var method: String? = "GET"

    /**
     * Sets any extra path information associated with the URL the client sent when it made this
     * request. The extra path information follows the servlet path but precedes the query string.
     *
     * @param pathInfo
     */
    @get:Override
    var pathInfo: String? = null

    /**
     * sets any extra path information after the servlet name but before the query string, translates to
     * a real path. Same as the value of the CGI variable PATH_TRANSLATED.
     *
     * @param pathTranslated
     */
    @get:Override
    var pathTranslated: String? = null

    /**
     * sets the portion of the request URI that indicates the context of the request. The context path
     * always comes first in a request URI. The path starts with a "/" character but does not end with a
     * "/" character.
     *
     * @param contextPath
     */
    @get:Override
    var contextPath: String? = ""
    private var queryString: String?

    /**
     * sets the login of the user making this request, if the user has been authenticated, or null if
     * the user has not been authenticated. Whether the user name is sent with each subsequent request
     * depends on the browser and type of authentication. Same as the value of the CGI variable
     * REMOTE_USER.
     *
     * @param remoteUser
     */
    @get:Override
    var remoteUser: String? = null

    /**
     * sets the session ID specified by the client. This may not be the same as the ID of the actual
     * session in use. For example, if the request specified an old (expired) session ID and the server
     * has started a new session, this method gets a new session with a new ID.
     *
     * @param requestedSessionId
     */
    @get:Override
    var requestedSessionId: String? = null
    // TODO when different ?
    /**
     * sets the part of this request's URL from the protocol name up to the query string in the first
     * line of the HTTP request. The web container does not decode this String.
     *
     * @param requestURI
     */
    @get:Override
    var servletPath: String?
        set
        @Override get() =// TODO when different ?
            field

    /**
     * set the Protocol (Default "http")
     *
     * @param protocol
     */
    @get:Override
    var protocol: String? = "HTTP/1.1"

    @get:Override
    val serverName: String? = "localhost"

    /**
     * @param port The port to set.
     */
    @get:Override
    var serverPort = 80

    @get:Override
    @set:Throws(UnsupportedEncodingException::class)
    @set:Override
    var characterEncoding: String? = "ISO-8859-1"

    /**
     * sets the content Type of the Request
     *
     * @param contentType
     */
    @get:Override
    var contentType: String? = null
    /**
     * @return the inputData
     */
    /**
     * @param inputData the inputData to set
     */
    var inputData: ByteArray? = ByteArray(0)

    companion object {
        private var DEFAULT_REMOTE: InetAddress? = null
        private var DEFAULT_REMOTE_ADDR: String? = null
        private var DEFAULT_REMOTE_HOST: String? = null
        fun clone(config: Config?, rootDirectory: Resource?, req: HttpServletRequest?): HttpServletRequestDummy? {
            var inputData: ByteArray? = null
            try {
                inputData = IOUtil.toBytes(req.getInputStream(), true, null)
            } catch (e: IOException) {
            }
            val dest = HttpServletRequestDummy(rootDirectory, req.getServerName(), req.getRequestURI(), req.getQueryString(),
                    HttpUtil.cloneCookies(config, req), HttpUtil.cloneHeaders(req), HttpUtil.cloneParameters(req), HttpUtil.getAttributesAsStruct(req), getSessionEL(req, false),
                    inputData)
            try {
                dest.characterEncoding = req.getCharacterEncoding()
            } catch (e: Exception) {
            }
            dest.remoteAddr = req.getRemoteAddr()
            dest.remoteHost = req.getRemoteHost()
            dest.authType = req.getAuthType()
            dest.contentType = req.getContentType()
            dest.contextPath = req.getContextPath()
            dest.locale = req.getLocale()
            dest.method = req.getMethod()
            dest.pathInfo = req.getPathInfo()
            dest.protocol = req.getProtocol()
            dest.requestedSessionId = req.getRequestedSessionId()
            dest.scheme = req.getScheme()
            dest.serverPort = req.getServerPort()
            dest.setSession(getSessionEL(req, false))
            return dest
        }

        private fun getSessionEL(req: HttpServletRequest?, createIfNecessary: Boolean): HttpSession? {
            try {
                return req.getSession(createIfNecessary)
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
            }
            return null
        }

        init {
            try {
                DEFAULT_REMOTE = InetAddress.getLocalHost()
                DEFAULT_REMOTE_ADDR = DEFAULT_REMOTE.getHostAddress()
                DEFAULT_REMOTE_HOST = DEFAULT_REMOTE.getHostName()
            } catch (e: UnknownHostException) {
            }
        }
    }

    // private InetAddress remoteq=DEFAULT_REMOTE;
    @get:Override
    var remoteAddr = DEFAULT_REMOTE_ADDR

    @get:Override
    var remoteHost = DEFAULT_REMOTE_HOST

    @get:Override
    var locale: Locale? = Locale.getDefault()

    @get:Override
    var isSecure = false
    private val contextRoot: Resource?

    @get:Override
    var scheme: String? = "http"
    private var session: HttpSession?

    /**
     * constructor of the class
     *
     * @throws PageException / public HttpServletRequestDummy(String serverName, String
     * scriptName,Struct queryString) throws PageException { this.serverName=serverName;
     * requestURI=scriptName;
     *
     * StringBuffer qs=new StringBuffer(); String[] keys=queryString.keys(); parameters=new
     * Item[keys.length]; String key; Object value; for(int i=0;i<keys.length></keys.length>;i++) { if(i>0)
     * qs.append('&'); key=keys[i]; value=queryString.get(key); parameters[i]=new
     * Item(key,value);
     *
     * qs.append(key); qs.append('='); qs.append(Caster.toString(value)); }
     *
     * this.queryString=qs.toString(); }
     */
    private fun translateQS(qs: String?): Array<Pair?>? {
        if (qs == null) return arrayOfNulls<Pair?>(0)
        val arr: Array = lucee.runtime.type.util.ListUtil.listToArrayRemoveEmpty(qs, "&")
        val parameters: Array<Pair?> = arrayOfNulls<Pair?>(arr.size())
        // Array item;
        var index: Int
        var name: String
        for (i in 1..parameters.size) {
            name = Caster.toString(arr.get(i, ""), "")
            index = name.indexOf('=')
            if (index != -1) parameters[i - 1] = Pair(name.substring(0, index), name.substring(index + 1)) else parameters[i - 1] = Pair(name, "")
        }
        return parameters
    }

    @Override
    fun getDateHeader(name: String?): Long {
        val value: Object? = getHeader(name)
        if (value != null) {
            val date: Date = DateCaster.toDateAdvanced(value, null, null)
            if (date != null) return date.getTime()
            throw IllegalArgumentException("can't convert value $value to a Date")
        }
        return -1
    }

    fun setDateHeader(name: String?, value: Long) {
        // TODO wrong format
        setHeader(name, DateTimeImpl(value, false).castToString())
    }

    @Override
    fun getHeader(name: String?): String? {
        return ReqRspUtil.get(headers, name)
    }

    /**
     * sets a new header value
     *
     * @param name name of the new value
     * @param value header value
     */
    fun setHeader(name: String?, value: String?) {
        headers = ReqRspUtil.set(headers, name, value)
    }

    /**
     * add a new header value
     *
     * @param name name of the new value
     * @param value header value
     */
    fun addHeader(name: String?, value: String?) {
        headers = ReqRspUtil.add(headers, name, value)
    }

    @Override
    fun getHeaders(name: String?): Enumeration? {
        val set = HashSet()
        for (i in headers.indices) {
            if (headers!![i].getName().equalsIgnoreCase(name)) set.add(Caster.toString(headers!![i].getValue(), null))
        }
        return EnumerationWrapper(set)
    }

    @get:Override
    val headerNames: Enumeration<String?>?
        get() {
            val set: HashSet<String?> = HashSet<String?>()
            for (i in headers.indices) {
                set.add(headers!![i].getName())
            }
            return EnumerationWrapper<String?>(set)
        }

    @Override
    fun getIntHeader(name: String?): Int {
        val value: Object? = getHeader(name)
        return if (value != null) {
            try {
                Caster.toIntValue(value)
            } catch (e: PageException) {
                throw NumberFormatException(e.getMessage())
            }
        } else -1
    }

    @Override
    fun getQueryString(): String? {
        return queryString
    }

    /**
     * sets the query string that is contained in the request URL after the path. Same as the value of
     * the CGI variable QUERY_STRING.
     *
     * @param queryString
     */
    fun setQueryString(queryString: String?) {
        this.queryString = queryString
        parameters = translateQS(queryString)
    }

    @Override
    fun isUserInRole(role: String?): Boolean {
        // TODO impl
        return false
    }

    // TODO impl
    @get:Override
    val userPrincipal: Principal?
        get() =// TODO impl
            null

    @get:Override
    val requestURL: StringBuffer?
        get() = StringBuffer(if (isSecure) "https" else "http").append("://").append(serverName).append(':').append(serverPort).append('/').append(servletPath)

    @Override
    fun getSession(arg0: Boolean): HttpSession? {
        return session
    }

    @Override
    fun getSession(): HttpSession? {
        return getSession(true)
    }

    // not supported
    @get:Override
    val isRequestedSessionIdValid: Boolean
        get() =// not supported
            false

    // not supported
    @get:Override
    val isRequestedSessionIdFromCookie: Boolean
        get() =// not supported
            false

    // not supported
    @get:Override
    val isRequestedSessionIdFromURL: Boolean
        get() =// not supported
            false

    @get:Override
    val isRequestedSessionIdFromUrl: Boolean
        get() = isRequestedSessionIdFromURL

    @Override
    fun getAttribute(key: String?): Object? {
        return attributes.get(key, null)
    }

    @Override
    fun setAttribute(key: String?, value: Object?) {
        attributes.setEL(key, value)
    }

    @Override
    fun removeAttribute(key: String?) {
        attributes.removeEL(KeyImpl.init(key))
    }

    @get:Override
    val attributeNames: Enumeration<String?>?
        get() = ItAsEnum.toStringEnumeration(attributes.keyIterator())

    @get:Override
    val contentLength: Int
        get() = if (inputData == null) -1 else inputData!!.size

    @get:Throws(IOException::class)
    @get:Override
    val inputStream: ServletInputStream?
        get() = ServletInputStreamDummy(inputData)

    fun setParameter(key: String?, value: String?) {
        parameters = ReqRspUtil.set(parameters, key, value)
        rewriteQS()
    }

    fun addParameter(key: String?, value: String?) {
        parameters = ReqRspUtil.add(parameters, key, value)
        rewriteQS()
    }

    @Override
    fun getParameter(key: String?): String? {
        return ReqRspUtil.get(parameters, key)
    }

    @Override
    fun getParameterValues(key: String?): Array<String?>? {
        val list: ArrayList<String?> = ArrayList<String?>()
        for (i in parameters.indices) {
            if (parameters!![i].getName().equalsIgnoreCase(key)) list.add(Caster.toString(parameters!![i].getValue(), null))
        }
        return list.toArray(arrayOfNulls<String?>(list.size()))
    }

    @get:Override
    val parameterNames: Enumeration<String?>?
        get() {
            val set: HashSet<String?> = HashSet<String?>()
            for (i in parameters.indices) {
                set.add(parameters!![i].getName())
            }
            return EnumerationWrapper<String?>(set)
        }

    @get:Override
    val parameterMap: Map?
        get() {
            val p: Map<String?, Object?> = MapFactory.< String, Object>getConcurrentMap<String?, Object?>()
            for (i in parameters.indices) {
                p.put(parameters!![i].getName(), parameters!![i].getValue())
            }
            return p
        }

    @get:Throws(IOException::class)
    @get:Override
    val reader: BufferedReader?
        get() = IOUtil.toBufferedReader(IOUtil.getReader(inputStream, CharsetUtil.ISO88591))

    fun setRemoteInetAddress(ia: InetAddress?) {
        remoteAddr = ia.getHostAddress()
        remoteHost = ia.getHostName()
    }

    @get:Override
    val locales: Enumeration<java.util.Locale?>?
        get() = EnumerationWrapper<Locale?>(Locale.getAvailableLocales())

    @Override
    fun getRequestDispatcher(arg0: String?): RequestDispatcher? {
        return RequestDispatcherDummy(this)
    }

    @Override
    fun getRealPath(path: String?): String? {
        return contextRoot.getReal(path)
    }

    private fun rewriteQS() {
        val qs = StringBuffer()
        var p: Pair<String?, Object?>?
        for (i in parameters.indices) {
            if (i > 0) qs.append('&')
            p = parameters!![i]
            qs.append(p.getName())
            qs.append('=')
            qs.append(Caster.toString(p.getValue(), ""))
        }
        queryString = qs.toString()
    }

    fun setSession(session: HttpSession?) {
        this.session = session
    }

    fun setAttributes(attributes: Struct?) {
        this.attributes = attributes
    }

    @get:Override
    val asyncContext: AsyncContext?
        get() {
            throw RuntimeException("not supported!")
        }

    @get:Override
    val contentLengthLong: Long
        get() = contentLength.toLong()

    @get:Override
    val dispatcherType: DispatcherType?
        get() {
            throw RuntimeException("not supported!")
        }

    @get:Override
    val localAddr: String?
        get() {
            throw RuntimeException("not supported!")
        }

    @get:Override
    val localName: String?
        get() {
            throw RuntimeException("not supported!")
        }

    @get:Override
    val localPort: Int
        get() {
            throw RuntimeException("not supported!")
        }

    @get:Override
    val remotePort: Int
        get() {
            throw RuntimeException("not supported!")
        }

    @get:Override
    val servletContext: ServletContext?
        get() {
            throw RuntimeException("not supported!")
        }

    @get:Override
    val isAsyncStarted: Boolean
        get() {
            throw RuntimeException("not supported!")
        }

    @get:Override
    val isAsyncSupported: Boolean
        get() {
            throw RuntimeException("not supported!")
        }

    @Override
    @Throws(IllegalStateException::class)
    fun startAsync(): AsyncContext? {
        throw RuntimeException("not supported!")
    }

    @Override
    @Throws(IllegalStateException::class)
    fun startAsync(arg0: ServletRequest?, arg1: ServletResponse?): AsyncContext? {
        throw RuntimeException("not supported!")
    }

    @Override
    @Throws(IOException::class, ServletException::class)
    fun authenticate(arg0: HttpServletResponse?): Boolean {
        throw RuntimeException("not supported!")
    }

    @Override
    fun changeSessionId(): String? {
        throw RuntimeException("not supported!")
    }

    @Override
    @Throws(IOException::class, ServletException::class)
    fun getPart(arg0: String?): Part? {
        throw RuntimeException("not supported!")
    }

    @get:Throws(IOException::class, ServletException::class)
    @get:Override
    val parts: Collection<Any?>?
        get() {
            throw RuntimeException("not supported!")
        }

    @Override
    @Throws(ServletException::class)
    fun login(arg0: String?, arg1: String?) {
        throw RuntimeException("not supported!")
    }

    @Override
    @Throws(ServletException::class)
    fun logout() {
        throw RuntimeException("not supported!")
    }

    @Override
    @Throws(IOException::class, ServletException::class)
    fun <T : HttpUpgradeHandler?> upgrade(arg0: Class<T?>?): T? {
        throw RuntimeException("not supported!")
    }

    /**
     * constructor of the class
     *
     * @param headers
     * @param parameters
     * @param httpSession
     * @param pairs
     * @param cookiess
     */
    init {
        this.serverName = serverName
        servletPath = scriptName
        this.queryString = queryString
        this.parameters = translateQS(queryString)
        this.contextRoot = contextRoot
        if (cookies != null) cookies = cookies
        if (headers != null) this.headers = headers
        if (parameters != null) this.parameters = parameters
        if (attributes != null) this.attributes = attributes
        this.session = session
        this.inputData = inputData
    }
}