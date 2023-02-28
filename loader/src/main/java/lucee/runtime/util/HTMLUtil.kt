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
package lucee.runtime.util

import java.net.URL

interface HTMLUtil {
    /**
     * escapes html character inside a string
     *
     * @param str html code to escape
     * @return escaped html code
     */
    fun escapeHTML(str: String?): String?

    /**
     * escapes html character inside a string
     *
     * @param str html code to escape
     * @param version HTML Version ()
     * @return escaped html code
     */
    fun escapeHTML(str: String?, version: Short): String?

    /**
     * unescapes html character inside a string
     *
     * @param str html code to unescape
     * @return unescaped html code
     */
    fun unescapeHTML(str: String?): String?

    /**
     * returns all urls in a html String
     *
     * @param html HTML String to search urls
     * @param url Absolute URL path to set
     * @return urls found in html String
     */
    fun getURLS(html: String?, url: URL?): List<URL?>?

    companion object {
        const val HTMLV20: Short = 1
        const val HTMLV32: Short = 1
        const val HTMLV40: Short = 2
    }
}