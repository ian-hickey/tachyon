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

import tachyon.runtime.PageSource

class Source(mapping: Mapping?, pageSource: PageSource?, path: String?) {
    private val mapping: Mapping?
    private val path: Array<Path?>?
    val rawPath: String?
    private val pageSource: PageSource?

    /**
     * @return the pageSource
     */
    fun getPageSource(): PageSource? {
        return pageSource
    }

    /**
     * @return the mapping
     */
    fun getMapping(): Mapping? {
        return mapping
    }

    /**
     * @return the path
     */
    fun getPath(): Array<Path?>? {
        return path
    }

    init {
        this.mapping = mapping
        this.pageSource = pageSource
        this.path = Path.init(path)
        rawPath = path
    }
}