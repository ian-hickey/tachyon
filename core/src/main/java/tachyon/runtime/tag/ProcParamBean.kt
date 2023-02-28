/**
 * Copyright (c) 2023, TachyonCFML.org
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
package tachyon.runtime.tag

import java.sql.Types

class ProcParamBean : SQLItem {
    private var direction = DIRECTION_IN
    private var variable: String? = null
    private var value: Object? = null
    /**
     * @return Returns the cfsqltype.
     */
    /**
     * @param cfsqltype The cfsqltype to set.
     */
    @get:Override
    @set:Override
    var type: Int = Types.VARCHAR
    private var maxLength = 0
    private var scale = 0
    /**
     * @return Returns the ignoreNull.
     */
    /**
     * @param ignoreNull The ignoreNull to set.
     */
    var `null` = false
    private var index = -1

    /**
     * @return Returns the maxLength.
     */
    fun getMaxLength(): Int {
        return maxLength
    }

    /**
     * @param maxLength The maxLength to set.
     */
    fun setMaxLength(maxLength: Int) {
        this.maxLength = maxLength
    }

    /**
     * @return Returns the scale.
     */
    @Override
    fun getScale(): Int {
        return scale
    }

    /**
     * @param scale The scale to set.
     */
    @Override
    fun setScale(scale: Int) {
        this.scale = scale
    }

    /**
     * @return Returns the type.
     */
    fun getDirection(): Int {
        return direction
    }

    /**
     * @param type The type to set.
     */
    fun setDirection(direction: Int) {
        this.direction = direction
    }

    /**
     * @return Returns the value.
     */
    @Override
    fun getValue(): Object? {
        return if (`null`) null else value
    }

    /**
     * @param value The value to set.
     */
    @Override
    fun setValue(value: Object?) {
        this.value = value
    }

    /**
     * @return Returns the variable.
     */
    fun getVariable(): String? {
        return variable
    }

    /**
     * @param variable The variable to set.
     */
    fun setVariable(variable: String?) {
        this.variable = variable
    }

    /**
     * @return Returns the index.
     */
    fun getIndex(): Int {
        return index
    }

    /**
     * @param index The index to set.
     */
    fun setIndex(index: Int) {
        this.index = index
    }

    @Override
    fun clone(`object`: Object?): SQLItem? {
        val ppb = ProcParamBean()
        ppb.direction = direction
        ppb.variable = variable
        ppb.value = value
        ppb.type = type
        ppb.maxLength = maxLength
        ppb.scale = scale
        ppb.`null` = `null`
        ppb.index = index
        return ppb
    }

    @get:Throws(PageException::class)
    @get:Override
    val valueForCF: Object?
        get() = SQLCaster.toCFTypex(this)

    // TODO impl
    @get:Override
    @set:Override
    var isNulls: Boolean
        get() = (getValue() == null
                || type != Types.VARCHAR && type != Types.LONGVARCHAR && type != Types.NVARCHAR && getValue() is String && StringUtil.isEmpty(getValue()))
        set(nulls) {
            // TODO impl
        }

    // TODO impl
    @get:Override
    val isValueSet: Boolean
        get() = value != null || `null` // TODO impl

    companion object {
        const val DIRECTION_IN = 0
        const val DIRECTION_OUT = 1
        const val DIRECTION_INOUT = 3
    }
}