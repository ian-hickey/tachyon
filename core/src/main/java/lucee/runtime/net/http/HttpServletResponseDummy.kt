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

/**
 *
 */
class HttpServletResponseDummy @JvmOverloads constructor(out: OutputStream? = DevNullOutputStream.DEV_NULL_OUTPUT_STREAM) : HttpServletResponse, Serializable {
    private var cookies: Array<Cookie?>? = arrayOfNulls<Cookie?>(0)
    private var headers: Array<Pair<String?, Object?>?>? = arrayOfNulls<Pair?>(0)

    /**
     * @return the status
     */
    @get:Override
    @set:Override
    var status = 200

    /**
     * @return the statusCode
     */
    var statusCode: String? = "OK"
        private set

    /**
     * @return the charset
     */
    @get:Override
    @set:Override
    var charsetEncoding: String? = "ISO-8859-1"
        get() = field
        set

    /**
     * @return the contentLength
     */
    @set:Override
    var contentLength = -1

    /**
     * @return the contentType
     */
    @get:Override
    @set:Override
    var contentType: String? = null

    @get:Override
    @set:Override
    var locale: Locale? = Locale.getDefault()

    @get:Override
    @set:Override
    var bufferSize = -1

    /**
     * @return the commited
     */
    var isCommitted = false
        private set
        @Override get() = field
    set
    // private byte[] outputDatad;
    private val out // =new DevNullOutputStream();
            : OutputStream?
    private var outInit = false
    @Override
    fun addCookie(cookie: Cookie?) {
        val tmp: Array<Cookie?> = arrayOfNulls<Cookie?>(cookies!!.size + 1)
        for (i in cookies.indices) {
            tmp[i] = cookies!![i]
        }
        tmp[cookies!!.size] = cookie
        cookies = tmp
    }

    @Override
    fun containsHeader(key: String?): Boolean {
        return ReqRspUtil.get(headers, key) != null
    }

    @Override
    fun encodeURL(value: String?): String? {
        return URLEncoder.encode(value)
    }

    @Override
    fun encodeRedirectURL(url: String?): String? {
        return URLEncoder.encode(url)
    }

    @Override
    fun encodeUrl(value: String?): String? {
        return URLEncoder.encode(value)
    }

    @Override
    fun encodeRedirectUrl(value: String?): String? {
        return URLEncoder.encode(value)
    }

    @Override
    @Throws(IOException::class)
    fun sendError(code: Int, codeText: String?) {
        // TODO impl
    }

    @Override
    @Throws(IOException::class)
    fun sendError(code: Int) {
        // TODO impl
    }

    @Override
    @Throws(IOException::class)
    fun sendRedirect(location: String?) {
        addHeader("location", location)
    }

    @Override
    fun setDateHeader(key: String?, value: Long) {
        setHeader(key, DateTimeImpl(value, false).castToString())
    }

    @Override
    fun addDateHeader(key: String?, value: Long) {
        addHeader(key, DateTimeImpl(value, false).castToString())
    }

    @Override
    fun setHeader(key: String?, value: String?) {
        headers = ReqRspUtil.set(headers, key, value)
    }

    @Override
    fun addHeader(key: String?, value: String?) {
        headers = ReqRspUtil.add(headers, key, value)
    }

    @Override
    fun setIntHeader(key: String?, value: Int) {
        setHeader(key, String.valueOf(value))
    }

    @Override
    fun addIntHeader(key: String?, value: Int) {
        addHeader(key, String.valueOf(value))
    }

    @Override
    fun setStatus(status: Int, statusCode: String?) {
        status = status
        this.statusCode = statusCode
    }

    @get:Throws(IOException::class)
    @get:Override
    val outputStream: ServletOutputStream?
        get() {
            if (outInit) throw IOException("output already initallised")
            outInit = true
            return ServletOutputStreamDummy(out)
        }

    @get:Throws(IOException::class)
    @get:Override
    val writer: PrintWriter?
        get() {
            if (outInit) throw IOException("output already initallised")
            outInit = true
            return PrintWriter(out)
        }

    @Override
    @Throws(IOException::class)
    fun flushBuffer() {
        isCommitted = true
    }

    @Override
    fun resetBuffer() {
        isCommitted = true
    }

    @Override
    fun reset() {
        isCommitted = true
    }

    /**
     * @return the cookies
     */
    fun getCookies(): Array<Cookie?>? { // do not chnage this is used in flex extension
        return cookies
    }

    /**
     * @return the headers
     */
    fun getHeaders(): Array<Pair<String?, Object?>?>? {
        return headers
    }

    @get:Override
    val headerNames: Collection<String?>?
        get() {
            val names: Set<String?> = HashSet<String?>()
            for (i in headers.indices) {
                names.add(headers!![i].getName())
            }
            return names
        }

    @Override
    fun getHeaders(name: String?): Collection<String?>? {
        val values: Set<String?> = HashSet<String?>()
        for (i in headers.indices) {
            if (headers!![i].getName().equals(name)) {
                values.add(Caster.toString(headers!![i].getValue(), null))
            }
        }
        return if (values.size() === 0) null else values
    }

    @Override
    fun getHeader(name: String?): String? {
        for (i in headers.indices) {
            if (headers!![i].getName().equals(name)) {
                return Caster.toString(headers!![i].getValue(), null)
            }
        }
        return null
    }

    /*
	 * *
	 * 
	 * @return the outputData / public byte[] getOutputData() { return outputData; }
	 * 
	 * public void setOutputData(byte[] outputData) { this.outputData=outputData; }
	 */
    @Override
    fun setContentLengthLong(length: Long) {
        ReqRspUtil.setContentLength(this, length)
    }

    /**
     * Constructor of the class
     */
    init {
        this.out = out
    }
}