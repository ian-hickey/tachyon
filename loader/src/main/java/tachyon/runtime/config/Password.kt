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
package tachyon.runtime.config

import kotlin.Throws
import tachyon.loader.util.Util
import kotlin.jvm.Synchronized
import kotlin.jvm.Transient
import org.osgi.framework.Version

interface Password {
    fun getPassword(): String?
    fun getSalt(): String?
    fun getType(): Int
    fun getOrigin(): Int
    fun isEqual(config: Config?, otherPassword: String?): Password?

    companion object {
        const val HASHED = 1
        const val HASHED_SALTED = 2
        const val ORIGIN_ENCRYPTED = 3
        const val ORIGIN_HASHED = 4
        const val ORIGIN_HASHED_SALTED = 5
        const val ORIGIN_UNKNOW = 6
    }
}