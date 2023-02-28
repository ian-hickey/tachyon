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
 * comparator implementation, compare to numbers
 */
class NumberComparator
/**
 * constructor of the class
 *
 * @param isAsc is ascendinf or descending
 */ @JvmOverloads constructor(private val isAsc: Boolean, private val allowEmpty: Boolean = false) : Comparator {
    @Override
    fun compare(oLeft: Object?, oRight: Object?): Int {
        return try {
            if (isAsc) compareObjects(oLeft, oRight) else compareObjects(oRight, oLeft)
        } catch (e: PageException) {
            throw PageRuntimeException(ExpressionException("can only sort arrays with simple values", e.getMessage()))
        }
    }

    @Throws(PageException::class)
    private fun compareObjects(oLeft: Object?, oRight: Object?): Int {
        // If we're allowing empty/null values, then run this logic
        var oLeft: Object? = oLeft
        var oRight: Object? = oRight
        if (allowEmpty) {
            oLeft = v(oLeft)
            oRight = v(oRight)
            if (oLeft == null && oRight == null) {
                return 0
            } else if (oLeft == null && oRight != null) {
                return -1
            } else if (oLeft != null && oRight == null) {
                return 1
            }
        }
        // This logic assumes we have two non-null values
        val left: Double = Caster.toDoubleValue(oLeft)
        val right: Double = Caster.toDoubleValue(oRight)
        if (left < right) return -1
        return if (left > right) 1 else 0
    }

    private fun v(value: Object?): Object? {
        return if (value is String && StringUtil.isEmpty(value.toString())) null else value
    }
}