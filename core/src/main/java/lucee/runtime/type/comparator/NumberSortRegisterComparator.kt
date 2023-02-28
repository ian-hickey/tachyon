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

import lucee.commons.lang.StringUtil

/**
 * Implementation of a Comparator, compares to Softregister Objects
 */
class NumberSortRegisterComparator
/**
 * constructor of the class
 *
 * @param isAsc is ascendinf or descending
 */(private val isAsc: Boolean) : ExceptionComparator {
    private var pageException: PageException? = null

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
            if (pageException != null) 0 else if (isAsc) compareObjects(oLeft, oRight) else compareObjects(oRight, oLeft)
        } catch (e: PageException) {
            pageException = e
            0
        }
    }

    @Throws(PageException::class)
    private fun compareObjects(oLeft: Object?, oRight: Object?): Int {
        /*
		 * return Operator.compare( ((SortRegister)oLeft).getValue(), ((SortRegister)oRight).getValue() );
		 */
        return OpUtil.compare(ThreadLocalPageContext.get(), Caster.toNumber(v((oLeft as SortRegister?)!!.getValue())), Caster.toNumber(v((oRight as SortRegister?)!!.getValue())))
    }

    private fun v(value: Object?): Object? {
        return if (value is String && StringUtil.isEmpty(value.toString())) null else value
    }
}