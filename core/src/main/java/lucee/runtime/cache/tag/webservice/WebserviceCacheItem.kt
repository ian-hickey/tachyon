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
package lucee.runtime.cache.tag.webservice

import java.io.Serializable

class WebserviceCacheItem(data: Object?, url: String?, methodName: String?, executionTimeNS: Long) : CacheItem, Serializable, Dumpable, Duplicable {
    private val data: Object?
    private val url: String?
    private val methodName: String?
    private val executionTimeNS: Long
    @Override
    fun toDumpData(pageContext: PageContext?, maxlevel: Int, properties: DumpProperties?): DumpData? {
        val table = DumpTable("#669999", "#ccffff", "#000000")
        table.setTitle("WebserviceCacheEntry")
        table.appendRow(1, SimpleDumpData("URL"), DumpUtil.toDumpData(SimpleDumpData(url), pageContext, maxlevel, properties))
        table.appendRow(1, SimpleDumpData("Method Name"), DumpUtil.toDumpData(SimpleDumpData(methodName), pageContext, maxlevel, properties))
        return table
    }

    @Override
    override fun toString(): String {
        return data.toString()
    }

    @Override
    fun getHashFromValue(): String? {
        return Long.toString(HashUtil.create64BitHash(data.toString()))
    }

    fun getData(): Object? {
        return data
    }

    @Override
    fun getName(): String? {
        return url.toString() + "&method=" + methodName
    }

    @Override
    fun getPayload(): Long {
        return if (data is Collection) (data as Collection?)!!.size().toLong() else 1
    }

    @Override
    fun getMeta(): String? {
        return url
    }

    @Override
    fun getExecutionTime(): Long {
        return executionTimeNS
    }

    @Override
    fun duplicate(deepCopy: Boolean): Object? {
        return WebserviceCacheItem(Duplicator.duplicate(data, deepCopy), url, methodName, executionTimeNS)
    }

    companion object {
        private const val serialVersionUID = -8462614105941179140L
    }

    init {
        this.data = data
        this.url = url
        this.methodName = methodName
        this.executionTimeNS = executionTimeNS
    }
}