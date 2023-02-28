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
package lucee.commons.lang.mimetype

import lucee.commons.lang.StringUtil

class ContentType {
    var mimeType: String
        private set
    var charset: String? = null
        private set

    constructor(mimeType: String) {
        this.mimeType = mimeType
    }

    constructor(mimeType: String, charset: String) {
        this.mimeType = mimeType
        setCharset(charset)
    }

    fun setCharset(charset: String) {
        if (!StringUtil.isEmpty(charset, true)) {
            this.charset = charset.trim()
        } else this.charset = null
    }

    @Override
    override fun toString(): String {
        return if (charset == null) mimeType else "$mimeType; charset=$charset"
    }
}