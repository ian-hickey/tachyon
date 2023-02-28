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

import java.io.IOException

class HTTPResponse4Impl(url: URL, context: HttpContext?, req: HttpUriRequest, rsp: HttpResponse) : HTTPResponseSupport(), HTTPResponse {
    var rsp: HttpResponse
    var req: HttpUriRequest
    private val url: URL
    private val context: HttpContext?

    @get:Throws(IOException::class)
    @get:Override
    val contentAsString: String
        get() = getContentAsString(null)

    @Override
    @Throws(IOException::class)
    fun getContentAsString(charset: String?): String {
        var charset = charset
        val entity: HttpEntity = rsp.getEntity()
        var `is`: InputStream? = null
        if (StringUtil.isEmpty(charset, true)) charset = getCharset()
        return try {
            IOUtil.toString(entity.getContent().also { `is` = it }, charset)
        } finally {
            IOUtil.close(`is`)
        }
    }

    @get:Throws(IOException::class)
    @get:Override
    val contentAsStream: InputStream?
        get() {
            val e: HttpEntity = rsp.getEntity() ?: return null
            return e.getContent()
        }

    @get:Throws(IOException::class)
    @get:Override
    val contentAsByteArray: ByteArray
        get() {
            val entity: HttpEntity = rsp.getEntity()
            var `is`: InputStream? = null
            return if (entity == null) ByteArray(0) else try {
                IOUtil.toBytes(entity.getContent().also { `is` = it })
            } finally {
                IOUtil.close(`is`)
            }
        }
    val locations: Array?
        get() {
            try {
                val locations: List<URI> = (context as HttpClientContext?).getRedirectLocations()
                if (locations != null) {
                    val arr: Array = ArrayImpl()
                    for (loc in locations) {
                        arr.appendEL(loc.toString())
                    }
                    return arr
                }
            } catch (e: Exception) {
            }
            return null
        }

    @Override
    fun getLastHeader(name: String?): Header? {
        val header: org.apache.http.Header = rsp.getLastHeader(name)
        return if (header != null) HeaderWrap(header) else null
    }

    @Override
    fun getLastHeaderIgnoreCase(name: String): Header? {
        return getLastHeaderIgnoreCase(rsp, name)
    }

    @get:Override
    val uRL: URL
        get() = try {
            req.getURI().toURL()
        } catch (e: MalformedURLException) {
            url
        }
    val targetURL: URL
        get() {
            val start: URL = uRL
            val req: HttpUriRequest = context.getAttribute(ExecutionContext.HTTP_REQUEST) as HttpUriRequest
            val uri: URI = req.getURI()
            var path: String = uri.getPath()
            val query: String = uri.getQuery()
            if (!StringUtil.isEmpty(query)) path += "?$query"
            var _url: URL = start
            try {
                _url = URL(start.getProtocol(), start.getHost(), start.getPort(), path)
            } catch (e: MalformedURLException) {
            }
            return _url
        }

    @get:Override
    val statusCode: Int
        get() = rsp.getStatusLine().getStatusCode()

    @get:Override
    val statusText: String
        get() = rsp.getStatusLine().getReasonPhrase()

    @get:Override
    val protocolVersion: String
        get() = rsp.getStatusLine().getProtocolVersion().toString()

    @get:Override
    val statusLine: String
        get() = rsp.getStatusLine().toString()

    @get:Override
    val allHeaders: Array<Any?>
        get() {
            val src: Array<org.apache.http.Header> = rsp.getAllHeaders() ?: return arrayOfNulls(0)
            val trg: Array<Header?> = arrayOfNulls<Header>(src.size)
            for (i in src.indices) {
                trg[i] = HeaderWrap(src[i])
            }
            return trg
        }

    @Throws(IOException::class)
    fun close() {
        /*
		 * if(rsp instanceof CloseableHttpResponse) { ((CloseableHttpResponse)rsp).close(); }
		 */
    }

    companion object {
        fun getLastHeaderIgnoreCase(rsp: HttpResponse, name: String): Header? {
            val header: org.apache.http.Header = rsp.getLastHeader(name)
            if (header != null) return HeaderWrap(header)
            val headers: Array<org.apache.http.Header> = rsp.getAllHeaders()
            for (i in headers.indices.reversed()) {
                if (name.equalsIgnoreCase(headers[i].getName())) {
                    return HeaderWrap(headers[i])
                }
            }
            return null
        }
    }

    init {
        this.url = url
        this.context = context
        this.req = req
        this.rsp = rsp
    }
}