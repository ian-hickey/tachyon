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
package tachyon.runtime.interpreter.ref.op

import java.math.BigDecimal

/**
 * Plus operation
 */
abstract class Big(left: Ref?, right: Ref?, limited: Boolean) : RefSupport(), Ref {
    private val right: Ref?
    private val left: Ref?
    protected var limited: Boolean
    @Throws(PageException::class)
    protected fun getLeft(pc: PageContext?): BigDecimal? {
        return toBigDecimal(pc, left)
    }

    @Throws(PageException::class)
    protected fun getRight(pc: PageContext?): BigDecimal? {
        return toBigDecimal(pc, right)
    }

    @Override
    fun getTypeName(): String? {
        return "operation"
    }

    companion object {
        @Throws(PageException::class)
        protected fun toBigDecimal(pc: PageContext?, ref: Ref?): BigDecimal? {
            return if (ref is LBigDecimal) (ref as LBigDecimal?).getBigDecimal() else Caster.toBigDecimal(ref.getValue(pc))
        }
    }

    /**
     * constructor of the class
     *
     * @param left
     * @param right
     */
    init {
        this.left = left
        this.right = right
        this.limited = limited
    }
}