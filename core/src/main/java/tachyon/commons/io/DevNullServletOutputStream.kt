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
package tachyon.commons.io

import javax.servlet.ServletOutputStream

/**
 * ServletOutputStream impl.
 */
class DevNullServletOutputStream : ServletOutputStream() {
    @Override
    fun close() {
    }

    @Override
    fun flush() {
    }

    @Override
    fun write(b: ByteArray?, off: Int, len: Int) {
    }

    @Override
    fun write(b: ByteArray?) {
    }

    @Override
    fun write(b: Int) {
    }

    @Override
    fun print(b: Boolean) {
    }

    @Override
    fun print(c: Char) {
    }

    @Override
    fun print(d: Double) {
    }

    @Override
    fun print(f: Float) {
    }

    @Override
    fun print(i: Int) {
    }

    @Override
    fun print(l: Long) {
    }

    @Override
    fun print(str: String?) {
    }

    @Override
    fun println() {
    }

    @Override
    fun println(b: Boolean) {
    }

    @Override
    fun println(c: Char) {
    }

    @Override
    fun println(d: Double) {
    }

    @Override
    fun println(f: Float) {
    }

    @Override
    fun println(i: Int) {
    }

    @Override
    fun println(l: Long) {
    }

    @Override
    fun println(str: String?) {
    }

    @get:Override
    val isReady: Boolean
        get() = true

    @Override
    fun setWriteListener(arg0: WriteListener?) {
    }
}