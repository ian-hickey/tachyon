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
package lucee.runtime.util

import java.io.IOException

class HTTPUtilImpl private constructor() : lucee.runtime.util.HTTPUtil {
    /**
     * @see lucee.runtime.util.HTTPUtil.decode
     */
    @Override
    @Throws(UnsupportedEncodingException::class)
    fun decode(str: String?, charset: String?): String? {
        return URLDecoder.decode(str, charset, false)
    }

    /**
     * @see lucee.runtime.util.HTTPUtil.delete
     */
    @Override
    @Throws(IOException::class)
    fun delete(url: URL?, username: String?, password: String?, timeout: Int, charset: String?, useragent: String?, proxyserver: String?, proxyport: Int, proxyuser: String?,
               proxypassword: String?, headers: Array<Header?>?): HTTPResponse? {
        return HTTPEngine.delete(url, username, password, timeout, true, charset, useragent, ProxyDataImpl.getInstance(proxyserver, proxyport, proxyuser, proxypassword), headers)
    }

    /**
     * @param str
     * @param charset
     * @return
     * @throws UnsupportedEncodingException
     */
    @Override
    @Throws(UnsupportedEncodingException::class)
    fun encode(str: String?, charset: String?): String? {
        return URLEncoder.encode(str, charset)
    }

    /**
     * @see lucee.runtime.util.HTTPUtil.head
     */
    @Override
    @Throws(IOException::class)
    fun head(url: URL?, username: String?, password: String?, timeout: Int, charset: String?, useragent: String?, proxyserver: String?, proxyport: Int, proxyuser: String?,
             proxypassword: String?, headers: Array<Header?>?): HTTPResponse? {
        return HTTPEngine.head(url, username, password, timeout, true, charset, useragent, ProxyDataImpl.getInstance(proxyserver, proxyport, proxyuser, proxypassword), headers)
    }

    /**
     * @see lucee.runtime.util.HTTPUtil.get
     */
    @Override
    @Throws(IOException::class)
    operator fun get(url: URL?, username: String?, password: String?, timeout: Int, charset: String?, useragent: String?, proxyserver: String?, proxyport: Int, proxyuser: String?,
                     proxypassword: String?, headers: Array<Header?>?): HTTPResponse? {
        return HTTPEngine.get(url, username, password, timeout, true, charset, useragent, ProxyDataImpl.getInstance(proxyserver, proxyport, proxyuser, proxypassword), headers)
    }

    @Override
    @Throws(IOException::class)
    fun put(url: URL?, username: String?, password: String?, timeout: Int, charset: String?, useragent: String?, proxyserver: String?, proxyport: Int, proxyuser: String?,
            proxypassword: String?, headers: Array<Header?>?, body: Object?): HTTPResponse? {
        return put(url, username, proxypassword, timeout, null, charset, useragent, proxyserver, proxyport, proxyuser, proxypassword, headers, body)
    }

    @Override
    @Throws(IOException::class)
    fun put(url: URL?, username: String?, password: String?, timeout: Int, mimetype: String?, charset: String?, useragent: String?, proxyserver: String?, proxyport: Int,
            proxyuser: String?, proxypassword: String?, headers: Array<Header?>?, body: Object?): HTTPResponse? {
        return HTTPEngine.put(url, username, password, timeout, true, mimetype, charset, useragent, ProxyDataImpl.getInstance(proxyserver, proxyport, proxyuser, proxypassword),
                headers, body)
    }

    @Override
    @Throws(MalformedURLException::class)
    fun toURL(strUrl: String?, port: Int): URL? {
        return toURL(strUrl, port, true)
    }

    @Override
    @Throws(MalformedURLException::class)
    fun toURL(strUrl: String?, port: Int, encodeIfNecessary: Boolean): URL? {
        return lucee.commons.net.HTTPUtil.toURL(strUrl!!, port, if (encodeIfNecessary) lucee.commons.net.HTTPUtil.ENCODED_AUTO else lucee.commons.net.HTTPUtil.ENCODED_NO)
    }

    /**
     * @see lucee.commons.net.HTTPUtil.toURL
     */
    @Override
    @Throws(MalformedURLException::class)
    fun toURL(strUrl: String?): URL? {
        return lucee.commons.net.HTTPUtil.toURL(strUrl!!, lucee.commons.net.HTTPUtil.ENCODED_AUTO)
    }

    @Override
    @Throws(URISyntaxException::class)
    fun toURI(strUrl: String?): URI? {
        return lucee.commons.net.HTTPUtil.toURI(strUrl)
    }

    @Override
    @Throws(URISyntaxException::class)
    fun toURI(strUrl: String?, port: Int): URI? {
        return lucee.commons.net.HTTPUtil.toURI(strUrl, port)
    }

    @Override
    fun removeUnecessaryPort(url: URL?): URL? {
        return HTTPUtil().removeUnecessaryPort(url)
    }

    @Override
    fun createHeader(name: String?, value: String?): Header? {
        return HTTPEngine.header(name, value)
    }

    companion object {
        private val instance: lucee.runtime.util.HTTPUtil? = HTTPUtilImpl()
        fun getInstance(): lucee.runtime.util.HTTPUtil? {
            return instance
        }
    }
}