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

// TODO tag invokeargument
// attr omit
/**
 * Required for cfhttp POST operations, cfhttpparam is used to specify the parameters necessary to
 * build a cfhttp POST.
 *
 *
 *
 */
class InvokeArgument : TagImpl() {
    /** A variable name for the data being passed.  */
    private var name: String? = null

    /** Specifies the value of the variable being passed.  */
    private var value: Object? = null
    private var omit = false

    /**
     * set the value value
     *
     * @param value value to set
     */
    fun setValue(value: Object?) {
        this.value = value
    }

    /**
     * set the value name
     *
     * @param name value to set
     */
    fun setName(name: String?) {
        this.name = name
    }

    /**
     * @param omit the omit to set
     */
    fun setOmit(omit: Boolean) {
        this.omit = omit
    }

    @Override
    @Throws(PageException::class)
    fun doStartTag(): Int {
        var parent: Tag = getParent()
        while (parent != null && parent !is Invoke) {
            parent = parent.getParent()
        }
        if (parent is Invoke) {
            parent!!.setArgument(name, value)
        } else {
            throw ApplicationException("Wrong Context, tag InvokeArgument must be inside an Invoke tag")
        }
        return SKIP_BODY
    }

    @Override
    fun doEndTag(): Int {
        return EVAL_PAGE
    }

    @Override
    fun release() {
        super.release()
        value = null
        name = null
        omit = false
    }
}