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
 * implementation of the BodyContent
 */
class BodyContentImpl(jspWriter: JspWriter?) : BodyContent(jspWriter) {
    var charBuffer: CharBuffer? = CharBuffer(128)
    var enclosingWriter: JspWriter?

    /**
     * initialize the BodyContent with the enclosing jsp writer
     *
     * @param jspWriter
     */
    fun init(jspWriter: JspWriter?) {
        enclosingWriter = jspWriter
        clearBuffer()
    }

    /**
     * @see javax.servlet.jsp.tagext.BodyContent.getReader
     */
    @Override
    fun getReader(): Reader? {
        return StringReader(charBuffer.toString())
    }

    /**
     * @see javax.servlet.jsp.tagext.BodyContent.getString
     */
    @Override
    fun getString(): String? {
        return charBuffer.toString()
    }

    /**
     * @see javax.servlet.jsp.tagext.BodyContent.writeOut
     */
    @Override
    @Throws(IOException::class)
    fun writeOut(writer: Writer?) {
        charBuffer.writeOut(writer)
    }

    /**
     * @see javax.servlet.jsp.JspWriter.newLine
     */
    @Override
    fun newLine() {
        println()
    }

    /**
     * @see javax.servlet.jsp.JspWriter.print
     */
    @Override
    fun print(arg: Boolean) {
        print(if (arg) "true" else "false")
    }

    /**
     * @see javax.servlet.jsp.JspWriter.print
     */
    @Override
    fun print(arg: Char) {
        charBuffer.append(String.valueOf(arg))
    }

    /**
     * @see javax.servlet.jsp.JspWriter.print
     */
    @Override
    fun print(arg: Int) {
        charBuffer.append(String.valueOf(arg))
    }

    /**
     * @see javax.servlet.jsp.JspWriter.print
     */
    @Override
    fun print(arg: Long) {
        charBuffer.append(String.valueOf(arg))
    }

    /**
     * @see javax.servlet.jsp.JspWriter.print
     */
    @Override
    fun print(arg: Float) {
        charBuffer.append(String.valueOf(arg))
    }

    /**
     * @see javax.servlet.jsp.JspWriter.print
     */
    @Override
    fun print(arg: Double) {
        charBuffer.append(String.valueOf(arg))
    }

    /**
     * @see javax.servlet.jsp.JspWriter.print
     */
    @Override
    fun print(arg: CharArray?) {
        charBuffer.append(arg)
    }

    /**
     * @see javax.servlet.jsp.JspWriter.print
     */
    @Override
    fun print(arg: String?) {
        charBuffer.append(arg)
    }

    /**
     * @see javax.servlet.jsp.JspWriter.print
     */
    @Override
    fun print(arg: Object?) {
        charBuffer.append(String.valueOf(arg))
    }

    /**
     * @see javax.servlet.jsp.JspWriter.println
     */
    @Override
    fun println() {
        charBuffer.append("\n")
    }

    /**
     * @see javax.servlet.jsp.JspWriter.println
     */
    @Override
    fun println(arg: Boolean) {
        print(arg)
        println()
    }

    /**
     * @see javax.servlet.jsp.JspWriter.println
     */
    @Override
    fun println(arg: Char) {
        print(arg)
        println()
    }

    /**
     * @see javax.servlet.jsp.JspWriter.println
     */
    @Override
    fun println(arg: Int) {
        print(arg)
        println()
    }

    /**
     * @see javax.servlet.jsp.JspWriter.println
     */
    @Override
    fun println(arg: Long) {
        print(arg)
        println()
    }

    /**
     * @see javax.servlet.jsp.JspWriter.println
     */
    @Override
    fun println(arg: Float) {
        print(arg)
        println()
    }

    /**
     * @see javax.servlet.jsp.JspWriter.println
     */
    @Override
    fun println(arg: Double) {
        print(arg)
        println()
    }

    /**
     * @see javax.servlet.jsp.JspWriter.println
     */
    @Override
    fun println(arg: CharArray?) {
        print(arg)
        println()
    }

    /**
     * @see javax.servlet.jsp.JspWriter.println
     */
    @Override
    fun println(arg: String?) {
        print(arg)
        println()
    }

    /**
     * @see javax.servlet.jsp.JspWriter.println
     */
    @Override
    fun println(arg: Object?) {
        print(arg)
        println()
    }

    /**
     * @throws IOException
     * @see javax.servlet.jsp.JspWriter.clear
     */
    @Override
    @Throws(IOException::class)
    fun clear() {
        charBuffer.clear()
        enclosingWriter.clear()
    }

    /**
     * @see javax.servlet.jsp.JspWriter.clearBuffer
     */
    @Override
    fun clearBuffer() {
        charBuffer.clear()
    }

    /**
     * @see java.io.Writer.flush
     */
    @Override
    @Throws(IOException::class)
    fun flush() {
        enclosingWriter.write(charBuffer.toCharArray())
        charBuffer.clear()
    }

    /**
     * @see java.io.Writer.close
     */
    @Override
    @Throws(IOException::class)
    fun close() {
        flush()
        enclosingWriter.close()
    }

    /**
     * @see javax.servlet.jsp.JspWriter.getRemaining
     */
    @Override
    fun getRemaining(): Int {
        return bufferSize - charBuffer.size()
    }

    /**
     * @see java.io.Writer.write
     */
    @Override
    fun write(cbuf: CharArray?, off: Int, len: Int) {
        charBuffer.append(cbuf, off, len)
    }

    /**
     * @see java.io.Writer.write
     */
    @Override
    fun write(cbuf: CharArray?) {
        charBuffer.append(cbuf)
    }

    /**
     * @see java.io.Writer.write
     */
    @Override
    fun write(c: Int) {
        print(c)
    }

    /**
     * @see java.io.Writer.write
     */
    @Override
    fun write(str: String?, off: Int, len: Int) {
        charBuffer.append(str, off, len)
    }

    /**
     * @see java.io.Writer.write
     */
    @Override
    fun write(str: String?) {
        charBuffer.append(str)
    }

    /**
     * @see java.lang.Object.toString
     */
    @Override
    override fun toString(): String {
        return charBuffer.toString()
    }

    /**
     * @see javax.servlet.jsp.tagext.BodyContent.clearBody
     */
    @Override
    fun clearBody() {
        charBuffer.clear()
    }

    /**
     * @see javax.servlet.jsp.tagext.BodyContent.getEnclosingWriter
     */
    @Override
    fun getEnclosingWriter(): JspWriter? {
        return enclosingWriter
    }

    /**
     * returns the inner char buffer
     *
     * @return intern CharBuffer
     */
    fun getCharBuffer(): CharBuffer? {
        return charBuffer
    }

    /**
     * sets the inner Charbuffer
     *
     * @param charBuffer
     */
    fun setCharBuffer(charBuffer: CharBuffer?) {
        this.charBuffer = charBuffer
    }

    /**
     * @see javax.servlet.jsp.JspWriter.getBufferSize
     */
    @Override
    fun getBufferSize(): Int {
        return charBuffer.size()
    }

    /**
     * @see javax.servlet.jsp.JspWriter.isAutoFlush
     */
    @Override
    fun isAutoFlush(): Boolean {
        return super.isAutoFlush()
    }

    /**
     * @see java.io.Writer.append
     */
    @Override
    @Throws(IOException::class)
    fun append(csq: CharSequence?): Writer? {
        write(csq.toString())
        return this
    }

    /**
     * @see java.io.Writer.append
     */
    @Override
    @Throws(IOException::class)
    fun append(csq: CharSequence?, start: Int, end: Int): Writer? {
        write(csq!!.subSequence(start, end).toString())
        return this
    }

    /**
     * @see java.io.Writer.append
     */
    @Override
    @Throws(IOException::class)
    fun append(c: Char): Writer? {
        write(c.toInt())
        return this
    }

    /**
     * default constructor
     *
     * @param jspWriter
     */
    init {
        enclosingWriter = jspWriter
    }
}