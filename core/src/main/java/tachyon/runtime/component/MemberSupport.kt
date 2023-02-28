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
package tachyon.runtime.component

import java.io.Serializable

abstract class MemberSupport : Serializable, Member, Duplicable {
    private var access: Int
    private val modifier: Int

    /**
     * Constructor of the class
     *
     * @param access
     * @param value
     */
    constructor(access: Int) {
        this.access = access
        modifier = Member.MODIFIER_NONE
    }

    /**
     * Constructor of the class
     *
     * @param access
     * @param value
     */
    constructor(access: Int, modifier: Int) {
        this.access = access
        this.modifier = modifier
    }

    @Override
    fun getModifier(): Int {
        return modifier
    }

    @Override
    fun getAccess(): Int {
        return access
    }

    /**
     * @param access
     */
    fun setAccess(access: Int) {
        this.access = access
    }

    /**
     * @param access the access to set
     * @throws ExpressionException
     */
    @Throws(ApplicationException::class)
    fun setAccess(access: String?) {
        this.access = ComponentUtil.toIntAccess(access)
    }
}