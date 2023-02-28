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
package tachyon.commons.digest

import java.io.ByteArrayInputStream

/**
 *
 */
class MD5 {
    private fun F(x: Int, y: Int, z: Int): Int {
        return x and y or x.inv() and z
    }

    private fun G(x: Int, y: Int, z: Int): Int {
        return x and z or y and z.inv()
    }

    private fun H(x: Int, y: Int, z: Int): Int {
        return x xor y xor z
    }

    private fun I(x: Int, y: Int, z: Int): Int {
        return y xor (x or z.inv())
    }

    private fun rotate_left(x: Int, n: Int): Int {
        return x shl n or x ushr 32 - n
    }

    private fun FF(a: Int, b: Int, c: Int, d: Int, x: Int, s: Int, ac: Int): Int {
        var a = a
        a += F(b, c, d) + x + ac
        a = rotate_left(a, s)
        a += b
        return a
    }

    private fun GG(a: Int, b: Int, c: Int, d: Int, x: Int, s: Int, ac: Int): Int {
        var a = a
        a += G(b, c, d) + x + ac
        a = rotate_left(a, s)
        a += b
        return a
    }

    private fun HH(a: Int, b: Int, c: Int, d: Int, x: Int, s: Int, ac: Int): Int {
        var a = a
        a += H(b, c, d) + x + ac
        a = rotate_left(a, s)
        a += b
        return a
    }

    private fun II(a: Int, b: Int, c: Int, d: Int, x: Int, s: Int, ac: Int): Int {
        var a = a
        a += I(b, c, d) + x + ac
        a = rotate_left(a, s)
        a += b
        return a
    }

    private fun decode(output: IntArray, input: ByteArray?, off: Int, len: Int) {
        var i = 0
        var j = 0
        while (j < len) {
            output[i] = input!![off + j] and 0xff or (input[off + j + 1] and 0xff shl 8) or (input[off + j + 2] and 0xff shl 16) or (input[off + j + 3] and 0xff shl 24)
            i++
            j += 4
        }
    }

    private fun transform(block: ByteArray?, offset: Int) {
        var a = state!![0]
        var b = state!![1]
        var c = state!![2]
        var d = state!![3]
        val x = IntArray(16)
        decode(x, block, offset, 64)
        a = FF(a, b, c, d, x[0], 7, -0x28955b88)
        d = FF(d, a, b, c, x[1], 12, -0x173848aa)
        c = FF(c, d, a, b, x[2], 17, 0x242070db)
        b = FF(b, c, d, a, x[3], 22, -0x3e423112)
        a = FF(a, b, c, d, x[4], 7, -0xa83f051)
        d = FF(d, a, b, c, x[5], 12, 0x4787c62a)
        c = FF(c, d, a, b, x[6], 17, -0x57cfb9ed)
        b = FF(b, c, d, a, x[7], 22, -0x2b96aff)
        a = FF(a, b, c, d, x[8], 7, 0x698098d8)
        d = FF(d, a, b, c, x[9], 12, -0x74bb0851)
        c = FF(c, d, a, b, x[10], 17, -42063)
        b = FF(b, c, d, a, x[11], 22, -0x76a32842)
        a = FF(a, b, c, d, x[12], 7, 0x6b901122)
        d = FF(d, a, b, c, x[13], 12, -0x2678e6d)
        c = FF(c, d, a, b, x[14], 17, -0x5986bc72)
        b = FF(b, c, d, a, x[15], 22, 0x49b40821)
        a = GG(a, b, c, d, x[1], 5, -0x9e1da9e)
        d = GG(d, a, b, c, x[6], 9, -0x3fbf4cc0)
        c = GG(c, d, a, b, x[11], 14, 0x265e5a51)
        b = GG(b, c, d, a, x[0], 20, -0x16493856)
        a = GG(a, b, c, d, x[5], 5, -0x29d0efa3)
        d = GG(d, a, b, c, x[10], 9, 0x2441453)
        c = GG(c, d, a, b, x[15], 14, -0x275e197f)
        b = GG(b, c, d, a, x[4], 20, -0x182c0438)
        a = GG(a, b, c, d, x[9], 5, 0x21e1cde6)
        d = GG(d, a, b, c, x[14], 9, -0x3cc8f82a)
        c = GG(c, d, a, b, x[3], 14, -0xb2af279)
        b = GG(b, c, d, a, x[8], 20, 0x455a14ed)
        a = GG(a, b, c, d, x[13], 5, -0x561c16fb)
        d = GG(d, a, b, c, x[2], 9, -0x3105c08)
        c = GG(c, d, a, b, x[7], 14, 0x676f02d9)
        b = GG(b, c, d, a, x[12], 20, -0x72d5b376)
        a = HH(a, b, c, d, x[5], 4, -0x5c6be)
        d = HH(d, a, b, c, x[8], 11, -0x788e097f)
        c = HH(c, d, a, b, x[11], 16, 0x6d9d6122)
        b = HH(b, c, d, a, x[14], 23, -0x21ac7f4)
        a = HH(a, b, c, d, x[1], 4, -0x5b4115bc)
        d = HH(d, a, b, c, x[4], 11, 0x4bdecfa9)
        c = HH(c, d, a, b, x[7], 16, -0x944b4a0)
        b = HH(b, c, d, a, x[10], 23, -0x41404390)
        a = HH(a, b, c, d, x[13], 4, 0x289b7ec6)
        d = HH(d, a, b, c, x[0], 11, -0x155ed806)
        c = HH(c, d, a, b, x[3], 16, -0x2b10cf7b)
        b = HH(b, c, d, a, x[6], 23, 0x4881d05)
        a = HH(a, b, c, d, x[9], 4, -0x262b2fc7)
        d = HH(d, a, b, c, x[12], 11, -0x1924661b)
        c = HH(c, d, a, b, x[15], 16, 0x1fa27cf8)
        b = HH(b, c, d, a, x[2], 23, -0x3b53a99b)
        a = II(a, b, c, d, x[0], 6, -0xbd6ddbc)
        d = II(d, a, b, c, x[7], 10, 0x432aff97)
        c = II(c, d, a, b, x[14], 15, -0x546bdc59)
        b = II(b, c, d, a, x[5], 21, -0x36c5fc7)
        a = II(a, b, c, d, x[12], 6, 0x655b59c3)
        d = II(d, a, b, c, x[3], 10, -0x70f3336e)
        c = II(c, d, a, b, x[10], 15, -0x100b83)
        b = II(b, c, d, a, x[1], 21, -0x7a7ba22f)
        a = II(a, b, c, d, x[8], 6, 0x6fa87e4f)
        d = II(d, a, b, c, x[15], 10, -0x1d31920)
        c = II(c, d, a, b, x[6], 15, -0x5cfebcec)
        b = II(b, c, d, a, x[13], 21, 0x4e0811a1)
        a = II(a, b, c, d, x[4], 6, -0x8ac817e)
        d = II(d, a, b, c, x[11], 10, -0x42c50dcb)
        c = II(c, d, a, b, x[2], 15, 0x2ad7d2bb)
        b = II(b, c, d, a, x[9], 21, -0x14792c6f)
        state!![0] += a
        state!![1] += b
        state!![2] += c
        state!![3] += d
    }

    private fun update(input: ByteArray, len: Int) {
        var index = (count shr 3).toInt() and 0x3f
        count += (len shl 3).toLong()
        val partLen = 64 - index
        var i = 0
        if (len >= partLen) {
            System.arraycopy(input, 0, buffer, index, partLen)
            transform(buffer, 0)
            i = partLen
            while (i + 63 < len) {
                transform(input, i)
                i += 64
            }
            index = 0
        } else {
            i = 0
        }
        System.arraycopy(input, i, buffer, index, len - i)
    }

    private fun end(): ByteArray {
        val bits = ByteArray(8)
        for (i in 0..7) bits[i] = (count ushr i * 8 and 255L).toInt().toByte()
        val index = (count shr 3).toInt() and 0x3f
        val padlen = if (index >= 56) 120 - index else 56 - index
        update(padding, padlen)
        update(bits, 8)
        return encode(state, 16)
    }

    private fun encode(input: IntArray?, len: Int): ByteArray {
        val output = ByteArray(len)
        var i = 0
        var j = 0
        while (j < len) {
            output[j] = (input!![i] and 0xff).toByte()
            output[j + 1] = (input[i] shr 8 and 0xff).toByte()
            output[j + 2] = (input[i] shr 16 and 0xff).toByte()
            output[j + 3] = (input[i] shr 24 and 0xff).toByte()
            i++
            j += 4
        }
        return output
    }

    /**
     * @return return the digest
     * @throws IOException
     */
    @Throws(IOException::class)
    fun getDigest(): String {
        val buffer = ByteArray(1024)
        var got = -1
        if (digest != null) return stringify(digest)
        while (`in`.read(buffer).also { got = it } > 0) update(buffer, got)
        digest = end()
        return stringify(digest)
    }

    /**
     * @param input
     */
    constructor(input: String) {
        `in` = null
        // stringp = false;
        state = null
        count = 0L
        buffer = null
        digest = null
        val bytes: ByteArray = input.getBytes(CharsetUtil.UTF8)
        // stringp = true;
        `in` = ByteArrayInputStream(bytes)
        state = IntArray(4)
        buffer = ByteArray(64)
        count = 0L
        state!![0] = 0x67452301
        state!![1] = -0x10325477
        state!![2] = -0x67452302
        state!![3] = 0x10325476
    }

    constructor(bytes: ByteArray?) {
        `in` = null
        // stringp = false;
        state = null
        count = 0L
        buffer = null
        digest = null
        `in` = ByteArrayInputStream(bytes)
        state = IntArray(4)
        buffer = ByteArray(64)
        count = 0L
        state!![0] = 0x67452301
        state!![1] = -0x10325477
        state!![2] = -0x67452302
        state!![3] = 0x10325476
    }

    private var `in`: InputStream?

    // private boolean stringp;
    private var state: IntArray?
    private var count: Long
    private var buffer: ByteArray?
    private var digest: ByteArray?

    companion object {
        /**
         * return md5 from string as string
         *
         * @param str plain string to get md5 from
         * @return md5 from string
         * @throws IOException
         */
        @Throws(IOException::class)
        fun getDigestAsString(str: String): String {
            return MD5(str).getDigest()
        }

        @Throws(IOException::class)
        fun getDigestAsString(barr: ByteArray?): String {
            return MD5(barr).getDigest()
        }

        fun getDigestAsString(str: String, defaultValue: String): String {
            return try {
                MD5(str).getDigest()
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
                defaultValue
            }
        }

        fun stringify(digest: ByteArray?): String {
            val chars = CharArray(2 * digest!!.size)
            var h: Int
            var l: Int
            var count = 0
            for (i in digest.indices) {
                h = digest[i] and 0xf0 shr 4
                l = digest[i] and 0x0f
                chars[count++] = (if (h > 9) 'a'.toInt() + h - 10 else '0'.toInt() + h).toChar()
                chars[count++] = (if (l > 9) 'a'.toInt() + l - 10 else '0'.toInt() + l).toChar()
            }
            return String(chars)
        }

        /*
	 * public static final int DIGEST_CHARS = 32; public static final int DIGEST_BYTES = 16; private
	 * static final int BUFFER_SIZE = 1024; private static final int S11 = 7; private static final int
	 * S12 = 12; private static final int S13 = 17; private static final int S14 = 22; private static
	 * final int S21 = 5; private static final int S22 = 9; private static final int S23 = 14; private
	 * static final int S24 = 20; private static final int S31 = 4; private static final int S32 = 11;
	 * private static final int S33 = 16; private static final int S34 = 23; private static final int
	 * S41 = 6; private static final int S42 = 10; private static final int S43 = 15; private static
	 * final int S44 = 21;
	 */
        private val padding = byteArrayOf(-128, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
    }
}