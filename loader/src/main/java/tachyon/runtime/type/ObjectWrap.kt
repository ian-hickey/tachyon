/**
 * Copyright (c) 2015, Tachyon Assosication Switzerland. All rights reserved.
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

import tachyon.runtime.exp.PageException

/**
 * Wraps another Object
 */
interface ObjectWrap {
    /**
     * returns embedded Object EL
     *
     * @param defaultValue default value
     * @return embedded Object
     */
    fun getEmbededObject(defaultValue: Object?): Object?

    /**
     * returns embedded Object
     *
     * @return embedded Object
     * @throws PageException Page Exception
     */
    @get:Throws(PageException::class)
    val embededObject: Object?
}