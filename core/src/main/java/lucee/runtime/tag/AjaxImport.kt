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

class AjaxImport : TagImpl() {
    // private String csssrc;
    // private String scriptsrc;
    // private String tags;
    @Override
    fun release() {
        super.release()
        // csssrc=null;
        // scriptsrc=null;
        // tags=null;
    }

    /**
     * @param csssrc the csssrc to set
     */
    fun setCsssrc(csssrc: String?) {
        // this.csssrc = csssrc;
    }

    /**
     * @param scriptsrc the scriptsrc to set
     */
    fun setScriptsrc(scriptsrc: String?) {
        // this.scriptsrc = scriptsrc;
    }

    /**
     * @param tags the tags to set
     */
    fun setTags(tags: String?) {
        // this.tags = tags;
    }

    @Override
    @Throws(PageException::class)
    fun doStartTag(): Int {
        throw TagNotSupported("AjaxImport")
        // return SKIP_BODY;
    }

    @Override
    fun doEndTag(): Int {
        return EVAL_PAGE
    }
}