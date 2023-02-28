package tachyon.runtime.crypt

import kotlin.Throws
import kotlin.jvm.Synchronized
import tachyon.commons.io.SystemUtil.Caller
import kotlin.jvm.Transient
import kotlin.jvm.JvmOverloads
import kotlin.jvm.Volatile
import tachyon.commons.collection.concurrent.ConcurrentHashMapNullSupport.EntrySet
import tachyon.commons.collection.concurrent.ConcurrentHashMapNullSupport.EntryIterator
import tachyon.commons.collection.LongKeyList.Pair
import tachyon.commons.collection.AbstractCollection
import tachyon.runtime.type.Array
import java.sql.Array
import tachyon.commons.lang.Pair
import tachyon.runtime.exp.CatchBlockImpl.Pair
import tachyon.runtime.type.util.ListIteratorImpl
import tachyon.runtime.type.Lambda
import java.util.Random
import tachyon.runtime.config.Constants
import tachyon.runtime.engine.Request
import tachyon.runtime.engine.ExecutionLogSupport.Pair
import tachyon.runtime.functions.other.NullValue
import tachyon.runtime.functions.string.Val
import tachyon.runtime.reflection.Reflector.JavaAnnotation
import tachyon.transformer.cfml.evaluator.impl.Output
import tachyon.transformer.cfml.evaluator.impl.Property
import tachyon.transformer.bytecode.statement.Condition.Pair

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
class BlowfishCBC : BlowfishECB {
    // here we hold the CBC IV
    var m_lCBCIV: Long = 0

    /**
     * get the current CBC IV (for cipher resets)
     *
     * @return current CBC IV
     */
    fun getCBCIV(): Long {
        return m_lCBCIV
    }

    /**
     * get the current CBC IV (for cipher resets)
     *
     * @param dest wher eto put current CBC IV in network byte ordered array
     */
    fun getCBCIV(dest: ByteArray?) {
        BinConverter.longToByteArray(m_lCBCIV, dest, 0)
    }

    /**
     * set the current CBC IV (for cipher resets)
     *
     * @param lNewCBCIV the new CBC IV
     */
    fun setCBCIV(lNewCBCIV: Long) {
        m_lCBCIV = lNewCBCIV
    }

    /**
     *
     * set the current CBC IV (for cipher resets)
     *
     * @param newCBCIV the new CBC IV in network byte ordered array
     */
    fun setCBCIV(newCBCIV: ByteArray?) {
        m_lCBCIV = BinConverter.byteArrayToLong(newCBCIV, 0)
    }

    /**
     *
     * constructor, stores a zero CBC IV
     *
     * @param bfkey key material, up to MAXKEYLENGTH bytes
     */
    constructor(bfkey: ByteArray?) : super(bfkey) {

        // store zero CBCB IV
        setCBCIV(0)
    }

    /**
     *
     * constructor
     *
     * @param bfkey key material, up to MAXKEYLENGTH bytes
     *
     * @param lInitCBCIV the CBC IV
     */
    constructor(bfkey: ByteArray?,
                lInitCBCIV: Long) : super(bfkey) {

        // store the CBCB IV
        setCBCIV(lInitCBCIV)
    }

    /**
     *
     * constructor
     *
     * @param bfkey key material, up to MAXKEYLENGTH bytes
     *
     * @param lInitCBCIV the CBC IV (array with min. BLOCKSIZE bytes)
     */
    constructor(bfkey: ByteArray?,
                initCBCIV: ByteArray?) : super(bfkey) {

        // store the CBCB IV
        setCBCIV(initCBCIV)
    }

    /**
     *
     * cleans up all critical internals,
     *
     * call this if you don't need an instance anymore
     *
     */
    @Override
    override fun cleanUp() {
        m_lCBCIV = 0
        super.cleanUp()
    }

    // internal routine to encrypt a block in CBC mode
    @Override
    protected override fun encryptBlock(lPlainblock: Long): Long {

        // chain with the CBC IV
        var lPlainblock = lPlainblock
        lPlainblock = lPlainblock xor m_lCBCIV

        // encrypt the block
        lPlainblock = super.encryptBlock(lPlainblock)

        // the encrypted block is the new CBC IV
        return lPlainblock.also { m_lCBCIV = it }
    }

    // internal routine to decrypt a block in CBC mode
    @Override
    protected override fun decryptBlock(lCipherblock: Long): Long {

        // save the current block
        var lCipherblock = lCipherblock
        val lTemp = lCipherblock

        // decrypt the block
        lCipherblock = super.decryptBlock(lCipherblock)

        // dechain the block
        lCipherblock = lCipherblock xor m_lCBCIV

        // set the new CBC IV
        m_lCBCIV = lTemp

        // return the decrypted block
        return lCipherblock
    }

    /**
     *
     * encrypts a byte buffer (should be aligned to an 8 byte border)
     *
     * to another buffer (of the same size or bigger)
     *
     * @param inbuffer buffer with plaintext data
     *
     * @param outbuffer buffer to get the ciphertext data
     */
    @Override
    override fun encrypt(inbuffer: ByteArray?,
                         outbuffer: ByteArray?) {
        val nLen = inbuffer!!.size
        var lTemp: Long
        var nI = 0
        while (nI < nLen) {


            // encrypt a temporary 64bit block
            lTemp = BinConverter.byteArrayToLong(inbuffer, nI)
            lTemp = encryptBlock(lTemp)
            BinConverter.longToByteArray(lTemp, outbuffer, nI)
            nI += 8
        }
    }

    /**
     *
     * encrypts a byte buffer (should be aligned to an 8 byte border) to itself
     *
     * @param buffer buffer to encrypt
     */
    @Override
    override fun encrypt(buffer: ByteArray?) {
        val nLen = buffer!!.size
        var lTemp: Long
        var nI = 0
        while (nI < nLen) {


            // encrypt a temporary 64bit block
            lTemp = BinConverter.byteArrayToLong(buffer, nI)
            lTemp = encryptBlock(lTemp)
            BinConverter.longToByteArray(lTemp, buffer, nI)
            nI += 8
        }
    }

    /**
     *
     * encrypts an int buffer (should be aligned to an
     *
     * two integer border) to another int buffer (of the same
     *
     * size or bigger)
     *
     * @param inbuffer buffer with plaintext data
     *
     * @param outBuffer buffer to get the ciphertext data
     */
    @Override
    override fun encrypt(inbuffer: IntArray?,
                         outbuffer: IntArray?) {
        val nLen = inbuffer!!.size
        var lTemp: Long
        var nI = 0
        while (nI < nLen) {


            // encrypt a temporary 64bit block
            lTemp = BinConverter.intArrayToLong(inbuffer, nI)
            lTemp = encryptBlock(lTemp)
            BinConverter.longToIntArray(lTemp, outbuffer, nI)
            nI += 2
        }
    }

    /**
     *
     * encrypts an integer buffer (should be aligned to an
     *
     * @param buffer buffer to encrypt
     */
    @Override
    override fun encrypt(buffer: IntArray?) {
        val nLen = buffer!!.size
        var lTemp: Long
        var nI = 0
        while (nI < nLen) {


            // encrypt a temporary 64bit block
            lTemp = BinConverter.intArrayToLong(buffer, nI)
            lTemp = encryptBlock(lTemp)
            BinConverter.longToIntArray(lTemp, buffer, nI)
            nI += 2
        }
    }

    /**
     *
     * encrypts a long buffer to another long buffer (of the same size or bigger)
     *
     * @param inbuffer buffer with plaintext data
     *
     * @param outbuffer buffer to get the ciphertext data
     */
    @Override
    override fun encrypt(inbuffer: LongArray?,
                         outbuffer: LongArray?) {
        val nLen = inbuffer!!.size
        for (nI in 0 until nLen) outbuffer!![nI] = encryptBlock(inbuffer[nI])
    }

    /**
     *
     * encrypts a long buffer to itself
     *
     * @param buffer buffer to encrypt
     */
    @Override
    override fun encrypt(buffer: LongArray?) {
        val nLen = buffer!!.size
        for (nI in 0 until nLen) {
            buffer[nI] = encryptBlock(buffer[nI])
        }
    }

    /**
     *
     * decrypts a byte buffer (should be aligned to an 8 byte border)
     *
     * to another buffer (of the same size or bigger)
     *
     * @param inbuffer buffer with ciphertext data
     *
     * @param outBuffer buffer to get the plaintext data
     */
    @Override
    override fun decrypt(inbuffer: ByteArray?,
                         outbuffer: ByteArray?) {
        val nLen = inbuffer!!.size
        var lTemp: Long
        var nI = 0
        while (nI < nLen) {


            // decrypt a temporary 64bit block
            lTemp = BinConverter.byteArrayToLong(inbuffer, nI)
            lTemp = decryptBlock(lTemp)
            BinConverter.longToByteArray(lTemp, outbuffer, nI)
            nI += 8
        }
    }

    /**
     *
     * decrypts a byte buffer (should be aligned to an 8 byte border) to itself
     *
     * @param buffer buffer to decrypt
     */
    @Override
    override fun decrypt(buffer: ByteArray?) {
        val nLen = buffer!!.size
        var lTemp: Long
        var nI = 0
        while (nI < nLen) {


            // decrypt over a temporary 64bit block
            lTemp = BinConverter.byteArrayToLong(buffer, nI)
            lTemp = decryptBlock(lTemp)
            BinConverter.longToByteArray(lTemp, buffer, nI)
            nI += 8
        }
    }

    /**
     *
     * decrypts an integer buffer (should be aligned to an
     *
     * two integer border) to another int buffer (of the same size or bigger)
     *
     * @param inbuffer buffer with ciphertext data
     *
     * @param outbuffer buffer to get the plaintext data
     */
    @Override
    override fun decrypt(inbuffer: IntArray?,
                         outbuffer: IntArray?) {
        val nLen = inbuffer!!.size
        var lTemp: Long
        var nI = 0
        while (nI < nLen) {


            // decrypt a temporary 64bit block
            lTemp = BinConverter.intArrayToLong(inbuffer, nI)
            lTemp = decryptBlock(lTemp)
            BinConverter.longToIntArray(lTemp, outbuffer, nI)
            nI += 2
        }
    }

    /**
     *
     * decrypts an int buffer (should be aligned to a
     *
     * two integer border)
     *
     * @param buffer buffer to decrypt
     */
    @Override
    override fun decrypt(buffer: IntArray?) {
        val nLen = buffer!!.size
        var lTemp: Long
        var nI = 0
        while (nI < nLen) {


            // decrypt a temporary 64bit block
            lTemp = BinConverter.intArrayToLong(buffer, nI)
            lTemp = decryptBlock(lTemp)
            BinConverter.longToIntArray(lTemp, buffer, nI)
            nI += 2
        }
    }

    /**
     *
     * decrypts a long buffer to another long buffer (of the same size or bigger)
     *
     * @param inbuffer buffer with ciphertext data
     *
     * @param outbuffer buffer to get the plaintext data
     */
    @Override
    override fun decrypt(inbuffer: LongArray?,
                         outbuffer: LongArray?) {
        val nLen = inbuffer!!.size
        for (nI in 0 until nLen) outbuffer!![nI] = decryptBlock(inbuffer[nI])
    }

    /**
     *
     * decrypts a long buffer to itself
     *
     * @param buffer buffer to decrypt
     */
    @Override
    override fun decrypt(buffer: LongArray?) {
        val nLen = buffer!!.size
        for (nI in 0 until nLen) buffer[nI] = decryptBlock(buffer[nI])
    }
}