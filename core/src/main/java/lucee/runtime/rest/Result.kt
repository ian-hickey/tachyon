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
package lucee.runtime.rest

import java.util.List

class Result(source: Source?, variables: Struct?, path: Array<String?>?, matrix: Struct?, format: Int, hasFormatExtension: Boolean, accept: List<MimeType?>?, contentType: MimeType?) {
    private val source: Source?

    /**
     * @return the path
     */
    val path: Array<String?>?
    private val variables: Struct?

    /**
     * @return the format
     */
    val format: Int
    private val matrix: Struct?
    private var rsp: Struct? = null
    private val accept: List<MimeType?>?
    private val contentType: MimeType?
    private val hasFormatExtension: Boolean

    /**
     * @return the hasFormatExtension
     */
    fun hasFormatExtension(): Boolean {
        return hasFormatExtension
    }

    /**
     * @return the accept
     */
    fun getAccept(): Array<MimeType?>? {
        return accept.toArray(arrayOfNulls<MimeType?>(accept!!.size()))
    }

    /**
     * @return the accept
     */
    fun getContentType(): MimeType? {
        return if (contentType == null) MimeType.ALL else contentType
    }

    /**
     * @return the variables
     */
    fun getVariables(): Struct? {
        return variables
    }

    /**
     * @return the source
     */
    fun getSource(): Source? {
        return source
    }

    /**
     * @return the matrix
     */
    fun getMatrix(): Struct? {
        return matrix
    }

    var customResponse: Struct?
        get() = rsp
        set(rsp) {
            this.rsp = rsp
        }

    init {
        this.source = source
        this.variables = variables
        this.path = path
        this.format = format
        this.matrix = matrix
        this.hasFormatExtension = hasFormatExtension
        this.accept = accept
        this.contentType = contentType
    }
}