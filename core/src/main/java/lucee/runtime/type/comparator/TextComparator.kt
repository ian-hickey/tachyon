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
package lucee.runtime.type.comparator

import java.util.Comparator

/**
 * comparator implementation to compare textes
 */
class TextComparator
/**
 * constructor of the class
 *
 * @param isAsc ascending or desending
 * @param ignoreCase ignore case or not
 */(private val isAsc: Boolean, private val ignoreCase: Boolean) : Comparator {
    @Override
    fun compare(oLeft: Object?, oRight: Object?): Int {
        return try {
            if (isAsc) _compare(oLeft, oRight) else _compare(oRight, oLeft)
        } catch (e: PageException) {
            throw PageRuntimeException(ExpressionException("can only sort arrays with simple values", e.getMessage()))
        }
    }

    @Throws(PageException::class)
    private fun _compare(oLeft: Object?, oRight: Object?): Int {
        return if (ignoreCase) Caster.toString(oLeft).compareToIgnoreCase(Caster.toString(oRight)) else Caster.toString(oLeft).compareTo(Caster.toString(oRight))
    }

    @Override
    override fun toString(): String {
        return "TextComparator:isAsc$isAsc;ignoreCase:$ignoreCase"
    }
}