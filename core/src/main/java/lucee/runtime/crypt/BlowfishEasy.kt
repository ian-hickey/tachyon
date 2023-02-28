package lucee.runtime.crypt

import java.util.Random

//package maddany.crypto;
/*   Coding by maddany@madloutre.org

 *   01-01-2000

 *

 *   This program is free software; you can redistribute it and/or modify

 *   it under the terms of the GNU General Public License as published by

 *   the Free Software Foundation; either version 2 of the License, or

 *   (at your option) any later version.

 *

 *   This program is distributed in the hope that it will be useful,

 *   but WITHOUT ANY WARRANTY; without even the implied warranty of

 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the

 *   GNU General Public License for more details.

 *

 *   You should have received a copy of the GNU General Public License

 *   along with this program; if not, write to the Free Software

 *   Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.

 *

 */
class BlowfishEasy(sPassword: String?) {
    // the Blowfish CBC instance
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
     * encrypts a string (treated in UNICODE) using the
     *
     * standard Java random generator, which isn't that
     *
     * great for creating IVs
     *
     * @param sPlainText string to encrypt
     *
     * @return encrypted string in binhex format
     */
    fun encryptString(sPlainText: String?): String? {

        // get the IV
        var lCBCIV: Long
        synchronized(m_rndGen) { lCBCIV = m_rndGen.nextLong() }

        // map the call;
        return encStr(sPlainText, lCBCIV)
    }

    /**
     *
     * encrypts a string (treated in UNICODE)
     *
     * @param sPlainText string to encrypt
     *
     * @param rndGen random generator (usually a java.security.SecureRandom instance)
     *
     * @return encrypted string in binhex format
     */
    fun encryptString(sPlainText: String?,
                      rndGen: Random?): String? {

        // get the IV
        val lCBCIV: Long = rndGen.nextLong()

        // map the call;
        return encStr(sPlainText, lCBCIV)
    }

    // internal routine for string encryption
    private fun encStr(sPlainText: String?,
                       lNewCBCIV: Long): String? {

        // allocate the buffer (align to the next 8 byte border plus padding)
        val nStrLen: Int = sPlainText!!.length()
        val buf = ByteArray((nStrLen shl 1 and -0x8) + 8)

        // copy all bytes of the string into the buffer (use network byte order)
        var nI: Int
        var nPos = 0
        nI = 0
        while (nI < nStrLen) {
            val cActChar: Char = sPlainText.charAt(nI)
            buf[nPos++] = (cActChar.toInt() shr 8 and 0x0ff).toByte()
            buf[nPos++] = (cActChar.toInt() and 0x0ff).toByte()
            nI++
        }

        // pad the rest with the PKCS5 scheme
        val bPadVal = (buf.size - (nStrLen shl 1)).toByte()
        while (nPos < buf.size) buf[nPos++] = bPadVal

        // create the encryptor
        m_bfish!!.setCBCIV(lNewCBCIV)

        // encrypt the buffer
        m_bfish!!.encrypt(buf)

        // return the binhex string
        val newCBCIV = ByteArray(BlowfishECB.BLOCKSIZE)
        BinConverter.longToByteArray(lNewCBCIV,
                newCBCIV,
                0)
        return BinConverter.bytesToBinHex(newCBCIV,
                0,
                BlowfishECB.BLOCKSIZE) +
                BinConverter.bytesToBinHex(buf,
                        0,
                        buf.size)
    }

    /**
     *
     * decrypts a hexbin string (handling is case sensitive)
     *
     * @param sCipherText hexbin string to decrypt
     *
     * @return decrypted string (null equals an error)
     */
    fun decryptString(sCipherText: String?): String? {

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
        m_bfish!!.setCBCIV(cbciv)

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
        m_bfish!!.decrypt(buf)

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
        m_bfish!!.cleanUp()
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
        hasher!!.update(sPassword)
        hasher!!.finalize()

        // setup the encryptor (use a dummy IV)
        m_bfish = BlowfishCBC(hasher!!.getDigest(), 0)
        hasher!!.clear()
    }
}