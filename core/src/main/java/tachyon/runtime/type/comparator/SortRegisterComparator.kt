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
package tachyon.runtime.type.comparator

import java.util.Comparator

/**
 * Implementation of a Comparator, compares to Softregister Objects
 */
class SortRegisterComparator(pc: PageContext?, isAsc: Boolean, private val ignoreCase: Boolean, localeSensitive: Boolean) : ExceptionComparator {
    private var pageException: PageException? = null
    private val comparator: Comparator?

    /**
     * @return Returns the expressionException.
     */
    @Override
    override fun getPageException(): PageException? {
        return pageException
    }

    @Override
    fun compare(oLeft: Object?, oRight: Object?): Int {
        return try {
            if (pageException != null) 0 else comparator.compare(Caster.toString((oLeft as SortRegister?)!!.getValue()), Caster.toString((oRight as SortRegister?)!!.getValue()))
        } catch (e: PageException) {
            pageException = e
            0
        }
    }

    /**
     * constructor of the class
     *
     * @param isAsc is ascending or descending
     * @param ignoreCase do ignore case
     */
    init {
        comparator = ComparatorUtil.toComparator(if (ignoreCase) ComparatorUtil.SORT_TYPE_TEXT_NO_CASE else ComparatorUtil.SORT_TYPE_TEXT, isAsc,
                if (localeSensitive) ThreadLocalPageContext.getLocale(pc) else null, null)
    }
}