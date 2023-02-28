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

import lucee.runtime.exp.TagNotSupported

class Formgroup : BodyTagImpl() {
    private val type = 0
    private var query: Query? = null
    private var startrow = 0
    private var maxrows = -1
    private var label: String? = null
    private var style: String? = null
    private var selectedIndex = -1
    private var width = -1
    private var height = -1
    private var enabled = true
    private var visible = true
    private var onChange: String? = null
    private var tooltip: String? = null
    private var id: String? = null
    @Override
    fun release() {
        super.release()
        query = null
        startrow = 0
        maxrows = -1
        label = null
        style = null
        selectedIndex = -1
        width = -1
        height = -1
        enabled = true
        visible = true
        onChange = null
        tooltip = null
        id = null
    }

    /**
     * @param type the type to set
     */
    fun setType(type: String?) {
        // this.type = type;
    }

    /**
     * @param label the label to set
     */
    fun setLabel(label: String?) {
        this.label = label
    }

    /**
     * @param maxrows the maxrows to set
     */
    fun setMaxrows(maxrows: Double) {
        this.maxrows = maxrows.toInt()
    }

    /**
     * @param onChange the onChange to set
     */
    fun setOnchange(onChange: String?) {
        this.onChange = onChange
    }

    /**
     * @param query the query to set
     */
    fun setQuery(queryName: String?) {
        // this.query = query;
    }

    /**
     * @param selectedIndex the selectedIndex to set
     */
    fun setSelectedindex(selectedIndex: Double) {
        this.selectedIndex = selectedIndex.toInt()
    }

    /**
     * @param startrow the startrow to set
     */
    fun setStartrow(startrow: Double) {
        this.startrow = startrow.toInt()
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
     * @param width the width to set
     */
    fun setWidth(width: Double) {
        this.width = width.toInt()
    }

    init {
        throw TagNotSupported("formgroup")
        // TODO impl tag formgroup
    }
}