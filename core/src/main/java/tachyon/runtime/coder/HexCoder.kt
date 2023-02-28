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

import tachyon.commons.io.CharsetUtil

/**
 *
 */
object HexCoder {
    private val HEX_ARRAY: CharArray? = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F')

    /**
     * encodes a byte array to a String
     *
     * @param bytes
     * @return encoded String
     */
    fun encode(bytes: ByteArray?): String? {
        val hexChars = CharArray(bytes!!.size * 2)
        for (j in bytes.indices) {
            val v: Int = bytes[j] and 0xFF
            hexChars[j * 2] = HEX_ARRAY!![v ushr 4]
            hexChars[j * 2 + 1] = HEX_ARRAY[v and 0x0F]
        }
        return String(hexChars)
    }

    /**
     * decodes back a String to a byte array
     *
     * @param hexa
     * @return decoded byte array
     * @throws CoderException
     */
    @Throws(CoderException::class)
    fun decode(hexa: String?): ByteArray? {
        if (hexa == null) {
            throw CoderException("can't decode empty String")
        }
        if (hexa.length() % 2 !== 0) {
            throw CoderException("invalid hexadecimal String for, [ $hexa ]. The number of characters passed in, must be even, Allowed characters are [0-9], [a-f], [A-F]")
        }
        val tamArray: Int = hexa.length() / 2
        val retorno = ByteArray(tamArray)
        for (i in 0 until tamArray) {
            retorno[i] = hexToByte(hexa.substring(i * 2, i * 2 + 2))
        }
        return retorno
    }

    @Throws(CoderException::class)
    private fun hexToByte(hexa: String?): Byte {
        if (hexa == null) {
            throw CoderException("can't decode empty String")
        }
        if (hexa.length() !== 2) {
            throw CoderException("invalid hexadecimal String for, [ $hexa ]. The number of characters passed in, must be 2. Allowed characters are [0-9], [a-f], [A-F]")
        }
        val b: ByteArray = hexa.getBytes(CharsetUtil.UTF8)
        return (hexDigitValue(b[0].toChar()) * 16 + hexDigitValue(b[1].toChar())).toByte()
    }

    @Throws(CoderException::class)
    private fun hexDigitValue(c: Char): Int {
        var retorno = 0
        retorno = if (c >= '0' && c <= '9') {
            c.toByte() - 48
        } else if (c >= 'A' && c <= 'F') {
            c.toByte() - 55
        } else if (c >= 'a' && c <= 'f') {
            c.toByte() - 87
        } else {
            throw CoderException("invalid hexadecimal String for, [ $c ]. Allowed characters are [0-9], [a-f], [A-F]")
        }
        return retorno
    }
}