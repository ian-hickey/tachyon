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
package tachyon.commons.res.io.filter

import tachyon.commons.io.res.Resource

/**
 * A FileFilter providing conditional AND logic across a list of file filters. This filter returns
 * true if all filters in the list return true. Otherwise, it returns false. Checking of the file
 * filter list stops when the first filter returns false.
 */
class AndFileFilter(filters: Array<ResourceFilter>) : ResourceFilter {
    private val filters: Array<ResourceFilter>
    @Override
    fun accept(res: Resource?): Boolean {
        for (i in filters.indices) {
            if (!filters[i].accept(res)) return false
        }
        return true
    }

    /**
     * @param filters
     */
    init {
        this.filters = filters
    }
}