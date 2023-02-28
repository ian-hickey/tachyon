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
package lucee.runtime.debug

import java.io.IOException

class DebugCFMLWriter(writer: CFMLWriter?) : CFMLWriter(writer.getBufferSize(), writer.isAutoFlush()), DebugOutputLog {
    private val writer: CFMLWriter?
    private val fragments: List<DebugTextFragment?>? = ArrayList<DebugTextFragment?>()
    @Override
    fun getBufferSize(): Int {
        return writer.getBufferSize()
    }

    @Override
    fun isAutoFlush(): Boolean {
        return writer.isAutoFlush()
    }

    @Override
    @Throws(IOException::class)
    fun append(csq: CharSequence?): Writer? {
        log(csq.toString())
        return writer.append(csq)
    }

    @Override
    @Throws(IOException::class)
    fun append(c: Char): Writer? {
        log(String(charArrayOf(c)))
        return writer.append(c)
    }

    @Override
    @Throws(IOException::class)
    fun append(csq: CharSequence?, start: Int, end: Int): Writer? {
        log(csq!!.subSequence(start, end).toString())
        return writer.append(csq, start, end)
    }

    @Override
    @Throws(IOException::class)
    fun write(i: Int) {
        print(i)
    }

    @Override
    @Throws(IOException::class)
    fun write(cbuf: CharArray?) {
        print(cbuf)
    }

    @Override
    @Throws(IOException::class)
    fun write(str: String?) {
        print(str)
    }

    @Override
    @Throws(IOException::class)
    fun write(str: String?, off: Int, len: Int) {
        log(StringUtil.substring(str, off, len))
        writer.write(str, off, len)
    }

    @Override
    @Throws(IOException::class)
    fun getResponseStream(): OutputStream? {
        return writer.getResponseStream()
    }

    @Override
    fun setClosed(b: Boolean) {
        writer.setClosed(b)
    }

    @Override
    @Throws(IOException::class)
    fun setBufferConfig(interval: Int, b: Boolean) {
        writer.setBufferConfig(interval, b)
    }

    @Override
    @Throws(IOException::class)
    fun appendHTMLBody(text: String?) {
        writer.appendHTMLBody(text)
    }

    @Override
    @Throws(IOException::class)
    fun writeHTMLBody(text: String?) {
        writer.writeHTMLBody(text)
    }

    @Override
    @Throws(IOException::class)
    fun getHTMLBody(): String? {
        return writer.getHTMLBody()
    }

    @Override
    @Throws(IOException::class)
    fun flushHTMLBody() {
        writer.flushHTMLBody()
    }

    @Override
    @Throws(IOException::class)
    fun resetHTMLBody() {
        writer.resetHTMLBody()
    }

    @Override
    @Throws(IOException::class)
    fun appendHTMLHead(text: String?) {
        writer.appendHTMLHead(text)
    }

    @Override
    @Throws(IOException::class)
    fun writeHTMLHead(text: String?) {
        writer.writeHTMLHead(text)
    }

    @Override
    @Throws(IOException::class)
    fun getHTMLHead(): String? {
        return writer.getHTMLHead()
    }

    @Override
    @Throws(IOException::class)
    fun flushHTMLHead() {
        writer.flushHTMLHead()
    }

    @Override
    @Throws(IOException::class)
    fun resetHTMLHead() {
        writer.resetHTMLHead()
    }

    @Override
    @Throws(IOException::class)
    fun writeRaw(str: String?) {
        print(str)
    }

    @Override
    @Throws(IOException::class)
    fun clear() {
        writer.clear()
    }

    @Override
    @Throws(IOException::class)
    fun clearBuffer() {
        writer.clearBuffer()
    }

    @Override
    @Throws(IOException::class)
    fun close() {
        writer.close()
    }

    @Override
    @Throws(IOException::class)
    fun flush() {
        writer.flush()
    }

    @Override
    fun getRemaining(): Int {
        return writer.getRemaining()
    }

    @Override
    @Throws(IOException::class)
    fun newLine() {
        println()
    }

    @Override
    @Throws(IOException::class)
    fun print(b: Boolean) {
        writer.print(b)
        log(if (b) "true" else "false")
    }

    @Override
    @Throws(IOException::class)
    fun print(c: Char) {
        log(String(charArrayOf(c)))
        writer.write(c)
    }

    @Override
    @Throws(IOException::class)
    fun print(i: Int) {
        log(Caster.toString(i))
        writer.write(i)
    }

    @Override
    @Throws(IOException::class)
    fun print(l: Long) {
        log(Caster.toString(l))
        writer.print(l)
    }

    @Override
    @Throws(IOException::class)
    fun print(f: Float) {
        log(Caster.toString(f))
        writer.print(f)
    }

    @Override
    @Throws(IOException::class)
    fun print(d: Double) {
        log(Caster.toString(d))
        writer.print(d)
    }

    @Override
    @Throws(IOException::class)
    fun print(carr: CharArray?) {
        log(String(carr))
        writer.write(carr)
    }

    @Override
    @Throws(IOException::class)
    fun print(str: String?) {
        log(str)
        writer.write(str)
    }

    @Override
    @Throws(IOException::class)
    fun print(obj: Object?) {
        log(String.valueOf(obj))
        writer.print(obj)
    }

    @Override
    @Throws(IOException::class)
    fun println() {
        print("\n")
    }

    @Override
    @Throws(IOException::class)
    fun println(b: Boolean) {
        print(b)
        print("\n")
    }

    @Override
    @Throws(IOException::class)
    fun println(c: Char) {
        print(c)
        print("\n")
    }

    @Override
    @Throws(IOException::class)
    fun println(i: Int) {
        print(i)
        print("\n")
    }

    @Override
    @Throws(IOException::class)
    fun println(l: Long) {
        print(l)
        print("\n")
    }

    @Override
    @Throws(IOException::class)
    fun println(f: Float) {
        print(f)
        print("\n")
    }

    @Override
    @Throws(IOException::class)
    fun println(d: Double) {
        print(d)
        print("\n")
    }

    @Override
    @Throws(IOException::class)
    fun println(carr: CharArray?) {
        print(carr)
        print("\n")
    }

    @Override
    @Throws(IOException::class)
    fun println(str: String?) {
        print(str)
        print("\n")
    }

    @Override
    @Throws(IOException::class)
    fun println(obj: Object?) {
        print(obj)
        print("\n")
    }

    @Override
    @Throws(IOException::class)
    fun write(carr: CharArray?, off: Int, len: Int) {
        log(StringUtil.substring(String(carr), off, len))
        writer.write(carr, off, len)
    }

    private fun log(str: String?) {
        val tl: TemplateLine = SystemUtil.getCurrentContext(null)
        if (tl != null) {
            fragments.add(DebugTextFragmentImpl(str, tl))
        }
    }

    @Override
    fun getFragments(): Array<DebugTextFragment?>? {
        return fragments.toArray(arrayOfNulls<DebugTextFragment?>(fragments!!.size()))
    }

    @Override
    fun setAllowCompression(allowCompression: Boolean) {
        writer.setAllowCompression(allowCompression)
    }

    @Override
    fun doCache(ci: CacheItem?) {
        writer.doCache(ci)
    }

    @Override
    fun getCacheItem(): CacheItem? {
        return writer.getCacheItem()
    }

    init {
        this.writer = writer
    }
}