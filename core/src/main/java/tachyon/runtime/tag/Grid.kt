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

import tachyon.runtime.exp.TagNotSupported

class Grid : BodyTagImpl() {
    /** Width value of the grid control, in pixels.  */
    private var width = 0.0

    /**
     * The name of a query column when the grid uses a query. The column specified becomes the Key
     * regardless of the select mode for the grid.
     */
    private var hrefkey: String? = null

    /**
     * If Yes, sort buttons are added to the grid control. When clicked, sort buttons perform a simple
     * text sort on the selected column. Default is No. Note that columns are sortable by clicking the
     * column head, even if no sort button is displayed.
     */
    private var sort = false

    /** Yes or No. Yes displays column headers in the grid control. Default is Yes.  */
    private var colheaders = false

    /**
     * Text color value for the grid control row headers. Entries are: black (default), magenta, cyan,
     * orange, darkgray, pink, gray, white, lightgray, yellow. A hex value can be entered in the form:
     * rowHeaderTextColor = "##xxxxxx" Where x is 0-9 or A-F. Use two pound signs or no pound signs.
     */
    private var rowheadertextcolor: String? = null

    /** Font to use for column data in the grid control.  */
    private var font: String? = null

    /** Yes or No. Yes displays column header text in italic. Default is No.  */
    private var colheaderitalic = false

    /**
     * Optional. Yes or No. Default is No. If Yes, automatically sets the width of each column so that
     * all the columns are visible within the grid's specified width. All columns are initially set to
     * equal widths. Users can resize any column. No horizontal scroll bars are available since all
     * columns are visible. note that if you explicitly specify the width of a column and set autoWidth
     * to Yes, CFML will set the column to the explicit width, if possible.
     */
    private var autowidth = false

    /** Background color for a selected item. See bgColor for color options.  */
    private var selectcolor: String? = null

    /**
     * Yes highlights links associated with a cfgrid with an href attribute value. No disables
     * highlight. Default is Yes.
     */
    private var highlighthref = false

    /** Yes displays grid control text in italic. Default is No.  */
    private var italic = false

    /**
     * Yes or No. Yes enables row and column rules (lines) in the grid control. No suppresses rules.
     * Default is Yes.
     */
    private var gridlines = false

    /**
     * Yes or No. If Yes, images are used for the Insert, delete, and Sort buttons rather than text.
     * Default is No.
     */
    private var picturebar = false

    /** Text to use for the delete action button. The default is delete.  */
    private var deletebutton: String? = null

    /**
     * Color value for text in the grid control. Options are: black (default), magenta, cyan, orange,
     * darkgray, pink, gray, white, lightgray, yellow. A hex value can be entered in the form: textColor
     * = "##xxxxxx" where x is 0-9 or A-F. Use two pound signs or no pound signs.
     */
    private var textcolor: String? = null

    /** Text to use for the Insert action button. The default is Insert.  */
    private var insertbutton: String? = null

    /**
     * Number of pixels for the minimum row height of the grid control. Used with cfgridcolumn type =
     * "Image", you can use rowHeight to define enough space for graphics to display in the row.
     */
    private var rowheight = 0.0

    /** Selection mode for items in the grid control.  */
    private var notsupported: String? = null

    /** Font size for text in the grid control, in points.  */
    private var fontsize = 0.0

    /** The width, in pixels, of the row header column.  */
    private var rowheaderwidth = 0.0

    /** The name of a JavaScript function to execute in the event of a failed validation.  */
    private var onerror: String? = null

    /** Target attribute for href URL.  */
    private var target: String? = null

    /** Font for the column header in the grid control.  */
    private var colheaderfont: String? = null

    /** Enter Left, Right, or Center to position data in the grid within a column. Default is Left.  */
    private var griddataalign: String? = null

    /** Enter Left, Right, or Center to position data within a column header. Default is Left.  */
    private var colheaderalign: String? = null

    /** Height value of the grid control, in pixels.  */
    private var height = 0.0

    /** Name of the query associated with the grid control.  */
    private var query: String? = null

    /** Specifies the maximum number of rows to display in the grid.  */
    private var maxrows: String? = null

    /**
     * Alignment value. Options are: Top, Left, Bottom, Baseline, Texttop, Absbottom, Middle, Absmiddle,
     * Right.
     */
    private var align: String? = null

    /** Vertical margin spacing above and below the grid control, in pixels.  */
    private var vspace = 0.0

    /** Yes lets end users insert row data into the grid. Default is No.  */
    private var insert = false

    /**
     * Background color value for the grid control. Entries are: black, magenta, cyan, orange, darkgray,
     * pink, gray, white, lightgray, yellow. A hex value can be entered in the form: bgColor =
     * "##xxxxxx" where x is 0-9 or A-F. Use either two pound signs or no pound signs.
     */
    private var bgcolor: String? = null

    /**
     * When used with href, Yes passes query string value of the selected tree item in the URL to the
     * application page specified in the cfform action attribute. Default is Yes.
     */
    private var appendkey = false

    /** A name for the grid element.  */
    private var name: String? = null

    /** Text to use for the Sort button. Default is "A - Z".  */
    private var sortascendingbutton: String? = null

    /** Yes or No. Yes displays row label text in italic. Default is No.  */
    private var rowheaderitalic = false

    /**
     * The name of a JavaScript function used to validate user input. The form object, input object, and
     * input object value are passed to the routine, which should return True if validation succeeds and
     * False otherwise.
     */
    private var onvalidate: String? = null

    /**
     * URL to associate with the grid item or a query column for a grid that is populated from a query.
     * If href is a query column, the href value is populated by the query. If href is not recognized as
     * a query column, it is assumed that the href text is an actual HTML href.
     */
    private var href: String? = null

    /** Yes or No. Yes displays column header text in boldface. Default is No.  */
    private var colheaderbold = false

    /** Yes lets end users delete row data from the grid. Default is No.  */
    private var delete = false

    /** Size for row label text in the grid control, in points.  */
    private var rowheaderfontsize = 0.0

    /** Selection mode for items in the grid control.  */
    private var selectmode: String? = null

    /** Yes or No. Yes displays row label text in boldface. Default is No.  */
    private var rowheaderbold = false

    /** Size for column header text in the grid control, in points.  */
    private var colheaderfontsize = 0.0

    /** Enter Left, Right, or Center to position data within a row header. Default is Left.  */
    private var rowheaderalign: String? = null

    /** Font to use for the row label.  */
    private var rowheaderfont: String? = null

    /**
     * Yes or No. Yes displays a column of numeric row labels in the grid control. Defaults to Yes.
     */
    private var rowheaders = false

    /** Yes displays grid control text in boldface. Default is No.  */
    private var bold = false

    /**
     * Color value for the grid control column headers. Valid entries are: black (default), magenta,
     * cyan, orange, darkgray, pink, gray, white, lightgray, yellow.A hex value can be entered in the
     * form: colHeaderTextColor = "##xxxxxx" where x is 0-9 or A-F. Use either two pound signs or no
     * pound signs.
     */
    private var colheadertextcolor: String? = null

    /** Horizontal margin spacing to the left and right of the grid control, in pixels.  */
    private var hspace = 0.0

    /** Text to use for the Sort button. Default is "Z - A".  */
    private var sortdescendingbutton: String? = null
    private var format = 0
    private var enabled = false
    private var onchange: String? = null
    private var onblur: String? = null
    private var onfocus: String? = null
    private var style: String? = null
    private var tooltip: String? = null
    private var visible = false
    @Override
    fun release() {
        super.release()
        width = 0.0
        hrefkey = ""
        sort = false
        colheaders = false
        rowheadertextcolor = ""
        font = ""
        colheaderitalic = false
        autowidth = false
        selectcolor = ""
        highlighthref = false
        italic = false
        gridlines = false
        picturebar = false
        deletebutton = ""
        textcolor = ""
        insertbutton = ""
        rowheight = 0.0
        notsupported = ""
        fontsize = 0.0
        rowheaderwidth = 0.0
        onerror = ""
        target = ""
        colheaderfont = ""
        griddataalign = ""
        colheaderalign = ""
        height = 0.0
        query = ""
        maxrows = ""
        align = ""
        vspace = 0.0
        insert = false
        bgcolor = ""
        appendkey = false
        name = ""
        sortascendingbutton = ""
        rowheaderitalic = false
        onvalidate = ""
        href = ""
        colheaderbold = false
        delete = false
        rowheaderfontsize = 0.0
        selectmode = ""
        rowheaderbold = false
        colheaderfontsize = 0.0
        rowheaderalign = ""
        rowheaderfont = ""
        rowheaders = false
        bold = false
        colheadertextcolor = ""
        hspace = 0.0
        sortdescendingbutton = ""
        format = 0
        enabled = true
        onchange = null
        onblur = null
        onfocus = null
        style = null
        tooltip = null
        visible = true
    }

    /**
     * set the value width Width value of the grid control, in pixels.
     *
     * @param width value to set
     */
    fun setWidth(width: Double) {
        this.width = width
    }

    /**
     * set the value hrefkey The name of a query column when the grid uses a query. The column specified
     * becomes the Key regardless of the select mode for the grid.
     *
     * @param hrefkey value to set
     */
    fun setHrefkey(hrefkey: String?) {
        this.hrefkey = hrefkey
    }

    /**
     * set the value sort If Yes, sort buttons are added to the grid control. When clicked, sort buttons
     * perform a simple text sort on the selected column. Default is No. Note that columns are sortable
     * by clicking the column head, even if no sort button is displayed.
     *
     * @param sort value to set
     */
    fun setSort(sort: Boolean) {
        this.sort = sort
    }

    /**
     * set the value colheaders Yes or No. Yes displays column headers in the grid control. Default is
     * Yes.
     *
     * @param colheaders value to set
     */
    fun setColheaders(colheaders: Boolean) {
        this.colheaders = colheaders
    }

    /**
     * set the value rowheadertextcolor Text color value for the grid control row headers. Entries are:
     * black (default), magenta, cyan, orange, darkgray, pink, gray, white, lightgray, yellow. A hex
     * value can be entered in the form: rowHeaderTextColor = "##xxxxxx" Where x is 0-9 or A-F. Use two
     * pound signs or no pound signs.
     *
     * @param rowheadertextcolor value to set
     */
    fun setRowheadertextcolor(rowheadertextcolor: String?) {
        this.rowheadertextcolor = rowheadertextcolor
    }

    /**
     * set the value font Font to use for column data in the grid control.
     *
     * @param font value to set
     */
    fun setFont(font: String?) {
        this.font = font
    }

    /**
     * set the value colheaderitalic Yes or No. Yes displays column header text in italic. Default is
     * No.
     *
     * @param colheaderitalic value to set
     */
    fun setColheaderitalic(colheaderitalic: Boolean) {
        this.colheaderitalic = colheaderitalic
    }

    /**
     * set the value autowidth Optional. Yes or No. Default is No. If Yes, automatically sets the width
     * of each column so that all the columns are visible within the grid's specified width. All columns
     * are initially set to equal widths. Users can resize any column. No horizontal scroll bars are
     * available since all columns are visible. note that if you explicitly specify the width of a
     * column and set autoWidth to Yes, CFML will set the column to the explicit width, if possible.
     *
     * @param autowidth value to set
     */
    fun setAutowidth(autowidth: Boolean) {
        this.autowidth = autowidth
    }

    /**
     * set the value selectcolor Background color for a selected item. See bgColor for color options.
     *
     * @param selectcolor value to set
     */
    fun setSelectcolor(selectcolor: String?) {
        this.selectcolor = selectcolor
    }

    /**
     * set the value highlighthref Yes highlights links associated with a cfgrid with an href attribute
     * value. No disables highlight. Default is Yes.
     *
     * @param highlighthref value to set
     */
    fun setHighlighthref(highlighthref: Boolean) {
        this.highlighthref = highlighthref
    }

    /**
     * set the value italic Yes displays grid control text in italic. Default is No.
     *
     * @param italic value to set
     */
    fun setItalic(italic: Boolean) {
        this.italic = italic
    }

    /**
     * set the value gridlines Yes or No. Yes enables row and column rules (lines) in the grid control.
     * No suppresses rules. Default is Yes.
     *
     * @param gridlines value to set
     */
    fun setGridlines(gridlines: Boolean) {
        this.gridlines = gridlines
    }

    /**
     * set the value picturebar Yes or No. If Yes, images are used for the Insert, delete, and Sort
     * buttons rather than text. Default is No.
     *
     * @param picturebar value to set
     */
    fun setPicturebar(picturebar: Boolean) {
        this.picturebar = picturebar
    }

    /**
     * set the value deletebutton Text to use for the delete action button. The default is delete.
     *
     * @param deletebutton value to set
     */
    fun setDeletebutton(deletebutton: String?) {
        this.deletebutton = deletebutton
    }

    /**
     * set the value textcolor Color value for text in the grid control. Options are: black (default),
     * magenta, cyan, orange, darkgray, pink, gray, white, lightgray, yellow. A hex value can be entered
     * in the form: textColor = "##xxxxxx" where x is 0-9 or A-F. Use two pound signs or no pound signs.
     *
     * @param textcolor value to set
     */
    fun setTextcolor(textcolor: String?) {
        this.textcolor = textcolor
    }

    /**
     * set the value insertbutton Text to use for the Insert action button. The default is Insert.
     *
     * @param insertbutton value to set
     */
    fun setInsertbutton(insertbutton: String?) {
        this.insertbutton = insertbutton
    }

    /**
     * set the value rowheight Number of pixels for the minimum row height of the grid control. Used
     * with cfgridcolumn type = "Image", you can use rowHeight to define enough space for graphics to
     * display in the row.
     *
     * @param rowheight value to set
     */
    fun setRowheight(rowheight: Double) {
        this.rowheight = rowheight
    }

    /**
     * set the value notsupported Selection mode for items in the grid control.
     *
     * @param notsupported value to set
     */
    fun setNotsupported(notsupported: String?) {
        this.notsupported = notsupported
    }

    /**
     * set the value fontsize Font size for text in the grid control, in points.
     *
     * @param fontsize value to set
     */
    fun setFontsize(fontsize: Double) {
        this.fontsize = fontsize
    }

    /**
     * set the value rowheaderwidth The width, in pixels, of the row header column.
     *
     * @param rowheaderwidth value to set
     */
    fun setRowheaderwidth(rowheaderwidth: Double) {
        this.rowheaderwidth = rowheaderwidth
    }

    /**
     * set the value onerror The name of a JavaScript function to execute in the event of a failed
     * validation.
     *
     * @param onerror value to set
     */
    fun setOnerror(onerror: String?) {
        this.onerror = onerror
    }

    /**
     * set the value target Target attribute for href URL.
     *
     * @param target value to set
     */
    fun setTarget(target: String?) {
        this.target = target
    }

    /**
     * set the value colheaderfont Font for the column header in the grid control.
     *
     * @param colheaderfont value to set
     */
    fun setColheaderfont(colheaderfont: String?) {
        this.colheaderfont = colheaderfont
    }

    /**
     * set the value griddataalign Enter Left, Right, or Center to position data in the grid within a
     * column. Default is Left.
     *
     * @param griddataalign value to set
     */
    fun setGriddataalign(griddataalign: String?) {
        this.griddataalign = griddataalign
    }

    /**
     * set the value colheaderalign Enter Left, Right, or Center to position data within a column
     * header. Default is Left.
     *
     * @param colheaderalign value to set
     */
    fun setColheaderalign(colheaderalign: String?) {
        this.colheaderalign = colheaderalign
    }

    /**
     * set the value height Height value of the grid control, in pixels.
     *
     * @param height value to set
     */
    fun setHeight(height: Double) {
        this.height = height
    }

    /**
     * set the value query Name of the query associated with the grid control.
     *
     * @param query value to set
     */
    fun setQuery(query: String?) {
        this.query = query
    }

    /**
     * set the value maxrows Specifies the maximum number of rows to display in the grid.
     *
     * @param maxrows value to set
     */
    fun setMaxrows(maxrows: String?) {
        this.maxrows = maxrows
    }

    /**
     * set the value align Alignment value. Options are: Top, Left, Bottom, Baseline, Texttop,
     * Absbottom, Middle, Absmiddle, Right.
     *
     * @param align value to set
     */
    fun setAlign(align: String?) {
        this.align = align
    }

    /**
     * set the value vspace Vertical margin spacing above and below the grid control, in pixels.
     *
     * @param vspace value to set
     */
    fun setVspace(vspace: Double) {
        this.vspace = vspace
    }

    /**
     * set the value insert Yes lets end users insert row data into the grid. Default is No.
     *
     * @param insert value to set
     */
    fun setInsert(insert: Boolean) {
        this.insert = insert
    }

    /**
     * set the value bgcolor Background color value for the grid control. Entries are: black, magenta,
     * cyan, orange, darkgray, pink, gray, white, lightgray, yellow. A hex value can be entered in the
     * form: bgColor = "##xxxxxx" where x is 0-9 or A-F. Use either two pound signs or no pound signs.
     *
     * @param bgcolor value to set
     */
    fun setBgcolor(bgcolor: String?) {
        this.bgcolor = bgcolor
    }

    /**
     * set the value appendkey When used with href, Yes passes query string value of the selected tree
     * item in the URL to the application page specified in the cfform action attribute. Default is Yes.
     *
     * @param appendkey value to set
     */
    fun setAppendkey(appendkey: Boolean) {
        this.appendkey = appendkey
    }

    /**
     * set the value name A name for the grid element.
     *
     * @param name value to set
     */
    fun setName(name: String?) {
        this.name = name
    }

    /**
     * set the value sortascendingbutton Text to use for the Sort button. Default is "A - Z".
     *
     * @param sortascendingbutton value to set
     */
    fun setSortascendingbutton(sortascendingbutton: String?) {
        this.sortascendingbutton = sortascendingbutton
    }

    /**
     * set the value rowheaderitalic Yes or No. Yes displays row label text in italic. Default is No.
     *
     * @param rowheaderitalic value to set
     */
    fun setRowheaderitalic(rowheaderitalic: Boolean) {
        this.rowheaderitalic = rowheaderitalic
    }

    /**
     * set the value onvalidate The name of a JavaScript function used to validate user input. The form
     * object, input object, and input object value are passed to the routine, which should return True
     * if validation succeeds and False otherwise.
     *
     * @param onvalidate value to set
     */
    fun setOnvalidate(onvalidate: String?) {
        this.onvalidate = onvalidate
    }

    /**
     * set the value href URL to associate with the grid item or a query column for a grid that is
     * populated from a query. If href is a query column, the href value is populated by the query. If
     * href is not recognized as a query column, it is assumed that the href text is an actual HTML
     * href.
     *
     * @param href value to set
     */
    fun setHref(href: String?) {
        this.href = href
    }

    /**
     * set the value colheaderbold Yes or No. Yes displays column header text in boldface. Default is
     * No.
     *
     * @param colheaderbold value to set
     */
    fun setColheaderbold(colheaderbold: Boolean) {
        this.colheaderbold = colheaderbold
    }

    /**
     * set the value delete Yes lets end users delete row data from the grid. Default is No.
     *
     * @param delete value to set
     */
    fun setDelete(delete: Boolean) {
        this.delete = delete
    }

    /**
     * set the value rowheaderfontsize Size for row label text in the grid control, in points.
     *
     * @param rowheaderfontsize value to set
     */
    fun setRowheaderfontsize(rowheaderfontsize: Double) {
        this.rowheaderfontsize = rowheaderfontsize
    }

    /**
     * set the value selectmode Selection mode for items in the grid control.
     *
     * @param selectmode value to set
     */
    fun setSelectmode(selectmode: String?) {
        this.selectmode = selectmode
    }

    /**
     * set the value rowheaderbold Yes or No. Yes displays row label text in boldface. Default is No.
     *
     * @param rowheaderbold value to set
     */
    fun setRowheaderbold(rowheaderbold: Boolean) {
        this.rowheaderbold = rowheaderbold
    }

    /**
     * set the value colheaderfontsize Size for column header text in the grid control, in points.
     *
     * @param colheaderfontsize value to set
     */
    fun setColheaderfontsize(colheaderfontsize: Double) {
        this.colheaderfontsize = colheaderfontsize
    }

    /**
     * set the value rowheaderalign Enter Left, Right, or Center to position data within a row header.
     * Default is Left.
     *
     * @param rowheaderalign value to set
     */
    fun setRowheaderalign(rowheaderalign: String?) {
        this.rowheaderalign = rowheaderalign
    }

    /**
     * set the value rowheaderfont Font to use for the row label.
     *
     * @param rowheaderfont value to set
     */
    fun setRowheaderfont(rowheaderfont: String?) {
        this.rowheaderfont = rowheaderfont
    }

    /**
     * set the value rowheaders Yes or No. Yes displays a column of numeric row labels in the grid
     * control. Defaults to Yes.
     *
     * @param rowheaders value to set
     */
    fun setRowheaders(rowheaders: Boolean) {
        this.rowheaders = rowheaders
    }

    /**
     * set the value bold Yes displays grid control text in boldface. Default is No.
     *
     * @param bold value to set
     */
    fun setBold(bold: Boolean) {
        this.bold = bold
    }

    /**
     * set the value colheadertextcolor Color value for the grid control column headers. Valid entries
     * are: black (default), magenta, cyan, orange, darkgray, pink, gray, white, lightgray, yellow.A hex
     * value can be entered in the form: colHeaderTextColor = "##xxxxxx" where x is 0-9 or A-F. Use
     * either two pound signs or no pound signs.
     *
     * @param colheadertextcolor value to set
     */
    fun setColheadertextcolor(colheadertextcolor: String?) {
        this.colheadertextcolor = colheadertextcolor
    }

    /**
     * set the value hspace Horizontal margin spacing to the left and right of the grid control, in
     * pixels.
     *
     * @param hspace value to set
     */
    fun setHspace(hspace: Double) {
        this.hspace = hspace
    }

    /**
     * set the value sortdescendingbutton Text to use for the Sort button. Default is "Z - A".
     *
     * @param sortdescendingbutton value to set
     */
    fun setSortdescendingbutton(sortdescendingbutton: String?) {
        this.sortdescendingbutton = sortdescendingbutton
    }

    /**
     * @param enabled the enabled to set
     */
    fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
    }

    /**
     * @param format the format to set
     */
    fun setFormat(format: String?) {
        // this.format = format;
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
     * @param onfocus the onfocus to set
     */
    fun setOnfocus(onfocus: String?) {
        this.onfocus = onfocus
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

    @Override
    fun doStartTag(): Int {
        return SKIP_BODY
    }

    @Override
    fun doEndTag(): Int {
        return EVAL_PAGE
    }

    @Override
    fun doInitBody() {
    }

    fun addRow(data: Array<String?>?) {}
    fun addColumn(column: GridColumnBean?) {}

    /**
     * constructor for the tag class
     *
     * @throws TagNotSupported
     */
    init {
        // TODO implement tag
        throw TagNotSupported("grid")
    }
}