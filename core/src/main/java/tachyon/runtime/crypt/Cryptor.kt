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
package tachyon.runtime.crypt

import java.security.Key

/**
 *
 */
object Cryptor {
    val DEFAULT_CHARSET: String? = "UTF-8"
    val DEFAULT_ENCODING: String? = "UU"
    const val DEFAULT_ITERATIONS = 1000 // minimum recommended per NIST
    private val secureRandom: SecureRandom? = SecureRandom()

    /**
     * @param input - the clear-text input to be encrypted, or the encrypted input to be decrypted
     * @param key - the encryption key
     * @param algorithm - algorithm in JCE scheme
     * @param ivOrSalt - Initialization Vector for algorithms with Feedback Mode that is not ECB, or
     * Salt for Password Based Encryption algorithms
     * @param iterations - number of Iterations for Password Based Encryption algorithms (recommended
     * minimum value is 1000)
     * @param doDecrypt - the Operation Type, pass false for Encrypt or true for Decrypt
     * @return
     * @throws PageException
     */
    @Throws(PageException::class)
    fun crypt(input: ByteArray?, key: String?, algorithm: String?, ivOrSalt: ByteArray?, iterations: Int, doDecrypt: Boolean, precise: Boolean): ByteArray? {
        return try {
            _crypt(input, key, algorithm, ivOrSalt, iterations, doDecrypt, precise)
        } // this is an ugly patch but it looks lime that ACF simply double to short keys
        catch (pe: PageException) {
            val msg: String = pe.getMessage()
            if (msg != null && key!!.length() === 4 && msg.indexOf(" 40 ") !== -1 && msg.indexOf(" 1024 ") !== -1) {
                return _crypt(input, key + key, algorithm, ivOrSalt, iterations, doDecrypt, precise)
            }
            if (msg != null && key!!.length() > 4 && key!!.length() % 4 === 0 && msg.indexOf("Illegal key size") !== -1) {
                return crypt(input, key.substring(0, key!!.length() - 4), algorithm, ivOrSalt, iterations, doDecrypt, precise)
            }
            throw pe
        }
    }

    @Throws(PageException::class)
    private fun _crypt(input: ByteArray?, key: String?, algorithm: String?, ivOrSalt: ByteArray?, iterations: Int, doDecrypt: Boolean, precise: Boolean): ByteArray? {
        var ivOrSalt = ivOrSalt
        var result: ByteArray? = null
        var secretKey: Key? = null
        var params: AlgorithmParameterSpec? = null
        var algo = algorithm
        var isFBM = false
        val isPBE: Boolean = StringUtil.startsWithIgnoreCase(algo, "PBE")
        var ivsLen = 0
        val algoDelimPos: Int = algorithm.indexOf('/')
        if (algoDelimPos > -1) {
            algo = algorithm.substring(0, algoDelimPos)
            isFBM = !StringUtil.startsWithIgnoreCase(algorithm.substring(algoDelimPos + 1), "ECB")
        }
        return try {
            val cipher: Cipher = Cipher.getInstance(algorithm)
            if (ivOrSalt == null) {
                if (isPBE || isFBM) {
                    ivsLen = cipher.getBlockSize()
                    ivOrSalt = ByteArray(ivsLen)
                    if (doDecrypt) System.arraycopy(input, 0, ivOrSalt, 0, ivsLen) else secureRandom.nextBytes(ivOrSalt)
                }
            }
            if (isPBE) {
                secretKey = SecretKeyFactory.getInstance(algorithm).generateSecret(PBEKeySpec(key.toCharArray()))
                params = PBEParameterSpec(ivOrSalt, if (iterations > 0) iterations else DEFAULT_ITERATIONS) // set Salt and Iterations for PasswordBasedEncryption
            } else {
                secretKey = SecretKeySpec(Coder.decode(Coder.ENCODING_BASE64, key, precise), algo)
                if (isFBM) params = IvParameterSpec(ivOrSalt) // set Initialization Vector for non-ECB Feedback Mode
            }
            if (doDecrypt) {
                cipher.init(Cipher.DECRYPT_MODE, secretKey, params)
                result = cipher.doFinal(input, ivsLen, input!!.size - ivsLen)
            } else {
                cipher.init(Cipher.ENCRYPT_MODE, secretKey, params)
                result = ByteArray(ivsLen + cipher.getOutputSize(input!!.size))
                if (ivsLen > 0) System.arraycopy(ivOrSalt, 0, result, 0, ivsLen)
                cipher.doFinal(input, 0, input!!.size, result, ivsLen)
            }
            result
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            throw Caster.toPageException(t)
        }
    }

    /**
     * an encrypt method that takes a byte-array for input and returns an encrypted byte-array
     */
    @Throws(PageException::class)
    fun encrypt(input: ByteArray?, key: String?, algorithm: String?, ivOrSalt: ByteArray?, iterations: Int, precise: Boolean): ByteArray? {
        return crypt(input, key, algorithm, ivOrSalt, iterations, false, precise)
    }

    /**
     * an encrypt method that takes a clear-text String for input and returns an encrypted, encoded,
     * String
     */
    @Throws(PageException::class)
    fun encrypt(input: String?, key: String?, algorithm: String?, ivOrSalt: ByteArray?, iterations: Int, encoding: String?, charset: String?, precise: Boolean): String? {
        var encoding = encoding
        var charset = charset
        return try {
            if (charset == null) charset = DEFAULT_CHARSET
            if (encoding == null) encoding = DEFAULT_ENCODING
            val baInput: ByteArray = input.getBytes(charset)
            val encrypted = encrypt(baInput, key, algorithm, ivOrSalt, iterations, precise)
            Coder.encode(encoding, encrypted)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            throw Caster.toPageException(t)
        }
    }

    /**
     * a decrypt method that takes an encrypted byte-array for input and returns an unencrypted
     * byte-array
     */
    @Throws(PageException::class)
    fun decrypt(input: ByteArray?, key: String?, algorithm: String?, ivOrSalt: ByteArray?, iterations: Int, precise: Boolean): ByteArray? {
        return crypt(input, key, algorithm, ivOrSalt, iterations, true, precise)
    }

    /**
     * a decrypt method that takes an encrypted, encoded, String for input and returns a clear-text
     * String
     */
    @Throws(PageException::class)
    fun decrypt(input: String?, key: String?, algorithm: String?, ivOrSalt: ByteArray?, iterations: Int, encoding: String?, charset: String?, precise: Boolean): String? {
        var encoding = encoding
        var charset = charset
        return try {
            if (charset == null) charset = DEFAULT_CHARSET
            if (encoding == null) encoding = DEFAULT_ENCODING
            val baInput: ByteArray = Coder.decode(encoding, input, precise)
            val decrypted = decrypt(baInput, key, algorithm, ivOrSalt, iterations, precise)
            String(decrypted, charset)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            throw Caster.toPageException(t)
        }
    }
}