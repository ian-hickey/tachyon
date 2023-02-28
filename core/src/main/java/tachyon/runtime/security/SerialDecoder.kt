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
package tachyon.runtime.security

import java.util.Random

/**
 * support class for easy string encryption with the Blowfish algorithm, now in CBC mode with a
 * SHA-1 key setup and correct padding
 */
class SerialDecoder(sPassword: String?) {
    var m_bfish: BlowfishCBC?

    companion object {
        // one random generator for all simple callers...
        var m_rndGen: Random? = null

        // ...and created early
        init {
            m_rndGen = Random()
        }
    }

    /**
     *
     * decrypts a hexbin string (handling is case sensitive)
     *
     * @param sCipherText hexbin string to decrypt
     *
     * @return decrypted string (null equals an error)
     */
    fun decrypt(sCipherText: String?): String? {

        // get the number of estimated bytes in the string (cut off broken blocks)
        var nLen: Int = sCipherText!!.length() shr 1 and 7.inv()

        // does the given stuff make sense (at least the CBC IV)?
        if (nLen < BlowfishECB.BLOCKSIZE) return null

        // get the CBC IV
        val cbciv = ByteArray(BlowfishECB.BLOCKSIZE)
        var nNumOfBytes: Int = BinConverter.binHexToBytes(sCipherText,
                cbciv,
                0,
                0,
                BlowfishECB.BLOCKSIZE)
        if (nNumOfBytes < BlowfishECB.BLOCKSIZE) return null

        // (got it)
        m_bfish.setCBCIV(cbciv)

        // something left to decrypt?
        nLen -= BlowfishECB.BLOCKSIZE
        if (nLen == 0) return ""

        // get all data bytes now
        val buf = ByteArray(nLen)
        nNumOfBytes = BinConverter.binHexToBytes(sCipherText,
                buf,
                BlowfishECB.BLOCKSIZE * 2,
                0,
                nLen)

        // we cannot accept broken binhex sequences due to padding

        // and decryption
        if (nNumOfBytes < nLen) return null

        // decrypt the buffer
        m_bfish.decrypt(buf)

        // get the last padding byte
        var nPadByte: Int = buf[buf.size - 1] and 0x0ff

        // ( try to get all information if the padding doesn't seem to be correct)
        if (nPadByte > 8 || nPadByte < 0) nPadByte = 0

        // calculate the real size of this message
        nNumOfBytes -= nPadByte
        return if (nNumOfBytes < 0) "" else BinConverter.byteArrayToUNCString(buf, 0, nNumOfBytes)

        // success
    }

    /**
     *
     * destroys (clears) the encryption engine,
     *
     * after that the instance is not valid anymore
     *
     */
    fun destroy() {
        m_bfish.cleanUp()
    }

    /**
     *
     * constructor to set up a string as the key (oversized password will be cut)
     *
     * @param sPassword the password (treated as a real unicode array)
     */
    init {

        // hash down the password to a 160bit key
        val hasher = SHA1()
        hasher.update(sPassword)
        hasher.finalize()

        // setup the encryptor (use a dummy IV)
        m_bfish = BlowfishCBC(hasher.getDigest(), 0)
        hasher.clear()
    }
}