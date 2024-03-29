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
package tachyon.runtime.tag

import javax.servlet.jsp.tagext.Tag

/**
 * Required for cfhttp POST operations, cfhttpparam is used to specify the parameters necessary to
 * build a cfhttp POST.
 *
 *
 *
 */
class HttpParam : TagImpl() {
    var param: HttpParamBean? = HttpParamBean()

    /**
     * Applies to FormField and CGI types; ignored for all other types. Specifies whether to URLEncode
     * the form field or header.
     *
     * @param encoded
     */
    fun setEncoded(encoded: Boolean) {
        param.setEncoded(if (encoded) Http.ENCODED_YES else Http.ENCODED_NO)
    }

    /**
     * Applies to File type; invalid for all other types. Specifies the MIME media type of the file
     * contents. The content type can include an identifier for the character encoding of the file; for
     * example, text/html; charset=ISO-8859-1 indicates that the file is HTML text in the ISO Latin-1
     * character encoding.
     *
     * @param mimetype
     */
    fun setMimetype(mimetype: String?) {
        param.setMimeType(mimetype)
    }

    /**
     * set the value value Specifies the value of the URL, FormField, Cookie, File, or CGI variable
     * being passed.
     *
     * @param value value to set
     */
    fun setValue(value: Object?) {
        param!!.setValue(value)
    }

    /**
     * set the value type The transaction type.
     *
     * @param type value to set
     * @throws ApplicationException
     */
    @Throws(ApplicationException::class)
    fun setType(type: String?) {
        var type = type
        if (StringUtil.isEmpty(type, true)) return
        type = type.toLowerCase().trim()
        if (type.equals("url")) param.setType(HttpParamBean.TYPE_URL) else if (type.equals("formfield") || type.equals("form")) param.setType(HttpParamBean.TYPE_FORM) else if (type.equals("cgi")) param.setType(HttpParamBean.TYPE_CGI) else if (type.startsWith("head") || type.startsWith("header")) param.setType(HttpParamBean.TYPE_HEADER) else if (type.equals("cookie")) param.setType(HttpParamBean.TYPE_COOKIE) else if (type.equals("file")) param.setType(HttpParamBean.TYPE_FILE) else if (type.equals("xml")) param.setType(HttpParamBean.TYPE_XML) else if (type.equals("body")) param.setType(HttpParamBean.TYPE_BODY) else throw ApplicationException("invalid type [$type], valid types are [body,cgi,cookie,file,form,head,url,xml]")
    }

    /**
     * set the value file Required for type = "File".
     *
     * @param file value to set
     */
    fun setFile(file: String?) {
        param.setFile(ResourceUtil.toResourceNotExisting(pageContext, file))
    }

    /**
     * set the value name A variable name for the data being passed.
     *
     * @param name value to set
     */
    fun setName(name: String?) {
        param.setName(name)
    }

    @Override
    @Throws(ApplicationException::class)
    fun doStartTag(): Int {
        if (StringUtil.isEmpty(param.getName()) && HttpParamBean.TYPE_BODY !== param.getType() && HttpParamBean.TYPE_XML !== param.getType()) {
            throw ApplicationException("attribute [name] is required for tag [httpparam] if type is not [body or xml]")
        }
        if (HttpParamBean.TYPE_FILE === param.getType() && param.getFile() == null) throw ApplicationException("attribute [file] is required for tag [httpparam] if type is [file]")

        // get HTTP Tag
        var parent: Tag = getParent()
        while (parent != null && parent !is Http) {
            parent = parent.getParent()
        }
        if (parent is Http) {
            parent!!.setParam(param)
        } else {
            throw ApplicationException("Wrong Context, tag HttpParam must be inside a Http tag")
        }
        return SKIP_BODY
    }

    @Override
    fun doEndTag(): Int {
        return EVAL_PAGE
    }

    @Override
    fun release() {
        super.release()
        param = HttpParamBean()
    }
}