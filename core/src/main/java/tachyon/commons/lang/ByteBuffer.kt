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

import java.io.ByteArrayOutputStream

/**
 * class to handle characters, similar to StringBuffer, but dont copy big blocks of char arrays.
 */
class ByteBuffer(charset: String?, size: Int) {
    private var buffer: ByteArray
    private var pos = 0
    private var length = 0
    private val root: Entity = Entity(null)
    private var curr: Entity? = root
    private val charset: String?

    /**
     * default constructor
     */
    constructor(charset: String?) : this(charset, BLOCK_LENGTH) {}

    @Throws(IOException::class)
    fun append(c: Char) {
        append(String(charArrayOf(c)))
    }

    /**
     * method to append a char array to the buffer
     *
     * @param c char array to append
     * @throws IOException
     */
    @Throws(IOException::class)
    fun append(c: CharArray?) {
        append(String(c))
    }

    fun append(c: ByteArray) {
        val maxlength = buffer.size - pos
        if (c.size < maxlength) {
            System.arraycopy(c, 0, buffer, pos, c.size)
            pos += c.size
        } else {
            System.arraycopy(c, 0, buffer, pos, maxlength)
            curr!!.next = Entity(buffer)
            curr = curr!!.next
            length += buffer.size
            buffer = ByteArray(if (buffer.size > c.size - maxlength) buffer.size else c.size - maxlength)
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
     * @throws IOException
     */
    @Throws(IOException::class)
    fun append(c: CharArray?, off: Int, len: Int) {
        append(String(c, off, len))
    }

    /**
     * Method to append a string to char buffer
     *
     * @param str String to append
     * @throws IOException
     */
    @Throws(IOException::class)
    fun append(str: String) {
        append(str.getBytes(charset))
    }

    /**
     * method to append a part of a String
     *
     * @param str string to get part from
     * @param off start index on the string
     * @param len length of the sequenz to get from string
     * @throws IOException
     */
    @Throws(IOException::class)
    fun append(str: String, off: Int, len: Int) {
        append(str.substring(off, off + len))
    }

    /**
     * method to writeout content of the char buffer in a writer, this is faster than get char array
     * with (toCharArray()) and write this in writer.
     *
     * @param writer writer to write inside
     * @throws IOException
     */
    @Throws(IOException::class)
    fun writeOut(os: OutputStream) {
        var e: Entity? = root
        while (e!!.next != null) {
            e = e.next
            os.write(e!!.data)
        }
        os.write(buffer, 0, pos)
    }

    @Override
    override fun toString(): String {
        return try {
            String(bytes, charset)
        } catch (e: UnsupportedEncodingException) {
            String(bytes)
        }
    }

    /**
     * clear the content of the buffer
     */
    fun clear() {
        if (size() == 0) return
        buffer = ByteArray(buffer.size)
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

    private inner class Entity private constructor(val data: ByteArray) {
        val next: Entity? = null
    }

    val bytes: ByteArray
        get() {
            val baos = ByteArrayOutputStream()
            try {
                writeOut(baos)
            } catch (e: IOException) {
            }
            return baos.toByteArray()
        }

    companion object {
        private const val BLOCK_LENGTH = 1024
    }

    /**
     * constructor with size of the buffer
     *
     * @param size
     */
    init {
        buffer = ByteArray(size)
        this.charset = charset
    }
}