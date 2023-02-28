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

import javax.servlet.jsp.tagext.Tag

/**
 * Defines table column header, width, alignment, and text. Used only inside a cftable.
 *
 *
 *
 */
class Col : TagImpl() {
    /**
     * Double-quote delimited text that determines what displays in the column. The rules for the text
     * attribute are identical to the rules for cfoutput sections; it can consist of a combination of
     * literal text, HTML tags, and query record set field references. You can embed hyperlinks, image
     * references, and input controls in columns.
     */
    private var text: String? = ""

    /**
     * The width of the column in characters (the default is 20). If the length of the data displayed
     * exceeds the width value, the data is truncated to fit.
     */
    private var width = -1

    /** Column alignment, Left, Right, or Center.  */
    private var align: Short = Table.ALIGN_LEFT

    /** The text for the column's header.  */
    private var header: String? = ""
    @Override
    fun release() {
        super.release()
        text = ""
        width = -1
        align = Table.ALIGN_LEFT
        header = ""
    }

    /**
     * set the value text Double-quote delimited text that determines what displays in the column. The
     * rules for the text attribute are identical to the rules for cfoutput sections; it can consist of
     * a combination of literal text, HTML tags, and query record set field references. You can embed
     * hyperlinks, image references, and input controls in columns.
     *
     * @param text value to set
     */
    fun setText(text: String?) {
        this.text = text
    }

    /**
     * set the value width The width of the column in characters (the default is 20). If the length of
     * the data displayed exceeds the width value, the data is truncated to fit.
     *
     * @param width value to set
     */
    fun setWidth(width: Double) {
        this.width = width.toInt()
        if (this.width < 0) this.width = -1
    }

    /**
     * set the value align Column alignment, Left, Right, or Center.
     *
     * @param align value to set
     * @throws ApplicationException
     */
    @Throws(ApplicationException::class)
    fun setAlign(align: String?) {
        var align = align
        align = StringUtil.toLowerCase(align)
        if (align.equals("left")) this.align = Table.ALIGN_LEFT else if (align.equals("center")) this.align = Table.ALIGN_CENTER else if (align.equals("right")) this.align = Table.ALIGN_RIGHT else throw ApplicationException("value [$align] of attribute align from tag col is invalid", "valid values are [left, center, right]")
    }

    /**
     * set the value header The text for the column's header.
     *
     * @param header value to set
     */
    fun setHeader(header: String?) {
        this.header = header
    }

    @Override
    @Throws(ExpressionException::class, ApplicationException::class)
    fun doStartTag(): Int {
        var parent: Tag = getParent()
        while (parent != null && parent !is Table) {
            parent = parent.getParent()
        }
        if (parent is Table) {
            parent!!.setCol(header, text, align, width)
        } else throw ApplicationException("invalid context for tag col, tag must be inside a table tag")
        return SKIP_BODY
    }

    @Override
    fun doEndTag(): Int {
        return EVAL_PAGE
    }
}