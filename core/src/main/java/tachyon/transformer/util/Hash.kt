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
package tachyon.transformer.util

import java.security.MessageDigest

/**
 * Class Hash produces a MessageDigest hash for a given string.
 */
class Hash(plainText: String?, algorithm: String?) {
    private var plainText: String? = null
    private var algorithm: String? = null

    /**
     * @see java.lang.Object.toString
     */
    @Override
    override fun toString(): String {
        var hashText: String? = null
        try {
            hashText = getHashText(plainText, algorithm)
        } catch (nsae: NoSuchAlgorithmException) {
            LogUtil.log(Hash::class.java.getName(), nsae)
        }
        return hashText!!
    }

    /**
     * Returns the algorithm.
     *
     * @return String
     */
    fun getAlgorithm(): String? {
        return algorithm
    }

    /**
     * Returns the plainText.
     *
     * @return String
     */
    fun getPlainText(): String? {
        return plainText
    }

    /**
     * Sets the algorithm.
     *
     * @param algorithm The algorithm to set
     */
    fun setAlgorithm(algorithm: String?) {
        this.algorithm = algorithm
    }

    /**
     * Sets the plainText.
     *
     * @param plainText The plainText to set
     */
    fun setPlainText(plainText: String?) {
        this.plainText = plainText
    }

    companion object {
        /**
         * Method getHashText.
         *
         * @param plainText
         * @param algorithm The algorithm to use like MD2, MD5, SHA-1, etc.
         * @return String
         * @throws NoSuchAlgorithmException
         */
        @Throws(NoSuchAlgorithmException::class)
        fun getHashText(plainText: String?, algorithm: String?): String? {
            var plainText = plainText
            val mdAlgorithm: MessageDigest = MessageDigest.getInstance(algorithm)
            mdAlgorithm.update(plainText.getBytes())
            val digest: ByteArray = mdAlgorithm.digest()
            val hexString = StringBuffer()
            for (i in digest.indices) {
                plainText = Integer.toHexString(0xFF and digest[i].toInt())
                if (plainText.length() < 2) {
                    plainText = "0$plainText"
                }
                hexString.append(plainText)
            }
            return hexString.toString()
        }
    }

    /**
     * Method Hash.
     *
     * @param plainText
     * @param algorithm The algorithm to use like MD2, MD5, SHA-1, etc.
     */
    init {
        setPlainText(plainText)
        setAlgorithm(algorithm)
    }
}