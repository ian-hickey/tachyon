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
package lucee.runtime.ext.tag

import lucee.runtime.tag.MissingAttribute

class TagMetaDataAttrImpl(name: Collection.Key?, alias: Array<String?>?, required: Boolean, type: String?, isRuntimeExpressionValue: Boolean, defaultValue: String?, description: String?) : MissingAttribute(name, type, alias), TagMetaDataAttr {
    @get:Override
    val description: String? = null

    @get:Override
    var isRequired = false

    @get:Override
    val isRuntimeExpressionValue = false

    @get:Override
    var defaultVaue: String? = null

    /**
     * Constructor of the class
     *
     * @param name
     * @param required
     * @param type
     */
    constructor(name: String?, alias: Array<String?>?, required: Boolean, type: String?, isRuntimeExpressionValue: Boolean, defaultValue: String?, description: String?) : this(KeyImpl.getInstance(name), alias, required, type, isRuntimeExpressionValue, defaultValue, description) {}

    /**
     * Constructor of the class
     *
     * @param name
     * @param required
     * @param type
     * @param description
     */
    init {
        isRequired = required
        this.description = description
        defaultVaue = defaultValue
        this.isRuntimeExpressionValue = isRuntimeExpressionValue
    }
}