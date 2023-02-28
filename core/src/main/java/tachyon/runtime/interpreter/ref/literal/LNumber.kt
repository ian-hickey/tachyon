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
package tachyon.runtime.interpreter.ref.literal

import java.math.BigDecimal

/**
 * Literal Number
 */
class LNumber : Literal {
    private var literal: BigDecimal?

    constructor(literal: BigDecimal?) {
        this.literal = literal
    }

    /**
     * constructor of the class
     *
     * @param literal
     * @throws PageException
     */
    constructor(literal: String?) {
        this.literal = Caster.toBigDecimal(literal)
    }

    @Override
    fun getValue(pc: PageContext?): Object? {
        return if (!AppListenerUtil.getPreciseMath(pc, null)) Double.valueOf(literal.doubleValue()) else literal
    }

    @Override
    fun getCollection(pc: PageContext?): Object? {
        return getValue(pc)
    }

    @Override
    fun getTypeName(): String? {
        return "number"
    }

    @Override
    fun touchValue(pc: PageContext?): Object? {
        return getValue(pc)
    }

    @Override
    override fun getString(pc: PageContext?): String? {
        return toString()
    }

    @Override
    override fun toString(): String {
        return Caster.toString(literal)
    }

    @Override
    @Throws(PageException::class)
    fun eeq(pc: PageContext?, other: Ref?): Boolean {
        return if (other is LNumber) {
            getValue(pc).equals((other as LNumber?)!!.getValue(pc)) // doing the methods here to have precisemath flag
        } else RefUtil.eeq(pc, this, other)
    }

    companion object {
        val ZERO: LNumber? = LNumber(BigDecimal.ZERO)
        val ONE: LNumber? = LNumber(BigDecimal.ONE)
        val MINUS_ONE: LNumber? = LNumber(BigDecimal.valueOf(Double.valueOf(-1)))
    }
}