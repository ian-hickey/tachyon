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
package tachyon.runtime.tag

import tachyon.runtime.exp.ApplicationException

/**
 * Tests for a parameter's existence, tests its data type, and provides a default value if one is
 * not assigned.
 *
 *
 *
 */
class Param : TagImpl() {
    /** The type of parameter that is required. The default is 'any'.  */
    private var type: String? = "any"

    /** Default value to set the parameter to if it does not exist.  */
    private var _default: Object? = null

    /**
     * The name of the parameter to test, such as Client.Email or Cookie.BackgroundColor. If you omit
     * the DEFAULT attribute, an error occurs if the specified parameter does not exist
     */
    private var name: String? = null
    private var min = 0.0
    private var max = 0.0
    private var pattern: String? = null
    @Override
    fun release() {
        super.release()
        type = "any"
        _default = null
        name = null
        min = -1.0
        max = -1.0
        pattern = null
    }

    /**
     * set the value type The type of parameter that is required. The default is 'any'.
     *
     * @param type value to set
     */
    fun setType(type: String?) {
        this.type = type.trim().toLowerCase()
    }

    /**
     * set the value default Default value to set the parameter to if it does not exist.
     *
     * @param _default value to set
     */
    fun setDefault(_default: Object?) {
        this._default = _default
    }

    /**
     * @param max the max to set
     */
    fun setMax(max: Double) {
        this.max = max
    }

    /**
     * @param min the min to set
     */
    fun setMin(min: Double) {
        this.min = min
    }

    /**
     * @param pattern the pattern to set
     */
    fun setPattern(pattern: String?) {
        this.pattern = pattern
    }

    /**
     * set the value name The name of the parameter to test, such as Client.Email or
     * Cookie.BackgroundColor. If you omit the DEFAULT attribute, an error occurs if the specified
     * parameter does not exist
     *
     * @param name value to set
     */
    fun setName(name: String?) {
        this.name = name
    }

    @Override
    @Throws(PageException::class)
    fun doStartTag(): Int {
        if ("range".equals(type)) pageContext.param(type, name, _default, min, max) else if ("regex".equals(type) || "regular_expression".equals(type)) pageContext.param(type, name, _default, pattern) else pageContext.param(type, name, _default)
        return SKIP_BODY
    }

    init {
        throw ApplicationException("this Tag Implementation is deprecated and replaced with a Translation Time Transformer")
    }
}