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
package tachyon.runtime.component

import java.io.Serializable
/**
 *
 */
interface Property : Serializable, Member {
    /**
     * @return the _default
     */
    fun getDefault(): String?

    /**
     * @return the displayname
     */
    fun getDisplayname(): String?

    /**
     * @return the hint
     */
    fun getHint(): String?

    /**
     * @return the name
     */
    fun getName(): String?

    /**
     * @return the required
     */
    fun isRequired(): Boolean

    /**
     * @return the type
     */
    fun getType(): String?

    /**
     * @return the setter
     */
    fun getSetter(): Boolean

    /**
     * @return the getter
     */
    fun getGetter(): Boolean
    fun getMetaData(): Object?
    fun getMeta(): Struct?
    fun getClazz(): Class<*>?
    fun isPeristent(): Boolean
    fun getOwnerName(): String?
    fun getDynamicAttributes(): Struct?
}