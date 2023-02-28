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
package lucee.commons.io

import java.nio.charset.Charset

object CharsetUtil {
    val UTF8: Charset? = null
    val ISO88591: Charset? = null
    val UTF16BE: Charset? = null
    val UTF16LE: Charset? = null
    val UTF32BE: Charset? = null
    val UTF32LE: Charset? = null
    fun toCharset(charset: String): Charset? {
        return if (StringUtil.isEmpty(charset, true)) null else Charset.forName(charset.trim())
    }

    fun toCharset(charset: String?, defaultValue: Charset?): Charset? {
        return if (StringUtil.isEmpty(charset)) defaultValue else try {
            Charset.forName(charset)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            defaultValue
        }
    }

    fun toCharSet(charset: String): CharSet? {
        return if (StringUtil.isEmpty(charset, true)) null else CharSet(Charset.forName(charset.trim()))
    }

    fun toCharSet(charset: String?, defaultValue: CharSet?): CharSet? {
        return if (StringUtil.isEmpty(charset)) defaultValue else try {
            CharSet(Charset.forName(charset))
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            defaultValue
        }
    }

    fun toCharSet(charset: Charset?): CharSet? {
        return if (charset == null) null else CharSet(charset)
    }

    fun toCharset(charset: CharSet?): Charset? {
        return if (charset == null) null else charset.toCharset()
    }

    val webCharset: Charset?
        get() {
            val pc: PageContext = ThreadLocalPageContext.get()
            if (pc != null) return pc.getWebCharset()
            val config: Config = ThreadLocalPageContext.getConfig()
            return if (config != null) config.getWebCharset() else ISO88591
        }
    val availableCharsets: Array<String>
        get() {
            val map: SortedMap<String, Charset> = java.nio.charset.Charset.availableCharsets()
            val keys: Array<String> = map.keySet().toArray(arrayOfNulls<String>(map.size()))
            Arrays.sort(keys)
            return keys
        }

    /**
     * is given charset supported or not
     *
     * @param charset
     * @return
     */
    fun isSupported(charset: String?): Boolean {
        return java.nio.charset.Charset.isSupported(charset)
    }

    init {
        UTF8 = toCharset("utf-8", null)
        ISO88591 = toCharset("iso-8859-1", null)
        UTF16BE = toCharset("utf-16BE", null)
        UTF16LE = toCharset("utf-16LE", null)
        UTF32BE = toCharset("utf-32BE", null)
        UTF32LE = toCharset("utf-32LE", null)
    }
}