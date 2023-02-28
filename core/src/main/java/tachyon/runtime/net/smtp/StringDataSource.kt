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
package tachyon.runtime.net.smtp

import java.io.ByteArrayInputStream

class StringDataSource : DataSource {
    private val text: String?

    @get:Override
    val contentType: String?
    private val charset: CharSet?

    /*
	 * Some types of transfer encoding such as "quoted-printable" and "base64"
	 * do not require wrapping of lines, because it's handled automatically
	 * in the encoding.
	 */
    constructor(text: String?, ct: String?, charset: CharSet?) {
        this.text = text
        contentType = ct
        this.charset = charset
    }

    constructor(text: String?, ct: String?, charset: CharSet?, maxLineLength: Int) {
        this.text = wrapText(text, maxLineLength)
        contentType = ct
        this.charset = charset
    }

    @get:Throws(IOException::class)
    @get:Override
    val inputStream: InputStream?
        get() = ByteArrayInputStream(if (charset == null) text.getBytes() else text.getBytes(CharsetUtil.toCharset(charset)))

    @get:Override
    val name: String?
        get() = "StringDataSource"

    @get:Throws(IOException::class)
    @get:Override
    val outputStream: OutputStream?
        get() {
            throw IOException("no access to write")
        }

    companion object {
        const val CR = 13.toChar()
        const val LF = 10.toChar()
        fun wrapText(text: String?, maxLineLength: Int): String? {
            if (StringUtil.isEmpty(text)) return ""
            val sb = StringBuilder(text!!.length())
            val scanner = Scanner(text)
            var line: String
            while (scanner.hasNextLine()) {
                line = scanner.nextLine()
                if (line.length() > maxLineLength) line = WordUtils.wrap(line, maxLineLength)
                sb.append(line).append(CR).append(LF)
            }
            return sb.toString()
        }
    }
}