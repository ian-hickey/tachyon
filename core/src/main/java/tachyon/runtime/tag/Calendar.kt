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

import javax.servlet.jsp.JspException

class Calendar : TagImpl() {
    private var name: String? = null
    private var height = -1
    private var width = -1
    private var selectedDate: DateTime? = null
    private var startRange: DateTime? = null
    private var endRange: DateTime? = null
    private var disabled = false
    private var mask: String? = "MM/DD/YYYY"
    private var firstDayOfWeek = 0
    private var dayNames = DAY_NAMES_DEFAULT
    private var monthNames = MONTH_NAMES_DEFAULT
    private var style: String? = null
    private var enabled = true
    private var visible = true
    private var tooltip: String? = null
    private var onChange: String? = null
    private var onBlur: String? = null
    private var onFocus: String? = null
    @Override
    fun release() {
        super.release()
        name = null
        height = -1
        width = -1
        selectedDate = null
        startRange = null
        endRange = null
        disabled = false
        mask = "MM/DD/YYYY"
        firstDayOfWeek = 0
        dayNames = DAY_NAMES_DEFAULT
        monthNames = MONTH_NAMES_DEFAULT
        style = null
        enabled = true
        visible = true
        tooltip = null
        onChange = null
        onBlur = null
        onFocus = null
    }

    @Override
    @Throws(JspException::class)
    fun doStartTag(): Int {
        return super.doStartTag()
    }

    /**
     * @param dayNames the dayNames to set
     */
    fun setDaynames(listDayNames: String?) {
        dayNames = ListUtil.listToStringArray(listDayNames, ',')
    }

    /**
     * @param disabled the disabled to set
     */
    fun setDisabled(disabled: Boolean) {
        this.disabled = disabled
    }

    /**
     * @param enabled the enabled to set
     */
    fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
    }

    /**
     * @param endRange the endRange to set
     */
    fun setEndrange(endRange: DateTime?) {
        this.endRange = endRange
    }

    /**
     * @param firstDayOfWeek the firstDayOfWeek to set
     */
    fun setFirstdayofweek(firstDayOfWeek: Double) {
        this.firstDayOfWeek = firstDayOfWeek.toInt()
    }

    /**
     * @param height the height to set
     */
    fun setHeight(height: Double) {
        this.height = height.toInt()
    }

    /**
     * @param mask the mask to set
     */
    fun setMask(mask: String?) {
        this.mask = mask
    }

    /**
     * @param monthNames the monthNames to set
     */
    fun setMonthnames(listMonthNames: String?) {
        monthNames = monthNames
    }

    /**
     * @param name the name to set
     */
    fun setName(name: String?) {
        this.name = name
    }

    /**
     * @param onBlur the onBlur to set
     */
    fun setOnblur(onBlur: String?) {
        this.onBlur = onBlur
    }

    /**
     * @param onChange the onChange to set
     */
    fun setOnchange(onChange: String?) {
        this.onChange = onChange
    }

    /**
     * @param onFocus the onFocus to set
     */
    fun setOnfocus(onFocus: String?) {
        this.onFocus = onFocus
    }

    /**
     * @param selectedDate the selectedDate to set
     */
    fun setSelecteddate(selectedDate: DateTime?) {
        this.selectedDate = selectedDate
    }

    /**
     * @param startRange the startRange to set
     */
    fun setStartrange(startRange: DateTime?) {
        this.startRange = startRange
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

    companion object {
        private val DAY_NAMES_DEFAULT: Array<String?>? = arrayOf("S", "M", "T", "W", "Th", "F", "S")
        private val MONTH_NAMES_DEFAULT: Array<String?>? = arrayOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November",
                "December")
    }

    init {
        // TODO impl. tag Calendar
        throw TagNotSupported("Calendar")
    }
}