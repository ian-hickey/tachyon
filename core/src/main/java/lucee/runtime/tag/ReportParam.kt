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

class ReportParam : TagImpl() {
    private var param: ReportParamBean? = ReportParamBean()
    @Override
    fun release() {
        param = ReportParamBean()
        super.release()
    }

    /**
     * @param name the name to set
     */
    fun setName(name: String?) {
        param.setName(name)
    }

    /**
     * @param value the value to set
     */
    fun setValue(value: Object?) {
        param.setValue(value)
    }

    @Override
    @Throws(ApplicationException::class)
    fun doStartTag(): Int {
        // check

        // provide to parent
        var parent: Tag = this
        do {
            parent = parent.getParent()
            if (parent is Report) {
                (parent as Report)!!.addReportParam(param)
                break
            }
        } while (parent != null)
        return SKIP_BODY
    }

    init {
        // TODO implement tag
        throw TagNotSupported("ReportParam")
    }
}