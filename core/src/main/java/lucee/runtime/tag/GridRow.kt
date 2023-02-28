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
 * Lets you define a cfgrid that does not use a query as source for row data. If a query attribute
 * is specified in cfgrid, the cfgridrow tags are ignored.
 *
 *
 *
 */
class GridRow : TagImpl() {
    /**
     * A comma-separated list of column values. If a column value contains a comma character, it must be
     * escaped with a second comma character.
     */
    private var data: Array<String?>?
    @Override
    fun release() {
        super.release()
        data = null
    }

    /**
     * set the value data A comma-separated list of column values. If a column value contains a comma
     * character, it must be escaped with a second comma character.
     *
     * @param data value to set
     */
    fun setData(data: String?) {
        this.data = ListUtil.listToStringArray(data, ',')
    }

    @Override
    fun doStartTag(): Int {
        // provide to parent
        var parent: Tag = this
        do {
            parent = parent.getParent()
            if (parent is Grid) {
                (parent as Grid)!!.addRow(data)
                break
            }
        } while (parent != null)
        return SKIP_BODY
    }

    @Override
    fun doEndTag(): Int {
        return EVAL_PAGE
    }

    init {
        throw TagNotSupported("GridRow")
    }
}