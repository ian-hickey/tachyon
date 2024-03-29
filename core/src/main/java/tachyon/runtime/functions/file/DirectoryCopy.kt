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
package tachyon.runtime.functions.file

import tachyon.commons.io.res.Resource

object DirectoryCopy : Function {
    private const val serialVersionUID = -8591512197642527401L
    @Throws(PageException::class)
    fun call(pc: PageContext?, source: String?, destination: String?): String? {
        return call(pc, source, destination, false, null, true)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, source: String?, destination: String?, recurse: Boolean): String? {
        return call(pc, source, destination, recurse, null, true)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, source: String?, destination: String?, recurse: Boolean, filter: Object?): String? {
        return call(pc, source, destination, recurse, filter, true)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, source: String?, destination: String?, recurse: Boolean, filter: Object?, createPath: Boolean): String? {
        val src: Resource = ResourceUtil.toResourceNotExisting(pc, source)
        val fi: ResourceAndResourceNameFilter? = if (filter == null) null else UDFFilter.createResourceAndResourceNameFilter(filter)
        Directory.actionCopy(pc, src, destination, null, createPath, null, null, fi, recurse, Directory.NAMECONFLICT_DEFAULT)
        return null
    }
}