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
package lucee.runtime.tag

import java.io.BufferedInputStream

/**
 * Defines the MIME type returned by the current page. Optionally, lets you specify the name of a
 * file to be returned with the page.
 */
class Content : BodyTagImpl() {
    /** Defines the File/ MIME content type returned by the current page.  */
    private var type: String? = null

    /** The name of the file being retrieved  */
    private var strFile: String? = null

    /**
     * Yes or No. Yes discards output that precedes the call to cfcontent. No preserves the output that
     * precedes the call. Defaults to Yes. The reset and file attributes are mutually exclusive. If you
     * specify a file, the reset attribute has no effect.
     */
    private var reset = true
    private var _range = RANGE_NONE

    /**
     * Yes or No. Yes deletes the file after the download operation. Defaults to No. This attribute
     * applies only if you specify a file with the file attribute.
     */
    private var deletefile = false
    private var content: ByteArray?
    @Override
    fun release() {
        super.release()
        type = null
        strFile = null
        reset = true
        deletefile = false
        content = null
        _range = RANGE_NONE
    }

    /**
     * set the value type Defines the File/ MIME content type returned by the current page.
     *
     * @param type value to set
     */
    fun setType(type: String?) {
        this.type = type.trim()
    }

    fun setRange(range: Boolean) {
        _range = if (range) RANGE_YES else RANGE_NO
    }

    /**
     * set the value file The name of the file being retrieved
     *
     * @param file value to set
     */
    fun setFile(file: String?) {
        strFile = file
    }

    /**
     * the content to output as binary
     *
     * @param content value to set
     */
    @Deprecated
    @Deprecated("replaced with <code>{@link #setVariable(Object)}</code>")
    fun setContent(content: ByteArray?) {
        this.content = content
    }

    @Throws(PageException::class)
    fun setVariable(variable: Object?) {
        if (variable is String) content = Caster.toBinary(pageContext.getVariable(variable as String?)) else content = Caster.toBinary(variable)
    }

    /**
     * set the value reset Yes or No. Yes discards output that precedes the call to cfcontent. No
     * preserves the output that precedes the call. Defaults to Yes. The reset and file attributes are
     * mutually exclusive. If you specify a file, the reset attribute has no effect.
     *
     * @param reset value to set
     */
    fun setReset(reset: Boolean) {
        this.reset = reset
    }

    /**
     * set the value deletefile Yes or No. Yes deletes the file after the download operation. Defaults
     * to No. This attribute applies only if you specify a file with the file attribute.
     *
     * @param deletefile value to set
     */
    fun setDeletefile(deletefile: Boolean) {
        this.deletefile = deletefile
    }

    @Override
    @Throws(PageException::class)
    fun doStartTag(): Int {
        // try {
        return _doStartTag()
        /*
		 * } catch (IOException e) { throw Caster.toPageException(e); }
		 */
    }

    @Throws(PageException::class)
    private fun _doStartTag(): Int {
        // check the file before doing anything else
        var file: Resource? = null
        if (content == null && !StringUtil.isEmpty(strFile)) file = ResourceUtil.toResourceExisting(pageContext, strFile)
        if (content == null && !StringUtil.isEmpty(strFile)) {
            file = ResourceUtil.toResourceExisting(pageContext, strFile)
            // Do not overwrite type-attribute
            if (StringUtil.isEmpty(type, true)) {
                type = ResourceUtil.getMimeType(file, "text/html")
            }
        }

        // get response object
        val rsp: HttpServletResponse = pageContext.getHttpServletResponse()

        // check committed
        if (rsp.isCommitted()) throw ApplicationException("Content was already flushed", "you can't rewrite the header of a response after part of the page was flushed")

        // set type
        if (!StringUtil.isEmpty(type, true)) {
            type = type.trim()
            ReqRspUtil.setContentType(rsp, type)

            // TODO more dynamic implementation, configuration in admin?
            if (!(HTTPUtil.isTextMimeType(type) === Boolean.TRUE)) {
                (pageContext as PageContextImpl?).getRootOut().setAllowCompression(false)
            }
        }
        val ranges = ranges
        var hasRanges = ranges != null && ranges.size > 0
        if (_range == RANGE_YES || hasRanges) {
            rsp.setHeader("Accept-Ranges", "bytes")
        } else if (_range == RANGE_NO) {
            rsp.setHeader("Accept-Ranges", "none")
            hasRanges = false
        }

        // set content
        if (content != null || file != null) {
            pageContext.clear()
            var `is`: InputStream? = null
            var os: OutputStream? = null
            val totalLength: Long
            var contentLength: Long
            try {
                os = outputStream
                if (content != null) {
                    // ReqRspUtil.setContentLength(rsp,content.length);
                    contentLength = content!!.size.toLong()
                    totalLength = content!!.size.toLong()
                    `is` = BufferedInputStream(ByteArrayInputStream(content))
                } else {
                    // ReqRspUtil.setContentLength(rsp,file.length());
                    pageContext.getConfig().getSecurityManager().checkFileLocation(file)
                    totalLength = file.length()
                    contentLength = totalLength
                    `is` = IOUtil.toBufferedInputStream(file.getInputStream())
                }

                // write
                if (!hasRanges) IOUtil.copy(`is`, os, false, false) else {
                    contentLength = 0
                    var off: Long
                    var len: Long
                    var to: Long
                    for (i in ranges.indices) {
                        off = ranges!![i]!!.from
                        if (ranges[i]!!.to == -1L) {
                            len = -1
                            to = totalLength - 1
                        } else {
                            to = ranges[i]!!.to
                            if (to >= totalLength) to = totalLength - 1
                            len = to - ranges[i]!!.from + 1
                        }
                        rsp.addHeader("Content-Range", "bytes " + off + "-" + to + "/" + Caster.toString(totalLength))
                        rsp.setStatus(206)
                        // print.e("Content-Range: bytes "+off+"-"+to+"/"+Caster.toString(totalLength));
                        contentLength += to - off + 1L
                        // ReqRspUtil.setContentLength(rsp,len);
                        IOUtil.copy(`is`, os, off, len)
                    }
                }
                if (os !is GZIPOutputStream) ReqRspUtil.setContentLength(rsp, contentLength)
            } catch (ioe: IOException) {
            } finally {
                IOUtil.flushEL(os)
                IOUtil.closeEL(`is`, os)
                if (deletefile && file != null) ResourceUtil.removeEL(file, true)
                // disable debugging output
                (pageContext as PageContextImpl?).getDebugger().setOutput(false)
                (pageContext as PageContextImpl?).getRootOut().setClosed(true)
            }
            throw PostContentAbort()
        } else if (reset) pageContext.clear()
        return EVAL_BODY_INCLUDE // EVAL_PAGE;
    }

    @get:Throws(PageException::class, IOException::class)
    private val outputStream: OutputStream?
        private get() = try {
            pageContext.getResponseStream()
        } catch (ise: IllegalStateException) {
            throw TemplateException("Content was already sent to user, flush")
        }

    @Override
    fun doEndTag(): Int {
        return if (strFile == null) EVAL_PAGE else SKIP_PAGE
    }

    /**
     * sets if tag has a body or not
     *
     * @param hasBody
     */
    fun hasBody(hasBody: Boolean) {}
    private val ranges: Array<Range?>?
        private get() {
            val req: HttpServletRequest = pageContext.getHttpServletRequest()
            val names: Enumeration = req.getHeaderNames() ?: return null
            var name: String
            var range: Array<Range?>?
            while (names.hasMoreElements()) {
                name = names.nextElement()
                if ("range".equalsIgnoreCase(name)) {
                    range = getRanges(name, req.getHeader(name))
                    if (range != null) return range
                }
            }
            return null
        }

    private fun getRanges(name: String?, range: String?): Array<Range?>? {
        var range = range
        if (StringUtil.isEmpty(range, true)) return null
        range = StringUtil.removeWhiteSpace(range)
        if (range.indexOf("bytes=") === 0) range = range.substring(6)
        var arr: Array<String?>? = null
        arr = try {
            ListUtil.toStringArray(ListUtil.listToArrayRemoveEmpty(range, ','))
        } catch (e: PageException) {
            failRange(name, range)
            return null
        }
        var item: String?
        var index: Int
        var from: Long
        var to: Long
        val ranges = arrayOfNulls<Range?>(arr.size)
        for (i in ranges.indices) {
            item = arr.get(i).trim()
            index = item.indexOf('-')
            if (index != -1) {
                from = Caster.toLongValue(item.substring(0, index), 0)
                to = Caster.toLongValue(item.substring(index + 1), -1)
                if (to != -1L && from > to) {
                    failRange(name, range)
                    return null
                    // throw new ExpressionException("invalid range definition, from have to bigger than to
                    // ("+from+"-"+to+")");
                }
            } else {
                from = Caster.toLongValue(item, 0)
                to = -1
            }
            ranges[i] = Range(from, to)
            if (i > 0 && ranges[i - 1]!!.to >= from) {
                LogUtil.log(pageContext, Log.LEVEL_ERROR, Content::class.java.getName(), "there is an overlapping of 2 ranges (" + ranges[i - 1] + "," + ranges[i] + ")")
                return null
            }
        }
        return ranges
    }

    private fun failRange(name: String?, range: String?) {
        LogUtil.log(pageContext, Log.LEVEL_INFO, Content::class.java.getName(), "failed to parse the header field [$name:$range]")
    }

    companion object {
        private const val RANGE_NONE = 0
        private const val RANGE_YES = 1
        private const val RANGE_NO = 2
    }
}

internal class Range(var from: Long, var to: Long) {
    @Override
    override fun toString(): String {
        return "$from-$to"
    }
}