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
package tachyon.runtime.util

import java.io.IOException

interface HTTPUtil {
    // private static final String NO_MIMETYPE="Unable to determine MIME type of file.";
    /**
     * make a http request to given url
     *
     * @param url url
     * @param username username
     * @param password password
     * @param timeout timeoute
     * @param charset charset
     * @param useragent user agent
     * @param proxyserver proxy server
     * @param proxyport proxy port
     * @param proxyuser proxy user
     * @param proxypassword proxy password
     * @param headers headers
     * @return resulting inputstream
     * @throws IOException IO Exception
     */
    @Throws(IOException::class)
    operator fun get(url: URL?, username: String?, password: String?, timeout: Int, charset: String?, useragent: String?, proxyserver: String?, proxyport: Int, proxyuser: String?,
                     proxypassword: String?, headers: Array<Header?>?): HTTPResponse?

    /**
     *
     * @param url url
     * @param username username
     * @param password password
     * @param timeout timeout
     * @param charset charset
     * @param useragent user agent
     * @param proxyserver proxy server
     * @param proxyport proxy port
     * @param proxyuser proxy user
     * @param proxypassword proxy password
     * @param headers headers
     * @param body body
     * @return resulting inputstream
     * @throws IOException IO Exception
     * @see .put
     */
    @Deprecated
    @Deprecated("""use instead
	  """)
    @Throws(IOException::class)
    fun put(url: URL?, username: String?, password: String?, timeout: Int, charset: String?, useragent: String?, proxyserver: String?, proxyport: Int, proxyuser: String?,
            proxypassword: String?, headers: Array<Header?>?, body: Object?): HTTPResponse?

    @Throws(IOException::class)
    fun put(url: URL?, username: String?, password: String?, timeout: Int, mimetype: String?, charset: String?, useragent: String?, proxyserver: String?, proxyport: Int,
            proxyuser: String?, proxypassword: String?, headers: Array<Header?>?, body: Object?): HTTPResponse?

    @Throws(IOException::class)
    fun delete(url: URL?, username: String?, password: String?, timeout: Int, charset: String?, useragent: String?, proxyserver: String?, proxyport: Int, proxyuser: String?,
               proxypassword: String?, headers: Array<Header?>?): HTTPResponse?

    @Throws(IOException::class)
    fun head(url: URL?, username: String?, password: String?, timeout: Int, charset: String?, useragent: String?, proxyserver: String?, proxyport: Int, proxyuser: String?,
             proxypassword: String?, headers: Array<Header?>?): HTTPResponse?

    /**
     * cast a string to a url
     *
     * @param strUrl url
     * @param port port
     * @return url from string
     * @throws MalformedURLException Malformed URL Exception
     * @see .toURL
     */
    @Deprecated
    @Deprecated("""use instead
	  """)
    @Throws(MalformedURLException::class)
    fun toURL(strUrl: String?, port: Int): URL?

    /**
     *
     * @param strUrl url
     * @param port port
     * @param encodeIfNecessary encode I fNecessary
     * @return URL generated
     * @throws MalformedURLException Malformed URL Exception
     */
    @Throws(MalformedURLException::class)
    fun toURL(strUrl: String?, port: Int, encodeIfNecessary: Boolean): URL?

    /**
     * cast a string to a url
     *
     * @param strUrl string represent a url
     * @return url from string
     * @throws MalformedURLException Malformed URL Exception
     */
    @Throws(MalformedURLException::class)
    fun toURL(strUrl: String?): URL?

    @Throws(URISyntaxException::class)
    fun toURI(strUrl: String?): URI?

    @Throws(URISyntaxException::class)
    fun toURI(strUrl: String?, port: Int): URI?

    /**
     * translate a string in the URLEncoded Format
     *
     * @param str String to translate
     * @param charset charset used for translation
     * @return encoded String
     * @throws UnsupportedEncodingException Unsupported Encoding Exception
     */
    @Throws(UnsupportedEncodingException::class)
    fun encode(str: String?, charset: String?): String?

    /**
     * translate a url encoded string to a regular string
     *
     * @param str encoded string
     * @param charset charset used
     * @return raw string
     * @throws UnsupportedEncodingException Unsupported Encoding Exception
     */
    @Throws(UnsupportedEncodingException::class)
    fun decode(str: String?, charset: String?): String?

    /**
     * remove port information if the port is the default port for this protocol (http=80,https=443)
     *
     * @param url url
     * @return Returns a Url.
     */
    fun removeUnecessaryPort(url: URL?): URL?
    fun createHeader(name: String?, value: String?): Header?

    companion object {
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
    }
}