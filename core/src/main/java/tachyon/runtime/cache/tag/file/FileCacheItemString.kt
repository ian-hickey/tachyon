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
package tachyon.runtime.cache.tag.file

import tachyon.commons.digest.HashUtil

class FileCacheItemString(path: String?, val data: String?, executionTimeNS: Long) : FileCacheItem(path, executionTimeNS), Duplicable {
    @Override
    override fun toString(): String {
        return data!!
    }

    @Override
    fun getHashFromValue(): String? {
        return Long.toString(HashUtil.create64BitHash(data!!))
    }

    @Override
    fun getPayload(): Long {
        return data!!.length()
    }

    @Override
    override fun getData(): String? {
        return data
    }

    @Override
    fun duplicate(deepCopy: Boolean): Object? {
        return FileCacheItemString(path, data, getExecutionTime())
    }

    companion object {
        private const val serialVersionUID = 1655467049819824671L
    }
}