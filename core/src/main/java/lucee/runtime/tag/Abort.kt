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

import lucee.commons.io.log.Log

/**
 * Stops processing of a page at the tag location. the engine returns everything that was processed
 * before the cfabort tag. The cfabort tag is often used with conditional logic to stop processing a
 * page when a condition occurs.
 *
 *
 *
 */
class Abort : TagImpl() {
    /**
     * The error to display when cfabort executes. The error message displays in the standard CFML error
     * page.
     */
    private var showerror: String? = null
    private var type: Int = lucee.runtime.exp.Abort.SCOPE_REQUEST

    /**
     * set the value showerror The error to display when cfabort executes. The error message displays in
     * the standard CFML error page.
     *
     * @param showerror value to set
     */
    fun setShowerror(showerror: String?) {
        this.showerror = showerror
    }

    /**
     * sets the type of the abort (page,request)
     *
     * @param type
     * @throws ApplicationException
     */
    @Throws(ApplicationException::class)
    fun setType(type: String?) {
        var type = type
        type = type.toLowerCase().trim()
        if (type.equals("page")) this.type = lucee.runtime.exp.Abort.SCOPE_PAGE else if (type.equals("request")) this.type = lucee.runtime.exp.Abort.SCOPE_REQUEST else throw ApplicationException("attribute type has an invalid value [$type], valid values are [page,request]")
    }

    @Override
    @Throws(PageException::class)
    fun doStartTag(): Int {
        val log: Log = ThreadLocalPageContext.getLog(pageContext, "application")
        if (log != null) log.log(Log.LEVEL_TRACE, "cfabort", "abort at " + CallStackGet.call(pageContext, "text"))
        if (showerror != null) throw AbortException(showerror)
        throw Abort(type)
    }

    @Override
    fun doEndTag(): Int {
        return EVAL_PAGE
    }

    @Override
    fun release() {
        super.release()
        showerror = null
        type = lucee.runtime.exp.Abort.SCOPE_REQUEST
    }
}