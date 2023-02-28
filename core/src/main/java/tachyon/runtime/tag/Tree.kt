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

import java.util.ArrayList

class Tree : BodyTagImpl() {
    private var name: String? = null
    private var required = false
    private var delimiter: String? = null
    private var completepath: String? = null
    private var appendkey = false
    private var highlightref = false
    private var onvalidate: String? = null
    private var message: String? = null
    private var onerror: String? = null
    private var lookandfeel: String? = null
    private var font: String? = null
    private var fontsize = 0.0
    private var italic = false
    private var bold = false
    private var height = 0.0
    private var width = 0.0
    private var vspace = 0.0
    private var align: String? = null
    private var border = false
    private var hscroll = false
    private var vscroll = false
    private var notsupported: String? = null
    private var onblur: String? = null
    private var onfocus: String? = null
    private var format: String? = null
    private var onchange: String? = null
    private var style: String? = null
    private var tooltip: String? = null
    private var visible = false
    private var enabled: String? = null
    private val items: List? = ArrayList()

    /**
     * @param align the align to set
     */
    fun setAlign(align: String?) {
        this.align = align
    }

    /**
     * @param appendkey the appendkey to set
     */
    fun setAppendkey(appendkey: Boolean) {
        this.appendkey = appendkey
    }

    /**
     * @param bold the bold to set
     */
    fun setBold(bold: Boolean) {
        this.bold = bold
    }

    /**
     * @param border the border to set
     */
    fun setBorder(border: Boolean) {
        this.border = border
    }

    /**
     * @param completepath the completepath to set
     */
    fun setCompletepath(completepath: String?) {
        this.completepath = completepath
    }

    /**
     * @param delimiter the delimiter to set
     */
    fun setDelimiter(delimiter: String?) {
        this.delimiter = delimiter
    }

    /**
     * @param enabled the enabled to set
     */
    fun setEnabled(enabled: String?) {
        this.enabled = enabled
    }

    /**
     * @param font the font to set
     */
    fun setFont(font: String?) {
        this.font = font
    }

    /**
     * @param fontsize the fontsize to set
     */
    fun setFontsize(fontsize: Double) {
        this.fontsize = fontsize
    }

    /**
     * @param format the format to set
     */
    fun setFormat(format: String?) {
        this.format = format
    }

    /**
     * @param height the height to set
     */
    fun setHeight(height: Double) {
        this.height = height
    }

    /**
     * @param highlightref the highlightref to set
     */
    fun setHighlightref(highlightref: Boolean) {
        this.highlightref = highlightref
    }

    /**
     * @param hscroll the hscroll to set
     */
    fun setHscroll(hscroll: Boolean) {
        this.hscroll = hscroll
    }

    /**
     * @param italic the italic to set
     */
    fun setItalic(italic: Boolean) {
        this.italic = italic
    }

    /**
     * @param lookandfeel the lookandfeel to set
     */
    fun setLookandfeel(lookandfeel: String?) {
        this.lookandfeel = lookandfeel
    }

    /**
     * @param message the message to set
     */
    fun setMessage(message: String?) {
        this.message = message
    }

    /**
     * @param name the name to set
     */
    fun setName(name: String?) {
        this.name = name
    }

    /**
     * @param notsupported the notsupported to set
     */
    fun setNotsupported(notsupported: String?) {
        this.notsupported = notsupported
    }

    /**
     * @param onblur the onblur to set
     */
    fun setOnblur(onblur: String?) {
        this.onblur = onblur
    }

    /**
     * @param onchange the onchange to set
     */
    fun setOnchange(onchange: String?) {
        this.onchange = onchange
    }

    /**
     * @param onerror the onerror to set
     */
    fun setOnerror(onerror: String?) {
        this.onerror = onerror
    }

    /**
     * @param onfocus the onfocus to set
     */
    fun setOnfocus(onfocus: String?) {
        this.onfocus = onfocus
    }

    /**
     * @param onvalidate the onvalidate to set
     */
    fun setOnvalidate(onvalidate: String?) {
        this.onvalidate = onvalidate
    }

    /**
     * @param required the required to set
     */
    fun setRequired(required: Boolean) {
        this.required = required
    }

    /**
     * @param style the style to set
     */
    fun setStyle(style: String?) {
        this.style = style
    }

    /**
     * @param tooltip the tooltip to set
     */
    fun setTooltip(tooltip: String?) {
        this.tooltip = tooltip
    }

    /**
     * @param visible the visible to set
     */
    fun setVisible(visible: Boolean) {
        this.visible = visible
    }

    /**
     * @param vscroll the vscroll to set
     */
    fun setVscroll(vscroll: Boolean) {
        this.vscroll = vscroll
    }

    /**
     * @param vspace the vspace to set
     */
    fun setVspace(vspace: Double) {
        this.vspace = vspace
    }

    /**
     * @param width the width to set
     */
    fun setWidth(width: Double) {
        this.width = width
    }

    fun addTreeItem(item: TreeItemBean?) {
        items.add(item)
    }

    init {
        throw TagNotSupported("tree")
    }
}