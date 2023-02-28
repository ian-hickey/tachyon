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
package tachyon.commons.net.http

import java.io.IOException

object HTTPEngine {
    // private static final boolean use4=true;
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

    @Throws(IOException::class)
    operator fun get(url: URL?): HTTPResponse {
        // if(use4)
        return HTTPEngine4Impl.get(url, null, null, -1, true, null, null, null, null)
        // return HTTPEngine3Impl.get(url, null, null, -1,MAX_REDIRECT, null, null, null, null);
    }

    @Throws(IOException::class)
    fun post(url: URL?): HTTPResponse {
        // if(use4)
        return HTTPEngine4Impl.post(url, null, null, -1, true, null, null, null, null)
        // return HTTPEngine3Impl.post(url, null, null, -1,MAX_REDIRECT, null, null, null, null,null);
    }

    @Throws(IOException::class)
    operator fun get(url: URL?, username: String?, password: String?, timeout: Long, followRedirect: Boolean, charset: String?, useragent: String?, proxy: ProxyData?,
                     headers: Array<Header?>?): HTTPResponse {
        // if(use4)
        return HTTPEngine4Impl.get(url, username, password, timeout, followRedirect, charset, useragent, proxy, headers)
        // return HTTPEngine3Impl.get(url, username, password, timeout, followRedirect?MAX_REDIRECT:0,
        // charset, useragent, proxy, headers);
    }

    @Throws(IOException::class)
    fun post(url: URL?, username: String?, password: String?, timeout: Long, followRedirect: Boolean, charset: String?, useragent: String?, proxy: ProxyData?,
             headers: Map<String, String>, params: Map<String?, String?>?): HTTPResponse {
        // if(use4)
        return HTTPEngine4Impl.post(url, username, password, timeout, followRedirect, charset, useragent, proxy, toHeaders(headers), params)
        // return HTTPEngine3Impl.post(url, username, password, timeout, followRedirect?MAX_REDIRECT:0,
        // charset, useragent, proxy, toHeaders(headers),params);
    }

    @Throws(IOException::class)
    fun head(url: URL?, username: String?, password: String?, timeout: Int, followRedirect: Boolean, charset: String?, useragent: String?, proxy: ProxyData?,
             headers: Array<Header?>?): HTTPResponse {
        // if(use4)
        return HTTPEngine4Impl.head(url, username, password, timeout, followRedirect, charset, useragent, proxy, headers)
        // return HTTPEngine3Impl.head(url, username, password, timeout, followRedirect?MAX_REDIRECT:0,
        // charset, useragent, proxy, headers);
    }

    @Throws(IOException::class)
    fun put(url: URL?, username: String?, password: String?, timeout: Int, followRedirect: Boolean, mimetype: String?, charset: String?, useragent: String?,
            proxy: ProxyData?, headers: Array<Header?>?, body: Object?): HTTPResponse {
        // if(use4)
        return HTTPEngine4Impl.put(url, username, password, timeout, followRedirect, mimetype, charset, useragent, proxy, headers, body)
        // return HTTPEngine3Impl.put(url, username, password, timeout, followRedirect?MAX_REDIRECT:0,
        // mimetype,charset, useragent, proxy, headers,body);
    }

    @Throws(IOException::class)
    fun delete(url: URL?, username: String?, password: String?, timeout: Int, followRedirect: Boolean, charset: String?, useragent: String?, proxy: ProxyData?,
               headers: Array<Header?>?): HTTPResponse {
        // if(use4)
        return HTTPEngine4Impl.delete(url, username, password, timeout, followRedirect, charset, useragent, proxy, headers)
        // return HTTPEngine3Impl.delete(url, username, password, timeout, followRedirect?MAX_REDIRECT:0,
        // charset, useragent, proxy, headers);
    }

    fun header(name: String?, value: String?): Header {
        // if(use4)
        return HTTPEngine4Impl.header(name, value)
        // return HTTPEngine3Impl.header(name, value);
    }

    fun getEmptyEntity(mimetype: String, charset: String): Entity {
        val ct: ContentType? = toContentType(mimetype, charset)
        // if(use4)
        return HTTPEngine4Impl.getEmptyEntity(ct)
        // return HTTPEngine3Impl.getEmptyEntity(ct==null?null:ct.toString());
    }

    fun getByteArrayEntity(barr: ByteArray?, mimetype: String, charset: String): Entity {
        val ct: ContentType? = toContentType(mimetype, charset)
        // if(use4)
        return HTTPEngine4Impl.getByteArrayEntity(barr, ct)
        // return HTTPEngine3Impl.getByteArrayEntity(barr,ct==null?null:ct.toString());
    }

    fun getTemporaryStreamEntity(ts: TemporaryStream?, mimetype: String, charset: String): Entity {
        val ct: ContentType? = toContentType(mimetype, charset)
        // if(use4)
        return HTTPEngine4Impl.getTemporaryStreamEntity(ts, ct)
        // return HTTPEngine3Impl.getTemporaryStreamEntity(ts,ct==null?null:ct.toString());
    }

    fun getResourceEntity(res: Resource?, mimetype: String, charset: String): Entity {
        val ct: ContentType? = toContentType(mimetype, charset)
        // if(use4)
        return HTTPEngine4Impl.getResourceEntity(res, ct)
        // return HTTPEngine3Impl.getResourceEntity(res,ct==null?null:ct.toString());
    }

    private fun toHeaders(headers: Map<String, String>): Array<Header?>? {
        if (CollectionUtil.isEmpty(headers)) return null
        val rtn: Array<Header?> = arrayOfNulls<Header>(headers.size())
        val it: Iterator<Entry<String, String>> = headers.entrySet().iterator()
        var e: Entry<String, String>
        var index = 0
        while (it.hasNext()) {
            e = it.next()
            rtn[index++] = HeaderImpl(e.getKey(), e.getValue())
        }
        return rtn
    }

    fun toContentType(mimetype: String, charset: String): ContentType? {
        var ct: ContentType? = null
        if (!StringUtil.isEmpty(mimetype, true)) {
            ct = if (!StringUtil.isEmpty(charset, true)) ContentType.create(mimetype.trim(), charset.trim()) else ContentType.create(mimetype.trim())
        }
        return ct
    }

    fun closeEL(rsp: HTTPResponse?) {
        /*
		 * DISBALED BECAUSE THIS SEEM TO CAUSE PROBLEM WITH MULTITHREADING, THIS NEEDS MORE INVESTIGATION
		 * if(rsp instanceof HTTPResponse4Impl) { try { ((HTTPResponse4Impl)rsp).close(); } catch (Exception
		 * e) {} }
		 */
    }
}