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
package tachyon.runtime.rest

import java.util.List

class RestRequestListener(mapping: Mapping?, path: String?, matrix: Struct?, format: Int, hasFormatExtension: Boolean, accept: List<MimeType?>?, contentType: MimeType?,
                          defaultValue: Result?) : RequestListener {
    private val mapping: Mapping?
    private val path: String?
    private val format: Int
    private val matrix: Struct?
    private val defaultValue: Result?
    private var result: Result? = null
    private val accept: List<MimeType?>?
    private val contentType: MimeType?
    private val hasFormatExtension: Boolean
    @Override
    @Throws(PageException::class)
    fun execute(pc: PageContext?, requestedPage: PageSource?): PageSource? {
        result = mapping!!.getResult(pc, path, matrix, format, hasFormatExtension, accept, contentType, defaultValue)
        val req: HttpServletRequest = pc.getHttpServletRequest()
        req.setAttribute("client", "tachyon-rest-1-0")
        req.setAttribute("rest-path", path)
        req.setAttribute("rest-result", result)
        if (result == null) {
            RestUtil.setStatus(pc, 404, "no rest service for [" + HTMLEntities.escapeHTML(path).toString() + "] found in mapping [" + mapping.getVirtual().toString() + "]")
            ThreadLocalPageContext.getLog(pc, "rest").error("REST", "no rest service for [" + path + "] found in mapping [" + mapping.getVirtual() + "]")
            return null
        }
        return result.getSource()!!.getPageSource()
    }

    /**
     * @return the result
     */
    fun getResult(): Result? {
        return result
    }

    init {
        this.mapping = mapping
        this.path = path
        this.format = format
        this.hasFormatExtension = hasFormatExtension
        this.matrix = matrix
        this.defaultValue = defaultValue
        this.accept = accept
        this.contentType = contentType
    }
}