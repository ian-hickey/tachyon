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

class Formitem : BodyTagImpl() {
    private val type = 0
    private var style: String? = null
    private var width = -1
    private var height = -1
    private var enabled = true
    private var visible = true
    private var tooltip: String? = null
    private var bind: String? = null
    @Override
    fun release() {
        super.release()
        style = null
        width = -1
        height = -1
        enabled = true
        visible = true
        tooltip = null
        bind = null
    }

    /**
     * @param type the type to set
     */
    fun setType(type: String?) {
        // this.type = type;
    }

    /**
     * @param bind the bind to set
     */
    fun setBind(bind: String?) {
        this.bind = bind
    }

    /**
     * @param enabled the enabled to set
     */
    fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
    }

    /**
     * @param height the height to set
     */
    fun setHeight(height: Double) {
        this.height = height.toInt()
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
        throw TagNotSupported("formitem")
        // TODO impl. Tag formItem
    }
}