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
package lucee.commons.digest

import java.io.IOException

object Hash {
    val ENCODING_HEX = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f')
    val ENCODING_HEXUC = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F')

    // public static final char[] ENCODING_ASCII = "0123456789abcdefghijklmnopqrstuvwxyz".toCharArray();
    private val DEL = byteArrayOf(58)
    const val ALGORITHM_MD5 = "MD5"
    const val ALGORITHM_SHA_256 = "SHA-256"
    const val ALGORITHM_SHA_384 = "SHA-384"
    const val ALGORITHM_SHA_512 = "SHA-512"
    const val ALGORITHM_SHA = "SHA"

    // MD5
    @Throws(NoSuchAlgorithmException::class)
    fun md5(data: ByteArray?): String {
        return hash(data, ALGORITHM_MD5, ENCODING_HEX)
    }

    // MD5
    @Throws(NoSuchAlgorithmException::class, IOException::class)
    fun md5(res: Resource): String {
        val `is`: InputStream = res.getInputStream()
        return try {
            DigestUtils.md5Hex(`is`)
        } finally {
            IOUtil.close(`is`)
        }
    }

    @Throws(NoSuchAlgorithmException::class)
    fun md5(str: String?): String {
        return hash(str, ALGORITHM_MD5, ENCODING_HEX, CharsetUtil.UTF8)
    }

    @Throws(NoSuchAlgorithmException::class)
    fun md5(str: String?, charset: Charset?): String {
        return hash(str, ALGORITHM_MD5, ENCODING_HEX, charset)
    }

    // SHA
    @Throws(NoSuchAlgorithmException::class)
    fun sha(data: ByteArray?): String {
        return hash(data, ALGORITHM_SHA, ENCODING_HEX)
    }

    @Throws(NoSuchAlgorithmException::class)
    fun sha(str: String?): String {
        return hash(str, ALGORITHM_SHA, ENCODING_HEX, CharsetUtil.UTF8)
    }

    @Throws(NoSuchAlgorithmException::class)
    fun sha(str: String?, charset: Charset?): String {
        return hash(str, ALGORITHM_SHA, ENCODING_HEX, charset)
    }

    // SHA256
    @Throws(NoSuchAlgorithmException::class)
    fun sha256(data: ByteArray?): String {
        return hash(data, ALGORITHM_SHA_256, ENCODING_HEX)
    }

    @Throws(NoSuchAlgorithmException::class)
    fun sha256(str: String?): String {
        return hash(str, ALGORITHM_SHA_256, ENCODING_HEX, CharsetUtil.UTF8)
    }

    @Throws(NoSuchAlgorithmException::class)
    fun sha256(str: String?, charset: Charset?): String {
        return hash(str, ALGORITHM_SHA_256, ENCODING_HEX, charset)
    }

    // SHA384
    @Throws(NoSuchAlgorithmException::class)
    fun sha384(data: ByteArray?): String {
        return hash(data, ALGORITHM_SHA_384, ENCODING_HEX)
    }

    @Throws(NoSuchAlgorithmException::class)
    fun sha384(str: String?): String {
        return hash(str, ALGORITHM_SHA_384, ENCODING_HEX, CharsetUtil.UTF8)
    }

    @Throws(NoSuchAlgorithmException::class)
    fun sha384(str: String?, charset: Charset?): String {
        return hash(str, ALGORITHM_SHA_384, ENCODING_HEX, charset)
    }

    // SHA384
    @Throws(NoSuchAlgorithmException::class)
    fun sha512(data: ByteArray?): String {
        return hash(data, ALGORITHM_SHA_512, ENCODING_HEX)
    }

    @Throws(NoSuchAlgorithmException::class)
    fun sha512(str: String?): String {
        return hash(str, ALGORITHM_SHA_512, ENCODING_HEX, CharsetUtil.UTF8)
    }

    @Throws(NoSuchAlgorithmException::class)
    fun sha512(str: String?, charset: Charset?): String {
        return hash(str, ALGORITHM_SHA_512, ENCODING_HEX, charset)
    }

    @Throws(NoSuchAlgorithmException::class)
    fun hash(str: String?, nonce: String?, algorithm: String?, encoding: CharArray): String {
        val md: MessageDigest = MessageDigest.getInstance(algorithm)
        md.reset()
        md.update(toBytes(str, CharsetUtil.UTF8))
        md.update(DEL)
        md.update(toBytes(nonce, CharsetUtil.UTF8))
        return String(enc(md.digest(), encoding)) // no charset needed because all characters are below us-ascii (hex)
    }

    @Throws(NoSuchAlgorithmException::class)
    fun hash(input: String?, algorithm: String?, numIterations: Int): String? {
        return hash(input, algorithm, numIterations, ENCODING_HEXUC)
    }

    @Throws(NoSuchAlgorithmException::class)
    fun hash(str: String?, algorithm: String?, numIterations: Int, encoding: CharArray): String? {
        var str = str
        try {
            val md: MessageDigest = MessageDigest.getInstance(algorithm)
            var mdc: MessageDigest
            for (i in 0 until numIterations) {
                mdc = md.clone() as MessageDigest
                mdc.reset()
                mdc.update(toBytes(str, CharsetUtil.UTF8))
                str = String(enc(mdc.digest(), encoding))
            }
            return str
        } catch (e: CloneNotSupportedException) {
        }

        // if not possible to clone the MessageDigest create always a new instance
        for (i in 0 until numIterations) {
            str = hash(str, algorithm, encoding, CharsetUtil.UTF8)
        }
        return str
    }

    @Throws(NoSuchAlgorithmException::class)
    fun hash(str: String?, algorithm: String?, encoding: CharArray, charset: Charset?): String {
        return hash(toBytes(str, charset), algorithm, encoding)
    }

    @Throws(NoSuchAlgorithmException::class)
    fun hash(data: ByteArray?, algorithm: String?, encoding: CharArray): String {
        val md: MessageDigest = MessageDigest.getInstance(algorithm)
        md.reset()
        md.update(data)
        return String(enc(md.digest(), encoding)) // no charset needed because all characters are below us-ascii (hex)
    }

    private fun toBytes(str: String?, charset: Charset?): ByteArray? {
        return str?.getBytes(charset)
    }

    fun toHexString(data: ByteArray?, upperCase: Boolean): String {
        return String(enc(data, if (upperCase) ENCODING_HEXUC else ENCODING_HEX))
    }

    private fun enc(data: ByteArray?, enc: CharArray): CharArray {
        val len = data!!.size
        val out = CharArray(len shl 1)
        // two characters form the hex value.
        var i = 0
        var j = 0
        while (i < len) {
            out[j++] = enc[0xF0 and data[i].toInt() ushr 4]
            out[j++] = enc[0x0F and data[i].toInt()]
            i++
        }
        return out
    }
}