/**
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
package lucee.commons.digest

import java.util.HashMap

object Base64Encoder {
    private val ALPHABET = charArrayOf('A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y',
            'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', '+', '/')
    private const val PAD = '='
    private val REVERSE: Map<Character, Integer> = HashMap<Character, Integer>()
    fun encodeFromString(data: String): String {
        return encode(data.getBytes(CharsetUtil.UTF8))
    }

    /**
     * Translates the specified byte array into Base64 string.
     *
     * @param data the byte array (not null)
     * @return the translated Base64 string (not null)
     */
    fun encode(data: ByteArray): String {
        val builder = StringBuilder()
        var position = 0
        while (position < data.size) {
            builder.append(encodeGroup(data, position))
            position += 3
        }
        return builder.toString()
    }
    //// Helper methods
    /**
     * Encode three bytes of data into four characters.
     */
    private fun encodeGroup(data: ByteArray, position: Int): CharArray {
        val c = charArrayOf('=', '=', '=', '=')
        var b1 = 0
        var b2 = 0
        var b3 = 0
        val length = data.size - position
        if (length == 0) return c
        if (length >= 1) {
            b1 = data[position] and 0xFF
        }
        if (length >= 2) {
            b2 = data[position + 1] and 0xFF
        }
        if (length >= 3) {
            b3 = data[position + 2] and 0xFF
        }
        c[0] = ALPHABET[b1 shr 2]
        c[1] = ALPHABET[b1 and 3 shl 4 or (b2 shr 4)]
        if (length == 1) return c
        c[2] = ALPHABET[b2 and 15 shl 2 or (b3 shr 6)]
        if (length == 2) return c
        c[3] = ALPHABET[b3 and 0x3f]
        return c
    }

    @Throws(CoderException::class)
    fun decodeAsString(data: String, precise: Boolean): String {
        return String(decode(data, precise), CharsetUtil.UTF8)
    }

    /**
     * Translates the specified Base64 string into a byte array.
     *
     * @param data the Base64 string (not null)
     * @return the byte array (not null)
     * @throws CoderException
     */
    @Throws(CoderException::class)
    fun decode(data: String, precise: Boolean): ByteArray {
        if (StringUtil.isEmpty(data)) return ByteArray(0)
        if (precise) {
            val l: Int = data.length()
            if (l / 4 * 4 != l) {
                throw CoderException("cannot convert the input to a binary, invalid length ($l) of the string")
            }

            // A–Z, a–z, 0–9, +, / and =
            var c: Char
            var i: Int = data.length() - 1
            var count = 0
            while (i >= 0) {
                c = data.charAt(i)
                if (c != '=') break
                count++
                i--
            }
            if (count > 3) throw CoderException("invalid padding length [$count], maximal length is [3]")
            while (i >= 0) {
                c = data.charAt(i)
                if (c >= '0' && c <= '9' || c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c == '+' || c == '/') {
                    i--
                    continue
                }
                throw CoderException("invalid character [" + c + "] in base64 string at position [" + (i + 1) + "]")
                i--
            }
        }
        val res: ByteArray = org.apache.commons.codec.binary.Base64.decodeBase64(data)
        if (res == null || res.size == 0) throw CoderException("cannot convert the input to a binary")
        return res
    }

    init {
        for (i in 0..63) {
            REVERSE.put(ALPHABET[i], i)
        }
        REVERSE.put('-', 62)
        REVERSE.put('_', 63)
        REVERSE.put(PAD, 0)
    }
}