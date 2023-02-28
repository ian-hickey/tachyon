/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Tachyon Assosication Switzerland
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
package tachyon.runtime.ext.tag

import tachyon.runtime.type.Collection

interface TagMetaDataAttr {
    /**
     * A description of the attribute.
     *
     * @return the description of the attribute
     */
    val description: String?

    /**
     * The runtime type of the attribute's value For example:String,Number,Boolean,Object,...
     *
     * @return the type of the attribute
     */
    val type: String?

    /**
     * The unique name of the attribute being declared
     *
     * @return the name of the attribute
     */
    val name: Collection.Key?

    /**
     * return the default value for this attribute or null if no default value is defined
     *
     * @return the default value of the attribute
     */
    val defaultVaue: String?

    /**
     * Whether the attribute is required.
     *
     * @return is required
     */
    val isRequired: Boolean

    /**
     * Whether the attribute's value can be dynamically calculated at runtime.
     *
     * @return is a runtime expression
     */
    val isRuntimeExpressionValue: Boolean
}