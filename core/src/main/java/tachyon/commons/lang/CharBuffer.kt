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

import java.io.IOException

/**
 * class to handle characters, similar to StringBuffer, but dont copy big blocks of char arrays.
 */
class CharBuffer @JvmOverloads constructor(size: Int = BLOCK_LENGTH) {
    private var buffer: CharArray
    private var pos = 0
    private var length = 0
    private val root: Entity = Entity(null)
    private var curr: Entity? = root
    fun append(c: Char) {
        append(charArrayOf(c))
    }

    /**
     * method to append a char array to the buffer
     *
     * @param c char array to append
     */
    fun append(c: CharArray) {
        val maxlength = buffer.size - pos
        if (c.size < maxlength) {
            System.arraycopy(c, 0, buffer, pos, c.size)
            pos += c.size
        } else {
            System.arraycopy(c, 0, buffer, pos, maxlength)
            curr!!.next = Entity(buffer)
            curr = curr!!.next
            length += buffer.size
            buffer = CharArray(if (buffer.size > c.size - maxlength) buffer.size else c.size - maxlength)
            pos = if (c.size > maxlength) {
                System.arraycopy(c, maxlength, buffer, 0, c.size - maxlength)
                c.size - maxlength
            } else {
                0
            }
        }
    }

    /**
     * method to append a part of a char array
     *
     * @param c char array to get part from
     * @param off start index on the char array
     * @param len length of the sequenz to get from array
     */
    fun append(c: CharArray?, off: Int, len: Int) {
        val restLength = buffer.size - pos
        if (len < restLength) {
            System.arraycopy(c, off, buffer, pos, len)
            pos += len
        } else {
            System.arraycopy(c, off, buffer, pos, restLength)
            curr!!.next = Entity(buffer)
            curr = curr!!.next
            length += buffer.size
            buffer = CharArray(if (buffer.size > len - restLength) buffer.size else len - restLength)
            System.arraycopy(c, off + restLength, buffer, 0, len - restLength)
            pos = len - restLength
        }
    }

    /**
     * Method to append a string to char buffer
     *
     * @param str String to append
     */
    fun append(str: String?) {
        if (str == null) return
        val restLength = buffer.size - pos
        if (str.length() < restLength) {
            str.getChars(0, str.length(), buffer, pos)
            pos += str.length()
        } else {
            str.getChars(0, restLength, buffer, pos)
            curr!!.next = Entity(buffer)
            curr = curr!!.next
            length += buffer.size
            buffer = CharArray(if (buffer.size > str.length() - restLength) buffer.size else str.length() - restLength)
            str.getChars(restLength, str.length(), buffer, 0)
            pos = str.length() - restLength
        }
    }

    /**
     * method to append a part of a String
     *
     * @param str string to get part from
     * @param off start index on the string
     * @param len length of the sequenz to get from string
     */
    fun append(str: String, off: Int, len: Int) {
        val restLength = buffer.size - pos
        if (len < restLength) {
            str.getChars(off, off + len, buffer, pos)
            pos += len
        } else {
            str.getChars(off, off + restLength, buffer, pos)
            curr!!.next = Entity(buffer)
            curr = curr!!.next
            length += buffer.size
            buffer = CharArray(if (buffer.size > len - restLength) buffer.size else len - restLength)
            str.getChars(off + restLength, off + len, buffer, 0)
            pos = len - restLength
        }
    }

    /**
     * method to writeout content of the char buffer in a writer, this is faster than get char array
     * with (toCharArray()) and write this in writer.
     *
     * @param writer writer to write inside
     * @throws IOException
     */
    @Throws(IOException::class)
    fun writeOut(writer: Writer) {
        var e: Entity? = root
        while (e!!.next != null) {
            e = e.next
            writer.write(e!!.data)
        }
        writer.write(buffer, 0, pos)
    }

    @Throws(IOException::class)
    fun writeOut(os: OutputStream, charset: String?) {
        var e: Entity? = root
        while (e!!.next != null) {
            e = e.next
            os.write(String(e!!.data).getBytes(charset))
        }
        os.write(String(buffer, 0, pos).getBytes(charset))
    }

    /**
     * return content of the Char Buffer as char array
     *
     * @return char array
     */
    fun toCharArray(): CharArray {
        var e: Entity? = root
        val chrs = CharArray(size())
        var off = 0
        while (e!!.next != null) {
            e = e.next
            System.arraycopy(e!!.data, 0, chrs, off, e.data.size)
            off += e.data.size
        }
        System.arraycopy(buffer, 0, chrs, off, pos)
        return chrs
    }

    @Override
    override fun toString(): String {
        return String(toCharArray())
    }

    /**
     * clear the content of the buffer
     */
    fun clear() {
        if (size() == 0) return
        buffer = CharArray(buffer.size)
        root.next = null
        pos = 0
        length = 0
        curr = root
    }

    /**
     * @return returns the size of the content of the buffer
     */
    fun size(): Int {
        return length + pos
    }

    private inner class Entity private constructor(val data: CharArray) {
        val next: Entity? = null
    }

    @Throws(UnsupportedEncodingException::class)
    fun getBytes(characterEncoding: String?): ByteArray {
        return toString().getBytes(characterEncoding)
    }

    companion object {
        private const val BLOCK_LENGTH = 1024
    }
    /**
     * constructor with size of the buffer
     *
     * @param size
     */
    /**
     * default constructor
     */
    init {
        buffer = CharArray(size)
    }
}