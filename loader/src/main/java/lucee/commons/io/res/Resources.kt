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
package lucee.commons.io.res

import kotlin.Throws
import lucee.loader.util.Util
import kotlin.jvm.Synchronized
import kotlin.jvm.Transient
import org.osgi.framework.Version

interface Resources {
    /**
     * adds a default factory, this factory is used, when shemecan't be mapped to another factory
     *
     * @param provider resource provider to register
     */
    fun registerDefaultResourceProvider(provider: ResourceProvider?)

    /**
     * adds an additional resource to System
     *
     * @param provider resource provider to register
     */
    fun registerResourceProvider(provider: ResourceProvider?)

    /**
     * returns a resource that matching the given path
     *
     * @param path path to resource
     * @return matching resource
     */
    fun getResource(path: String?): Resource?

    /**
     * @return the defaultResource
     */
    val defaultResourceProvider: lucee.commons.io.res.ResourceProvider?
    val resourceProviders: Array<lucee.commons.io.res.ResourceProvider?>?
    fun createResourceLock(timeout: Long, caseSensitive: Boolean): ResourceLock?
    fun reset()
}