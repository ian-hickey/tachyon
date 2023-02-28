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
package lucee.commons.io

import java.io.IOException

class ForkWriter(w1: Writer, w2: Writer) : Writer() {
    private val w1: Writer
    private val w2: Writer
    @Override
    @Throws(IOException::class)
    fun append(c: Char): Writer {
        try {
            w1.write(c)
        } finally {
            w2.write(c)
        }
        return this
    }

    @Override
    @Throws(IOException::class)
    fun append(csq: CharSequence, start: Int, end: Int): Writer {
        try {
            w1.write(csq.toString(), start, end)
        } finally {
            w2.write(csq.toString(), start, end)
        }
        return this
    }

    @Override
    @Throws(IOException::class)
    fun append(csq: CharSequence): Writer {
        try {
            w1.write(csq.toString())
        } finally {
            w2.write(csq.toString())
        }
        return this
    }

    @Override
    @Throws(IOException::class)
    fun write(cbuf: CharArray?) {
        try {
            w1.write(cbuf)
        } finally {
            w2.write(cbuf)
        }
    }

    @Override
    @Throws(IOException::class)
    fun write(c: Int) {
        try {
            w1.write(c)
        } finally {
            w2.write(c)
        }
    }

    @Override
    @Throws(IOException::class)
    fun write(str: String?, off: Int, len: Int) {
        try {
            w1.write(str, off, len)
        } finally {
            w2.write(str, off, len)
        }
    }

    @Override
    @Throws(IOException::class)
    fun write(str: String?) {
        try {
            w1.write(str)
        } finally {
            w2.write(str)
        }
    }

    @Override
    @Throws(IOException::class)
    fun close() {
        try {
            w1.close()
        } finally {
            w2.close()
        }
    }

    @Override
    @Throws(IOException::class)
    fun flush() {
        try {
            w1.flush()
        } finally {
            w2.flush()
        }
    }

    @Override
    @Throws(IOException::class)
    fun write(cbuf: CharArray?, off: Int, len: Int) {
        try {
            w1.write(cbuf, off, len)
        } finally {
            w2.write(cbuf, off, len)
        }
    }

    init {
        this.w1 = w1
        this.w2 = w2
    }
}