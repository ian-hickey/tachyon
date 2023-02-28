/**
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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
package lucee.transformer.expression.`var`

import lucee.transformer.Context

interface Variable : Expression, Invoker {
    fun getScope(): Int

    /**
     * @return the first member or null if there no member
     */
    fun getFirstMember(): Member?

    /**
     * @return the first member or null if there no member
     */
    fun getLastMember(): Member?
    fun ignoredFirstMember(b: Boolean)
    fun ignoredFirstMember(): Boolean
    fun fromHash(fromHash: Boolean)
    fun fromHash(): Boolean
    fun getDefaultValue(): Expression?
    fun setDefaultValue(defaultValue: Expression?)
    fun getAsCollection(): Boolean?
    fun setAsCollection(asCollection: Boolean?)
    fun getCount(): Int

    @Throws(TransformerException::class)
    fun writeOutCollection(c: Context?, mode: Int): Class<*>?
    fun removeMember(index: Int): Member?
    fun assign(assign: Assign?)
    fun assign(): Assign?
}