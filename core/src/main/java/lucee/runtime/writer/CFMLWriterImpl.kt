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
package lucee.runtime.writer

import java.io.IOException

/**
 * Implementation of a JSpWriter
 */
class CFMLWriterImpl(pc: PageContext?, request: HttpServletRequest?, response: HttpServletResponse?, bufferSize: Int, autoFlush: Boolean, closeConn: Boolean, showVersion: Boolean,
                     contentLength: Boolean) : CFMLWriter(bufferSize, autoFlush) {
    // private static final String VERSIONj = Info.getVersionAsString();
    private var out: OutputStream? = null
    private val response: HttpServletResponse?
    private var flushed = false
    private var htmlHead: StringBuilder? = null
    private var htmlBody: StringBuilder? = null
    private var buffer: StringBuilder? = StringBuilder(BUFFER_SIZE)
    private var closed = false
    private val closeConn: Boolean
    private val showVersion: Boolean
    private val contentLength: Boolean
    private var cacheItem: CacheItem? = null
    private val request: HttpServletRequest?
    private var _allowCompression: Boolean? = null
    private val pc: PageContext?
    private val version: String?

    /*
	 * * constructor of the class
	 * 
	 * @param response Response Object / public JspWriterImpl(HttpServletResponse response) {
	 * this(response, BUFFER_SIZE, false); }
	 */
    @Throws(IOException::class)
    private fun _check() {
        if (autoFlush && buffer.length() > bufferSize) {
            _flush(true)
        }
    }

    /**
     * @throws IOException
     */
    @Throws(IOException::class)
    protected fun initOut() {
        if (out == null) {
            out = getOutputStream(false)
            // out=response.getWriter();
        }
    }

    /**
     * @see javax.servlet.jsp.JspWriter.print
     */
    @Override
    @Throws(IOException::class)
    fun print(arg: CharArray?) {
        buffer.append(arg)
        _check()
    }

    /**
     * reset configuration of buffer
     *
     * @param bufferSize size of the buffer
     * @param autoFlush does the buffer autoflush
     * @throws IOException
     */
    @Override
    @Throws(IOException::class)
    override fun setBufferConfig(bufferSize: Int, autoFlush: Boolean) {
        bufferSize = bufferSize
        autoFlush = autoFlush
        _check()
    }

    @Override
    @Throws(IOException::class)
    override fun appendHTMLBody(text: String?) {
        if (htmlBody == null) htmlBody = StringBuilder(256)
        htmlBody.append(text)
    }

    @Override
    @Throws(IOException::class)
    override fun writeHTMLBody(text: String?) {
        if (flushed) throw IOException("Page is already flushed")
        htmlBody = StringBuilder(text)
    }

    @Override
    @Throws(IOException::class)
    override fun getHTMLBody(): String? {
        if (flushed) throw IOException("Page is already flushed")
        return if (htmlBody == null) "" else htmlBody.toString()
    }

    @Override
    @Throws(IOException::class)
    override fun flushHTMLBody() {
        if (htmlBody != null) {
            buffer.append(htmlBody)
            resetHTMLBody()
        }
    }

    /**
     * @see lucee.runtime.writer.CFMLWriter.resetHTMLHead
     */
    @Override
    @Throws(IOException::class)
    override fun resetHTMLBody() {
        if (flushed) throw IOException("Page is already flushed")
        htmlBody = null
    }

    /**
     *
     * @param text
     * @throws IOException
     */
    @Override
    @Throws(IOException::class)
    override fun appendHTMLHead(text: String?) {
        if (flushed) throw IOException("Page is already flushed")
        if (htmlHead == null) htmlHead = StringBuilder(256)
        htmlHead.append(text)
    }

    @Override
    @Throws(IOException::class)
    override fun writeHTMLHead(text: String?) {
        if (flushed) throw IOException("Page is already flushed")
        htmlHead = StringBuilder(text)
    }

    /**
     * @see lucee.runtime.writer.CFMLWriter.getHTMLHead
     */
    @Override
    @Throws(IOException::class)
    override fun getHTMLHead(): String? {
        if (flushed) throw IOException("Page is already flushed")
        return if (htmlHead == null) "" else htmlHead.toString()
    }

    @Override
    @Throws(IOException::class)
    override fun flushHTMLHead() {
        if (htmlHead != null) {
            buffer.append(htmlHead)
            resetHTMLHead()
        }
    }

    /**
     * @see lucee.runtime.writer.CFMLWriter.resetHTMLHead
     */
    @Override
    @Throws(IOException::class)
    override fun resetHTMLHead() {
        if (flushed) throw IOException("Page is already flushed")
        htmlHead = null
    }

    /**
     * just a wrapper function for ACF
     *
     * @throws IOException
     */
    @Throws(IOException::class)
    fun initHeaderBuffer() {
        resetHTMLHead()
    }

    /**
     * @see java.io.Writer.write
     */
    @Override
    @Throws(IOException::class)
    fun write(cbuf: CharArray?, off: Int, len: Int) {
        buffer.append(cbuf, off, len)
        _check()
    }

    /**
     * @see javax.servlet.jsp.JspWriter.clear
     */
    @Override
    @Throws(IOException::class)
    fun clear() {
        if (flushed) throw IOException("Response buffer is already flushed")
        clearBuffer()
    }

    /**
     * @see javax.servlet.jsp.JspWriter.clearBuffer
     */
    @Override
    fun clearBuffer() {
        buffer = StringBuilder(BUFFER_SIZE)
    }

    /**
     * @see java.io.Writer.flush
     */
    @Override
    @Throws(IOException::class)
    fun flush() {
        flushBuffer(true)
        // weil flushbuffer das out erstellt muss ich nicht mehr checken
        out.flush()
    }

    /**
     * @see java.io.Writer.flush
     */
    @Throws(IOException::class)
    private fun _flush(closeConn: Boolean) {
        flushBuffer(closeConn)
        // weil flushbuffer das out erstellt muss ich nicht mehr checken
        out.flush()
    }

    /**
     * Flush the output buffer to the underlying character stream, without flushing the stream itself.
     * This method is non-private only so that it may be invoked by PrintStream. @throws
     * IOException @throws
     */
    @Throws(IOException::class)
    protected fun flushBuffer(closeConn: Boolean) {
        if (!flushed && closeConn) {
            response.setHeader("connection", "close")
            // if(showVersion)response.setHeader(Constants.NAME+"-Version", version);
        }
        initOut()
        val barr: ByteArray = _toString(true).getBytes(ReqRspUtil.getCharacterEncoding(null, response))
        if (cacheItem != null && cacheItem.isValid()) {
            cacheItem.store(barr, flushed)
            // writeCache(barr,flushed);
        }
        flushed = true
        out.write(barr)
        buffer = StringBuilder(BUFFER_SIZE) // to not change to clearBuffer, produce problem with CFMLWriterWhiteSpace.clearBuffer
    }

    private fun _toString(releaseHeadData: Boolean): String? {
        if (htmlBody == null && htmlHead == null) return buffer.toString()
        var str: String = buffer.toString()
        if (htmlHead != null) {
            var index: Int = StringUtil.indexOfIgnoreCase(str, "</head>")
            if (index > -1) {
                str = StringUtil.insertAt(str, htmlHead, index)
            } else {
                index = StringUtil.indexOfIgnoreCase(str, "<head>") + 7
                str = if (index > 6) {
                    StringUtil.insertAt(str, htmlHead, index)
                } else {
                    htmlHead.append(str).toString()
                }
            }
        }
        if (htmlBody != null) {
            val index: Int = StringUtil.indexOfIgnoreCase(str, "</body>")
            if (index > -1) {
                str = StringUtil.insertAt(str, htmlBody, index)
            } else {
                str += htmlBody.toString()
            }
        }
        if (releaseHeadData) {
            htmlBody = null
            htmlHead = null
        }
        return str
    }

    /**
     * @see java.lang.Object.toString
     */
    @Override
    override fun toString(): String {
        return _toString(false)!!
    }

    /**
     * @see java.io.Writer.close
     */
    @Override
    @Throws(IOException::class)
    fun close() {
        if (response == null || closed) return
        // boolean closeConn=true;
        if (out == null) {
            if (response.isCommitted()) {
                closed = true
                return
            }
            // print.out(_toString());
            val barr: ByteArray = _toString(true).getBytes(ReqRspUtil.getCharacterEncoding(null, response))
            if (cacheItem != null) {
                cacheItem.store(barr, false)
                // writeCache(barr,false);
            }
            if (closeConn) response.setHeader("connection", "close")
            // if(showVersion)response.setHeader(Constants.NAME+"-Version", version);
            val allowCompression: Boolean
            if (barr.size <= 512) allowCompression = false else if (_allowCompression != null) allowCompression = _allowCompression.booleanValue() else allowCompression = (pc as PageContextImpl?).getAllowCompression()
            out = getOutputStream(allowCompression)
            if (contentLength && out !is GZIPOutputStream) ReqRspUtil.setContentLength(response, barr.size)
            out.write(barr)
            out.flush()
            out.close()
            out = null
        } else {
            _flush(closeConn)
            out.close()
            out = null
        }
        closed = true
    }

    @Throws(IOException::class)
    private fun getOutputStream(allowCompression: Boolean): OutputStream? {
        if (allowCompression) {
            val encodings: String = ReqRspUtil.getHeader(request, "Accept-Encoding", "")
            if (encodings != null && encodings.indexOf("gzip") !== -1) {
                val inline: Boolean = HttpServletResponseWrap.get()
                if (!inline) {
                    val os: ServletOutputStream = response.getOutputStream()
                    response.setHeader("Content-Encoding", "gzip")
                    return GZIPOutputStream(os)
                }
            }
        }
        return response.getOutputStream()
    }
    /*
	 * private void writeCache(byte[] barr,boolean append) throws IOException { cacheItem.store(barr,
	 * append); //IOUtil.copy(new ByteArrayInputStream(barr),
	 * cacheItem.getResource().getOutputStream(append),true,true);
	 * //MetaData.getInstance(cacheItem.getDirectory()).add(cacheItem.getName(), cacheItem.getRaw()); }
	 */
    /**
     * @see javax.servlet.jsp.JspWriter.getRemaining
     */
    @Override
    fun getRemaining(): Int {
        return bufferSize - buffer.length()
    }

    /**
     * @see javax.servlet.jsp.JspWriter.newLine
     */
    @Override
    @Throws(IOException::class)
    fun newLine() {
        println()
    }

    /**
     * @see javax.servlet.jsp.JspWriter.print
     */
    @Override
    @Throws(IOException::class)
    fun print(arg: Boolean) {
        print(if (arg) charArrayOf('t', 'r', 'u', 'e') else charArrayOf('f', 'a', 'l', 's', 'e'))
    }

    /**
     * @see javax.servlet.jsp.JspWriter.print
     */
    @Override
    @Throws(IOException::class)
    fun print(arg: Char) {
        buffer.append(arg)
        _check()
    }

    /**
     * @see javax.servlet.jsp.JspWriter.print
     */
    @Override
    @Throws(IOException::class)
    fun print(arg: Int) {
        _print(String.valueOf(arg))
    }

    /**
     * @see javax.servlet.jsp.JspWriter.print
     */
    @Override
    @Throws(IOException::class)
    fun print(arg: Long) {
        _print(String.valueOf(arg))
    }

    /**
     * @see javax.servlet.jsp.JspWriter.print
     */
    @Override
    @Throws(IOException::class)
    fun print(arg: Float) {
        _print(String.valueOf(arg))
    }

    /**
     * @see javax.servlet.jsp.JspWriter.print
     */
    @Override
    @Throws(IOException::class)
    fun print(arg: Double) {
        _print(String.valueOf(arg))
    }

    /**
     * @see javax.servlet.jsp.JspWriter.print
     */
    @Override
    @Throws(IOException::class)
    fun print(arg: String?) {
        buffer.append(arg)
        _check()
    }

    /**
     * @see javax.servlet.jsp.JspWriter.print
     */
    @Override
    @Throws(IOException::class)
    fun print(arg: Object?) {
        _print(String.valueOf(arg))
    }

    /**
     * @see javax.servlet.jsp.JspWriter.println
     */
    @Override
    @Throws(IOException::class)
    fun println() {
        _print("\n")
    }

    /**
     * @see javax.servlet.jsp.JspWriter.println
     */
    @Override
    @Throws(IOException::class)
    fun println(arg: Boolean) {
        print(if (arg) charArrayOf('t', 'r', 'u', 'e', '\n') else charArrayOf('f', 'a', 'l', 's', 'e', '\n'))
    }

    /**
     * @see javax.servlet.jsp.JspWriter.println
     */
    @Override
    @Throws(IOException::class)
    fun println(arg: Char) {
        print(charArrayOf(arg, '\n'))
    }

    /**
     * @see javax.servlet.jsp.JspWriter.println
     */
    @Override
    @Throws(IOException::class)
    fun println(arg: Int) {
        print(arg)
        println()
    }

    /**
     * @see javax.servlet.jsp.JspWriter.println
     */
    @Override
    @Throws(IOException::class)
    fun println(arg: Long) {
        print(arg)
        println()
    }

    /**
     * @see javax.servlet.jsp.JspWriter.println
     */
    @Override
    @Throws(IOException::class)
    fun println(arg: Float) {
        print(arg)
        println()
    }

    /**
     * @see javax.servlet.jsp.JspWriter.println
     */
    @Override
    @Throws(IOException::class)
    fun println(arg: Double) {
        print(arg)
        println()
    }

    /**
     * @see javax.servlet.jsp.JspWriter.println
     */
    @Override
    @Throws(IOException::class)
    fun println(arg: CharArray?) {
        print(arg)
        println()
    }

    /**
     * @see javax.servlet.jsp.JspWriter.println
     */
    @Override
    @Throws(IOException::class)
    fun println(arg: String?) {
        _print(arg)
        println()
    }

    /**
     * @see javax.servlet.jsp.JspWriter.println
     */
    @Override
    @Throws(IOException::class)
    fun println(arg: Object?) {
        print(arg)
        println()
    }

    /**
     * @see java.io.Writer.write
     */
    @Override
    @Throws(IOException::class)
    fun write(cbuf: CharArray?) {
        print(cbuf)
    }

    /**
     * @see java.io.Writer.write
     */
    @Override
    @Throws(IOException::class)
    fun write(c: Int) {
        print(c)
    }

    /**
     * @see java.io.Writer.write
     */
    @Override
    @Throws(IOException::class)
    fun write(str: String?, off: Int, len: Int) {
        write(str.toCharArray(), off, len)
    }

    /**
     * @see java.io.Writer.write
     */
    @Override
    @Throws(IOException::class)
    fun write(str: String?) {
        buffer.append(str)
        _check()
    }

    /**
     * @see lucee.runtime.writer.CFMLWriter.writeRaw
     */
    @Override
    @Throws(IOException::class)
    override fun writeRaw(str: String?) {
        _print(str)
    }

    /**
     * @return Returns the flushed.
     */
    fun isFlushed(): Boolean {
        return flushed
    }

    @Override
    override fun setClosed(closed: Boolean) {
        this.closed = closed
    }

    @Throws(IOException::class)
    private fun _print(arg: String?) {
        buffer.append(arg)
        _check()
    }

    /**
     * @see lucee.runtime.writer.CFMLWriter.getResponseStream
     */
    @Override
    @Throws(IOException::class)
    override fun getResponseStream(): OutputStream? {
        initOut()
        return out
    }

    @Override
    override fun doCache(ci: lucee.runtime.cache.legacy.CacheItem?) {
        cacheItem = ci
    }

    /**
     * @return the cacheResource
     */
    @Override
    override fun getCacheItem(): CacheItem? {
        return cacheItem
    }

    // only for compatibility to other vendors
    fun getString(): String? {
        return toString()
    }

    @Override
    override fun setAllowCompression(allowCompression: Boolean) {
        _allowCompression = Caster.toBoolean(allowCompression)
    }

    companion object {
        private const val BUFFER_SIZE = 100000
    }

    /**
     * constructor of the class
     *
     * @param response Response Object
     * @param bufferSize buffer Size
     * @param autoFlush do auto flush Content
     */
    init {
        this.pc = pc
        this.request = request
        this.response = response
        autoFlush = autoFlush
        bufferSize = bufferSize
        this.closeConn = closeConn
        this.showVersion = showVersion
        this.contentLength = contentLength
        // this.allowCompression=allowCompression;
        version = pc.getConfig().getFactory().getEngine().getInfo().getVersion().toString()
    }
}