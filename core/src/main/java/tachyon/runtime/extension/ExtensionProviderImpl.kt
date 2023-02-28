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
package tachyon.runtime.extension

import java.net.MalformedURLException

class ExtensionProviderImpl : ExtensionProvider {
    // private String name;
    private var url: URL? = null
    private var strUrl: String? = null
    private var readOnly: Boolean

    constructor(url: URL?, readOnly: Boolean) {
        // this.name = name;
        this.url = url
        this.readOnly = readOnly
    }

    constructor(strUrl: String?, readOnly: Boolean) {
        // this.name = name;
        this.strUrl = strUrl
        this.readOnly = readOnly
    }

    /**
     * @return the url
     * @throws MalformedURLException
     */
    @Override
    @Throws(MalformedURLException::class)
    fun getUrl(): URL? {
        if (url == null) url = URL(strUrl)
        return url
    }

    @Override
    fun getUrlAsString(): String? {
        return if (strUrl != null) strUrl else url.toExternalForm()
    }

    @Override
    fun isReadOnly(): Boolean {
        return readOnly
    }

    @Override
    override fun toString(): String {
        return "url:" + getUrlAsString() + ";"
    }

    @Override
    override fun hashCode(): Int {
        return toString().hashCode()
    }

    @Override
    override fun equals(obj: Object?): Boolean {
        // if(!(obj instanceof ExtensionProvider))return false;
        return toString().equals(obj.toString())
    }
}