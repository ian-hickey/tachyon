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

class SHA1 {
    // members
    private val m_state: IntArray?
    private var m_lCount: Long = 0
    private var m_digestBits: ByteArray?
    private val m_block: IntArray?
    private var m_nBlockIndex = 0

    /**
     *
     * clears all data, use reset() to start again
     *
     */
    fun clear() {
        var nI: Int
        nI = 0
        while (nI < m_state!!.size) {
            m_state[nI] = 0
            nI++
        }
        m_lCount = 0
        nI = 0
        while (nI < m_digestBits!!.size) {
            m_digestBits!![nI] = 0
            nI++
        }
        nI = 0
        while (nI < m_block!!.size) {
            m_block[nI] = 0
            nI++
        }
        m_nBlockIndex = 0
    }

    // some helper methods...
    fun rol(nValue: Int,
            nBits: Int): Int {
        return nValue shl nBits or (nValue ushr 32 - nBits)
    }

    fun blk0(nI: Int): Int {
        return rol(m_block!![nI], 24) and -0xff0100 or
                (rol(m_block[nI], 8) and 0x00ff00ff).also { m_block[nI] = it }
    }

    fun blk(nI: Int): Int {
        return rol(m_block!![nI + 13 and 15] xor m_block[nI + 8 and 15] xor
                m_block[nI + 2 and 15] xor m_block[nI and 15], 1).also { m_block[nI and 15] = it }
    }

    fun r0(data: IntArray?,
           nV: Int,
           nW: Int,
           nX: Int,
           nY: Int,
           nZ: Int,
           nI: Int) {
        data!![nZ] += (data!![nW] and (data[nX] xor data[nY]) xor data[nY]) +
                blk0(nI) +
                0x5a827999 +
                rol(data[nV], 5)
        data[nW] = rol(data[nW], 30)
    }

    fun r1(data: IntArray?,
           nV: Int,
           nW: Int,
           nX: Int,
           nY: Int,
           nZ: Int,
           nI: Int) {
        data!![nZ] += (data!![nW] and (data[nX] xor data[nY]) xor data[nY]) +
                blk(nI) +
                0x5a827999 +
                rol(data[nV], 5)
        data[nW] = rol(data[nW], 30)
    }

    fun r2(data: IntArray?,
           nV: Int,
           nW: Int,
           nX: Int,
           nY: Int,
           nZ: Int,
           nI: Int) {
        data!![nZ] += (data!![nW] xor data[nX] xor data[nY]) +
                blk(nI) +
                0x6eD9eba1 +
                rol(data[nV], 5)
        data[nW] = rol(data[nW], 30)
    }

    fun r3(data: IntArray?,
           nV: Int,
           nW: Int,
           nX: Int,
           nY: Int,
           nZ: Int,
           nI: Int) {
        data!![nZ] += (data!![nW] or data[nX] and data[nY] or (data[nW] and data[nX])) +
                blk(nI) +
                -0x70e44324 +
                rol(data[nV], 5)
        data[nW] = rol(data[nW], 30)
    }

    fun r4(data: IntArray?,
           nV: Int,
           nW: Int,
           nX: Int,
           nY: Int,
           nZ: Int,
           nI: Int) {
        data!![nZ] += (data!![nW] xor data[nX] xor data[nY]) +
                blk(nI) +
                -0x359d3e2a +
                rol(data[nV], 5)
        data[nW] = rol(data[nW], 30)
    }

    fun transform() {
        val dd = IntArray(5)
        dd[0] = m_state!![0]
        dd[1] = m_state[1]
        dd[2] = m_state[2]
        dd[3] = m_state[3]
        dd[4] = m_state[4]
        r0(dd, 0, 1, 2, 3, 4, 0)
        r0(dd, 4, 0, 1, 2, 3, 1)
        r0(dd, 3, 4, 0, 1, 2, 2)
        r0(dd, 2, 3, 4, 0, 1, 3)
        r0(dd, 1, 2, 3, 4, 0, 4)
        r0(dd, 0, 1, 2, 3, 4, 5)
        r0(dd, 4, 0, 1, 2, 3, 6)
        r0(dd, 3, 4, 0, 1, 2, 7)
        r0(dd, 2, 3, 4, 0, 1, 8)
        r0(dd, 1, 2, 3, 4, 0, 9)
        r0(dd, 0, 1, 2, 3, 4, 10)
        r0(dd, 4, 0, 1, 2, 3, 11)
        r0(dd, 3, 4, 0, 1, 2, 12)
        r0(dd, 2, 3, 4, 0, 1, 13)
        r0(dd, 1, 2, 3, 4, 0, 14)
        r0(dd, 0, 1, 2, 3, 4, 15)
        r1(dd, 4, 0, 1, 2, 3, 16)
        r1(dd, 3, 4, 0, 1, 2, 17)
        r1(dd, 2, 3, 4, 0, 1, 18)
        r1(dd, 1, 2, 3, 4, 0, 19)
        r2(dd, 0, 1, 2, 3, 4, 20)
        r2(dd, 4, 0, 1, 2, 3, 21)
        r2(dd, 3, 4, 0, 1, 2, 22)
        r2(dd, 2, 3, 4, 0, 1, 23)
        r2(dd, 1, 2, 3, 4, 0, 24)
        r2(dd, 0, 1, 2, 3, 4, 25)
        r2(dd, 4, 0, 1, 2, 3, 26)
        r2(dd, 3, 4, 0, 1, 2, 27)
        r2(dd, 2, 3, 4, 0, 1, 28)
        r2(dd, 1, 2, 3, 4, 0, 29)
        r2(dd, 0, 1, 2, 3, 4, 30)
        r2(dd, 4, 0, 1, 2, 3, 31)
        r2(dd, 3, 4, 0, 1, 2, 32)
        r2(dd, 2, 3, 4, 0, 1, 33)
        r2(dd, 1, 2, 3, 4, 0, 34)
        r2(dd, 0, 1, 2, 3, 4, 35)
        r2(dd, 4, 0, 1, 2, 3, 36)
        r2(dd, 3, 4, 0, 1, 2, 37)
        r2(dd, 2, 3, 4, 0, 1, 38)
        r2(dd, 1, 2, 3, 4, 0, 39)
        r3(dd, 0, 1, 2, 3, 4, 40)
        r3(dd, 4, 0, 1, 2, 3, 41)
        r3(dd, 3, 4, 0, 1, 2, 42)
        r3(dd, 2, 3, 4, 0, 1, 43)
        r3(dd, 1, 2, 3, 4, 0, 44)
        r3(dd, 0, 1, 2, 3, 4, 45)
        r3(dd, 4, 0, 1, 2, 3, 46)
        r3(dd, 3, 4, 0, 1, 2, 47)
        r3(dd, 2, 3, 4, 0, 1, 48)
        r3(dd, 1, 2, 3, 4, 0, 49)
        r3(dd, 0, 1, 2, 3, 4, 50)
        r3(dd, 4, 0, 1, 2, 3, 51)
        r3(dd, 3, 4, 0, 1, 2, 52)
        r3(dd, 2, 3, 4, 0, 1, 53)
        r3(dd, 1, 2, 3, 4, 0, 54)
        r3(dd, 0, 1, 2, 3, 4, 55)
        r3(dd, 4, 0, 1, 2, 3, 56)
        r3(dd, 3, 4, 0, 1, 2, 57)
        r3(dd, 2, 3, 4, 0, 1, 58)
        r3(dd, 1, 2, 3, 4, 0, 59)
        r4(dd, 0, 1, 2, 3, 4, 60)
        r4(dd, 4, 0, 1, 2, 3, 61)
        r4(dd, 3, 4, 0, 1, 2, 62)
        r4(dd, 2, 3, 4, 0, 1, 63)
        r4(dd, 1, 2, 3, 4, 0, 64)
        r4(dd, 0, 1, 2, 3, 4, 65)
        r4(dd, 4, 0, 1, 2, 3, 66)
        r4(dd, 3, 4, 0, 1, 2, 67)
        r4(dd, 2, 3, 4, 0, 1, 68)
        r4(dd, 1, 2, 3, 4, 0, 69)
        r4(dd, 0, 1, 2, 3, 4, 70)
        r4(dd, 4, 0, 1, 2, 3, 71)
        r4(dd, 3, 4, 0, 1, 2, 72)
        r4(dd, 2, 3, 4, 0, 1, 73)
        r4(dd, 1, 2, 3, 4, 0, 74)
        r4(dd, 0, 1, 2, 3, 4, 75)
        r4(dd, 4, 0, 1, 2, 3, 76)
        r4(dd, 3, 4, 0, 1, 2, 77)
        r4(dd, 2, 3, 4, 0, 1, 78)
        r4(dd, 1, 2, 3, 4, 0, 79)
        m_state[0] += dd[0]
        m_state[1] += dd[1]
        m_state[2] += dd[2]
        m_state[3] += dd[3]
        m_state[4] += dd[4]
    }

    /**
     *
     * initializes or resets the hasher for a new session respectively
     *
     */
    fun reset() {
        m_state!![0] = 0x67452301
        m_state[1] = -0x10325477
        m_state[2] = -0x67452302
        m_state[3] = 0x10325476
        m_state[4] = -0x3c2d1e10
        m_lCount = 0
        m_digestBits = ByteArray(20)
        m_nBlockIndex = 0
    }

    /**
     *
     * adds a single byte to the digest
     *
     */
    fun update(bB: Byte) {
        val nMask = m_nBlockIndex and 3 shl 3
        m_lCount += 8
        m_block!![m_nBlockIndex shr 2] = m_block[m_nBlockIndex shr 2] and (0xff shl nMask).inv()
        m_block[m_nBlockIndex shr 2] = m_block[m_nBlockIndex shr 2] or (bB and 0xff shl nMask)
        m_nBlockIndex++
        if (m_nBlockIndex == 64) {
            transform()
            m_nBlockIndex = 0
        }
    }

    /**
     *
     * adds a byte array to the digest
     *
     */
    fun update(data: ByteArray?) {
        for (nI in data.indices) update(data!![nI])
    }

    /**
     *
     * adds an ASCII string to the digest
     *
     */
    fun update(sData: String?) {
        for (nI in 0 until sData!!.length()) update((sData.charAt(nI) and 0x0ff) as Byte)
    }

    /**
     *
     * finalizes the digest
     *
     */
    @Override
    fun finalize() {
        var nI: Int
        val bits = ByteArray(8)
        nI = 0
        while (nI < 8) {
            bits[nI] = (m_lCount ushr (7 - nI shl 3) and 0xff).toByte()
            nI++
        }
        update(128.toByte())
        while (m_nBlockIndex != 56) update(0.toByte())
        nI = 0
        while (nI < bits.size) {
            update(bits[nI])
            nI++
        }
        nI = 0
        while (nI < 20) {
            m_digestBits!![nI] = (m_state!![nI shr 2] shr (3 - (nI and 3) shl 3) and 0xff).toByte()
            nI++
        }
    }

    /**
     *
     * gets the digest
     *
     * @return the digst bytes as an array if DIGEST_SIZE bytes
     */
    fun getDigest(): ByteArray? {

        // deliver a _copy_
        val result = ByteArray(DIGEST_SIZE)
        System.arraycopy(m_digestBits, 0, result, 0, DIGEST_SIZE)
        return result
    }

    /**
     *
     * makes a binhex string representation of the current digest
     *
     * @return the string representation
     */
    @Override
    override fun toString(): String {
        val buf = StringBuilder(DIGEST_SIZE * 2)
        for (nI in 0 until DIGEST_SIZE) {
            buf.append(HEXTAB.charAt(m_digestBits!![nI] ushr 4 and 0x0f))
            buf.append(HEXTAB.charAt(m_digestBits!![nI] and 0x0f))
        }
        return buf.toString()
    }

    /**
     *
     * runs a selftest
     *
     * @return true: selftest passed / false: selftest failed
     */
    fun selfTest(): Boolean {
        val tester = SHA1()
        tester.update(SELFTEST_MESSAGE)
        tester.finalize()
        val digest = tester.getDigest()
        tester.clear()
        for (nI in 0 until DIGEST_SIZE) if (digest!![nI] != SELFTEST_DIGEST!![nI]) return false

        // test passed
        return true
    }

    companion object {
        /** size of a SHA-1 digest in octets  */
        const val DIGEST_SIZE = 20

        // we need this table for the following method
        private val HEXTAB: String? = "0123456789abcdef"

        // references for the selftest
        private val SELFTEST_MESSAGE: String? = "abcdbcdecdefdefgefghfghighijhijkijkljklmklmnlmnomnopnopq"
        private val SELFTEST_DIGEST: ByteArray? = byteArrayOf(
                0x84.toByte(), 0x98.toByte(), 0x3e.toByte(), 0x44.toByte(), 0x1c.toByte(),
                0x3b.toByte(), 0xd2.toByte(), 0x6e.toByte(), 0xba.toByte(), 0xae.toByte(),
                0x4a.toByte(), 0xa1.toByte(), 0xf9.toByte(), 0x51.toByte(), 0x29.toByte(),
                0xE5.toByte(), 0xe5.toByte(), 0x46.toByte(), 0x70.toByte(), 0xf1.toByte()
        )
    }

    /**
     *
     * constructor
     *
     */
    init {
        m_state = IntArray(5)
        m_block = IntArray(16)
        m_digestBits = ByteArray(DIGEST_SIZE)
        reset()
    }
}