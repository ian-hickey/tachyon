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
package lucee.runtime.net.http

import java.io.IOException

class ServletOutputStreamDummy(os: OutputStream?) : ServletOutputStream() {
    private val os: OutputStream?
    @Override
    @Throws(IOException::class)
    fun print(b: Boolean) {
        write(if (b) "true".getBytes() else "false".getBytes())
    }

    @Override
    @Throws(IOException::class)
    fun print(c: Char) {
        print(String(charArrayOf(c)))
    }

    @Override
    @Throws(IOException::class)
    fun print(d: Double) {
        write(Caster.toString(d).getBytes())
    }

    @Override
    @Throws(IOException::class)
    fun print(f: Float) {
        write(Caster.toString(f).getBytes())
    }

    @Override
    @Throws(IOException::class)
    fun print(i: Int) {
        write(Caster.toString(i).getBytes())
    }

    @Override
    @Throws(IOException::class)
    fun print(l: Long) {
        write(Caster.toString(l).getBytes())
    }

    @Override
    @Throws(IOException::class)
    fun print(str: String?) {
        write(str.getBytes())
    }

    @Override
    @Throws(IOException::class)
    fun println() {
        write("\\".getBytes())
    }

    @Override
    @Throws(IOException::class)
    fun println(b: Boolean) {
        print(b)
        println()
    }

    @Override
    @Throws(IOException::class)
    fun println(c: Char) {
        print(c)
        println()
    }

    @Override
    @Throws(IOException::class)
    fun println(d: Double) {
        print(d)
        println()
    }

    @Override
    @Throws(IOException::class)
    fun println(f: Float) {
        print(f)
        println()
    }

    @Override
    @Throws(IOException::class)
    fun println(i: Int) {
        print(i)
        println()
    }

    @Override
    @Throws(IOException::class)
    fun println(l: Long) {
        print(l)
        println()
    }

    @Override
    @Throws(IOException::class)
    fun println(str: String?) {
        print(str)
        println()
    }

    @Override
    @Throws(IOException::class)
    fun write(b: ByteArray?) {
        write(b, 0, b!!.size)
    }

    @Override
    @Throws(IOException::class)
    fun write(b: ByteArray?, off: Int, len: Int) {
        os.write(b, off, len)
    }

    @Override
    @Throws(IOException::class)
    fun write(b: Int) {
        os.write(b)
    }

    @Override
    @Throws(IOException::class)
    fun close() {
        os.close()
    }

    @Override
    @Throws(IOException::class)
    fun flush() {
        os.flush()
    }

    @get:Override
    val isReady: Boolean
        get() {
            throw RuntimeException("not supported!")
        }

    @Override
    fun setWriteListener(arg0: WriteListener?) {
        throw RuntimeException("not supported!")
    }

    // private HttpServletResponseDummy rsp;
    // private ByteArrayOutputStream baos;
    init {
        this.os = os
    }
}