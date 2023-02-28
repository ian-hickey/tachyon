/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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
package lucee.runtime.writer

import java.io.IOException

/**
 * JSP Writer that Remove WhiteSpace from given content while preserving pre-formatted spaces in
 * Tags like &lt;CODE&gt; &lt;PRE&gt; and &lt;TEXTAREA&gt;
 */
class CFMLWriterWSPref(pc: PageContext?, req: HttpServletRequest?, rsp: HttpServletResponse?, bufferSize: Int, autoFlush: Boolean, closeConn: Boolean, showVersion: Boolean,
                       contentLength: Boolean) : CFMLWriterImpl(pc, req, rsp, bufferSize, autoFlush, closeConn, showVersion, contentLength), WhiteSpaceWriter {
    private val depths: IntArray?
    private var depthSum = 0
    private var lastChar = 0.toChar()
    private var isFirstChar = true
    private val sb: StringBuilder? = StringBuilder()

    companion object {
        const val CHAR_NL = '\n'
        const val CHAR_RETURN = '\r'
        private const val CHAR_GT = '>'
        private const val CHAR_LT = '<'
        private const val CHAR_SL = '/'
        private val EXCLUDE_TAGS: Array<String?>? = arrayOf("code", "pre", "textarea")
        private var minTagLen = 64

        init {
            for (s in EXCLUDE_TAGS!!) if (lucee.runtime.writer.s.length() < minTagLen) minTagLen = lucee.runtime.writer.s.length()
            minTagLen++ // add 1 for LessThan symbol
        }
    }

    /**
     * prints the characters from the buffer and resets it
     *
     * TODO: make sure that printBuffer() is called at the end of the stream in case we have some
     * characters there! (flush() ?)
     */
    @Synchronized
    @Throws(IOException::class)
    fun printBuffer() { // TODO: is synchronized really needed here?
        val len: Int = sb.length()
        if (len > 0) {
            val chars = CharArray(len)
            sb.getChars(0, len, chars, 0)
            sb.setLength(0)
            super.write(chars, 0, chars.size)
        }
    }

    fun printBufferEL() {
        if (sb.length() > 0) {
            try {
                printBuffer()
            } catch (e: IOException) {
            }
        }
    }

    /**
     * checks if a character is part of an open html tag or close html tag, and if so adds it to the
     * buffer, otherwise returns false.
     *
     * @param c
     * @return true if the char was added to the buffer, false otherwise
     */
    @Throws(IOException::class)
    fun addToBuffer(c: Char): Boolean {
        var len: Int = sb.length()
        if (len == 0 && c != CHAR_LT) return false // buffer must starts with '<'
        sb.append(c) // if we reached this point then we will return true
        if (++len >= minTagLen) { // increment len as it was sampled before we appended c
            val isClosingTag = len >= 2 && sb.charAt(1) === CHAR_SL
            val substr: String
            substr = if (isClosingTag) sb.substring(2) // we know that the 1st two chars are "</"
            else sb.substring(1) // we know that the 1st char is "<"
            for (i in EXCLUDE_TAGS.indices) { // loop thru list of WS-preserving tags
                if (substr.equalsIgnoreCase(EXCLUDE_TAGS!![i])) { // we have a match
                    if (isClosingTag) {
                        depthDec(i) // decrement the depth at i and calc depthSum
                        printBuffer()
                        lastChar = 0.toChar() // needed to allow WS after buffer was printed
                    } else {
                        depthInc(i) // increment the depth at i and calc depthSum
                    }
                }
            }
        }
        return true
    }

    /**
     * decrement the depth at index and calc the new depthSum
     *
     * @param index
     */
    private fun depthDec(index: Int) {
        if (--depths!![index] < 0) depths[index] = 0
        depthCalc()
    }

    /**
     * increment the depth at index and calc the new depthSum
     *
     * @param index
     */
    private fun depthInc(index: Int) {
        depths!![index]++
        depthCalc()
    }

    /**
     * calc the new depthSum
     */
    private fun depthCalc() {
        var sum = 0
        for (d in depths!!) sum += d
        depthSum = sum
    }

    /**
     * sends a character to output stream if it is not a consecutive white-space unless we're inside a
     * PRE or TEXTAREA tag.
     *
     * @param c
     * @throws IOException
     */
    @Override
    @Throws(IOException::class)
    override fun print(c: Char) {
        val isWS: Boolean = Character.isWhitespace(c)
        if (isWS) {
            if (isFirstChar) // ignore all WS before non-WS content
                return
            if (c == CHAR_RETURN) // ignore Carriage-Return chars
                return
            if (sb.length() > 0) {
                printBuffer() // buffer should never contain WS so flush it
                lastChar = if (c == CHAR_NL) CHAR_NL else c
                super.print(lastChar)
                return
            }
        }
        isFirstChar = false
        if (c == CHAR_GT && sb.length() > 0) printBuffer() // buffer should never contain ">" so flush it
        if (isWS || !addToBuffer(c)) {
            if (depthSum == 0) { // we're not in a WS-preserving tag; suppress whitespace
                if (isWS) { // this char is WS
                    if (lastChar == CHAR_NL) // lastChar was NL; discard this WS char
                        return
                    if (c != CHAR_NL) { // this WS char is not NL
                        if (Character.isWhitespace(lastChar)) return  // lastChar was WS but Not NL; discard this WS char
                    }
                }
            }
            lastChar = c // remember c as lastChar and write it to output stream
            super.print(c)
        }
    }

    /**
     * @see lucee.runtime.writer.CFMLWriter.writeRaw
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
     * @see lucee.runtime.writer.CFMLWriterImpl.clear
     */
    @Override
    @Throws(IOException::class)
    override fun clear() {
        printBuffer()
        super.clear()
    }

    /**
     * @see lucee.runtime.writer.CFMLWriterImpl.clearBuffer
     */
    @Override
    override fun clearBuffer() {
        printBufferEL()
        super.clearBuffer()
    }

    /**
     * @see lucee.runtime.writer.CFMLWriterImpl.close
     */
    @Override
    @Throws(IOException::class)
    override fun close() {
        printBuffer()
        super.close()
    }

    /**
     * @see lucee.runtime.writer.CFMLWriterImpl.flush
     */
    @Override
    @Throws(IOException::class)
    override fun flush() {
        printBuffer()
        super.flush()
    }

    /**
     * @see lucee.runtime.writer.CFMLWriterImpl.getRemaining
     */
    @Override
    override fun getRemaining(): Int {
        printBufferEL()
        return super.getRemaining()
    }

    /**
     * @see lucee.runtime.writer.CFMLWriterImpl.newLine
     */
    @Override
    @Throws(IOException::class)
    override fun newLine() {
        print(CHAR_NL)
    }

    /**
     * @see lucee.runtime.writer.CFMLWriterImpl.print
     */
    @Override
    @Throws(IOException::class)
    override fun print(b: Boolean) {
        printBuffer()
        super.print(b)
    }

    /**
     * @see lucee.runtime.writer.CFMLWriterImpl.print
     */
    @Override
    @Throws(IOException::class)
    override fun print(chars: CharArray?) {
        write(chars, 0, chars!!.size)
    }

    /**
     * @see lucee.runtime.writer.CFMLWriterImpl.print
     */
    @Override
    @Throws(IOException::class)
    override fun print(d: Double) {
        printBuffer()
        super.print(d)
    }

    /**
     * @see lucee.runtime.writer.CFMLWriterImpl.print
     */
    @Override
    @Throws(IOException::class)
    override fun print(f: Float) {
        printBuffer()
        super.print(f)
    }

    /**
     * @see lucee.runtime.writer.CFMLWriterImpl.print
     */
    @Override
    @Throws(IOException::class)
    override fun print(i: Int) {
        printBuffer()
        super.print(i)
    }

    /**
     * @see lucee.runtime.writer.CFMLWriterImpl.print
     */
    @Override
    @Throws(IOException::class)
    override fun print(l: Long) {
        printBuffer()
        super.print(l)
    }

    /**
     * @see lucee.runtime.writer.CFMLWriterImpl.print
     */
    @Override
    @Throws(IOException::class)
    override fun print(obj: Object?) {
        print(obj.toString())
    }

    /**
     * @see lucee.runtime.writer.CFMLWriterImpl.print
     */
    @Override
    @Throws(IOException::class)
    override fun print(str: String?) {
        write(str.toCharArray(), 0, str!!.length())
    }

    /**
     * @see lucee.runtime.writer.CFMLWriterImpl.println
     */
    @Override
    @Throws(IOException::class)
    override fun println() {
        print(CHAR_NL)
    }

    /**
     * @see lucee.runtime.writer.CFMLWriterImpl.println
     */
    @Override
    @Throws(IOException::class)
    override fun println(b: Boolean) {
        printBuffer()
        super.print(b)
        print(CHAR_NL)
    }

    /**
     * @see lucee.runtime.writer.CFMLWriterImpl.println
     */
    @Override
    @Throws(IOException::class)
    override fun println(c: Char) {
        print(c)
        print(CHAR_NL)
    }

    /**
     * @see lucee.runtime.writer.CFMLWriterImpl.println
     */
    @Override
    @Throws(IOException::class)
    override fun println(chars: CharArray?) {
        write(chars, 0, chars!!.size)
        print(CHAR_NL)
    }

    /**
     * @see lucee.runtime.writer.CFMLWriterImpl.println
     */
    @Override
    @Throws(IOException::class)
    override fun println(d: Double) {
        printBuffer()
        super.print(d)
        print(CHAR_NL)
    }

    /**
     * @see lucee.runtime.writer.CFMLWriterImpl.println
     */
    @Override
    @Throws(IOException::class)
    override fun println(f: Float) {
        printBuffer()
        super.print(f)
        print(CHAR_NL)
    }

    /**
     * @see lucee.runtime.writer.CFMLWriterImpl.println
     */
    @Override
    @Throws(IOException::class)
    override fun println(i: Int) {
        printBuffer()
        super.print(i)
        print(CHAR_NL)
    }

    /**
     * @see lucee.runtime.writer.CFMLWriterImpl.println
     */
    @Override
    @Throws(IOException::class)
    override fun println(l: Long) {
        printBuffer()
        super.print(l)
        print(CHAR_NL)
    }

    /**
     * @see lucee.runtime.writer.CFMLWriterImpl.println
     */
    @Override
    @Throws(IOException::class)
    override fun println(obj: Object?) {
        println(obj.toString())
    }

    /**
     * @see lucee.runtime.writer.CFMLWriterImpl.println
     */
    @Override
    @Throws(IOException::class)
    override fun println(str: String?) {
        print(str)
        print(CHAR_NL)
    }

    /**
     * @see lucee.runtime.writer.CFMLWriterImpl.write
     */
    @Override
    @Throws(IOException::class)
    override fun write(chars: CharArray?, off: Int, len: Int) {
        for (i in off until len) {
            print(chars!![i])
        }
    }

    /**
     * @see lucee.runtime.writer.CFMLWriterImpl.write
     */
    @Override
    @Throws(IOException::class)
    override fun write(str: String?, off: Int, len: Int) {
        write(str.toCharArray(), off, len)
    }

    /**
     * @see lucee.runtime.writer.CFMLWriterImpl.write
     */
    @Override
    @Throws(IOException::class)
    override fun write(chars: CharArray?) {
        write(chars, 0, chars!!.size)
    }

    /**
     * @see lucee.runtime.writer.CFMLWriterImpl.write
     */
    @Override
    @Throws(IOException::class)
    override fun write(i: Int) {
        print(i)
    }

    /**
     * @see lucee.runtime.writer.CFMLWriterImpl.write
     */
    @Override
    @Throws(IOException::class)
    override fun write(str: String?) {
        write(str.toCharArray(), 0, str!!.length())
    }

    /**
     * constructor of the class
     *
     * @param rsp
     * @param bufferSize
     * @param autoFlush
     */
    init {
        depths = IntArray(EXCLUDE_TAGS!!.size)
    }
}