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
package lucee.runtime.err

import java.util.ArrayList

/**
 * Handle Page Errors
 */
class ErrorPagePool {
    private val pages: ArrayList<ErrorPage?>? = ArrayList<ErrorPage?>()
    private var hasChanged = false

    /**
     * sets the page error
     *
     * @param errorPage
     */
    fun setErrorPage(errorPage: ErrorPage?) {
        pages.add(errorPage)
        hasChanged = true
    }

    /**
     * returns the error page
     *
     * @param pe Page Exception
     * @return
     */
    fun getErrorPage(pe: PageException?, type: Short): ErrorPage? {
        for (i in pages.size() - 1 downTo 0) {
            val ep: ErrorPageImpl = pages.get(i)
            if (ep.getType() === type) {
                if (type == ErrorPage.TYPE_EXCEPTION) {
                    if (pe.typeEqual(ep.getTypeAsString())) return ep
                } else return ep
            }
        }
        return null
    }

    /**
     * clear the error page pool
     */
    fun clear() {
        if (hasChanged) {
            pages.clear()
        }
        hasChanged = false
    }

    /**
     * remove this error page
     *
     * @param type
     */
    fun removeErrorPage(pe: PageException?) {
        // exception
        var ep: ErrorPage? = getErrorPage(pe, ErrorPage.TYPE_EXCEPTION)
        if (ep != null) {
            pages.remove(ep)
            hasChanged = true
        }
        // request
        ep = getErrorPage(pe, ErrorPage.TYPE_REQUEST)
        if (ep != null) {
            pages.remove(ep)
            hasChanged = true
        }
        // validation
        ep = getErrorPage(pe, ErrorPage.TYPE_VALIDATION)
        if (ep != null) {
            pages.remove(ep)
            hasChanged = true
        }
    }
}