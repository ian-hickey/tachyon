/**
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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
package lucee.runtime.op

import java.io.BufferedInputStream

class IOImpl : IO {
    @Override
    fun closeSilent(`is`: InputStream?) {
        IOUtil.closeEL(`is`)
    }

    @Override
    fun closeSilent(os: OutputStream?) {
        IOUtil.closeEL(os)
    }

    @Override
    fun closeSilent(`is`: InputStream?, os: OutputStream?) {
        IOUtil.closeEL(`is`, os)
    }

    @Override
    fun closeSilent(r: Reader?) {
        IOUtil.closeEL(r)
    }

    @Override
    fun closeSilent(w: Writer?) {
        IOUtil.closeEL(w)
    }

    @Override
    fun closeSilent(o: Object?) {
        IOUtil.closeEL(o)
    }

    @Override
    @Throws(IOException::class)
    fun toString(`is`: InputStream?, charset: Charset?): String? {
        return IOUtil.toString(`is`, charset)
    }

    @Override
    @Throws(IOException::class)
    fun toString(r: Reader?): String? {
        return IOUtil.toString(r)
    }

    @Override
    @Throws(IOException::class)
    fun toString(barr: ByteArray?, charset: Charset?): String? {
        return IOUtil.toString(barr, charset)
    }

    @Override
    @Throws(IOException::class)
    fun toString(res: Resource?, charset: Charset?): String? {
        return IOUtil.toString(res, charset)
    }

    @Override
    @Throws(IOException::class)
    fun copy(`in`: InputStream?, out: OutputStream?, closeIS: Boolean, closeOS: Boolean) {
        IOUtil.copy(`in`, out, closeIS, closeOS)
    }

    @Override
    @Throws(IOException::class)
    fun copy(r: Reader?, w: Writer?, closeR: Boolean, closeW: Boolean) {
        IOUtil.copy(r, w, closeR, closeW)
    }

    @Override
    @Throws(IOException::class)
    fun copy(src: Resource?, trg: Resource?) {
        IOUtil.copy(src, trg)
    }

    @Override
    fun toBufferedInputStream(`is`: InputStream?): BufferedInputStream? {
        return IOUtil.toBufferedInputStream(`is`)
    }

    @Override
    fun toBufferedOutputStream(os: OutputStream?): BufferedOutputStream? {
        return IOUtil.toBufferedOutputStream(os)
    }

    @Override
    @Throws(IOException::class)
    fun write(res: Resource?, content: String?, append: Boolean, charset: Charset?) {
        IOUtil.write(res, content, charset, append)
    }

    @Override
    @Throws(IOException::class)
    fun write(res: Resource?, content: ByteArray?, append: Boolean) {
        IOUtil.write(res, content, append)
    }

    @Override
    @Throws(IOException::class)
    fun getReader(`is`: InputStream?, charset: Charset?): Reader? {
        return IOUtil.getReader(`is`, charset)
    }

    @Override
    @Throws(IOException::class)
    fun getReader(res: Resource?, charset: Charset?): Reader? {
        return IOUtil.getReader(res, charset)
    }

    @Override
    fun toBufferedReader(reader: Reader?): Reader? {
        return IOUtil.toBufferedReader(reader)
    }

    @Override
    @Throws(IOException::class)
    fun copy(`is`: InputStream?, out: Resource?, closeIS: Boolean) {
        IOUtil.copy(`is`, out, closeIS)
    }

    @Override
    fun createTemporaryStream(): OutputStream? {
        return TemporaryStream()
    }

    companion object {
        private var singelton: IO? = null
        val instance: IO?
            get() {
                if (singelton == null) singelton = IOImpl()
                return singelton
            }
    }
}