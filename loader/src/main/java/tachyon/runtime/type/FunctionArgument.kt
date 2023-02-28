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
package tachyon.runtime.type

import java.io.Serializable

/**
 * a function argument definition
 */
interface FunctionArgument : Serializable {
    /**
     * @return Returns the name of the argument.
     */
    val name: Collection.Key?

    /**
     * @return Returns if argument is required or not.
     */
    val isRequired: Boolean

    /**
     * @return Returns the type of the argument.
     */
    val type: Short

    /**
     * @return Returns the type of the argument.
     */
    val typeAsString: String?

    /**
     * @return Returns the Hint of the argument.
     */
    val hint: String?

    /**
     * @return Returns the Display name of the argument.
     */
    val displayName: String?

    /**
     * @return the default type of the argument
     */
    val defaultType: Int

    /**
     * @return the meta data defined
     */
    val metaData: tachyon.runtime.type.Struct?
    val isPassByReference: Boolean

    companion object {
        const val DEFAULT_TYPE_NULL = 0
        const val DEFAULT_TYPE_LITERAL = 1
        const val DEFAULT_TYPE_RUNTIME_EXPRESSION = 2
    }
}