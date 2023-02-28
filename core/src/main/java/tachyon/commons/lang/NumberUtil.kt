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

import java.util.concurrent.ThreadLocalRandom

object NumberUtil {
    fun hexToInt(s: String, defaultValue: Int): Int {
        return try {
            hexToInt(s)
        } catch (e: ExpressionException) {
            defaultValue
        }
    }

    @Throws(ExpressionException::class)
    fun hexToInt(s: String): Int {
        var s = s
        s = s.toLowerCase()
        val n = IntArray(s.length())
        var c: Char
        var sum = 0
        var koef = 1
        for (i in n.indices.reversed()) {
            c = s.charAt(i)
            if (!(c >= '0' && c <= '9' || c >= 'a' && c <= 'f')) {
                throw ExpressionException("invalid hex constant [$c], hex constants are [0-9,a-f]")
            }
            when (c) {
                48 -> n[i] = 0
                49 -> n[i] = 1
                50 -> n[i] = 2
                51 -> n[i] = 3
                52 -> n[i] = 4
                53 -> n[i] = 5
                54 -> n[i] = 6
                55 -> n[i] = 7
                56 -> n[i] = 8
                57 -> n[i] = 9
                97 -> n[i] = 10
                98 -> n[i] = 11
                99 -> n[i] = 12
                100 -> n[i] = 13
                101 -> n[i] = 14
                102 -> n[i] = 15
            }
            sum = sum + n[i] * koef
            koef = koef * 16
        }
        return sum
    }

    fun longToByteArray(l: Long): ByteArray {
        val ba = ByteArray(8)
        var i = 0
        while (i < 64) {
            ba[i shr 3] = Long.valueOf(l and (255L shl i) shr i).byteValue()
            i += 8
        }
        return ba
    }

    fun byteArrayToLong(ba: ByteArray): Long {
        var l: Long = 0
        var i = 0
        while (i < 8 && i < 8) {
            l = l or (ba[i].toLong() shl (i shl 3) and (255L shl (i shl 3)))
            i++
        }
        return l
    }

    fun randomRange(min: Int, max: Int): Int {
        return min + ThreadLocalRandom.current().nextInt(max - min + 1) as Int
    }
}