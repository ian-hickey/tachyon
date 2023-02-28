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
package lucee.runtime.coder

import java.text.StringCharacterIterator

/**
 * Unix Coding for java
 */
object UUCoder {
    /**
     * encodes a byte array to a String
     *
     * @param barr
     * @return encoded String
     */
    fun encode(barr: ByteArray?): String? {
        val rtn = StringBuilder()
        val len = barr!!.size
        var read = 0
        var stop = false
        var b: Byte = 0
        var offset = 0
        do {
            val left = len - read
            if (left == 0) stop = true
            b = if (left <= 45) left.toByte() else 45
            rtn.append(_enc(b))
            var i = 0
            while (i < b) {
                if (len - offset < 3) {
                    val padding = ByteArray(3)
                    var z = 0
                    while (offset + z < len) {
                        padding[z] = barr[offset + z]
                        z++
                    }
                    encodeBytes(padding, 0, rtn)
                } else {
                    encodeBytes(barr, offset, rtn)
                }
                offset += 3
                i += 3
            }
            rtn.append('\n')
            read += b.toInt()
            if (b < 45) stop = true
        } while (!stop)
        return rtn.toString()
    }

    /**
     * decodes back a String to a byte array
     *
     * @param b
     * @return decoded byte array
     */
    @Throws(CoderException::class)
    fun decode(str: String?): ByteArray? {
        val out = ByteArray(str!!.length())
        var len = 0
        var offset = 0
        // int current = 0;
        var b: Byte = 0
        var stop = false
        val it = StringCharacterIterator(str)
        do {
            b = _dec(it.current())
            it.next()
            if (b > 45) throw CoderException("can't decode string [$str]")
            if (b < 45) stop = true
            len += b.toInt()
            while (b > 0) {
                decodeChars(it, out, offset)
                offset += 3
                (b -= 3).toByte()
            }
            it.next()
        } while (!stop)
        val rtn = ByteArray(len)
        for (i in 0 until len) rtn[i] = out[i]
        return rtn
    }

    private fun encodeBytes(`in`: ByteArray?, off: Int, out: StringBuilder?) {
        out.append(_enc((`in`!![off] ushr 2) as Byte))
        out.append(_enc((`in`[off] shl 4 and 0x30 or `in`[off + 1] ushr 4 and 0xf) as Byte))
        out.append(_enc((`in`[off + 1] shl 2 and 0x3c or `in`[off + 2] ushr 6 and 3) as Byte))
        out.append(_enc((`in`[off + 2] and 0x3f) as Byte))
    }

    private fun decodeChars(it: StringCharacterIterator?, out: ByteArray?, off: Int) {
        val b1 = _dec(it.current())
        val b2 = _dec(it.next())
        val b3 = _dec(it.next())
        val b4 = _dec(it.next())
        it.next()
        val b5 = (b1 shl 2 or b2 shr 4) as Byte
        val b6 = (b2 shl 4 or b3 shr 2) as Byte
        val b7 = (b3 shl 6 or b4) as Byte
        out!![off] = b5
        out[off + 1] = b6
        out[off + 2] = b7
    }

    private fun _enc(c: Byte): Char {
        return ((c and 0x3f) + 32)
    }

    private fun _dec(c: Char): Byte {
        return (c.toInt() - 32 and 0x3f).toByte()
    }
}