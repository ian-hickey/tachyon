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

class FileNameFilterWrapper(filter: FilenameFilter) : FileNameResourceFilter {
    private val filter: FilenameFilter
    @Override
    fun accept(dir: Resource, name: String?): Boolean {
        return if (dir is File) accept(dir as File, name) else accept(FileWrapper.toFile(dir), name)
    }

    @Override
    fun accept(dir: File?, name: String?): Boolean {
        return filter.accept(dir, name)
    }

    init {
        this.filter = filter
    }
}