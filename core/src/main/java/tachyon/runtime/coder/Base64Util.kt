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

object Base64Util {
    private val base64Encoder: Base64.Encoder? = Base64.getEncoder()
    private val base64Decoder: Base64.Decoder? = Base64.getDecoder()
    private val base64UrlEncoder: Base64.Encoder? = Base64.getUrlEncoder().withoutPadding()
    private val base64UrlDecoder: Base64.Decoder? = Base64.getUrlDecoder()
    private var base64Alphabet: ByteArray?
    private var lookUpBase64Alphabet: ByteArray?

    /**
     * @param arrayOctect byte array to check
     * @return true if base64
     */
    fun isBase64(arrayOctect: ByteArray?): Boolean {
        val length = arrayOctect!!.size
        if (length == 0) return true
        for (i in 0 until length) {
            if (!isBase64(arrayOctect[i])) return false
        }
        return true
    }

    /**
     * @param octect byte to check
     * @return true if base64
     */
    fun isBase64(octect: Byte): Boolean {
        return octect.toInt() == 61 || base64Alphabet!![octect.toInt()] != -1
    }

    /**
     * @param isValidString string to check
     * @return true if base64
     */
    fun isBase64(isValidString: String?): Boolean {
        return isBase64(isValidString.getBytes(CharsetUtil.UTF8))
    }

    /**
     * creates a new random UUID and encodes it as a URL-safe Bas64 string
     *
     * @return a 22 character string
     */
    fun createUuidAsBase64(): String? {
        return encodeUuidAsBase64(UUID.randomUUID())
    }

    /**
     * encodes a 36 character long UUID string as a URL-safe Base64 string
     *
     * @param uuid a UUID in the format xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
     * @return a 22 character string
     */
    fun encodeUuidAsBase64(uuid: String?): String? {
        return encodeUuidAsBase64(UUID.fromString(uuid))
    }

    /**
     * encodes a UUID object as a URL-safe Base64 string
     *
     * @param uuid a java.util.UUID object
     * @return a 22 character string
     */
    fun encodeUuidAsBase64(uuid: UUID?): String? {
        val bb: ByteBuffer = ByteBuffer.allocate(16)
        bb.putLong(uuid.getMostSignificantBits())
        bb.putLong(uuid.getLeastSignificantBits())
        val barr: ByteArray = bb.array()
        val benc: ByteArray = base64UrlEncoder.encode(barr)
        return String(benc, 0, benc.size, StandardCharsets.US_ASCII)
    }

    /**
     * decodes a 22 character long Base64 string to a UUID string
     *
     * @param base64 a 22 character long string
     * @return a 36 character UUID string in the format xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
     */
    fun decodeBase64AsUuid(base64: String?): String? {
        val barr: ByteArray = base64.getBytes(StandardCharsets.US_ASCII)
        val bdec: ByteArray = base64UrlDecoder.decode(barr)
        val buffer: ByteBuffer = ByteBuffer.wrap(bdec)
        val uuid = UUID(buffer.getLong(), buffer.getLong())
        return uuid.toString()
    }

    /**
     * encodes a number as a Base64 string, e.g. 9876543210 => AkywFuo
     *
     * @param number a string that represents a whole number
     * @return a URL-safe Base64 string
     */
    fun encodeNumberAsBase64(number: String?): String? {
        val bint = BigInteger(number)
        val barr: ByteArray = bint.toByteArray()
        val benc: ByteArray = base64UrlEncoder.encode(barr)
        return String(benc, 0, benc.size, StandardCharsets.US_ASCII)
    }

    /**
     * decodes a Base64 string to a string that represents a number, e.g. AkywFuo => 9876543210
     *
     * @param base64 the Base64 string
     * @return a string representation of the decoded number
     */
    fun decodeBase64AsNumber(base64: String?): String? {
        val barr: ByteArray = base64.getBytes(StandardCharsets.US_ASCII)
        val bdec: ByteArray = base64UrlDecoder.decode(barr)
        val buffer: ByteBuffer = ByteBuffer.wrap(bdec)
        val bint = BigInteger(buffer.array())
        return bint.toString()
    }

    fun base64Encode(barr: ByteArray?, urlSafe: Boolean): ByteArray? {
        val encoder: Base64.Encoder = if (urlSafe) base64UrlEncoder else base64Encoder
        return encoder.encode(barr)
    }

    fun base64Decode(b64: String?, urlSafe: Boolean): ByteArray? {
        val decoder: Base64.Decoder = if (urlSafe) base64UrlDecoder else base64Decoder
        return decoder.decode(b64)
    }

    /** Initializations  */
    init {
        base64Alphabet = ByteArray(255)
        lookUpBase64Alphabet = ByteArray(64)
        for (i in 0..254) base64Alphabet!![i] = -1
        for (i in 90 downTo 65) base64Alphabet!![i] = (i - 65) as Byte
        for (i in 122 downTo 97) base64Alphabet!![i] = (i - 97 + 26) as Byte
        for (i in 57 downTo 48) base64Alphabet!![i] = (i - 48 + 52) as Byte
        base64Alphabet!![43] = 62
        base64Alphabet!![47] = 63
        for (i in 0..25) lookUpBase64Alphabet!![i] = (65 + i).toByte()
        val i = 26
        run {
            val j = 0
            while (tachyon.runtime.coder.i <= 51) {
                lookUpBase64Alphabet!![tachyon.runtime.coder.i] = (97 + tachyon.runtime.coder.j) as Byte
                tachyon.runtime.coder.i++
                tachyon.runtime.coder.j++
            }
        }
        tachyon.runtime.coder.i = 52
        val j = 0
        while (tachyon.runtime.coder.i <= 61) {
            lookUpBase64Alphabet!![tachyon.runtime.coder.i] = (48 + tachyon.runtime.coder.j) as Byte
            tachyon.runtime.coder.i++
            tachyon.runtime.coder.j++
        }
        lookUpBase64Alphabet!![62] = 43
        lookUpBase64Alphabet!![63] = 47
    }
}