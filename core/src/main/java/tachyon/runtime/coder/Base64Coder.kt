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
package tachyon.runtime.coder

import java.io.UnsupportedEncodingException

/**
 * Util class to handle Base 64 Encoded Strings
 */
object Base64Coder {
    /**
     * decodes a Base64 String to a Plain String
     *
     * @param encoded
     * @return
     * @throws ExpressionException
     */
    @Throws(CoderException::class, UnsupportedEncodingException::class)
    fun decodeToString(encoded: String?, charset: String?, precise: Boolean): String? {
        val dec = decode(Caster.toString(encoded, null), precise)
        return String(dec, charset)
    }

    /**
     * encodes a String to Base64 String
     *
     * @param plain String to encode
     * @return encoded String
     * @throws CoderException
     * @throws UnsupportedEncodingException
     */
    @Throws(CoderException::class, UnsupportedEncodingException::class)
    fun encodeFromString(plain: String?, charset: String?): String? {
        return encode(plain.getBytes(charset))
    }

    /**
     * encodes a byte array to Base64 String
     *
     * @param barr byte array to encode
     * @return encoded String
     * @throws CoderException
     */
    fun encode(barr: ByteArray?): String? {
        return Base64Encoder.encode(barr)
    }

    /**
     * decodes a Base64 String to a Plain String
     *
     * @param encoded
     * @return decoded binary data
     * @throws CoderException
     */
    @Throws(CoderException::class)
    fun decode(encoded: String?, precise: Boolean): ByteArray? {
        return Base64Encoder.decode(encoded, precise)
    }
}