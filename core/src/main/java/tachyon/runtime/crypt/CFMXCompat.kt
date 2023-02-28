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

import tachyon.commons.lang.StringUtil

class CFMXCompat {
    private var m_Key: String? = null
    private var m_LFSR_A = 0x13579bdf
    private var m_LFSR_B = 0x2468ace0
    private var m_LFSR_C = -0x2468acf
    private val m_Mask_A = -0x7fffff9e
    private val m_Mask_B = 0x40000020
    private val m_Mask_C = 0x10000002
    private val m_Rot0_A = 0x7fffffff
    private val m_Rot0_B = 0x3fffffff
    private val m_Rot0_C = 0xfffffff
    private val m_Rot1_A = -0x80000000
    private val m_Rot1_B = -0x40000000
    private val m_Rot1_C = -0x10000000
    fun transformString(key: String?, inBytes: ByteArray?): ByteArray? {
        setKey(key)
        val length = inBytes!!.size
        val outBytes = ByteArray(length)
        for (i in 0 until length) {
            outBytes[i] = transformByte(inBytes[i])
        }
        return outBytes
    }

    private fun transformByte(target: Byte): Byte {
        var target = target
        var crypto: Byte = 0
        var b = m_LFSR_B and 1
        var c = m_LFSR_C and 1
        for (i in 0..7) {
            if (0 != m_LFSR_A and 1) {
                m_LFSR_A = m_LFSR_A xor m_Mask_A ushr 1 or m_Rot1_A
                if (0 != m_LFSR_B and 1) {
                    m_LFSR_B = m_LFSR_B xor m_Mask_B ushr 1 or m_Rot1_B
                    b = 1
                } else {
                    m_LFSR_B = m_LFSR_B ushr 1 and m_Rot0_B
                    b = 0
                }
            } else {
                m_LFSR_A = m_LFSR_A ushr 1 and m_Rot0_A
                if (0 != m_LFSR_C and 1) {
                    m_LFSR_C = m_LFSR_C xor m_Mask_C ushr 1 or m_Rot1_C
                    c = 1
                } else {
                    m_LFSR_C = m_LFSR_C ushr 1 and m_Rot0_C
                    c = 0
                }
            }
            crypto = (crypto shl 1 or b xor c)
        }
        target = target xor crypto
        return target
    }

    private fun setKey(key: String?) {
        var key = key
        var i = 0
        m_Key = key
        if (StringUtil.isEmpty(key)) key = "Default Seed"
        val Seed = CharArray(if (key!!.length() >= 12) key.length() else 12)
        m_Key.getChars(0, m_Key!!.length(), Seed, 0)
        val originalLength: Int = m_Key!!.length()
        i = 0
        while (originalLength + i < 12) {
            Seed[originalLength + i] = Seed[i]
            i++
        }
        i = 0
        while (i < 4) {
            m_LFSR_A = 8.let { m_LFSR_A = m_LFSR_A shl it; m_LFSR_A } or Seed[i + 4]
            m_LFSR_B = 8.let { m_LFSR_B = m_LFSR_B shl it; m_LFSR_B } or Seed[i + 4]
            m_LFSR_C = 8.let { m_LFSR_C = m_LFSR_C shl it; m_LFSR_C } or Seed[i + 4]
            i++
        }
        if (0 == m_LFSR_A) m_LFSR_A = 0x13579bdf
        if (0 == m_LFSR_B) m_LFSR_B = 0x2468ace0
        if (0 == m_LFSR_C) m_LFSR_C = -0x2468acf
    }

    companion object {
        val ALGORITHM_NAME: String? = "cfmx_compat"

        /**
         * returns true if the passed value is empty or is CFMX_COMPAT
         */
        fun isCfmxCompat(algorithm: String?): Boolean {
            return if (StringUtil.isEmpty(algorithm, true)) true else algorithm.equalsIgnoreCase(ALGORITHM_NAME)
        }
    }
}