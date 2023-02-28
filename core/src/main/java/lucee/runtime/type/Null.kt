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
package lucee.runtime.type

import lucee.runtime.PageContext

/**
 * Custom Null Type
 */
class Null private constructor() : Castable, Dumpable {
    @Override
    @Throws(ExpressionException::class)
    fun castToString(): String? {
        return ""
    }

    @Override
    fun castToString(defaultValue: String?): String? {
        return ""
    }

    @Override
    @Throws(ExpressionException::class)
    fun castToBooleanValue(): Boolean {
        throw ExpressionException("can't convert null to a boolean")
    }

    @Override
    fun castToBoolean(defaultValue: Boolean?): Boolean? {
        return defaultValue
    }

    @Override
    @Throws(ExpressionException::class)
    fun castToDoubleValue(): Double {
        throw ExpressionException("can't convert null to a numberic value")
    }

    @Override
    fun castToDoubleValue(defaultValue: Double): Double {
        return defaultValue
    }

    @Override
    @Throws(ExpressionException::class)
    fun castToDateTime(): DateTime? {
        throw ExpressionException("can't convert null to a date object")
    }

    @Override
    fun castToDateTime(defaultValue: DateTime?): DateTime? {
        return defaultValue
    }

    @Override
    fun toDumpData(pageContext: PageContext?, maxlevel: Int, dp: DumpProperties?): DumpData? {
        return DumpUtil.toDumpData(null, pageContext, maxlevel, dp)
    }

    @Override
    override fun toString(): String {
        return null
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(str: String?): Int {
        return "".compareTo(str!!)
        // throw new ExpressionException("can't compare null with a string value");
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(b: Boolean): Int {
        throw ExpressionException("can't compare null with a boolean value")
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(d: Double): Int {
        throw ExpressionException("can't compare null with a numeric value")
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(dt: DateTime?): Int {
        throw ExpressionException("can't compare null with a date object")
    }

    companion object {
        val NULL: Null? = Null()
    }
}