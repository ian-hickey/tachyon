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
package tachyon.commons.net.http.httpclient

import java.io.File

object HTTPEngine4Impl {
    private var connMan: PoolingHttpClientConnectionManager? = null
    private var csfReg: Registry<ConnectionSocketFactory>? = null
    const val POOL_MAX_CONN = 500
    const val POOL_MAX_CONN_PER_ROUTE = 50
    const val POOL_CONN_TTL_MS = 15000
    const val POOL_CONN_INACTIVITY_DURATION = 300

    /**
     * does a http get request
     *
     * @param url
     * @param username
     * @param password
     * @param timeout
     * @param charset
     * @param useragent
     * @param proxyserver
     * @param proxyport
     * @param proxyuser
     * @param proxypassword
     * @param headers
     * @return
     * @throws IOException
     */
    @Throws(IOException::class)
    operator fun get(url: URL, username: String, password: String, timeout: Long, redirect: Boolean, charset: String, useragent: String, proxy: ProxyData,
                     headers: Array<tachyon.commons.net.http.Header>): HTTPResponse {
        val get = HttpGet(url.toExternalForm())
        return _invoke(url, get, username, password, timeout, redirect, charset, useragent, proxy, headers, null)
    }

    /**
     * does a http post request
     *
     * @param url
     * @param username
     * @param password
     * @param timeout
     * @param charset
     * @param useragent
     * @param proxyserver
     * @param proxyport
     * @param proxyuser
     * @param proxypassword
     * @param headers
     * @return
     * @throws IOException
     */
    @Throws(IOException::class)
    fun post(url: URL, username: String, password: String, timeout: Long, redirect: Boolean, charset: String, useragent: String, proxy: ProxyData,
             headers: Array<tachyon.commons.net.http.Header>): HTTPResponse {
        val post = HttpPost(url.toExternalForm())
        return _invoke(url, post, username, password, timeout, redirect, charset, useragent, proxy, headers, null)
    }

    @Throws(IOException::class)
    fun post(url: URL, username: String, password: String, timeout: Long, redirect: Boolean, charset: String, useragent: String, proxy: ProxyData,
             headers: Array<tachyon.commons.net.http.Header>, formfields: Map<String, String>?): HTTPResponse {
        val post = HttpPost(url.toExternalForm())
        return _invoke(url, post, username, password, timeout, redirect, charset, useragent, proxy, headers, formfields)
    }

    /**
     * does a http put request
     *
     * @param url
     * @param username
     * @param password
     * @param timeout
     * @param charset
     * @param useragent
     * @param proxyserver
     * @param proxyport
     * @param proxyuser
     * @param proxypassword
     * @param headers
     * @param body
     * @return
     * @throws IOException
     * @throws PageException
     */
    @Throws(IOException::class)
    fun put(url: URL, username: String, password: String, timeout: Long, redirect: Boolean, mimetype: String?, charset: String, useragent: String, proxy: ProxyData,
            headers: Array<tachyon.commons.net.http.Header>, body: Object?): HTTPResponse {
        val put = HttpPut(url.toExternalForm())
        setBody(put, body, mimetype, charset)
        return _invoke(url, put, username, password, timeout, redirect, charset, useragent, proxy, headers, null)
    }

    /**
     * does a http delete request
     *
     * @param url
     * @param username
     * @param password
     * @param timeout
     * @param charset
     * @param useragent
     * @param proxyserver
     * @param proxyport
     * @param proxyuser
     * @param proxypassword
     * @param headers
     * @return
     * @throws IOException
     */
    @Throws(IOException::class)
    fun delete(url: URL, username: String, password: String, timeout: Long, redirect: Boolean, charset: String, useragent: String, proxy: ProxyData,
               headers: Array<tachyon.commons.net.http.Header>): HTTPResponse {
        val delete = HttpDelete(url.toExternalForm())
        return _invoke(url, delete, username, password, timeout, redirect, charset, useragent, proxy, headers, null)
    }

    /**
     * does a http head request
     *
     * @param url
     * @param username
     * @param password
     * @param timeout
     * @param charset
     * @param useragent
     * @param proxyserver
     * @param proxyport
     * @param proxyuser
     * @param proxypassword
     * @param headers
     * @return
     * @throws IOException
     */
    @Throws(IOException::class)
    fun head(url: URL, username: String, password: String, timeout: Long, redirect: Boolean, charset: String, useragent: String, proxy: ProxyData,
             headers: Array<tachyon.commons.net.http.Header>): HTTPResponse {
        val head = HttpHead(url.toExternalForm())
        return _invoke(url, head, username, password, timeout, redirect, charset, useragent, proxy, headers, null)
    }

    fun header(name: String?, value: String?): tachyon.commons.net.http.Header {
        return HeaderImpl(name, value)
    }

    private fun toHeader(header: tachyon.commons.net.http.Header): Header {
        if (header is Header) return header as Header
        return if (header is HeaderWrap) (header as HeaderWrap).header else HeaderImpl(header.getName(), header.getValue())
    }

    val httpClientBuilder: HttpClientBuilder
        get() = HttpClients.custom()

    @Throws(PageException::class)
    fun setConnectionManager(builder: HttpClientBuilder) {
        setConnectionManager(builder, true)
    }

    @Throws(PageException::class)
    fun setConnectionManager(builder: HttpClientBuilder, pooling: Boolean) {
        try {
            initDefaultConnectionFactoryRegistry()
            if (!pooling) {
                val cm: HttpClientConnectionManager = BasicHttpClientConnectionManager(DefaultHttpClientConnectionOperatorImpl(csfReg), null)
                builder.setConnectionManager(cm)
                        .setConnectionManagerShared(false)
                return
            }
            if (connMan == null) {
                connMan = PoolingHttpClientConnectionManager(DefaultHttpClientConnectionOperatorImpl(csfReg), null, POOL_CONN_TTL_MS, TimeUnit.MILLISECONDS)
                connMan.setDefaultMaxPerRoute(POOL_MAX_CONN_PER_ROUTE)
                connMan.setMaxTotal(POOL_MAX_CONN)
                connMan.setDefaultSocketConfig(SocketConfig.copy(SocketConfig.DEFAULT).setTcpNoDelay(true).setSoReuseAddress(true).setSoLinger(0).build())
                connMan.setValidateAfterInactivity(POOL_CONN_INACTIVITY_DURATION)
            }
            builder.setConnectionManager(connMan)
                    .setConnectionManagerShared(true)
                    .setConnectionTimeToLive(POOL_CONN_TTL_MS, TimeUnit.MILLISECONDS)
                    .setConnectionReuseStrategy(DefaultClientConnectionReuseStrategy())
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
    }

    @Throws(java.security.GeneralSecurityException::class)
    private fun initDefaultConnectionFactoryRegistry() {
        if (csfReg == null) {
            /* Default TLS settings */
            val sslcontext: SSLContext = SSLContext.getInstance("TLS")
            sslcontext.init(null, null, SecureRandom())
            val defaultsslsf: SSLConnectionSocketFactory = SSLConnectionSocketFactoryImpl(sslcontext, DefaultHostnameVerifierImpl())
            /* Register connection handlers */csfReg = RegistryBuilder.< ConnectionSocketFactory > create < ConnectionSocketFactory ? > ()
                    .register("http", PlainConnectionSocketFactory.getSocketFactory())
                    .register("https", defaultsslsf)
                    .build()
        }
    }

    @Throws(PageException::class)
    fun setConnectionManager(builder: HttpClientBuilder, pooling: Boolean, clientCert: String?, clientCertPassword: String?) {
        var clientCertPassword = clientCertPassword
        try {
            if (StringUtil.isEmpty(clientCert)) {
                setConnectionManager(builder, pooling)
                return
            }
            // FIXME : create a clientCert Hashmap to allow reusable connexions with client_certs
            // Currently, clientCert force usePool to being ignored
            if (clientCertPassword == null) clientCertPassword = ""
            // Load the client cert
            val ksFile = File(clientCert)
            val clientStore: KeyStore = KeyStore.getInstance("PKCS12")
            clientStore.load(FileInputStream(ksFile), clientCertPassword.toCharArray())

            // Prepare the keys
            val kmf: KeyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
            kmf.init(clientStore, clientCertPassword.toCharArray())
            // Init SSL Context
            val sslcontext: SSLContext = SSLContext.getInstance("TLS")
            // Configure the socket factory
            sslcontext.init(kmf.getKeyManagers(), null, SecureRandom())
            val sslsf: SSLConnectionSocketFactory = SSLConnectionSocketFactoryImpl(sslcontext, DefaultHostnameVerifierImpl())
            // Fill in the registry
            val reg: Registry<ConnectionSocketFactory> = RegistryBuilder.< ConnectionSocketFactory > create < ConnectionSocketFactory ? > ()
                    .register("http", PlainConnectionSocketFactory.getSocketFactory())
                    .register("https", sslsf)
                    .build()
            // Provide a one off connection manager
            val cm: HttpClientConnectionManager = BasicHttpClientConnectionManager(DefaultHttpClientConnectionOperatorImpl(reg), null)
            builder.setConnectionManager(cm)
                    .setConnectionManagerShared(false)
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
    }

    fun releaseConnectionManager() {
        if (connMan != null) {
            connMan.close()
            connMan = null
        }
    }

    fun closeIdleConnections() {
        if (connMan != null) {
            connMan.closeIdleConnections(POOL_CONN_TTL_MS, TimeUnit.MILLISECONDS)
            connMan.closeExpiredConnections()
        }
    }

    @Throws(IOException::class)
    private fun _invoke(url: URL, request: HttpUriRequest, username: String, password: String, timeout: Long, redirect: Boolean, charset: String, useragent: String,
                        proxy: ProxyData, headers: Array<tachyon.commons.net.http.Header>, formfields: Map<String, String>?): HTTPResponse {
        var proxy: ProxyData = proxy
        proxy = ProxyDataImpl.validate(proxy, url.getHost())
        val builder: HttpClientBuilder = httpClientBuilder
        try {
            setConnectionManager(builder)
        } catch (e: PageException) {
            // Ignore pooling if an issue happens
        }

        // LDEV-2321
        builder.setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build())

        // redirect
        if (redirect) builder.setRedirectStrategy(DefaultRedirectStrategy.INSTANCE) else builder.disableRedirectHandling()
        val hh = HttpHost(url.getHost(), url.getPort())
        setHeader(request, headers)
        if (CollectionUtil.isEmpty(formfields)) setContentType(request, charset)
        setFormFields(request, formfields, charset)
        setUserAgent(request, useragent)
        if (timeout > 0) Http.setTimeout(builder, TimeSpanImpl.fromMillis(timeout))
        var context: HttpContext? = setCredentials(builder, hh, username, password, false)
        setProxy(url.getHost(), builder, request, proxy)
        val client: CloseableHttpClient = builder.build()
        if (context == null) context = BasicHttpContext()
        return HTTPResponse4Impl(url, context, request, client.execute(request, context))
    }

    @Throws(IOException::class)
    private fun setFormFields(request: HttpUriRequest, formfields: Map<String, String>?, charset: String) {
        var charset: String? = charset
        if (!CollectionUtil.isEmpty(formfields)) {
            if (request !is HttpPost) throw IOException("form fields are only suppported for post request")
            val post: HttpPost = request as HttpPost
            val list: List<NameValuePair> = ArrayList<NameValuePair>()
            val it: Iterator<Entry<String, String>> = formfields.entrySet().iterator()
            var e: Entry<String, String>
            while (it.hasNext()) {
                e = it.next()
                list.add(BasicNameValuePair(e.getKey(), e.getValue()))
            }
            if (StringUtil.isEmpty(charset)) charset = (ThreadLocalPageContext.get() as PageContextImpl).getWebCharset().name()
            post.setEntity(UrlEncodedFormEntity(list, charset))
        }
    }

    private fun setUserAgent(hm: HttpMessage, useragent: String?) {
        if (useragent != null) hm.setHeader("User-Agent", useragent)
    }

    private fun setContentType(hm: HttpMessage, charset: String?) {
        if (charset != null) hm.setHeader("Content-type", "text/html; charset=$charset")
    }

    private fun setHeader(hm: HttpMessage, headers: Array<tachyon.commons.net.http.Header>) {
        addHeader(hm, headers)
    }

    private fun addHeader(hm: HttpMessage, headers: Array<tachyon.commons.net.http.Header>?) {
        if (headers != null) {
            for (i in headers.indices) hm.addHeader(toHeader(headers[i]))
        }
    }

    fun setCredentials(builder: HttpClientBuilder, httpHost: HttpHost?, username: String?, password: String?, preAuth: Boolean): BasicHttpContext? {
        // set Username and Password
        var password = password
        if (!StringUtil.isEmpty(username, true)) {
            if (password == null) password = ""
            val cp: CredentialsProvider = BasicCredentialsProvider()
            builder.setDefaultCredentialsProvider(cp)
            cp.setCredentials(AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT), UsernamePasswordCredentials(username, password))
            val httpContext = BasicHttpContext()
            if (preAuth) {
                val authCache: AuthCache = BasicAuthCache()
                authCache.put(httpHost, BasicScheme())
                httpContext.setAttribute(ClientContext.AUTH_CACHE, authCache)
            }
            return httpContext
        }
        return null
    }

    fun setNTCredentials(builder: HttpClientBuilder, username: String?, password: String?, workStation: String?, domain: String?) {
        // set Username and Password
        var password = password
        if (!StringUtil.isEmpty(username, true)) {
            if (password == null) password = ""
            val cp: CredentialsProvider = BasicCredentialsProvider()
            builder.setDefaultCredentialsProvider(cp)
            cp.setCredentials(AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT), NTCredentials(username, password, workStation, domain))
        }
    }

    @Throws(IOException::class)
    fun setBody(req: HttpEntityEnclosingRequest, body: Object?, mimetype: String?, charset: String) {
        if (body != null) req.setEntity(toHttpEntity(body, mimetype, charset))
    }

    fun setProxy(host: String?, builder: HttpClientBuilder, request: HttpUriRequest?, proxy: ProxyData) {
        // set Proxy
        if (ProxyDataImpl.isValid(proxy, host)) {
            val hh = HttpHost(proxy.getServer(), if (proxy.getPort() === -1) 80 else proxy.getPort())
            builder.setProxy(hh)

            // username/password
            if (!StringUtil.isEmpty(proxy.getUsername())) {
                val cp: CredentialsProvider = BasicCredentialsProvider()
                builder.setDefaultCredentialsProvider(cp)
                cp.setCredentials(AuthScope(proxy.getServer(), proxy.getPort()), UsernamePasswordCredentials(proxy.getUsername(), proxy.getPassword()))
            }
        }
    }

    fun addCookie(cookieStore: CookieStore, domain: String?, name: String?, value: String?, path: String?, charset: String?) {
        var name = name
        var value = value
        if (ReqRspUtil.needEncoding(name, false)) name = ReqRspUtil.encode(name, charset)
        if (ReqRspUtil.needEncoding(value, false)) value = ReqRspUtil.encode(value, charset)
        val cookie = BasicClientCookie(name, value)
        if (!StringUtil.isEmpty(domain, true)) cookie.setDomain(domain)
        if (!StringUtil.isEmpty(path, true)) cookie.setPath(path)
        cookieStore.addCookie(cookie)
    }

    /**
     * convert input to HTTP Entity
     *
     * @param value
     * @param mimetype not used for binary input
     * @param charset not used for binary input
     * @return
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun toHttpEntity(value: Object, mimetype: String?, charset: String): HttpEntity {
        if (value is HttpEntity) return value as HttpEntity

        // content type
        var ct: ContentType = HTTPEngine.toContentType(mimetype, charset)
        return try {
            if (value is TemporaryStream) {
                if (ct != null) TemporaryStreamHttpEntity(value as TemporaryStream, ct) else TemporaryStreamHttpEntity(value as TemporaryStream, null)
            } else if (value is InputStream) {
                if (ct != null) ByteArrayEntity(IOUtil.toBytes(value as InputStream), ct) else ByteArrayEntity(IOUtil.toBytes(value as InputStream))
            } else if (Decision.isCastableToBinary(value, false)) {
                if (ct != null) ByteArrayEntity(Caster.toBinary(value), ct) else ByteArrayEntity(Caster.toBinary(value))
            } else {
                var wasNull = false
                if (ct == null) {
                    wasNull = true
                    ct = ContentType.APPLICATION_OCTET_STREAM
                }
                val str: String = Caster.toString(value)
                if (str.equals("<empty>")) {
                    return EmptyHttpEntity(ct)
                }
                if (wasNull && !StringUtil.isEmpty(charset, true)) StringEntity(str, charset.trim()) else StringEntity(str, ct)
            }
        } catch (e: Exception) {
            throw ExceptionUtil.toIOException(e)
        }
    }

    fun getEmptyEntity(contentType: ContentType?): Entity {
        return EmptyHttpEntity(contentType)
    }

    fun getByteArrayEntity(barr: ByteArray?, contentType: ContentType?): Entity {
        return ByteArrayHttpEntity(barr, contentType)
    }

    fun getTemporaryStreamEntity(ts: TemporaryStream?, contentType: ContentType?): Entity {
        return TemporaryStreamHttpEntity(ts, contentType)
    }

    fun getResourceEntity(res: Resource?, contentType: ContentType?): Entity {
        return ResourceHttpEntity(res, contentType)
    }
}