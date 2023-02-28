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
 * BodyContent implementation that dont store input
 */
class DevNullBodyContent
/**
 * default constructor
 */
    : BodyContent(null) {
    private val enclosingWriter: JspWriter? = null

    /**
     * @see javax.servlet.jsp.tagext.BodyContent.getReader
     */
    @Override
    fun getReader(): Reader? {
        return StringReader("")
    }

    /**
     * @see javax.servlet.jsp.tagext.BodyContent.getString
     */
    @Override
    fun getString(): String? {
        return ""
    }

    /**
     *
     * @see javax.servlet.jsp.tagext.BodyContent.writeOut
     */
    @Override
    fun writeOut(writer: Writer?) {
    }

    /**
     * @see javax.servlet.jsp.JspWriter.newLine
     */
    @Override
    fun newLine() {
    }

    /**
     * @see javax.servlet.jsp.JspWriter.print
     */
    @Override
    fun print(b: Boolean) {
    }

    /**
     * @see javax.servlet.jsp.JspWriter.print
     */
    @Override
    fun print(c: Char) {
    }

    /**
     * @see javax.servlet.jsp.JspWriter.print
     */
    @Override
    fun print(i: Int) {
    }

    /**
     * @see javax.servlet.jsp.JspWriter.print
     */
    @Override
    fun print(l: Long) {
    }

    /**
     * @see javax.servlet.jsp.JspWriter.print
     */
    @Override
    fun print(f: Float) {
    }

    /**
     * @see javax.servlet.jsp.JspWriter.print
     */
    @Override
    fun print(d: Double) {
    }

    /**
     * @see javax.servlet.jsp.JspWriter.print
     */
    @Override
    fun print(c: CharArray?) {
    }

    /**
     * @see javax.servlet.jsp.JspWriter.print
     */
    @Override
    fun print(str: String?) {
    }

    /**
     * @see javax.servlet.jsp.JspWriter.print
     */
    @Override
    fun print(o: Object?) {
    }

    /**
     * @see javax.servlet.jsp.JspWriter.println
     */
    @Override
    fun println() {
    }

    /**
     * @see javax.servlet.jsp.JspWriter.println
     */
    @Override
    fun println(b: Boolean) {
    }

    /**
     * @see javax.servlet.jsp.JspWriter.println
     */
    @Override
    fun println(c: Char) {
    }

    /**
     * @see javax.servlet.jsp.JspWriter.println
     */
    @Override
    fun println(i: Int) {
    }

    /**
     * @see javax.servlet.jsp.JspWriter.println
     */
    @Override
    fun println(l: Long) {
    }

    /**
     * @see javax.servlet.jsp.JspWriter.println
     */
    @Override
    fun println(f: Float) {
    }

    /**
     * @see javax.servlet.jsp.JspWriter.println
     */
    @Override
    fun println(d: Double) {
    }

    /**
     * @see javax.servlet.jsp.JspWriter.println
     */
    @Override
    fun println(c: CharArray?) {
    }

    /**
     * @see javax.servlet.jsp.JspWriter.println
     */
    @Override
    fun println(str: String?) {
    }

    /**
     * @see javax.servlet.jsp.JspWriter.println
     */
    @Override
    fun println(o: Object?) {
    }

    /**
     * @see javax.servlet.jsp.JspWriter.clear
     */
    @Override
    fun clear() {
    }

    /**
     * @see javax.servlet.jsp.JspWriter.clearBuffer
     */
    @Override
    fun clearBuffer() {
    }

    /**
     * @see java.io.Writer.close
     */
    @Override
    @Throws(IOException::class)
    fun close() {
        if (enclosingWriter != null) enclosingWriter.close()
    }

    /**
     * @see javax.servlet.jsp.JspWriter.getRemaining
     */
    @Override
    fun getRemaining(): Int {
        return 0
    }

    /**
     * @see java.io.Writer.write
     */
    @Override
    fun write(cbuf: CharArray?, off: Int, len: Int) {
    }

    /**
     * @see javax.servlet.jsp.tagext.BodyContent.clearBody
     */
    @Override
    fun clearBody() {
    }

    /**
     * @see java.io.Writer.flush
     */
    @Override
    @Throws(IOException::class)
    fun flush() {
        if (enclosingWriter != null) enclosingWriter.flush()
    }

    /**
     * @see javax.servlet.jsp.tagext.BodyContent.getEnclosingWriter
     */
    @Override
    fun getEnclosingWriter(): JspWriter? {
        return enclosingWriter
    }

    /**
     * @see javax.servlet.jsp.JspWriter.getBufferSize
     */
    @Override
    fun getBufferSize(): Int {
        return 0
    }

    /**
     * @see javax.servlet.jsp.JspWriter.isAutoFlush
     */
    @Override
    fun isAutoFlush(): Boolean {
        return false
    }
}