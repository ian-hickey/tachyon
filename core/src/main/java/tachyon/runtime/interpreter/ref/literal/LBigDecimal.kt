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
class LBigDecimal : Ref {
    private var literal: BigDecimal?

    /**
     * constructor of the class
     *
     * @param literal
     */
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

    fun getBigDecimal(): BigDecimal? {
        return literal
    }

    @Override
    fun getValue(pc: PageContext?): Object? {
        return literal
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
    override fun toString(): String {
        return literal.toString()
    }

    @Override
    @Throws(PageException::class)
    fun eeq(pc: PageContext?, other: Ref?): Boolean {
        return RefUtil.eeq(pc, this, other)
    }

    companion object {
        val ZERO: LBigDecimal? = LBigDecimal(BigDecimal.ZERO)
        val ONE: LBigDecimal? = LBigDecimal(BigDecimal.ONE)
    }
}