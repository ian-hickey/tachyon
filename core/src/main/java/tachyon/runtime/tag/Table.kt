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
 * Builds a table in a CFML page. Use the cfcol tag to define table column and row characteristics.
 * The cftable tag renders data either as preformatted text, or, with the HTMLTable attribute, as an
 * HTML table. Use cftable to create tables if you don't want to write HTML table tag code, or if
 * your data can be well presented as preformatted text.
 *
 *
 *
 */
class Table : BodyTagTryCatchFinallyImpl() {
    /** Name of the cfquery from which to draw data.  */
    private var query: tachyon.runtime.type.Query? = null

    /** Maximum number of rows to display in the table.  */
    private var maxrows: Int = Integer.MAX_VALUE

    /** Specifies the query row from which to start processing.  */
    private var startrow = 1

    /** Adds a border to the table. Use only when you specify the HTMLTable attribute for the table.  */
    private var border = false

    /** Displays headers for each column, as specified in the cfcol tag.  */
    private var colheaders = false

    /** Number of spaces to insert between columns 'default is 2'.  */
    private var colspacing = 2

    /** Renders the table as an HTML 3.0 table.  */
    private var htmltable = false

    /**
     * Number of lines to use for the table header. The default is 2, which leaves one line between the
     * headers and the first row of the table.
     */
    private var headerlines = 2
    var header: StringBuffer? = StringBuffer()
    var body: StringBuffer? = StringBuffer()
    private var initRow = 0
    private var count = 0
    private var startNewRow = false
    @Override
    fun release() {
        super.release()
        query = null
        maxrows = Integer.MAX_VALUE
        startrow = 1
        border = false
        colheaders = false
        colspacing = 2
        htmltable = false
        headerlines = 2
        if (header.length() > 0) header = StringBuffer()
        body = StringBuffer()
        count = 0
    }

    /**
     * set the value query Name of the cfquery from which to draw data.
     *
     * @param query value to set
     * @throws PageException
     */
    @Throws(PageException::class)
    fun setQuery(query: String?) {
        this.query = Caster.toQuery(pageContext.getVariable(query))
    }

    /**
     * set the value maxrows Maximum number of rows to display in the table.
     *
     * @param maxrows value to set
     */
    fun setMaxrows(maxrows: Double) {
        this.maxrows = maxrows.toInt()
    }

    /**
     * set the value startrow Specifies the query row from which to start processing.
     *
     * @param startrow value to set
     */
    fun setStartrow(startrow: Double) {
        this.startrow = startrow.toInt()
        if (this.startrow <= 0) this.startrow = 1
    }

    /**
     * set the value border Adds a border to the table. Use only when you specify the HTMLTable
     * attribute for the table.
     *
     * @param border value to set
     */
    fun setBorder(border: Boolean) {
        this.border = border
    }

    /**
     * set the value colheaders Displays headers for each column, as specified in the cfcol tag.
     *
     * @param colheaders value to set
     */
    fun setColheaders(colheaders: Boolean) {
        this.colheaders = colheaders
    }

    /**
     * set the value colspacing Number of spaces to insert between columns 'default is 2'.
     *
     * @param colspacing value to set
     */
    fun setColspacing(colspacing: Double) {
        this.colspacing = colspacing.toInt()
    }

    /**
     * set the value htmltable Renders the table as an HTML 3.0 table.
     *
     * @param htmltable value to set
     */
    fun setHtmltable(htmltable: Boolean) {
        this.htmltable = htmltable
    }

    /**
     * set the value headerlines Number of lines to use for the table header. The default is 2, which
     * leaves one line between the headers and the first row of the table.
     *
     * @param headerlines value to set
     */
    fun setHeaderlines(headerlines: Double) {
        this.headerlines = headerlines.toInt()
        if (this.headerlines < 2) this.headerlines = 2
    }

    @Override
    @Throws(PageException::class)
    fun doStartTag(): Int {
        startNewRow = true
        initRow = query.getRecordcount()
        query.go(startrow, pageContext.getId())
        pageContext.undefinedScope().addQuery(query)
        return if (query.getRecordcount() >= startrow) EVAL_BODY_INCLUDE else SKIP_BODY
    }

    @Override
    fun doInitBody() {
        // if(htmltable) body.append("<tr>\n");
    }

    @Override
    @Throws(PageException::class)
    fun doAfterBody(): Int {
        if (htmltable) body.append("</tr>\n") else body.append('\n')
        startNewRow = true
        // print.out(query.getCurrentrow()+"-"+query.getRecordcount());
        return if (++count < maxrows && query.next()) EVAL_BODY_AGAIN else SKIP_BODY
    }

    @Override
    @Throws(PageException::class)
    fun doEndTag(): Int {
        try {
            _doEndTag()
        } catch (e: IOException) {
            throw Caster.toPageException(e)
        }
        return EVAL_PAGE
    }

    @Throws(IOException::class)
    private fun _doEndTag() {
        if (htmltable) {
            pageContext.forceWrite("<table colspacing=\"$colspacing\"")
            if (border) {
                pageContext.forceWrite(" border=\"1\"")
            }
            pageContext.forceWrite(">\n")
            if (header.length() > 0) {
                pageContext.forceWrite("<tr>\n")
                pageContext.forceWrite(header.toString())
                pageContext.forceWrite("</tr>\n")
            }
            pageContext.forceWrite(body.toString())
            pageContext.forceWrite("</table>")
        } else {
            pageContext.forceWrite("<pre>")
            if (header.length() > 0) {
                pageContext.forceWrite(header.toString())
                pageContext.forceWrite(StringUtil.repeatString("\n", headerlines - 1))
            }
            pageContext.forceWrite(body.toString())
            pageContext.forceWrite("</pre>")
        }
    }

    @Override
    fun doFinally() {
        try {
            pageContext.undefinedScope().removeQuery()
            if (query != null) query.go(initRow, pageContext.getId())
        } catch (e: PageException) {
        }
    }

    /**
     * @param strHeader
     * @param text
     * @param align
     * @param width
     * @throws ExpressionException
     */
    @Throws(ExpressionException::class)
    fun setCol(strHeader: String?, text: String?, align: Short, width: Int) {
        // HTML
        var width = width
        if (htmltable) {
            if (colheaders && count == 0 && strHeader.trim().length() > 0) {
                header.append("\t<th")
                addAlign(header, align)
                addWidth(header, width)
                header.append(">")
                header.append(strHeader)
                header.append("</th>\n")
            }
            if (htmltable && startNewRow) {
                body.append("<tr>\n")
                startNewRow = false
            }
            body.append("\t<td")
            addAlign(body, align)
            addWidth(body, width)
            body.append(">")
            body.append(text)
            body.append("</td>\n")
        } else {
            if (width < 0) width = 20
            if (colheaders && count == 0 && strHeader.trim().length() > 0) {
                addPre(header, align, strHeader, width)
            }
            addPre(body, align, text, width)
        }
    }

    private fun addAlign(data: StringBuffer?, align: Short) {
        data.append(" align=\"")
        data.append(toStringAlign(align))
        data.append("\"")
    }

    private fun addWidth(data: StringBuffer?, width: Int) {
        if (width >= -1) {
            data.append(" width=\"")
            data.append(width)
            data.append("%\"")
        }
    }

    @Throws(ExpressionException::class)
    private fun addPre(data: StringBuffer?, align: Short, value: String?, length: Int) {
        if (align == ALIGN_RIGHT) data.append(RJustify.call(pageContext, value, length)) else if (align == ALIGN_CENTER) data.append(CJustify.call(pageContext, value, length)) else data.append(LJustify.call(pageContext, value, length))
    }

    private fun toStringAlign(align: Short): String? {
        if (align == ALIGN_RIGHT) return "right"
        return if (align == ALIGN_CENTER) "center" else "left"
    }

    companion object {
        /**
         * Field `ALIGN_LEFT`
         */
        const val ALIGN_LEFT: Short = 0

        /**
         * Field `ALIGN_CENTER`
         */
        const val ALIGN_CENTER: Short = 1

        /**
         * Field `ALIGN_RIGHT`
         */
        const val ALIGN_RIGHT: Short = 2
    }
}