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
package lucee.runtime.dump

import java.util.ArrayList

/**
 * class to generate Lucee HTML Boxes for dumps
 */
class DumpTable(
        /**
         * @return the type
         */
        val type: String?,
        /**
         * @param highLightColor the highLightColor to set
         */
        var highLightColor: String?,
        /**
         * @param normalColor the normalColor to set
         */
        var normalColor: String?,
        /**
         * @param borderColor the borderColor to set
         */
        var borderColor: String?,
        /**
         * @param fontColor the fontColor to set
         */
        var fontColor: String?) : DumpData {
    private val rows: List<DumpRow> = ArrayList<DumpRow>()
    /**
     * returns the title of the DumpTable, if not defined returns null
     *
     * @return title of the DumpTable
     */
    /**
     * @param title sets the title of the HTML Box
     */
    var title: String? = null
    /**
     * returns the comment of the DumpTable, if not defined returns null
     *
     * @return title of the DumpTable
     */
    /**
     * @param comment sets the comment of the HTML Box
     */
    var comment: String? = null
    /**
     * @return the highLightColor
     */
    /**
     * @return the normalColor
     */
    /**
     * @return the borderColor
     */
    /**
     * @return the fontColor
     */
    /**
     * @return the width
     */
    /**
     * @param width sets the With of the HTML Box, can be a number or a percentage value
     */
    var width: String? = null
    /**
     * @return the height
     */
    /**
     * @param height sets the Height of the HTML Box, can be a number or a percentage value
     */
    var height: String? = null

    var id: String? = null
    var ref: String? = null

    constructor(highLightColor: String?, normalColor: String?, borderColor: String?) : this(null, highLightColor, normalColor, borderColor, borderColor) {}
    constructor(type: String?, highLightColor: String?, normalColor: String?, borderColor: String?) : this(type, highLightColor, normalColor, borderColor, borderColor) {}

    /**
     * @return returns if the box has content or not
     */
    val isEmpty: Boolean
        get() = rows.isEmpty()

    /**
     * clear all data set in the HTMLBox
     */
    fun clear() {
        rows.clear()
    }

    /**
     * @return the rows
     */
    fun getRows(): Array<DumpRow> {
        return rows.toArray(arrayOfNulls<DumpRow>(rows.size()))
    }

    fun appendRow(row: DumpRow?) {
        rows.add(row)
    }

    fun appendRow(highlightType: Int, item1: DumpData) {
        appendRow(DumpRow(highlightType, arrayOf<DumpData>(item1)))
    }

    fun appendRow(highlightType: Int, item1: DumpData, item2: DumpData) {
        appendRow(DumpRow(highlightType, arrayOf<DumpData>(item1, item2)))
    }

    fun appendRow(highlightType: Int, item1: DumpData, item2: DumpData, item3: DumpData) {
        appendRow(DumpRow(highlightType, arrayOf<DumpData>(item1, item2, item3)))
    }

    fun appendRow(highlightType: Int, item1: DumpData, item2: DumpData, item3: DumpData, item4: DumpData) {
        appendRow(DumpRow(highlightType, arrayOf<DumpData>(item1, item2, item3, item4)))
    }

    fun appendRow(highlightType: Int, item1: DumpData, item2: DumpData, item3: DumpData, item4: DumpData, item5: DumpData) {
        appendRow(DumpRow(highlightType, arrayOf<DumpData>(item1, item2, item3, item4, item5)))
    }

    fun appendRow(highlightType: Int, item1: DumpData, item2: DumpData, item3: DumpData, item4: DumpData, item5: DumpData,
                  item6: DumpData) {
        appendRow(DumpRow(highlightType, arrayOf<DumpData>(item1, item2, item3, item4, item5, item6)))
    }

    fun prependRow(row: DumpRow?) {
        rows.add(0, row)
    }
}