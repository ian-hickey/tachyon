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

import java.nio.charset.Charset

/**
 * Specifies one part of a multipart e-mail message. Can only be used in the cfmail tag. You can use
 * more than one cfmailpart tag within a cfmail tag
 *
 *
 *
 */
class MailPart : BodyTagImpl() {
    var part: tachyon.runtime.net.mail.MailPart? = MailPart()
    @Override
    fun release() {
        super.release()
        part = MailPart()
    }

    /**
     * @param type The type to set.
     * @throws ApplicationException
     */
    @Throws(ApplicationException::class)
    fun setType(type: String?) {
        part.setType(type)
    }

    /**
     * @param charset The charset to set.
     */
    fun setCharset(charset: Charset?) {
        part!!.setCharset(charset)
    }

    fun setCharset(charset: String?) {
        setCharset(CharsetUtil.toCharset(charset))
    }

    /**
     * @param wraptext The wraptext to set.
     */
    fun setWraptext(wraptext: Double) {
        part.setWraptext(wraptext.toInt())
    }

    @Override
    fun doStartTag(): Int {
        return EVAL_BODY_BUFFERED
    }

    @Override
    fun doInitBody() {
    }

    @Override
    fun doAfterBody(): Int {
        part!!.setBody(bodyContent.getString())
        return SKIP_BODY
    }

    @Override
    @Throws(PageException::class)
    fun doEndTag(): Int {
        mail!!.addPart(part)
        /*
		 * String type = part.getType(); if(StringUtil.isEmpty(part.getCharset()))
		 * part.setCharset(mail.getCharset()); if(type!=null && (type.equals("text/plain") ||
		 * type.equals("plain") || type.equals("text"))){ part.isPlain(true); mail.setBodyPart(part); } else
		 * if(type!=null && (type.equals("text/html") || type.equals("html") || type.equals("htm"))){
		 * part.isHTML(true); mail.setBodyPart(part); } else {
		 * 
		 * getMail().setParam(type, null, "susi", part.getBody(), "inline", null); }
		 */
        // throw new ApplicationException("attribute type of tag mailpart has an invalid values","valid
        // values are [plain,text,html] but value is now ["+type+"]");
        return EVAL_PAGE
    }

    @get:Throws(ApplicationException::class)
    private val mail: tachyon.runtime.tag.Mail?
        private get() {
            var parent: Tag = getParent()
            while (parent != null && parent !is Mail) {
                parent = parent.getParent()
            }
            if (parent is Mail) return parent
            throw ApplicationException("Wrong Context, tag MailPart must be inside a Mail tag")
        }
}