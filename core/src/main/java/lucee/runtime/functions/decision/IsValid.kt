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
package lucee.runtime.functions.decision

import lucee.runtime.PageContext

/**
 *
 */
object IsValid : Function {
    private const val serialVersionUID = -1383105304624662986L

    /**
     * check for many diff types
     *
     * @param pc
     * @param type
     * @param value
     * @return
     * @throws ExpressionException
     */
    @Throws(ExpressionException::class)
    fun call(pc: PageContext?, type: String?, value: Object?): Boolean {
        var type = type
        type = type.trim()
        if ("range".equalsIgnoreCase(type)) throw FunctionException(pc, "isValid", 1, "type", "for [range] you have to define a min and max value")
        if ("regex".equalsIgnoreCase(type) || "regular_expression".equalsIgnoreCase(type)) throw FunctionException(pc, "isValid", 1, "type", "for [regex] you have to define a pattern")
        return Decision.isValid(type, value)
    }

    /**
     * regex check
     *
     * @param pc
     * @param type
     * @param value
     * @param objPattern
     * @return
     * @throws PageException
     */
    @Throws(PageException::class)
    fun call(pc: PageContext?, type: String?, value: Object?, objPattern: Object?): Boolean {
        var type = type
        type = type.trim()
        if (!"regex".equalsIgnoreCase(type) && !"regular_expression".equalsIgnoreCase(type)) throw FunctionException(pc, "isValid", 1, "type", "wrong attribute count for type [$type]")
        return regex(pc, Caster.toString(value, null), Caster.toString(objPattern))
    }

    fun regex(pc: PageContext?, value: String?, strPattern: String?): Boolean {
        return if (value == null) false else (pc as PageContextImpl?).getRegex().matches(strPattern, value, false)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, type: String?, value: Object?, objMin: Object?, objMax: Object?): Boolean {

        // for named argument calls
        var type = type
        if (objMax == null) {
            return if (objMin == null) call(pc, type, value) else call(pc, type, value, objMin)
        }
        type = type.trim().toLowerCase()

        // numeric
        return if ("range".equals(type) || "integer".equals(type) || "float".equals(type) || "numeric".equals(type) || "number".equals(type)) {
            val number: Double = Caster.toDoubleValue(value, true, Double.NaN)
            if (!Decision.isValid(number)) return false
            val min = toRangeNumber(pc, objMin, 3, "min")
            val max = toRangeNumber(pc, objMax, 4, "max")
            number >= min && number <= max
        } else if ("string".equals(type)) {
            val str: String = Caster.toString(value, null) ?: return false
            val min = toRangeNumber(pc, objMin, 3, "min")
            val max = toRangeNumber(pc, objMax, 4, "max")
            str.length() >= min && str.length() <= max
        } else throw FunctionException(pc, "isValid", 1, "type", "wrong attribute count for type [$type]")
    }

    @Throws(FunctionException::class)
    private fun toRangeNumber(pc: PageContext?, objMin: Object?, index: Int, name: String?): Double {
        val d: Double = Caster.toDoubleValue(objMin, false, Double.NaN)
        if (!Decision.isValid(d)) throw FunctionException(pc, "isValid", index, name, "value must be numeric")
        return d
    }
}