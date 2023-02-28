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
package tachyon.runtime.net.http

import java.io.IOException

/**
 *
 */
class HttpServletResponseWrap(rsp: HttpServletResponse?, out: OutputStream?) : HttpServletResponseWrapper(rsp), HttpServletResponse, Serializable {
    private var cookies: Array<Cookie?>? = arrayOfNulls<Cookie?>(0)
    private var headers: Array<Pair?>? = arrayOfNulls<Pair?>(0)

    /**
     * @return the status
     */
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
    private val outInit = false
    private var writer: PrintWriter? = null
    private var outputStream: ServletOutputStreamDummy? = null

    /**
     * Constructor of the class
     */
    constructor(rsp: HttpServletResponse?) : this(rsp, DevNullOutputStream.DEV_NULL_OUTPUT_STREAM) {}

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

    @Override
    @Throws(IOException::class)
    fun getOutputStream(): ServletOutputStream? {
        // if(writer!=null) throw new IOException("output already initallised as Writer");
        return if (outputStream != null) outputStream else ServletOutputStreamDummy(out).also { outputStream = it }
    }

    val existingOutputStream: ServletOutputStream?
        get() = outputStream

    @Override
    @Throws(IOException::class)
    fun getWriter(): PrintWriter? {
        // if(outputStream!=null) throw new IOException("output already initallised as OutputStream");
        return if (writer != null) writer else PrintWriter(getOutputStream()).also { writer = it }
    }

    val existingWriter: PrintWriter?
        get() = writer

    @Override
    @Throws(IOException::class)
    fun flushBuffer() {
        if (writer != null) writer.flush() else if (outputStream != null) outputStream.flush()
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
    fun getCookies(): Array<Cookie?>? {
        return cookies
    }

    /**
     * @return the headers
     */
    fun getHeaders(): Array<Pair?>? {
        return headers
    }

    /*
	 * *
	 * 
	 * @return the outputData / public byte[] getOutputData() { return outputData; }
	 * 
	 * public void setOutputData(byte[] outputData) { this.outputData=outputData; }
	 */
    companion object {
        private val local: ThreadLocal<Boolean?>? = ThreadLocal<Boolean?>()
        fun set(value: Boolean) {
            local.set(Caster.toBoolean(value))
        }

        fun get(): Boolean {
            return Caster.toBooleanValue(local.get(), false)
        }

        fun release() {
            local.set(Boolean.FALSE)
        }
    }

    init {
        this.out = out
    }
}