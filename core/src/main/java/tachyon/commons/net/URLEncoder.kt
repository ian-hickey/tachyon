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
package tachyon.commons.net

import java.io.UnsupportedEncodingException

/**
 * Utility class for HTML form encoding. This class contains static methods for converting a String
 * to the <CODE>application/x-www-form-urlencoded</CODE> MIME format. For more information about
 * HTML form encoding, consult the HTML <A HREF="http://www.w3.org/TR/html4/">specification</A>.
 *
 *
 *
 * When encoding a String, the following rules apply:
 *
 *
 *
 *
 *  * The alphanumeric characters &quot;`a`&quot; through &quot;`z`&quot;,
 * &quot;`A`&quot; through &quot;`Z`&quot; and &quot;`0`&quot;
 * through &quot;`9`&quot; remain the same.
 *  * The special characters &quot;`.`&quot;, &quot;`-`&quot;,
 * &quot;`*`&quot;, and &quot;`_`&quot; remain the same.
 *  * The space character &quot;`&nbsp;`&quot; is converted into a plus sign
 * &quot;`+`&quot;.
 *  * All other characters are unsafe and are first converted into one or more bytes using some
 * encoding scheme. Then each byte is represented by the 3-character string
 * &quot;`%*xy*`&quot;, where *xy* is the two-digit hexadecimal representation
 * of the byte. The recommended encoding scheme to use is UTF-8. However, for compatibility reasons,
 * if an encoding is not specified, then the default encoding of the platform is used.
 *
 *
 *
 *
 * For example using UTF-8 as the encoding scheme the string &quot;The string &#252;@foo-bar&quot;
 * would get converted to &quot;The+string+%C3%BC%40foo-bar&quot; because in UTF-8 the character
 * &#252; is encoded as two bytes C3 (hex) and BC (hex), and the character @ is encoded as one byte
 * 40 (hex).
 *
 * @author Herb Jellinek
 * @version 1.25, 12/03/01
 * @since JDK1.0
 */
object URLEncoder {
    /**
     * Translates a string into `x-www-form-urlencoded` format. This method uses the
     * platform's default encoding as the encoding scheme to obtain the bytes for unsafe characters.
     *
     * @param s `String` to be translated.
     * @return the translated `String`.
     */
    @Deprecated
    @Deprecated("""The resulting string may vary depending on the platform's default encoding. Instead,
	              use the encode(String,String) method to specify the encoding.
	  """)
    fun encode(s: String?): String? {
        var s = s
        s = java.net.URLEncoder.encode(s)
        if (s.indexOf('+') !== -1) s = StringUtil.replace(s, "+", "%20", false)
        return s
    }

    /**
     * @param s
     * @param enc
     * @return
     * @throws UnsupportedEncodingException
     */
    @Deprecated
    @Deprecated("""use instead <code>encode(String s, Charset cs)</code>
	  """)
    @Throws(UnsupportedEncodingException::class)
    fun encode(s: String, enc: String?): String {
        var s = s
        s = java.net.URLEncoder.encode(s, enc)
        if (s.indexOf('+') !== -1) s = StringUtil.replace(s, "+", "%20", false)
        return s
    }

    @Throws(UnsupportedEncodingException::class)
    fun encode(s: String, cs: Charset): String {
        var s = s
        s = java.net.URLEncoder.encode(s, cs.name())
        if (s.indexOf('+') !== -1) s = StringUtil.replace(s, "+", "%20", false)
        return s
    }
}