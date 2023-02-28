/**
 * Copyright (c) 2015, Tachyon Assosication Switzerland. All rights reserved.
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
package tachyon.runtime.util

import java.io.BufferedInputStream

interface IO {
    /**
     * close stream silently (no Exception)
     *
     * @param is Input Stream
     */
    fun closeSilent(`is`: InputStream?)

    /**
     * close stream silently (no Exception)
     *
     * @param os Output Stream
     */
    fun closeSilent(os: OutputStream?)

    /**
     * close streams silently (no Exception)
     *
     * @param is Input Stream
     * @param os Output Stream
     */
    fun closeSilent(`is`: InputStream?, os: OutputStream?)

    /**
     * close streams silently (no Exception)
     *
     * @param r Reader
     */
    fun closeSilent(r: Reader?)

    /**
     * close streams silently (no Exception)
     *
     * @param w Writer
     */
    fun closeSilent(w: Writer?)

    /**
     * close any object with a close method silently
     *
     * @param o Object
     */
    fun closeSilent(o: Object?)

    /**
     * converts an InputStream to a String
     *
     * @param is Input Stream
     * @param charset Charset
     * @return Returns the Content.
     * @throws IOException IO Exception
     */
    @Throws(IOException::class)
    fun toString(`is`: InputStream?, charset: Charset?): String?

    /**
     * reads the content of a Resource
     *
     * @param res Resource
     * @param charset Charset
     * @return Returns the Content.
     * @throws IOException IO Exception
     */
    @Throws(IOException::class)
    fun toString(res: Resource?, charset: Charset?): String?

    /**
     * converts a Byte Array to a String
     *
     * @param barr Byte Array
     * @param charset Charset
     * @return Returns the Content.
     * @throws IOException IO Exception
     */
    @Throws(IOException::class)
    fun toString(barr: ByteArray?, charset: Charset?): String?

    /**
     * reads Readers data as String
     *
     * @param r Reader
     * @return Returns the Content.
     * @throws IOException IO Exception
     */
    @Throws(IOException::class)
    fun toString(r: Reader?): String

    /**
     * copy data from Input Stream to Output Stream
     *
     * @param in Input stream
     * @param out Output stream
     * @param closeIS close Input Stream when done
     * @param closeOS close Output Stream when done
     * @throws IOException IO Exception
     */
    @Throws(IOException::class)
    fun copy(`in`: InputStream?, out: OutputStream?, closeIS: Boolean, closeOS: Boolean)

    /**
     * copy data from Reader to Writer
     *
     * @param in Input stream
     * @param out Output stream
     * @param closeR close the Reader when done
     * @param closeW close the Writer when done
     * @throws IOException IO Exception
     */
    @Throws(IOException::class)
    fun copy(`in`: Reader?, out: Writer?, closeR: Boolean, closeW: Boolean)

    /**
     * copy content from Source to Target
     *
     * @param src Source
     * @param trg Target
     * @throws IOException IO Exception
     */
    @Throws(IOException::class)
    fun copy(src: Resource?, trg: Resource?)
    fun toBufferedInputStream(`is`: InputStream?): BufferedInputStream?
    fun toBufferedOutputStream(os: OutputStream?): BufferedOutputStream?

    @Throws(IOException::class)
    fun write(res: Resource?, content: String?, append: Boolean, charset: Charset?)

    @Throws(IOException::class)
    fun write(res: Resource?, content: ByteArray?, append: Boolean)

    @Throws(IOException::class)
    fun getReader(`is`: InputStream?, charset: Charset?): Reader?

    @Throws(IOException::class)
    fun getReader(res: Resource?, charset: Charset?): Reader?
    fun toBufferedReader(reader: Reader?): Reader?

    @Throws(IOException::class)
    fun copy(`is`: InputStream?, out: Resource?, closeIS: Boolean)
    fun createTemporaryStream(): OutputStream?
}