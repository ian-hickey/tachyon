/**
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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
package lucee.runtime.extension

import java.net.MalformedURLException

class RHExtensionProvider {
    private val url: URL?
    private val readonly: Boolean

    constructor(strUrl: String?, readonly: Boolean) {
        url = HTTPUtil.toURL(strUrl, HTTPUtil.ENCODED_AUTO)
        this.readonly = readonly
    }

    constructor(url: URL?, readonly: Boolean) {
        this.url = url
        this.readonly = readonly
    }

    fun getURL(): URL? {
        return url
    }

    fun isReadonly(): Boolean {
        return readonly
    }

    @Override
    override fun toString(): String {
        return url.toExternalForm()
    }
}