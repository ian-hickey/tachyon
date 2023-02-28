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
package tachyon.commons.io.res.util

import java.io.File

class FileFilterWrapper(fileFilter: FileFilter) : FileResourceFilter {
    private val filter: FileFilter
    @Override
    fun accept(res: Resource): Boolean {
        return if (res is File) accept(res as File) else accept(FileWrapper.toFile(res))
    }

    @Override
    fun accept(pathname: File?): Boolean {
        return filter.accept(pathname)
    }

    init {
        filter = fileFilter
    }
}