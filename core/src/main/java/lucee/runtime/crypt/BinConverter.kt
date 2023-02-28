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
package lucee.runtime.crypt

import kotlin.Throws
import kotlin.jvm.Synchronized
import lucee.commons.io.SystemUtil.Caller
import kotlin.jvm.Transient
import kotlin.jvm.JvmOverloads
import kotlin.jvm.Volatile
import lucee.commons.collection.concurrent.ConcurrentHashMapNullSupport.EntrySet
import lucee.commons.collection.concurrent.ConcurrentHashMapNullSupport.EntryIterator
import lucee.commons.collection.LongKeyList.Pair
import lucee.commons.collection.AbstractCollection
import lucee.runtime.type.Array
import java.sql.Array
import lucee.commons.lang.Pair
import lucee.runtime.exp.CatchBlockImpl.Pair
import lucee.runtime.type.util.ListIteratorImpl
import lucee.runtime.type.Lambda
import java.util.Random
import lucee.runtime.config.Constants
import lucee.runtime.engine.Request
import lucee.runtime.engine.ExecutionLogSupport.Pair
import lucee.runtime.functions.other.NullValue
import lucee.runtime.functions.string.Val
import lucee.runtime.reflection.Reflector.JavaAnnotation
import lucee.transformer.cfml.evaluator.impl.Output
import lucee.transformer.cfml.evaluator.impl.Property
import lucee.transformer.bytecode.statement.Condition.Pair

object BinConverter {
    /**
     * gets bytes from an array into a long
     *
     * @param buffer where to get the bytes
     * @param nStartIndex index from where to read the data
     * @return the 64bit integer
     */
    fun byteArrayToLong(buffer: ByteArray?, nStartIndex: Int): Long {
        return (buffer!![nStartIndex].toLong() shl 56 or (buffer[nStartIndex + 1] and 0x0ffL shl 48) or (buffer[nStartIndex + 2] and 0x0ffL shl 40)
                or (buffer[nStartIndex + 3] and 0x0ffL shl 32) or (buffer[nStartIndex + 4] and 0x0ffL shl 24) or (buffer[nStartIndex + 5] and 0x0ffL shl 16)
                or (buffer[nStartIndex + 6] and 0x0ffL shl 8) or (buffer[nStartIndex + 7].toLong() and 0x0ff))
    }

    /**
     * converts a long o bytes which are put into a given array
     *
     * @param lValue the 64bit integer to convert
     * @param buffer the target buffer
     * @param nStartIndex where to place the bytes in the buffer
     */
    fun longToByteArray(lValue: Long, buffer: ByteArray?, nStartIndex: Int) {
        buffer!![nStartIndex] = (lValue ushr 56).toByte()
        buffer[nStartIndex + 1] = (lValue ushr 48 and 0x0ff).toByte()
        buffer[nStartIndex + 2] = (lValue ushr 40 and 0x0ff).toByte()
        buffer[nStartIndex + 3] = (lValue ushr 32 and 0x0ff).toByte()
        buffer[nStartIndex + 4] = (lValue ushr 24 and 0x0ff).toByte()
        buffer[nStartIndex + 5] = (lValue ushr 16 and 0x0ff).toByte()
        buffer[nStartIndex + 6] = (lValue ushr 8 and 0x0ff).toByte()
        buffer[nStartIndex + 7] = lValue.toByte()
    }

    /**
     * converts values from an integer array to a long
     *
     * @param buffer where to get the bytes
     * @param nStartIndex index from where to read the data
     * @return the 64bit integer
     */
    fun intArrayToLong(buffer: IntArray?, nStartIndex: Int): Long {
        return buffer!![nStartIndex].toLong() shl 32 or (buffer[nStartIndex + 1] and 0x0ffffffffL).toLong()
    }

    /**
     * converts a long to integers which are put into a given array
     *
     * @param lValue the 64bit integer to convert
     * @param buffer the target buffer
     * @param nStartIndex where to place the bytes in the buffer
     */
    fun longToIntArray(lValue: Long, buffer: IntArray?, nStartIndex: Int) {
        buffer!![nStartIndex] = (lValue ushr 32).toInt()
        buffer[nStartIndex + 1] = lValue.toInt()
    }

    /**
     * makes a long from two integers (treated unsigned)
     *
     * @param nLo lower 32bits
     * @param nHi higher 32bits
     * @return the built long
     */
    fun makeLong(nLo: Int, nHi: Int): Long {
        return nHi.toLong() shl 32 or (nLo and 0x00000000ffffffffL).toLong()
    }

    /**
     * gets the lower 32 bits of a long
     *
     * @param lVal the long integer
     * @return lower 32 bits
     */
    fun longLo32(lVal: Long): Int {
        return lVal.toInt()
    }

    /**
     * gets the higher 32 bits of a long
     *
     * @param lVal the long integer
     * @return higher 32 bits
     */
    fun longHi32(lVal: Long): Int {
        return (lVal ushr 32).toInt()
    }

    val HEXTAB: CharArray? = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f')

    /**
     * converts a byte array to a binhex string
     *
     * @param data the byte array
     * @return the binhex string
     */
    fun bytesToBinHex(data: ByteArray?): String? {
        return bytesToBinHex(data, 0, data!!.size)
    }

    /**
     * converts a byte array to a binhex string
     *
     * @param data the byte array
     * @param nStartPos start index where to get the bytes
     * @param nNumOfBytes number of bytes to convert
     * @return the binhex string
     */
    fun bytesToBinHex(data: ByteArray?, nStartPos: Int, nNumOfBytes: Int): String? {
        val sbuf = StringBuilder()
        sbuf.setLength(nNumOfBytes shl 1)
        var nPos = 0
        for (nI in 0 until nNumOfBytes) {
            sbuf.setCharAt(nPos++, HEXTAB!![data!![nI + nStartPos] shr 4 and 0x0f])
            sbuf.setCharAt(nPos++, HEXTAB[data[nI + nStartPos] and 0x0f])
        }
        return sbuf.toString()
    }

    /**
     * converts a binhex string back into a byte array (invalid codes will be skipped)
     *
     * @param sBinHex binhex string
     * @param data the target array
     * @param nSrcPos from which character in the string the conversion should begin, remember that
     * (nSrcPos modulo 2) should equals 0 normally
     * @param nDstPos to store the bytes from which position in the array
     * @param nNumOfBytes number of bytes to extract
     * @return number of extracted bytes
     */
    fun binHexToBytes(sBinHex: String?, data: ByteArray?, nSrcPos: Int, nDstPos: Int, nNumOfBytes: Int): Int {
        // check for correct ranges
        var nSrcPos = nSrcPos
        var nDstPos = nDstPos
        var nNumOfBytes = nNumOfBytes
        val nStrLen: Int = sBinHex!!.length()
        val nAvailBytes = nStrLen - nSrcPos shr 1
        if (nAvailBytes < nNumOfBytes) nNumOfBytes = nAvailBytes
        val nOutputCapacity = data!!.size - nDstPos
        if (nNumOfBytes > nOutputCapacity) nNumOfBytes = nOutputCapacity
        // convert now
        var nResult = 0
        for (nI in 0 until nNumOfBytes) {
            var bActByte: Byte = 0
            var blConvertOK = true
            for (nJ in 0..1) {
                bActByte = bActByte shl 4
                val cActChar: Char = sBinHex.charAt(nSrcPos++)
                if (cActChar >= 'a' && cActChar <= 'f') bActByte = bActByte or (cActChar - 'a').toByte() + 10 else if (cActChar >= '0' && cActChar <= '9') bActByte = bActByte or (cActChar - '0').toByte() else blConvertOK = false
            }
            if (blConvertOK) {
                data[nDstPos++] = bActByte
                nResult++
            }
        }
        return nResult
    }

    /**
     * converts a byte array into an UNICODE string
     *
     * @param data the byte array
     * @param nStartPos where to begin the conversion
     * @param nNumOfBytes number of bytes to handle
     * @return the string
     */
    fun byteArrayToUNCString(data: ByteArray?, nStartPos: Int, nNumOfBytes: Int): String? {
        // we need two bytes for every character
        var nStartPos = nStartPos
        var nNumOfBytes = nNumOfBytes
        nNumOfBytes = nNumOfBytes and 1.inv()
        // enough bytes in the buffer?
        val nAvailCapacity = data!!.size - nStartPos
        if (nAvailCapacity < nNumOfBytes) nNumOfBytes = nAvailCapacity
        val sbuf = StringBuilder()
        sbuf.setLength(nNumOfBytes shr 1)
        var nSBufPos = 0
        while (nNumOfBytes > 0) {
            sbuf.setCharAt(nSBufPos++, (data[nStartPos] shl 8 or (data[nStartPos + 1] and 0x0ff)) as Char)
            nStartPos += 2
            nNumOfBytes -= 2
        }
        return sbuf.toString()
    }
}