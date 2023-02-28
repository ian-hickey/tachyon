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
package lucee.commons.io

import java.io.BufferedInputStream

/**
 * I/O Util
 */
class IOUtil {
    /**
     * copy content of in file to out File
     *
     * @param in input
     * @param out output
     * @throws IOException
     */
    @Throws(IOException::class)
    fun copy(`in`: File?, out: File?) {
        var `is`: InputStream? = null
        var os: OutputStream? = null
        try {
            `is` = BufferedFileInputStream(`in`)
            os = BufferedFileOutputStream(out)
        } catch (ioe: IOException) {
            close(`is`, os)
            throw ioe
        }
        copy(`is`, os, true, true)
    }

    private class Copy(r: Reader, w: Writer, blockSize: Int, timeout: Long) : Thread() {
        private val r: Reader
        private val w: Writer
        private val blockSize: Int
        private val timeout: Long
        var finished = false
        var t: Throwable? = null
        val notifier: Object = Object()
        @Override
        fun run() {
            try {
                copy(r, w, blockSize, -1)
            } catch (e: Exception) {
                t = e
            } finally {
                finished = true
            }
        }

        init {
            this.r = r
            this.w = w
            this.blockSize = blockSize
            this.timeout = timeout
        }
    }

    companion object {
        /**
         * copy an inputstream to an outputstream
         *
         * @param in
         * @param out
         * @param closeIS
         * @param closeOS
         * @throws IOException
         */
        @Throws(IOException::class)
        fun copy(`in`: InputStream?, out: OutputStream?, closeIS: Boolean, closeOS: Boolean) {
            try {
                copy(`in`, out, 0xffff) // 65535
            } finally {
                if (closeIS && closeOS) close(`in`, out) else {
                    if (closeIS) close(`in`)
                    if (closeOS) close(out)
                }
            }
        }

        @Throws(IOException::class)
        fun copy(`in`: InputStream?, out: OutputStream?, blockSize: Int, closeIS: Boolean, closeOS: Boolean) {
            try {
                copy(`in`, out, blockSize) // 65535
            } finally {
                if (closeIS && closeOS) close(`in`, out) else {
                    if (closeIS) close(`in`)
                    if (closeOS) close(out)
                }
            }
        }

        /**
         * copy an inputstream to an outputstream
         *
         * @param in
         * @param out
         * @param closeIS
         * @param closeOS
         * @throws IOException
         */
        @Throws(IOException::class)
        fun merge(in1: InputStream?, in2: InputStream?, out: OutputStream?, closeIS1: Boolean, closeIS2: Boolean, closeOS: Boolean) {
            try {
                merge(in1, in2, out, 0xffff)
            } finally {
                if (closeIS1) closeEL(in1)
                if (closeIS2) closeEL(in2)
                if (closeOS) close(out)
            }
        }

        /**
         * copy an inputstream to an outputstream
         *
         * @param in
         * @param out
         * @param closeIS
         * @param closeOS
         * @throws IOException
         */
        @Throws(IOException::class)
        fun copy(out: OutputStream?, `in`: InputStream?, closeIS: Boolean, closeOS: Boolean) {
            copy(`in`, out, closeIS, closeOS)
        }

        /**
         * copy an input resource to an output resource
         *
         * @param in
         * @param out
         * @throws IOException
         */
        @Throws(IOException::class)
        fun copy(`in`: Resource, out: Resource?) {
            `in`.copyTo(out, false)
        }

        @Throws(IOException::class)
        fun merge(in1: Resource, in2: Resource, out: Resource) {
            var is1: InputStream? = null
            var is2: InputStream? = null
            var os: OutputStream? = null
            try {
                is1 = toBufferedInputStream(in1.getInputStream())
                is2 = toBufferedInputStream(in2.getInputStream())
                os = toBufferedOutputStream(out.getOutputStream())
            } catch (ioe: IOException) {
                closeEL(is1)
                closeEL(is2)
                close(os)
                throw ioe
            }
            merge(is1, is2, os, true, true, true)
        }

        /**
         * copy an input resource to an output resource
         *
         * @param in
         * @param out
         * @throws IOException
         */
        @Throws(IOException::class)
        fun copy(`is`: InputStream?, out: Resource, closeIS: Boolean) {
            var os: OutputStream? = null
            os = try {
                toBufferedOutputStream(out.getOutputStream())
            } catch (ioe: IOException) {
                close(os)
                throw ioe
            }
            copy(`is`, os, closeIS, true)
        }

        /**
         * copy an input resource to an output resource
         *
         * @param in
         * @param out
         * @throws IOException
         */
        @Throws(IOException::class)
        fun copy(`in`: Resource, os: OutputStream?, closeOS: Boolean) {
            var `is`: InputStream? = null
            `is` = try {
                toBufferedInputStream(`in`.getInputStream())
            } catch (ioe: IOException) {
                close(`is`)
                throw ioe
            }
            copy(`is`, os, true, closeOS)
        }

        @Throws(IOException::class)
        fun copy(`in`: InputStream, out: OutputStream, offset: Int, length: Int) {
            copy(`in`, out, offset, length, 0xffff)
        }

        @Throws(IOException::class)
        fun copy(`in`: InputStream, out: OutputStream, offset: Long, length: Long) {
            var offset = offset
            var length = length
            var len: Int
            var buffer: ByteArray
            var block = 0xffff

            // first offset to start
            if (offset > 0) {
                var skipped: Long = 0
                try {
                    skipped = `in`.skip(offset)
                } catch (t: Throwable) {
                    ExceptionUtil.rethrowIfNecessary(t)
                    // skipped will be -1, see below
                }
                if (skipped <= 0) {
                    while (true) {
                        if (block > offset) block = offset.toInt()
                        buffer = ByteArray(block)
                        len = `in`.read(buffer)
                        if (len == -1) throw IOException("reading offset is bigger than input itself")
                        // dnos.write(buffer, 0, len);
                        offset -= len.toLong()
                        if (offset <= 0) break
                    }
                }
            }

            // write part
            if (length < 0) {
                copy(`in`, out, block)
                return
            }
            while (true) {
                if (block > length) block = length.toInt()
                buffer = ByteArray(block)
                len = `in`.read(buffer)
                if (len == -1) break
                out.write(buffer, 0, len)
                length -= len.toLong()
                if (length <= 0) break
            }
        }

        @Throws(IOException::class)
        fun copy(`in`: InputStream, out: OutputStream, offset: Int, length: Int, blockSize: Int) {
            var offset = offset
            var length = length
            var len: Int
            var buffer: ByteArray
            var block: Int // 0xffff;

            // first offset to start
            if (offset > 0) {
                var skipped: Long = 0
                try {
                    skipped = `in`.skip(offset)
                } catch (t: Throwable) {
                    ExceptionUtil.rethrowIfNecessary(t)
                    // skipped will be -1, see below
                }
                if (skipped <= 0) {
                    block = blockSize // 0xffff;
                    while (true) {
                        if (block > offset) block = offset
                        buffer = ByteArray(block)
                        len = `in`.read(buffer)
                        if (len == -1) throw IOException("reading offset is bigger than input itself")
                        // dnos.write(buffer, 0, len);
                        offset -= len
                        if (offset <= 0) break
                    }
                }
            }

            // write part
            if (length < 0) {
                copy(`in`, out, blockSize)
                return
            }
            block = blockSize // 0xffff;
            while (true) {
                if (block > length) block = length
                buffer = ByteArray(block)
                len = `in`.read(buffer)
                if (len == -1) break
                out.write(buffer, 0, len)
                length -= len
                if (length <= 0) break
            }
        }

        /**
         * copy an inputstream to an outputstream
         *
         * @param in
         * @param out
         * @param blockSize
         * @throws IOException
         */
        @Throws(IOException::class)
        private fun copy(`in`: InputStream?, out: OutputStream?, blockSize: Int) {
            val buffer = ByteArray(blockSize)
            var len: Int
            while (`in`.read(buffer).also { len = it } != -1) {
                out.write(buffer, 0, len)
            }
        }

        /**
         * copy data from in to out, if max is reached an exception is thrown, max must be the multiply of
         * blocksize
         *
         * @param in
         * @param out
         * @param blockSize
         * @param max
         * @throws IOException
         */
        @Throws(IOException::class)
        fun copyMax(`in`: InputStream, out: OutputStream, max: Long): Boolean {
            val buffer = ByteArray(0xffff)
            var len: Int
            var total: Long = 0
            while (`in`.read(buffer).also { len = it } != -1) {
                total += len.toLong()
                out.write(buffer, 0, len)
                if (total > max) {
                    // print.e("reached:" + len + ":" + total);
                    return true
                }
            }
            return false
        }

        @Throws(IOException::class)
        private fun merge(in1: InputStream?, in2: InputStream?, out: OutputStream?, blockSize: Int) {
            copy(in1, out, blockSize)
            copy(in2, out, blockSize)
        }

        /**
         * copy a reader to a writer
         *
         * @param r
         * @param w
         * @throws IOException
         */
        @Throws(IOException::class)
        private fun copy(r: Reader, w: Writer, timeout: Long) {
            copy(r, w, 0xffff, timeout)
        }

        /**
         * copy a reader to a writer
         *
         * @param reader
         * @param writer
         * @param closeReader
         * @param closeWriter
         * @throws IOException
         */
        @Throws(IOException::class)
        fun copy(reader: Reader, writer: Writer, closeReader: Boolean, closeWriter: Boolean) {
            try {
                copy(reader, writer, 0xffff, -1)
            } finally {
                if (closeReader && closeWriter) close(reader, writer) else {
                    if (closeReader) close(reader)
                    if (closeWriter) close(writer)
                }
            }
        }

        /**
         * copy a reader to a writer
         *
         * @param r
         * @param w
         * @param blockSize
         * @throws IOException
         */
        @Throws(IOException::class)
        private fun copy(r: Reader, w: Writer, blockSize: Int, timeout: Long) {
            if (timeout < 1) {
                val buffer = CharArray(blockSize)
                var len: Int
                while (r.read(buffer).also { len = it } != -1) w.write(buffer, 0, len)
            } else {
                val c = Copy(r, w, blockSize, timeout)
                c.start()
                try {
                    c.join(timeout + 1)
                } catch (ie: InterruptedException) {
                    throw IOException(c.t)
                }
                if (c.t != null) IOException(c.t)
                if (!c.finished) throw IOException("reached timeout (" + timeout + "ms) while copying data")
            }
        }

        /**
         * close inputstream , ignore when one of the objects is null
         *
         * @param is
         * @param os
         * @throws IOException
         */
        @Throws(IOException::class)
        fun close(`is`: InputStream?, os: OutputStream?) {
            var ioe: IOException? = null
            if (`is` != null) {
                try {
                    `is`.close()
                } catch (e: IOException) {
                    ioe = e
                }
            }
            if (os != null) os.close()
            if (ioe != null) throw ioe
        }

        /**
         * close inputstream without an Exception
         *
         * @param is
         * @param os
         * @throws IOException
         */
        fun closeEL(`is`: InputStream?, os: OutputStream?) {
            closeEL(`is`)
            closeEL(os)
        }

        @Throws(SQLException::class)
        fun close(conn: Connection?) {
            if (conn != null) conn.close()
        }

        fun closeEL(conn: Connection?) {
            try {
                if (conn != null) conn.close()
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
            }
        }

        /**
         * close inputstream , ignore it when object is null
         *
         * @param is
         */
        @Throws(IOException::class)
        fun close(`is`: InputStream?) {
            if (`is` != null) `is`.close()
        }

        /**
         * close inputstream without an Exception
         *
         * @param is
         */
        fun closeEL(`is`: InputStream?) {
            try {
                if (`is` != null) `is`.close()
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
            }
        }

        @Throws(IOException::class)
        fun closeEL(zip: ZipFile?) {
            if (zip != null) zip.close()
        }

        fun closeELL(zip: ZipFile?) {
            try {
                if (zip != null) zip.close()
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
            }
        }

        /**
         * close outputstream, ignore when the object is null
         *
         * @param os
         */
        @Throws(IOException::class)
        fun close(os: OutputStream?) {
            if (os != null) os.close()
        }

        /**
         * close outputstream without an Exception
         *
         * @param os
         */
        fun closeEL(os: OutputStream?) {
            try {
                if (os != null) os.close()
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
            }
        }

        @Throws(SQLException::class)
        fun close(rs: ResultSet?) {
            if (rs != null) rs.close()
        }

        fun closeEL(rs: ResultSet?) {
            try {
                if (rs != null) rs.close()
            } catch (e: Throwable) {
                ExceptionUtil.rethrowIfNecessary(e)
            }
        }

        /**
         * close Reader if object is null it is ignored
         *
         * @param r
         */
        @Throws(IOException::class)
        fun close(r: Reader?) {
            if (r != null) r.close()
        }

        /**
         * close Reader without an Exception
         *
         * @param r
         */
        fun closeEL(r: Reader?) {
            try {
                if (r != null) r.close()
            } catch (e: Throwable) {
                ExceptionUtil.rethrowIfNecessary(e)
            }
        }

        /**
         * close Closeable, when null ignores it
         *
         * @param r
         */
        @Throws(IOException::class)
        fun close(c: Closeable?) {
            if (c != null) c.close()
        }

        @Throws(IOException::class)
        fun close(c1: Closeable?, c2: Closeable?) {
            var ioe: IOException? = null
            if (c1 != null) {
                try {
                    c1.close()
                } catch (e: IOException) {
                    ioe = e
                }
            }
            if (c2 != null) c2.close()
            if (ioe != null) throw ioe
        }

        /**
         * close Closeable without an Exception
         *
         * @param r
         */
        fun closeEL(c: Closeable?) {
            try {
                if (c != null) c.close()
            } catch (e: Throwable) {
                ExceptionUtil.rethrowIfNecessary(e)
            }
        }

        /**
         * close Writer ignore the object when null
         *
         * @param w
         */
        @Throws(IOException::class)
        fun close(w: Writer?) {
            if (w != null) w.close()
        }

        fun closeEL(w: Writer?) {
            try {
                if (w != null) w.close()
            } catch (e: Throwable) {
                ExceptionUtil.rethrowIfNecessary(e)
            }
        }

        /**
         * call close method from any Object with a close method.
         *
         * @param obj
         * @throws SQLException
         */
        @Throws(Exception::class)
        fun close(obj: Object) {
            if (obj is InputStream) close(obj as InputStream) else if (obj is OutputStream) close(obj as OutputStream) else if (obj is Writer) close(obj as Writer) else if (obj is Reader) close(obj as Reader) else if (obj is Closeable) close(obj as Closeable) else if (obj is ZipFile) closeEL(obj as ZipFile) else if (obj is ResultSet) close(obj as ResultSet) else if (obj is Connection) close(obj as Connection) else {
                try {
                    val method: Method = obj.getClass().getMethod("close", arrayOfNulls<Class>(0))
                    method.invoke(obj, arrayOfNulls<Object>(0))
                } catch (e: Throwable) {
                    ExceptionUtil.rethrowIfNecessary(e)
                }
            }
        }

        /**
         * call close method from any Object with a close method.
         *
         * @param obj
         */
        fun closeEL(obj: Object) {
            if (obj is InputStream) closeEL(obj as InputStream) else if (obj is OutputStream) closeEL(obj as OutputStream) else if (obj is Writer) closeEL(obj as Writer) else if (obj is Reader) closeEL(obj as Reader) else if (obj is Closeable) closeEL(obj as Closeable) else if (obj is ZipFile) closeELL(obj as ZipFile) else if (obj is ResultSet) closeEL(obj as ResultSet) else if (obj is Connection) closeEL(obj as Connection) else {
                try {
                    val method: Method = obj.getClass().getMethod("close", arrayOfNulls<Class>(0))
                    method.invoke(obj, arrayOfNulls<Object>(0))
                } catch (e: Throwable) {
                    ExceptionUtil.rethrowIfNecessary(e)
                }
            }
        }

        /**
         * @param res
         * @param charset
         * @return
         * @throws IOException
         */
        @Deprecated
        @Deprecated("""use instead <code>{@link #getReader(Resource, Charset)}</code>
	  """)
        @Throws(IOException::class)
        fun getReader(res: Resource?, charset: String?): Reader {
            return getReader(res, CharsetUtil.toCharset(charset))
        }

        @Throws(IOException::class)
        fun getReader(res: Resource, charset: Charset?): Reader {
            /*
		 * 00 00 FE FF UTF-32, big-endian FF FE 00 00 UTF-32, little-endian
		 */
            var `is`: InputStream? = null
            try {
                `is` = res.getInputStream()
                val markSupported: Boolean = `is`.markSupported()
                if (markSupported) `is`.mark(4)
                val first: Int = `is`.read()
                val second: Int = `is`.read()
                // FE FF UTF-16, big-endian
                if (first == 0xFE && second == 0xFF) {
                    return _getReader(`is`, CharsetUtil.UTF16BE)
                }
                // FF FE UTF-16, little-endian
                if (first == 0xFF && second == 0xFE) {
                    return _getReader(`is`, CharsetUtil.UTF16LE)
                }
                val third: Int = `is`.read()
                // EF BB BF UTF-8
                if (first == 0xEF && second == 0xBB && third == 0xBF) {
                    // is.reset();
                    return _getReader(`is`, CharsetUtil.UTF8)
                }
                /*
			 * int forth=is.read(); // 00 00 FE FF UTF-32, big-endian if (first == 0x00 && second == 0x00 &&
			 * third == 0xFE && forth == 0xFF) { is.reset(); return _getReader(is, "utf-32"); } // FF FE 00 00
			 * UTF-32, little-endian if (first == 0xFF && second == 0xFE && third == 0x00 && forth == 0x00) {
			 * is.reset(); return _getReader(is, "utf-32"); }
			 */if (markSupported) {
                    `is`.reset()
                    return _getReader(`is`, charset)
                }
            } catch (ioe: IOException) {
                close(`is`)
                throw ioe
            }

            // when mark not supported return new reader
            close(`is`)
            `is` = null
            `is` = try {
                res.getInputStream()
            } catch (ioe: IOException) {
                close(`is`)
                throw ioe
            }
            return _getReader(`is`, charset)
        }

        @Throws(IOException::class)
        fun getReader(`is`: InputStream, charset: Charset?): Reader {
            val markSupported: Boolean = `is`.markSupported()
            if (!markSupported) return _getReader(`is`, charset)
            if (markSupported) `is`.mark(4)
            val first: Int = `is`.read()
            val second: Int = `is`.read()
            // FE FF UTF-16, big-endian
            if (first == 0xFE && second == 0xFF) {
                // is.reset();
                return _getReader(`is`, CharsetUtil.UTF16BE)
            }
            // FF FE UTF-16, little-endian
            if (first == 0xFF && second == 0xFE) {
                // TODO FF FE 00 00 UTF-32 little-endian
                return _getReader(`is`, CharsetUtil.UTF16LE)
            }
            val third: Int = `is`.read()
            // EF BB BF UTF-8
            if (first == 0xEF && second == 0xBB && third == 0xBF) {
                return _getReader(`is`, CharsetUtil.UTF8)
            }

            // 00 00 FE FF UTF-32 big-endian
            val forth: Int = `is`.read()
            if (first == 0x00 && second == 0x00 && third == 0xFE && forth == 0xFF) {
                return _getReader(`is`, CharsetUtil.UTF32BE)
            }
            `is`.reset()
            return _getReader(`is`, charset)
        }

        /**
         * @param is
         * @param charset
         * @return
         * @throws IOException
         */
        @Deprecated
        @Deprecated("""use instead <code>{@link #getReader(InputStream, Charset)}</code>
	  """)
        @Throws(IOException::class)
        fun getReader(`is`: InputStream?, charset: String?): Reader {
            return getReader(`is`, CharsetUtil.toCharset(charset))
        }

        /**
         * returns a Reader for the given InputStream
         *
         * @param is
         * @param charset
         * @return Reader
         * @throws IOException
         */
        @Throws(IOException::class)
        private fun _getReader(`is`: InputStream?, charset: Charset?): Reader {
            var charset: Charset? = charset
            if (charset == null) charset = SystemUtil.getCharset()
            return BufferedReader(InputStreamReader(`is`, charset))
        }

        /**
         * @param is
         * @param charset
         * @return
         * @throws IOException
         */
        @Deprecated
        @Deprecated("""use instead <code>{@link #toString(InputStream, Charset)}</code>
	  """)
        @Throws(IOException::class)
        fun toString(`is`: InputStream?, charset: String?): String {
            return toString(`is`, CharsetUtil.toCharset(charset))
        }

        /**
         * reads string data from an InputStream
         *
         * @param is
         * @param charset
         * @return string from inputstream
         * @throws IOException
         */
        @Throws(IOException::class)
        fun toString(`is`: InputStream?, charset: Charset?): String {
            return toString(getReader(`is`, charset))
        }

        /**
         * reads string data from an InputStream
         *
         * @param is
         * @param charset
         * @param timeout in milliseconds
         * @return string from inputstream
         * @throws IOException
         */
        @Throws(IOException::class)
        fun toString(`is`: InputStream?, charset: Charset?, timeout: Long): String {
            return toString(getReader(`is`, charset), timeout)
        }

        /**
         * @param barr
         * @param charset
         * @return
         * @throws IOException
         */
        @Deprecated
        @Deprecated("""use instead <code>{@link #toString(byte[], Charset)}</code>
	  """)
        @Throws(IOException::class)
        fun toString(barr: ByteArray?, charset: String?): String {
            return toString(barr, CharsetUtil.toCharset(charset))
        }

        @Throws(IOException::class)
        fun toString(barr: ByteArray?, charset: Charset?): String {
            return toString(getReader(ByteArrayInputStream(barr), charset))
        }

        /**
         * reads String data from a Reader
         *
         * @param reader
         * @return readed string
         * @throws IOException
         */
        @Throws(IOException::class)
        fun toString(reader: Reader?): String {
            return toString(reader, -1)
        }

        /**
         * reads String data from a Reader
         *
         * @param reader
         * @param timeout timeout in milliseconds
         * @return readed string
         * @throws IOException
         */
        @Throws(IOException::class)
        fun toString(reader: Reader, timeout: Long): String {
            val sw = StringWriter(512)
            copy(toBufferedReader(reader), sw, timeout)
            sw.close()
            return sw.toString()
        }

        /**
         * reads String data from a Reader
         *
         * @param reader
         * @return readed string
         * @throws IOException
         */
        @Throws(IOException::class)
        fun toString(reader: Reader, buffered: Boolean): String {
            val sw = StringWriter(512)
            if (buffered) copy(toBufferedReader(reader), sw, -1) else copy(reader, sw, -1)
            sw.close()
            return sw.toString()
        }

        /**
         * @param file
         * @param charset
         * @return
         * @throws IOException
         */
        @Deprecated
        @Deprecated("""use instead <code>{@link #toString(Resource, Charset)}</code>
	  """)
        @Throws(IOException::class)
        fun toString(file: Resource?, charset: String?): String {
            return toString(file, CharsetUtil.toCharset(charset))
        }

        /**
         * reads String data from File
         *
         * @param file
         * @param charset
         * @return readed string
         * @throws IOException
         */
        @Throws(IOException::class)
        fun toString(file: Resource?, charset: Charset?): String {
            var r: Reader? = null
            return try {
                r = getReader(file, charset)
                toString(r)
            } finally {
                close(r)
            }
        }

        /**
         * @param reader Reader to get content from it
         * @return returns the content of the file as String Array (Line by Line)
         * @throws IOException
         */
        @Throws(IOException::class)
        fun toStringArray(reader: Reader?): Array<String?> {
            if (reader == null) return arrayOfNulls(0)
            val br = BufferedReader(reader)
            val list: LinkedList<String> = LinkedList<String>()
            var line: String?
            while (br.readLine().also { line = it } != null) {
                list.add(line)
            }
            br.close()
            val content = arrayOfNulls<String>(list.size())
            var count = 0
            while (!list.isEmpty()) {
                content[count++] = list.removeFirst()
            }
            return content
        }

        /**
         * @param file
         * @param string String to write to file
         * @param charset
         * @param append append to cuuretn data or overwrite existing data
         * @throws IOException
         */
        @Deprecated
        @Deprecated("""use instead <code>{@link #write(Resource, String, Charset, boolean)}</code> writes a
	              String to an object
	  """)
        @Throws(IOException::class)
        fun write(file: File?, string: String?, strCharset: String?, append: Boolean) {
            val charset: Charset
            charset = if (StringUtil.isEmpty(strCharset)) {
                SystemUtil.getCharset()
            } else CharsetUtil.toCharset(strCharset)
            var writer: OutputStreamWriter? = null
            try {
                writer = OutputStreamWriter(BufferedFileOutputStream(file, append), charset)
                writer.write(string)
            } finally {
                close(writer)
            }
        }

        /**
         * @param res
         * @param string
         * @param charset
         * @param append
         * @throws IOException
         */
        @Deprecated
        @Deprecated("""use instead <code>{@link #write(Resource, String, Charset, boolean)}</code>
	  """)
        @Throws(IOException::class)
        fun write(res: Resource?, string: String?, charset: String?, append: Boolean) {
            write(res, string, CharsetUtil.toCharset(charset), append)
        }

        @Throws(IOException::class)
        fun write(res: Resource?, string: String?, charset: Charset?, append: Boolean) {
            var charset: Charset? = charset
            if (charset == null) {
                charset = SystemUtil.getCharset()
            }
            var writer: Writer? = null
            try {
                writer = getWriter(res, charset, append)
                writer.write(string)
            } finally {
                close(writer)
            }
        }

        @Throws(IOException::class)
        fun write(res: Resource, barr: ByteArray?) {
            val bais = ByteArrayInputStream(barr)
            val os: OutputStream = toBufferedOutputStream(res.getOutputStream())
            copy(bais, os, true, true)
        }

        @Throws(IOException::class)
        fun write(res: Resource, barr: ByteArray?, append: Boolean) {
            val bais = ByteArrayInputStream(barr)
            val os: OutputStream = toBufferedOutputStream(res.getOutputStream(append))
            copy(bais, os, true, true)
        }

        /**
         * @param file
         * @return returns the Content of the file as byte array
         * @throws IOException
         */
        @Deprecated
        @Deprecated("""use instead <code>{@link #toBytes(Resource)}</code>
	  """)
        @Throws(IOException::class)
        fun toBytes(file: File?): ByteArray {
            var bfis: BufferedFileInputStream? = null
            return try {
                bfis = BufferedFileInputStream(file)
                toBytes(bfis)
            } finally {
                close(bfis)
            }
        }

        /**
         * @param res
         * @return returns the Content of the file as byte array
         * @throws IOException
         */
        @Throws(IOException::class)
        fun toBytes(res: Resource): ByteArray {
            var bfis: BufferedInputStream? = null
            return try {
                bfis = toBufferedInputStream(res.getInputStream())
                toBytes(bfis)
            } finally {
                close(bfis)
            }
        }

        fun toBufferedInputStream(`is`: InputStream?): BufferedInputStream? {
            return if (`is` is BufferedInputStream) `is` as BufferedInputStream? else BufferedInputStream(`is`)
        }

        fun toBufferedOutputStream(os: OutputStream): BufferedOutputStream {
            return if (os is BufferedOutputStream) os as BufferedOutputStream else BufferedOutputStream(os)
        }

        fun toBufferedReader(r: Reader): BufferedReader {
            return if (r is BufferedReader) r as BufferedReader else BufferedReader(r)
        }

        /**
         * @param res
         * @param charset
         * @return
         * @throws IOException
         */
        @Deprecated
        @Deprecated("""use instead <code>{@link #getBufferedReader(Resource, Charset)}</code>
	  """)
        @Throws(IOException::class)
        fun getBufferedReader(res: Resource?, charset: String?): BufferedReader {
            return getBufferedReader(res, CharsetUtil.toCharset(charset))
        }

        @Throws(IOException::class)
        fun getBufferedReader(res: Resource?, charset: Charset?): BufferedReader {
            return toBufferedReader(getReader(res, charset))
        }

        fun toBufferedWriter(w: Writer?): BufferedWriter? {
            return if (w is BufferedWriter) w as BufferedWriter? else BufferedWriter(w)
        }

        /**
         * @param is
         * @return returns the Content of the file as byte array
         * @throws IOException
         */
        @Throws(IOException::class)
        fun toBytes(`is`: InputStream?): ByteArray {
            return toBytes(`is`, false)
        }

        @Throws(IOException::class)
        fun toBytes(`is`: InputStream?, closeStream: Boolean): ByteArray {
            val baos = ByteArrayOutputStream()
            copy(`is`, baos, closeStream, true)
            return baos.toByteArray()
        }

        @Throws(IOException::class)
        fun toBytesMax(`is`: InputStream, max: Long, maxReached: RefBoolean): ByteArray {
            val baos = ByteArrayOutputStream()
            maxReached.setValue(copyMax(`is`, baos, max))
            return baos.toByteArray()
        }

        fun toBytes(`is`: InputStream?, closeStream: Boolean, defaultValue: ByteArray): ByteArray {
            return try {
                val baos = ByteArrayOutputStream()
                copy(`is`, baos, closeStream, true)
                baos.toByteArray()
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
                defaultValue
            }
        }

        @Throws(IOException::class)
        fun toBytesMax(`is`: InputStream, max: Int): ByteArray {
            val baos = ByteArrayOutputStream()
            copy(`is`, baos, 0, max)
            return baos.toByteArray()
        }

        /**
         * flush OutputStream without an Exception
         *
         * @param os
         */
        fun flushEL(os: OutputStream?) {
            try {
                if (os != null) os.flush()
            } catch (e: Exception) {
            }
        }

        /**
         * flush OutputStream without an Exception
         *
         * @param os
         */
        fun flushEL(w: Writer?) {
            try {
                if (w != null) w.flush()
            } catch (e: Exception) {
            }
        }

        /**
         * check if given encoding is ok
         *
         * @param encoding
         * @throws PageException
         */
        @Throws(IOException::class)
        fun checkEncoding(encoding: String) {
            try {
                URLEncoder.encode("", encoding)
            } catch (e: UnsupportedEncodingException) {
                throw IOException("invalid encoding [$encoding]")
            }
        }

        /**
         * return the mime type of a file, dont check extension
         *
         * @param barr
         * @param defaultValue
         * @return mime type of the file
         */
        fun getMimeType(`is`: InputStream, defaultValue: String): String {
            return try {
                getMimeType(toBytesMax(`is`, 1000), defaultValue)
            } catch (e: IOException) {
                defaultValue
            }
        }

        /**
         * return the mime type of a file, dont check extension
         *
         * @param barr
         * @return mime type of the file
         * @throws IOException
         */
        fun getMimeType(barr: ByteArray?, defaultValue: String): String {
            return try {
                val tika = Tika()
                tika.detect(barr)
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
                defaultValue
            }
        }

        fun getMimeType(res: Resource, defaultValue: String?): String? {
            return getMimeType(res, null, defaultValue)
        }

        fun getMimeType(fileName: String?, defaultValue: String): String {
            return try {
                val tika = Tika()
                tika.detect(fileName)
            } catch (e: Exception) {
                defaultValue
            }
        }

        fun getMimeType(res: Resource, fileName: String?, defaultValue: String?): String? {
            val md = Metadata()
            val ext: String? = if (StringUtil.isEmpty(fileName, true)) null else ResourceUtil.getExtension(fileName.trim(), null)
            md.set(Metadata.RESOURCE_NAME_KEY, if (ext == null) res.getName() else fileName.trim())
            md.set(Metadata.CONTENT_LENGTH, Long.toString(res.length()))
            var `is`: InputStream? = null
            return try {
                val tika = Tika()
                val result: String = tika.detect(res.getInputStream().also { `is` = it }, md)
                if (result.indexOf("tika") !== -1) {
                    val tmp: String = ResourceUtil.EXT_MT.get(ext ?: ResourceUtil.getExtension(res, "").toLowerCase())
                    if (!StringUtil.isEmpty(tmp)) return tmp
                    if (!StringUtil.isEmpty(defaultValue)) return defaultValue
                }
                result
            } catch (e: Exception) {
                val tmp: String = ResourceUtil.EXT_MT.get(ext ?: ResourceUtil.getExtension(res, "").toLowerCase())
                if (tmp != null && tmp.indexOf("tika") === -1 && !StringUtil.isEmpty(tmp)) tmp else defaultValue
            } finally {
                closeEL(`is`)
            }
        }

        fun getMimeType(url: URL?, defaultValue: String): String {
            return try {
                val tika = Tika()
                tika.detect(url)
            } catch (e: Exception) {
                defaultValue
            }
        }

        /**
         * @param res
         * @param charset
         * @return
         * @throws IOException
         */
        @Deprecated
        @Deprecated("""use instead <code>{@link #getWriter(Resource, Charset)}</code>
	  """)
        @Throws(IOException::class)
        fun getWriter(res: Resource?, charset: String?): Writer {
            return getWriter(res, CharsetUtil.toCharset(charset))
        }

        @Throws(IOException::class)
        fun getWriter(res: Resource, charset: Charset?): Writer {
            var os: OutputStream? = null
            os = try {
                res.getOutputStream()
            } catch (ioe: IOException) {
                close(os)
                throw ioe
            }
            return getWriter(os, charset)
        }

        /**
         * @param res
         * @param charset
         * @param append
         * @return
         * @throws IOException
         */
        @Deprecated
        @Deprecated("""use instead <code>{@link #getWriter(Resource, Charset,boolean)}</code>
	  """)
        @Throws(IOException::class)
        fun getWriter(res: Resource?, charset: String?, append: Boolean): Writer {
            return getWriter(res, CharsetUtil.toCharset(charset), append)
        }

        @Throws(IOException::class)
        fun getWriter(res: Resource, charset: Charset?, append: Boolean): Writer {
            var os: OutputStream? = null
            os = try {
                res.getOutputStream(append)
            } catch (ioe: IOException) {
                close(os)
                throw ioe
            }
            return getWriter(os, charset)
        }

        /**
         * @param file
         * @param charset
         * @return Reader
         * @throws IOException
         */
        @Deprecated
        @Deprecated("""use instead <code>{@link #getWriter(Resource, Charset)}</code> returns a Reader for
	              the given File and charset (Automatically check BOM Files)
	  """)
        @Throws(IOException::class)
        fun getWriter(file: File?, charset: String?): Writer {
            var os: OutputStream? = null
            try {
                os = FileOutputStream(file)
            } catch (ioe: IOException) {
                close(os)
                throw ioe
            }
            return getWriter(os, charset)
        }

        /**
         * @param file
         * @param charset
         * @return Reader
         * @throws IOException
         */
        @Deprecated
        @Deprecated("""use instead <code>{@link #getWriter(Resource, Charset, boolean)}</code> returns a
	              Reader for the given File and charset (Automatically check BOM Files)
	  """)
        @Throws(IOException::class)
        fun getWriter(file: File?, charset: String?, append: Boolean): Writer {
            var os: OutputStream? = null
            try {
                os = FileOutputStream(file, append)
            } catch (ioe: IOException) {
                close(os)
                throw ioe
            }
            return getWriter(os, charset)
        }

        /**
         * @param os
         * @param charset
         * @return
         * @throws IOException
         */
        @Deprecated
        @Deprecated("""use instead <code>{@link #getWriter(OutputStream, Charset)}</code>
	  """)
        @Throws(IOException::class)
        fun getWriter(os: OutputStream?, charset: String?): Writer {
            return getWriter(os, CharsetUtil.toCharset(charset))
        }

        /**
         * returns a Reader for the given InputStream
         *
         * @param is
         * @param charset
         * @return Reader
         * @throws IOException
         */
        @Throws(IOException::class)
        fun getWriter(os: OutputStream?, charset: Charset?): Writer {
            var charset: Charset? = charset
            if (charset == null) charset = SystemUtil.getCharset()
            return BufferedWriter(OutputStreamWriter(os, charset))
        }

        @Throws(IOException::class)
        fun read(reader: Reader, size: Int): String? {
            return read(reader, CharArray(size))
        }

        @Throws(IOException::class)
        fun read(reader: Reader, carr: CharArray?): String? {
            val rst: Int = reader.read(carr)
            return if (rst == -1) null else String(carr, 0, rst)
        }
    }
}