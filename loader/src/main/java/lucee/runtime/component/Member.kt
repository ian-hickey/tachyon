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
package lucee.runtime.component

import java.io.Serializable

interface Member : Serializable {
    /**
     * return the access modifier of this member
     *
     * @return the access
     */
    fun getAccess(): Int

    /**
     * return the value itself
     *
     * @return value
     */
    fun getValue(): Object?

    /**
     * return Member.MODIFIER_FINAL, Member.MODIFIER_ABSTRACT or Member.MODIFIER_NONE
     *
     * @return the modifier.
     */
    fun getModifier(): Int

    companion object {
        const val MODIFIER_NONE = 0
        const val MODIFIER_FINAL = 1
        const val MODIFIER_ABSTRACT = 2
    }
}