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
package tachyon.runtime.db

import org.osgi.framework.BundleException

interface ClassDefinition<T> {
    @get:Throws(ClassException::class, BundleException::class)
    val clazz: Class<T>?
    fun getClazz(defaultValue: Class<T>?): Class<T>?
    fun hasClass(): Boolean
    val isBundle: Boolean
    fun hasVersion(): Boolean
    fun isClassNameEqualTo(otherClassName: String?): Boolean
    fun isClassNameEqualTo(otherClassName: String?, ignoreCase: Boolean): Boolean
    val className: String?
    val name: String?
    val version: Version?
    val versionAsString: String?
    val id: String?
}