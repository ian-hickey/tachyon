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
package lucee.transformer.bytecode.statement.tag

import lucee.transformer.expression.Expression

class Attribute(private val dynamicType: Boolean, val nameOC: String?, value: Expression?, type: String?, isDefaultValue: Boolean) {
    val nameLC: String?
    val value: Expression?
    private val type: String?
    private var defaultAttribute = false
    private val setterName: String? = null
    private val isDefaultValue: Boolean

    constructor(dynamicType: Boolean, name: String?, value: Expression?, type: String?) : this(dynamicType, name, value, type, false) {}

    fun isDefaultValue(): Boolean {
        return isDefaultValue
    }

    fun isDefaultAttribute(): Boolean {
        return defaultAttribute
    }

    fun setDefaultAttribute(defaultAttribute: Boolean) {
        this.defaultAttribute = defaultAttribute
    }

    /**
     * @return the name
     */
    fun getName(): String? {
        return nameLC
    }

    // TODO make this method obsolete
    fun getNameOC(): String? {
        return nameOC
    }

    /**
     * @return the value
     */
    fun getValue(): Expression? {
        return value
    }

    /**
     * @return the type
     */
    fun getType(): String? {
        return type
    }

    /**
     * @return the dynamicType
     */
    fun isDynamicType(): Boolean {
        return dynamicType
    }

    @Override
    override fun toString(): String {
        return "name:" + nameLC + ";value:" + value + ";type:" + type + ";dynamicType:" + dynamicType + ";setterName:" + setterName
    }

    init {
        nameLC = nameOC.toLowerCase()
        this.value = value
        this.type = type
        this.isDefaultValue = isDefaultValue
    }
}