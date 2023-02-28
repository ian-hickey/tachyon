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
package tachyon.runtime.net.mail

import java.io.IOException

/**
 * An HTML multipart email.
 *
 *
 *
 * This class is used to send HTML formatted email. A text message can also be set for HTML unaware
 * email clients, such as text-based email clients.
 *
 *
 *
 * This class also inherits from MultiPartEmail, so it is easy to add attachments to the email.
 *
 *
 *
 * To send an email in HTML, one should create a HtmlEmail, then use the setFrom, addTo, etc.
 * methods. The HTML content can be set with the setHtmlMsg method. The alternative text content can
 * be set with setTextMsg.
 *
 *
 *
 * Either the text or HTML can be omitted, in which case the "main" part of the multipart becomes
 * whichever is supplied rather than a multipart/alternative.
 *
 *
 */
class HtmlEmailImpl : MultiPartEmail() {
    /**
     * Text part of the message. This will be used as alternative text if the email client does not
     * support HTML messages.
     */
    protected var text: String? = null

    /** Html part of the message  */
    protected var html: String? = null

    /** Embedded images  */
    protected var inlineImages: List? = ArrayList()

    /**
     * Set the text content.
     *
     * @param aText A String.
     * @return An HtmlEmail.
     * @throws EmailException see javax.mail.internet.MimeBodyPart for definitions
     */
    @Throws(EmailException::class)
    fun setTextMsg(aText: String?): HtmlEmailImpl? {
        if (StringUtil.isEmpty(aText)) {
            throw EmailException("Invalid message supplied")
        }
        text = aText
        return this
    }

    /**
     * Set the HTML content.
     *
     * @param aHtml A String.
     * @return An HtmlEmail.
     * @throws EmailException see javax.mail.internet.MimeBodyPart for definitions
     */
    @Throws(EmailException::class)
    fun setHtmlMsg(aHtml: String?): HtmlEmailImpl? {
        if (StringUtil.isEmpty(aHtml)) {
            throw EmailException("Invalid message supplied")
        }
        html = aHtml
        return this
    }

    /**
     * Set the message.
     *
     *
     *
     * This method overrides the MultiPartEmail setMsg() method in order to send an HTML message instead
     * of a full text message in the mail body. The message is formatted in HTML for the HTML part of
     * the message, it is let as is in the alternate text part.
     *
     * @param msg A String.
     * @return An Email.
     * @throws EmailException see javax.mail.internet.MimeBodyPart for definitions
     */
    @Override
    @Throws(EmailException::class)
    fun setMsg(msg: String?): Email? {
        if (StringUtil.isEmpty(msg)) {
            throw EmailException("Invalid message supplied")
        }
        setTextMsg(msg)
        setHtmlMsg(StringBuffer().append("<html><body><pre>").append(msg).append("</pre></body></html>").toString())
        return this
    }

    /**
     * Embeds an URL in the HTML.
     *
     *
     *
     * This method allows to embed a file located by an URL into the mail body. It allows, for instance,
     * to add inline images to the email. Inline files may be referenced with a `cid:xxxxxx`
     * URL, where xxxxxx is the Content-ID returned by the embed function.
     *
     *
     *
     * Example of use:<br></br>
     * ``<pre>
     * HtmlEmail he = new HtmlEmail();
     * he.setHtmlMsg("&lt;html&gt;&lt;img src=cid:" +
     * embed("file:/my/image.gif","image.gif") +
     * "&gt;&lt;/html&gt;");
     * // code to set the others email fields (not shown)
    </pre> *
     *
     * @param url The URL of the file.
     * @param cid A String with the Content-ID of the file.
     * @param name The name that will be set in the filename header field.
     * @throws EmailException when URL supplied is invalid also see javax.mail.internet.MimeBodyPart for
     * definitions
     */
    @Throws(EmailException::class)
    fun embed(url: URL?, cid: String?, name: String?) {
        // verify that the URL is valid
        try {
            val `is`: InputStream = url.openStream()
            `is`.close()
        } catch (e: IOException) {
            throw EmailException("Invalid URL")
        }
        val mbp = MimeBodyPart()
        try {
            mbp.setDataHandler(DataHandler(URLDataSource(url)))
            mbp.setFileName(name)
            mbp.setDisposition("inline")
            mbp.addHeader("Content-ID", "<$cid>")
            inlineImages.add(mbp)
        } catch (me: MessagingException) {
            throw EmailException(me)
        }
    }

    /**
     * Does the work of actually building the email.
     *
     * @exception EmailException if there was an error.
     */
    @Override
    @Throws(EmailException::class)
    fun buildMimeMessage() {
        try {
            // if the email has attachments then the base type is mixed,
            // otherwise it should be related
            if (this.isBoolHasAttachments()) {
                buildAttachments()
            } else {
                buildNoAttachments()
            }
        } catch (me: MessagingException) {
            throw EmailException(me)
        }
        super.buildMimeMessage()
    }

    /**
     * @throws EmailException EmailException
     * @throws MessagingException MessagingException
     */
    @Throws(MessagingException::class, EmailException::class)
    private fun buildAttachments() {
        val container: MimeMultipart = this.getContainer()
        var subContainer: MimeMultipart? = null
        val subContainerHTML = MimeMultipart("related")
        var msgHtml: BodyPart? = null
        var msgText: BodyPart? = null
        container.setSubType("mixed")
        subContainer = MimeMultipart("alternative")
        if (!StringUtil.isEmpty(text)) {
            msgText = MimeBodyPart()
            subContainer.addBodyPart(msgText)
            if (!StringUtil.isEmpty(this.charset)) {
                msgText.setContent(text, Email.TEXT_PLAIN.toString() + "; charset=" + this.charset)
            } else {
                msgText.setContent(text, Email.TEXT_PLAIN)
            }
        }
        if (!StringUtil.isEmpty(html)) {
            if (inlineImages.size() > 0) {
                msgHtml = MimeBodyPart()
                subContainerHTML.addBodyPart(msgHtml)
            } else {
                msgHtml = MimeBodyPart()
                subContainer.addBodyPart(msgHtml)
            }
            if (!StringUtil.isEmpty(this.charset)) {
                msgHtml.setContent(html, Email.TEXT_HTML.toString() + "; charset=" + this.charset)
            } else {
                msgHtml.setContent(html, Email.TEXT_HTML)
            }
            val iter: Iterator = inlineImages.iterator()
            while (iter.hasNext()) {
                subContainerHTML.addBodyPart(iter.next() as BodyPart)
            }
        }

        // add sub containers to message
        this.addPart(subContainer, 0)
        if (inlineImages.size() > 0) {
            // add sub container to message
            this.addPart(subContainerHTML, 1)
        }
    }

    /**
     * @throws EmailException EmailException
     * @throws MessagingException MessagingException
     */
    @Throws(MessagingException::class, EmailException::class)
    private fun buildNoAttachments() {
        val container: MimeMultipart = this.getContainer()
        val subContainerHTML = MimeMultipart("related")
        container.setSubType("alternative")
        var msgText: BodyPart? = null
        var msgHtml: BodyPart? = null
        if (!StringUtil.isEmpty(text)) {
            msgText = this.getPrimaryBodyPart()
            if (!StringUtil.isEmpty(this.charset)) {
                msgText.setContent(text, Email.TEXT_PLAIN.toString() + "; charset=" + this.charset)
            } else {
                msgText.setContent(text, Email.TEXT_PLAIN)
            }
        }
        if (!StringUtil.isEmpty(html)) {
            // if the txt part of the message was null, then the html part
            // will become the primary body part
            if (msgText == null) {
                msgHtml = getPrimaryBodyPart()
            } else {
                if (inlineImages.size() > 0) {
                    msgHtml = MimeBodyPart()
                    subContainerHTML.addBodyPart(msgHtml)
                } else {
                    msgHtml = MimeBodyPart()
                    container.addBodyPart(msgHtml, 1)
                }
            }
            if (!StringUtil.isEmpty(this.charset)) {
                msgHtml.setContent(html, Email.TEXT_HTML.toString() + "; charset=" + this.charset)
            } else {
                msgHtml.setContent(html, Email.TEXT_HTML)
            }
            val iter: Iterator = inlineImages.iterator()
            while (iter.hasNext()) {
                subContainerHTML.addBodyPart(iter.next() as BodyPart)
            }
            if (inlineImages.size() > 0) {
                // add sub container to message
                this.addPart(subContainerHTML)
            }
        }
    }

    companion object {
        /** Definition of the length of generated CID's  */
        const val CID_LENGTH = 10
    }
}