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

import java.io.IOException

/**
 * Can either attach a file or add a header to a message. It is nested within a cfmail tag. You can
 * use more than one cfmailparam tag within a cfmail tag.
 *
 *
 *
 */
class MailParam : TagImpl() {
    /** Indicates the value of the header.  */
    private var value: String? = ""

    /**
     * Attaches the specified file to the message. This attribute is mutually exclusive with the name
     * attribute.
     */
    private var file: String? = null

    /**
     * Specifies the name of the header. Header names are case insensitive. This attribute is mutually
     * exclusive with the file attribute.
     */
    private var name: String? = null
    private var fileName: String? = null
    private var type: String? = ""
    private var disposition: String? = null
    private var contentID: String? = null
    private var remove: Boolean? = false
    private var content: ByteArray? = null
    @Override
    fun release() {
        super.release()
        value = ""
        file = null
        name = null
        type = ""
        disposition = null
        contentID = null
        remove = null
        content = null
        fileName = null
    }

    /**
     * @param remove the remove to set
     */
    fun setRemove(remove: Boolean) {
        this.remove = Caster.toBoolean(remove)
    }

    /**
     * @param content the content to set
     * @throws ExpressionException
     */
    @Throws(PageException::class)
    fun setContent(content: Object?) {
        if (content is String) this.content = (content as String?).getBytes() else this.content = Caster.toBinary(content)
    }

    /**
     * @param type
     */
    fun setType(type: String?) {
        var type = type
        type = type.toLowerCase().trim()
        if (type.equals("text")) type = "text/plain" else if (type.equals("plain")) type = "text/plain" else if (type.equals("html")) type = "text/html" else if (type.startsWith("multipart/")) return  // TODO see LDEV-570 maybe add support for content-type in the future
        this.type = type
    }

    /**
     * set the value value Indicates the value of the header.
     *
     * @param value value to set
     */
    fun setValue(value: String?) {
        this.value = value
    }

    /**
     * set the value file Attaches the specified file to the message. This attribute is mutually
     * exclusive with the name attribute.
     *
     * @param strFile value to set
     * @throws PageException
     */
    @Throws(PageException::class)
    fun setFile(strFile: String?) {
        file = strFile
    }

    /**
     * set the value name Specifies the name of the header. Header names are case insensitive. This
     * attribute is mutually exclusive with the file attribute.
     *
     * @param name value to set
     */
    fun setName(name: String?) {
        this.name = name
    }

    fun setFilename(fileName: String?) {
        this.fileName = fileName
    }

    /**
     * @param disposition The disposition to set.
     * @throws ApplicationException
     */
    @Throws(ApplicationException::class)
    fun setDisposition(disposition: String?) {
        var disposition = disposition
        disposition = disposition.trim().toLowerCase()
        if (disposition.equals("attachment")) this.disposition = EmailAttachment.ATTACHMENT else if (disposition.equals("inline")) this.disposition = EmailAttachment.INLINE else throw ApplicationException("For the tag [MailParam], the attribute [disposition] must be one of the following values [attachment, inline]")
    }

    /**
     * @param contentID The contentID to set.
     */
    fun setContentid(contentID: String?) {
        this.contentID = contentID
    }

    @Override
    @Throws(PageException::class)
    fun doStartTag(): Int {
        if (content != null) {
            required("mailparam", "file", file)
            val id = "id-" + CreateUniqueId.invoke()
            val ext: String = ResourceUtil.getExtension(file, "tmp")
            if (StringUtil.isEmpty(fileName) && !StringUtil.isEmpty(file)) fileName = ListUtil.last(file, "/\\", true)
            val res: Resource = SystemUtil.getTempDirectory().getRealResource("$id.$ext")
            if (res.exists()) ResourceUtil.removeEL(res, true)
            try {
                IOUtil.write(res, content)
            } catch (e: IOException) {
                throw Caster.toPageException(e)
            }
            file = ResourceUtil.getCanonicalPathEL(res)
            remove = true
        } else if (!StringUtil.isEmpty(file)) {
            val res: Resource = ResourceUtil.toResourceNotExisting(pageContext, file)
            if (res != null) {
                if (res.exists()) pageContext.getConfig().getSecurityManager().checkFileLocation(res)
                file = ResourceUtil.getCanonicalPathEL(res)
            }
        }

        // check attributes
        val hasFile: Boolean = !StringUtil.isEmpty(file)
        val hasName: Boolean = !StringUtil.isEmpty(name)
        // both attributes
        if (hasName && hasFile) {
            throw ApplicationException("Wrong Context for tag [MailParam], you cannot use the attributes [file] and [name] together")
        }
        // no attributes
        if (!hasName && !hasFile) {
            throw ApplicationException("Wrong Context for tag [MailParam], one of the attributes [file] or [name] is required")
        }

        // get Mail Tag
        var parent: Tag = getParent()
        while (parent != null && parent !is Mail) {
            parent = parent.getParent()
        }
        if (parent is Mail) {
            parent!!.setParam(type, file, fileName, name, value, disposition, contentID, remove)
        } else {
            throw ApplicationException("Wrong Context, tag [MailParam] must be inside a [Mail] tag")
        }
        return SKIP_BODY
    }

    @Override
    fun doEndTag(): Int {
        return EVAL_PAGE
    }
}