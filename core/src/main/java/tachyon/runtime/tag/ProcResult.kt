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

import javax.servlet.jsp.tagext.Tag

class ProcResult : TagSupport() {
    private var result: ProcResultBean? = ProcResultBean()
    @Override
    fun release() {
        result = ProcResultBean()
        super.release()
    }

    /**
     * @param maxrows The maxrows to set.
     */
    fun setMaxrows(maxrows: Double) {
        result.setMaxrows(Caster.toIntValue(maxrows))
    }

    /**
     * @param name The name to set.
     */
    fun setName(name: String?) {
        result.setName(name)
    }

    /**
     * @param resultset The resultset to set.
     * @throws ApplicationException
     */
    @Throws(ApplicationException::class)
    fun setResultset(resultset: Double) {
        if (resultset < 1) throw ApplicationException("value of attribute resultset must be a numeric value greater or equal to 1")
        result.setResultset(resultset.toInt())
    }

    @Override
    @Throws(ApplicationException::class)
    fun doStartTag(): Int {

        // provide to parent
        var parent: Tag = getParent()
        while (parent != null && parent !is StoredProc) {
            parent = parent.getParent()
        }
        if (parent is StoredProc) {
            (parent as StoredProc)!!.addProcResult(result)
        } else {
            throw ApplicationException("Wrong Context, tag ProcResult must be inside a StoredProc tag")
        }
        return SKIP_BODY
    }
}