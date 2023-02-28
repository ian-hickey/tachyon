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

import java.awt.Color

class GridColumnBean {
    /**
     * @return the display
     */
    /**
     * @param display the display to set
     */
    var isDisplay = false
    private var width = 0
    /**
     * @return the header
     */
    /**
     * @param header the header to set
     */
    var header: String? = null
    /**
     * @return the headerFont
     */
    /**
     * @param headerFont the headerFont to set
     */
    var headerFont: String? = null
    /**
     * @return the headerItalic
     */
    /**
     * @param headerItalic the headerItalic to set
     */
    var isHeaderItalic = false
    /**
     * @return the headerBold
     */
    /**
     * @param headerBold the headerBold to set
     */
    var isHeaderBold = false
    /**
     * @return the headerFontSize
     */
    /**
     * @param headerFontSize the headerFontSize to set
     */
    var headerFontSize = 0
    /**
     * @return the headerTextColor
     */
    /**
     * @param headerTextColor the headerTextColor to set
     */
    var headerTextColor: Color? = null
    /**
     * @return the headerAlign
     */
    /**
     * @param headerAlign the headerAlign to set
     */
    var headerAlign: String? = null
    /**
     * @return the href
     */
    /**
     * @param href the href to set
     */
    var href: String? = null
    /**
     * @return the hrefKey
     */
    /**
     * @param hrefKey the hrefKey to set
     */
    var hrefKey: String? = null
    /**
     * @return the target
     */
    /**
     * @param target the target to set
     */
    var target: String? = null
    /**
     * @return the values
     */
    /**
     * @param values the values to set
     */
    var values: Array<String?>?
    /**
     * @return the valuesDisplay
     */
    /**
     * @param valuesDisplay the valuesDisplay to set
     */
    var valuesDisplay: Array<String?>?
    /**
     * @return the font
     */
    /**
     * @param font the font to set
     */
    var font: String? = null
    /**
     * @return the fontSize
     */
    /**
     * @param fontSize the fontSize to set
     */
    var fontSize = 0
    /**
     * @return the italic
     */
    /**
     * @param italic the italic to set
     */
    var isItalic = false
    /**
     * @return the bgColor
     */
    /**
     * @param bgColor the bgColor to set
     */
    var bgColor: Color? = null
    /**
     * @return the name
     */
    /**
     * @param name the name to set
     */
    var name: String? = null
    /**
     * @return the type
     */
    /**
     * @param type the type to set
     */
    var type: String? = null
    /**
     * @return the numberFormat
     */
    /**
     * @param numberFormat the numberFormat to set
     */
    var numberFormat: String? = null
    /**
     * @return the textColor
     */
    /**
     * @param textColor the textColor to set
     */
    var textColor: Color? = null
    /**
     * @return the select
     */
    /**
     * @param select the select to set
     */
    var isSelect = false
    /**
     * @return the dataAlign
     */
    /**
     * @param dataAlign the dataAlign to set
     */
    var dataAlign: String? = null
    /**
     * @return the bold
     */
    /**
     * @param bold the bold to set
     */
    var isBold = false
    /**
     * @return the mask
     */
    /**
     * @param mask the mask to set
     */
    var mask: String? = null

    /**
     * @return the width
     */
    fun getWidth(): Double {
        return width.toDouble()
    }

    /**
     * @param width the width to set
     */
    fun setWidth(width: Int) {
        this.width = width
    }
}