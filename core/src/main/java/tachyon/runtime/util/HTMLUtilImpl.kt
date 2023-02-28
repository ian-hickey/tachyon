/**
 * Copyright (c) 2015, Tachyon Assosication Switzerland. All rights reserved.
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
package tachyon.runtime.util

import java.net.URL

class HTMLUtilImpl : HTMLUtil {
    @Override
    fun escapeHTML(str: String?): String? {
        return HTMLEntities.escapeHTML(str)
    }

    @Override
    fun escapeHTML(str: String?, version: Short): String? {
        return HTMLEntities.escapeHTML(str, version)
    }

    @Override
    fun unescapeHTML(str: String?): String? {
        return HTMLEntities.unescapeHTML(str)
    }

    @Override
    fun getURLS(html: String?, url: URL?): List<URL?>? {
        val hu: tachyon.commons.lang.HTMLUtil = HTMLUtil()
        return hu!!.getURLS(html, url)
    }
}