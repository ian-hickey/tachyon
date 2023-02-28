/**
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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
package lucee.transformer.util

import java.io.IOException

class PageSourceCode : SourceCode {
    private val charset: Charset?
    private val ps: PageSource?

    constructor(ps: PageSource?, charset: Charset?, writeLog: Boolean) : super(null, toString(ps, charset), writeLog, ps.getDialect()) {
        this.charset = charset
        this.ps = ps
        // this.source=ps.getPhyscalFile().getAbsolutePath();
    }

    constructor(ps: PageSource?, text: String?, charset: Charset?, writeLog: Boolean) : super(null, text, writeLog, ps.getDialect()) {
        this.charset = charset
        this.ps = ps
    }

    @Override
    override fun id(): String? {
        return HashUtil.create64BitHashAsString(getPageSource().getDisplayPath())
    }

    /**
     * Gibt die Quelle aus dem der CFML Code stammt als File Objekt zurueck, falls dies nicht aud einem
     * File stammt wird null zurueck gegeben.
     *
     * @return source Quelle des CFML Code.
     */
    fun getPageSource(): PageSource? {
        return ps
    }

    fun getCharset(): Charset? {
        return charset
    }

    companion object {
        @Throws(IOException::class)
        fun toString(ps: PageSource?, charset: Charset?): String? {
            val content: String
            var `is`: InputStream? = null
            try {
                `is` = IOUtil.toBufferedInputStream(ps.getPhyscalFile().getInputStream())
                if (ClassUtil.isBytecode(`is`)) throw AlreadyClassException(ps.getPhyscalFile(), false)
                if (ClassUtil.isEncryptedBytecode(`is`)) throw AlreadyClassException(ps.getPhyscalFile(), true)
                content = IOUtil.toString(`is`, charset)
            } finally {
                IOUtil.close(`is`)
            }
            return content
        }
    }
}