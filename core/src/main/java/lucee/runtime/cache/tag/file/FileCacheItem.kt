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

import java.io.Serializable

abstract class FileCacheItem(protected val path: String?, private val executionTimeNS: Long) : CacheItem, Serializable, Dumpable {
    @Override
    fun toDumpData(pageContext: PageContext?, maxlevel: Int, properties: DumpProperties?): DumpData? {
        val table = DumpTable("#669999", "#ccffff", "#000000")
        table.setTitle("FileCacheEntry")
        table.appendRow(1, SimpleDumpData("Path"), SimpleDumpData(path))
        return table
    }

    @Override
    fun getName(): String? {
        return path
    }

    @Override
    fun getMeta(): String? {
        return path
    }

    @Override
    fun getExecutionTime(): Long {
        return executionTimeNS
    }

    abstract fun getData(): Object?

    companion object {
        private const val serialVersionUID = -8462614105941179140L
        fun getInstance(path: String?, data: Object?, executionTimeNS: Long): FileCacheItem? {
            return if (data is ByteArray) FileCacheItemBinary(path, data as ByteArray?, executionTimeNS) else FileCacheItemString(path, data as String?, executionTimeNS)
        }
    }
}