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
package lucee.commons.io.res.filter

import lucee.commons.io.res.Resource

/**
 * This filter produces a logical NOT of the filters specified.
 */
class NotResourceFilter(filter: ResourceFilter) : ResourceFilter {
    private val filter: ResourceFilter
    @Override
    fun accept(f: Resource?): Boolean {
        return !filter.accept(f)
    }

    /**
     * @param filter
     */
    init {
        this.filter = filter
    }
}