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

import java.io.IOException

class Location : TagImpl() {
    /**
     * Yes or No. clientManagement must be enabled. Yes appends client variable information to the URL
     * you specify in the url.
     */
    private var addtoken = true

    /** The URL of the HTML file or CFML page to open.  */
    private var url: String? = ""
    private var encode = false
    private var statuscode = 302
    private var abort = false
    @Override
    fun release() {
        super.release()
        addtoken = false
        url = ""
        statuscode = 302
        abort = false
        encode = false
    }

    /**
     * @param statuscode the statuscode to set
     * @throws ApplicationException
     */
    @Throws(ApplicationException::class)
    fun setStatuscode(statuscode: Double) {
        val sc = statuscode.toInt()
        if (sc < 300 || sc > 307) throw ApplicationException("invalid value for attribute statuscode [" + Caster.toString(statuscode).toString() + "]",
                "attribute must have one of the folloing values [300|301|302|303|304|305|307]")
        this.statuscode = sc
    }

    /**
     * set the value addtoken Yes or No. clientManagement must be enabled. Yes appends client variable
     * information to the URL you specify in the url.
     *
     * @param addtoken value to set
     */
    fun setAddtoken(addtoken: Boolean) {
        this.addtoken = addtoken
    }

    /**
     * if set to true then the request will be aborted instead of redirected to allow developers to
     * troubleshoot code that contains redirects
     *
     * @param abort
     */
    fun setAbort(abort: Boolean) {
        this.abort = abort
    }

    /**
     * set the value Encode true or false.
     *
     * @param encode value to set
     */
    fun setEncode(encode: Boolean) {
        this.encode = encode
    }

    /**
     * set the value url The URL of the HTML file or CFML page to open.
     *
     * @param url value to set
     * @throws ApplicationException
     */
    @Throws(ApplicationException::class)
    fun setUrl(url: String?) {
        this.url = url.trim()
        if (this.url!!.length() === 0) throw ApplicationException("invalid url [empty string] for attribute url")
        if (StringUtil.hasLineFeed(url)) throw ApplicationException("invalid url [$url] for attribute url, carriage-return or line-feed inside the url are not allowed")
    }

    @Override
    @Throws(PageException::class)
    fun doStartTag(): Int {
        try {
            pageContext.getOut().clear()
        } catch (e: IOException) {
            throw Caster.toPageException(e)
        }
        val rsp: HttpServletResponse = pageContext.getHttpServletResponse()
        url = if (encode) {
            HTTPUtil.encode(url)
        } else {
            url
        }

        // add token
        if (addtoken && needId()) {
            val arr: Array<String?> = url.split("\\?")

            // only string_name
            if (arr.size == 1) {
                url += "?" + pageContext.getURLToken()
            } else if (arr.size > 1) {
                url = arr[0].toString() + "?" + pageContext.getURLToken()
                for (i in 1 until arr.size) url += "&" + arr[i]
            }
            url = ReqRspUtil.encodeRedirectURLEL(rsp, url)
        }
        val log: Log = ThreadLocalPageContext.getLog(pageContext, "application")
        if (abort) {
            if (log != null) log.log(Log.LEVEL_ERROR, "cftrace", "abort redirect to " + url + " at " + CallStackGet.call(pageContext, "text"))
            throw ExpressionException("abort redirect to $url")
        } else {
            if (log != null) log.log(Log.LEVEL_TRACE, "cftrace", "redirect to " + url + " at " + CallStackGet.call(pageContext, "text"))
        }
        rsp.setHeader("Connection", "close") // IE unter IIS6, Win2K3 und Resin
        rsp.setStatus(statuscode)
        rsp.setHeader("location", url)
        try {
            pageContext.forceWrite("<html>\n<head>\n\t<title>Document Moved</title>\n")
            // pageContext.forceWrite("\t<script>window.location='"+JSStringFormat.invoke(url)+"';</script>\n");
            pageContext.forceWrite("</head>\n<body>\n\t<h1>Object Moved</h1>\n")
            pageContext.forceWrite("</body>\n</html>")
        } catch (e: IOException) {
            throw Caster.toPageException(e)
        }
        if (pageContext.getConfig().debug()) pageContext.getDebugger().setOutput(false)
        throw Abort(Abort.SCOPE_REQUEST)
    }

    private fun needId(): Boolean {
        val ac: ApplicationContext = pageContext.getApplicationContext()
        return ac.isSetClientManagement() || ac.isSetSessionManagement()
    }

    @Override
    fun doEndTag(): Int {
        return EVAL_PAGE
    }
}