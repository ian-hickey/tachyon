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

import lucee.runtime.exp.PageException

class AjaxProxy : TagImpl() {
    private var bind: String? = null
    private var cfc: String? = null
    private var jsClassName: String? = null
    private var onError: String? = null
    private var onSuccess: String? = null
    @Override
    fun release() {
        super.release()
        bind = null
        cfc = null
        jsClassName = null
        onError = null
        onSuccess = null
    }

    /**
     * @param bind the bind to set
     */
    fun setBind(bind: String?) {
        this.bind = bind
    }

    /**
     * @param cfc the cfc to set
     */
    fun setCfc(cfc: String?) {
        this.cfc = cfc
    }

    /**
     * @param jsClassName the jsClassName to set
     */
    fun setJsclassname(jsClassName: String?) {
        this.jsClassName = jsClassName
    }

    /**
     * @param onError the onError to set
     */
    fun setOnerror(onError: String?) {
        this.onError = onError
    }

    /**
     * @param onSuccess the onSuccess to set
     */
    fun setOnsuccess(onSuccess: String?) {
        this.onSuccess = onSuccess
    }

    @Override
    @Throws(PageException::class)
    fun doStartTag(): Int {
        throw TagNotSupported("AjaxProxy")
        // return SKIP_BODY;
    }

    @Override
    fun doEndTag(): Int {
        return EVAL_PAGE
    }
}