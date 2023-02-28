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
package lucee.commons.lang

import java.text.Collator

object ComparatorUtil {
    const val SORT_TYPE_TEXT = 1
    const val SORT_TYPE_TEXT_NO_CASE = 2
    const val SORT_TYPE_NUMBER = 3
    fun toComparator(sortType: Int, orderAsc: Boolean, l: Locale?, defaultValue: Comparator): Comparator {
        // check sortorder
        // text
        return if (sortType == SORT_TYPE_TEXT) {
            if (l != null) toCollator(l, Collator.IDENTICAL, orderAsc) else TextComparator(orderAsc, false)
        } else if (sortType == SORT_TYPE_TEXT_NO_CASE) {
            if (l != null) toCollator(l, Collator.TERTIARY, orderAsc) else TextComparator(orderAsc, true)
        } else if (sortType == SORT_TYPE_NUMBER) {
            NumberComparator(orderAsc)
        } else {
            defaultValue
        }
    }

    private fun toCollator(l: Locale, strength: Int, orderAsc: Boolean): Comparator {
        val c: Collator = Collator.getInstance(l)
        c.setStrength(strength)
        c.setDecomposition(Collator.CANONICAL_DECOMPOSITION)
        return if (!orderAsc) Inverter<Any?>(c) else c
    }

    private class Inverter<T>(c: Collator) : Comparator<T> {
        private val c: Collator
        @Override
        fun compare(o1: T, o2: T): Int {
            return c.compare(o2, o1)
        }

        init {
            this.c = c
        }
    }
}