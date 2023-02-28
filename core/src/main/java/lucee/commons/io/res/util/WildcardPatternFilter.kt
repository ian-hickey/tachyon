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
package lucee.commons.io.res.util

import lucee.commons.io.SystemUtil

class WildcardPatternFilter(patt: String, ignoreCase: Boolean, patternDelimiters: String?) : ResourceAndResourceNameFilter {
    private val matcher: WildcardPattern

    constructor(pattern: String, patternDelimiters: String?) : this(pattern, SystemUtil.isWindows(), patternDelimiters) {}

    @Override
    fun accept(res: Resource): Boolean {
        return matcher.isMatch(res.getName())
    }

    @Override
    fun accept(res: Resource?, name: String): Boolean {
        return matcher.isMatch(name)
    }

    fun accept(name: String): Boolean {
        return matcher.isMatch(name)
    }

    @Override
    override fun toString(): String {
        return matcher.toString()
    }

    init {
        matcher = WildcardPattern(patt, !ignoreCase, patternDelimiters)
    }
}