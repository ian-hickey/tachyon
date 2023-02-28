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
package tachyon.runtime.writer

import java.io.IOException

abstract class CFMLWriter protected constructor(bufferSize: Int, autoFlush: Boolean) : JspWriter(bufferSize, autoFlush) {
    @Throws(IOException::class)
    abstract fun getResponseStream(): OutputStream?
    abstract fun setClosed(closed: Boolean) // do not change used in p d f extension
    @Throws(IOException::class)
    abstract fun setBufferConfig(interval: Int, b: Boolean)
    @Throws(IOException::class)
    abstract fun appendHTMLBody(text: String?)
    @Throws(IOException::class)
    abstract fun writeHTMLBody(text: String?)
    @Throws(IOException::class)
    abstract fun flushHTMLBody()
    @Throws(IOException::class)
    abstract fun getHTMLBody(): String?
    @Throws(IOException::class)
    abstract fun resetHTMLBody()
    @Throws(IOException::class)
    abstract fun appendHTMLHead(text: String?)
    @Throws(IOException::class)
    abstract fun writeHTMLHead(text: String?)
    @Throws(IOException::class)
    abstract fun flushHTMLHead()
    @Throws(IOException::class)
    abstract fun getHTMLHead(): String?
    @Throws(IOException::class)
    abstract fun resetHTMLHead()

    /**
     * write the given string without removing whitespace.
     *
     * @param str
     * @throws IOException
     */
    @Throws(IOException::class)
    abstract fun writeRaw(str: String?)
    abstract fun setAllowCompression(allowCompression: Boolean)
    abstract fun doCache(ci: tachyon.runtime.cache.legacy.CacheItem?)

    /**
     * @return the cacheResource
     */
    abstract fun getCacheItem(): CacheItem?
}