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

import lucee.runtime.exp.ApplicationException

/**
 * this tag is not used, it will ranslation over an evaluator
 *
 *
 * Imports a jsp Tag Library or a Custom Tag Directory
 *
 *
 *
 */
class ImportTag : TagImpl() {
    private var path: String? = null
    @Override
    fun release() {
        path = null
        super.release()
    }

    /**
     * @param prefix
     */
    fun setPrefix(prefix: String?) {}
    fun setPath(path: String?) {
        this.path = path
    }

    /**
     * @param taglib
     */
    fun setTaglib(taglib: String?) {}
    @Override
    @Throws(ExpressionException::class, ApplicationException::class)
    fun doStartTag(): Int {
        return SKIP_BODY
    }

    @Override
    fun doEndTag(): Int {
        return EVAL_PAGE
    }
}