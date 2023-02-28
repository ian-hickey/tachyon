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

object DirectoryCreate {
    @Throws(PageException::class)
    fun call(pc: PageContext?, path: String?): String? {
        return call(pc, path, true)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, path: String?, createPath: Boolean): String? {
        return call(pc, path, createPath, false)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, path: String?, createPath: Boolean, ignoreExists: Boolean): String? {
        val dir: Resource = ResourceUtil.toResourceNotExisting(pc, path)
        Directory.actionCreate(pc, dir, null, createPath, -1, null, null, if (ignoreExists) FileUtil.NAMECONFLICT_SKIP else FileUtil.NAMECONFLICT_ERROR)
        return null
    }
}