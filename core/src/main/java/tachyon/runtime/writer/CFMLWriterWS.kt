/**
 * Copyright (c) 2023, TachyonCFML.org
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
package tachyon.runtime.writer

import java.io.IOException

/**
 * JSP Writer that Remove WhiteSpace from given content
 */
class CFMLWriterWS
/**
 * constructor of the class
 *
 * @param rsp
 * @param bufferSize
 * @param autoFlush
 */
(pc: PageContext?, req: HttpServletRequest?, rsp: HttpServletResponse?, bufferSize: Int, autoFlush: Boolean, closeConn: Boolean, showVersion: Boolean,
 contentLength: Boolean) : CFMLWriterImpl(pc, req, rsp, bufferSize, autoFlush, closeConn, showVersion, contentLength), WhiteSpaceWriter {
    var charBuffer = CHAR_EMPTY

    /**
     * @see tachyon.runtime.writer.CFMLWriterImpl.print
     */
    @Override
    @Throws(IOException::class)
    override fun print(c: Char) {
        when (c) {
            CHAR_NL -> if (charBuffer != CHAR_NL) charBuffer = c
            CHAR_BS, CHAR_FW, CHAR_RETURN, CHAR_SPACE, CHAR_TAB -> if (charBuffer == CHAR_EMPTY) charBuffer = c
            else -> {
                printBuffer()
                super.print(c)
            }
        }
    }

    @Synchronized
    @Throws(IOException::class)
    fun printBuffer() {
        if (charBuffer != CHAR_EMPTY) {
            val b = charBuffer // muss so bleiben!
            charBuffer = CHAR_EMPTY
            super.print(b)
        }
    }

    fun printBufferEL() {
        if (charBuffer != CHAR_EMPTY) {
            try {
                val b = charBuffer
                charBuffer = CHAR_EMPTY
                super.print(b)
            } catch (e: IOException) {
            }
        }
    }

    /**
     * @see tachyon.runtime.writer.CFMLWriter.writeRaw
     */
    @Override
    @Throws(IOException::class)
    override fun writeRaw(str: String?) {
        printBuffer()
        super.write(str)
    }

    /**
     * just a wrapper function for ACF
     *
     * @throws IOException
     */
    @Override
    @Throws(IOException::class)
    override fun initHeaderBuffer() {
        resetHTMLHead()
    }

    /**
     * @see tachyon.runtime.writer.CFMLWriterImpl.clear
     */
    @Override
    @Throws(IOException::class)
    override fun clear() {
        printBuffer()
        super.clear()
    }

    /**
     * @see tachyon.runtime.writer.CFMLWriterImpl.clearBuffer
     */
    @Override
    override fun clearBuffer() {
        printBufferEL()
        super.clearBuffer()
    }

    /**
     * @see tachyon.runtime.writer.CFMLWriterImpl.close
     */
    @Override
    @Throws(IOException::class)
    override fun close() {
        printBuffer()
        super.close()
    }

    /**
     * @see tachyon.runtime.writer.CFMLWriterImpl.flush
     */
    @Override
    @Throws(IOException::class)
    override fun flush() {
        printBuffer()
        super.flush()
    }

    /**
     * @see tachyon.runtime.writer.CFMLWriterImpl.getRemaining
     */
    @Override
    override fun getRemaining(): Int {
        printBufferEL()
        return super.getRemaining()
    }

    /**
     * @see tachyon.runtime.writer.CFMLWriterImpl.newLine
     */
    @Override
    @Throws(IOException::class)
    override fun newLine() {
        print(CHAR_NL)
    }

    /**
     * @see tachyon.runtime.writer.CFMLWriterImpl.print
     */
    @Override
    @Throws(IOException::class)
    override fun print(b: Boolean) {
        printBuffer()
        super.print(b)
    }

    /**
     * @see tachyon.runtime.writer.CFMLWriterImpl.print
     */
    @Override
    @Throws(IOException::class)
    override fun print(chars: CharArray?) {
        write(chars, 0, chars!!.size)
    }

    /**
     * @see tachyon.runtime.writer.CFMLWriterImpl.print
     */
    @Override
    @Throws(IOException::class)
    override fun print(d: Double) {
        printBuffer()
        super.print(d)
    }

    /**
     * @see tachyon.runtime.writer.CFMLWriterImpl.print
     */
    @Override
    @Throws(IOException::class)
    override fun print(f: Float) {
        printBuffer()
        super.print(f)
    }

    /**
     * @see tachyon.runtime.writer.CFMLWriterImpl.print
     */
    @Override
    @Throws(IOException::class)
    override fun print(i: Int) {
        printBuffer()
        super.print(i)
    }

    /**
     * @see tachyon.runtime.writer.CFMLWriterImpl.print
     */
    @Override
    @Throws(IOException::class)
    override fun print(l: Long) {
        printBuffer()
        super.print(l)
    }

    /**
     * @see tachyon.runtime.writer.CFMLWriterImpl.print
     */
    @Override
    @Throws(IOException::class)
    override fun print(obj: Object?) {
        print(obj.toString())
    }

    /**
     * @see tachyon.runtime.writer.CFMLWriterImpl.print
     */
    @Override
    @Throws(IOException::class)
    override fun print(str: String?) {
        val len: Int = str!!.length()
        for (i in 0 until len) {
            print(str.charAt(i))
        }
    }

    /**
     * @see tachyon.runtime.writer.CFMLWriterImpl.println
     */
    @Override
    @Throws(IOException::class)
    override fun println() {
        print(CHAR_NL)
    }

    /**
     * @see tachyon.runtime.writer.CFMLWriterImpl.println
     */
    @Override
    @Throws(IOException::class)
    override fun println(b: Boolean) {
        printBuffer()
        super.print(b)
        print(CHAR_NL)
    }

    /**
     * @see tachyon.runtime.writer.CFMLWriterImpl.println
     */
    @Override
    @Throws(IOException::class)
    override fun println(c: Char) {
        print(c)
        print(CHAR_NL)
    }

    /**
     * @see tachyon.runtime.writer.CFMLWriterImpl.println
     */
    @Override
    @Throws(IOException::class)
    override fun println(chars: CharArray?) {
        write(chars, 0, chars!!.size)
        print(CHAR_NL)
    }

    /**
     * @see tachyon.runtime.writer.CFMLWriterImpl.println
     */
    @Override
    @Throws(IOException::class)
    override fun println(d: Double) {
        printBuffer()
        super.print(d)
        print(CHAR_NL)
    }

    /**
     * @see tachyon.runtime.writer.CFMLWriterImpl.println
     */
    @Override
    @Throws(IOException::class)
    override fun println(f: Float) {
        printBuffer()
        super.print(f)
        print(CHAR_NL)
    }

    /**
     * @see tachyon.runtime.writer.CFMLWriterImpl.println
     */
    @Override
    @Throws(IOException::class)
    override fun println(i: Int) {
        printBuffer()
        super.print(i)
        print(CHAR_NL)
    }

    /**
     * @see tachyon.runtime.writer.CFMLWriterImpl.println
     */
    @Override
    @Throws(IOException::class)
    override fun println(l: Long) {
        printBuffer()
        super.print(l)
        print(CHAR_NL)
    }

    /**
     * @see tachyon.runtime.writer.CFMLWriterImpl.println
     */
    @Override
    @Throws(IOException::class)
    override fun println(obj: Object?) {
        println(obj.toString())
    }

    /**
     * @see tachyon.runtime.writer.CFMLWriterImpl.println
     */
    @Override
    @Throws(IOException::class)
    override fun println(str: String?) {
        print(str)
        print(CHAR_NL)
    }

    /**
     * @see tachyon.runtime.writer.CFMLWriterImpl.write
     */
    @Override
    @Throws(IOException::class)
    override fun write(chars: CharArray?, off: Int, len: Int) {
        for (i in off until len) {
            print(chars!![i])
        }
    }

    /**
     * @see tachyon.runtime.writer.CFMLWriterImpl.write
     */
    @Override
    @Throws(IOException::class)
    override fun write(str: String?, off: Int, len: Int) {
        write(str.toCharArray(), off, len)
    }

    /**
     * @see tachyon.runtime.writer.CFMLWriterImpl.write
     */
    @Override
    @Throws(IOException::class)
    override fun write(chars: CharArray?) {
        write(chars, 0, chars!!.size)
    }

    /**
     * @see tachyon.runtime.writer.CFMLWriterImpl.write
     */
    @Override
    @Throws(IOException::class)
    override fun write(i: Int) {
        print(i)
    }

    /**
     * @see tachyon.runtime.writer.CFMLWriterImpl.write
     */
    @Override
    @Throws(IOException::class)
    override fun write(str: String?) {
        write(str.toCharArray(), 0, str!!.length())
    }

    companion object {
        const val CHAR_EMPTY = 0.toChar()
        const val CHAR_NL = '\n'
        const val CHAR_SPACE = ' '
        const val CHAR_TAB = '\t'
        const val CHAR_BS = '\b' // \x0B\
        val CHAR_FW = '\f'
        const val CHAR_RETURN = '\r'
    }
}