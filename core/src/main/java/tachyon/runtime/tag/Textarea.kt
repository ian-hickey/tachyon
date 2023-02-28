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

// TODO tag textarea
// attribute html macht irgendwie keinen sinn, aber auch unter neo nicht
class Textarea : Input(), BodyTag {
    private var bodyContent: BodyContent? = null
    private var basepath = BASE_PATH
    private var fontFormats: String? = null
    private var fontNames: String? = null
    private var fontSizes: String? = null
    private var html = false
    private var richText = false
    private var skin = SKIN
    private var stylesXML = STYLE_XML
    private var templatesXML = TEMPLATE_XML
    private var toolbar = TOOLBAR
    private var toolbarOnFocus = false
    private var wrap = WRAP_OFF

    @Override
    override fun release() {
        super.release()
        bodyContent = null
        basepath = BASE_PATH
        fontFormats = null
        fontNames = null
        fontSizes = null
        html = false
        richText = false
        skin = SKIN
        stylesXML = STYLE_XML
        templatesXML = TEMPLATE_XML
        toolbar = TOOLBAR
        toolbarOnFocus = false
        wrap = WRAP_OFF
    }

    @Throws(PageException::class)
    fun setCols(cols: Double) {
        attributes.set("cols", Caster.toString(cols))
    }

    @Throws(PageException::class)
    fun setRows(rows: Double) {
        attributes.set("rows", Caster.toString(rows))
    }

    fun setBasepath(basepath: String?) {
        this.basepath = basepath
    }

    fun setFontFormats(fontFormats: String?) {
        this.fontFormats = fontFormats
    }

    fun setFontNames(fontNames: String?) {
        this.fontNames = fontNames
    }

    fun setFontSizes(fontSizes: String?) {
        this.fontSizes = fontSizes
    }

    fun setHtml(html: Boolean) {
        this.html = html
    }

    fun setRichtext(richText: Boolean) {
        this.richText = richText
    }

    fun setSkin(skin: String?) {
        this.skin = skin
    }

    fun setStylesxml(stylesXML: String?) {
        this.stylesXML = stylesXML
    }

    fun setTemplatesxml(templatesXML: String?) {
        this.templatesXML = templatesXML
    }

    fun setToolbar(toolbar: String?) {
        this.toolbar = toolbar
    }

    fun setToolbaronfocus(toolbarOnFocus: Boolean) {
        this.toolbarOnFocus = toolbarOnFocus
    }

    @Throws(ExpressionException::class)
    fun setWrap(strWrap: String?) {
        var strWrap = strWrap
        strWrap = strWrap.trim().toLowerCase()
        wrap = if ("hard".equals(strWrap)) WRAP_HARD else if ("soft".equals(strWrap)) WRAP_SOFT else if ("off".equals(strWrap)) WRAP_OFF else if ("physical".equals(strWrap)) WRAP_PHYSICAL else if ("virtual".equals(strWrap)) WRAP_VIRTUAL else throw ExpressionException("invalid value [$strWrap] for attribute wrap, valid values are [hard,soft,off,physical,virtual]")
    }

    @Override
    @Throws(IOException::class, PageException::class)
    override fun draw() {

        // value
        var attrValue: String? = null
        var bodyValue: String? = null
        var value: String? = ""
        if (bodyContent != null) bodyValue = bodyContent.getString()
        if (attributes.containsKey(KeyConstants._value)) attrValue = Caster.toString(attributes.get(KeyConstants._value, null))

        // check values
        if (!StringUtil.isEmpty(bodyValue) && !StringUtil.isEmpty(attrValue)) {
            throw ApplicationException("the value of tag can't be set twice (tag body and attribute value)")
        } else if (!StringUtil.isEmpty(bodyValue)) {
            value = enc(bodyValue)
        } else if (!StringUtil.isEmpty(attrValue)) {
            value = enc(attrValue)
        }
        // id
        if (StringUtil.isEmpty(attributes.get(KeyConstants._id, null))) attributes.set(KeyConstants._id, StringUtil.toVariableName(attributes.get(KeyConstants._name) as String))

        // start output
        pageContext.forceWrite("<textarea")
        val it: Iterator<Entry<Key?, Object?>?> = attributes.entryIterator()
        var e: Entry<Key?, Object?>?
        while (it.hasNext()) {
            e = it.next()
            pageContext.forceWrite(" ")
            pageContext.forceWrite(e.getKey().getString())
            pageContext.forceWrite("=\"")
            pageContext.forceWrite(enc(Caster.toString(e.getValue())))
            pageContext.forceWrite("\"")
        }
        if (passthrough != null) {
            pageContext.forceWrite(" ")
            pageContext.forceWrite(passthrough)
        }
        pageContext.forceWrite(">")
        pageContext.forceWrite(value)
        pageContext.forceWrite("</textarea>")
    }

    @Override
    fun doStartTag(): Int {
        return EVAL_BODY_BUFFERED
    }

    @Override
    fun setBodyContent(bodyContent: BodyContent?) {
        this.bodyContent = bodyContent
    }

    @Override
    @Throws(JspException::class)
    fun doInitBody() {
    }

    @Override
    @Throws(JspException::class)
    fun doAfterBody(): Int {
        return SKIP_BODY
    }

    fun hasBody(hasBody: Boolean) {}

    companion object {
        private val BASE_PATH: String? = null // TODO
        private val STYLE_XML: String? = null
        private val TEMPLATE_XML: String? = null
        private val SKIN: String? = "default"
        private val TOOLBAR: String? = "default"
        private const val WRAP_OFF = 0
        private const val WRAP_HARD = 1
        private const val WRAP_SOFT = 2
        private const val WRAP_PHYSICAL = 3
        private const val WRAP_VIRTUAL = 4
    }
}