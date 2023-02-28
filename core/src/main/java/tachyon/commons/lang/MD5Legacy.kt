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
package tachyon.commons.lang

import java.io.ByteArrayInputStream

/**
 * class to create a MD5 sum
 */
class MD5Legacy {
    private val `in`: InputStream
    private val state: IntArray
    private var count: Long = 0
    private val buffer: ByteArray

    /**
     * Get the digest for our input stream. This method constructs the input stream digest, and return
     * it, as a String, following the MD5 (rfc1321) algorithm,
     *
     * @return An instance of String, giving the message digest.
     * @exception IOException Thrown if the digestifier was unable to read the input stream.
     */
    @get:Throws(IOException::class)
    private var digest: ByteArray?
        private get() {
            val buffer = ByteArray(BUFFER_SIZE)
            var got = -1
            if (field != null) return stringify(field)
            while (`in`.read(buffer).also { got = it } > 0) update(buffer, got)
            field = end()
            return stringify(field)
        }

    private fun F(x: Int, y: Int, z: Int): Int {
        return x and y or (x.inv() and z)
    }

    private fun G(x: Int, y: Int, z: Int): Int {
        return x and z or (y and z.inv())
    }

    private fun H(x: Int, y: Int, z: Int): Int {
        return x xor y xor z
    }

    private fun I(x: Int, y: Int, z: Int): Int {
        return y xor (x or z.inv())
    }

    private fun rotate_left(x: Int, n: Int): Int {
        return x shl n or (x ushr 32 - n)
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

    private fun decode(output: IntArray, input: ByteArray, off: Int, len: Int) {
        var i = 0
        var j = 0
        while (j < len) {
            output[i] = input[off + j] and 0xff or (input[off + j + 1] and 0xff shl 8) or (input[off + j + 2] and 0xff shl 16) or (input[off + j + 3] and 0xff shl 24)
            i++
            j += 4
        }
    }

    private fun transform(block: ByteArray, offset: Int) {
        var a = state[0]
        var b = state[1]
        var c = state[2]
        var d = state[3]
        val x = IntArray(16)
        decode(x, block, offset, 64)
        /* Round 1 */a = FF(a, b, c, d, x[0], S11, -0x28955b88) /* 1 */
        d = FF(d, a, b, c, x[1], S12, -0x173848aa) /* 2 */
        c = FF(c, d, a, b, x[2], S13, 0x242070db) /* 3 */
        b = FF(b, c, d, a, x[3], S14, -0x3e423112) /* 4 */
        a = FF(a, b, c, d, x[4], S11, -0xa83f051) /* 5 */
        d = FF(d, a, b, c, x[5], S12, 0x4787c62a) /* 6 */
        c = FF(c, d, a, b, x[6], S13, -0x57cfb9ed) /* 7 */
        b = FF(b, c, d, a, x[7], S14, -0x2b96aff) /* 8 */
        a = FF(a, b, c, d, x[8], S11, 0x698098d8) /* 9 */
        d = FF(d, a, b, c, x[9], S12, -0x74bb0851) /* 10 */
        c = FF(c, d, a, b, x[10], S13, -0xa44f) /* 11 */
        b = FF(b, c, d, a, x[11], S14, -0x76a32842) /* 12 */
        a = FF(a, b, c, d, x[12], S11, 0x6b901122) /* 13 */
        d = FF(d, a, b, c, x[13], S12, -0x2678e6d) /* 14 */
        c = FF(c, d, a, b, x[14], S13, -0x5986bc72) /* 15 */
        b = FF(b, c, d, a, x[15], S14, 0x49b40821) /* 16 */
        /* Round 2 */a = GG(a, b, c, d, x[1], S21, -0x9e1da9e) /* 17 */
        d = GG(d, a, b, c, x[6], S22, -0x3fbf4cc0) /* 18 */
        c = GG(c, d, a, b, x[11], S23, 0x265e5a51) /* 19 */
        b = GG(b, c, d, a, x[0], S24, -0x16493856) /* 20 */
        a = GG(a, b, c, d, x[5], S21, -0x29d0efa3) /* 21 */
        d = GG(d, a, b, c, x[10], S22, 0x2441453) /* 22 */
        c = GG(c, d, a, b, x[15], S23, -0x275e197f) /* 23 */
        b = GG(b, c, d, a, x[4], S24, -0x182c0438) /* 24 */
        a = GG(a, b, c, d, x[9], S21, 0x21e1cde6) /* 25 */
        d = GG(d, a, b, c, x[14], S22, -0x3cc8f82a) /* 26 */
        c = GG(c, d, a, b, x[3], S23, -0xb2af279) /* 27 */
        b = GG(b, c, d, a, x[8], S24, 0x455a14ed) /* 28 */
        a = GG(a, b, c, d, x[13], S21, -0x561c16fb) /* 29 */
        d = GG(d, a, b, c, x[2], S22, -0x3105c08) /* 30 */
        c = GG(c, d, a, b, x[7], S23, 0x676f02d9) /* 31 */
        b = GG(b, c, d, a, x[12], S24, -0x72d5b376) /* 32 */

        /* Round 3 */a = HH(a, b, c, d, x[5], S31, -0x5c6be) /* 33 */
        d = HH(d, a, b, c, x[8], S32, -0x788e097f) /* 34 */
        c = HH(c, d, a, b, x[11], S33, 0x6d9d6122) /* 35 */
        b = HH(b, c, d, a, x[14], S34, -0x21ac7f4) /* 36 */
        a = HH(a, b, c, d, x[1], S31, -0x5b4115bc) /* 37 */
        d = HH(d, a, b, c, x[4], S32, 0x4bdecfa9) /* 38 */
        c = HH(c, d, a, b, x[7], S33, -0x944b4a0) /* 39 */
        b = HH(b, c, d, a, x[10], S34, -0x41404390) /* 40 */
        a = HH(a, b, c, d, x[13], S31, 0x289b7ec6) /* 41 */
        d = HH(d, a, b, c, x[0], S32, -0x155ed806) /* 42 */
        c = HH(c, d, a, b, x[3], S33, -0x2b10cf7b) /* 43 */
        b = HH(b, c, d, a, x[6], S34, 0x4881d05) /* 44 */
        a = HH(a, b, c, d, x[9], S31, -0x262b2fc7) /* 45 */
        d = HH(d, a, b, c, x[12], S32, -0x1924661b) /* 46 */
        c = HH(c, d, a, b, x[15], S33, 0x1fa27cf8) /* 47 */
        b = HH(b, c, d, a, x[2], S34, -0x3b53a99b) /* 48 */

        /* Round 4 */a = II(a, b, c, d, x[0], S41, -0xbd6ddbc) /* 49 */
        d = II(d, a, b, c, x[7], S42, 0x432aff97) /* 50 */
        c = II(c, d, a, b, x[14], S43, -0x546bdc59) /* 51 */
        b = II(b, c, d, a, x[5], S44, -0x36c5fc7) /* 52 */
        a = II(a, b, c, d, x[12], S41, 0x655b59c3) /* 53 */
        d = II(d, a, b, c, x[3], S42, -0x70f3336e) /* 54 */
        c = II(c, d, a, b, x[10], S43, -0x100b83) /* 55 */
        b = II(b, c, d, a, x[1], S44, -0x7a7ba22f) /* 56 */
        a = II(a, b, c, d, x[8], S41, 0x6fa87e4f) /* 57 */
        d = II(d, a, b, c, x[15], S42, -0x1d31920) /* 58 */
        c = II(c, d, a, b, x[6], S43, -0x5cfebcec) /* 59 */
        b = II(b, c, d, a, x[13], S44, 0x4e0811a1) /* 60 */
        a = II(a, b, c, d, x[4], S41, -0x8ac817e) /* 61 */
        d = II(d, a, b, c, x[11], S42, -0x42c50dcb) /* 62 */
        c = II(c, d, a, b, x[2], S43, 0x2ad7d2bb) /* 63 */
        b = II(b, c, d, a, x[9], S44, -0x14792c6f) /* 64 */
        state[0] += a
        state[1] += b
        state[2] += c
        state[3] += d
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
        for (i in 0..7) bits[i] = (count ushr i * 8 and 0xff).toByte()
        val index = (count shr 3).toInt() and 0x3f
        val padlen = if (index < 56) 56 - index else 120 - index
        update(padding, padlen)
        update(bits, 8)
        return encode(state, 16)
    }

    // Encode the content.state array into 16 bytes array
    private fun encode(input: IntArray, len: Int): ByteArray {
        val output = ByteArray(len)
        var i = 0
        var j = 0
        while (j < len) {
            output[j] = (input[i] and 0xff).toByte()
            output[j + 1] = (input[i] shr 8 and 0xff).toByte()
            output[j + 2] = (input[i] shr 16 and 0xff).toByte()
            output[j + 3] = (input[i] shr 24 and 0xff).toByte()
            i++
            j += 4
        }
        return output
    }

    /**
     * Construct a digestifier for the given byte array.
     *
     * @param input The byte array to be digestified.
     */
    private constructor(bytes: ByteArray) {
        `in` = ByteArrayInputStream(bytes)
        state = IntArray(4)
        buffer = ByteArray(64)
        count = 0L
        state[0] = 0x67452301
        state[1] = -0x10325477
        state[2] = -0x67452302
        state[3] = 0x10325476
    }

    /**
     * Construct a digestifier for the given string.
     *
     * @param input The string to be digestified.
     */
    private constructor(input: String) : this(input.getBytes()) {}

    /**
     * Construct a digestifier for the given input stream.
     *
     * @param in The input stream to be digestified.
     */
    private constructor(`in`: InputStream) {
        // this.stringp = false ;
        this.`in` = `in`
        state = IntArray(4)
        buffer = ByteArray(64)
        count = 0
        state[0] = 0x67452301
        state[1] = -0x10325477
        state[2] = -0x67452302
        state[3] = 0x10325476
    }

    companion object {
        private const val BUFFER_SIZE = 1024
        private const val S11 = 7
        private const val S12 = 12
        private const val S13 = 17
        private const val S14 = 22
        private const val S21 = 5
        private const val S22 = 9
        private const val S23 = 14
        private const val S24 = 20
        private const val S31 = 4
        private const val S32 = 11
        private const val S33 = 16
        private const val S34 = 23
        private const val S41 = 6
        private const val S42 = 10
        private const val S43 = 15
        private const val S44 = 21
        private val padding = byteArrayOf(0x80.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(),
                0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(),
                0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(),
                0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(),
                0.toByte(), 0.toByte(), 0.toByte())

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

        /**
         * return md5 from byte array
         *
         * @param barr byte array to get md5 from
         * @return md5 from string
         * @throws IOException
         */
        @Throws(IOException::class)
        fun getDigestAsString(barr: ByteArray?): String {
            return MD5Legacy(barr).digest
        }

        /**
         * return md5 from string as string
         *
         * @param str plain string to get md5 from
         * @return md5 from string
         * @throws IOException
         */
        @Throws(IOException::class)
        fun getDigestAsString(str: String?): String {
            return MD5Legacy(str).digest
        }

        /**
         * return md5 from InputStream as string
         *
         * @param in The input stream to be digestified.
         * @return md5 from string
         * @throws IOException
         */
        @Throws(IOException::class)
        fun getDigestAsString(`in`: InputStream): String {
            return MD5Legacy(`in`).digest
        }
    }
}