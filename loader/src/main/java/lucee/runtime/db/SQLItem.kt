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
package lucee.runtime.db

import lucee.runtime.exp.PageException

/**
 * an Item of a SQL Statement
 */
interface SQLItem {
    /**
     * @return Returns the nulls.
     */
    /**
     * @param nulls The nulls to set.
     */
    var isNulls: Boolean
    /**
     * @return Returns the scale.
     */
    /**
     * @param scale The scale to set.
     */
    var scale: Int
    /**
     * @return Returns the value.
     */
    /**
     * @param value The value to set.
     */
    var value: Object?
    /**
     * @return Returns the cfsqltype.
     */
    /**
     * @param type The cfsqltype to set.
     */
    var type: Int

    /**
     * @param object object
     * @return cloned SQL Item
     */
    fun clone(`object`: Object?): SQLItem?

    /**
     * @return CF compatible Type
     * @throws PageException Page Exception
     */
    @get:Throws(PageException::class)
    val valueForCF: Object?

    /**
     * @return Returns the isValueSet.
     */
    val isValueSet: Boolean
}