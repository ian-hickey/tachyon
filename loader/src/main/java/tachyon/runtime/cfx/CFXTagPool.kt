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
package tachyon.runtime.cfx

import java.util.Map

/**
 * Pool for cfx tags
 */
interface CFXTagPool {
    /**
     * @return Returns the classes.
     */
    val classes: Map<String?, Any?>?

    /**
     * return custom tag that match the name
     *
     * @param name custom tag name
     * @return matching tag
     * @throws CFXTagException CFX Tag Exception
     */
    @Throws(CFXTagException::class)
    fun getCustomTag(name: String?): CustomTag?

    @Throws(CFXTagException::class)
    fun getCFXTagClass(name: String?): CFXTagClass?

    /**
     * realese custom tag
     *
     * @param ct Custom Tag
     */
    fun releaseCustomTag(ct: CustomTag?)
    fun releaseTag(tag: Object?)
}