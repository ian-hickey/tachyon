/**
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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
package lucee.runtime.cache.tag.file

import java.io.IOException

class FileCacheItemBinary(path: String?, val data: ByteArray?, executionTimeNS: Long) : FileCacheItem(path, executionTimeNS), Duplicable {
    @Override
    override fun toString(): String {
        return Base64Coder.encode(data)
    }

    @Override
    fun getHashFromValue(): String? {
        return try {
            MD5.getDigestAsString(data)
        } catch (e: IOException) {
            Long.toString(HashUtil.create64BitHash(toString()))
        }
    }

    @Override
    fun getPayload(): Long {
        return data!!.size.toLong()
    }

    @Override
    override fun getData(): ByteArray? {
        return data
    }

    @Override
    fun duplicate(deepCopy: Boolean): Object? {
        if (data != null) {
            val tmp = ByteArray(data.size)
            for (i in data.indices) {
                tmp[i] = data[i]
            }
            return FileCacheItemBinary(path, tmp, getExecutionTime())
        }
        return FileCacheItemBinary(path, data, getExecutionTime())
    }

    companion object {
        private const val serialVersionUID = -7426486016811317332L
    }
}