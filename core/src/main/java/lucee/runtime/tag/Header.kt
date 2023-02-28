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

import javax.servlet.http.HttpServletResponse

/**
 * Generates custom HTTP response headers to return to the client.
 *
 *
 *
 */
class Header : TagImpl() {
    /** A value for the HTTP header. This attribute is used in conjunction with the name attribute.  */
    private var value: String? = ""

    /**
     * Text that explains the status code. This attribute is used in conjunction with the statusCode
     * attribute.
     */
    private var statustext: String? = null

    /** A name for the header.  */
    private var name: String? = null

    /** A number that sets the HTTP status code.  */
    private var statuscode = 0
    private var hasStatucCode = false
    private var charset: CharSet? = null
    @Override
    fun release() {
        super.release()
        value = ""
        statustext = null
        name = null
        statuscode = 0
        hasStatucCode = false
        charset = null
    }

    /**
     * set the value value A value for the HTTP header. This attribute is used in conjunction with the
     * name attribute.
     *
     * @param value value to set
     */
    fun setValue(value: String?) {
        this.value = value
    }

    /**
     * set the value statustext Text that explains the status code. This attribute is used in
     * conjunction with the statusCode attribute.
     *
     * @param statustext value to set
     */
    fun setStatustext(statustext: String?) {
        this.statustext = statustext
    }

    /**
     * set the value name A name for the header.
     *
     * @param name value to set
     */
    fun setName(name: String?) {
        this.name = name
    }

    /**
     * set the value statuscode A number that sets the HTTP status code.
     *
     * @param statuscode value to set
     */
    fun setStatuscode(statuscode: Double) {
        this.statuscode = statuscode.toInt()
        hasStatucCode = true
    }

    /**
     * @param charset The charset to set.
     */
    fun setCharset(charset: String?) {
        this.charset = CharsetUtil.toCharSet(charset)
    }

    @Override
    @Throws(PageException::class)
    fun doStartTag(): Int {
        val rsp: HttpServletResponse = pageContext.getHttpServletResponse()
        if (rsp.isCommitted()) throw TemplateException("can't assign value to header, header is already committed")

        // set name value
        if (name != null) {
            if (charset == null && name.equalsIgnoreCase("content-disposition")) {
                charset = CharsetUtil.toCharSet((pageContext as PageContextImpl?).getWebCharset())
            }
            if (charset != null) {
                name = String(name.getBytes(CharsetUtil.toCharset(charset)), CharsetUtil.ISO88591)
                value = String(value.getBytes(CharsetUtil.toCharset(charset)), CharsetUtil.ISO88591)
            } else {
                name = String(name.getBytes(), CharsetUtil.ISO88591)
                value = String(value.getBytes(), CharsetUtil.ISO88591)
            }
            if (name.toLowerCase().equals("content-type") && value!!.length() > 0) {
                ReqRspUtil.setContentType(rsp, value)
            } else {
                rsp.addHeader(name, value)
            }
        }
        // set status
        if (hasStatucCode) {
            if (statustext != null) {
                // try {
                /// rsp.sendError(statuscode, statustext);
                rsp.setStatus(statuscode, statustext)
                /*
				 * } catch (IOException e) { throw new
				 * TemplateException("can't assign value to header, header is already committed",e.getMessage()); }
				 */
            } else {
                rsp.setStatus(statuscode)
            }
        }
        return SKIP_BODY
    }

    @Override
    fun doEndTag(): Int {
        return EVAL_PAGE
    }
}