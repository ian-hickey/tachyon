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

import tachyon.runtime.PageContext

/**
 * constructor of the class
 */
class LBoolean : RefSupport, Literal {
    private var literal: Boolean?

    /**
     * constructor with Boolean
     *
     * @param literal
     */
    constructor(literal: Boolean?) {
        this.literal = literal
    }

    /**
     * constructor with boolean
     *
     * @param b
     */
    constructor(b: Boolean) {
        literal = if (b) Boolean.TRUE else Boolean.FALSE
    }

    /**
     * constructor with boolean
     *
     * @param str
     * @throws PageException
     */
    constructor(str: String?) {
        literal = Caster.toBoolean(str)
    }

    @Override
    fun getValue(pc: PageContext?): Object? {
        return literal
    }

    @Override
    fun getTypeName(): String? {
        return "literal"
    }

    @Override
    override fun getString(pc: PageContext?): String? {
        return toString()
    }

    @Override
    override fun toString(): String {
        return Caster.toString(literal.booleanValue())
    }

    @Override
    @Throws(PageException::class)
    fun eeq(pc: PageContext?, other: Ref?): Boolean {
        return if (other is LNumber) {
            literal.booleanValue() === (other as tachyon.runtime.interpreter.ref.literal.LBoolean?).literal.booleanValue()
        } else RefUtil.eeq(pc, this, other)
    }

    companion object {
        /**
         * Field `TRUE`
         */
        val TRUE: Ref? = LBoolean(Boolean.TRUE)

        /**
         * Field `FALSE`
         */
        val FALSE: Ref? = LBoolean(Boolean.FALSE)
    }
}