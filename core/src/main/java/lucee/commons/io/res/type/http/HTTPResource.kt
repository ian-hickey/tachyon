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
package lucee.commons.io.res.type.http

import java.io.IOException

class HTTPResource(provider: HTTPResourceProvider, data: HTTPConnectionData) : ReadOnlyResourceSupport() {
    private val provider: HTTPResourceProvider
    private val data: HTTPConnectionData
    private val path: String

    @get:Override
    val name: String
    private var http: HTTPResponse? = null
    @Throws(IOException::class)
    private fun getHTTPResponse(create: Boolean): HTTPResponse? {
        if (create || http == null) {
            // URL url = HTTPUtil.toURL("http://"+data.host+":"+data.port+"/"+data.path);
            val url = URL(provider.getProtocol(), data.host, data.port, data.path)
            // TODO Support for proxy
            val pd: ProxyData = if (ProxyDataImpl.isValid(data.proxyData, url.getHost())) data.proxyData else ProxyDataImpl.NO_PROXY
            http = HTTPEngine.get(url, data.username, data.password, _getTimeout(), true, null, data.userAgent, pd, null)
        }
        return http
    }

    @get:Throws(IOException::class)
    private val statusCode: Int
        private get() {
            if (http == null) {
                val url = URL(provider.getProtocol(), data.host, data.port, data.path)
                val pd: ProxyData = if (ProxyDataImpl.isValid(data.proxyData, url.getHost())) data.proxyData else ProxyDataImpl.NO_PROXY
                return HTTPEngine.head(url, data.username, data.password, _getTimeout(), true, null, data.userAgent, pd, null).getStatusCode()
            }
            return http.getStatusCode()
        }

    @get:Throws(IOException::class)
    val contentType: ContentType
        get() {
            if (http == null) {
                val url = URL(provider.getProtocol(), data.host, data.port, data.path)
                val pd: ProxyData = if (ProxyDataImpl.isValid(data.proxyData, url.getHost())) data.proxyData else ProxyDataImpl.NO_PROXY
                return HTTPEngine.head(url, data.username, data.password, _getTimeout(), true, null, data.userAgent, pd, null).getContentType()
            }
            return http.getContentType()
        }

    @Override
    fun exists(): Boolean {
        return try {
            provider.read(this)
            val code = statusCode // getHttpMethod().getStatusCode();
            code != 404
        } catch (e: IOException) {
            false
        }
    }

    fun statusCode(): Int {
        var rsp: HTTPResponse? = null
        return try {
            provider.read(this)
            getHTTPResponse(false).also { rsp = it }.getStatusCode()
        } catch (e: IOException) {
            0
        } finally {
            HTTPEngine.closeEL(rsp)
        }
    }// provider.unlock(this);

    // ResourceUtil.checkGetInputStreamOK(this);
    // provider.lock(this);
    @get:Throws(IOException::class)
    @get:Override
    val inputStream: InputStream
        get() {
            // ResourceUtil.checkGetInputStreamOK(this);
            // provider.lock(this);
            provider.read(this)
            val method: HTTPResponse? = getHTTPResponse(true)
            return try {
                IOUtil.toBufferedInputStream(method.getContentAsStream())
            } catch (e: IOException) {
                // provider.unlock(this);
                throw e
            } finally {
                HTTPEngine.closeEL(method)
            }
        }

    @get:Override
    val parent: String?
        get() = if (isRoot) null else provider.getProtocol().concat("://").concat(data.key()).concat(path.substring(0, path.length() - 1))
    private val isRoot: Boolean
        private get() = StringUtil.isEmpty(name)

    @get:Override
    val parentResource: Resource?
        get() = if (isRoot) null else HTTPResource(provider, HTTPConnectionData(data.username, data.password, data.host, data.port, path, data.proxyData, data.userAgent))

    @Override
    fun getPath(): String {
        return provider.getProtocol().concat("://").concat(data.key()).concat(path).concat(name)
    }

    @Override
    fun getRealResource(realpath: String): Resource? {
        var realpath = realpath
        realpath = ResourceUtil.merge(path.concat(name), realpath)
        return if (realpath.startsWith("../")) null else HTTPResource(provider, HTTPConnectionData(data.username, data.password, data.host, data.port, realpath, data.proxyData, data.userAgent))
    }

    @get:Override
    val resourceProvider: ResourceProvider
        get() = provider

    @get:Override
    val isAbsolute: Boolean
        get() = true

    @get:Override
    val isDirectory: Boolean
        get() = false

    @get:Override
    val isFile: Boolean
        get() = exists()

    @get:Override
    val isReadable: Boolean
        get() = exists()

    @Override
    fun lastModified(): Long {
        var last = 0
        var rsp: HTTPResponse? = null
        try {
            val cl: Header = getHTTPResponse(false).also { rsp = it }.getLastHeaderIgnoreCase("last-modified")
            if (cl != null && exists()) last = Caster.toIntValue(cl.getValue(), 0)
        } catch (e: IOException) {
        } finally {
            HTTPEngine.closeEL(rsp)
        }
        return last.toLong()
    }

    @Override
    fun length(): Long {
        var rsp: HTTPResponse? = null
        return try {
            if (!exists()) 0 else getHTTPResponse(false).also { rsp = it }.getContentLength()
        } catch (e: IOException) {
            0
        } finally {
            HTTPEngine.closeEL(rsp)
        }
    }

    @Override
    fun listResources(): Array<Resource>? {
        return null
    }

    fun setProxyData(pd: ProxyData?) {
        http = null
        data.setProxyData(pd)
    }

    fun setUserAgent(userAgent: String?) {
        http = null
        data.userAgent = userAgent
    }

    fun setTimeout(timeout: Int) {
        http = null
        data.timeout = timeout
    }

    private fun _getTimeout(): Int {
        return if (data.timeout < provider.getSocketTimeout()) data.timeout else provider.getSocketTimeout()
    }

    init {
        this.provider = provider
        this.data = data
        val pathName: Array<String> = ResourceUtil.translatePathName(data.path)
        path = pathName[0]
        name = pathName[1]
    }
}