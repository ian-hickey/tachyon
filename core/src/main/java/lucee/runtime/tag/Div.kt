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

// MUST change behavior of multiple headers now is an array, it das so?
/**
 * Lets you execute HTTP POST and GET operations on files. Using cfhttp, you can execute standard
 * GET operations and create a query object from a text file. POST operations lets you upload MIME
 * file types to a server, or post cookie, formfield, URL, file, or CGI variables directly to a
 * specified server.
 *
 *
 *
 *
 */
class Div : BodyTagImpl() {
    private var bind: String? = null
    private var bindOnLoad = false
    private var id: String? = null
    private var onBindError: String? = null
    private var tagName: String? = null
    @Override
    fun release() {
        super.release()
        bind = null
        bindOnLoad = false
        id = null
        onBindError = null
        tagName = null
    }

    /**
     * @param bind the bind to set
     */
    fun setBind(bind: String?) {
        this.bind = bind
    }

    /**
     * @param bindOnLoad the bindOnLoad to set
     */
    fun setBindonload(bindOnLoad: Boolean) {
        this.bindOnLoad = bindOnLoad
    }

    /**
     * @param id the id to set
     */
    fun setId(id: String?) {
        this.id = id
    }

    /**
     * @param onBindError the onBindError to set
     */
    fun setOnbinderror(onBindError: String?) {
        this.onBindError = onBindError
    }

    /**
     * @param tagName the tagName to set
     */
    fun setTagname(tagName: String?) {
        this.tagName = tagName
    }

    @Override
    @Throws(TagNotSupported::class)
    fun doStartTag(): Int {
        throw TagNotSupported("Div")
        // return EVAL_BODY_INCLUDE;
    }

    @Override
    @Throws(PageException::class)
    fun doEndTag(): Int {
        return EVAL_PAGE
    }

    @Override
    fun doInitBody() {
    }

    @Override
    fun doAfterBody(): Int {
        return SKIP_BODY
    }

    /**
     * sets if has body or not
     *
     * @param hasBody
     */
    fun hasBody(hasBody: Boolean) {}
}