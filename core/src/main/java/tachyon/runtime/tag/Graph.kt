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

import tachyon.runtime.exp.ExpressionException

/**
 * Displays a graphical representation of data.
 *
 *
 *
 */
class Graph : TagImpl() {
    /** The font used for the item labels.  */
    private var itemlabelfont: String? = null

    /** The placement of the legend that identifies colors with the data labels.  */
    private var showlegend: String? = null

    /**
     * Title to display centered above the chart, or below the chart if the legend is above the chart.
     */
    private var title: String? = null

    /** The size the value text, in points.  */
    private var valuelabelsize = 0.0

    /** The size of the item labels, in points.  */
    private var itemlabelsize = 0.0

    /** Width of the graph line, in pixels.  */
    private var fill = 0.0

    /** Border color.  */
    private var bordercolor: String? = null

    /**
     * Name of the query containing the data to graph. Required if you do not use cfgraphdata tags in
     * the cfgraph tag body to specify the data values.
     */
    private var query: String? = null

    /** The font used to display data values.  */
    private var valuelabelfont: String? = null

    /** The font used to display the title.  */
    private var titlefont: String? = null

    /**
     * An integer that specifies the number of grid lines to display on the chart between the top and
     * bottom lines.
     */
    private var gridlines = 0.0

    /** A URL to load when the user clicks any data point on the chart.  */
    private var url: String? = null

    /**
     * Query column that contains the data values. Required if you do not use cfgraphdata tags in the
     * cfgraph tag body to specify the data values.
     */
    private var valuecolumn: String? = null

    /** Spacing between bars in the chart, in pixels.  */
    private var barspacing = 0.0

    /**
     * Specifies whether to fill the area below the line with the line color to create an area graph.
     */
    private var linewidth = 0.0

    /** Border thickness, in pixels.  */
    private var borderwidth: String? = null

    /** Specifies whether values are displayed for the data points.  */
    private var showvaluelabel = false

    /**
     * The minimum value of the graph value axis (the vertical axis for Bar charts, the horizontal axis
     * for HorizontalBar charts).
     */
    private var scalefrom = 0.0

    /**
     * Specifies whether to put item labels on the horizontal axis of bar charts and the vertical axis
     * of HorizontalBar charts.
     */
    private var showitemlabel = false

    /** Type of chart to display.  */
    private var type: String? = null

    /** Depth of 3D chart appearance, in pixels.  */
    private var depth = 0.0

    /**
     * Query column containing URL information to load when the user clicks the corresponding data
     * point.
     */
    private var urlcolumn: String? = null

    /** The font used to display the legend.  */
    private var legendfont: String? = null

    /** Color of the chart background.  */
    private var backgroundcolor: String? = null

    /** Comma delimited list of colors to use for each data point.  */
    private var colorlist: String? = null

    /** The maximum value of the graph value axis.  */
    private var scaleto = 0.0

    /** Width of the graph, in pixels. Default is 320.  */
    private var graphwidth = 0.0

    /** Where value labels are placed.  */
    private var valuelocation: String? = null

    /**
     * Query column that contains the item label for the corresponding data point. The item labels
     * appear in the chart legend.
     */
    private var itemcolumn: String? = null

    /** Orientation of item labels.  */
    private var itemlabelorientation: String? = null

    /** The color used to draw the data line.  */
    private var linecolor: String? = null

    /** Height of the graph, in pixels. Default is 240.  */
    private var graphheight = 0.0

    /** File type to be used for the output displayed in the browser.  */
    private var fileformat: String? = null

    /**
     * set the value itemlabelfont The font used for the item labels.
     *
     * @param itemlabelfont value to set
     */
    fun setItemlabelfont(itemlabelfont: String?) {
        this.itemlabelfont = itemlabelfont
    }

    /**
     * set the value showlegend The placement of the legend that identifies colors with the data labels.
     *
     * @param showlegend value to set
     */
    fun setShowlegend(showlegend: String?) {
        this.showlegend = showlegend
    }

    /**
     * set the value title Title to display centered above the chart, or below the chart if the legend
     * is above the chart.
     *
     * @param title value to set
     */
    fun setTitle(title: String?) {
        this.title = title
    }

    /**
     * set the value valuelabelsize The size the value text, in points.
     *
     * @param valuelabelsize value to set
     */
    fun setValuelabelsize(valuelabelsize: Double) {
        this.valuelabelsize = valuelabelsize
    }

    /**
     * set the value itemlabelsize The size of the item labels, in points.
     *
     * @param itemlabelsize value to set
     */
    fun setItemlabelsize(itemlabelsize: Double) {
        this.itemlabelsize = itemlabelsize
    }

    /**
     * set the value fill Width of the graph line, in pixels.
     *
     * @param fill value to set
     */
    fun setFill(fill: Double) {
        this.fill = fill
    }

    /**
     * set the value bordercolor Border color.
     *
     * @param bordercolor value to set
     */
    fun setBordercolor(bordercolor: String?) {
        this.bordercolor = bordercolor
    }

    /**
     * set the value query Name of the query containing the data to graph. Required if you do not use
     * cfgraphdata tags in the cfgraph tag body to specify the data values.
     *
     * @param query value to set
     */
    fun setQuery(query: String?) {
        this.query = query
    }

    /**
     * set the value valuelabelfont The font used to display data values.
     *
     * @param valuelabelfont value to set
     */
    fun setValuelabelfont(valuelabelfont: String?) {
        this.valuelabelfont = valuelabelfont
    }

    /**
     * set the value titlefont The font used to display the title.
     *
     * @param titlefont value to set
     */
    fun setTitlefont(titlefont: String?) {
        this.titlefont = titlefont
    }

    /**
     * set the value gridlines An integer that specifies the number of grid lines to display on the
     * chart between the top and bottom lines.
     *
     * @param gridlines value to set
     */
    fun setGridlines(gridlines: Double) {
        this.gridlines = gridlines
    }

    /**
     * set the value url A URL to load when the user clicks any data point on the chart.
     *
     * @param url value to set
     */
    fun setUrl(url: String?) {
        this.url = url
    }

    /**
     * set the value valuecolumn Query column that contains the data values. Required if you do not use
     * cfgraphdata tags in the cfgraph tag body to specify the data values.
     *
     * @param valuecolumn value to set
     */
    fun setValuecolumn(valuecolumn: String?) {
        this.valuecolumn = valuecolumn
    }

    /**
     * set the value barspacing Spacing between bars in the chart, in pixels.
     *
     * @param barspacing value to set
     */
    fun setBarspacing(barspacing: Double) {
        this.barspacing = barspacing
    }

    /**
     * set the value linewidth Specifies whether to fill the area below the line with the line color to
     * create an area graph.
     *
     * @param linewidth value to set
     */
    fun setLinewidth(linewidth: Double) {
        this.linewidth = linewidth
    }

    /**
     * set the value borderwidth Border thickness, in pixels.
     *
     * @param borderwidth value to set
     */
    fun setBorderwidth(borderwidth: String?) {
        this.borderwidth = borderwidth
    }

    /**
     * set the value showvaluelabel Specifies whether values are displayed for the data points.
     *
     * @param showvaluelabel value to set
     */
    fun setShowvaluelabel(showvaluelabel: Boolean) {
        this.showvaluelabel = showvaluelabel
    }

    /**
     * set the value scalefrom The minimum value of the graph value axis (the vertical axis for Bar
     * charts, the horizontal axis for HorizontalBar charts).
     *
     * @param scalefrom value to set
     */
    fun setScalefrom(scalefrom: Double) {
        this.scalefrom = scalefrom
    }

    /**
     * set the value showitemlabel Specifies whether to put item labels on the horizontal axis of bar
     * charts and the vertical axis of HorizontalBar charts.
     *
     * @param showitemlabel value to set
     */
    fun setShowitemlabel(showitemlabel: Boolean) {
        this.showitemlabel = showitemlabel
    }

    /**
     * set the value type Type of chart to display.
     *
     * @param type value to set
     */
    fun setType(type: String?) {
        this.type = type
    }

    /**
     * set the value depth Depth of 3D chart appearance, in pixels.
     *
     * @param depth value to set
     */
    fun setDepth(depth: Double) {
        this.depth = depth
    }

    /**
     * set the value urlcolumn Query column containing URL information to load when the user clicks the
     * corresponding data point.
     *
     * @param urlcolumn value to set
     */
    fun setUrlcolumn(urlcolumn: String?) {
        this.urlcolumn = urlcolumn
    }

    /**
     * set the value legendfont The font used to display the legend.
     *
     * @param legendfont value to set
     */
    fun setLegendfont(legendfont: String?) {
        this.legendfont = legendfont
    }

    /**
     * set the value backgroundcolor Color of the chart background.
     *
     * @param backgroundcolor value to set
     */
    fun setBackgroundcolor(backgroundcolor: String?) {
        this.backgroundcolor = backgroundcolor
    }

    /**
     * set the value colorlist Comma delimited list of colors to use for each data point.
     *
     * @param colorlist value to set
     */
    fun setColorlist(colorlist: String?) {
        this.colorlist = colorlist
    }

    /**
     * set the value scaleto The maximum value of the graph value axis.
     *
     * @param scaleto value to set
     */
    fun setScaleto(scaleto: Double) {
        this.scaleto = scaleto
    }

    /**
     * set the value graphwidth Width of the graph, in pixels. Default is 320.
     *
     * @param graphwidth value to set
     */
    fun setGraphwidth(graphwidth: Double) {
        this.graphwidth = graphwidth
    }

    /**
     * set the value valuelocation Where value labels are placed.
     *
     * @param valuelocation value to set
     */
    fun setValuelocation(valuelocation: String?) {
        this.valuelocation = valuelocation
    }

    /**
     * set the value itemcolumn Query column that contains the item label for the corresponding data
     * point. The item labels appear in the chart legend.
     *
     * @param itemcolumn value to set
     */
    fun setItemcolumn(itemcolumn: String?) {
        this.itemcolumn = itemcolumn
    }

    /**
     * set the value itemlabelorientation Orientation of item labels.
     *
     * @param itemlabelorientation value to set
     */
    fun setItemlabelorientation(itemlabelorientation: String?) {
        this.itemlabelorientation = itemlabelorientation
    }

    /**
     * set the value linecolor The color used to draw the data line.
     *
     * @param linecolor value to set
     */
    fun setLinecolor(linecolor: String?) {
        this.linecolor = linecolor
    }

    /**
     * set the value graphheight Height of the graph, in pixels. Default is 240.
     *
     * @param graphheight value to set
     */
    fun setGraphheight(graphheight: Double) {
        this.graphheight = graphheight
    }

    /**
     * set the value fileformat File type to be used for the output displayed in the browser.
     *
     * @param fileformat value to set
     */
    fun setFileformat(fileformat: String?) {
        this.fileformat = fileformat
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
    fun release() {
        super.release()
        itemlabelfont = ""
        showlegend = ""
        title = ""
        valuelabelsize = 0.0
        itemlabelsize = 0.0
        fill = 0.0
        bordercolor = ""
        query = ""
        valuelabelfont = ""
        titlefont = ""
        gridlines = 0.0
        url = ""
        valuecolumn = ""
        barspacing = 0.0
        linewidth = 0.0
        borderwidth = ""
        showvaluelabel = false
        scalefrom = 0.0
        showitemlabel = false
        type = ""
        depth = 0.0
        urlcolumn = ""
        legendfont = ""
        backgroundcolor = ""
        colorlist = ""
        scaleto = 0.0
        graphwidth = 0.0
        valuelocation = ""
        itemcolumn = ""
        itemlabelorientation = ""
        linecolor = ""
        graphheight = 0.0
        fileformat = ""
    }

    /**
     * constructor for the tag class
     */
    init {
        throw ExpressionException("tag cfgraph is deprecated")
    }
}