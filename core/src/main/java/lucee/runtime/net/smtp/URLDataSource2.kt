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
package lucee.runtime.net.smtp

import java.io.ByteArrayInputStream

class URLDataSource2(url: URL?) : DataSource {
    private val url: URL?
    private var barr: ByteArray?

    /**
     * Returns the value of the URL content-type header field
     *
     */
    @get:Override
    val contentType: String?
        get() {
            var connection: URLConnection? = null
            try {
                connection = url.openConnection()
            } catch (e: IOException) {
            }
            return if (connection == null) DEFAULT_CONTENT_TYPE else connection.getContentType()
        }

    /**
     * Returns the file name of the URL object
     */
    @get:Override
    val name: String?
        get() = url.getFile()

    /**
     * Returns an InputStream obtained from the data source
     */
    @get:Throws(IOException::class)
    @get:Override
    val inputStream: InputStream?
        get() {
            if (barr == null) {
                barr = IOUtil.toBytes(url.openStream())
            }
            return ByteArrayInputStream(barr)
        }// is it necessary?

    /**
     * Returns an OutputStream obtained from the data source
     */
    @get:Throws(IOException::class)
    @get:Override
    val outputStream: OutputStream?
        get() {
            val connection: URLConnection = url.openConnection() ?: return null
            connection.setDoOutput(true) // is it necessary?
            return connection.getOutputStream()
        }

    /**
     * Returns the URL of the data source
     */
    val uRL: URL?
        get() = url

    companion object {
        private val DEFAULT_CONTENT_TYPE: String? = "application/octet-stream"
    }

    /**
     * Creates a URLDataSource from a URL object
     */
    init {
        this.url = url
    }
}