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
package tachyon.runtime.net.http

import java.io.BufferedReader

/**
 * extends an existing [HttpServletRequest] with the possibility to reread the input as many
 * you want.
 */
class HTTPServletRequestWrap(req: HttpServletRequest?) : HttpServletRequest, Serializable {
    private var firstRead = true
    private var bytes: ByteArray?
    private var file: File? = null

    @get:Override
    var servletPath: String? = null

    @get:Override
    var requestURI: String? = null

    @get:Override
    var contextPath: String? = null

    @get:Override
    var pathInfo: String? = null

    @get:Override
    var queryString: String? = null
    private var disconnected = false
    private val req: HttpServletRequest?

    class DisconnectData {
        val attributes: Map<String?, Object?>? = null
        val authType: String? = null
        val cookies: Array<Cookie?>?
        val headers // this is a Pait List because there could by multiple entries with the same name
                : Map<Collection.Key?, LinkedList<String?>?>? = null
        val method: String? = null
        val pathTranslated: String? = null
        val remoteUser: String? = null
        val requestedSessionId: String? = null
        val requestedSessionIdFromCookie = false

        // private Request _request;
        val requestedSessionIdFromURL = false
        val secure = false
        val requestedSessionIdValid = false
        val characterEncoding: String? = null
        val contentLength = 0
        val contentType: String? = null
        val serverPort = 0
        val serverName: String? = null
        val scheme: String? = null
        val remoteHost: String? = null
        val remoteAddr: String? = null
        val protocol: String? = null
        val locale: Locale? = null
        val session: HttpSession? = null
        val userPrincipal: Principal? = null
    }

    var disconnectData: DisconnectData? = null
    private fun attrAsString(key: String?): String? {
        val res: Object = getAttribute(key) ?: return null
        return res.toString()
    }

    @get:Override
    val requestURL: StringBuffer?
        get() = if (String.valueOf(serverPort).equals("80") || String.valueOf(serverPort).equals("443")) {
            StringBuffer(if (isSecure) "https" else "http").append("://").append(serverName).append(if (requestURI.startsWith("/")) requestURI else "/" + requestURI)
        } else {
            StringBuffer(if (isSecure) "https" else "http").append("://").append(serverName).append(':').append(serverPort)
                    .append(if (requestURI.startsWith("/")) requestURI else "/" + requestURI)
        }

    @Override
    fun getRequestDispatcher(realpath: String?): RequestDispatcher? {
        return RequestDispatcherWrap(this, realpath)
    }

    fun getOriginalRequestDispatcher(realpath: String?): RequestDispatcher? {
        return if (disconnected) null else req.getRequestDispatcher(realpath)
    }

    @Override
    @Synchronized
    fun removeAttribute(name: String?) {
        if (disconnected) disconnectData!!.attributes.remove(name) else req.removeAttribute(name)
    }

    @Override
    @Synchronized
    fun setAttribute(name: String?, value: Object?) {
        if (disconnected) disconnectData!!.attributes.put(name, value) else req.setAttribute(name, value)
    }

    @Override
    @Synchronized
    fun getAttribute(name: String?): Object? {
        return if (disconnected) disconnectData!!.attributes!![name] else req.getAttribute(name)
    }

    @get:Synchronized
    @get:Override
    val attributeNames: Enumeration?
        get() = if (disconnected) {
            EnumerationWrapper(disconnectData!!.attributes.keySet().toArray())
        } else req.getAttributeNames()

    // throw new IllegalStateException();
    // keep the content in memory
    @get:Throws(IOException::class)
    @get:Override
    val inputStream: ServletInputStream?
        get() {
            if (bytes == null && file == null) {
                if (!firstRead) {
                    if (bytes != null) return ServletInputStreamDummy(bytes)
                    if (file != null) return ServletInputStreamDummy(file)
                    val pc: PageContext = ThreadLocalPageContext.get()
                    return if (pc != null) pc.formScope().getInputStream() else ServletInputStreamDummy(byteArrayOf())
                    // throw new IllegalStateException();
                }
                firstRead = false
                // keep the content in memory
                storeEL()
            }
            if (file != null) return ServletInputStreamDummy(file)
            return if (bytes != null) ServletInputStreamDummy(bytes) else ServletInputStreamDummy(byteArrayOf())
        }

    private fun storeEL() {
        var `is`: ServletInputStream? = null
        val maxReached: RefBoolean = RefBooleanImpl()
        try {
            run {
                try {
                    `is` = req.getInputStream()
                    bytes = IOUtil.toBytesMax(`is`, MAX_MEMORY_SIZE, maxReached)
                    if (!maxReached.toBooleanValue()) {
                        return
                    }
                } catch (e: Exception) {
                }
            }
            var fos: FileOutputStream? = null
            try {
                file = File.createTempFile("upload", ".tmp")
                fos = FileOutputStream(file)
                // first we store what we did already load
                if (maxReached.toBooleanValue()) {
                    IOUtil.copy(ByteArrayInputStream(bytes), fos, true, false)
                    bytes = null
                }
                if (`is` == null) `is` = req.getInputStream()
                // now we store the rest
                IOUtil.copy(`is`, fos, 0xfffff, true, true)
                file.deleteOnExit()
            } catch (e: Exception) {
            } finally {
                IOUtil.closeEL(fos)
            }
        } finally {
            IOUtil.closeEL(`is`)
        }
    }

    @get:Override
    val parameterMap: Map<String?, Array<String?>?>?
        get() {
            val pc: PageContext = ThreadLocalPageContext.get()
            val form: FormImpl? = _form(pc)
            val url: URLImpl? = _url(pc)
            return ScopeUtil.getParameterMap(arrayOf<Array<URLItem?>?>(form.getRaw(), url.getRaw()), arrayOf<String?>(form.getEncoding(), url.getEncoding()))
        }

    @Override
    fun getParameter(name: String?): String? {
        if (!disconnected) {
            val `val`: String = req.getParameter(name)
            if (`val` != null) return `val`
        }
        val values = getParameterValues(name)
        return if (ArrayUtil.isEmpty(values)) null else values!![0]
    }

    @get:Override
    val parameterNames: Enumeration<String?>?
        get() = ItasEnum<String?>(parameterMap.keySet().iterator())

    @Override
    fun getParameterValues(name: String?): Array<String?>? {
        return getParameterValues(ThreadLocalPageContext.get(), name)
    }

    @get:Throws(IOException::class)
    @get:Override
    val reader: BufferedReader?
        get() {
            val strEnc = characterEncoding
            var enc: Charset? = null
            if (StringUtil.isEmpty(strEnc)) enc = CharsetUtil.ISO88591 else CharsetUtil.toCharset(strEnc)
            return IOUtil.toBufferedReader(IOUtil.getReader(inputStream, enc))
        }
    val originalRequest: HttpServletRequest?
        get() = if (disconnected) null else req

    @Synchronized
    fun disconnect(pc: PageContextImpl?) {
        if (disconnected) return
        disconnectData = DisconnectData()

        // attributes
        run {
            val it: Iterator<String?> = ListUtil.toIterator(req.getAttributeNames())
            disconnectData!!.attributes = MapFactory.getConcurrentMap()
            var k: String?
            while (it.hasNext()) {
                k = it.next()
                if (!StringUtil.isEmpty(k)) disconnectData!!.attributes.put(k, req.getAttribute(k))
            }
        }

        // headers
        run {
            val headerNames: Enumeration<String?> = req.getHeaderNames()
            disconnectData!!.headers = MapFactory.getConcurrentMap() // new ConcurrentHashMap<Collection.Key, LinkedList<String>>();
            var k: String
            var e: Enumeration<String?>
            while (headerNames.hasMoreElements()) {
                k = headerNames.nextElement().toString()
                e = req.getHeaders(k)
                val list: LinkedList<String?> = LinkedList<String?>()
                while (e.hasMoreElements()) {
                    list.add(e.nextElement().toString())
                }
                if (!StringUtil.isEmpty(k)) disconnectData!!.headers.put(KeyImpl.init(k), list)
            }
        }

        // cookies
        run {
            val _cookies: Array<Cookie?> = req.getCookies()
            if (!ArrayUtil.isEmpty(_cookies)) {
                disconnectData!!.cookies = arrayOfNulls<Cookie?>(_cookies.size)
                for (i in _cookies.indices) disconnectData!!.cookies!![i] = _cookies[i]
            } else disconnectData!!.cookies = arrayOfNulls<Cookie?>(0)
        }
        disconnectData!!.authType = req.getAuthType()
        disconnectData!!.method = req.getMethod()
        disconnectData!!.pathTranslated = req.getPathTranslated()
        disconnectData!!.remoteUser = req.getRemoteUser()
        disconnectData!!.requestedSessionId = req.getRequestedSessionId()
        disconnectData!!.requestedSessionIdFromCookie = req.isRequestedSessionIdFromCookie()
        disconnectData!!.requestedSessionIdFromURL = req.isRequestedSessionIdFromURL()
        disconnectData!!.secure = req.isSecure()
        disconnectData!!.requestedSessionIdValid = req.isRequestedSessionIdValid()
        disconnectData!!.characterEncoding = req.getCharacterEncoding()
        disconnectData!!.contentLength = req.getContentLength()
        disconnectData!!.contentType = req.getContentType()
        disconnectData!!.serverPort = req.getServerPort()
        disconnectData!!.serverName = req.getServerName()
        disconnectData!!.scheme = req.getScheme()
        disconnectData!!.remoteHost = req.getRemoteHost()
        disconnectData!!.remoteAddr = req.getRemoteAddr()
        disconnectData!!.protocol = req.getProtocol()
        disconnectData!!.locale = req.getLocale()
        // only store it when j2ee sessions are enabled
        if (pc.getSessionType() === Config.SESSION_TYPE_JEE) disconnectData!!.session = req.getSession(true) // create if necessary
        disconnectData!!.userPrincipal = req.getUserPrincipal()
        if (bytes == null || file == null) {
            storeEL()
        }
        disconnected = true
        // req=null;
    }

    internal class ArrayEnum<E> : Enumeration<E?> {
        @Override
        fun hasMoreElements(): Boolean {
            return false
        }

        @Override
        fun nextElement(): E? {
            return null
        }
    }

    internal class ItasEnum<E>(private val it: Iterator<E?>?) : Enumeration<E?> {
        @Override
        fun hasMoreElements(): Boolean {
            return it!!.hasNext()
        }

        @Override
        fun nextElement(): E? {
            return it!!.next()
        }
    }

    internal class EmptyEnum<E> : Enumeration<E?> {
        @Override
        fun hasMoreElements(): Boolean {
            return false
        }

        @Override
        fun nextElement(): E? {
            return null
        }
    }

    internal class StringItasEnum(private val it: Iterator<*>?) : Enumeration<String?> {
        @Override
        fun hasMoreElements(): Boolean {
            return it!!.hasNext()
        }

        @Override
        fun nextElement(): String? {
            return StringUtil.toStringNative(it!!.next(), "")
        }
    }

    @get:Override
    val authType: String?
        get() = if (disconnected) disconnectData!!.authType else req.getAuthType()

    @get:Override
    val cookies: Array<Any?>?
        get() = if (disconnected) disconnectData!!.cookies else req.getCookies()

    @Override
    fun getDateHeader(name: String?): Long {
        if (!disconnected) return req.getDateHeader(name)
        val h = getHeader(name) ?: return -1
        val dt: DateTime = DateCaster.toDateAdvanced(h, null, null)
                ?: throw IllegalArgumentException("cannot convert [" + getHeader(name) + "] to date time value")
        return dt.getTime()
    }

    @Override
    fun getIntHeader(name: String?): Int {
        if (!disconnected) return req.getIntHeader(name)
        val h = getHeader(name) ?: return -1
        val i: Integer = Caster.toInteger(h, null)
                ?: throw NumberFormatException("cannot convert [" + getHeader(name) + "] to int value")
        return i.intValue()
    }

    @Override
    fun getHeader(name: String?): String? {
        if (!disconnected) return req.getHeader(name)
        val value: LinkedList<String?> = disconnectData!!.headers!![KeyImpl.init(name)] ?: return null
        return value.getFirst()
    }

    @get:Override
    val headerNames: Enumeration?
        get() {
            if (!disconnected) return req.getHeaderNames()
            val set: Set<Key?> = disconnectData!!.headers.keySet()
            return StringIterator(set.toArray(arrayOfNulls<Key?>(set.size())))
        }

    @Override
    fun getHeaders(name: String?): Enumeration? {
        if (!disconnected) return req.getHeaders(name)
        val value: LinkedList<String?>? = disconnectData!!.headers!![KeyImpl.init(name)]
        return if (value != null) ItasEnum<String?>(value.iterator()) else EmptyEnum<String?>()
    }

    @get:Override
    val method: String?
        get() = if (!disconnected) req.getMethod() else disconnectData!!.method

    @get:Override
    val pathTranslated: String?
        get() = if (!disconnected) req.getPathTranslated() else disconnectData!!.pathTranslated

    @get:Override
    val remoteUser: String?
        get() = if (!disconnected) req.getRemoteUser() else disconnectData!!.remoteUser

    @get:Override
    val requestedSessionId: String?
        get() = if (!disconnected) req.getRequestedSessionId() else disconnectData!!.requestedSessionId

    @get:Override
    val session: HttpSession?
        get() = getSession(true)

    @Override
    fun getSession(create: Boolean): HttpSession? {
        return if (!disconnected) req.getSession(create) else disconnectData!!.session
    }

    @get:Override
    val userPrincipal: Principal?
        get() = if (!disconnected) req.getUserPrincipal() else disconnectData!!.userPrincipal

    @get:Override
    val isRequestedSessionIdFromCookie: Boolean
        get() = if (!disconnected) req.isRequestedSessionIdFromCookie() else disconnectData!!.requestedSessionIdFromCookie

    @get:Override
    val isRequestedSessionIdFromURL: Boolean
        get() = if (!disconnected) req.isRequestedSessionIdFromURL() else disconnectData!!.requestedSessionIdFromURL

    @get:Override
    val isRequestedSessionIdFromUrl: Boolean
        get() = isRequestedSessionIdFromURL

    @get:Override
    val isRequestedSessionIdValid: Boolean
        get() = if (!disconnected) req.isRequestedSessionIdValid() else disconnectData!!.requestedSessionIdValid

    @get:Override
    @set:Throws(UnsupportedEncodingException::class)
    @set:Override
    var characterEncoding: String?
        get() = if (!disconnected) req.getCharacterEncoding() else disconnectData!!.characterEncoding
        set(enc) {
            if (!disconnected) req.setCharacterEncoding(enc) else disconnectData!!.characterEncoding = enc
        }

    @get:Override
    val contentLength: Int
        get() = if (!disconnected) req.getContentLength() else disconnectData!!.contentLength

    @get:Override
    val contentType: String?
        get() = if (!disconnected) req.getContentType() else disconnectData!!.contentType

    @get:Override
    val locale: Locale?
        get() = if (!disconnected) req.getLocale() else disconnectData!!.locale

    @Override
    fun isUserInRole(role: String?): Boolean {
        if (!disconnected) return req.isUserInRole(role)
        // try it anyway, in some servlet engine it is still working
        try {
            return req.isUserInRole(role)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
        }
        throw RuntimeException("this method is not supported when root request is gone")
    }

    // try it anyway, in some servlet engine it is still working
    @get:Override
    val locales: Enumeration?
        get() {
            if (!disconnected) return req.getLocales()
            // try it anyway, in some servlet engine it is still working
            try {
                return req.getLocales()
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
            }
            throw RuntimeException("this method is not supported when root request is gone")
        }

    @Override
    fun getRealPath(path: String?): String? {
        if (!disconnected) return req.getRealPath(path)
        // try it anyway, in some servlet engine it is still working
        try {
            return req.getRealPath(path)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
        }
        throw RuntimeException("this method is not supported when root request is gone")
    }

    @get:Override
    val protocol: String?
        get() = if (!disconnected) req.getProtocol() else disconnectData!!.protocol

    @get:Override
    val remoteAddr: String?
        get() = if (!disconnected) req.getRemoteAddr() else disconnectData!!.remoteAddr

    @get:Override
    val remoteHost: String?
        get() = if (!disconnected) req.getRemoteHost() else disconnectData!!.remoteHost

    @get:Override
    val scheme: String?
        get() = if (!disconnected) req.getScheme() else disconnectData!!.scheme

    @get:Override
    val serverName: String?
        get() = if (!disconnected) req.getServerName() else disconnectData!!.serverName

    @get:Override
    val serverPort: Int
        get() = if (!disconnected) req.getServerPort() else disconnectData!!.serverPort

    @get:Override
    val isSecure: Boolean
        get() = if (!disconnected) req.isSecure() else disconnectData!!.secure

    @get:Override
    val asyncContext: AsyncContext?
        get() {
            if (!disconnected) return req.getAsyncContext()
            throw RuntimeException("not supported!")
        }

    @get:Override
    val contentLengthLong: Long
        get() = if (!disconnected) req.getContentLengthLong() else contentLength.toLong()

    @get:Override
    val dispatcherType: DispatcherType?
        get() {
            if (!disconnected) return req.getDispatcherType()
            throw RuntimeException("not supported!")
        }

    @get:Override
    val localAddr: String?
        get() {
            if (!disconnected) return req.getLocalAddr()
            throw RuntimeException("not supported!")
        }

    @get:Override
    val localName: String?
        get() {
            if (!disconnected) return req.getLocalName()
            throw RuntimeException("not supported!")
        }

    @get:Override
    val localPort: Int
        get() {
            if (!disconnected) return req.getLocalPort()
            throw RuntimeException("not supported!")
        }

    @get:Override
    val remotePort: Int
        get() {
            if (!disconnected) return req.getRemotePort()
            throw RuntimeException("not supported!")
        }

    @get:Override
    val servletContext: ServletContext?
        get() {
            if (!disconnected) return req.getServletContext()
            throw RuntimeException("not supported!")
        }

    @get:Override
    val isAsyncStarted: Boolean
        get() {
            if (!disconnected) return req.isAsyncStarted()
            throw RuntimeException("not supported!")
        }

    @get:Override
    val isAsyncSupported: Boolean
        get() {
            if (!disconnected) return req.isAsyncSupported()
            throw RuntimeException("not supported!")
        }

    @Override
    @Throws(IllegalStateException::class)
    fun startAsync(): AsyncContext? {
        if (!disconnected) return req.startAsync()
        throw RuntimeException("not supported!")
    }

    @Override
    @Throws(IllegalStateException::class)
    fun startAsync(arg0: ServletRequest?, arg1: ServletResponse?): AsyncContext? {
        if (!disconnected) return req.startAsync(arg0, arg1)
        throw RuntimeException("not supported!")
    }

    @Override
    @Throws(IOException::class, ServletException::class)
    fun authenticate(arg0: HttpServletResponse?): Boolean {
        if (!disconnected) return req.authenticate(arg0)
        throw RuntimeException("not supported!")
    }

    @Override
    fun changeSessionId(): String? {
        if (!disconnected) return req.changeSessionId()
        throw RuntimeException("not supported!")
    }

    @Override
    @Throws(IOException::class, ServletException::class)
    fun getPart(arg0: String?): Part? {
        if (!disconnected) return req.getPart(arg0)
        throw RuntimeException("not supported!")
    }

    @get:Throws(IOException::class, ServletException::class)
    @get:Override
    val parts: Collection<Any?>?
        get() {
            if (!disconnected) return req.getParts()
            throw RuntimeException("not supported!")
        }

    @Override
    @Throws(ServletException::class)
    fun login(arg0: String?, arg1: String?) {
        if (!disconnected) req.login(arg0, arg1)
        throw RuntimeException("not supported!")
    }

    @Override
    @Throws(ServletException::class)
    fun logout() {
        if (!disconnected) req.logout()
        throw RuntimeException("not supported!")
    }

    @Override
    @Throws(IOException::class, ServletException::class)
    fun <T : HttpUpgradeHandler?> upgrade(arg0: Class<T?>?): T? {
        if (!disconnected) return req.upgrade(arg0)
        throw RuntimeException("not supported!")
    }

    fun close() {
        if (file != null) {
            if (!file.delete()) file.deleteOnExit()
            file = null
        }
        bytes = null
    }

    companion object {
        private const val serialVersionUID = 7286638632320246809L
        private const val MAX_MEMORY_SIZE = 1024 * 1024
        fun pure(req: HttpServletRequest?): HttpServletRequest? {
            var req: HttpServletRequest? = req
            var req2: HttpServletRequest?
            while (req is HTTPServletRequestWrap) {
                req2 = (req as HTTPServletRequestWrap?)!!.originalRequest
                if (req2 === req) break
                req = req2
            }
            return req
        }

        private fun _url(pc: PageContext?): URLImpl? {
            val u: URL = pc.urlScope()
            return if (u is UrlFormImpl) {
                (u as UrlFormImpl).getURL()
            } else u as URLImpl
        }

        private fun _form(pc: PageContext?): FormImpl? {
            val f: Form = pc.formScope()
            return if (f is UrlFormImpl) {
                (f as UrlFormImpl).getForm()
            } else f as FormImpl
        }

        fun getParameterValues(pc: PageContext?, name: String?): Array<String?>? {
            var pc: PageContext? = pc
            pc = ThreadLocalPageContext.get(pc)
            val form: FormImpl? = _form(pc)
            val url: URLImpl? = _url(pc)
            return ScopeUtil.getParameterValues(arrayOf<Array<URLItem?>?>(form.getRaw(), url.getRaw()), arrayOf<String?>(form.getEncoding(), url.getEncoding()), name)
        }
    }

    /**
     * Constructor of the class
     *
     * @param req
     * @param max how many is possible to re read
     */
    init {
        this.req = pure(req)
        if (attrAsString("javax.servlet.include.servlet_path").also { servletPath = it } != null) {
            requestURI = attrAsString("javax.servlet.include.request_uri")
            contextPath = attrAsString("javax.servlet.include.context_path")
            pathInfo = attrAsString("javax.servlet.include.path_info")
            queryString = attrAsString("javax.servlet.include.query_string")
        } else {
            servletPath = req.getServletPath()
            requestURI = req.getRequestURI()
            contextPath = req.getContextPath()
            pathInfo = req.getPathInfo()
            queryString = req.getQueryString()
        }
    }
}